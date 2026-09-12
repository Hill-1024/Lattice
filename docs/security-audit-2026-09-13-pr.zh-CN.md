# 原生加载、JNI 边界与构建供应链修复报告

本次变更为 Lattice 1.21.11 增加原生库下载的本地可信摘要校验，修复噪声与 tick JNI 的长度计算问题，并补充解压资源上限、测试命令权限及构建供应链约束。提交前基线为 `605b1991212a29265dc6a60ec0c7ba7a986dc48c`，目标分支为上游 `ver/1.21.11`。

**本 PR 包含已有本地修复及完整审计材料，并非所有审计发现均已修复。** 下表区分实际代码变化、仅提供可选接口的加固以及仍未解决的问题。初次报告的严重度和“已修复”统计保留为历史记录；与本文或复扫结论冲突时，以本文和复扫为准。已安装的 JVM 插件本身拥有进程权限，管理员控制的配置也不能等同于普通远程玩家输入。

## 一、问题与修复方式

| 编号 / 位置 | 原问题与影响 | 本次修复方式 | 当前状态与边界 |
| --- | --- | --- | --- |
| V1：`LatticeNativeLoader` | 下载库与 `.sha256` 同源；源被控制时可同时替换载荷和摘要，随后进入 `System.load` | 使用本地 `native.sha256` / `lattice.native.sha256` 或内置摘要作为可信锚点；无摘要拒绝下载；下载后按完整 SHA-256 核对 | 下载信任校验已实现；内置表目前为空，发布注入尚未实现 |
| V2：octave / double-perlin JNI | `N*3` / `N*256` 转为 32 位 `jsize` 后截断，过短数组可能通过比较并触发越界读取 | 增加 `checked_count` 和 `array_has_length`，使用 `size_t` 计算、比较并拒绝溢出 | 已替换相关校验；测试覆盖 `N=2^24` 截断场景和乘法溢出。未证明普通玩家可任意控制这些参数 |
| V3：原生库缓存 | 旧缓存只比较文件大小，跟随符号链接，使用固定共享目录 | 使用用户名称派生目录、尝试设置 POSIX 0700；拒绝目录符号链接；常规文件检查及完整摘要验证；临时写入后替换、再次校验 | **部分缓解**。权限设置失败被忽略、目录所有权未验证，跨账户预占风险仍在，见第二节 |
| V4：tick JNI | `sectionCount * maskLongsPerSection` 使用 32 位乘法，溢出后可能绕过长度约束 | 改用 `checked_count` 计算需要的掩码长度并在宽类型中比较 | 已修复该乘法；复扫确认该 JNI 导出当前无对应 Java native 声明，不声称玩家远程可达 |
| V5：下载 URL / 重定向 | 接受 HTTP 并自动跟随任意重定向 | 默认 HTTPS；手动处理重定向，限制到 GitHub 相关主机且最多五跳 | 默认路径已加固；首次请求地址仍由管理员配置。显式 insecure 开关同时放宽 HTTP 和重定向主机限制，不能称为完整 SSRF 隔离 |
| V6：`zlib_validate` | scratch 缓冲不断翻倍，没有输出上限 | 增加 `max_output_bytes`，默认 1 GiB；超过上限返回 `kBadData`，零上限返回 `kBadArg` | 已限制该 helper；当前仅测试使用，不是生产所有解压入口的全局配额 |
| V7：本地库路径覆盖 | 显式 `lattice.native.path` 可加载任意本地原生代码 | 增加绕过校验的日志告警 | 保留管理员覆盖能力；属于信任边界说明 |
| V8：Gradle wrapper | 第三方镜像分发的 Gradle ZIP 没有固定校验和 | 配置 Gradle 9.2.0 的 `distributionSha256Sum`，保留原镜像 | 已增加分发包校验；本次利用现有缓存，未重新下载 ZIP 验证 |
| V9：Gradle 依赖 | 无完整依赖锁或校验元数据，并使用 SNAPSHOT | 记录完整元数据生成失败及上游依赖约束；Dependabot 跟踪 Gradle，暂排除 `velocity-native` | **未完成**。没有提交完整 `verification-metadata.xml` 或锁文件，不宣称构建完全可复现 |
| V10：上游同步 | `schedule` 事件没有 `github.event.commits`，原跳过守卫无效；`echo -e` 会解释远端提交文本中的转义 | checkout 完整历史后检查 HEAD 的 `[ci-skip]`，门控后续步骤；写权限下放到 job；使用 `printf`，剔除远端 `[ci-skip]` | 已调整；继续使用普通非强制 push，保留定时自动同步行为 |
| V11：Actions | 使用可移动 tag，包含 `lukka/get-cmake@latest` | 三个工作流的 Actions 固定完整 commit SHA；增加每周 Dependabot 更新 | 固定引用已提交；未在本地复现全部 GitHub runner 环境 |
| V12：CMake 依赖 | libdeflate / doctest 使用可移动 tag | 固定版本对应完整 commit，移除限制任意 commit 检出的浅克隆选项 | 已提交，提交前原生构建使用这些依赖 |
| V13：RegionFile API | 单参数 `open(Path)` 未提供明确目录边界 | 新增 `open(Path, allowedRoot)`，使用 `toRealPath` 和根目录包含判断；失败返回 `null` | **可选加固**。原重载仍传 `null`，生产调用未强制根约束，也没有解决检查到打开之间的所有竞态 |
| V14：测试插件命令 | 三种基准命令缺少权限门禁，可被用于制造大量实体或高负载 | 注册 `lattice.bench.pathfinder`、`lattice.bench.item`、`lattice.bench.activation`，执行前调用 `testPermission(sender)` | 已实现；插件默认未启用，需显式启用才影响部署 |
| V15：仓库规则 | `.gitignore` 缺少常见凭据文件规则，审计文档被默认忽略 | 忽略 `.env`、私钥和密钥库扩展名；允许跟踪审计 Markdown | 已提交；忽略规则不等同于完整秘密扫描 |

