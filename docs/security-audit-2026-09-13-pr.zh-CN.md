# 原生库安全提取、寻路缓存、NBT 与供应链修复报告

本次变更为 Lattice 1.21.11 增加原生库下载的本地可信摘要校验，修复噪声与 tick JNI 的长度计算问题，限制每个原生寻路镜像的容量、修复 NBT 栈扩容后的悬空引用，并补充解压资源上限、测试命令权限及构建供应链约束。提交前基线为 `605b1991212a29265dc6a60ec0c7ba7a986dc48c`，目标分支为上游 `ver/1.21.11`。

**本 PR 包含已有本地修复及完整审计材料，并非所有审计发现均已修复。** 下表区分实际代码变化、仅提供可选接口的加固以及仍未解决的问题。初次报告的严重度和“已修复”统计保留为历史记录；与本文或复扫结论冲突时，以本文和复扫为准。已安装的 JVM 插件本身拥有进程权限，管理员控制的配置也不能等同于普通远程玩家输入。

## 一、问题与修复方式

| 编号 / 位置 | 原问题与影响 | 本次修复方式 | 当前状态与边界 |
| --- | --- | --- | --- |
| V1：`LatticeNativeLoader` | 下载库与 `.sha256` 同源；源被控制时可同时替换载荷和摘要，随后进入 `System.load` | 使用本地 `native.sha256` / `lattice.native.sha256` 或内置摘要作为可信锚点；无摘要拒绝下载；下载后按完整 SHA-256 核对 | 下载信任校验已实现；内置表目前为空，发布注入尚未实现 |
| V2：octave / double-perlin JNI | `N*3` / `N*256` 转为 32 位 `jsize` 后截断，过短数组可能通过比较并触发越界读取 | 增加 `checked_count` 和 `array_has_length`，使用 `size_t` 计算、比较并拒绝溢出 | 已替换相关校验；测试覆盖 `N=2^24` 截断场景和乘法溢出。未证明普通玩家可任意控制这些参数 |
| V3：原生库缓存 | 可预测共享缓存及未受保护的路径可被预占或替换，导致不可信文件进入 `System.load` | 保留下载 pin 校验；将提取统一交给 `NativeLibraryCache`：核验祖先链、所有权和权限/ACL，原子创建随机私有目录，禁止复用旧缓存，检查失败拒绝加载 | 已合并完整私有提取方案；覆盖 POSIX、Darwin 扩展 ACL 与 Windows ACL，实际跨平台验证状态见第四节 |
| V4：tick JNI | `sectionCount * maskLongsPerSection` 使用 32 位乘法，溢出后可能绕过长度约束 | 改用 `checked_count` 计算需要的掩码长度并在宽类型中比较 | 已修复该乘法；复扫确认该 JNI 导出当前无对应 Java native 声明，不声称玩家远程可达 |
| V5：下载 URL / 重定向 | 接受 HTTP 并自动跟随任意重定向 | 默认 HTTPS；手动处理重定向，限制到 GitHub 相关主机且最多五跳 | 默认路径已加固；首次请求地址仍由管理员配置。显式 insecure 开关同时放宽 HTTP 和重定向主机限制，不能称为完整 SSRF 隔离 |
| V6：`zlib_validate` | scratch 缓冲不断翻倍，没有输出上限 | 增加 `max_output_bytes`，默认 1 GiB；超过上限返回 `kBadData`，零上限返回 `kBadArg` | 已限制该 helper；当前仅测试使用，不是生产所有解压入口的全局配额 |
| V7：本地库路径覆盖 | 显式 `lattice.native.path` 可加载任意本地原生代码 | 增加绕过校验的日志告警 | 保留管理员覆盖能力；属于信任边界说明 |
| V8：Gradle wrapper | 第三方镜像分发的 Gradle ZIP 没有固定校验和 | 配置 Gradle 9.2.0 的 `distributionSha256Sum`，保留原镜像 | 已增加分发包校验；本次利用现有缓存，未重新下载 ZIP 验证 |
| V9：Gradle 依赖 | 无完整依赖锁或校验元数据，并使用 SNAPSHOT | 记录完整元数据生成失败及上游依赖约束；Dependabot 跟踪 Gradle，暂排除 `velocity-native` | **未完成**。没有提交完整 `verification-metadata.xml` 或锁文件，不宣称构建完全可复现 |
| V10：上游同步 | `schedule` 事件没有 `github.event.commits`，原跳过守卫无效；`echo -e` 会解释远端提交文本中的转义 | checkout 完整历史后检查 HEAD 的 `[ci-skip]`，门控后续步骤；写权限下放到 job；使用 `printf`，剔除远端 `[ci-skip]` | 已调整；继续使用普通非强制 push，保留定时自动同步行为 |
| V11：Actions | 使用可移动 tag，包含 `lukka/get-cmake@latest` | 四个工作流的 Actions 固定完整 commit SHA；增加每周 Dependabot 更新 | 固定引用已提交；未在本地复现全部 GitHub runner 环境 |
| V12：CMake 依赖 | libdeflate / doctest 使用可移动 tag | 固定版本对应完整 commit，移除限制任意 commit 检出的浅克隆选项 | 已提交，提交前原生构建使用这些依赖 |
| V13：RegionFile API | 单参数 `open(Path)` 未提供明确目录边界 | 新增 `open(Path, allowedRoot)`，使用 `toRealPath` 和根目录包含判断；失败返回 `null` | **可选加固**。原重载仍传 `null`，生产调用未强制根约束，也没有解决检查到打开之间的所有竞态 |
| V14：测试插件命令 | 三种基准命令缺少权限门禁，可被用于制造大量实体或高负载 | 注册 `lattice.bench.pathfinder`、`lattice.bench.item`、`lattice.bench.activation`，执行前调用 `testPermission(sender)` | 已实现；插件默认未启用，需显式启用才影响部署 |
| V15：仓库规则 | `.gitignore` 缺少常见凭据文件规则，审计文档被默认忽略 | 忽略 `.env`、私钥和密钥库扩展名；允许跟踪审计 Markdown | 已提交；忽略规则不等同于完整秘密扫描 |

