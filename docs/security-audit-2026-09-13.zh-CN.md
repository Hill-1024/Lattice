# Lattice 安全审计报告 — 2026-09-13

> **提交前勘误（2026-09-13）：** 本文保留初次审计记录；当前修复状态、验证结果和适用边界以[中文 PR 修复报告](security-audit-2026-09-13-pr.zh-CN.md)及[复扫报告](security-audit-2026-09-13-rescan/report.md)为准。V3 仍有跨账户目录预占风险；V13 仅新增可选约束重载，未强制约束生产调用；V9 未提交完整依赖校验。原严重度统计不代表已验证的远程漏洞数量，已安装插件与管理员配置不属于普通远程玩家权限。

审计对象：`Lattice` 仓库（基于 Purpur 的 Minecraft 1.21.11 服务端分支，含通过 JNI 加载的 C++ 原生加速库）。
审计时版本：分支 `ver/1.21.11`。

英文版见 [`security-audit-2026-09-13.md`](security-audit-2026-09-13.md)。

## 1. 摘要

本次审计共发现 **15 个问题：2 高危、7 中危、5 低危、1 信息级**。除明确标注为"受控例外/残余风险"的条目外，均已在本次变更中修复。

两个关键问题：

- **V1（高危）** —— `LatticeNativeLoader` 从网络下载原生库后 `System.load`，但 SHA-256 校验值**与载荷同源获取**，接受明文 `http://`，跟随任意重定向，且默认开启下载。任何能篡改或冒充下载源的人即可在服务端 JVM 中**执行任意原生代码**。
- **V2（高危）** —— 四处 JNI 长度校验按 `N * 3` / `N * 256` 计算后**截断为 32 位 `jsize`**。当 `N = 2^24` 时 `N * 256` 回绕为 `0`，空 permutation 数组即可通过校验，随后的 `memcpy(..., perms + i * 256, 256)` **越界读**。入口 `NativeOctavePerlinNoise.tryCreate(...)` 为 `public static`，任意插件均可触发 JVM 崩溃或内存泄露。

未在代码树或 git 历史中发现硬编码凭据、私钥、密钥库或 `.env` 文件。CI 中未发现 `pull_request_target`、`curl | bash` 或将密钥拼接进 `run:` 的用法。

## 2. 范围、信任模型与严重度

**审计范围：** 自有 Java（`lattice-server/src/main/java/com/latticemc/lattice`、`test-plugin`）、自有 C++（`lattice-native/{src,jni}`）、构建脚本、CI 工作流，以及依赖与上游更新供应链。

**不在范围：** 上游 Paper/Purpur/Minecraft 补丁内容（以 `.patch` 形式存在，构建时应用）以及 Minecraft 网络协议实现。

**信任模型。** 考虑两类攻击者：

- *远程/不可信数据* —— 世界数据、网络输入、以及插件可达的一切。这是主要威胁模型。
- *本地同机 / 供应链* —— 能在主机上预置文件，或控制下载/更新源的人。对 `lattice.yml` 或 JVM 命令行有写权限的服主视为可信（其本就可执行任意代码）；但能让**恶意下载源**或**本地非特权用户**提权的缺陷仍按真实漏洞处理。

**严重度：** **高危** = 无需管理员权限即可达成代码执行或内存破坏；**中危** = 内存泄露/拒绝服务，或在特定条件下绕过控制；**低危** = 纵深防御与加固；**信息级** = 卫生问题。

## 3. 发现明细

### 汇总