## 二、复扫发现与尚未完成的修复

以下内容是复扫记录与后续方案，**不在本 PR 中冒充已修复**。

1. **原生寻路镜像无容量上限（中危，置信度中）。** 长期 `thread_local` 镜像持续保存新 section，每 tick 上传限额不能限制总常驻内存；普通生物追逐的调用链可能导致持续增长。建议设置每线程/每世界 section 或字节预算，增加 LRU/TTL、chunk unload/world close 清理和达到预算时的 Java 回退；淘汰必须保证正在查找的指针有效。需要新增超预算、卸载、跨世界清理以及实际 RSS 稳态验证。
2. **缓存目录跨账户预占（中危，置信度高）。** `java.io.tmpdir/lattice-native-<user>` 可预测，未验证所有权；POSIX chmod 失败被忽略，摘要核对不能替代从写入到 `System.load` 的目录保护。建议在可信私有根下原子创建不可预测、仅当前用户可访问的目录；拒绝非当前账户所有或其他账户可写的路径，权限设置失败时拒绝加载。需要不同 UID 预占、chmod 失败和并发提取测试。
3. **未接入 NBT 解析器存在休眠 UAF。** 栈扩容后继续使用旧 `Frame&`；当前没有对应生产 Java 入口，不声称玩家可远程利用。接入前应在扩容后重取父节点或在扩容前更新计数，并用 64/128/256 层嵌套输入及 sanitizer 回归验证。
4. **死代码及预留接口未清理。** 附件包含私有残留、恒假分支、未构建文件、测试专用 helper 和 51 个无对应 Java 声明的 JNI 导出。公共 API 与 JNI 清理需要兼容性评估，不能仅凭没有仓库调用就直接删除。
5. **发布和依赖信任链仍有待办。** 内置摘要表注入、完整 Gradle 依赖校验与锁定、SNAPSHOT 处理、发布签名及 SBOM 均未完成。初次报告关于“发布流水线已填充摘要”的表述已勘误，源码注释也已更正。

复扫完整阅读 282/478 个本地源码、补丁及构建文件，其余 196 个为局部阅读或搜索。复扫本身未执行服务器、PoC、sanitizer 或联网 CVE 核查；提交前测试另见第四节。

## 三、兼容性与部署迁移

- **没有内置原生库的平台需要提供可信摘要。** 在 `lattice.yml` 设置 `native.sha256`，或使用 JVM 参数 `-Dlattice.native.sha256=<64位十六进制摘要>`；摘要应通过独立可信渠道取得并与对应平台产物匹配。内置表为空时，不配置摘要会拒绝下载。
- 建议将 `native.release` 固定到与摘要匹配的不可变 release；继续使用 `native-latest` 时，上游二进制改变会导致摘要不匹配，需要同步更新 pin。下载失败后的具体服务行为取决于现有调用方的原生加载回退。
- `native.allow-insecure-http` 默认关闭；显式开启同时放宽重定向主机限制。自定义镜像发生非白名单跨主机重定向时，默认下载可能被拒绝。
- 缓存目录默认名称改变；内置库与显式本地库路径的选择顺序保留。跨账户共享临时目录仍需依照第二节处理，不能将本次缓存加固视为安全隔离保证。
- 配置项是启动期配置，更改后需要重启。基准命令现在需要相应权限；没有相关权限的调用会被拒绝。