## 二、复扫问题的修复与剩余边界

| 问题 | 合并后的修复方式 | 回归覆盖与保留边界 |
| --- | --- | --- |
| 原生寻路镜像持续保留新区域 section | 每个 `PathfinderStateMirror` 最多 512 个 section，约 12 MiB 数据加 map 开销；写入前预检整个快照，合并后超预算则先清空旧工作集；单个超大快照不缓存，保留原 eager 查找路径 | 增加连续上传不同区域、超大快照与世界切换测试；清理发生在上传前，不在持有 section 指针的搜索过程中。并未增加 LRU/TTL 或 chunk unload 钩子；预算是每个镜像的上限，不是整个进程的总内存上限 |
| 原生库缓存跨账户预占/替换 | 验证父目录及祖先链的所有权、模式位和 ACL，在可信父目录内创建不可预测的私有目录；POSIX 使用原子 0700 创建，Darwin 检查扩展 ACL，Windows 检查继承后的 ACL；失败关闭，不再吞掉权限错误 | 覆盖旧缓存投毒、危险父目录/祖先、符号链接别名、权限、失败清理、外部所有者策略及平台 ACL；保持路径保护直到后续 `System.load`。显式本地库覆盖仍属于可信管理员入口 |
| NBT 栈扩容后使用旧父节点引用 | 在 compound-list 与嵌套 list 两个分支中，把父列表剩余计数递减移动到 `push_frame` 之前，避免扩容使 `Frame&` 失效 | 增加跨栈增长边界的嵌套容器测试并运行 ASan/UBSan。NBT-index JNI 仍没有生产 Java 声明，不声称是已确认的远程玩家漏洞 |

仍待完成的事项：

- NBT 索引的一字节 depth 格式与 512 深度策略不一致，未在本次重设计。
- 死代码及预留接口清单保留：包括私有残留、恒假分支、未构建文件、测试专用 helper 和 51 个无对应 Java 声明的 JNI 导出。公共 API 清理需要兼容性评估。
- 内置下载摘要表注入、完整 Gradle 依赖校验与锁定、SNAPSHOT 处理、发布签名和 SBOM 均未完成。
- RegionFile 根目录约束仍是可选重载；不将其描述为已经强制接入生产。

