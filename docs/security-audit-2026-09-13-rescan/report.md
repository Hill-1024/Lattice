# Security Review: Lattice

## Scope

对当前Lattice工作区执行标准静态安全审计及死代码分析，包含已有未提交修改。

- Scan mode: repository
- Target kind: git_worktree
- Target ID: target_sha256_a4ba0dd2e738eaceb1340aad9e964d59b9a6e978ae407a78f3e0826a565b3711
- Revision: 605b1991212a29265dc6a60ec0c7ba7a986dc48c
- Snapshot digest: codex-security-snapshot/v1:sha256:b837220ffc5abc7a7348bcaafc63108672285a1d92215f71ba87e18cdb46833d
- Inventory strategy: repository
- Included paths: .
- Excluded paths: none
- Runtime or test status: 未运行服务器、测试、攻击复现或sanitizer。
- Artifacts reviewed: .github/dependabot.yml, .github/workflows/build.yml, .github/workflows/native.yml, .github/workflows/upstream.yml, .gitignore, README.md, build-data/dev-imports.txt, build-data/tentacles.at, build.gradle.kts, gradle.properties, gradle/wrapper/gradle-wrapper.properties, lattice-api/build.gradle.kts, lattice-api/build.gradle.kts.patch, lattice-native/CMakeLists.txt, lattice-native/jni/beardifier.cpp, lattice-native/jni/biological_ai.cpp, lattice-native/jni/brain_eligibility.cpp, lattice-native/jni/chunk_noise_sampler.cpp, lattice-native/jni/chunk_serializer.cpp, lattice-native/jni/double_perlin_noise.cpp, lattice-native/jni/entity_query.cpp, lattice-native/jni/entity_visibility.cpp, lattice-native/jni/heightmap.cpp, lattice-native/jni/interpolated_noise.cpp, lattice-native/jni/jni_helper.hpp, lattice-native/jni/light_engine.cpp, lattice-native/jni/los.cpp, lattice-native/jni/material_rules.cpp, lattice-native/jni/native_region_file_read.cpp, lattice-native/jni/noise_handle.hpp, lattice-native/jni/octave_perlin_noise.cpp, lattice-native/jni/ore_vein.cpp, lattice-native/jni/palette_ops.cpp, lattice-native/jni/pathfinder.cpp, lattice-native/jni/perlin_noise.cpp, lattice-native/jni/simplex_noise.cpp, lattice-native/jni/tick.cpp, lattice-native/src/io/compression/zlib_codec.cpp, lattice-native/src/io/compression/zlib_codec.hpp, lattice-native/src/io/nbt/nbt_parser.cpp, lattice-native/src/io/nbt/nbt_parser.hpp, lattice-native/src/world/entity/aabb_query.cpp, lattice-native/src/world/entity/aabb_query.hpp, lattice-native/src/world/entity/aabb_query_avx2.cpp, lattice-native/src/world/entity/aabb_query_avx512.cpp, lattice-native/src/world/entity/aabb_query_neon.cpp, lattice-native/src/world/entity/approach_target_sampler.cpp, lattice-native/src/world/entity/approach_target_sampler.hpp, lattice-native/src/world/entity/biological_ai.cpp, lattice-native/src/world/entity/biological_ai.hpp, lattice-native/src/world/entity/brain_eligibility.cpp, lattice-native/src/world/entity/brain_eligibility.hpp, lattice-native/src/world/entity/brain_eligibility_neon.cpp, lattice-native/src/world/entity/collision_sweep.cpp, lattice-native/src/world/entity/collision_sweep.hpp, lattice-native/src/world/entity/collision_sweep_avx2.cpp, lattice-native/src/world/entity/collision_sweep_avx512.cpp, lattice-native/src/world/entity/collision_sweep_neon.cpp, lattice-native/src/world/entity/entity_query.cpp, lattice-native/src/world/entity/entity_query.hpp, lattice-native/src/world/entity/flee_target_sampler.cpp, lattice-native/src/world/entity/flee_target_sampler.hpp, lattice-native/src/world/entity/home_target_sampler.cpp, lattice-native/src/world/entity/home_target_sampler.hpp, lattice-native/src/world/entity/los.cpp, lattice-native/src/world/entity/los.hpp, lattice-native/src/world/entity/pathfinder.cpp, lattice-native/src/world/entity/pathfinder.hpp, lattice-native/src/world/entity/pathfinder_avx2.cpp, lattice-native/src/world/entity/pathfinder_avx512.cpp, lattice-native/src/world/entity/pathfinder_neon.cpp, lattice-native/src/world/entity/spawn_filter.cpp, lattice-native/src/world/entity/spawn_filter.hpp, lattice-native/src/world/entity/visibility_scan.cpp, lattice-native/src/world/entity/visibility_scan.hpp, lattice-native/src/world/entity/visibility_scan_avx2.cpp, lattice-native/src/world/entity/visibility_scan_avx512.cpp, lattice-native/src/world/entity/visibility_scan_neon.cpp, lattice-native/src/world/entity/water_target_sampler.cpp, lattice-native/src/world/entity/water_target_sampler.hpp, lattice-native/src/world/gen/densityfunction/beardifier.cpp, lattice-native/src/world/gen/densityfunction/beardifier.hpp, lattice-native/src/world/gen/densityfunction/spline.hpp, lattice-native/src/world/gen/noise/double_perlin_noise.cpp, lattice-native/src/world/gen/noise/double_perlin_noise.hpp, lattice-native/src/world/gen/noise/double_perlin_noise_avx2.cpp, lattice-native/src/world/gen/noise/double_perlin_noise_avx512.cpp, lattice-native/src/world/gen/noise/interpolated_noise.cpp, lattice-native/src/world/gen/noise/interpolated_noise.hpp, lattice-native/src/world/gen/noise/octave_perlin_noise.cpp, lattice-native/src/world/gen/noise/octave_perlin_noise.hpp, lattice-native/src/world/gen/noise/perlin_noise.cpp, lattice-native/src/world/gen/noise/perlin_noise.hpp, lattice-native/src/world/gen/noise/simplex_noise.cpp, lattice-native/src/world/gen/noise/simplex_noise.hpp, lattice-native/src/world/gen/noise/simplex_noise_avx2.cpp, lattice-native/src/world/gen/orevein/ore_vein.cpp, lattice-native/src/world/gen/orevein/ore_vein.hpp, lattice-native/src/world/gen/rng/xoroshiro128pp.hpp, lattice-native/src/world/gen/surfacebuilder/material_rules.cpp, lattice-native/src/world/gen/surfacebuilder/material_rules.hpp, lattice-native/src/world/heightmap/heightmap_scan.cpp, lattice-native/src/world/heightmap/heightmap_scan.hpp, lattice-native/src/world/heightmap/heightmap_scan_avx2.cpp, lattice-native/src/world/heightmap/heightmap_scan_neon.cpp, lattice-native/src/world/light/block_light_engine.cpp, lattice-native/src/world/light/block_light_engine.hpp, lattice-native/src/world/light/chunk_light_provider.hpp, lattice-native/src/world/light/int64_set.hpp, lattice-native/src/world/light/level_propagator.cpp, lattice-native/src/world/light/level_propagator.hpp, lattice-native/src/world/light/long_to_byte_map.hpp, lattice-native/src/world/light/packed_info.hpp, lattice-native/src/world/light/pending_update_queue.hpp, lattice-native/src/world/palette/packed_storage.cpp, lattice-native/src/world/palette/packed_storage.hpp, lattice-native/src/world/palette/packed_storage_bmi2.cpp, lattice-native/src/world/palette/packed_storage_neon.cpp, lattice-native/src/world/palette/packed_storage_simd_inl.hpp, lattice-native/src/world/tick/random_tick_filter.cpp, lattice-native/src/world/tick/random_tick_filter.hpp, lattice-native/tests/CMakeLists.txt, lattice-native/tests/test_heightmap.cpp, lattice-native/tests/test_jni_bounds.cpp, lattice-native/tests/test_nbt_parser.cpp, lattice-native/tests/test_palette.cpp, lattice-native/tests/test_random_tick_filter.cpp, lattice-native/tests/test_zlib_codec.cpp, lattice-server/build.gradle.kts, lattice-server/build.gradle.kts.patch, lattice-server/minecraft-patches/features/0002-Accelerate-palette-storage-and-region-file-I-O-with-.patch, lattice-server/minecraft-patches/features/0003-Route-path-finding-through-native-support.patch, lattice-server/minecraft-patches/features/0005-Route-entity-queries-through-the-native-entity-index.patch, lattice-server/minecraft-patches/features/0006-Accelerate-entity-sensors-with-native-queries.patch, lattice-server/minecraft-patches/features/0007-Accelerate-tracked-entity-visibility-checks-with-nat.patch, lattice-server/minecraft-patches/features/0008-Route-tagged-nearest-entity-queries-through-native-l.patch, lattice-server/minecraft-patches/features/0009-Accelerate-line-of-sight-checks-with-native-code.patch, lattice-server/minecraft-patches/features/0010-Accelerate-entity-collision-sweeps-with-native-code.patch, lattice-server/minecraft-patches/features/0011-Accelerate-light-propagation-and-spawn-checks-with-n.patch, lattice-server/minecraft-patches/features/0013-Route-biological-AI-through-explicit-animal-support.patch, lattice-server/minecraft-patches/features/0021-Cache-the-BlockState-to-PathType-mapping.patch, lattice-server/minecraft-patches/features/0022-Measure-native-and-vanilla-pathfinder-costs.patch, lattice-server/minecraft-patches/features/0023-Expose-BlockState-path-type-classification.patch, lattice-server/minecraft-patches/features/0024-Invalidate-pathfinder-static-cache-on-block-updates.patch, lattice-server/minecraft-patches/features/0025-perf-reuse-SoA-buffers-for-entity-AABB-queries.patch, lattice-server/minecraft-patches/features/0026-perf-scan-dense-Moonrise-entity-sections-natively.patch, lattice-server/minecraft-patches/features/0027-perf-cache-native-entity-AABB-section-planes.patch, lattice-server/minecraft-patches/features/0028-perf-limit-AABB-cache-updates-to-collision-indexes.patch, lattice-server/minecraft-patches/features/0029-perf-spatially-index-dense-entity-AABB-queries.patch, lattice-server/minecraft-patches/features/0030-perf-cap-push-collision-entity-queries.patch, lattice-server/minecraft-patches/features/0033-perf-skip-local-entity-chunk-load-lookups.patch, lattice-server/minecraft-patches/features/0036-perf-reuse-entity-chunk-status-for-light-checks.patch, lattice-server/minecraft-patches/features/0039-perf-prefilter-block-removal-goal-searches.patch, lattice-server/minecraft-patches/features/0040-perf-optimize-goal-selector-hot-loops.patch, lattice-server/minecraft-patches/features/0047-perf-avoid-scanning-unused-TPS-windows.patch, lattice-server/minecraft-patches/features/0048-perf-reuse-inside-block-effect-collector-storage.patch, lattice-server/minecraft-patches/features/0049-perf-flatten-pushable-entity-predicate.patch, lattice-server/minecraft-patches/features/0052-perf-make-vanilla-profiler-optional.patch, lattice-server/minecraft-patches/features/0065-perf-cache-loaded-chunks-during-inside-block-travers.patch, lattice-server/minecraft-patches/features/0066-perf-remove-spatial-entity-reverse-hash.patch, lattice-server/minecraft-patches/features/0067-Optimize-spatial-cell-bucket-lookup.patch, lattice-server/minecraft-patches/features/0070-perf-gate-goal-selector-locked-priority-cache.patch, lattice-server/minecraft-patches/features/0073-Reapply-perf-cache-repeated-spatial-cell-bucket-look.patch, lattice-server/minecraft-patches/features/0074-perf-replace-brain-maps-with-optimized-collections.patch, lattice-server/minecraft-patches/features/0075-perf-gate-native-brain-eligibility-by-memory-version.patch, lattice-server/minecraft-patches/features/0076-perf-add-Leaf-KD-tree-entity-activation.patch, lattice-server/minecraft-patches/features/0077-perf-shrink-entity-AABB-query-cache-footprint.patch, lattice-server/minecraft-patches/features/0078-perf-presize-brain-behavior-groups.patch, lattice-server/minecraft-patches/features/0079-perf-replace-dense-entity-reverse-hash-with-direct-I.patch, lattice-server/minecraft-patches/features/0080-perf-entity-stream-bounded-pushable-collision-querie.patch, lattice-server/minecraft-patches/features/0081-perf-entity-accelerate-streamed-push-queries.patch, lattice-server/minecraft-patches/features/0082-perf-entity-retain-limited-push-query-without-crammi.patch, lattice-server/minecraft-patches/features/0083-perf-entity-compact-fluid-height-storage.patch, lattice-server/minecraft-patches/features/0084-perf-entity-avoid-single-chunk-fluid-scan-arrays.patch, lattice-server/minecraft-patches/features/0085-perf-entity-reuse-inside-block-step-scratch.patch, lattice-server/minecraft-patches/features/0086-perf-entity-reuse-block-scan-sets.patch, lattice-server/minecraft-patches/features/0087-perf-entity-bound-block-scan-scratch-retention.patch, lattice-server/minecraft-patches/features/0088-perf-entity-reuse-fluid-chunk-availability.patch, lattice-server/minecraft-patches/features/0089-perf-hopper-reuse-item-query-snapshots.patch, lattice-server/minecraft-patches/features/0090-perf-item-avoid-transient-velocity-vector.patch, lattice-server/minecraft-patches/features/0091-perf-collision-reuse-hard-collision-query-snapshots.patch, lattice-server/minecraft-patches/features/0092-perf-entity-reuse-collision-work-buffers.patch, lattice-server/minecraft-patches/features/0093-perf-collision-reuse-entity-collision-candidates.patch, lattice-server/minecraft-patches/features/0094-perf-entity-short-circuit-frog-attack-checks.patch, lattice-server/minecraft-patches/features/0095-perf-explosion-skip-final-ray-coordinate-update.patch, lattice-server/minecraft-patches/features/0096-perf-ai-precheck-non-POI-repellent-sections.patch, lattice-server/minecraft-patches/features/0097-Leaf-0165-0181-cache-positions-and-optimize-villager.patch, lattice-server/minecraft-patches/features/0098-Leaf-0195-optimize-visible-living-entity-selection.patch, lattice-server/minecraft-patches/features/0099-perf-inventory-remove-iterator-allocations.patch, lattice-server/minecraft-patches/features/0100-feat-region-use-native-positioned-payload-reads.patch, lattice-server/minecraft-patches/features/0101-docs-provenance-pin-Leaf-source-URLs.patch, lattice-server/minecraft-patches/features/0102-perf-tracker-skip-unchanged-motion-distance-checks.patch, lattice-server/minecraft-patches/features/0103-Use-ChunkMap-specific-visibility-check.patch, lattice-server/minecraft-patches/features/0104-perf-item-cache-max-stack-size-component.patch, lattice-server/minecraft-patches/features/0105-perf-entity-shortcut-sparse-visibility-replay.patch, lattice-server/minecraft-patches/features/0106-perf-entity-configure-native-visibility-player-gate.patch, lattice-server/minecraft-patches/features/0107-perf-entity-resolve-visibility-gate-at-startup.patch, lattice-server/minecraft-patches/features/0114-perf-los-expose-batched-sensing-queries.patch, lattice-server/minecraft-patches/features/0115-fix-los-deduplicate-batched-sensing-requests.patch, lattice-server/minecraft-patches/features/0116-perf-los-batch-nearest-item-visibility-checks.patch, lattice-server/minecraft-patches/features/0118-perf-item-skip-full-stack-merge-checks.patch, lattice-server/minecraft-patches/sources/net/minecraft/server/level/ServerLevel.java.patch, lattice-server/minecraft-patches/sources/net/minecraft/world/entity/ai/sensing/NearestLivingEntitySensor.java.patch, lattice-server/minecraft-patches/sources/net/minecraft/world/level/levelgen/Beardifier.java.patch, lattice-server/paper-patches/features/0001-Rebrand.patch, lattice-server/paper-patches/features/0003-Optimize-absent-chunk-checks.patch, lattice-server/paper-patches/features/0004-Optimize-CraftPlayer-canSee-checks.patch, lattice-server/src/main/java/com/latticemc/lattice/bootstrap/LatticeBootstrap.java, lattice-server/src/main/java/com/latticemc/lattice/bootstrap/LatticeNativeLoader.java, lattice-server/src/main/java/com/latticemc/lattice/bootstrap/NativeZlibStreams.java, lattice-server/src/main/java/com/latticemc/lattice/bridge/AnimalBiologicalAiSupport.java, lattice-server/src/main/java/com/latticemc/lattice/bridge/AquaticAiSupport.java, lattice-server/src/main/java/com/latticemc/lattice/bridge/HerbivoreAiSupport.java, lattice-server/src/main/java/com/latticemc/lattice/bridge/LightEngineCallbacks.java, lattice-server/src/main/java/com/latticemc/lattice/bridge/NativeGoalQuerySupport.java, lattice-server/src/main/java/com/latticemc/lattice/bridge/NativeLightEngineBridge.java, lattice-server/src/main/java/com/latticemc/lattice/bridge/PathFinderNativeSupport.java, lattice-server/src/main/java/com/latticemc/lattice/bridge/PathfinderBuffers.java, lattice-server/src/main/java/com/latticemc/lattice/bridge/PathfinderStateSnapshot.java, lattice-server/src/main/java/com/latticemc/lattice/bridge/PathfinderStaticCache.java, lattice-server/src/main/java/com/latticemc/lattice/bridge/PathfinderTickStateCache.java, lattice-server/src/main/java/com/latticemc/lattice/bridge/PollinatorAiSupport.java, lattice-server/src/main/java/com/latticemc/lattice/bridge/PredatoryAnimalAiSupport.java, lattice-server/src/main/java/com/latticemc/lattice/bridge/WorldgenNativeSupport.java, lattice-server/src/main/java/com/latticemc/lattice/command/LatticeDensityCommand.java, lattice-server/src/main/java/com/latticemc/lattice/config/LatticeConfig.java, lattice-server/src/main/java/com/latticemc/lattice/nativelib/BiologicalAiProfiles.java, lattice-server/src/main/java/com/latticemc/lattice/nativelib/ChunkDataFastCheck.java, lattice-server/src/main/java/com/latticemc/lattice/nativelib/NativeAabbQuery.java, lattice-server/src/main/java/com/latticemc/lattice/nativelib/NativeApproachTargetSampler.java, lattice-server/src/main/java/com/latticemc/lattice/nativelib/NativeBeardifier.java, lattice-server/src/main/java/com/latticemc/lattice/nativelib/NativeBiologicalAi.java, lattice-server/src/main/java/com/latticemc/lattice/nativelib/NativeBrainEligibility.java, lattice-server/src/main/java/com/latticemc/lattice/nativelib/NativeChunkSerializer.java, lattice-server/src/main/java/com/latticemc/lattice/nativelib/NativeCollisionSweep.java, lattice-server/src/main/java/com/latticemc/lattice/nativelib/NativeDoublePerlinNoise.java, lattice-server/src/main/java/com/latticemc/lattice/nativelib/NativeEntityQuery.java, lattice-server/src/main/java/com/latticemc/lattice/nativelib/NativeEntityVisibility.java, lattice-server/src/main/java/com/latticemc/lattice/nativelib/NativeFleeTargetSampler.java, lattice-server/src/main/java/com/latticemc/lattice/nativelib/NativeHeightmap.java, lattice-server/src/main/java/com/latticemc/lattice/nativelib/NativeHomeTargetSampler.java, lattice-server/src/main/java/com/latticemc/lattice/nativelib/NativeInterpolatedNoise.java, lattice-server/src/main/java/com/latticemc/lattice/nativelib/NativeIoCapability.java, lattice-server/src/main/java/com/latticemc/lattice/nativelib/NativeLightEngine.java, lattice-server/src/main/java/com/latticemc/lattice/nativelib/NativeLineOfSight.java, lattice-server/src/main/java/com/latticemc/lattice/nativelib/NativeMaterialRules.java, lattice-server/src/main/java/com/latticemc/lattice/nativelib/NativeOctavePerlinNoise.java, lattice-server/src/main/java/com/latticemc/lattice/nativelib/NativePaletteOps.java, lattice-server/src/main/java/com/latticemc/lattice/nativelib/NativePathfinder.java, lattice-server/src/main/java/com/latticemc/lattice/nativelib/NativePerlinNoise.java, lattice-server/src/main/java/com/latticemc/lattice/nativelib/NativeRegionFileRead.java, lattice-server/src/main/java/com/latticemc/lattice/nativelib/NativeSimplexNoise.java, lattice-server/src/main/java/com/latticemc/lattice/nativelib/NativeSpawnFilter.java, lattice-server/src/main/java/com/latticemc/lattice/nativelib/NativeTargetSamplerGate.java, lattice-server/src/main/java/com/latticemc/lattice/nativelib/NativeWaterTargetSampler.java, lattice-server/src/main/java/com/latticemc/lattice/nativelib/NativeWorldgenToggle.java, lattice-server/src/main/java/com/latticemc/lattice/nativelib/PathfinderJfrEvent.java, lattice-server/src/main/java/com/latticemc/lattice/nativelib/WorldStateSnapshot.java, lattice-server/src/main/java/com/latticemc/lattice/util/EntityActivationKdTree.java, lattice-server/src/main/java/com/latticemc/lattice/util/EntityDistanceRadixSort.java, lattice-server/src/main/java/com/latticemc/lattice/util/collection/ActivityArrayMap.java, lattice-server/src/main/java/com/latticemc/lattice/util/collection/ActivityBitSet.java, lattice-server/src/main/java/com/latticemc/lattice/util/collection/ActivityRegistryIndex.java, lattice-server/src/main/java/com/latticemc/lattice/util/collection/BehaviorControlArraySet.java, lattice-server/src/main/java/com/latticemc/lattice/world/EntityPushState.java, lattice-server/src/test/java/com/latticemc/lattice/bootstrap/LatticeBootstrapTestSuite.java, lattice-server/src/test/java/com/latticemc/lattice/bootstrap/LatticeNativeLoaderTestSuite.java, lattice-server/src/test/java/com/latticemc/lattice/config/LatticeConfigTestSuite.java, lattice-server/src/test/java/com/latticemc/lattice/nativelib/NativeIoCapabilityTestSuite.java, lattice-server/src/test/java/com/latticemc/lattice/nativelib/NativeRegionFileReadTestSuite.java, lattice-server/src/test/java/net/minecraft/world/level/levelgen/SurfaceRegionParityTest.java, lattice-server/src/test/java/net/minecraft/world/level/levelgen/SurfaceRegionParityTestSuite.java, scripts/apatch.sh, scripts/updateUpstream.sh, scripts/upstreamCommit.sh, settings.gradle.kts, test-plugin/activation-bench/README.md, test-plugin/build.gradle.kts, test-plugin/src/activation-bench/java/org/purpurmc/testplugin/activationbench/ActivationBenchBotRunner.java, test-plugin/src/main/java/org/purpurmc/testplugin/EntityActivationBenchmarkCommand.java, test-plugin/src/main/java/org/purpurmc/testplugin/ItemBenchmarkCommand.java, test-plugin/src/main/java/org/purpurmc/testplugin/PathfinderBenchmarkCommand.java, test-plugin/src/main/java/org/purpurmc/testplugin/TestPlugin.java, test-plugin/src/main/java/org/purpurmc/testplugin/TestPluginBootstrap.java, test-plugin/src/main/java/org/purpurmc/testplugin/TestPluginLoader.java, test-plugin/src/main/resources/paper-plugin.yml

