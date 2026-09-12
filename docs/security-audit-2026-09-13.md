# Lattice Security Audit — 2026-09-13

> **Pre-submission correction (2026-09-13):** This is the initial audit record. The [Chinese PR report](security-audit-2026-09-13-pr.zh-CN.md) and [rescan](security-audit-2026-09-13-rescan/report.md) supersede its completion claims. V3's residual cache pre-creation risk is addressed by the private extraction implementation consolidated from PR #1; V13 adds an optional constrained overload without enforcing it in production callers; V9 has no complete dependency verification metadata. Initial severity totals are not counts of verified remote vulnerabilities; installed plugins and administrator-controlled settings are separate trust boundaries.

Audit of the `Lattice` repository (Minecraft 1.21.11 server fork of Purpur, with a C++ native
acceleration library loaded via JNI). Revision at audit time: branch `ver/1.21.11`.

Chinese version: [`security-audit-2026-09-13.zh-CN.md`](security-audit-2026-09-13.zh-CN.md).

## 1. Executive summary

The audit found **15 issues: 2 high, 7 medium, 5 low, 1 informational**. All were remediated in
this change set, except where explicitly noted as a documented residual item.

Two issues are worth calling out:

- **V1 (High)** — `LatticeNativeLoader` downloaded a native library over the network and
  `System.load`-ed it, but obtained the SHA-256 checksum from **the same origin as the payload**,
  accepted plain `http://`, followed redirects anywhere, and enabled download by default. Anyone
  who could tamper with, or impersonate, the download origin obtained **arbitrary native code
  execution** in the server JVM.
- **V2 (High)** — Four JNI length checks computed `N * 3` / `N * 256` and truncated the result to a
  32-bit `jsize`. With `N = 2^24`, `N * 256` wraps to `0`, an empty permutation array passed
  validation, and the following `memcpy(..., perms + i * 256, 256)` read **out of bounds**.
  `NativeOctavePerlinNoise.tryCreate(...)` is `public static`, so any plugin could trigger a JVM
  crash or memory disclosure.

No hardcoded credentials, private keys, keystores, or `.env` files were found in the tree or in git
history. No `pull_request_target`, `curl | bash`, or secret interpolation into `run:` steps was
found in CI.

## 2. Scope, trust model, and severity

**In scope:** first-party Java (`lattice-server/src/main/java/com/latticemc/lattice`,
`test-plugin`), first-party C++ (`lattice-native/{src,jni}`), build files, CI workflows, and the
dependency/update supply chain.

**Out of scope:** upstream Paper/Purpur/Minecraft patch contents (present only as `.patch` files and
applied at build time), and the Minecraft network protocol implementation.

**Trust model.** Two attacker classes are considered:

- *Remote/untrusted data* — world data, network input, and anything reachable from a plugin. This
  is the primary threat model.
- *Local/same-host or supply-chain* — an attacker who can pre-place files on the host, or who
  controls a download/update origin. Server operators with write access to `lattice.yml` or the JVM
  command line are treated as trusted (they can already run arbitrary code), but weaknesses that let
  a *hostile download origin* or a *local unprivileged user* escalate are treated as real findings.

Severity: **High** = code execution or memory corruption reachable without operator privileges;
**Medium** = memory disclosure/DoS or a control bypass requiring a specific condition; **Low** =
defense-in-depth or hardening; **Info** = hygiene.

## 3. Findings

### Summary

