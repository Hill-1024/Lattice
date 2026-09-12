# Lattice 死代码与未接入功能复核

日期：2026-09-13（Asia/Shanghai）。基线提交：605b1991212a29265dc6a60ec0c7ba7a986dc48c，包含扫描开始时已有的未提交修改。只记录与标记，未删除或修改业务源码。

判定结合当前 Java/C++ 源码、CMake 源文件列表、JNI 名称映射、测试消费者及已有的打补丁后 Minecraft 源码。生成源码未重新构建，涉及其调用关系的结论仍需构建复验。公共 API 的仓库外使用无法通过本地搜索排除；“生产无引用”不等于可以直接删除所有底层实现。SIMD 分发、反射、Cleaner 回调和有效可选开关不按零文本引用草率判死。

## 1. 可明确标记的死代码

| 编号 | 位置（仓库相对路径:行） | 证据与处理 |
|---|---|---|
| D01 | `scripts/apatch.sh:73` | `noapply` 初始化和所有赋值均为 1，结尾 `noapply != 1` 恒假，`mv` 分支不可达；`applied` 的计算也随之无效。决定是否恢复原有选项语义，或删除整个分支与无用变量。 |
| D02 | `lattice-server/src/main/java/com/latticemc/lattice/bridge/PathFinderNativeSupport.java:354` | 私有嵌套类 `CachingPathfindingContext` 没有实例化入口；当前寻路使用 state snapshot/mirror。删除类及失效 import。 |
| D03 | `lattice-server/src/main/java/com/latticemc/lattice/bridge/PathfinderStateSnapshot.java:16` | 私有 `descriptorCount` 字段从未读写；同名方法返回 `cache.descriptorCount()`，须保留该方法。 |
| D04 | `lattice-server/src/main/java/com/latticemc/lattice/bridge/HerbivoreAiSupport.java:295` | 私有 `buildFleeCandidates` 只有定义，无调用；删除旧候选生成器。 |
| D05 | `lattice-server/src/main/java/com/latticemc/lattice/util/EntityActivationKdTree.java:161` | `PlayerTree.axis/x/z` 三个成员数组仅分配和写入，查询读取的是包围盒及子节点。删除数组及赋值，保留 `select/coordinate` 的同名局部 axis 参数。 |
| D06 | `lattice-native/src/world/entity/pathfinder.cpp:952` | 952–1076 为 `#if 0` 的旧镜像物化实现，不参与编译。可删除，版本控制仍保存历史。 |
| D07 | `lattice-native/src/world/gen/densityfunction/density_function_avx512.cpp:1` | 主库与测试 CMake 均未加入该文件，使用的 `NodeCapability::kAvx512/has_capability` 已不在当前头文件。标为过时未构建实现；不要顺手删除明确保留兼容用途的 no-op setter。 |
| D08 | `test-plugin/src/main/java/org/purpurmc/testplugin/TestPluginBootstrap.java:1`、`TestPluginLoader.java:1` | 两个空占位类；资源中的 bootstrapper/loader 注册行均为注释。可清理占位类及对应注释。 |
| D09 | `test-plugin/src/activation-bench/java/org/purpurmc/testplugin/activationbench/ActivationBenchBotRunner.java:647` | `Result.states` 只在构造器赋值，序列化使用 snapshots；可删除字段及对应参数，保留运行过程实际使用的局部 states。 |

## 2. 生产无消费者、测试专用或预留代码

| 位置 | 分类与建议 |
|---|---|
| `LatticeNativeLoader.java:252–271` 的 `parseChecksum/verifyChecksum` | 当前生产下载使用 `trustedDigestFor/verifyTrustedDigest`，旧相邻摘要解析只由测试引用。清理旧 helper、专属正则与测试，保留真实 pin 校验测试；`StandardCharsets` import 也未使用。 |
| `PathfinderBuffers.java:7–9,33–50,89–168` | 旧 pathTypes/floorLevels API 没有仓库消费者；RawPathTypeCache 只供测试和 D02 死适配器。对象其他缓存仍在生产使用，不能删除整个类；public API 如需兼容应先弃用。 |
| `PathFinderNativeSupport.java:386` | package-private `isNativePathTypeSupported` 只有测试调用，不控制真实寻路。删除或将确有必要的校验接入真实路径。 |
| `lattice-native/src/io/compression/zlib_codec.cpp` 的 `zlib_validate` | 只有测试消费者，生产使用解压函数。保留为原生测试工具或移出发布模块。 |
| `NativeIoCapability.java:5–10,29–54` | 明确标注的 I/O 能力脚手架，只有测试引用，始终 available=false。与已接入的同步 NativeRegionFileRead 是不同功能。 |
| `NativeLightEngine` / feature patch 0011 | 已有生成源码会分配 native engine，但找不到生产 native 入队调用；Moonrise 实际使用自己的距离 tracker。属于“有无效分配、传播功能未接入”，不是整个类从未执行。移除无效分配或完整重做队列所有权与卸载释放。 |
| `lattice-server/build.gradle.kts:468–523` | 注释掉的旧 fill/rebuild 帮助代码及对应 import 无执行作用；从拥有该内容的构建补丁维护，避免只改生成文件。 |