| 编号 | 严重度 | 领域 | 问题 | 状态 |
|------|--------|------|------|------|
| V1 | 高危 | 加载器 | 同源校验值 + `http://` + 开放重定向 → 原生 RCE | 已修复 |
| V2 | 高危 | JNI | 数组长度校验 32 位截断 → 堆越界读 | 已修复 |
| V3 | 中高危 | 加载器 | 缓存路径可预测、复用检查跟随符号链接、共享可写 tmp | 已修复 |
| V4 | 中危 | JNI | tick 掩码长度校验 32 位乘法溢出 | 已修复 |
| V5 | 中危 | 加载器 | 可配置 release base URL / 重定向导致 SSRF | 已修复 |
| V6 | 中危 | 原生 I/O | `zlib_validate` 无上限扩容（解压炸弹） | 已修复 |
| V7 | 低危 | 加载器 | `lattice.native.path` 可加载任意原生库 | 已记录 + 告警 |
| V8 | 中危 | 供应链 | Gradle wrapper 来自第三方镜像且无 SHA-256 固定 | 已修复 |
| V9 | 中危 | 供应链 | 无依赖校验/锁；存在 SNAPSHOT 依赖 | 部分修复 |
| V10 | 中危 | CI | `upstream.yml` 自动 push；`[ci-skip]` 守卫在 schedule 下失效 | 已修复 |
| V11 | 中危 | CI | Actions 固定到可变 tag，含 `lukka/get-cmake@latest` | 已修复 |
| V12 | 低危 | 供应链 | CMake `FetchContent` 未固定到 commit | 已修复 |
| V13 | 低危 | Java API | `NativeRegionFileRead.open(Path)` 实为任意文件读原语 | 已修复 |
| V14 | 低危 | test-plugin | 基准测试命令无权限门禁 | 已修复 |
| V15 | 信息级 | 仓库卫生 | `.gitignore` 未排除 `.env`/密钥/密钥库 | 已修复 |

---

### V1 — 原生库下载信任模型缺陷导致任意原生代码执行（高危）

**位置：** `lattice-server/src/main/java/com/latticemc/lattice/bootstrap/LatticeNativeLoader.java`（原 `downloadReleaseNative` / `verifyChecksum` / `downloadBytes`）。

**证据（修复前）：** 载荷取自 `buildReleaseAssetUrl(base, release, asset)`，期望摘要取自 `endpoint + ".sha256"` —— **同一来源**。`downloadBytes` 接受任意 `HttpURLConnection`（含 `http://`），并设置 `setInstanceFollowRedirects(true)`。下载默认开启（`native.download` 默认 `true`），结果直接传给 `System.load(...)`。

**影响：** 控制下载源、DNS 或 TLS 中间盒的攻击者可同时提供库文件与匹配的校验值，在服务端 JVM 内实现原生代码执行。这是本次影响最大的问题：它使校验值完全失去意义。

**修复：**

- 下载现在要求**本地固定的摘要**：内置发布清单（`BUILT_IN_RELEASE_DIGESTS`，源码构建为空，由发布流水线填充）或运维配置的 `lattice.native.sha256` / `native.sha256`。两者皆无时加载器**拒绝下载**并给出可操作的错误；不再获取远端 `.sha256`。
- `requireHttpUri` 默认拒绝一切非 `https` URL，除非显式设置 `lattice.native.allowInsecureHttp=true`（默认 `false`）。
- 关闭 JDK 自动重定向，改为手动跟随，并施加**主机白名单**（`github.com`、`objects.githubusercontent.com`、`release-assets.githubusercontent.com`、`codeload.github.com`）与最多 5 跳限制。
- `lattice.native.path`（管理员覆盖、可加载任意本地库）现在会打印明确告警，说明其绕过内置库与校验值验证。

**验证：** 单元测试覆盖 HTTPS 强制、scheme 拒绝、重定向白名单、可信摘要解析与摘要不匹配。

---

### V2 — JNI 数组长度校验 32 位截断导致堆越界读（高危）

**位置：** `lattice-native/jni/octave_perlin_noise.cpp`、`lattice-native/jni/double_perlin_noise.cpp`。

**证据（修复前）：**
`if (env->GetArrayLength(jPermutations) != static_cast<jsize>(N * 256))` —— `N` 为 `size_t`，`N * 256` 先按 64 位计算再截断为有符号 32 位 `jsize`。取 `N = 2^24` 时 `N * 256 = 2^32` 截断为 `0`，校验随即要求一个长度为 0 的 permutation 数组并通过，随后 `std::memcpy(out_octs[i].permutation, perms + i * 256, 256)` 严重越界读。`N * 3`（origins）以及 `double_perlin_noise.cpp` 的两半存在同样模式。

