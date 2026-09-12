package com.latticemc.lattice.bootstrap;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LatticeNativeLoaderTestSuite {

    @Test
    void assetNamesCoverSupportedPlatforms() {
        assertEquals("lattice-native-linux-x86_64.so", LatticeNativeLoader.assetName(new LatticeNativeLoader.Platform(LatticeNativeLoader.Os.LINUX, LatticeNativeLoader.Arch.X86_64)));
        assertEquals("lattice-native-linux-aarch64.so", LatticeNativeLoader.assetName(new LatticeNativeLoader.Platform(LatticeNativeLoader.Os.LINUX, LatticeNativeLoader.Arch.AARCH64)));
        assertEquals("lattice-native-windows-x86_64.dll", LatticeNativeLoader.assetName(new LatticeNativeLoader.Platform(LatticeNativeLoader.Os.WINDOWS, LatticeNativeLoader.Arch.X86_64)));
        assertEquals("lattice-native-windows-aarch64.dll", LatticeNativeLoader.assetName(new LatticeNativeLoader.Platform(LatticeNativeLoader.Os.WINDOWS, LatticeNativeLoader.Arch.AARCH64)));
        assertEquals("lattice-native-macos-x86_64.dylib", LatticeNativeLoader.assetName(new LatticeNativeLoader.Platform(LatticeNativeLoader.Os.MAC, LatticeNativeLoader.Arch.X86_64)));
        assertEquals("lattice-native-macos-aarch64.dylib", LatticeNativeLoader.assetName(new LatticeNativeLoader.Platform(LatticeNativeLoader.Os.MAC, LatticeNativeLoader.Arch.AARCH64)));
        assertEquals("lattice-native-freebsd-x86_64.so", LatticeNativeLoader.assetName(new LatticeNativeLoader.Platform(LatticeNativeLoader.Os.FREEBSD, LatticeNativeLoader.Arch.X86_64)));
        assertEquals("lattice-native-freebsd-aarch64.so", LatticeNativeLoader.assetName(new LatticeNativeLoader.Platform(LatticeNativeLoader.Os.FREEBSD, LatticeNativeLoader.Arch.AARCH64)));
    }

    @Test
    void detectCoversSupportedPlatformsAndArchitectures() {
        assertEquals(new LatticeNativeLoader.Platform(LatticeNativeLoader.Os.LINUX, LatticeNativeLoader.Arch.X86_64), LatticeNativeLoader.detect("Linux", "amd64"));
        assertEquals(new LatticeNativeLoader.Platform(LatticeNativeLoader.Os.LINUX, LatticeNativeLoader.Arch.AARCH64), LatticeNativeLoader.detect("Linux", "aarch64"));
        assertEquals(new LatticeNativeLoader.Platform(LatticeNativeLoader.Os.WINDOWS, LatticeNativeLoader.Arch.X86_64), LatticeNativeLoader.detect("Windows 11", "x86_64"));
        assertEquals(new LatticeNativeLoader.Platform(LatticeNativeLoader.Os.WINDOWS, LatticeNativeLoader.Arch.AARCH64), LatticeNativeLoader.detect("Windows", "ARM64"));
        assertEquals(new LatticeNativeLoader.Platform(LatticeNativeLoader.Os.MAC, LatticeNativeLoader.Arch.X86_64), LatticeNativeLoader.detect("Mac OS X", "x64"));
        assertEquals(new LatticeNativeLoader.Platform(LatticeNativeLoader.Os.MAC, LatticeNativeLoader.Arch.AARCH64), LatticeNativeLoader.detect("Darwin", "arm64"));
        assertEquals(new LatticeNativeLoader.Platform(LatticeNativeLoader.Os.FREEBSD, LatticeNativeLoader.Arch.X86_64), LatticeNativeLoader.detect("FreeBSD", "amd64"));
        assertEquals(new LatticeNativeLoader.Platform(LatticeNativeLoader.Os.FREEBSD, LatticeNativeLoader.Arch.AARCH64), LatticeNativeLoader.detect("FreeBSD", "aarch64"));
        assertEquals(LatticeNativeLoader.Os.UNKNOWN, LatticeNativeLoader.detect("Plan9", "amd64").os());
    }

    @Test
    void releaseUrlsUseNativeLatestAndExplicitTags() {
        assertEquals("https://github.com/LatticeMC/Lattice/releases/download/native-latest/lattice-native-linux-x86_64.so",
                LatticeNativeLoader.buildReleaseAssetUrl(
                        "https://github.com/LatticeMC/Lattice/releases/download/",
                        "native-latest", "lattice-native-linux-x86_64.so"));
        assertEquals("https://github.com/LatticeMC/Lattice/releases/download/native-latest/lattice-native-linux-x86_64.so",
                LatticeNativeLoader.buildReleaseAssetUrl(
                        "https://github.com/LatticeMC/Lattice/releases/download", "latest",
                        "lattice-native-linux-x86_64.so"));
        assertEquals("https://example.test/releases/download/v1.2.3/lattice-native-windows-x86_64.dll",
                LatticeNativeLoader.buildReleaseAssetUrl(
                        "https://example.test/releases/download", "v1.2.3",
                        "lattice-native-windows-x86_64.dll"));
    }

    @Test
    void checksumParsingAcceptsMatchingSha256Record() throws Exception {
        byte[] bytes = "native".getBytes(StandardCharsets.UTF_8);
        String digest = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        assertEquals(digest, LatticeNativeLoader.parseChecksum(
                digest + "  lattice-native-linux-x86_64.so\n", "lattice-native-linux-x86_64.so"));
        LatticeNativeLoader.verifyChecksum(bytes,
                digest + "  lattice-native-linux-x86_64.so\n", "lattice-native-linux-x86_64.so");
    }

    @Test
    void checksumParsingRejectsWrongFilename() {
        String digest = "a".repeat(64);
        assertThrows(IllegalArgumentException.class, () -> LatticeNativeLoader.parseChecksum(
                digest + "  wrong.so\n", "lattice-native-linux-x86_64.so"));
    }

    @Test
    void checksumParsingRejectsNon64Hex() {
        assertThrows(IllegalArgumentException.class, () -> LatticeNativeLoader.parseChecksum(
                "abc  lattice-native-linux-x86_64.so\n", "lattice-native-linux-x86_64.so"));
    }

    @Test
    void checksumParsingRejectsMalformedStructure() {
        assertThrows(IllegalArgumentException.class, () -> LatticeNativeLoader.parseChecksum(
                "a".repeat(64) + " lattice-native-linux-x86_64.so\n", "lattice-native-linux-x86_64.so"));
    }

    @Test
    void checksumVerificationRejectsHashMismatch() {
        byte[] bytes = "native".getBytes(StandardCharsets.UTF_8);
        assertThrows(IOException.class, () -> LatticeNativeLoader.verifyChecksum(bytes,
                "0".repeat(64) + "  lattice-native-linux-x86_64.so\n",
                "lattice-native-linux-x86_64.so"));
    }

    // ---- Download trust boundary (audit finding V1/V5) ---------------------

    @Test
    void downloadUrlRejectsPlainHttpUnlessExplicitlyAllowed() throws Exception {
        assertThrows(IOException.class, () -> LatticeNativeLoader.requireHttpUri(
                "http://github.com/LatticeMC/Lattice/releases/download/native-latest/x.so", false));
        assertEquals("http", LatticeNativeLoader.requireHttpUri(
                "http://mirror.internal/x.so", true).getScheme());
    }

    @Test
    void downloadUrlRejectsNonHttpSchemes() throws Exception {
        assertThrows(IOException.class, () -> LatticeNativeLoader.requireHttpUri("file:///etc/passwd", false));
        assertThrows(IOException.class, () -> LatticeNativeLoader.requireHttpUri("ftp://evil.test/x.so", true));
        assertEquals("https", LatticeNativeLoader.requireHttpUri("https://github.com/x.so", false).getScheme());
    }

    @Test
    void redirectsAreRestrictedToTrustedHosts() throws Exception {
        LatticeNativeLoader.requireAllowedRedirect(URI.create("https://objects.githubusercontent.com/x"), false);
        LatticeNativeLoader.requireAllowedRedirect(URI.create("https://github.com/x"), false);
        assertThrows(IOException.class,
                () -> LatticeNativeLoader.requireAllowedRedirect(URI.create("https://evil.test/x"), false));
        assertThrows(IOException.class,
                () -> LatticeNativeLoader.requireAllowedRedirect(URI.create("http://github.com/x"), false));
    }

    @Test
    void trustedDigestComesFromConfigurationAndRejectsMalformedValues() {
        String previous = System.getProperty("lattice.native.sha256");
        try {
            System.clearProperty("lattice.native.sha256");
            assertEquals(null, LatticeNativeLoader.trustedDigestFor("lattice-native-linux-x86_64.so"));

            System.setProperty("lattice.native.sha256", "AB".repeat(32));
            assertEquals("ab".repeat(32), LatticeNativeLoader.trustedDigestFor("lattice-native-linux-x86_64.so"));

            System.setProperty("lattice.native.sha256", "not-a-digest");
            assertThrows(IllegalArgumentException.class,
                    () -> LatticeNativeLoader.trustedDigestFor("lattice-native-linux-x86_64.so"));
        } finally {
            if (previous == null) System.clearProperty("lattice.native.sha256");
            else System.setProperty("lattice.native.sha256", previous);
        }
    }

    @Test
    void trustedDigestVerificationRejectsMismatch() throws Exception {
        byte[] bytes = "native".getBytes(StandardCharsets.UTF_8);
        String digest = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        LatticeNativeLoader.verifyTrustedDigest(bytes, digest, "lattice-native-linux-x86_64.so");
        assertThrows(IOException.class, () -> LatticeNativeLoader.verifyTrustedDigest(
                bytes, "0".repeat(64), "lattice-native-linux-x86_64.so"));
    }

    // ---- Cache extraction hardening (audit finding V3) ---------------------

    @Test
    void cacheExtractionIgnoresLegacySymlinkAndUsesPrivateDirectory(@TempDir Path tempDir) throws Exception {
        Path cacheDir = tempDir.resolve("cache");
        Path attacker = tempDir.resolve("attacker.so");
        byte[] payload = "trusted-native-bytes".getBytes(StandardCharsets.UTF_8);
        byte[] hostile = "hostile-native-bytes".getBytes(StandardCharsets.UTF_8);
        Files.write(attacker, hostile);

        String digest = HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256").digest(payload));
        Path target = cacheDir.resolve("liblattice.so." + digest.substring(0, 16));
        Files.createDirectories(cacheDir);
        try {
            Files.createSymbolicLink(target, attacker);
        } catch (IOException | UnsupportedOperationException cannotSymlink) {
            return; // e.g. unprivileged Windows; the no-follow check is exercised elsewhere
        }

        String previous = System.getProperty("lattice.native.cacheDir");
        System.setProperty("lattice.native.cacheDir", cacheDir.toString());
        try {
            Path extracted = LatticeNativeLoader.extractToCache("liblattice.so",
                    new ByteArrayInputStream(payload), digest);
            assertTrue(Files.isRegularFile(extracted, LinkOption.NOFOLLOW_LINKS),
                    "extracted entry must be a regular file, not a symlink");
            assertFalse(extracted.equals(target), "legacy cache path must not be reused");
            assertTrue(Files.isSymbolicLink(target), "legacy entry is left untouched");
            assertArrayEquals(payload, Files.readAllBytes(extracted));
            Path second = LatticeNativeLoader.extractToCache("liblattice.so",
                    new ByteArrayInputStream(payload), digest);
            assertFalse(extracted.getParent().equals(second.getParent()),
                    "each extraction must use a fresh private directory");
            assertArrayEquals(hostile, Files.readAllBytes(attacker),
                    "the symlink target must not have been written through");
        } finally {
            if (previous == null) System.clearProperty("lattice.native.cacheDir");
            else System.setProperty("lattice.native.cacheDir", previous);
        }
    }
    @Test
    void pinnedDigestMismatchDoesNotCreateExtractionDirectory(@TempDir Path tempDir) throws Exception {
        String previous = System.getProperty("lattice.native.cacheDir");
        System.setProperty("lattice.native.cacheDir", tempDir.toString());
        try {
            assertThrows(IOException.class, () -> LatticeNativeLoader.extractToCache("liblattice.so",
                    new ByteArrayInputStream("untrusted".getBytes(StandardCharsets.UTF_8)), "0".repeat(64)));
            try (var entries = Files.list(tempDir)) {
                assertEquals(0, entries.count(), "reject untrusted bytes before extraction");
            }
        } finally {
            if (previous == null) System.clearProperty("lattice.native.cacheDir");
            else System.setProperty("lattice.native.cacheDir", previous);
        }
    }

}