Limitations and exclusions:
- 按本地源码/补丁/构建文件清单，完整审阅282/478个，另196个仅部分审阅或搜索。未完整覆盖上游实现。
- 没有全量无漏洞保证，详细未完成路径保存在coverage.deferred。
- 报告以扫描开始时工作区为准；没有修改业务源码。
- Excluded \*\*/build/\*\*, .gradle/\*\*, \*\*/.git/\*\*: 构建缓存与仓库元数据不作为独立应用实现审计；仅使用已有生成 Minecraft 源码验证相关调用链。
- Excluded paper-api/\*\*, paper-server/\*\*, purpur-api/\*\*, purpur-server/\*\*: 未完整审计上游供应的所有实现；以 Lattice 自有源码和补丁为重点，必要调用方作局部佐证。
- Excluded .video_agent/\*\*, .zcode/\*\*, docs/\*\*, licenses/\*\*: 辅助会话资料、旧报告及许可证不是当前产品执行入口；旧报告仅作待复核线索。
- Excluded gradle/wrapper/gradle-wrapper.jar: 检查了下载摘要配置，没有反编译或证明 wrapper 二进制身份。

### Scan Summary

| Field | Value |
| --- | --- |
| Scan outcome | completed |
| Reportable findings | 2 |
| Severity mix | medium: 2 |
| Confidence mix | high: 1, medium: 1 |
| Coverage | partial |
| Validation mode | offline static source trace |