**影响：** 堆越界读 → JVM 崩溃（DoS）或内存泄露。`NativeOctavePerlinNoise.tryCreate(...)` 与 `NativeDoublePerlinNoise` 均为公开入口，服务端内任意插件可调用。

**修复：** 在 `jni_helper.hpp` 中新增纯函数、`constexpr`、防溢出的 `checked_count(count, per_item)`（溢出返回哨兵值），以及 `array_has_length`。四处校验全部改为 `size_t` 与 `size_t` 比较，并显式拒绝溢出。

**验证：** `tests/test_jni_bounds.cpp` 以运行时断言与编译期 `static_assert` 覆盖 `N = 2^24` 截断场景与真实溢出。三个改动的 JNI 翻译单元已用 `clang++ -std=c++20 -fno-exceptions -fno-rtti -Wall -Wextra -Wpedantic` 编译通过（无告警）。

---

### V3 — 原生库提取缓存：符号链接/TOCTOU 与共享可写目录（中高危）

**位置：** `LatticeNativeLoader.extractToCache` / `resolveCacheDir`。

**证据（修复前）：** 复用判定为 `Files.exists(target) && Files.size(target) == bytes.length` —— `Files.size` 会跟随符号链接，且从不重新校验内容。缓存文件名由 64 位截断摘要派生，默认目录为共享且全局可写的 `java.io.tmpdir/lattice-native`。本地攻击者在可预测的名字上预置符号链接，即可让 `System.load` 映射攻击者控制的库。

**修复：**

- 默认缓存目录改为**每用户私有**（`java.io.tmpdir/lattice-native-<user>`），并以 POSIX `0700` 创建；缓存目录本身为符号链接时直接拒绝。
- 复用要求目标是**常规文件（`NOFOLLOW_LINKS`）**且**完整 SHA-256** 与期望内容一致（重新读取并哈希，而非比较大小）。
- 字节先写入临时文件再原子移动到位，写入后再次校验才使用。

**验证：** 单元测试预先在目标名放置符号链接，断言其被替换（而非被跟随），且链接指向的文件未被改写。

---

### V4 — tick 掩码校验中的 32 位乘法溢出（中危）

**位置：** `lattice-native/jni/tick.cpp`。

**证据（修复前）：** `if (env->GetArrayLength(jSectionTickMasks) < sectionCount * maskLongsPerSection)` —— 两个操作数均为 `jint`，乘积按 32 位计算，可回绕为 `0` 或负数，使过短的数组通过 `<` 校验；`random_tick_filter` 随后越界索引。

**修复：** 乘积改用 `checked_count` 以 `size_t` 计算并在 `size_t` 下比较，溢出即拒绝。

---

### V5 — 可配置 release base URL 导致 SSRF（中危）

**位置：** `LatticeNativeLoader`（`native.release-base-url` / `lattice.native.releaseBaseUrl`）。

**证据（修复前）：** 端点由运维可配置的 base URL 拼装，且重定向可任意跟随。与 V1 组合可到达任意主机（如云元数据地址），并将恶意重定向升级为原生代码执行。

**修复：** 默认强制 HTTPS；重定向手动解析并受主机白名单约束；限制跳转次数。详见 V1。

---

### V6 — `zlib_validate` 无上限解压循环（中危）

**位置：** `lattice-native/src/io/compression/zlib_codec.cpp`。

**证据（修复前）：** `zlib_validate` 在循环中不断翻倍 scratch 容量直到解压成功，无上限 —— 典型的解压炸弹。当前仅测试调用，实际可达性有限，但一旦接入不可信输入即为潜在 DoS。

**修复：** 新增 `max_output_bytes` 参数（默认 `kMaxValidateOutputBytes` = 1 GiB）。所需输出超过上限时返回 `kBadData`，不再继续扩容。

**验证：** 新增测试断言低于真实大小的上限被拒绝、上限为 0 时返回 `kBadArg`。

---

### V7 — `lattice.native.path` 可加载任意原生代码（低危；管理员可信）

**位置：** `LatticeConfig`（`native.library-path`）→ `LatticeNativeLoader.load`。

