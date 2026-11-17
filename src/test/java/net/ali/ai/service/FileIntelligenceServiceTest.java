package net.ali.ai.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileIntelligenceServiceTest {
    @TempDir
    Path serverRoot;

    @Test
    void rejectsPathsOutsideServerRoot() throws IOException {
        FileIntelligenceService service = new FileIntelligenceService(serverRoot);
        assertThrows(IOException.class, () -> service.read("../outside.txt"));
    }

    @Test
    void boundsFilePreviewWithoutReadingTheWholeFile() throws IOException {
        Files.write(serverRoot.resolve("large.txt"), new byte[16 * 1024 + 1]);
        FileIntelligenceService service = new FileIntelligenceService(serverRoot);
        assertThrows(IOException.class, () -> service.read("large.txt"));
    }

    @Test
    void readsOnlyTheRequestedByteRange() throws IOException {
        Files.writeString(serverRoot.resolve("range.txt"), "0123456789");
        FileIntelligenceService service = new FileIntelligenceService(serverRoot);

        assertEquals("3456", service.readRange("range.txt", 3, 4));
    }

    @Test
    void rejectsInvalidRangeOffsetsAndLimits() throws IOException {
        Files.writeString(serverRoot.resolve("range.txt"), "0123456789");
        FileIntelligenceService service = new FileIntelligenceService(serverRoot);

        assertThrows(IOException.class, () -> service.readRange("range.txt", -1, 4));
        assertThrows(IOException.class, () -> service.readRange("range.txt", 11, 4));
        assertThrows(IOException.class, () -> service.readRange("range.txt", 0, 65 * 1024));
    }

    @Test
    void searchesTextFilesAndReportsLineNumbers() throws IOException {
        Path log = serverRoot.resolve("logs/latest.log");
        Files.createDirectories(log.getParent());
        Files.writeString(log, "server started\nERROR unable to load plugin\nserver stopped\n");

        FileIntelligenceService service = new FileIntelligenceService(serverRoot);
        String result = service.searchContent("error");

        assertTrue(result.contains("logs/latest.log:2: ERROR unable to load plugin"));
    }

    @Test
    void refusesSymbolicLinks() throws IOException {
        Path outside = Files.createTempFile("aliai-outside", ".txt");
        Files.writeString(outside, "must not be read");
        Path link = serverRoot.resolve("outside-link.txt");
        try {
            Files.createSymbolicLink(link, outside);
        } catch (UnsupportedOperationException | SecurityException exception) {
            return;
        }

        FileIntelligenceService service = new FileIntelligenceService(serverRoot);
        assertThrows(IOException.class, () -> service.read("outside-link.txt"));
    }
}
