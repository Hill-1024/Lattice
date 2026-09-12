package com.latticemc.lattice.bootstrap;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryFlag;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Keeps extracted executable code private until System.load has consumed it. */
final class NativeLibraryCache {
    private NativeLibraryCache() {}

    static Path extract(Path parent, String libFile, InputStream in) throws IOException {
        if (libFile.isEmpty() || libFile.equals(".") || libFile.equals("..") || !Path.of(libFile).getFileName().toString().equals(libFile)) {
            throw new IOException("Native library name must be a filename");
        }
        final var lookup = parent.getFileSystem().getUserPrincipalLookupService();
        final UserPrincipal user = lookup.lookupPrincipalByName(System.getProperty("user.name"));
        // Resolve normal OS aliases such as macOS /var before validating the
        // entire path. A private child cannot protect against its parent owner.
        final Path absolute = parent.toAbsolutePath().normalize();
        Path existing = absolute;
        while (!Files.exists(existing, LinkOption.NOFOLLOW_LINKS)) existing = existing.getParent();
        validateAncestors(existing.toRealPath(), user);
        Files.createDirectories(absolute);
        final Path resolved = absolute.toRealPath();
        validateAncestors(resolved, user);
        final FileAttribute<?> permissions;
        if (resolved.getFileSystem().supportedFileAttributeViews().contains("posix")) {
            permissions = PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwx------"));
        } else if (resolved.getFileSystem().supportedFileAttributeViews().contains("acl")) {
            final List<AclEntry> acl = List.of(AclEntry.newBuilder()
                    .setType(AclEntryType.ALLOW).setPrincipal(user)
                    .setPermissions(EnumSet.allOf(AclEntryPermission.class))
                    .setFlags(AclEntryFlag.DIRECTORY_INHERIT, AclEntryFlag.FILE_INHERIT).build());
            permissions = new FileAttribute<List<AclEntry>>() {
                public String name() { return "acl:acl"; }
                public List<AclEntry> value() { return acl; }
            };
        } else {
            throw new IOException("Native extraction requires POSIX permissions or ACLs");
        }
        final Path directory = Files.createTempDirectory(resolved, "lattice-native-", permissions);
        final Path target = directory.resolve(libFile);
        try {
            if (resolved.getFileSystem().supportedFileAttributeViews().contains("acl")) {
                // Windows may merge inherited ACEs with the supplied initial ACL.
                // Check the resulting directory before writing any executable bytes.
                final var trusted = trustedWindowsPrincipals(resolved, user);
                for (AclEntry entry : Files.getFileAttributeView(directory, AclFileAttributeView.class).getAcl()) {
                    if (entry.type() == AclEntryType.ALLOW && !trusted.contains(entry.principal())
                            && entry.permissions().stream().anyMatch(permission ->
                                    permission == AclEntryPermission.WRITE_DATA
                                    || permission == AclEntryPermission.APPEND_DATA
                                    || permission == AclEntryPermission.DELETE
                                    || permission == AclEntryPermission.DELETE_CHILD
                                    || permission == AclEntryPermission.WRITE_ACL
                                    || permission == AclEntryPermission.WRITE_OWNER)) {
                        throw new IOException("Unsafe inherited native extraction ACL: " + directory);
                    }
                }
            }
            // No reuse, symlink following, or replace-existing move. The private
            // directory protects both the write and the later pathname-based load.
            Files.copy(in, target);
            // Reverse registration order deletes the file before its directory.
            directory.toFile().deleteOnExit();
            target.toFile().deleteOnExit();
            return target;
        } catch (IOException | RuntimeException failure) {
            try {
                Files.deleteIfExists(target);
                Files.deleteIfExists(directory);
            } catch (IOException cleanup) {
                failure.addSuppressed(cleanup);
            }
            throw failure;
        }
    }