Canonical artifacts: `scan-manifest.json`, `findings.json`, and `coverage.json`. This report is a deterministic projection of those files.

## Threat Model

Lattice is a Java 21 Minecraft server fork of Purpur/Paper with C++20 JNI acceleration, distributed as a patched Paperclip JAR plus platform-specific native libraries (README.md:9-25, README.md:39-80). Paperweight composes upstream Purpur and Lattice source/feature patches (build.gradle.kts:11-36; lattice-server/build.gradle.kts:22-59). Both CraftBukkit main and MinecraftServer runServer invoke an idempotent bootstrap that preloads startup configuration before loading native code (lattice-server/paper-patches/features/0002-feat-initialize-Lattice-native-acceleration-at-start.patch:14-17; lattice-server/minecraft-patches/features/0002-Accelerate-palette-storage-and-region-file-I-O-with-.patch:15-19; lattice-server/src/main/java/com/latticemc/lattice/bootstrap/LatticeBootstrap.java:16-28). Server simulation and chunk persistence invoke Java wrappers and JNI; runtime acceleration controls are exposed through a permission-checked Bukkit command. Build, native publication, scheduled upstream updates, optional benchmark clients, and private PGO are separate operator/developer workflows. This model is offline source architecture analysis, not completed vulnerability validation.

### Assets

- Server process integrity and all files accessible to its OS account: native libraries execute inside the JVM through System.load, including an explicit trusted-administrator override (lattice-server/src/main/java/com/latticemc/lattice/bootstrap/LatticeNativeLoader.java:122-160).
- Native executable authenticity and extraction-cache integrity. The loaded file is \<cache\>/\<platform-lib-name\>.\<first-16-hex-of-full-SHA256\>; the full digest is checked on reuse and after extraction (lattice-server/src/main/java/com/latticemc/lattice/bootstrap/LatticeNativeLoader.java:361-398).
- World/chunk persistence integrity and server availability across RegionFile reads and zlib compression. Native positioned reads use a separate read-only OS handle; the Java RegionFile retains write/force/close ordering (lattice-server/minecraft-patches/features/0100-feat-region-use-native-positioned-payload-reads.patch:88-112,118-162; lattice-native/jni/native_region_file_read.cpp:123-157).
- In-memory entity state, player-specific visibility, native allocation lifetime, and JVM stability. JNI transfers primitive arrays and owned direct buffers, while entity tracking retains Java visibility decisions after native distance prefiltering (lattice-native/jni/jni_helper.hpp:103-164; lattice-server/minecraft-patches/features/0007-Accelerate-tracked-entity-visibility-checks-with-nat.patch:25-70; lattice-server/minecraft-patches/features/0103-Use-ChunkMap-specific-visibility-check.patch:12-18).
- Repository contents, released server JARs, native release assets, and CI publication authority. Build execution uses contents:read; separate publication jobs use contents:write ( .github/workflows/build.yml:31-35,151-176; .github/workflows/native.yml:16-17,257-318).
- Administrator-controlled startup settings in \<server-working-directory\>/lattice.yml, JVM properties, optional native-library path/digest, and runtime acceleration/profiling controls (lattice-server/src/main/java/com/latticemc/lattice/config/LatticeConfig.java:25-47,106-163; lattice-server/src/main/java/com/latticemc/lattice/command/LatticeDensityCommand.java:32-66).

### Trust Boundaries

- Server operator -\> bootstrap: valid explicit JVM properties take precedence over lattice.yml, followed by defaults; invalid explicit values fall back to YAML/defaults. Bootstrap runs configuration before static native consumers initialize. Configuration-file permissions are host-owned rather than a Lattice authorization boundary (lattice-server/src/main/java/com/latticemc/lattice/config/LatticeConfig.java:116-163; lattice-server/src/main/java/com/latticemc/lattice/bootstrap/LatticeBootstrap.java:16-21).
- Classpath/build publisher -\> native executable: the normal path loads META-INF/native/\<os\>-\<arch\>/\<libname\>, then a root-classpath \<libname\> fallback. The build bundles only its current host-platform library. Classpath content is trusted executable supply-chain input; extraction hashing protects cache consistency but does not independently authenticate a malicious JAR (build.gradle.kts:95-114,180-185; lattice-server/src/main/java/com/latticemc/lattice/bootstrap/LatticeNativeLoader.java:142-174,361-398).
- Release origin -\> native executable: only when both bundled resources are absent and downloads are enabled, the loader resolves releaseBaseUrl/release/asset and requires a locally trusted SHA-256. Default URL is https://github.com/LatticeMC/Lattice/releases/download/native-latest/lattice-native-\<os\>-\<arch\>.\<ext\>. HTTPS, up to five redirects, allowed GitHub/CDN redirect hosts, and digest equality are enforced unless the operator enables the documented insecure-transport override. The built-in digest map is currently empty, so default unbundled downloads refuse to load without an operator pin (lattice-server/src/main/java/com/latticemc/lattice/bootstrap/LatticeNativeLoader.java:29-60,178-244,273-350).
- Local filesystem actors -\> executable cache: default cache is \<java.io.tmpdir\>/lattice-native-\<sanitized-user.name\>, with an administrator cacheDir override. The final directory must not itself be a symlink; POSIX 0700 is attempted, but permission-setting failures are ignored and no Windows ACL is set. Target files are content-addressed, checked without following a target symlink on reuse, and moved from a same-directory temporary file before post-write hashing. Effective parent-directory ownership and filesystem ACLs remain deployment prerequisites (lattice-server/src/main/java/com/latticemc/lattice/bootstrap/LatticeNativeLoader.java:361-428).
- Players/world state -\> Java acceleration wrappers -\> native memory: player positions and world-derived arrays reach JNI operations through server patches. JNI owns array validation, conversion, native lifetime, and Java error translation; Java fallback is availability/semantic handling, not process isolation. In-process plugins already share server authority and must not be modeled as sandboxed callers (README.md:20-24; lattice-native/jni/jni_helper.hpp:29-84,103-164; lattice-server/minecraft-patches/features/0007-Accelerate-tracked-entity-visibility-checks-with-nat.patch:25-70).
- Entity distance prefilter -\> player-visible tracking: native scanning only narrows candidates, then updatePlayer retains broadcastToPlayer, chunk tracking, and Bukkit visibility checks. CraftPlayer uses visibleByDefault and per-player inverted visibility state. Native proximity must never replace those authorization decisions (lattice-server/minecraft-patches/features/0007-Accelerate-tracked-entity-visibility-checks-with-nat.patch:41-54; lattice-server/minecraft-patches/features/0103-Use-ChunkMap-specific-visibility-check.patch:12-18; lattice-server/paper-patches/features/0004-Optimize-CraftPlayer-canSee-checks.patch:20-22).
- RegionFile caller -\> native filesystem handle: production integration passes its existing path to open(path), which delegates to open(path,null), so the optional allowedRoot containment check is not used by this integration. Canonicalization, platform support, native availability, range checks, synchronized read/close, and OS read-only access are component controls. Provenance of the region path remains the upstream RegionFile caller's responsibility (lattice-server/minecraft-patches/features/0100-feat-region-use-native-positioned-payload-reads.patch:25-41,108-112; lattice-server/src/main/java/com/latticemc/lattice/nativelib/NativeRegionFileRead.java:26-65,75-103; lattice-native/jni/native_region_file_read.cpp:123-137).
- Chunk bytes -\> compression/parser allocations: native region inflation is disabled by default; enabling it buffers the compressed source and calls the default 64 MiB inflated-output limit. Deflation is used whenever native acceleration is loaded and buffers output until close. NBT index code is compiled/exported, but the current Java NativeChunkSerializer deliberately declares only zlib JNI methods, so the native NBT parser is not established as a production Java entry point (lattice-server/src/main/java/com/latticemc/lattice/bootstrap/NativeZlibStreams.java:15-49,61-70,103-135; lattice-server/src/main/java/com/latticemc/lattice/nativelib/NativeChunkSerializer.java:14-33,117-119; lattice-native/CMakeLists.txt:342-349).
- Command sender -\> runtime administration: /lattice has permission lattice.command, execute calls testPermission before reading/changing controls, and tab completion calls testPermissionSilent. Bukkit is the permission enforcer; installed permission-plugin behavior and effective grants are deployment-owned (lattice-server/src/main/java/com/latticemc/lattice/command/LatticeDensityCommand.java:32-53,95-143).
- Repository/build inputs -\> publishing authority: pull requests run read-only build jobs; server publishing requires a successful default-branch push build. Native publishing requires successful matrix builds and either default-branch push or explicit workflow_dispatch publish_release, validates the exact expected eight binaries plus checksum files, then writes native-\<sha\> and native-latest releases. Scheduled upstream updates separately fetch Purpur refs, apply/build patches, commit, and push under contents:write ( .github/workflows/build.yml:151-176; .github/workflows/native.yml:257-318; .github/workflows/upstream.yml:3-46; scripts/updateUpstream.sh:9-19).
- 玩家追逐目标→生物普通寻路→长期线程 native mirror：PathFinderNativeSupport.java:188,259-275 使用 Level identity 作为 worldKey；jni/pathfinder.cpp:53,418-419 保存线程镜像；src/world/entity/pathfinder.cpp:1337 无界插入。每tick上传1次仅限制速率，不能限制常驻内存。

### Attacker Capabilities

- A remote player may supply gameplay inputs and influence entity/world state permitted by the inherited server protocol. This does not imply control of JNI handles, native-library configuration, local region-file paths, arbitrary Java method arguments, or installed plugins; each stronger reachability claim requires a concrete caller trace.
- A supplied-world author or actor able to alter region files can affect persisted compressed chunk input if the operator later opens those worlds. Actual upload/import mechanisms and exposure are not established by the inspected source.
- A native download origin or network attacker can affect downloaded bytes only subject to TLS and the locally pinned digest. They do not initially control the administrator's trusted digest, bundled JAR, JVM properties, or override path.
- A different local OS user may influence a shared temporary directory only where host permissions permit it; same-user process or administrator control is not a new privilege boundary. Cache attacks require concrete ownership/ACL and race prerequisites.
- A pull-request author can change source processed by read-only PR jobs, but is not automatically a release publisher. Repository writers, authorized workflow-dispatch users, upstream maintainers, and package publishers carry separate supply-chain authority.
- An installed in-process Bukkit plugin is already trusted code with server-process authority. A malicious plugin's ability to call native functions or alter system properties alone does not establish escalation.