| ID | Sev | Area | Issue | Status |
|----|-----|------|-------|--------|
| V1 | High | Loader | Same-origin checksum + `http://` + open redirects → native RCE | Fixed |
| V2 | High | JNI | 32-bit truncation in array-length checks → heap OOB read | Fixed |
| V3 | Med-High | Loader | Predictable cache path, symlink-following reuse check, world-writable tmp | Fixed |
| V4 | Medium | JNI | 32-bit multiply overflow in tick-mask length check | Fixed |
| V5 | Medium | Loader | SSRF via configurable release base URL / redirects | Fixed |
| V6 | Medium | Native I/O | Unbounded `zlib_validate` scratch growth (decompression bomb) | Fixed |
| V7 | Low | Loader | `lattice.native.path` loads arbitrary native code | Documented + warn |
| V8 | Medium | Supply chain | Gradle wrapper from third-party mirror, no SHA-256 pin | Fixed |
| V9 | Medium | Supply chain | No dependency verification/lockfiles; SNAPSHOT dependency | Partially fixed |
| V10 | Medium | CI | `upstream.yml` auto-pushes; `[ci-skip]` guard never matched on schedule | Fixed |
| V11 | Medium | CI | Actions pinned to mutable tags, incl. `lukka/get-cmake@latest` | Fixed |
| V12 | Low | Supply chain | CMake `FetchContent` tags mutable, not commit-pinned | Fixed |
| V13 | Low | Java API | `NativeRegionFileRead.open(Path)` = arbitrary-file-read primitive | Fixed |
| V14 | Low | test-plugin | Benchmark commands had no permission gate | Fixed |
| V15 | Info | Repo hygiene | `.gitignore` did not exclude `.env`/key/keystore files | Fixed |

---

### V1 — Native download trust model allows arbitrary native code execution (High)

**Location:** `lattice-server/src/main/java/com/latticemc/lattice/bootstrap/LatticeNativeLoader.java`
(formerly `downloadReleaseNative` / `verifyChecksum` / `downloadBytes`).

**Evidence (before):** the payload was fetched from `buildReleaseAssetUrl(base, release, asset)` and
the expected digest from `endpoint + ".sha256"` — the same origin. `downloadBytes` accepted any
`HttpURLConnection` (including `http://`) and set `setInstanceFollowRedirects(true)`. Download was
enabled by default (`native.download` default `true`), and the result was passed to
`System.load(...)`.

**Impact:** an attacker controlling the origin, DNS, or a TLS-terminating middlebox supplied both
the library and the matching checksum, achieving native code execution inside the server JVM. This
is the highest-impact finding: it defeats the entire point of the checksum.

**Fix:**

- A download now requires a **locally pinned digest**: a built-in release manifest
  (`BUILT_IN_RELEASE_DIGESTS`, empty in source builds, populated by the release pipeline) or an
  operator-provided `lattice.native.sha256` / `native.sha256`. If neither is present, the loader
  **refuses to download** with an actionable error. The remote `.sha256` is no longer fetched.
- `requireHttpUri` rejects any non-`https` URL unless `lattice.native.allowInsecureHttp=true`
  (default `false`).
- Redirects are disabled at the JDK level and followed manually with a **host allowlist**
  (`github.com`, `objects.githubusercontent.com`, `release-assets.githubusercontent.com`,
  `codeload.github.com`) and a maximum of 5 hops.
- `lattice.native.path` (an admin override that loads an arbitrary local library) now logs an
  explicit warning that it bypasses bundled-library and checksum verification.

**Verification:** unit tests cover the HTTPS requirement, scheme rejection, redirect allowlist,
trusted-digest resolution, and digest mismatch.

---

### V2 — JNI array-length checks truncate to 32 bits, allowing a heap out-of-bounds read (High)

**Location:** `lattice-native/jni/octave_perlin_noise.cpp`, `lattice-native/jni/double_perlin_noise.cpp`.

**Evidence (before):**
`if (env->GetArrayLength(jPermutations) != static_cast<jsize>(N * 256))` — `N` is a `size_t`, so
`N * 256` is computed in 64-bit and then truncated to a signed 32-bit `jsize`. For `N = 2^24`,
`N * 256 = 2^32`, which truncates to `0`; the check then required a zero-length permutation array,
which passed, and `std::memcpy(out_octs[i].permutation, perms + i * 256, 256)` read far past the
array. The same pattern existed for `N * 3` origins and in both halves of `double_perlin_noise.cpp`.

