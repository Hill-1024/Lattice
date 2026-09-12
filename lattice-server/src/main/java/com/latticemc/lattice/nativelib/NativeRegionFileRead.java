package com.latticemc.lattice.nativelib;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;

/**
 * Experimental synchronous positioned reads for internal RegionFile payloads.
 *
 * <p>This is intentionally not an IOCP, io_uring, or kqueue backend. It owns only a separate
 * read-only operating-system handle; Java still owns RegionFile synchronization, writes, force,
 * and close sequencing.</p>
 */
public final class NativeRegionFileRead implements AutoCloseable {
    private long handle;

    private NativeRegionFileRead(long handle) {
        this.handle = handle;
    }

    /**
     * Opens a read-only native handle, or returns {@code null} when native reads cannot be used.
     * Construction failures deliberately retain the caller's Java I/O path.
     */
    public static NativeRegionFileRead open(Path path) {
        return open(path, null);
    }

    /**
     * Opens a read-only native handle constrained to {@code allowedRoot}. The path is resolved with
     * {@link Path#toRealPath()} first, so symlinks and {@code ..} cannot escape the root; a path
     * outside the root, or one that cannot be resolved, returns {@code null} rather than being
     * opened. Pass {@code null} as {@code allowedRoot} only for an internal caller that already
     * knows the path is a server-owned RegionFile.
     */
    public static NativeRegionFileRead open(Path path, Path allowedRoot) {
        Objects.requireNonNull(path, "path");
        if (!isSupportedPlatform()) {
            return null;
        }
        final Path canonical;
        try {
            canonical = path.toRealPath();
        } catch (IOException unresolved) {
            return null;
        }
        if (allowedRoot != null) {
            final Path canonicalRoot;
            try {
                canonicalRoot = allowedRoot.toRealPath();
            } catch (IOException unresolvedRoot) {
                return null;
            }
            if (!canonical.startsWith(canonicalRoot)) {
                return null;
            }
        }
        LatticeNative.ensureLoaded();
        if (!LatticeNative.isLoaded()) {
            return null;
        }
        try {
            long handle = nativeOpen(canonical.toString());
            return handle == 0L ? null : new NativeRegionFileRead(handle);
        } catch (IOException | UnsatisfiedLinkError exception) {
            return null;
        }
    }

    /**
     * Performs one synchronous positioned read. It follows {@code FileChannel.read}: {@code -1}
     * means EOF, and only {@code destination[offset, offset + length)} can be modified.
     */
    public synchronized int readAt(byte[] destination, int offset, int length, long position)
            throws IOException {
        Objects.requireNonNull(destination, "destination");
        Objects.checkFromIndexSize(offset, length, destination.length);
        if (position < 0L) {
            throw new IllegalArgumentException("position must not be negative");
        }
        if (this.handle == 0L) {
            throw new IOException("native RegionFile handle is closed");
        }
        if (length == 0) {
            return 0;
        }
        return nativeReadAt(this.handle, destination, offset, length, position);
    }

    @Override
    public synchronized void close() throws IOException {
        long handle = this.handle;
        this.handle = 0L;
        if (handle != 0L) {
            nativeClose(handle);
        }
    }

    private static boolean isSupportedPlatform() {
        String osName = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        return osName.startsWith("windows") || osName.contains("linux")
                || osName.contains("mac") || osName.contains("darwin");
    }

    private static native long nativeOpen(String path) throws IOException;

    private static native int nativeReadAt(long handle, byte[] destination, int offset, int length,
                                           long position) throws IOException;

    private static native void nativeClose(long handle) throws IOException;
}
