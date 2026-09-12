// Unit tests for the overflow-safe JNI length validation helpers.
//
// Regression coverage for the audit finding V2: the octave/double-perlin JNI
// entry points compared `static_cast<jsize>(N * 256)` against a real array
// length, so a large N truncated the required length and let an undersized
// array through (heap out-of-bounds read). `checked_count` is the pure helper
// that now performs the multiplication in 64-bit without truncation, so the
// overflow behaviour is testable here without a JVM or huge allocations.

#define DOCTEST_CONFIG_IMPLEMENT_WITH_MAIN
#include <doctest/doctest.h>

#include <cstddef>
#include <limits>

#include "jni_helper.hpp"

using lattice::jni::checked_count;
using lattice::jni::kCountOverflow;

// Compile-time guards for the exact cases the JNI fix depends on.
static_assert(checked_count(std::size_t{1} << 24, 256) == (std::size_t{1} << 32), "must not truncate to 32-bit");
static_assert(checked_count(0, 256) == 0, "zero items must be allowed");
static_assert(checked_count(4, 3) == 12, "exact multiply");
static_assert(checked_count(std::numeric_limits<std::size_t>::max(), 2) == kCountOverflow, "overflow must be flagged");

TEST_CASE("jni bounds: checked_count multiplies exactly") {
    CHECK(checked_count(0, 256) == 0);
    CHECK(checked_count(1, 256) == 256);
    CHECK(checked_count(4, 3) == 12);
    CHECK(checked_count(1024, 256) == 262144);
    CHECK(checked_count(7, 0) == 0);
}

TEST_CASE("jni bounds: the 32-bit truncation case is preserved in 64-bit") {
    // N = 2^24 makes N * 256 == 2^32, which a 32-bit `jsize` cast turns into 0.
    const std::size_t n = std::size_t{1} << 24;
    CHECK(checked_count(n, 256) == (std::size_t{1} << 32));
    CHECK(checked_count(n, 256) != 0); // the pre-fix comparison silently passed on 0
}

TEST_CASE("jni bounds: checked_count flags genuine overflow") {
    const std::size_t max = std::numeric_limits<std::size_t>::max();
    CHECK(checked_count(max, 2) == kCountOverflow);
    CHECK(checked_count(max / 2 + 1, 2) == kCountOverflow);
    CHECK(checked_count(max / 256 + 1, 256) == kCountOverflow);
    CHECK(checked_count(max / 256, 256) != kCountOverflow);
    CHECK(checked_count(max, 1) == max);
}