### Security Objectives

- Only operator-trusted native code should execute; preserve the local digest requirement on unbundled downloads, explicit nature of direct-path override, and cache integrity checks (lattice-server/src/main/java/com/latticemc/lattice/bootstrap/LatticeNativeLoader.java:122-132,188-224,361-398).
- Malformed or excessive world-derived inputs must not corrupt native memory, invalidate allocation lifetimes, or cause avoidable server-wide resource exhaustion. Maintain overflow-safe array checks and explicit JNI error handling while preserving Java/native semantics (lattice-native/jni/jni_helper.hpp:29-84; lattice-server/src/main/java/com/latticemc/lattice/nativelib/NativeChunkSerializer.java:22-38).
- Preserve persisted chunk format, filesystem write ownership, synchronized region-handle lifetime, and Java fallback behavior across unsupported platforms and disabled optional acceleration (lattice-server/src/main/java/com/latticemc/lattice/nativelib/NativeRegionFileRead.java:9-13,37-103; lattice-server/src/main/java/com/latticemc/lattice/bootstrap/NativeZlibStreams.java:33-49).
- Preserve per-player entity visibility and restrict runtime administrative controls to authorized command senders (lattice-server/minecraft-patches/features/0103-Use-ChunkMap-specific-visibility-check.patch:12-18; lattice-server/src/main/java/com/latticemc/lattice/command/LatticeDensityCommand.java:32-53).
- Bind published artifacts to the intended successful build and keep PR build authority distinct from release/repository write authority; preserve explicit separation of private PGO artifact names and normal public native assets ( .github/workflows/build.yml:151-176; .github/workflows/native.yml:257-318; lattice-native/CMakeLists.txt:152-158,256-264).
- User-supplied objective: inspect vulnerabilities, record/report substantiated issues with solutions, and identify dead code. Architecture observations and unreachable/native-only code are not themselves validated vulnerabilities.

### Assumptions

- No deployment model or authoritative security knowledge was supplied. The nested policy resolver returned no policy for lattice-server, lattice-native, .github, test-plugin, or scripts. Host permissions, network exposure, server authentication mode, actual plugins, and production world-import provenance remain unspecified.
- Patch-composed upstream functionality is modeled from inspected repository patch evidence, not a fully applied or executed Minecraft/Paper server. Full login/session/network parsing behavior and exact world-root construction were not established by this bounded pass.
- Loader documentation discrepancy: its comment says release pipelines populate BUILT_IN_RELEASE_DIGESTS, while the source map is empty and inspected Gradle/resource and release workflows only bundle, stage, checksum, and publish libraries. No digest-map generation was found. Consequently the code-established default for an unbundled platform is refusing download until an operator supplies a trusted digest (lattice-server/src/main/java/com/latticemc/lattice/bootstrap/LatticeNativeLoader.java:53-60,197-200; build.gradle.kts:180-185; .github/workflows/native.yml:220-255,271-318).
- Cache ownership caveat: the source calls its default a per-user directory, but this is a username-derived child of java.io.tmpdir; uniqueness of the path is not an access-control guarantee. POSIX hardening failures are ignored and Windows ACLs are not implemented (lattice-server/src/main/java/com/latticemc/lattice/bootstrap/LatticeNativeLoader.java:409-428).
- PGO documentation is consistent with inspected output naming/defaults: explicit GENERATE/USE modes produce distinct library names and cannot be installed; normal Gradle packaging references lattice rather than those names. Private PGO loading uses the already-trusted explicit native path override (README.md:100-106,128-153; lattice-native/CMakeLists.txt:41-47,152-158,256-264; build.gradle.kts:109-114).
- Native positioned read is disabled by default and its Java platform gate excludes FreeBSD even though the general loader supports FreeBSD. Its optional allowedRoot check is not evidence that the production RegionFile call is constrained to a separate root (lattice-server/src/main/java/com/latticemc/lattice/config/LatticeConfig.java:47; lattice-server/src/main/java/com/latticemc/lattice/nativelib/NativeRegionFileRead.java:26-57,100-103; lattice-server/src/main/java/com/latticemc/lattice/bootstrap/LatticeNativeLoader.java:64-69).
- The optional activation benchmark is a separate source set and MCProtocolLib dependency, not part of the plugin JAR (test-plugin/build.gradle.kts:9-25). Its README describes loopback connections, but --host accepts any nonblank string; 'offline-only' means no Mojang/Microsoft authentication, not enforced network locality. Operators select the destination and output file (test-plugin/activation-bench/README.md:3-5; test-plugin/src/activation-bench/java/org/purpurmc/testplugin/activationbench/ActivationBenchBotRunner.java:67-79,296-343).
- README broadly lists NBT acceleration, but current Java NativeChunkSerializer excludes NBT indexing while native NBT implementation remains compiled. Distinguish exported/build-linked code from reachable production Java APIs when assessing dead code or impact (README.md:84-88; lattice-server/src/main/java/com/latticemc/lattice/nativelib/NativeChunkSerializer.java:14-18,117-119; lattice-native/CMakeLists.txt:342-349).
- 本次额外读取了已有生成 Minecraft 源码中的 Zombie/MeleeAttackGoal/PathNavigation/PathFinder 调用链；未重建确认生成源码与patch队列一致性。

## Findings

