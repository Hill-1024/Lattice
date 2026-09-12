package com.latticemc.lattice.nativelib;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class NativeRegionFileReadTestSuite {
    @Test
    void readsOnlyTheRequestedRangeAndPreservesEofAndCloseSemantics(@TempDir Path directory)
            throws IOException {
        byte[] source = new byte[8 * 1024];
        for (int index = 0; index < source.length; ++index) {
            source[index] = (byte) index;
        }
        Path file = directory.resolve("region.mca");
        Files.write(file, source);

        NativeRegionFileRead reader = NativeRegionFileRead.open(file);
        if (reader == null) {
            // A missing native artifact is an expected test environment: callers retain Java I/O.
            assertFalse(LatticeNative.isLoaded(), "a loaded native library must open this readable file");
            return;
        }

        byte[] destination = new byte[128];
        Arrays.fill(destination, (byte) 0x5A);
        assertEquals(64, reader.readAt(destination, 16, 64, 512));
        assertArrayEquals(Arrays.copyOfRange(source, 512, 576),
                Arrays.copyOfRange(destination, 16, 80));
        for (int index = 0; index < 16; ++index) {
            assertEquals((byte) 0x5A, destination[index]);
        }
        for (int index = 80; index < destination.length; ++index) {
            assertEquals((byte) 0x5A, destination[index]);
        }

        Arrays.fill(destination, (byte) 0x22);
        assertEquals(-1, reader.readAt(destination, 24, 32, source.length));
        for (byte value : destination) {
            assertEquals((byte) 0x22, value);
        }

        reader.close();
        reader.close();
        assertThrows(IOException.class, () -> reader.readAt(destination, 0, 1, 0));
    }

    @Test
    void openRejectsPathsOutsideTheAllowedRoot(@TempDir Path directory) throws IOException {
        Path root = directory.resolve("world");
        Files.createDirectories(root);
        Files.write(root.resolve("region.mca"), new byte[16]);

        Path outside = directory.resolve("secret.txt");
        Files.write(outside, new byte[16]);

        // Path containment is decided before any native call, so this must hold
        // whether or not the native library is loaded (audit finding V13).
        assertNull(NativeRegionFileRead.open(outside, root));
    }

    @Test
    void openRejectsUnresolvablePath(@TempDir Path directory) {
        assertNull(NativeRegionFileRead.open(directory.resolve("missing.mca"), directory));
    }
}
