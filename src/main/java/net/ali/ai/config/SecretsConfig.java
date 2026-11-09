package net.ali.ai.config;

import net.ali.ai.AliAIPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Loads provider credentials from the server-local secrets.yml file.
 * Credentials are never copied into config.yml or written to the plugin log.
 */
public final class SecretsConfig {
    private static final int GEMINI_KEY_LIMIT = 3;
    private static final int OPENAI_KEY_LIMIT = 2;
    private static final int GROQ_KEY_LIMIT = 2;

    private final AliAIPlugin plugin;
    private YamlConfiguration secrets;

    public SecretsConfig(AliAIPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        File file = new File(plugin.getDataFolder(), "secrets.yml");
        if (!file.exists()) {
            plugin.saveResource("secrets.yml", false);
        }

        restrictFilePermissions(file);
        secrets = YamlConfiguration.loadConfiguration(file);
    }

    public List<String> keysFor(String provider) {
        if (secrets == null || provider == null) {
            return List.of();
        }

        String name = provider.toLowerCase(java.util.Locale.ROOT);
        int limit = switch (name) {
            case "gemini" -> GEMINI_KEY_LIMIT;
            case "openai" -> OPENAI_KEY_LIMIT;
            case "groq" -> GROQ_KEY_LIMIT;
            default -> 0;
        };
        if (limit == 0) {
            return List.of();
        }

        Set<String> uniqueKeys = new LinkedHashSet<>();
        for (String key : secrets.getStringList(name + ".api-keys")) {
            if (key != null && !key.isBlank()) {
                uniqueKeys.add(key.trim());
            }
            if (uniqueKeys.size() == limit) {
                break;
            }
        }
        return List.copyOf(new ArrayList<>(uniqueKeys));
    }

    public boolean hasAnyKey() {
        return !keysFor("gemini").isEmpty()
                || !keysFor("openai").isEmpty()
                || !keysFor("groq").isEmpty();
    }

    private void restrictFilePermissions(File file) {
        try {
            Set<PosixFilePermission> permissions = EnumSet.of(
                    PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_WRITE
            );
            Files.setPosixFilePermissions(file.toPath(), permissions);
        } catch (UnsupportedOperationException ignored) {
            // The filesystem does not support POSIX permissions.
        } catch (IOException exception) {
            plugin.getLogger().warning("Could not restrict permissions on secrets.yml. Check the file permissions manually.");
        }
    }
}