**Impact:** heap out-of-bounds read → JVM crash (DoS) or memory disclosure. Reachable from
`NativeOctavePerlinNoise.tryCreate(...)` and `NativeDoublePerlinNoise`, both public entry points
callable by any plugin loaded into the server.

**Fix:** added a pure, `constexpr`, overflow-safe helper `checked_count(count, per_item)` to
`jni_helper.hpp` that returns a sentinel on overflow, plus `array_has_length`. All four checks now
compare `size_t` to `size_t` and reject overflow explicitly.

**Verification:** `tests/test_jni_bounds.cpp` exercises the exact `N = 2^24` truncation case and
genuine overflow, as both runtime checks and compile-time `static_assert`s. The three changed JNI
translation units were compiled with `clang++ -std=c++20 -fno-exceptions -fno-rtti -Wall -Wextra
-Wpedantic` (clean).

---

### V3 — Native extraction cache: symlink/TOCTOU and shared world-writable directory (Medium-High)

**Location:** `LatticeNativeLoader.extractToCache` / `resolveCacheDir`.

**Evidence (before):** the reuse check was `Files.exists(target) && Files.size(target) == bytes.length`
— `Files.size` follows symlinks and the content was never re-verified. The cache file name was
derived from a 64-bit-truncated digest, and the default directory was the shared, world-writable
`java.io.tmpdir/lattice-native`. A local attacker who pre-placed a symlink at the predictable name
could get `System.load` to map an attacker-controlled library.

**Fix:**

- Default cache directory is now per-user (`java.io.tmpdir/lattice-native-<user>`) and created with
  POSIX `0700`. A symlinked cache directory is refused.
- Reuse requires the target to be a **regular file with `NOFOLLOW_LINKS`** whose **full SHA-256**
  matches the expected content (re-read and re-hashed, not a size comparison).
- Bytes are written to a temp file and atomically moved into place; the written entry is re-verified
  before use.

**Verification:** a unit test pre-places a symlink at the target name and asserts the symlink is
replaced (not followed) and the link target is left untouched.

---

### V4 — 32-bit multiply overflow in tick-mask validation (Medium)

**Location:** `lattice-native/jni/tick.cpp`.

**Evidence (before):** `if (env->GetArrayLength(jSectionTickMasks) < sectionCount * maskLongsPerSection)`
— both operands are `jint`, so the product is computed in 32-bit and can wrap to `0` or negative,
satisfying the `<` check with a too-short array; `random_tick_filter` then indexes out of bounds.

**Fix:** the product is computed with `checked_count` in `size_t` and compared in `size_t`; overflow
is rejected.

---

### V5 — SSRF through the configurable release base URL (Medium)

**Location:** `LatticeNativeLoader` (`native.release-base-url` / `lattice.native.releaseBaseUrl`).

**Evidence (before):** the endpoint was built from an operator-configurable base URL, and redirects
were followed anywhere. Combined with V1 this reached arbitrary hosts (e.g. cloud metadata) and
turned a hostile redirect into native code execution.

**Fix:** HTTPS required by default, redirects resolved manually against a host allowlist, redirect
count capped. See V1.

---

### V6 — Unbounded decompression loop in `zlib_validate` (Medium)

**Location:** `lattice-native/src/io/compression/zlib_codec.cpp`.

**Evidence (before):** `zlib_validate` doubled its scratch capacity in a loop until decompression
succeeded, with no upper bound — a classic decompression bomb. It is currently only called from
tests, so practical reachability is limited, but it is a latent DoS if wired to untrusted input.

**Fix:** added a `max_output_bytes` parameter (`kMaxValidateOutputBytes` = 1 GiB default). When the
stream needs more than the ceiling, validation returns `kBadData` instead of growing further.

**Verification:** new tests assert a sub-size ceiling is rejected and a zero ceiling is `kBadArg`.

---

### V7 — `lattice.native.path` loads arbitrary native code (Low; trusted-admin)

**Location:** `LatticeConfig` (`native.library-path`) → `LatticeNativeLoader.load`.