## 3. 未接入 NBT 解析器中的确定缺陷

`lattice-native/src/io/nbt/nbt_parser.cpp:328` 保留 `Frame& top`，随后 `push_frame`（284–293）可能搬迁并释放栈，380–385 和 429–434 再通过旧引用递减父列表计数。64 帧扩容会修改旧栈副本；128/256 帧扩容会写入已释放堆内存（CWE-416）。

默认深度上限 512 允许到达这些扩容边界。可先用 compound 构造早期层级，再令目标扩容处的父节点为 list，不能用“64 层时已有错误”否定后续 UAF。

当前 `NativeChunkSerializer.java` 明确仅声明 inflate/deflate；没有 NBT parse/free 的 Java native 声明及服务端调用链。因此记录为**确定的休眠内存安全缺陷**，不声称当前玩家可远程利用。建议在 push 前递减父计数，或保存父索引并在扩容后重取引用；接入前补 64/128/256 层 list-of-list 和 list-of-compound 的 sanitizer 回归。另需处理 uint8_t depth 与 512 上限不一致的问题。

现有原生 NBT 测试仅覆盖浅层与标量列表，未覆盖上述增长边界；本次没有执行 sanitizer 或攻击输入。

## 4. 其他待加固项（不算已证实的远程漏洞）

- Heightmap JNI 缺少逐 section 的 packed storage 长度和 bits 校验，部分 mask 乘法仍是 int。当前生产来源是 Minecraft BitStorage；未建立恶意磁盘数据越过上游约束的路径。建议在 pin 前做 checked-count 和完整长度校验。
- 未接入的 random-tick JNI 校验输出长度，却不校验 candidateCount 是否超过输入长度。启用前补齐。
- 部分 density 网格维度乘法、material batch 的 count*5 可溢出；真实生成调用用受限 chunk 尺寸，不能把任意插件参数当作远程玩家输入。
- 原生噪声 Cleaner/raw handle 的存活保证应显式化；未来调整缓存所有权时补 reachabilityFence 或等价所有权保护。
- 测试插件在管理员准备基准后，仅凭 bot 名称前缀授予飞行并传送，清理不恢复飞行；若测试服允许不可信玩家加入，应验证参与者并恢复原状态。隔离的本地测试不是该攻击场景。
- 无依赖锁/完整校验元数据与 SNAPSHOT 仍是供应链加固事项，未做联网 CVE 核查，也未把“未锁定”直接当成已验证漏洞。
- 内置下载摘要表为空，注释所述发布填表步骤未在流水线中找到；当前行为是缺少 pin 时拒绝下载。应补发布摘要注入或更正文档。

## 5. 孤立 JNI 导出清单

下表逐一比对 `Java_com_latticemc_lattice_nativelib_<Class>_<method>` 导出与当前同名 Java 类的 native 声明。另在 main、已有生成源码和 JNI 中检索，未发现替代声明或 RegisterNatives 接线。它证明当前项目 Java 无入口；底层 C++ 可能仍被其他模块或原生测试调用，清理应以导出为单位。


共 **51 个**无对应 Java native 声明的导出。

