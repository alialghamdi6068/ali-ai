package net.ali.ai.service;

import net.ali.ai.AliAIPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * Bounded server file inspection. Paths are confined to the server root and symbolic links
 * are never followed by the inspection operations.
 */
public final class FileIntelligenceService {
    private static final int MAX_RESULTS = 80;
    private static final int MAX_SEARCH_ENTRIES = 5_000;
    private static final int MAX_READ_BYTES = 16 * 1024;
    private static final int MAX_CONTENT_SEARCH_FILES = 500;
    private static final int MAX_CONTENT_SEARCH_FILE_BYTES = 256 * 1024;
    private static final int MAX_CONTENT_MATCHES = 50;

    private final Path serverRoot;

    public FileIntelligenceService(AliAIPlugin plugin) throws IOException {
        Path pluginFolder = plugin.getDataFolder().toPath().toAbsolutePath().normalize();
        Path root = pluginFolder.getParent() == null ? null : pluginFolder.getParent().getParent();
        if (root == null) {
            throw new IOException("Unable to resolve the Minecraft server directory.");
        }
        this.serverRoot = root.toRealPath();
    }

    FileIntelligenceService(Path serverRoot) throws IOException {
        this.serverRoot = serverRoot.toRealPath();
    }

    public String list(String relativePath) throws IOException {
        Path directory = resolve(relativePath == null || relativePath.isBlank() ? "." : relativePath);
        if (!Files.isDirectory(directory, LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException("The requested path is not a directory.");
        }

        List<String> entries = new ArrayList<>();
        try (Stream<Path> children = Files.list(directory)) {
            for (Path child : children.sorted().limit(MAX_RESULTS).toList()) {
                if (Files.isSymbolicLink(child)) {
                    entries.add("[LINK BLOCKED] " + serverRoot.relativize(child));
                } else {
                    String type = Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS) ? "[DIR] " : "[FILE] ";
                    entries.add(type + serverRoot.relativize(child));
                }
            }
        }
        return entries.isEmpty() ? "Directory is empty." : String.join("\n", entries);
    }

    public String read(String relativePath) throws IOException {
        Path file = resolve(relativePath);
        if (!Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException("The requested path is not a regular file.");
        }

        // Read at most the preview limit plus one byte; do not trust a prior size check.
        byte[] bytes;
        try (InputStream input = Files.newInputStream(file, LinkOption.NOFOLLOW_LINKS)) {
            bytes = input.readNBytes(MAX_READ_BYTES + 1);
        }
        if (bytes.length > MAX_READ_BYTES) {
            throw new IOException("File is larger than the safe preview limit (16 KiB).");
        }
        rejectBinary(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public String search(String query) throws IOException {
        if (query == null || query.isBlank()) {
            throw new IOException("Enter a file-name search term.");
        }

        String needle = query.toLowerCase(Locale.ROOT);
        List<String> matches = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(serverRoot, 8)) {
            for (Path path : paths.limit(MAX_SEARCH_ENTRIES).toList()) {
                if (path.equals(serverRoot) || Files.isSymbolicLink(path)) {
                    continue;
                }
                Path name = path.getFileName();
                if (name != null && name.toString().toLowerCase(Locale.ROOT).contains(needle)) {
                    matches.add(serverRoot.relativize(path) +
                            (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS) ? "/" : ""));
                    if (matches.size() >= MAX_RESULTS) {
                        break;
                    }
                }
            }
        }
        return matches.isEmpty() ? "No matching file names were found." : String.join("\n", matches);
    }

    /**
     * Searches a bounded number of small text-like files. Large files and binary files are skipped.
     * This is intended for targeted diagnostics, not a full-text index of the server.
     */
    public String searchContent(String query) throws IOException {
        if (query == null || query.isBlank()) {
            throw new IOException("Enter text to search for.");
        }

        String needle = query.toLowerCase(Locale.ROOT);
        List<String> matches = new ArrayList<>();
        int scanned = 0;
        try (Stream<Path> paths = Files.walk(serverRoot, 8)) {
            for (Path path : paths.limit(MAX_SEARCH_ENTRIES).toList()) {
                if (matches.size() >= MAX_CONTENT_MATCHES || scanned >= MAX_CONTENT_SEARCH_FILES) {
                    break;
                }
                if (path.equals(serverRoot) || Files.isSymbolicLink(path)
                        || !Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) {
                    continue;
                }

                scanned++;
                byte[] bytes;
                try (InputStream input = Files.newInputStream(path, LinkOption.NOFOLLOW_LINKS)) {
                    bytes = input.readNBytes(MAX_CONTENT_SEARCH_FILE_BYTES + 1);
                } catch (IOException ignored) {
                    continue;
                }
                if (bytes.length > MAX_CONTENT_SEARCH_FILE_BYTES || containsNullByte(bytes)) {
                    continue;
                }

                String content = new String(bytes, StandardCharsets.UTF_8);
                String[] lines = content.split("\\R", -1);
                for (int line = 0; line < lines.length; line++) {
                    if (lines[line].toLowerCase(Locale.ROOT).contains(needle)) {
                        String preview = lines[line].strip();
                        if (preview.length() > 180) {
                            preview = preview.substring(0, 177) + "...";
                        }
                        matches.add(serverRoot.relativize(path) + ":" + (line + 1) + ": " + preview);
                        if (matches.size() >= MAX_CONTENT_MATCHES) {
                            break;
                        }
                    }
                }
            }
        }

        if (matches.isEmpty()) {
            return "No matches found in the bounded text-file scan (" + scanned + " files inspected).";
        }
        return "Content matches (scanned " + scanned + " files; bounded search):\n" + String.join("\n", matches);
    }

    private Path resolve(String relativePath) throws IOException {
        if (relativePath == null || relativePath.isBlank()) {
            throw new IOException("A relative path is required.");
        }

        Path supplied = Path.of(relativePath);
        if (supplied.isAbsolute()) {
            throw new IOException("Absolute paths are not allowed.");
        }

        Path target = serverRoot.resolve(supplied).normalize();
        if (!target.startsWith(serverRoot)) {
            throw new IOException("Path is outside the Minecraft server directory.");
        }

        Path current = serverRoot;
        for (Path part : serverRoot.relativize(target)) {
            current = current.resolve(part);
            if (Files.isSymbolicLink(current)) {
                throw new IOException("Symbolic links are not allowed.");
            }
        }
        if (!Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException("The requested path does not exist.");
        }
        return target;
    }

    private void rejectBinary(byte[] bytes) throws IOException {
        if (containsNullByte(bytes)) {
            throw new IOException("Binary files cannot be displayed as text.");
        }
    }

    private boolean containsNullByte(byte[] bytes) {
        for (byte value : bytes) {
            if (value == 0) {
                return true;
            }
        }
        return false;
    }
}