**Assessment:** this is a deliberate developer/operator override (documented in the READMEs as a PGO
workflow). Writing it requires control of `lattice.yml` or the JVM command line, which already
implies code execution. Not treated as a vulnerability, but the loader now logs a clear warning that
the override bypasses all verification.

---

### V8 — Gradle wrapper served from a third-party mirror without a checksum (Medium)

**Location:** `gradle/wrapper/gradle-wrapper.properties`.

**Evidence (before):**
`distributionUrl=https://mirrors.aliyun.com/macports/distfiles/gradle/gradle-9.2.0-bin.zip` with
`validateDistributionUrl=true` but **no `distributionSha256Sum`**. `validateDistributionUrl` only
checks URL syntax, so the build toolchain binary itself was unverified.

**Fix:** added the official Gradle 9.2.0 checksum
(`distributionSha256Sum=df67a32e86e3276d011735facb1535f64d0d88df84fa87521e90becc2d735444`). The
mirror is retained for network reasons; if it ever serves bytes that differ from the official
artifact, the build now fails loudly instead of executing an unverified toolchain.

---

### V9 — No dependency verification or lockfiles; SNAPSHOT dependency (Medium; partially fixed)

**Location:** `*.gradle.kts`, `gradle.properties`.

**Evidence:** no `gradle/verification-metadata.xml`, no dependency locking, and
`com.velocitypowered:velocity-native:3.4.0-SNAPSHOT` resolves from a snapshot repository
(non-reproducible). `me.lucko:spark-api` uses a timestamped coordinate.

**Fix/status:** this remains a **documented residual**, not a committed mitigation. Generation was
attempted during the audit:

- `./gradlew --write-verification-metadata sha256 help` does produce a file, but it is incomplete
  (it only covers dependencies resolved at configuration time) and an incomplete file **breaks the
  build**: `./gradlew applyAllPatches` then fails, because Gradle refuses artifacts that have no
  entry in the metadata. This was observed directly.
- A full-resolution attempt (`--write-verification-metadata sha256 :lattice-server:compileJava
  :lattice-server:testClasses`) fails inside Gradle itself with
  `Multiple entries with same key: me.lucko:spark-api:0.1-20240720.200737-2`, because the
  timestamped snapshot coordinate produces duplicate component entries.

No `verification-metadata.xml` is therefore committed — a partial one is worse than none. The
`velocity-native:3.4.0-SNAPSHOT` coordinate is required by the Paper/Paperweight dev bundle, and
forcing a different version risks breaking the build; it is recorded here as a **controlled
exception** and excluded from automated updates in `.github/dependabot.yml`. **Recommended
sequence:** replace the timestamped/SNAPSHOT coordinates with pinned releases, *then* generate and
commit `gradle/verification-metadata.xml` and enable `dependencyVerification`.

---

### V10 — Upstream auto-sync could not honor its skip guard (Medium)

**Location:** `.github/workflows/upstream.yml`.

**Evidence (before):** `if: "!contains(github.event.commits[0].message, '[ci-skip]')"`. On a
`schedule` event `github.event.commits` is undefined, so the guard always evaluated true and could
never actually skip. The workflow ran every 15 minutes with top-level `contents: write` and pushed
directly to the default branch.

**Fix:** top-level permissions reduced to `contents: read` and `contents: write` scoped to the job;
a step now inspects `git log -1` for the `[ci-skip]` marker and gates the remaining steps; the push
remains a plain (non-force) push, so a non-fast-forward refuses rather than overwriting a concurrent
human commit. `scripts/upstreamCommit.sh` now builds the commit message with `printf` instead of
`echo -e` (so remote commit text is never reinterpreted) and strips `[ci-skip]` from upstream
messages.

---

### V11 — GitHub Actions pinned to mutable tags (Medium)

**Location:** all three workflows.

**Evidence (before):** `actions/checkout@v6`, `actions/setup-java@v4`,
`gradle/actions/setup-gradle@v5`, `actions/upload-artifact@v4`, `actions/download-artifact@v4`,
`actions/cache@v4`, `softprops/action-gh-release@v2`, and notably `lukka/get-cmake@latest`. A
compromised upstream action runs with the workflow's token (including `contents: write` in the
publish jobs).