**评估：** 这是有意的开发者/运维覆盖入口（README 中作为 PGO 流程记录）。写入它需要控制 `lattice.yml` 或 JVM 命令行，而这两者本就等价于代码执行权限。不作为漏洞，但加载器现在会明确告警该覆盖会绕过所有验证。

---

### V8 — Gradle wrapper 来自第三方镜像且无校验值（中危）

**位置：** `gradle/wrapper/gradle-wrapper.properties`。

**证据（修复前）：** `distributionUrl=https://mirrors.aliyun.com/macports/distfiles/gradle/gradle-9.2.0-bin.zip`，仅有 `validateDistributionUrl=true`，**没有 `distributionSha256Sum`**。`validateDistributionUrl` 只检查 URL 语法，构建工具链二进制本身未被校验。

**修复：** 加入官方 Gradle 9.2.0 校验值
（`distributionSha256Sum=df67a32e86e3276d011735facb1535f64d0d88df84fa87521e90becc2d735444`）。为兼顾网络环境保留镜像；若镜像提供的字节与官方产物不一致，构建现在会明确失败，而不会执行未经验证的工具链。

---

### V9 — 无依赖校验/锁文件；存在 SNAPSHOT 依赖（中危；部分修复）

**位置：** `*.gradle.kts`、`gradle.properties`。

**证据：** 无 `gradle/verification-metadata.xml`、无依赖锁定；`com.velocitypowered:velocity-native:3.4.0-SNAPSHOT` 从快照仓库解析（不可复现）；`me.lucko:spark-api` 使用时间戳坐标。

**修复/状态：** 该项为**已记录的残余风险**，未提交缓解措施。审计期间实际尝试生成：

- `./gradlew --write-verification-metadata sha256 help` 确实会生成文件，但该文件不完整（仅覆盖配置阶段解析到的依赖），而不完整的文件会**破坏构建**：随后执行 `./gradlew applyAllPatches` 失败，因为 Gradle 拒绝任何在元数据中无条目的构件。此现象已实测复现。
- 全量解析尝试（`--write-verification-metadata sha256 :lattice-server:compileJava :lattice-server:testClasses`）在 Gradle 内部失败，报错 `Multiple entries with same key: me.lucko:spark-api:0.1-20240720.200737-2` —— 带时间戳的快照坐标产生了重复的构件条目。

因此**不提交** `verification-metadata.xml`：不完整的文件比没有更糟。`velocity-native:3.4.0-SNAPSHOT` 由 Paper/Paperweight 开发包强制要求，强行更换版本可能破坏构建，故记录为**受控例外**，并在 `.github/dependabot.yml` 中排除自动更新。**建议顺序：** 先把带时间戳/SNAPSHOT 的坐标替换为固定正式版，再生成并提交 `gradle/verification-metadata.xml` 并启用 `dependencyVerification`。

---

### V10 — 上游自动同步的跳过守卫从未生效（中危）

**位置：** `.github/workflows/upstream.yml`。

**证据（修复前）：** `if: "!contains(github.event.commits[0].message, '[ci-skip]')"`。`schedule` 事件下 `github.event.commits` 为 undefined，该守卫恒为真、永远无法跳过。工作流每 15 分钟运行一次，顶层权限 `contents: write`，直接 push 到默认分支。

**修复：** 顶层权限降为 `contents: read`，`contents: write` 限定到该 job；新增步骤检查 `git log -1` 中的 `[ci-skip]` 标记并据此门控后续步骤；push 仍为非强制推送，非快进时拒绝而非覆盖并发的人工提交。`scripts/upstreamCommit.sh` 改用 `printf` 构造提交信息（不再用 `echo -e` 重新解释远端文本），并从上游信息中剔除 `[ci-skip]`。

---

### V11 — GitHub Actions 固定到可变 tag（中危）

**位置：** 三个工作流。

**证据（修复前）：** `actions/checkout@v6`、`actions/setup-java@v4`、`gradle/actions/setup-gradle@v5`、`actions/upload-artifact@v4`、`actions/download-artifact@v4`、`actions/cache@v4`、`softprops/action-gh-release@v2`，尤其是 `lukka/get-cmake@latest`。上游 action 一旦被投毒，将以工作流令牌（发布 job 含 `contents: write`）运行。