## 四、提交前验证

本次重新验证环境：macOS ARM64、Apple Clang、Java 21 测试工具链。Gradle 使用现有依赖和构建缓存；原生 CMake 验证在新的 `lattice-native/build-pr-verify` 目录执行。

| 验证项目 | 实际结果 |
| --- | --- |
| 服务端完整默认测试任务 `:lattice-server:test`，未使用 `--tests` 过滤 | **BUILD SUCCESSFUL，6 分 54 秒**；158 个 XML 测试套件，共 9,096 项，9,073 通过、23 跳过、0 失败、0 错误。保留仓库默认的 `Slow` 标签排除配置 |
| 改动相关 Java 套件 | `LatticeNativeLoaderTestSuite` 14/14、`LatticeConfigTestSuite` 4/4、`NativeRegionFileReadTestSuite` 3/3，全部通过 |
| 实际原生加载 | 服务端测试日志确认加载 `macos-aarch64` 库并识别 Apple NEON，测试包含 Java/JNI 对照；不等同于真实多人服务器验证 |
| 测试插件 `:test-plugin:compileJava` | 通过；用临时 Gradle init 脚本启用可选模块，没有修改默认启用状态 |
| CMake Release 构建及完整 CTest | **32/32 通过**，覆盖新增 `jni_bounds` 和 `zlib_codec`，同时运行已有全部原生测试目标 |
| 工作流与脚本 | 三个工作流及 Dependabot YAML 可解析；所有外部 Actions 引用为完整 40 位 SHA；`bash -n scripts/upstreamCommit.sh` 通过 |
| 变更与附件 | `git diff --cached --check` 通过；复扫 JSON/SARIF 可解析；本文附件链接均有对应文件 |

复现命令：

```bash
# 启用测试插件仅用于本次验证；临时文件不提交。
cat > /tmp/lattice-pr-enable-test-plugin.gradle <<'GRADLE'
settingsEvaluated { settings -> settings.include(':test-plugin') }
GRADLE
CC=/usr/bin/clang CXX=/usr/bin/clang++ ./gradlew \
  -I /tmp/lattice-pr-enable-test-plugin.gradle \
  :lattice-server:test :test-plugin:compileJava --console=plain

JAVA_HOME=$(/usr/libexec/java_home -v 21) \
CC=/usr/bin/clang CXX=/usr/bin/clang++ \
cmake -S lattice-native -B lattice-native/build-pr-verify -G Ninja \
  -DCMAKE_BUILD_TYPE=Release -DLATTICE_BUILD_TESTS=ON \
  -DCMAKE_POLICY_VERSION_MINIMUM=3.5
cmake --build lattice-native/build-pr-verify --parallel 8
ctest --test-dir lattice-native/build-pr-verify --output-on-failure
```

`CMAKE_POLICY_VERSION_MINIMUM=3.5` 是本机 CMake 4.x 与 doctest 的兼容参数。首次尝试直接指定未启用的 `:test-plugin` 曾因找不到该项目而退出，随后用上述临时脚本完成验证。原初次报告关于过滤测试引发失败的记录是历史记录；本轮未使用该过滤方式，完整默认任务通过。

本轮未运行跨平台发布矩阵、真实玩家负载、不同 UID 攻击复现或 sanitizer；没有重新执行完整 Paperweight 补丁生成。上游 GitHub Actions 结果须以 PR 页面后续运行状态为准。

## 五、完整报告与附件

- [初次中文审计及逐项修复记录](security-audit-2026-09-13.zh-CN.md)
- [初次英文审计](security-audit-2026-09-13.md)
- [复扫索引](security-audit-2026-09-13-rescan.md)
- [复扫问题报告：攻击条件、调用链、证据及修复建议](security-audit-2026-09-13-rescan/report.md)
- [死代码与未接入功能清单](security-audit-2026-09-13-rescan/dead-code-review.md)
- [扫描清单](security-audit-2026-09-13-rescan/scan-manifest.json)、[发现记录](security-audit-2026-09-13-rescan/findings.json)、[覆盖记录](security-audit-2026-09-13-rescan/coverage.json)、[SARIF](security-audit-2026-09-13-rescan/exports/results.sarif)

按“提交所有本地更改”的范围，提交也保留现有 `.zcode/plans` 审计计划和 `.video_agent/plugin_root` 工具路径记录；它们不参与构建，计划内容不作为最终实现或验证状态的依据。复扫详细目录显式加入版本管理，原始扫描附件保持原样。