**Fix:** every `uses:` is pinned to a full commit SHA with a trailing version comment.
`lukka/get-cmake@latest` was replaced by the pinned `v4.4.2` commit. `.github/dependabot.yml` now
tracks `github-actions` and `gradle` so pins stay current deliberately.

---

### V12 — CMake `FetchContent` dependencies not commit-pinned (Low)

**Location:** `lattice-native/CMakeLists.txt`.

**Evidence (before):** `GIT_TAG v1.20` (libdeflate) and `GIT_TAG v2.4.11` (doctest) — mutable tags
with shallow clones.

**Fix:** pinned to the resolved commits (`275aa514…` for libdeflate v1.20, `ae7a1353…` for doctest
v2.4.11); `GIT_SHALLOW` was removed because shallow clones cannot check out an arbitrary commit.

---

### V13 — `NativeRegionFileRead.open(Path)` is an arbitrary-file-read primitive (Low)

**Location:** `lattice-server/src/main/java/com/latticemc/lattice/nativelib/NativeRegionFileRead.java`.

**Evidence (before):** `open(Path)` passed `path.toAbsolutePath()` straight to native `open(..., O_RDONLY)`
with only NUL-byte validation. Callers currently pass internal RegionFile paths, but the public API
accepted any path.

**Fix:** added `open(Path path, Path allowedRoot)`; the path is resolved with `toRealPath()` (so
symlinks and `..` cannot escape) and must be contained in the root, otherwise `null` is returned and
the caller falls back to Java I/O. The unchecked single-argument overload is retained for internal
callers.

---

### V14 — Benchmark commands had no permission gate (Low)

**Location:** `test-plugin/src/main/java/org/purpurmc/testplugin/{Pathfinder,Item,EntityActivation}BenchmarkCommand.java`.

**Evidence (before):** all three commands were registered with no `setPermission`/`testPermission`,
so any player could spawn up to 4096 entities and drive heavy benchmark workloads.

**Fix:** each command now declares a permission (`lattice.bench.pathfinder` / `.item` / `.activation`)
and calls `testPermission(sender)` first. Note the `test-plugin` module is **disabled by default**
(`test-plugin.settings.gradle.kts` is generated commented out), so this only matters for a build
that explicitly enables it.

---

### V15 — `.gitignore` did not exclude secret file types (Info)

**Location:** `.gitignore`.

**Fix:** added `.env`, `.env.*`, `*.pem`, `*.key`, `*.p12`, `*.pfx`, `*.jks`, `*.keystore`. Also
added `!/docs/security-audit-*.md` so audit reports in the otherwise-ignored `docs/` directory can
be committed (the existing `docs/` file was force-added historically).

## 4. Remediation file map

| Area | Files changed |
|------|---------------|
| Loader / config | `bootstrap/LatticeNativeLoader.java`, `config/LatticeConfig.java` |
| JNI / native | `jni/jni_helper.hpp`, `jni/octave_perlin_noise.cpp`, `jni/double_perlin_noise.cpp`, `jni/tick.cpp`, `src/io/compression/zlib_codec.{hpp,cpp}` |
| Java API | `nativelib/NativeRegionFileRead.java` |
| Plugin | `test-plugin/.../{Pathfinder,Item,EntityActivation}BenchmarkCommand.java` |
| Build | `lattice-native/CMakeLists.txt`, `gradle/wrapper/gradle-wrapper.properties` |
| CI / supply chain | `.github/workflows/{build,native,upstream}.yml`, `.github/dependabot.yml`, `scripts/upstreamCommit.sh`, `.gitignore` |
| Tests | `LatticeNativeLoaderTestSuite.java`, `NativeRegionFileReadTestSuite.java`, `LatticeConfigTestSuite.java`, `lattice-native/tests/test_jni_bounds.cpp`, `test_zlib_codec.cpp`, `lattice-native/tests/CMakeLists.txt` |
| Docs | this report + Chinese translation |