| Finding | Severity | Confidence | Detailed write-up |
| --- | --- | --- | --- |
| [持续在新区域触发生物寻路可累积无上限原生内存](#finding-1) | medium | medium | inline below |
| [共享临时目录被预占时，原生库加载可跨账户执行代码](#finding-2) | medium | high | inline below |

### Confidence Scale

| Label | Meaning |
| --- | --- |
| high | Direct evidence supports the finding with no material unresolved blocker. |
| medium | Evidence supports a plausible issue, but material runtime or reachability proof remains. |
| low | Evidence is incomplete and the item is retained only for explicit follow-up. |

<a id="finding-1"></a>

### [1] 持续在新区域触发生物寻路可累积无上限原生内存

| Field | Value |
| --- | --- |
| Severity | medium |
| Confidence | medium |
| Confidence rationale | 已核对实际生成 Minecraft 调用链、默认 gate 和无界 map；未重新应用补丁或执行玩家追逐/内存测量。 |
| Category | resource-exhaustion |
| CWE | CWE-400, CWE-770 |
| Affected lines | lattice-native/src/world/entity/pathfinder.cpp:1335-1338, lattice-native/jni/pathfinder.cpp:45-89, lattice-server/src/main/java/com/latticemc/lattice/bridge/PathFinderNativeSupport.java:304-338 |

#### Summary

原生寻路将每个访问过的 section 保存在长期存活的线程镜像中，没有容量或区块卸载淘汰。普通玩家可通过生物追逐触发新区域快照；在同一世界且缺少足够失效/切换清理的工作负载下，内存持续增长，最终影响整个服务可用性。

#### Root Cause

玩家目标经 PathFinderNativeSupport 构造快照，JNI 在查找前将快照写入 thread_local 镜像。store_pathfinder_state_snapshot 用 map\[key\] 为每个新 section 建立约 24 KiB 存储，未限制总量或关联 chunk unload。每 tick 上传限额只降低增长速率；独立 Java 缓存的 512 section 上限也不会约束这张 native map。

**玩家目标进入寻路** — `lattice-server/src/minecraft/java/net/minecraft/world/entity/ai/goal/MeleeAttackGoal.java:39-46`

目标实体的位置决定生物的寻路请求；追逐中的玩家移动会触发重新寻路。

```
            LivingEntity target = this.mob.getTarget();
            if (target == null) {
                return false;
            } else if (!target.isAlive()) {
                return false;
            } else {
                this.path = this.mob.getNavigation().createPath(target, 0);
                return this.path != null || this.mob.isWithinMeleeAttackRange(target);
```

**真实 PathFinder 生产入口** — `lattice-server/src/minecraft/java/net/minecraft/world/level/pathfinder/PathFinder.java:45-51`

当前生成源将生物寻路交给 native support，再保留 Java 回退。

```
    public @Nullable Path findPath(PathNavigationRegion region, Mob mob, Set<BlockPos> targets, float maxRange, int reachRange, float maxVisitedNodesMultiplier) {
        Path nativePath = PathFinderNativeSupport.tryFindPath(
            this, this.maxVisitedNodes, region, mob, targets, maxRange, reachRange, maxVisitedNodesMultiplier
        );
        if (nativePath != null) {
            return nativePath;
        }
```

**上传至持久镜像** — `lattice-native/jni/pathfinder.cpp:417-419`

snapshot 在每次未命中的原生寻路中写入线程镜像，结果是否找到路径不影响此前插入。

```
    thread_local pf::PathfinderScratch scratch{};
    (void)refresh_state_mirror(worldKey);
    pf::store_pathfinder_state_snapshot(g_state_mirror.mirror, worldKey, snapshot);
```

**线程持有镜像** — `lattice-native/jni/pathfinder.cpp:45-53`

镜像随长寿命工作线程存在，而非随一次请求结束释放。

```
struct ThreadStateMirror {
    pf::PathfinderStateMirror mirror{};
    std::shared_ptr<MirrorInvalidationLog> log{};
    std::uint64_t generation = 0;
};

std::mutex g_state_mirror_mutex;
std::unordered_map<int, std::shared_ptr<MirrorInvalidationLog>> g_state_mirror_logs;
thread_local ThreadStateMirror g_state_mirror{};
```

**无容量检查的新 section 插入** — `lattice-native/src/world/entity/pathfinder.cpp:1333-1345`

operator\[\] 为新地理 section 分配完整数组；没有预算、LRU、TTL 或区块卸载驱逐。

```
                const int descriptor = snapshot.cells[index];
                if (descriptor < 0 || descriptor >= snapshot.descriptor_count) return;
                const std::uint64_t key = mirror_section_key(x, y, z);
                if (cached_section == nullptr || key != cached_key) {
                    cached_section = &mirror.sections[key];
                    cached_key = key;
                }
                const int local = mirror_section_index(x, y, z);
                cached_section->raw_path_types[local] = snapshot.raw_path_types[descriptor];
                cached_section->floor_heights[local] = snapshot.floor_heights[descriptor];
                if (cached_section->valid[local] == 0) {
                    cached_section->valid[local] = 1;
                    ++cached_section->valid_count;
```

**每 section 的常驻成本** — `lattice-native/src/world/entity/pathfinder.hpp:153-165`

三个 4096 长度数组单独即 24576 字节，另有 valid_count/对齐/hash 节点成本。

```
struct PathfinderStateMirrorSection {
    std::array<std::int8_t, 4096> raw_path_types{};
    std::array<float, 4096> floor_heights{};
    std::array<std::uint8_t, 4096> valid{};
    /// Number of set entries in `valid`. Lets a coverage probe answer "is this
    /// whole section populated?" without touching the 4096-byte `valid` array.
    std::uint16_t valid_count = 0;
};

struct PathfinderStateMirror {
    int world_key = 0;
    std::unordered_map<std::uint64_t, PathfinderStateMirrorSection> sections{};
};
```

**现有清理条件不限制总量** — `lattice-native/jni/pathfinder.cpp:75-89`

仅世界切换、失效日志丢失或对应 section 方块变动清理；同世界正常追逐新区域仍可不断新增。

```
    std::lock_guard lock(g_state_mirror.log->mutex);
    const MirrorInvalidationLog& log = *g_state_mirror.log;
    const std::uint64_t current = log.generation();
    if (g_state_mirror.generation == current) return false;
    if (g_state_mirror.generation < log.base_generation) {
        // Fell off the back of the log: no way to know which sections changed.
        g_state_mirror.mirror.sections.clear();
        g_state_mirror.generation = current;
        return true;
    }
    for (std::size_t i = static_cast<std::size_t>(g_state_mirror.generation - log.base_generation);
            i < log.keys.size(); ++i) {
        g_state_mirror.mirror.sections.erase(log.keys[i]);
    }
    g_state_mirror.generation = current;
```

#### Validation

确认 Zombie 玩家目标→MeleeAttackGoal→PathNavigation→PathFinder→native snapshot 的真实源码链，且默认短路径阈值24仍存在合法的更长追逐输入。相同 Level 的 worldKey 不随 tick 改变，JNI 同代失效日志不会清空。查遍镜像清理位置，没有容量或 unload 上限。

Validation method: independent static source trace

**玩家目标进入寻路** — `lattice-server/src/minecraft/java/net/minecraft/world/entity/ai/goal/MeleeAttackGoal.java:39-46`

目标实体的位置决定生物的寻路请求；追逐中的玩家移动会触发重新寻路。

```
            LivingEntity target = this.mob.getTarget();
            if (target == null) {
                return false;
            } else if (!target.isAlive()) {
                return false;
            } else {
                this.path = this.mob.getNavigation().createPath(target, 0);
                return this.path != null || this.mob.isWithinMeleeAttackRange(target);
```

**真实 PathFinder 生产入口** — `lattice-server/src/minecraft/java/net/minecraft/world/level/pathfinder/PathFinder.java:45-51`

当前生成源将生物寻路交给 native support，再保留 Java 回退。

```
    public @Nullable Path findPath(PathNavigationRegion region, Mob mob, Set<BlockPos> targets, float maxRange, int reachRange, float maxVisitedNodesMultiplier) {
        Path nativePath = PathFinderNativeSupport.tryFindPath(
            this, this.maxVisitedNodes, region, mob, targets, maxRange, reachRange, maxVisitedNodesMultiplier
        );
        if (nativePath != null) {
            return nativePath;
        }
```

**上传至持久镜像** — `lattice-native/jni/pathfinder.cpp:417-419`

snapshot 在每次未命中的原生寻路中写入线程镜像，结果是否找到路径不影响此前插入。

```
    thread_local pf::PathfinderScratch scratch{};
    (void)refresh_state_mirror(worldKey);
    pf::store_pathfinder_state_snapshot(g_state_mirror.mirror, worldKey, snapshot);
```

**线程持有镜像** — `lattice-native/jni/pathfinder.cpp:45-53`

镜像随长寿命工作线程存在，而非随一次请求结束释放。

```
struct ThreadStateMirror {
    pf::PathfinderStateMirror mirror{};
    std::shared_ptr<MirrorInvalidationLog> log{};
    std::uint64_t generation = 0;
};

std::mutex g_state_mirror_mutex;
std::unordered_map<int, std::shared_ptr<MirrorInvalidationLog>> g_state_mirror_logs;
thread_local ThreadStateMirror g_state_mirror{};
```

**无容量检查的新 section 插入** — `lattice-native/src/world/entity/pathfinder.cpp:1333-1345`

operator\[\] 为新地理 section 分配完整数组；没有预算、LRU、TTL 或区块卸载驱逐。

```
                const int descriptor = snapshot.cells[index];
                if (descriptor < 0 || descriptor >= snapshot.descriptor_count) return;
                const std::uint64_t key = mirror_section_key(x, y, z);
                if (cached_section == nullptr || key != cached_key) {
                    cached_section = &mirror.sections[key];
                    cached_key = key;
                }
                const int local = mirror_section_index(x, y, z);
                cached_section->raw_path_types[local] = snapshot.raw_path_types[descriptor];
                cached_section->floor_heights[local] = snapshot.floor_heights[descriptor];
                if (cached_section->valid[local] == 0) {
                    cached_section->valid[local] = 1;
                    ++cached_section->valid_count;
```

**每 section 的常驻成本** — `lattice-native/src/world/entity/pathfinder.hpp:153-165`

三个 4096 长度数组单独即 24576 字节，另有 valid_count/对齐/hash 节点成本。

```
struct PathfinderStateMirrorSection {
    std::array<std::int8_t, 4096> raw_path_types{};
    std::array<float, 4096> floor_heights{};
    std::array<std::uint8_t, 4096> valid{};
    /// Number of set entries in `valid`. Lets a coverage probe answer "is this
    /// whole section populated?" without touching the 4096-byte `valid` array.
    std::uint16_t valid_count = 0;
};

struct PathfinderStateMirror {
    int world_key = 0;
    std::unordered_map<std::uint64_t, PathfinderStateMirrorSection> sections{};
};
```

**现有清理条件不限制总量** — `lattice-native/jni/pathfinder.cpp:75-89`

仅世界切换、失效日志丢失或对应 section 方块变动清理；同世界正常追逐新区域仍可不断新增。

```
    std::lock_guard lock(g_state_mirror.log->mutex);
    const MirrorInvalidationLog& log = *g_state_mirror.log;
    const std::uint64_t current = log.generation();
    if (g_state_mirror.generation == current) return false;
    if (g_state_mirror.generation < log.base_generation) {
        // Fell off the back of the log: no way to know which sections changed.
        g_state_mirror.mirror.sections.clear();
        g_state_mirror.generation = current;
        return true;
    }
    for (std::size_t i = static_cast<std::size_t>(g_state_mirror.generation - log.base_generation);
            i < log.keys.size(); ++i) {
        g_state_mirror.mirror.sections.erase(log.keys[i]);
    }
    g_state_mirror.generation = current;
```

Limitations:
- 未运行服务、资源耗尽复现或测量增长速度。
- 生成 Minecraft 源码在本次未重新应用补丁验证一致性。
- 跨世界寻路、失效日志溢出以及方块变动会减少/清空镜像；不能声称所有默认部署都会无限增长。

#### Dataflow

玩家目标位置→MeleeAttackGoal→PathNavigation/PathFinder→PathFinderNativeSupport snapshot→JNI→thread_local mirror.sections

- **Source:** 追逐玩家的生物产生的新区域路径

- **Sink:** PathfinderStateMirror.sections 的无界插入

- **Outcome:** 持续原生内存增长直至服务内存压力/退出

**玩家目标进入寻路** — `lattice-server/src/minecraft/java/net/minecraft/world/entity/ai/goal/MeleeAttackGoal.java:39-46`

目标实体的位置决定生物的寻路请求；追逐中的玩家移动会触发重新寻路。

```
            LivingEntity target = this.mob.getTarget();
            if (target == null) {
                return false;
            } else if (!target.isAlive()) {
                return false;
            } else {
                this.path = this.mob.getNavigation().createPath(target, 0);
                return this.path != null || this.mob.isWithinMeleeAttackRange(target);
```

**真实 PathFinder 生产入口** — `lattice-server/src/minecraft/java/net/minecraft/world/level/pathfinder/PathFinder.java:45-51`

当前生成源将生物寻路交给 native support，再保留 Java 回退。

```
    public @Nullable Path findPath(PathNavigationRegion region, Mob mob, Set<BlockPos> targets, float maxRange, int reachRange, float maxVisitedNodesMultiplier) {
        Path nativePath = PathFinderNativeSupport.tryFindPath(
            this, this.maxVisitedNodes, region, mob, targets, maxRange, reachRange, maxVisitedNodesMultiplier
        );
        if (nativePath != null) {
            return nativePath;
        }
```

**上传至持久镜像** — `lattice-native/jni/pathfinder.cpp:417-419`

snapshot 在每次未命中的原生寻路中写入线程镜像，结果是否找到路径不影响此前插入。

```
    thread_local pf::PathfinderScratch scratch{};
    (void)refresh_state_mirror(worldKey);
    pf::store_pathfinder_state_snapshot(g_state_mirror.mirror, worldKey, snapshot);
```

**线程持有镜像** — `lattice-native/jni/pathfinder.cpp:45-53`

镜像随长寿命工作线程存在，而非随一次请求结束释放。

```
struct ThreadStateMirror {
    pf::PathfinderStateMirror mirror{};
    std::shared_ptr<MirrorInvalidationLog> log{};
    std::uint64_t generation = 0;
};

std::mutex g_state_mirror_mutex;
std::unordered_map<int, std::shared_ptr<MirrorInvalidationLog>> g_state_mirror_logs;
thread_local ThreadStateMirror g_state_mirror{};
```

**无容量检查的新 section 插入** — `lattice-native/src/world/entity/pathfinder.cpp:1333-1345`

operator\[\] 为新地理 section 分配完整数组；没有预算、LRU、TTL 或区块卸载驱逐。

```
                const int descriptor = snapshot.cells[index];
                if (descriptor < 0 || descriptor >= snapshot.descriptor_count) return;
                const std::uint64_t key = mirror_section_key(x, y, z);
                if (cached_section == nullptr || key != cached_key) {
                    cached_section = &mirror.sections[key];
                    cached_key = key;
                }
                const int local = mirror_section_index(x, y, z);
                cached_section->raw_path_types[local] = snapshot.raw_path_types[descriptor];
                cached_section->floor_heights[local] = snapshot.floor_heights[descriptor];
                if (cached_section->valid[local] == 0) {
                    cached_section->valid[local] = 1;
                    ++cached_section->valid_count;
```

**每 section 的常驻成本** — `lattice-native/src/world/entity/pathfinder.hpp:153-165`

三个 4096 长度数组单独即 24576 字节，另有 valid_count/对齐/hash 节点成本。

```
struct PathfinderStateMirrorSection {
    std::array<std::int8_t, 4096> raw_path_types{};
    std::array<float, 4096> floor_heights{};
    std::array<std::uint8_t, 4096> valid{};
    /// Number of set entries in `valid`. Lets a coverage probe answer "is this
    /// whole section populated?" without touching the 4096-byte `valid` array.
    std::uint16_t valid_count = 0;
};

struct PathfinderStateMirror {
    int world_key = 0;
    std::unordered_map<std::uint64_t, PathfinderStateMirrorSection> sections{};
};
```

**现有清理条件不限制总量** — `lattice-native/jni/pathfinder.cpp:75-89`

仅世界切换、失效日志丢失或对应 section 方块变动清理；同世界正常追逐新区域仍可不断新增。

```
    std::lock_guard lock(g_state_mirror.log->mutex);
    const MirrorInvalidationLog& log = *g_state_mirror.log;
    const std::uint64_t current = log.generation();
    if (g_state_mirror.generation == current) return false;
    if (g_state_mirror.generation < log.base_generation) {
        // Fell off the back of the log: no way to know which sections changed.
        g_state_mirror.mirror.sections.clear();
        g_state_mirror.generation = current;
        return true;
    }
    for (std::size_t i = static_cast<std::size_t>(g_state_mirror.generation - log.base_generation);
            i < log.keys.size(); ++i) {
        g_state_mirror.mirror.sections.erase(log.keys[i]);
    }
    g_state_mirror.generation = current;
```

#### Reachability

要求原生 ABI 可用、native pathfinder 开启、长路径与完整快照通过 gate，以及同一线程/世界持续增加新 section；默认开关与每 tick 1 次上传允许该路径。其他世界切换或大量失效可缓解。

- **Attacker:** 可在游戏世界内移动并被生物追逐的普通玩家

- **Entry point:** 常规生物寻路

- **Outcome:** 超出活跃区块集合的常驻原生内存

#### Severity

**Medium** — 可耗尽服务进程原生内存，但需持续产生满足原生门槛的新区域路径；世界切换和大量失效可清空，未测耗尽速度。

Additional runtime or deployment evidence could raise or lower this severity.

Impact assessment:
- **Level:** high
- **Why:** 原生分配影响整个服务进程，内存耗尽可导致退出。

Likelihood assessment:
- **Level:** medium
- **Why:** 持续新区域工作负载且不能被频繁世界切换/失效清空；没有运行时速率证据。

#### Remediation

给每线程/每世界镜像设置明确 section 或字节预算，按 LRU/TTL 淘汰并接入 chunk unload/world close 清理；达到预算时拒绝新增并走 Java 回退。淘汰时保证 LazyPathGrid 保存的 section 指针在一次查找内有效。保留上传速率门槛作为补充，增加 native mirror section/bytes 监控。

Tests:
- 依次上传超过预算的互不重叠 section，断言 map 大小和字节预算有界。
- 验证 chunk unload/world close、方块失效与跨世界切换会释放对应镜像。
- 在同世界重复新区域寻路后确认 RSS/镜像数量趋稳，覆盖并发查找与淘汰的指针安全。

Preventive controls:
- 所有长期 native 缓存同时定义容量、失效和所属世界生命周期。

<a id="finding-2"></a>

### [2] 共享临时目录被预占时，原生库加载可跨账户执行代码

| Field | Value |
| --- | --- |
| Severity | medium |
| Confidence | high |
| Confidence rationale | 路径生成、目录接受、失败忽略、校验和加载的完整源码链均已独立复核；未执行跨账户竞态复现。 |
| Category | insecure-temporary-file |
| CWE | CWE-377, CWE-367 |
| Affected lines | lattice-server/src/main/java/com/latticemc/lattice/bootstrap/LatticeNativeLoader.java:424-428, lattice-server/src/main/java/com/latticemc/lattice/bootstrap/LatticeNativeLoader.java:157-158, lattice-server/src/main/java/com/latticemc/lattice/bootstrap/LatticeNativeLoader.java:409-417, lattice-server/src/main/java/com/latticemc/lattice/bootstrap/LatticeNativeLoader.java:361-400 |

#### Summary

缓存目录名称可预测，且加载器忽略 POSIX 权限设置失败。另一同机用户可预建可写目录，在 SHA-256 检查和 System.load 之间替换库，使代码以服务账户权限运行。

#### Root Cause

resolveCacheDir 将用户名拼到 java.io.tmpdir 下；createDirectories 接受外部账户拥有的真实目录。hardenCacheDir 未验证所有者且吞掉 chmod 的 IOException，因而保护没有实际建立。后续即使对库执行完整摘要检查，拥有父目录的攻击者仍能在最终路径加载之前替换它。

**可预测的共享缓存路径** — `lattice-server/src/main/java/com/latticemc/lattice/bootstrap/LatticeNativeLoader.java:416-417`

另一操作系统用户可在共享 java.io.tmpdir 中预先创建此固定名称的真实目录。

```
        final String user = System.getProperty("user.name", "unknown").replaceAll("[^A-Za-z0-9._-]", "_");
        return Path.of(System.getProperty("java.io.tmpdir"), "lattice-native-" + user);
```

**接受现有目录** — `lattice-server/src/main/java/com/latticemc/lattice/bootstrap/LatticeNativeLoader.java:362-364`

createDirectories 不要求已有目录归服务账户所有，继续调用权限加固。

```
        final Path cacheDir = resolveCacheDir();
        Files.createDirectories(cacheDir);
        hardenCacheDir(cacheDir);
```

**权限设置失败后继续使用** — `lattice-server/src/main/java/com/latticemc/lattice/bootstrap/LatticeNativeLoader.java:424-428`

真实的外部用户目录通过符号链接检查；非所有者设置 POSIX 权限失败被忽略，目录所有者仍可替换其中的条目。

```
        try {
            Files.setPosixFilePermissions(cacheDir, PosixFilePermissions.fromString("rwx------"));
        } catch (UnsupportedOperationException | IOException ignored) {
            // Non-POSIX filesystems (Windows) have no equivalent; the per-user path still applies.
        }
```

**仅在返回路径前核对摘要** — `lattice-server/src/main/java/com/latticemc/lattice/bootstrap/LatticeNativeLoader.java:374-377`

摘要可排除静态投毒，但返回的是稍后会被重新解析的文件路径；新写入分支也只在返回前校验。

```
        final Path target = cacheDir.resolve(libFile + "." + digest.substring(0, 16));
        if (Files.isRegularFile(target, LinkOption.NOFOLLOW_LINKS) && hasDigest(target, digest)) {
            return target;
        }
```

**随后按路径加载原生库** — `lattice-server/src/main/java/com/latticemc/lattice/bootstrap/LatticeNativeLoader.java:157-158`

攻击者若在摘要读取完成后替换目录条目，System.load 会以服务账户权限执行替换后的库。

```
        try {
            System.load(extracted.toAbsolutePath().toString());
```

#### Validation

已复核符号链接拒绝并不拒绝攻击者拥有的真实目录、chmod 失败被忽略、完整哈希和 System.load 分离。无需假设攻击者拥有服务器配置或插件权限。

Validation method: independent static source trace

**可预测的共享缓存路径** — `lattice-server/src/main/java/com/latticemc/lattice/bootstrap/LatticeNativeLoader.java:416-417`

另一操作系统用户可在共享 java.io.tmpdir 中预先创建此固定名称的真实目录。

```
        final String user = System.getProperty("user.name", "unknown").replaceAll("[^A-Za-z0-9._-]", "_");
        return Path.of(System.getProperty("java.io.tmpdir"), "lattice-native-" + user);
```

**接受现有目录** — `lattice-server/src/main/java/com/latticemc/lattice/bootstrap/LatticeNativeLoader.java:362-364`

createDirectories 不要求已有目录归服务账户所有，继续调用权限加固。

```
        final Path cacheDir = resolveCacheDir();
        Files.createDirectories(cacheDir);
        hardenCacheDir(cacheDir);
```

**权限设置失败后继续使用** — `lattice-server/src/main/java/com/latticemc/lattice/bootstrap/LatticeNativeLoader.java:424-428`

真实的外部用户目录通过符号链接检查；非所有者设置 POSIX 权限失败被忽略，目录所有者仍可替换其中的条目。

```
        try {
            Files.setPosixFilePermissions(cacheDir, PosixFilePermissions.fromString("rwx------"));
        } catch (UnsupportedOperationException | IOException ignored) {
            // Non-POSIX filesystems (Windows) have no equivalent; the per-user path still applies.
        }
```

**仅在返回路径前核对摘要** — `lattice-server/src/main/java/com/latticemc/lattice/bootstrap/LatticeNativeLoader.java:374-377`

摘要可排除静态投毒，但返回的是稍后会被重新解析的文件路径；新写入分支也只在返回前校验。

```
        final Path target = cacheDir.resolve(libFile + "." + digest.substring(0, 16));
        if (Files.isRegularFile(target, LinkOption.NOFOLLOW_LINKS) && hasDigest(target, digest)) {
            return target;
        }
```

**随后按路径加载原生库** — `lattice-server/src/main/java/com/latticemc/lattice/bootstrap/LatticeNativeLoader.java:157-158`

攻击者若在摘要读取完成后替换目录条目，System.load 会以服务账户权限执行替换后的库。

```
        try {
            System.load(extracted.toAbsolutePath().toString());
```

Limitations:
- 未运行跨账户竞态或加载恶意库。
- 使用私有临时根目录或已正确保护的缓存目录时，所述攻击条件不成立。

#### Dataflow

共享 java.io.tmpdir → 固定用户名目录 → 接受目录并忽略加固失败 → 校验原生库 → 返回路径 → System.load

- **Source:** 另一 OS 账户拥有的缓存目录及目录条目

- **Sink:** System.load

- **Outcome:** 攻击者代码以服务账户权限运行

**可预测的共享缓存路径** — `lattice-server/src/main/java/com/latticemc/lattice/bootstrap/LatticeNativeLoader.java:416-417`

另一操作系统用户可在共享 java.io.tmpdir 中预先创建此固定名称的真实目录。

```
        final String user = System.getProperty("user.name", "unknown").replaceAll("[^A-Za-z0-9._-]", "_");
        return Path.of(System.getProperty("java.io.tmpdir"), "lattice-native-" + user);
```

**接受现有目录** — `lattice-server/src/main/java/com/latticemc/lattice/bootstrap/LatticeNativeLoader.java:362-364`

createDirectories 不要求已有目录归服务账户所有，继续调用权限加固。

```
        final Path cacheDir = resolveCacheDir();
        Files.createDirectories(cacheDir);
        hardenCacheDir(cacheDir);
```

**权限设置失败后继续使用** — `lattice-server/src/main/java/com/latticemc/lattice/bootstrap/LatticeNativeLoader.java:424-428`

真实的外部用户目录通过符号链接检查；非所有者设置 POSIX 权限失败被忽略，目录所有者仍可替换其中的条目。

```
        try {
            Files.setPosixFilePermissions(cacheDir, PosixFilePermissions.fromString("rwx------"));
        } catch (UnsupportedOperationException | IOException ignored) {
            // Non-POSIX filesystems (Windows) have no equivalent; the per-user path still applies.
        }
```

**仅在返回路径前核对摘要** — `lattice-server/src/main/java/com/latticemc/lattice/bootstrap/LatticeNativeLoader.java:374-377`

摘要可排除静态投毒，但返回的是稍后会被重新解析的文件路径；新写入分支也只在返回前校验。

```
        final Path target = cacheDir.resolve(libFile + "." + digest.substring(0, 16));
        if (Files.isRegularFile(target, LinkOption.NOFOLLOW_LINKS) && hasDigest(target, digest)) {
            return target;
        }
```

**随后按路径加载原生库** — `lattice-server/src/main/java/com/latticemc/lattice/bootstrap/LatticeNativeLoader.java:157-158`

攻击者若在摘要读取完成后替换目录条目，System.load 会以服务账户权限执行替换后的库。

```
        try {
            System.load(extracted.toAbsolutePath().toString());
```

#### Reachability

要求非 root 服务账户使用共享临时目录、攻击者预占真实目录且赢得校验后替换竞态。私有临时目录消除此路径。

- **Attacker:** 同机另一非特权 OS 账户

- **Entry point:** 服务启动时提取内置或已固定摘要的原生库

- **Outcome:** 跨账户原生代码执行

#### Severity

**Medium** — 影响为服务账户代码执行，但要求共享临时目录、提前预占目录以及赢得启动竞态；不属于已证实的远程攻击。

Additional runtime or deployment evidence could raise or lower this severity.

Impact assessment:
- **Level:** high
- **Why:** 原生库具有整个服务进程的权限。

Likelihood assessment:
- **Level:** medium
- **Why:** 需本地访问、目录预占和启动竞态；不是普通远程玩家可触发的缺陷。

#### Remediation

在经验证的私有根目录中，以原子的 owner-only 权限创建不可预测的提取目录；如需持久缓存，拒绝非当前服务账户所有或可被其他账户改写的目录与父路径，POSIX 权限设置失败必须终止加载。保护须持续到 System.load 完成，重复校验摘要不能替代目录所有权保护。

Tests:
- 用不同 UID 预建 0777 的默认缓存目录，断言加载拒绝且不会调用 System.load。
- 断言 POSIX chmod 失败不会被当作不支持 POSIX 而忽略。
- 检查正常私有目录的内置库、下载库、缓存复用、并发启动路径。

Preventive controls:
- 统一封装原生可执行内容的安全提取目录生命周期。

## Reviewed Surfaces

| Surface | Risk Area | Outcome | Notes |
| --- | --- | --- | --- |
| 原生库下载、缓存及加载 | not recorded | Reported | 确认 cache-owner；本地 pin、HTTPS、重定向白名单有效；管理员路径本已拥有代码执行权。 |
| 运行时命令、实体可见性、寻路与 LOS 集成 | not recorded | No issue found | LatticeDensityCommand.java:34/execute 有权限检查；ChunkMap 预筛后保留 canSee。PathFinderNativeSupport 中私有 CachingPathfindingContext 未构造；PathfinderStateSnapshot.java:16 descriptorCount 未读写。旧 light patch 无 native 入队消费；原生 I/O capability 仅测试脚手架。最终生成源未重新构建，结论受其一致性限制。 |
| NBT 原生解析器与 dormant JNI | not recorded | Needs follow-up | 独立确认 nbt_parser.cpp:328/385/434 在 push_frame:284-293 后使用失效 Frame 引用；64 层迁移导致旧副本修改，128/256 堆迁移后为 UAF。NativeChunkSerializer.java:117-119 无 NBT native 声明，当前不计为已证明可达的服务漏洞；修复父帧索引/引用寿命并测试后再接入。 |
| NBT/JNI 健壮性与生产可达性 | not recorded | Needs follow-up | heightmap 原生输入缺少 packed storage 长度与 bits 校验；生产 WorldgenNativeSupport 使用 Minecraft BitStorage，未建立玩家输入越过其约束的链路。tick JNI 缺少 candidates 长度校验，但 Java 类不存在。NBT uint8_t depth 与 512 默认限值不一致。保留健壮性问题，不夸大为远程漏洞。 |
| 原生实体、SIMD 和寻路镜像 | not recorded | Reported | 确认 pathfinder-mirror-retention；其余非法 JNI 参数仅属健壮性缺口，真实 Java 生产消费者提供有界匹配数组。 |
| 构建、发布与测试插件 | not recorded | No issue found | 读取三个CI工作流、构建脚本和benchmark命令。PR只读，发布单独门控，原生工具链有摘要；测试命令权限有效。基准加入者名称授飞行仅作为隔离测试前提下的部署注意事项，未提升为核心服务漏洞。 |
| 世界生成、噪声、palette 和 light | not recorded | Needs follow-up | 已审阅55个相关文件，部分大型 density evaluator/JNI、SIMD 和 Java density wrappers 为局部审查。保留网格乘法、批处理长度、Beardifier桶分配和Cleaner存活健壮性建议；尚无普通玩家越过真实调用约束的独立证据。 |
| 死代码与 JNI 声明配对 | not recorded | No issue found | dead-code-review.md 标记9组确定死代码，生产测试专用代码，以及51个没有当前 Java native声明的JNI导出；未删除源码。休眠代码的内存缺陷另列，不当作已接入玩家攻击面。 |
| 现有测试的安全覆盖 | not recorded | Needs follow-up | 检查敏感操作与相关边界测试，未运行测试。CMake CTest多为直接内核测试，不能代替JNI调用测试；NBT缺少64/128/256栈扩容回归，其余测试逐文件缺口见deferred。 |

## Open Questions And Follow Up

- Windows/实际部署的缓存 ACL、java.io.tmpdir 所有权未实测。
- 寻路镜像内存增长速率与游戏内资源耗尽未复现；世界切换和失效会缓解。
- JNI ABI、生成源码与当前补丁一致性未通过重新构建验证。
- 未做联网依赖漏洞数据库比对；SNAPSHOT/缺少锁定仅记录加固建议。
- 休眠 NBT 解析器 UAF 已源码确认，当前无 Java 生产入口；启用前必须修复。
- 本次标准静态审计覆盖关键执行边界、活动补丁及敏感操作搜索；以下文件仅部分阅读或仅完成引用/敏感操作搜索，未宣称全文审计。
  - Follow-up prompt: Review deferred unit remaining-source-review and close its stated proof gap. Paths: lattice-api/paper-patches/features/0001-Rebrand.patch, lattice-native/include/lattice/config.hpp, lattice-native/include/lattice/dispatch.hpp, lattice-native/include/lattice/lattice.hpp, lattice-native/jni/density_function.cpp, lattice-native/jni/loader.cpp, lattice-native/src/core/cpu/detect.cpp, lattice-native/src/world/gen/chunknoise/chunk_noise_sampler.cpp, lattice-native/src/world/gen/chunknoise/chunk_noise_sampler.hpp, lattice-native/src/world/gen/densityfunction/density_function.cpp, lattice-native/src/world/gen/densityfunction/density_function.hpp, lattice-native/src/world/gen/densityfunction/density_function_avx2.cpp, lattice-native/src/world/gen/densityfunction/density_function_avx512.cpp, lattice-native/src/world/gen/noise/perlin_noise_avx2.cpp, lattice-native/src/world/gen/noise/perlin_noise_avx512.cpp, lattice-native/tests/benchmark_noise.cpp, lattice-native/tests/test_aabb_query.cpp, lattice-native/tests/test_approach_target_sampler.cpp, lattice-native/tests/test_beardifier.cpp, lattice-native/tests/test_biological_ai.cpp, lattice-native/tests/test_block_light_engine.cpp, lattice-native/tests/test_brain_eligibility.cpp, lattice-native/tests/test_chunk_noise_sampler.cpp, lattice-native/tests/test_collision_sweep.cpp, lattice-native/tests/test_density_function.cpp, lattice-native/tests/test_double_perlin_noise.cpp, lattice-native/tests/test_entity_query.cpp, lattice-native/tests/test_entity_visibility.cpp, lattice-native/tests/test_flee_target_sampler.cpp, lattice-native/tests/test_home_target_sampler.cpp, lattice-native/tests/test_interpolated_noise.cpp, lattice-native/tests/test_level_propagator.cpp, lattice-native/tests/test_los.cpp, lattice-native/tests/test_material_rules.cpp, lattice-native/tests/test_octave_perlin_noise.cpp, lattice-native/tests/test_ore_vein.cpp, lattice-native/tests/test_pathfinder.cpp, lattice-native/tests/test_perlin_noise.cpp, lattice-native/tests/test_simplex_noise.cpp, lattice-native/tests/test_spawn_filter.cpp, lattice-native/tests/test_water_target_sampler.cpp, lattice-native/tests/test_xoroshiro128pp.cpp, lattice-server/minecraft-patches/features/0001-lattice-File-Patches.patch, lattice-server/minecraft-patches/features/0004-Optimize-PerlinNoise-amplitude-access.patch, lattice-server/minecraft-patches/features/0012-Add-native-worldgen-profiler-instrumentation.patch, lattice-server/minecraft-patches/features/0014-Accelerate-heightmap-priming-and-noise-sampling-with.patch, lattice-server/minecraft-patches/features/0015-Accelerate-NoiseChunk-cell-and-slice-sampling-with-n.patch, lattice-server/minecraft-patches/features/0016-Accelerate-SurfaceSystem-block-placement-with-native.patch, lattice-server/minecraft-patches/features/0017-Route-scalar-Perlin-noise-sampling-through-native-co.patch, lattice-server/minecraft-patches/features/0018-Optimize-NoiseChunk-interpolation-and-postprocessing.patch, lattice-server/minecraft-patches/features/0019-Carry-Climate-search-distance-through-recursion.patch, lattice-server/minecraft-patches/features/0020-Handle-Climate-leaves-without-virtual-recursion.patch, lattice-server/minecraft-patches/features/0031-perf-batch-dense-entity-push-collisions-per-tick.patch, lattice-server/minecraft-patches/features/0032-Revert-perf-batch-dense-entity-push-collisions-per-t.patch, lattice-server/minecraft-patches/features/0034-perf-cache-loaded-chunks-during-block-goal-search.patch, lattice-server/minecraft-patches/features/0035-Revert-perf-cache-loaded-chunks-during-block-goal-se.patch, lattice-server/minecraft-patches/features/0037-perf-cache-recent-chunks-during-block-goal-search.patch, lattice-server/minecraft-patches/features/0038-Revert-perf-cache-recent-chunks-during-block-goal-se.patch, lattice-server/minecraft-patches/features/0041-perf-reduce-dense-entity-collision-query-overhead.patch, lattice-server/minecraft-patches/features/0042-test-add-spatial-AABB-grid-A-B-switch.patch, lattice-server/minecraft-patches/features/0043-perf-preserve-dense-AABB-XZ-selectivity.patch, lattice-server/minecraft-patches/features/0044-Revert-perf-preserve-dense-AABB-XZ-selectivity.patch, lattice-server/minecraft-patches/features/0045-Revert-test-add-spatial-AABB-grid-A-B-switch.patch, lattice-server/minecraft-patches/features/0046-Revert-perf-reduce-dense-entity-collision-query-over.patch, lattice-server/minecraft-patches/features/0050-perf-cache-dense-entity-spatial-cell-lookups.patch, lattice-server/minecraft-patches/features/0051-Revert-perf-cache-dense-entity-spatial-cell-lookups.patch, lattice-server/minecraft-patches/features/0053-perf-cache-goal-selector-locked-priorities.patch, lattice-server/minecraft-patches/features/0054-Revert-perf-cache-goal-selector-locked-priorities.patch, lattice-server/minecraft-patches/features/0055-perf-add-direct-dense-AABB-cell-layout.patch, lattice-server/minecraft-patches/features/0056-Revert-perf-add-direct-dense-AABB-cell-layout.patch, lattice-server/minecraft-patches/features/0057-perf-reuse-inside-block-traversal-scratch-state.patch, lattice-server/minecraft-patches/features/0058-fix-defer-inside-block-scratch-allocation.patch, lattice-server/minecraft-patches/features/0059-Revert-fix-defer-inside-block-scratch-allocation.patch, lattice-server/minecraft-patches/features/0060-Reapply-fix-defer-inside-block-scratch-allocation.patch, lattice-server/minecraft-patches/features/0061-Revert-Reapply-fix-defer-inside-block-scratch-alloca.patch, lattice-server/minecraft-patches/features/0062-Revert-perf-reuse-inside-block-traversal-scratch-sta.patch, lattice-server/minecraft-patches/features/0063-perf-reuse-nearest-target-candidates.patch, lattice-server/minecraft-patches/features/0064-Revert-perf-reuse-nearest-target-candidates.patch, lattice-server/minecraft-patches/features/0068-perf-skip-static-inside-block-traversal-dedup.patch, lattice-server/minecraft-patches/features/0069-Revert-perf-skip-static-inside-block-traversal-dedup.patch, lattice-server/minecraft-patches/features/0071-perf-cache-repeated-spatial-cell-bucket-lookups.patch, lattice-server/minecraft-patches/features/0072-Revert-perf-cache-repeated-spatial-cell-bucket-looku.patch, lattice-server/minecraft-patches/features/0108-fix-worldgen-expose-xoroshiro-positional-seeds.patch, lattice-server/minecraft-patches/features/0109-perf-worldgen-add-SoA-noise-interpolation-state.patch, lattice-server/minecraft-patches/features/0110-perf-worldgen-reuse-palette-reencode-remap-scratch.patch, lattice-server/minecraft-patches/features/0111-perf-worldgen-cache-stable-ore-tag-matches.patch, lattice-server/minecraft-patches/features/0112-perf-worldgen-inline-cached-final-density-reads.patch, lattice-server/minecraft-patches/features/0113-fix-worldgen-expose-legacy-positional-seed.patch, lattice-server/minecraft-patches/features/0117-perf-worldgen-combine-live-heightmap-updates.patch, lattice-server/minecraft-patches/sources/net/minecraft/core/Holder.java.patch, lattice-server/minecraft-patches/sources/net/minecraft/server/level/WorldGenRegion.java.patch, lattice-server/minecraft-patches/sources/net/minecraft/world/level/biome/Biome.java.patch, lattice-server/minecraft-patches/sources/net/minecraft/world/level/biome/Climate.java.patch, lattice-server/minecraft-patches/sources/net/minecraft/world/level/chunk/BulkSectionAccess.java.patch, lattice-server/minecraft-patches/sources/net/minecraft/world/level/chunk/LevelChunkSection.java.patch, lattice-server/minecraft-patches/sources/net/minecraft/world/level/chunk/LinearPalette.java.patch, lattice-server/minecraft-patches/sources/net/minecraft/world/level/chunk/PalettedContainer.java.patch, lattice-server/minecraft-patches/sources/net/minecraft/world/level/chunk/ProtoChunk.java.patch, lattice-server/minecraft-patches/sources/net/minecraft/world/level/levelgen/Aquifer.java.patch, lattice-server/minecraft-patches/sources/net/minecraft/world/level/levelgen/DensityFunctions.java.patch, lattice-server/minecraft-patches/sources/net/minecraft/world/level/levelgen/NoiseBasedChunkGenerator.java.patch, lattice-server/minecraft-patches/sources/net/minecraft/world/level/levelgen/NoiseChunk.java.patch, lattice-server/minecraft-patches/sources/net/minecraft/world/level/levelgen/OreVeinifier.java.patch, lattice-server/minecraft-patches/sources/net/minecraft/world/level/levelgen/XoroshiroRandomSource.java.patch, lattice-server/minecraft-patches/sources/net/minecraft/world/level/levelgen/feature/OreFeature.java.patch, lattice-server/minecraft-patches/sources/net/minecraft/world/level/levelgen/synth/ImprovedNoise.java.patch, lattice-server/paper-patches/features/0002-feat-initialize-Lattice-native-acceleration-at-start.patch, lattice-server/purpur-patches/features/0001-Rebrand.patch, lattice-server/src/main/java/com/latticemc/lattice/bridge/ChunkBlockColumn.java, lattice-server/src/main/java/com/latticemc/lattice/bridge/HeightmapAccessor.java, lattice-server/src/main/java/com/latticemc/lattice/bridge/ImprovedNoiseAccessor.java, lattice-server/src/main/java/com/latticemc/lattice/bridge/NativeInterpolatedNoiseAccess.java, lattice-server/src/main/java/com/latticemc/lattice/bridge/NativeNormalNoiseAccess.java, lattice-server/src/main/java/com/latticemc/lattice/bridge/PalettedContainerAccess.java, lattice-server/src/main/java/com/latticemc/lattice/bridge/PerlinNoiseAccessor.java, lattice-server/src/main/java/com/latticemc/lattice/bridge/PerlinSnapshot.java, lattice-server/src/main/java/com/latticemc/lattice/bridge/SurfaceSystemAccessImpl.java, lattice-server/src/main/java/com/latticemc/lattice/bridge/SurfaceSystemAccessor.java, lattice-server/src/main/java/com/latticemc/lattice/bridge/SurfaceSystemCallbacks.java, lattice-server/src/main/java/com/latticemc/lattice/nativelib/CompiledSurfaceRules.java, lattice-server/src/main/java/com/latticemc/lattice/nativelib/DirectCellColumnCache.java, lattice-server/src/main/java/com/latticemc/lattice/nativelib/LatticeNative.java, lattice-server/src/main/java/com/latticemc/lattice/nativelib/NativeCacheAllInCellAccess.java, lattice-server/src/main/java/com/latticemc/lattice/nativelib/NativeClimateGrid.java, lattice-server/src/main/java/com/latticemc/lattice/nativelib/NativeDensityFunction.java, lattice-server/src/main/java/com/latticemc/lattice/nativelib/NativeNoiseChunkAccess.java, lattice-server/src/main/java/com/latticemc/lattice/nativelib/NativeNoiseInterpolatorAccess.java, lattice-server/src/main/java/com/latticemc/lattice/nativelib/NativeOreVeinBlockStateFiller.java, lattice-server/src/main/java/com/latticemc/lattice/nativelib/NativeOreVeinSampler.java, lattice-server/src/main/java/com/latticemc/lattice/nativelib/NativeScalarNoiseControl.java, lattice-server/src/main/java/com/latticemc/lattice/nativelib/SurfaceRuleCompiler.java, lattice-server/src/main/java/com/latticemc/lattice/nativelib/SurfaceSystemAccess.java, lattice-server/src/main/java/com/latticemc/lattice/nativelib/WorldgenProfiler.java, lattice-server/src/test/java/ca/spottedleaf/moonrise/patches/collisions/HardCollisionQueryScratchTestSuite.java, lattice-server/src/test/java/com/latticemc/lattice/bridge/ChunkBlockColumnTestSuite.java, lattice-server/src/test/java/com/latticemc/lattice/bridge/PathFinderNativeSupportTestSuite.java, lattice-server/src/test/java/com/latticemc/lattice/bridge/PathfinderBuffersTestSuite.java, lattice-server/src/test/java/com/latticemc/lattice/nativelib/NativeAabbQueryTestSuite.java, lattice-server/src/test/java/com/latticemc/lattice/nativelib/NativeBiologicalAiBenchmark.java, lattice-server/src/test/java/com/latticemc/lattice/nativelib/NativeBiologicalAiGateTestSuite.java, lattice-server/src/test/java/com/latticemc/lattice/nativelib/NativeBiologicalAiTest.java, lattice-server/src/test/java/com/latticemc/lattice/nativelib/NativeBrainEligibilityBenchmark.java, lattice-server/src/test/java/com/latticemc/lattice/nativelib/NativeBrainEligibilityTestSuite.java, lattice-server/src/test/java/com/latticemc/lattice/nativelib/NativeClimateGridTestSuite.java, lattice-server/src/test/java/com/latticemc/lattice/nativelib/NativeEntityVisibilityBenchmark.java, lattice-server/src/test/java/com/latticemc/lattice/nativelib/NativeFindTopSurfaceTestSuite.java, lattice-server/src/test/java/com/latticemc/lattice/nativelib/NativeLineOfSightBenchmarkTestSuite.java, lattice-server/src/test/java/com/latticemc/lattice/nativelib/NativeMaterialRulesSeedTestSuite.java, lattice-server/src/test/java/com/latticemc/lattice/nativelib/NativePathfinderRejectionStatsTestSuite.java, lattice-server/src/test/java/com/latticemc/lattice/nativelib/NativePathfinderSearchParityTestSuite.java, lattice-server/src/test/java/com/latticemc/lattice/nativelib/NativeTargetSamplerGateTestSuite.java, lattice-server/src/test/java/com/latticemc/lattice/util/EntityActivationKdTreeTestSuite.java, lattice-server/src/test/java/com/latticemc/lattice/util/EntityDistanceRadixSortTestSuite.java, lattice-server/src/test/java/com/latticemc/lattice/util/collection/BrainCollectionTestSuite.java, lattice-server/src/test/java/com/latticemc/lattice/world/EntityPushStateTestSuite.java, lattice-server/src/test/java/net/minecraft/core/HolderTagLookupTest.java, lattice-server/src/test/java/net/minecraft/core/HolderTagLookupTestSuite.java, lattice-server/src/test/java/net/minecraft/core/PatchedDataComponentMapMaxStackSizeTestSuite.java, lattice-server/src/test/java/net/minecraft/world/entity/EntityCollisionScratchTestSuite.java, lattice-server/src/test/java/net/minecraft/world/entity/InsideBlockEffectApplierTestSuite.java, lattice-server/src/test/java/net/minecraft/world/entity/ai/BrainOptimizedCollectionTestSuite.java, lattice-server/src/test/java/net/minecraft/world/entity/ai/goal/GoalSelectorLockedPriorityTestSuite.java, lattice-server/src/test/java/net/minecraft/world/entity/ai/sensing/NonPoiBlockSearchParityTestSuite.java, lattice-server/src/test/java/net/minecraft/world/entity/ai/sensing/SensingBatchTestSuite.java, lattice-server/src/test/java/net/minecraft/world/entity/item/ItemEntityDeltaMovementTestSuite.java, lattice-server/src/test/java/net/minecraft/world/entity/item/ItemEntityMergeParityTestSuite.java, lattice-server/src/test/java/net/minecraft/world/level/BlockGetterTraversalTestSuite.java, lattice-server/src/test/java/net/minecraft/world/level/ExplosionRayStepParityCheck.java, lattice-server/src/test/java/net/minecraft/world/level/biome/BiomeSurfaceTemperatureParityTest.java, lattice-server/src/test/java/net/minecraft/world/level/biome/BiomeSurfaceTemperatureParityTestSuite.java, lattice-server/src/test/java/net/minecraft/world/level/biome/ClimateRTreeParityTest.java, lattice-server/src/test/java/net/minecraft/world/level/biome/ClimateRTreeParityTestSuite.java, lattice-server/src/test/java/net/minecraft/world/level/block/entity/HopperItemQueryScratchTestSuite.java, lattice-server/src/test/java/net/minecraft/world/level/chunk/LinearPaletteLastValueTest.java, lattice-server/src/test/java/net/minecraft/world/level/chunk/LinearPaletteLastValueTestSuite.java, lattice-server/src/test/java/net/minecraft/world/level/chunk/PalettedContainerResizeRemapTestSuite.java, lattice-server/src/test/java/net/minecraft/world/level/levelgen/AquiferCenterCacheParityTest.java, lattice-server/src/test/java/net/minecraft/world/level/levelgen/AquiferCenterCacheParityTestSuite.java, lattice-server/src/test/java/net/minecraft/world/level/levelgen/DensityFunctionsAp2ScratchTest.java, lattice-server/src/test/java/net/minecraft/world/level/levelgen/DensityFunctionsAp2ScratchTestSuite.java, lattice-server/src/test/java/net/minecraft/world/level/levelgen/HeightmapCombinedUpdateTestSuite.java, lattice-server/src/test/java/net/minecraft/world/level/levelgen/NativeDensityFunctionWorldgenBenchmark.java, lattice-server/src/test/java/net/minecraft/world/level/levelgen/NoiseChunkFinalDensityCacheTest.java, lattice-server/src/test/java/net/minecraft/world/level/levelgen/NoiseChunkFinalDensityCacheTestSuite.java, lattice-server/src/test/java/net/minecraft/world/level/levelgen/NoiseChunkSoaInterpolationTestSuite.java, lattice-server/src/test/java/net/minecraft/world/level/levelgen/NoiseInterpolatorCellFillTest.java, lattice-server/src/test/java/net/minecraft/world/level/levelgen/NoiseInterpolatorCellFillTestSuite.java, lattice-server/src/test/java/net/minecraft/world/level/levelgen/OreVeinifierEarlyRejectTest.java, lattice-server/src/test/java/net/minecraft/world/level/levelgen/OreVeinifierEarlyRejectTestSuite.java, lattice-server/src/test/java/net/minecraft/world/level/levelgen/XoroshiroAquiferOffsetsTest.java, lattice-server/src/test/java/net/minecraft/world/level/levelgen/XoroshiroAquiferOffsetsTestSuite.java, lattice-server/src/test/java/net/minecraft/world/level/levelgen/feature/OreFeatureTagMatchCacheTestSuite.java, lattice-server/src/test/java/net/minecraft/world/level/levelgen/synth/ImprovedNoiseFlattenedParityTest.java, lattice-server/src/test/java/net/minecraft/world/level/levelgen/synth/ImprovedNoiseFlattenedParityTestSuite.java, lattice-server/src/test/java/net/minecraft/world/level/levelgen/synth/NativeNoiseJniBenchmark.java, test-plugin/activation-bench/Invoke-ActivationBenchManualAbba.ps1.
- Confirming native library exposure separately from absent Java production route
  - Follow-up prompt: Review deferred unit nbt-stack-uaf and close its stated proof gap.