原复扫完整阅读 282/478 个本地源码、补丁及构建文件，其余 196 个为局部阅读或搜索。扫描原件保留当时的“未修复/未运行测试”状态；当前代码和验证结论以本文为准。

## 三、兼容性与部署迁移

- **没有内置原生库的平台需要提供可信摘要。** 在 `lattice.yml` 设置 `native.sha256`，或使用 JVM 参数 `-Dlattice.native.sha256=<64位十六进制摘要>`；摘要应通过独立可信渠道取得并与对应平台产物匹配。内置表为空时，不配置摘要会拒绝下载。
- 建议将 `native.release` 固定到与摘要匹配的不可变 release；继续使用 `native-latest` 时，上游二进制改变会导致摘要不匹配，需要同步更新 pin。下载失败后的具体服务行为取决于现有调用方的原生加载回退。
- `native.allow-insecure-http` 默认关闭；显式开启同时放宽重定向主机限制。自定义镜像发生非白名单跨主机重定向时，默认下载可能被拒绝。
- `native.cache-directory` / `lattice.native.cacheDir` 现在用作提取父目录；默认父目录为 `java.io.tmpdir`，其下每次创建随机私有目录，旧固定缓存不再复用。自定义父目录的祖先链必须通过所有权和权限/ACL 检查；过于宽松的目录可能需要迁移。
- POSIX 要求 Unix 所有者/模式位支持，Windows 要求 ACL 支持；Darwin 通过系统 `/bin/ls -lde` 检查扩展 ACL，无法检查或存在不安全授权时拒绝加载。显式本地库路径覆盖保留。
- 提取文件保留至 JVM 退出，清理为尽力而为，强制结束进程可能留下私有目录。寻路缓存满时清空旧工作集可能增加后续 miss，现有回退路径保留。
- 配置项是启动期配置，更改后需要重启。基准命令现在需要相应权限；没有相关权限的调用会被拒绝。

## 四、合并后的验证

以下结果均在归并后的代码上重新执行：

| 验证项目 | 结果 |
| --- | --- |
| 完整默认 `:lattice-server:test` 与测试插件编译 | **BUILD SUCCESSFUL，8 分 40 秒**；159 个 XML 套件，共 9,109 项测试，9,083 通过、26 跳过、0 失败、0 错误；`:test-plugin:compileJava` 通过 Gradle 增量检查 |
| 改动相关 Java 测试 | `LatticeNativeLoaderTestSuite` 15/15；`LatticeConfigTestSuite` 4/4；`NativeRegionFileReadTestSuite` 3/3；`NativeLibraryCacheTestSuite` 9 成功、3 条件跳过 |
| 原生 ASan/UBSan | 新建 Debug sanitizer 构建，完整 CTest **32/32 通过**，含 64/128/256 层 NBT 容器、寻路缓存容量、JNI 长度与解压上限测试；无 sanitizer 失败报告 |
| Release 原生库 | Gradle 重新构建成功；服务端测试日志确认从随机私有目录加载 macOS ARM64 库 |
| 独立 bootstrap 与真实 `System.load` | `scripts/testNativeLoader.py` 使用 Java 21 和 Release 原生库，**25 项成功、2 项 Windows 条件跳过、0 失败**。该轮实际执行了加载测试，补足完整 Gradle 任务中未设置专用库参数而跳过的用例 |
| 工作流、附件与变更 | 四个工作流及 Dependabot YAML 可解析，外部 Actions 均固定完整 SHA；脚本语法、文档链接、差异空白检查通过 |
| 内容完整性 | #1 的 8 个非适配文件逐字节保留，另外 3 个文件为缓存实现、工作流和报告的明确整合；#1 的三个提交全部保留在合并历史中 |


复现方式（macOS ARM64、Java 21、Apple Clang）：

