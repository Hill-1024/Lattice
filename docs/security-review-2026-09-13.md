# Native security review and dead-code inventory

Scope: a source review of Lattice at `605b1991212a29265dc6a60ec0c7ba7a986dc48c`, followed by focused remediation. This is not a certification that the whole server is vulnerability-free. Unrelated local hardening changes from the audit workspace are not included in this patch.

## Findings and remediation

| Finding | Reachability / impact | Change |
| --- | --- | --- |
| Native extraction cache replacement (medium) | A local user able to pre-create or modify the shared cache could supply executable code subsequently passed to `System.load`. The base revision also accepted same-size poisoned cache files. Bundled resources, fallback resources and downloads share this sink. | Extract each library into a fresh private directory under a validated parent chain. Never reuse the old predictable cache. Reject foreign ownership, unsafe directory permissions and modifying ACL grants; inspect Darwin ACLs because mode 0700 alone does not remove inherited grants. |
| Unbounded pathfinder state mirror (medium) | Repeated snapshots for geographically distinct regions of one world retain native sections on each pathfinding thread. Sustained mob navigation can increase process memory; world changes and selective invalidations do not provide a capacity bound. | Limit each mirror to 512 sections (about 12 MiB of section data plus map overhead). Preflight the complete snapshot before insertion; clear old entries only when the combined working set would exceed the limit. Oversized snapshots bypass caching and still feed the existing eager pathfinder. |
| Stale NBT parent frame (dormant native defect) | `push_frame` relocates its stack; two container-list branches subsequently modify a stale parent reference. ASan reproduces a heap-use-after-free. The JNI NBT-index exports have no current Java declarations, so this review does not claim a remotely reachable server vulnerability. | Consume the parent list element before pushing the child in both compound-element and nested-list branches. |

### Compatibility and boundaries

- `lattice.native.path` remains an explicit trusted administrator override. `lattice.native.cacheDir` remains an extraction-parent override; missing parent directories can be created beneath a trusted ancestor. The default parent is `java.io.tmpdir`, without reusing its legacy `lattice-native` child.
- Permission/ownership inspection failures fail closed. POSIX systems require Unix owner/mode attributes; Windows requires ACL support. Windows system/administrator principals are trusted. Darwin inspection uses `/bin/ls -lde` with the C locale and rejects unknown or modifying ALLOW permissions, including inherited grants. Some permissive custom cache locations must be changed to a private location.
- Extracted libraries remain until JVM exit. This preserves Windows loaded-library lifetime behavior; shutdown cleanup is best effort, as before. Forced process termination can leave private directories behind.
- Eviction occurs before uploads, never while a path search holds section pointers. Existing coverage misses, Java fallback, invalidation and world switching remain intact.
- This patch fixes the NBT frame lifetime only. The existing one-byte index depth format versus the 512-depth policy remains a separate dormant compatibility issue; it has not been redesigned here.

## Verification

- Regressions run against the base revision reproduced same-size cache poisoning, unbounded mirror growth and the NBT heap-use-after-free.
- `cmake --build lattice-native/build-security` and `ctest --test-dir lattice-native/build-security --output-on-failure`: all 31 suites passed with AddressSanitizer and UndefinedBehaviorSanitizer on macOS ARM64.
- `scripts/testNativeLoader.py` compiles the production bootstrap classes with Java 21 and runs both JUnit suites independently of generated Minecraft sources. It verifies fresh content, unsafe parent/ancestor and alias rejection, private permissions, failed-write cleanup, foreign-owner policy, Darwin extended ACLs and Windows ACL cases. OS-specific cases are skipped elsewhere.
- Set `LATTICE_TEST_NATIVE_LIBRARY` to a built, unsanitized native library to additionally exercise `System.load` through extraction. This passed locally with the Release ARM64 library. Loading an ASan-instrumented dylib into the stock signed JVM is unsupported by the local sanitizer runtime policy; native sanitizers run in the C++ test binaries instead.
- `.github/workflows/native-loader-tests.yml` runs the bootstrap suites on Linux, macOS and Windows. Check the PR's actual CI results for platform status.
- Full generated-server Gradle validation is recorded in the PR; focused bootstrap success alone does not imply the full server build passed.

## Dead-code markers

These are review markers, not a request to remove public interfaces indiscriminately. Paths and symbol references were checked against this patch's upstream base. Private/unbuilt code can be removed separately; test-only and exported native surfaces need an explicit compatibility decision.

| Location | Classification and evidence | Suggested follow-up |
| --- | --- | --- |
| `scripts/apatch.sh:73` | Unreachable rename branch: every assignment leaves `noapply=1`; `applied` only feeds this branch. | Remove the obsolete branch or restore an intentional opt-in mode. |
| `lattice-server/src/main/java/com/latticemc/lattice/bridge/PathFinderNativeSupport.java:354` — `CachingPathfindingContext` | Private nested class with no construction site. | Remove after confirming no planned reuse. |
| `lattice-server/src/main/java/com/latticemc/lattice/bridge/PathfinderStateSnapshot.java:16` — `descriptorCount` | Unused private field; the method obtains the descriptor count from `cache`. | Delete the field. |
| `lattice-server/src/main/java/com/latticemc/lattice/bridge/HerbivoreAiSupport.java:295` — `buildFleeCandidates` | Private helper has no caller in this class. Other classes have their own active helpers with the same name. | Delete this helper only. |
| `lattice-server/src/main/java/com/latticemc/lattice/util/EntityActivationKdTree.java:161` — `PlayerTree.axis`, `x`, `z` | Arrays are allocated/written but not read. The surrounding KD-tree is active. | Remove unused storage and assignments. |
| `lattice-native/src/world/entity/pathfinder.cpp:952` | Old materialization implementation excluded by `#if 0`. | Remove the disabled implementation after retaining any useful benchmark history. |
| `lattice-native/src/world/gen/densityfunction/density_function_avx512.cpp` | Source is not selected by the current CMake library or test targets. | Reconcile with supported dispatch implementations before removal. |
| `lattice-native/src/world/light/packed_info.hpp` | No include sites in the tracked source. | Remove or document a concrete future consumer. |
| `test-plugin/src/main/java/org/purpurmc/testplugin/TestPluginBootstrap.java` and `TestPluginLoader.java` | Empty classes; their `paper-plugin.yml` registrations are commented out. | Delete placeholders or register real implementations when needed. |
| `lattice-server/src/main/java/com/latticemc/lattice/nativelib/NativeIoCapability.java` | Only its test suite currently references it. | Mark as a prototype; do not claim active I/O capability negotiation. |
| `lattice-native/jni/chunk_serializer.cpp` — `nativeParseNbtIndex` / `nativeFreeNbtIndex` | Exported native implementation without matching `NativeChunkSerializer` Java declarations. The parser itself has C++ tests. | Keep the lifetime fix; decide whether to expose or retire the dormant JNI API separately. |

No broad public JNI cleanup is included. In particular, the loader checksum helpers are active in the upstream download path and are not dead code in this PR.