| 文件:行 | Java 类 | 导出方法 |
|---|---|---|
| `lattice-native/jni/chunk_noise_sampler.cpp:70` | `NativeChunkNoiseSampler` | `nativeCreate` |
| `lattice-native/jni/chunk_noise_sampler.cpp:81` | `NativeChunkNoiseSampler` | `nativeDestroy` |
| `lattice-native/jni/chunk_noise_sampler.cpp:87` | `NativeChunkNoiseSampler` | `nativeSetChannel` |
| `lattice-native/jni/chunk_noise_sampler.cpp:111` | `NativeChunkNoiseSampler` | `nativePrepareCache` |
| `lattice-native/jni/chunk_noise_sampler.cpp:119` | `NativeChunkNoiseSampler` | `nativeClearCache` |
| `lattice-native/jni/chunk_noise_sampler.cpp:127` | `NativeChunkNoiseSampler` | `nativeSampleFinalDensity` |
| `lattice-native/jni/chunk_noise_sampler.cpp:137` | `NativeChunkNoiseSampler` | `nativeSample` |
| `lattice-native/jni/chunk_noise_sampler.cpp:150` | `NativeChunkNoiseSampler` | `nativeNumInterpolatorSlots` |
| `lattice-native/jni/chunk_noise_sampler.cpp:159` | `NativeChunkNoiseSampler` | `nativePrepareInterpolators` |
| `lattice-native/jni/chunk_noise_sampler.cpp:171` | `NativeChunkNoiseSampler` | `nativeStartInterpolation` |
| `lattice-native/jni/chunk_noise_sampler.cpp:180` | `NativeChunkNoiseSampler` | `nativeStopInterpolation` |
| `lattice-native/jni/chunk_noise_sampler.cpp:189` | `NativeChunkNoiseSampler` | `nativeSetStartDensity` |
| `lattice-native/jni/chunk_noise_sampler.cpp:203` | `NativeChunkNoiseSampler` | `nativeSetEndDensity` |
| `lattice-native/jni/chunk_noise_sampler.cpp:217` | `NativeChunkNoiseSampler` | `nativeOnSampledCellCorners` |
| `lattice-native/jni/chunk_noise_sampler.cpp:229` | `NativeChunkNoiseSampler` | `nativeInterpolateY` |
| `lattice-native/jni/chunk_noise_sampler.cpp:238` | `NativeChunkNoiseSampler` | `nativeInterpolateX` |
| `lattice-native/jni/chunk_noise_sampler.cpp:247` | `NativeChunkNoiseSampler` | `nativeInterpolateZ` |
| `lattice-native/jni/chunk_noise_sampler.cpp:256` | `NativeChunkNoiseSampler` | `nativeSwapBuffers` |
| `lattice-native/jni/chunk_noise_sampler.cpp:265` | `NativeChunkNoiseSampler` | `nativeAdvanceColumn` |
| `lattice-native/jni/chunk_noise_sampler.cpp:274` | `NativeChunkNoiseSampler` | `nativeSetDensityRow` |
| `lattice-native/jni/chunk_noise_sampler.cpp:302` | `NativeChunkNoiseSampler` | `nativeFillDensityColumn` |
| `lattice-native/jni/chunk_noise_sampler.cpp:337` | `NativeChunkNoiseSampler` | `nativePrimeFinalDensityColumns` |
| `lattice-native/jni/chunk_noise_sampler.cpp:360` | `NativeChunkNoiseSampler` | `nativeAdvanceFinalDensityColumn` |
| `lattice-native/jni/chunk_noise_sampler.cpp:381` | `NativeChunkNoiseSampler` | `nativePrimeChannelColumns` |
| `lattice-native/jni/chunk_noise_sampler.cpp:406` | `NativeChunkNoiseSampler` | `nativeAdvanceChannelColumn` |
| `lattice-native/jni/chunk_noise_sampler.cpp:429` | `NativeChunkNoiseSampler` | `nativeSampleFinalDensityCellGrid` |
| `lattice-native/jni/chunk_noise_sampler.cpp:463` | `NativeChunkNoiseSampler` | `nativeSampleCellGrid` |
| `lattice-native/jni/chunk_serializer.cpp:263` | `NativeChunkSerializer` | `nativeInflateZlibInto` |
| `lattice-native/jni/chunk_serializer.cpp:405` | `NativeChunkSerializer` | `nativeZlibCompressBound` |
| `lattice-native/jni/chunk_serializer.cpp:433` | `NativeChunkSerializer` | `nativeParseNbtIndex` |
| `lattice-native/jni/chunk_serializer.cpp:527` | `NativeChunkSerializer` | `nativeFreeNbtIndex` |
| `lattice-native/jni/density_function.cpp:701` | `NativeDensityFunction` | `nativeEvaluate` |
| `lattice-native/jni/density_function.cpp:712` | `NativeDensityFunction` | `nativeEvaluateCached` |
| `lattice-native/jni/density_function.cpp:1020` | `NativeDensityFunction` | `nativeAddLerp` |
| `lattice-native/jni/density_function.cpp:1162` | `NativeDensityFunction` | `nativeAddEndIslands` |
| `lattice-native/jni/density_function.cpp:1382` | `NativeDensityFunction` | `nativeEvaluateYColumns` |
| `lattice-native/jni/density_function.cpp:2539` | `NativeDensityFunction` | `nativeNumInterpolatorSlots` |
| `lattice-native/jni/density_function.cpp:2557` | `NativeDensityFunction` | `nativeStartInterpolation` |
| `lattice-native/jni/density_function.cpp:2565` | `NativeDensityFunction` | `nativeStopInterpolation` |
| `lattice-native/jni/density_function.cpp:2573` | `NativeDensityFunction` | `nativeSetStartDensity` |
| `lattice-native/jni/density_function.cpp:2585` | `NativeDensityFunction` | `nativeSetEndDensity` |
| `lattice-native/jni/density_function.cpp:2897` | `NativeDensityFunction` | `nativeSetInterpolatorColumnPacked` |
| `lattice-native/jni/density_function.cpp:2934` | `NativeDensityFunction` | `nativeSwapBuffers` |
| `lattice-native/jni/density_function.cpp:2942` | `NativeDensityFunction` | `nativeOnSampledCellCorners` |
| `lattice-native/jni/density_function.cpp:2951` | `NativeDensityFunction` | `nativeInterpolateY` |
| `lattice-native/jni/density_function.cpp:2959` | `NativeDensityFunction` | `nativeInterpolateX` |
| `lattice-native/jni/density_function.cpp:2967` | `NativeDensityFunction` | `nativeInterpolateZ` |
| `lattice-native/jni/material_rules.cpp:84` | `NativeMaterialRules` | `nativeAddCondAlwaysTrue` |
| `lattice-native/jni/material_rules.cpp:92` | `NativeMaterialRules` | `nativeAddCondAboveY` |
| `lattice-native/jni/palette_ops.cpp:268` | `NativePaletteOps` | `nativeCountUnique` |
| `lattice-native/jni/tick.cpp:24` | `NativeRandomTickFilter` | `nativeFilterRandomTicks` |