**修复：** 所有 `uses:` 固定到完整 commit SHA 并附版本注释；`lukka/get-cmake@latest` 替换为固定 commit 的 `v4.4.2`。新增 `.github/dependabot.yml` 跟踪 `github-actions` 与 `gradle`，让固定点有节奏地更新。

---

### V12 — CMake `FetchContent` 依赖未固定到 commit（低危）

**位置：** `lattice-native/CMakeLists.txt`。

**证据（修复前）：** `GIT_TAG v1.20`（libdeflate）与 `GIT_TAG v2.4.11`（doctest），均为可变 tag 且浅克隆。

**修复：** 固定到解析出的 commit（libdeflate v1.20 → `275aa514…`；doctest v2.4.11 → `ae7a1353…`）；同时移除 `GIT_SHALLOW`，因为浅克隆无法检出任意 commit。

---

### V13 — `NativeRegionFileRead.open(Path)` 实为任意文件读原语（低危）

**位置：** `lattice-server/src/main/java/com/latticemc/lattice/nativelib/NativeRegionFileRead.java`。

**证据（修复前）：** `open(Path)` 直接把 `path.toAbsolutePath()` 传给 native `open(..., O_RDONLY)`，仅做 NUL 字节校验。当前调用方传入内部 RegionFile 路径，但该公开 API 接受任意路径。

**修复：** 新增 `open(Path path, Path allowedRoot)`；路径先用 `toRealPath()` 解析（符号链接与 `..` 无法逃逸），且必须位于根目录之内，否则返回 `null` 由调用方回退到 Java I/O。无约束的单参重载保留给内部调用方。

---

### V14 — 基准测试命令无权限门禁（低危）

**位置：** `test-plugin/src/main/java/org/purpurmc/testplugin/{Pathfinder,Item,EntityActivation}BenchmarkCommand.java`。

**证据（修复前）：** 三个命令注册时均无 `setPermission`/`testPermission`，任意玩家都能生成最多 4096 个实体并制造高负载基准测试。

**修复：** 每个命令声明权限（`lattice.bench.pathfinder` / `.item` / `.activation`）并在首行调用 `testPermission(sender)`。注意 `test-plugin` 模块**默认未启用**（`test-plugin.settings.gradle.kts` 生成时为注释状态），仅在显式启用的构建中才有影响。

---

### V15 — `.gitignore` 未排除密钥类文件（信息级）

**位置：** `.gitignore`。

**修复：** 新增 `.env`、`.env.*`、`*.pem`、`*.key`、`*.p12`、`*.pfx`、`*.jks`、`*.keystore`。同时加入 `!/docs/security-audit-*.md`，使位于默认被忽略的 `docs/` 下的审计报告可正常提交（原有 `docs/` 文件历史上是强制加入的）。

## 4. 修复文件清单

| 领域 | 变更文件 |
|------|----------|
| 加载器 / 配置 | `bootstrap/LatticeNativeLoader.java`、`config/LatticeConfig.java` |
| JNI / 原生 | `jni/jni_helper.hpp`、`jni/octave_perlin_noise.cpp`、`jni/double_perlin_noise.cpp`、`jni/tick.cpp`、`src/io/compression/zlib_codec.{hpp,cpp}` |
| Java API | `nativelib/NativeRegionFileRead.java` |
| 插件 | `test-plugin/.../{Pathfinder,Item,EntityActivation}BenchmarkCommand.java` |
| 构建 | `lattice-native/CMakeLists.txt`、`gradle/wrapper/gradle-wrapper.properties` |
| CI / 供应链 | `.github/workflows/{build,native,upstream}.yml`、`.github/dependabot.yml`、`scripts/upstreamCommit.sh`、`.gitignore` |
| 测试 | `LatticeNativeLoaderTestSuite.java`、`NativeRegionFileReadTestSuite.java`、`LatticeConfigTestSuite.java`、`lattice-native/tests/test_jni_bounds.cpp`、`test_zlib_codec.cpp`、`lattice-native/tests/CMakeLists.txt` |
| 文档 | 本报告及英文版 |

