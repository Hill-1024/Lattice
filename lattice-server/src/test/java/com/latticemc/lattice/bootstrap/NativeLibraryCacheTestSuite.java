package com.latticemc.lattice.bootstrap;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class NativeLibraryCacheTestSuite {
    @TempDir Path temporary;
    private static final byte[] CONTENT = {1, 2, 3, 4};

    private Path extract(Path parent) throws IOException {
        return NativeLibraryCache.extract(parent, "native.bin", new ByteArrayInputStream(CONTENT));
    }

    @Test
    void freshExtractionsIgnorePoisonedLegacyCacheAndPreserveContent() throws Exception {
        Path legacy = Files.createDirectory(temporary.resolve("lattice-native"));
        Files.write(legacy.resolve("native.bin.9f64a747e1b97f13"), new byte[] {9, 9, 9, 9});
        Path first = extract(temporary);
        Path second = extract(temporary);
        assertNotEquals(first.getParent(), second.getParent());
        assertArrayEquals(CONTENT, Files.readAllBytes(first));
        assertArrayEquals(CONTENT, Files.readAllBytes(second));
        if (first.getFileSystem().supportedFileAttributeViews().contains("posix")) {
            assertEquals(PosixFilePermissions.fromString("rwx------"), Files.getPosixFilePermissions(first.getParent()));
        }
    }

    @Test
    void createsMissingOverrideParents() throws Exception {
        Path target = extract(temporary.resolve("missing/cache"));
        assertArrayEquals(CONTENT, Files.readAllBytes(target));
    }

    @Test
    void rejectsWritableParentAndWritableAncestor() throws Exception {
        assumeTrue(temporary.getFileSystem().supportedFileAttributeViews().contains("posix"));
        Path unsafe = Files.createDirectory(temporary.resolve("unsafe"));
        Path child = Files.createDirectory(unsafe.resolve("private"));
        Files.setPosixFilePermissions(unsafe, PosixFilePermissions.fromString("rwxrwxrwx"));
        assertThrows(IOException.class, () -> extract(unsafe));
        assertThrows(IOException.class, () -> extract(child));
        assertThrows(IOException.class, () -> extract(unsafe.resolve("new/cache")));
        assertFalse(Files.exists(unsafe.resolve("new")));
    }

    @Test
    void acceptsTrustedStickyTemporaryParent() throws Exception {
        assumeTrue(temporary.getFileSystem().supportedFileAttributeViews().contains("unix"));
        Files.setAttribute(temporary, "unix:mode", 01777);
        assertArrayEquals(CONTENT, Files.readAllBytes(extract(temporary)));
    }

    @Test
    void resolvesSafeAliasesButRejectsAliasesToUnsafeDirectories() throws Exception {
        assumeTrue(temporary.getFileSystem().supportedFileAttributeViews().contains("posix"));
        Path safe = Files.createDirectory(temporary.resolve("safe"));
        Path alias = Files.createSymbolicLink(temporary.resolve("alias"), safe);
        assertArrayEquals(CONTENT, Files.readAllBytes(extract(alias)));
        Files.setPosixFilePermissions(safe, PosixFilePermissions.fromString("rwxrwxrwx"));
        assertThrows(IOException.class, () -> extract(alias));
    }

    @Test
    void failedExtractionRemovesPartialFileAndDirectory() throws Exception {
        InputStream failing = new InputStream() {
            public int read() throws IOException { throw new IOException("test read failure"); }
        };
        assertThrows(IOException.class, () -> NativeLibraryCache.extract(temporary, "native.bin", failing));
        try (var children = Files.list(temporary)) { assertEquals(0, children.count()); }
    }

    @Test
    void rejectsMacInheritedAclGrantsBeyondPosixMode() throws Exception {
        assumeTrue(System.getProperty("os.name").contains("Mac"));
        Process chmod = new ProcessBuilder("/bin/chmod", "+a",
                "user:nobody allow list,search,add_file,delete_child,file_inherit,directory_inherit",
                temporary.toString()).start();
        assertEquals(0, chmod.waitFor());
        try {
            assertThrows(IOException.class, () -> extract(temporary));
            assertThrows(IOException.class, () -> extract(temporary.resolve("new/cache")));
        } finally {
            assertEquals(0, new ProcessBuilder("/bin/chmod", "-N", temporary.toString()).start().waitFor());
        }
    }

    @Test
    void rejectsForeignOwnerEvenWhenModeIsPrivate() throws Exception {
        assumeTrue(temporary.getFileSystem().supportedFileAttributeViews().contains("posix"));
        var otherUser = temporary.getFileSystem().getUserPrincipalLookupService().lookupPrincipalByName("nobody");
        assertThrows(IOException.class, () -> NativeLibraryCache.validateAncestors(temporary.toRealPath(), otherUser));
    }

    @Test
    void rejectsUntrustedWindowsDeleteGrants() throws Exception {
        assumeTrue(temporary.getFileSystem().supportedFileAttributeViews().contains("acl"));
        var view = Files.getFileAttributeView(temporary, java.nio.file.attribute.AclFileAttributeView.class);
        var acl = new java.util.ArrayList<>(view.getAcl());
        var everyone = temporary.getFileSystem().getUserPrincipalLookupService().lookupPrincipalByName("Everyone");
        acl.add(java.nio.file.attribute.AclEntry.newBuilder()
                .setType(java.nio.file.attribute.AclEntryType.ALLOW).setPrincipal(everyone)
                .setPermissions(java.nio.file.attribute.AclEntryPermission.DELETE_CHILD).build());
        view.setAcl(acl);
        assertThrows(IOException.class, () -> extract(temporary));
    }

    @Test
    void rejectsInheritedWindowsFileWriteGrants() throws Exception {
        assumeTrue(temporary.getFileSystem().supportedFileAttributeViews().contains("acl"));
        var view = Files.getFileAttributeView(temporary, java.nio.file.attribute.AclFileAttributeView.class);
        var acl = new java.util.ArrayList<>(view.getAcl());
        var everyone = temporary.getFileSystem().getUserPrincipalLookupService().lookupPrincipalByName("Everyone");
        acl.add(java.nio.file.attribute.AclEntry.newBuilder()
                .setType(java.nio.file.attribute.AclEntryType.ALLOW).setPrincipal(everyone)
                .setPermissions(java.nio.file.attribute.AclEntryPermission.WRITE_DATA)
                .setFlags(java.nio.file.attribute.AclEntryFlag.FILE_INHERIT,
                        java.nio.file.attribute.AclEntryFlag.DIRECTORY_INHERIT,
                        java.nio.file.attribute.AclEntryFlag.INHERIT_ONLY).build());
        view.setAcl(acl);
        assertThrows(IOException.class, () -> extract(temporary));
    }

    @Test
    void nativeLibraryLoadsFromPrivateExtraction() throws Exception {
        String library = System.getProperty("lattice.test.nativeLibrary");
        assumeTrue(library != null);
        try (InputStream in = Files.newInputStream(Path.of(library))) {
            Path extracted = NativeLibraryCache.extract(temporary, Path.of(library).getFileName().toString(), in);
            assertDoesNotThrow(() -> System.load(extracted.toString()));
        }
    }

    @Test
    void rejectsPathComponentsInLibraryName() {
        assertThrows(IOException.class, () -> NativeLibraryCache.extract(temporary, "../escape", new ByteArrayInputStream(CONTENT)));
    }
}