## 6. 补充标记

| 位置 | 判定与处理 |
|---|---|
| `src/minecraft/java/net/minecraft/world/entity/ai/sensing/VillagerBabiesSensor.java:38` | 私有 `isVillagerBaby` 只有定义；当前调用直接检查类型和 baby。应修改拥有该变化的 feature patch 0097，再生成源码。 |
| `src/minecraft/java/net/minecraft/world/entity/item/ItemEntity.java:286` | 单参数 `tryToMerge(ItemEntity)` 只有定义，当前调用三参数重载；修改 feature patch 0118 清理。 |
| `src/minecraft/java/net/minecraft/world/entity/ai/goal/GoalSelector.java:84` | 私有 `goalContainsAnyFlags` 无调用，两条有效 tick 路径直接检查 flags；在所属补丁中清理。以上三个生成路径均相对于 `lattice-server/`。 |
| `lattice-native/src/world/tick/random_tick_filter.cpp:46` | `packed & 0xFFF` 之后的 `local_idx >= 4096` 分支恒假。 |
| `lattice-native/src/world/tick/random_tick_filter.cpp:20` | 前面拒绝 bits>32，后面的 bits>=64 三元分支不可达。 |
| `lattice-native/src/world/light/packed_info.hpp:1` | 没有 include 或其命名空间的消费者，是未引用头文件。 |
| `lattice-native/src/world/light/block_light_engine.cpp:430` | 核心 block-light 实现只被原生测试使用，却编入生产库；与有 JNI 导出的 LevelPropagator 区分，不能删除整个 light 目录。 |
| `lattice-native/src/world/gen/orevein/ore_vein.cpp:93` | `sample_grid` 生产无消费者，JNI 直接调用 sample_at；如需保留 C++ API 则明确说明。 |
| `lattice-server/src/main/java/com/latticemc/lattice/nativelib/NativeHomeTargetSampler.java:3` | 没有仓库生产消费者，仅 Java fallback 测试；按公开预留 API 处理兼容性后再移出发布。 |
| `lattice-server/src/main/java/com/latticemc/lattice/nativelib/NativePathfinder.java:402` | 旧 raw-cache/unsupported-path 统计更新只由测试调用；旧 raw-grid findPath 供 parity 测试。保留仍在使用的 snapshot/mirror 接口，不应只因 stats 展示仍读取字段就认定旧事件仍会更新。 |

## 7. 审计边界与后续验证

本次是源码与调用关系复核，不是运行时覆盖或二进制死代码消除分析。未执行服务器、构建、测试、PoC 或联网依赖审计。完整阅读 282/478 个本地源码、补丁和构建文件，196 个为局部阅读或搜索；上游实现与生成源码仅在相关调用链中核查。所有剩余路径已记录在安全扫描的 coverage.json 中。

清理顺序建议：先处理安全报告中的两个生产风险；修好休眠 NBT 解析器；删除 private/恒假/未编译残留；再对 public API 和 JNI 导出做兼容性处理。清理后跑 Java 编译、JNI 声明与导出一致性检查、CMake/CTest、真实路径 parity 测试，尤其避免仅通过测试专用 helper 的测试即认为生产约束有效。