```bash
cat > /tmp/lattice-pr-enable-test-plugin.gradle <<'GRADLE'
settingsEvaluated { settings -> settings.include(':test-plugin') }
GRADLE
CC=/usr/bin/clang CXX=/usr/bin/clang++ ./gradlew \
  -I /tmp/lattice-pr-enable-test-plugin.gradle \
  :lattice-server:test :test-plugin:compileJava --console=plain

JAVA_HOME=$(/usr/libexec/java_home -v 21) CC=/usr/bin/clang CXX=/usr/bin/clang++ \
cmake -S lattice-native -B lattice-native/build-merged-asan -G Ninja \
  -DCMAKE_BUILD_TYPE=Debug -DLATTICE_BUILD_TESTS=ON -DLATTICE_ENABLE_LTO=OFF \
  -DCMAKE_POLICY_VERSION_MINIMUM=3.5 \
  '-DCMAKE_C_FLAGS=-fsanitize=address,undefined -fno-omit-frame-pointer' \
  '-DCMAKE_CXX_FLAGS=-fsanitize=address,undefined -fno-omit-frame-pointer' \
  '-DCMAKE_EXE_LINKER_FLAGS=-fsanitize=address,undefined' \
  '-DCMAKE_SHARED_LINKER_FLAGS=-fsanitize=address,undefined'
cmake --build lattice-native/build-merged-asan --parallel 6
ctest --test-dir lattice-native/build-merged-asan --output-on-failure

PATH="$(/usr/libexec/java_home -v 21)/bin:$PATH" \
LATTICE_TEST_NATIVE_LIBRARY="$PWD/build/lattice-native/liblattice.dylib" \
python3 scripts/testNativeLoader.py
```

本机 Python 默认 CA 路径缺失，独立脚本首次下载依赖时证书校验失败；设置 `SSL_CERT_FILE="$(python3 -m certifi)"` 后正常执行，未关闭 TLS 校验。`CMAKE_POLICY_VERSION_MINIMUM=3.5` 用于本机 CMake 4.x 与 doctest 的兼容。

构建和测试使用现有 Gradle 依赖及已生成 Minecraft 源码，未重做全部 Paperweight 补丁生成。保留默认 `Slow` 标签排除。Linux/Windows ACL 与跨平台发布矩阵须查看实际 CI；本轮未运行真实多人负载、跨 UID 攻击或进程 RSS 长期稳定性测试。


## 五、完整报告与附件

- [缓存、寻路和 NBT 修复报告及死代码标记](security-review-2026-09-13.md)
- [初次中文审计及逐项修复记录](security-audit-2026-09-13.zh-CN.md)
- [初次英文审计](security-audit-2026-09-13.md)
- [复扫索引](security-audit-2026-09-13-rescan.md)
- [复扫问题报告：攻击条件、调用链、证据及修复建议](security-audit-2026-09-13-rescan/report.md)
- [死代码与未接入功能清单](security-audit-2026-09-13-rescan/dead-code-review.md)
- [扫描清单](security-audit-2026-09-13-rescan/scan-manifest.json)、[发现记录](security-audit-2026-09-13-rescan/findings.json)、[覆盖记录](security-audit-2026-09-13-rescan/coverage.json)、[SARIF](security-audit-2026-09-13-rescan/exports/results.sarif)

按“提交所有本地更改”的范围，提交也保留现有 `.zcode/plans` 审计计划和 `.video_agent/plugin_root` 工具路径记录；它们不参与构建，计划内容不作为最终实现或验证状态的依据。复扫详细目录显式加入版本管理，原始扫描附件保持原样。

## 六、PR 内容归并

两个 PR 基于同一上游提交 `605b199`。代码文件交集只有 `LatticeNativeLoader.java` 的缓存提取实现；英文 PR #1 的其余 10 个文件为独有新增或修改。中文 PR #2 原有的 JNI 边界、解压上限、配置、命令权限、供应链修复与审计材料全部保留。

缓存冲突采用一个统一实现：中文 PR 的可信摘要校验先验证下载字节，英文 PR 的 `NativeLibraryCache` 再在随机私有目录中提取。增加组合回归，确认摘要不匹配时不会创建提取目录，以及旧缓存符号链接不被使用、每次提取目录独立。新加入的三平台 bootstrap 工作流同步固定 Actions SHA，保持现有供应链约束。

英文 PR #1 的三个提交通过非快进合并保留在中文 PR #2 的历史中；#2 为统一审阅入口。原始报告作为历史证据保留，最终行为及剩余事项以本文为准。
