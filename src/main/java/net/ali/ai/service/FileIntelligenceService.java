package net.ali.ai.service;

import net.ali.ai.AliAIPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * Bounded, read-only server file inspection. Paths are confined to the server root and symbolic links
 * are rejected so inspection cannot escape through a link.
 */
public final class FileIntelligenceService {
    private static final int MAX_RESULTS = 80;
    private static final int MAX_READ_BYTES = 16 * 1024;
    private final Path serverRoot;

    public FileIntelligenceService(AliAIPlugin plugin) throws IOException {
        Path pluginFolder = plugin.getDataFolder().toPath().toAbsolutePath().normalize();
        Path root = pluginFolder.getParent() == null ? null : pluginFolder.getParent().getParent();
        if (root == null) throw new IOException("Unable to resolve the Minecraft server directory.");
        this.serverRoot = root.toRealPath();
    }

    public String list(String relativePath) throws IOException {
        Path directory = resolve(relativePath == null || relativePath.isBlank() ? "." : relativePath);
        if (!Files.isDirectory(directory)) throw new IOException("The requested path is not a directory.");
        List<String> entries = new ArrayList<>();
        try (Stream<Path> children = Files.list(directory)) {
            children.sorted().limit(MAX_RESULTS).forEach(path -> {
                String type = Files.isDirectory(path) ? "[DIR] " : "[FILE] ";
                entries.add(type + serverRoot.relativize(path));
            });
        }
        return entries.isEmpty() ? "Directory is empty." : String.join("\n", entries);
    }

    public String read(String relativePath) throws IOException {
        Path file = resolve(relativePath);
        if (!Files.isRegularFile(file)) throw new IOException("The requested path is not a regular file.");
        long size = Files.size(file);
        if (size > MAX_READ_BYTES) {
            throw new IOException("File is larger than the safe preview limit (16 KiB). Use a narrower file or search.");
        }
        byte[] bytes = Files.readAllBytes(file);
        for (byte value : bytes) {
            if (value == 0) throw new IOException("Binary files cannot be displayed as text.");
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public String search(String query) throws IOException {
        if (query == null || query.isBlank()) throw new IOException("Enter a file-name search term.");
        String needle = query.toLowerCase(Locale.ROOT);
        List<String> matches = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(serverRoot, 8)) {
            paths.filter(path -> !path.equals(serverRoot))
                    .filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).contains(needle))
                    .limit(MAX_RESULTS)
                    .forEach(path -> matches.add(serverRoot.relativize(path).toString()
                            + (Files.isDirectory(path) ? "/" : "")));
        }
        return matches.isEmpty() ? "No matching file names were found." : String.join("\n", matches);
    }

    private Path resolve(String relativePath) throws IOException {
        if (relativePath == null || relativePath.isBlank()) throw new IOException("A relative path is required.");
        Path supplied = Path.of(relativePath);
        if (supplied.isAbsolute()) throw new IOException("Absolute paths are not allowed.");
        Path target = serverRoot.resolve(supplied).normalize();
        if (!target.startsWith(serverRoot)) throw new IOException("Path is outside the Minecraft server directory.");

        Path current = serverRoot;
        for (Path part : serverRoot.relativize(target)) {
            current = current.resolve(part);
            if (Files.isSymbolicLink(current)) throw new IOException("Symbolic links are not allowed.");
        }
        if (!Files.exists(target)) throw new IOException("The requested path does not exist.");
        return target;
    }
}