## 5. Verification

Executed in this environment:

- **C++ build and tests (CMake + Ninja + CTest, Release).** `cmake -S lattice-native -B
  lattice-native/build-verify -G Ninja -DLATTICE_BUILD_TESTS=ON` then
  `ctest -R "jni_bounds|zlib_codec|octave_perlin_noise|double_perlin_noise|random_tick_filter"`.
  **All 5 tests passed**, including the new overflow-bound and decompression-ceiling tests.
  (Local note: this machine has CMake 4.4.3, while CI pins 3.28.3; doctest 2.4.11 needs
  `-DCMAKE_POLICY_VERSION_MINIMUM=3.5` under CMake 4.x. This is a local-only workaround.)
- **C++ compiler checks.** The three changed JNI translation units compile cleanly with
  `clang++ -std=c++20 -fno-exceptions -fno-rtti -Wall -Wextra -Wpedantic` against the JDK 21 JNI
  headers.
- **Java compilation.** All changed main sources (`LatticeNativeLoader`, `LatticeConfig`,
  `NativeRegionFileRead`, `LatticeNative`) and all changed test suites compile with
  `javac --release 21` against the Gradle dependency cache. This caught and fixed two missing
  `throws` clauses in the new loader tests.
- **Paperweight.** `./gradlew applyAllPatches` succeeded (231 Purpur + 20 Minecraft + 41 Paper
  patches applied) once the incomplete verification metadata was removed.
- **Java tests via Gradle.** `./gradlew :lattice-server:test` was run with system clang
  (`CC=/usr/bin/clang CXX=/usr/bin/clang++`) so the native build matched CI's Apple toolchain.
  Results for the affected suites: **`LatticeNativeLoaderTestSuite` 14/14 passed,
  `LatticeConfigTestSuite` 4/4 passed, `NativeRegionFileReadTestSuite` 3/3 passed.** (The run also
  reported 12 failures in unrelated Minecraft `@Suite` classes — those are an artifact of the
  `--tests` filter defeating JUnit suite discovery, not a regression: they are upstream test
  scaffolds that discover nested classes by package scanning.)

Recommended CI addition: the native `ctest` matrix already runs; add/keep a
`./gradlew :lattice-server:test` step (the existing `build` task runs it) so the new loader/config
tests execute on every PR.

## 6. Residual risks and recommendations

1. **Populate `BUILT_IN_RELEASE_DIGESTS`** in the release pipeline so the official build can
   auto-download without an operator-supplied digest. Until then, operators of non-bundled builds
   must set `native.sha256`.
2. **Generate and commit `gradle/verification-metadata.xml`** in a networked environment, then
   enable `dependencyVerification`. Revisit the `velocity-native` SNAPSHOT when a release exists.
3. **Sign release artifacts / publish an SBOM.** The README already warns that third-party rebuilds
   are indistinguishable; signing and an SBOM would let operators verify provenance.
4. **Consider SHA-pinning the base image and OS packages** in CI (the LLVM-MinGW download is already
   SHA-verified — a good existing control).
5. **CodeQL** was intentionally not added: the Paperweight patch pipeline does not build from source
   in a way CodeQL's autobuild understands, so a naive workflow would fail. If desired, run CodeQL
   against the standalone `lattice-native` CMake project and the unpatched `lattice-server` Java
   sources only.

## 7. Areas reviewed with no findings

- No command execution (`Runtime.exec`/`ProcessBuilder`) in first-party code.
- No `ObjectInputStream`/`readObject`, XStream, Jackson polymorphic typing, `pickle`, or `Marshal`.
- No SQL built from string concatenation; no JDBC in first-party code.
- No XXE-prone XML parsing.
- No hardcoded secrets, private keys, or keystores (checked the tree and git history).
- Configurate/SnakeYAML usage relies on the library's safe defaults; no custom resolvers.