    // Darwin's NIO provider exposes POSIX mode bits, but not its extended ACLs.
    // Unlike Linux ACL masks, Darwin ALLOW entries can grant access beyond 0700.
    // Use the system ACL listing and fail closed when it cannot be inspected.
    private static void validateMacAcl(Path directory) throws IOException {
        final ProcessBuilder builder = new ProcessBuilder("/bin/ls", "-lde", directory.toString());
        builder.environment().put("LC_ALL", "C");
        final Process process = builder.redirectErrorStream(true).start();
        final String output;
        try {
            output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            if (process.waitFor() != 0) throw new IOException("Cannot inspect native extraction ACL: " + directory);
        } catch (InterruptedException interrupted) {
            process.destroyForcibly();
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while checking native extraction ACL", interrupted);
        }
        for (String line : output.lines().skip(1).toList()) {
            if (!line.matches("\\s*\\d+: .* (allow|deny) .*")) {
                throw new IOException("Unrecognized native extraction ACL: " + directory);
            }
            if (line.contains(" allow ")) {
                String permissions = line.substring(line.indexOf(" allow ") + 7);
                for (String permission : permissions.split(",")) {
                    switch (permission.trim()) {
                        case "read", "list", "search", "execute", "readattr", "readextattr", "readsecurity",
                                "file_inherit", "directory_inherit", "limit_inherit", "only_inherit", "inherited":
                            break;
                        default:
                            throw new IOException("Writable native extraction ACL: " + directory);
                    }
                }
            }
        }
    }

    private static Set<UserPrincipal> trustedWindowsPrincipals(Path path, UserPrincipal user) throws IOException {
        final var lookup = path.getFileSystem().getUserPrincipalLookupService();
        return new HashSet<>(List.of(user,
                lookup.lookupPrincipalByName("NT AUTHORITY\\SYSTEM"),
                lookup.lookupPrincipalByName("BUILTIN\\Administrators"),
                Files.getOwner(path.getRoot())));
    }

    static void validateAncestors(Path path, UserPrincipal user) throws IOException {
        final Set<String> views = path.getFileSystem().supportedFileAttributeViews();
        final List<Path> ancestors = new ArrayList<>();
        for (Path directory = path; directory != null; directory = directory.getParent()) ancestors.add(directory);
        Collections.reverse(ancestors);
        // Validate each parent before relying on the identity of its child.

        if (views.contains("posix") && views.contains("unix")) {
            final boolean mac = System.getProperty("os.name", "").toLowerCase(java.util.Locale.ROOT).contains("mac");
            for (Path directory : ancestors) {
                if (mac) validateMacAcl(directory);
                final int uid = (int) Files.getAttribute(directory, "unix:uid", LinkOption.NOFOLLOW_LINKS);
                final int mode = (int) Files.getAttribute(directory, "unix:mode", LinkOption.NOFOLLOW_LINKS);
                if (!Files.isDirectory(directory, LinkOption.NOFOLLOW_LINKS)
                        || (uid != 0 && !Files.getOwner(directory, LinkOption.NOFOLLOW_LINKS).equals(user))
                        || ((mode & 0022) != 0 && (mode & 01000) == 0)) {
                    throw new IOException("Unsafe native extraction ancestor: " + directory);
                }
            }
            return;
        }
        if (views.contains("acl")) {
            final Set<UserPrincipal> trusted = trustedWindowsPrincipals(path, user);
            for (Path directory : ancestors) {
                final var view = Files.getFileAttributeView(directory, AclFileAttributeView.class,
                        LinkOption.NOFOLLOW_LINKS);
                if (!Files.isDirectory(directory, LinkOption.NOFOLLOW_LINKS)
                        || view == null || !trusted.contains(view.getOwner())) {
                    throw new IOException("Unsafe native extraction ancestor: " + directory);
                }
                for (AclEntry entry : view.getAcl()) {
                    if (entry.type() == AclEntryType.ALLOW && !trusted.contains(entry.principal())
                            && !entry.flags().contains(AclEntryFlag.INHERIT_ONLY)
                            && entry.permissions().stream().anyMatch(permission ->
                                    permission == AclEntryPermission.DELETE
                                    || permission == AclEntryPermission.DELETE_CHILD
                                    || permission == AclEntryPermission.WRITE_ACL
                                    || permission == AclEntryPermission.WRITE_OWNER)) {
                        throw new IOException("Writable native extraction ancestor: " + directory);
                    }
                }
            }
            return;
        }
        throw new IOException("Cannot verify native extraction directory permissions");
    }
}