## 5. 验证

本次环境已执行：

- **C++ 构建与测试（CMake + Ninja + CTest，Release）。** 执行
  `cmake -S lattice-native -B lattice-native/build-verify -G Ninja -DLATTICE_BUILD_TESTS=ON`，随后
  `ctest -R "jni_bounds|zlib_codec|octave_perlin_noise|double_perlin_noise|random_tick_filter"`。
  **5 个测试全部通过**，包含新增的溢出边界与解压上限测试。（本地说明：本机 CMake 为 4.4.3，
  而 CI 固定 3.28.3；doctest 2.4.11 在 CMake 4.x 下需要 `-DCMAKE_POLICY_VERSION_MINIMUM=3.5`，
  仅为本地临时措施。）
- **C++ 编译检查。** 三个改动的 JNI 翻译单元在 JDK 21 JNI 头文件下用
  `clang++ -std=c++20 -fno-exceptions -fno-rtti -Wall -Wextra -Wpedantic` 编译无告警。
- **Java 编译。** 所有改动的主源码（`LatticeNativeLoader`、`LatticeConfig`、`NativeRegionFileRead`、
  `LatticeNative`）与全部改动的测试套件均通过 `javac --release 21`（基于 Gradle 依赖缓存）编译；
  该过程发现并修复了新增加载器测试中两处缺失的 `throws` 声明。
- **Paperweight。** 移除不完整的校验元数据后，`./gradlew applyAllPatches` 成功（应用 231 个 Purpur +
  20 个 Minecraft + 41 个 Paper 补丁）。
- **Java 测试（Gradle）。** 使用系统 clang（`CC=/usr/bin/clang CXX=/usr/bin/clang++`）运行
  `./gradlew :lattice-server:test`，使原生构建与 CI 的 Apple 工具链一致。相关套件结果：
  **`LatticeNativeLoaderTestSuite` 14/14 通过、`LatticeConfigTestSuite` 4/4 通过、
  `NativeRegionFileReadTestSuite` 3/3 通过。**（本次运行另有 12 个失败来自不相干的 Minecraft
  `@Suite` 类 —— 这是 `--tests` 过滤器破坏 JUnit 套件发现机制所致，并非回归：它们是上游测试脚手架，
  依赖包扫描发现嵌套类。）

建议 CI 增补：原生 `ctest` 矩阵已在执行；保留/新增 `./gradlew :lattice-server:test` 步骤（现有
`build` 任务已包含），使新增的加载器/配置测试在每次 PR 执行。

## 6. 残余风险与建议

1. **在发布流水线中填充 `BUILT_IN_RELEASE_DIGESTS`**，使官方构建无需运维提供摘要即可自动下载。在此之前，非内置构建的运维方必须设置 `native.sha256`。
2. **在联网环境生成并提交 `gradle/verification-metadata.xml`**，随后启用 `dependencyVerification`。待上游发布正式版后处理 `velocity-native` SNAPSHOT。
3. **对发布产物签名并生成 SBOM。** README 已提示第三方重构建难以区分；签名与 SBOM 可让运维方校验来源。
4. **考虑对 CI 基础镜像与系统包做固定**（LLVM-MinGW 下载已做 SHA 校验，是现有的一项良好控制）。
5. **未引入 CodeQL**：Paperweight 补丁流程的构建方式无法被 CodeQL 的 autobuild 识别，草率添加会导致 CI 失败。如有需要，可仅针对独立的 `lattice-native` CMake 工程与未打补丁的 `lattice-server` Java 源码运行 CodeQL。

## 7. 已审查但未发现问题

- 自有代码中无命令执行（`Runtime.exec`/`ProcessBuilder`）。
- 无 `ObjectInputStream`/`readObject`、XStream、Jackson 多态类型、`pickle`、`Marshal`。
- 无字符串拼接 SQL；自有代码无 JDBC。
- 无可导致 XXE 的 XML 解析。
- 无硬编码密钥、私钥、密钥库（已检查工作区与 git 历史）。
- Configurate/SnakeYAML 使用依赖库的安全默认值，无自定义解析器。
