package net.ali.ai.service;

import net.ali.ai.AliAIPlugin;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.management.ManagementFactory;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/** Collects live facts from the running Paper process. All Bukkit access must run on the server thread. */
public final class ServerIntelligenceService {
    private final AliAIPlugin plugin;

    public ServerIntelligenceService(AliAIPlugin plugin) {
        this.plugin = plugin;
    }

    public String serverStatus() {
        requirePrimaryThread();
        Runtime runtime = Runtime.getRuntime();
        long used = runtime.totalMemory() - runtime.freeMemory();
        double[] tps = Bukkit.getTPS();
        String tpsText = tps.length == 0 ? "unavailable" : String.format(Locale.ROOT, "%.2f", tps[0]);
        String worlds = Bukkit.getWorlds().stream().map(World::getName).sorted()
                .collect(Collectors.joining(", "));
        return """
                Server status
                Minecraft/Paper: %s
                Java: %s
                Online players: %d/%d
                Loaded worlds: %d (%s)
                TPS (1m): %s
                JVM memory: %s used / %s max
                JVM uptime: %d minutes
                """.formatted(
                Bukkit.getMinecraftVersion(),
                System.getProperty("java.version", "unknown"),
                Bukkit.getOnlinePlayers().size(),
                Bukkit.getMaxPlayers(),
                Bukkit.getWorlds().size(),
                worlds.isBlank() ? "none" : worlds,
                tpsText,
                formatBytes(used),
                formatBytes(runtime.maxMemory()),
                ManagementFactory.getRuntimeMXBean().getUptime() / 60_000L
        );
    }

    public String performance() {
        requirePrimaryThread();
        double[] tps = Bukkit.getTPS();
        Runtime runtime = Runtime.getRuntime();
        long used = runtime.totalMemory() - runtime.freeMemory();
        return """
                Performance snapshot
                TPS: 1m %s | 5m %s | 15m %s
                Average tick time (MSPT): %.2f ms
                JVM heap: %s used / %s committed / %s max
                """.formatted(
                tps.length > 0 ? String.format(Locale.ROOT, "%.2f", tps[0]) : "unavailable",
                tps.length > 1 ? String.format(Locale.ROOT, "%.2f", tps[1]) : "unavailable",
                tps.length > 2 ? String.format(Locale.ROOT, "%.2f", tps[2]) : "unavailable",
                Bukkit.getAverageTickTime(),
                formatBytes(used),
                formatBytes(runtime.totalMemory()),
                formatBytes(runtime.maxMemory())
        );
    }

    public String onlinePlayers() {
        requirePrimaryThread();
        List<? extends Player> players = Bukkit.getOnlinePlayers().stream()
                .sorted(Comparator.comparing(Player::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
        if (players.isEmpty()) return "No players are currently online.";
        StringBuilder result = new StringBuilder("Online players (").append(players.size()).append("):\n");
        for (Player player : players) {
            result.append("- ").append(player.getName())
                    .append(" | ").append(player.getUniqueId())
                    .append(" | ping ").append(player.getPing()).append(" ms")
                    .append(" | ").append(player.getWorld().getName())
                    .append(" | ").append(player.getGameMode())
                    .append(player.isOp() ? " | OP" : "")
                    .append('\n');
        }
        return result.toString();
    }

    public String worlds() {
        requirePrimaryThread();
        StringBuilder result = new StringBuilder("Loaded worlds (").append(Bukkit.getWorlds().size()).append("):\n");
        for (World world : Bukkit.getWorlds()) {
            result.append("- ").append(world.getName())
                    .append(" | ").append(world.getEnvironment())
                    .append(" | ").append(world.getDifficulty())
                    .append(" | time ").append(world.getTime())
                    .append(" | ").append(world.hasStorm() ? "storm" : "clear")
                    .append(" | ").append(world.getPlayers().size()).append(" players\n");
        }
        return result.toString();
    }

    public String plugins() {
        requirePrimaryThread();
        return java.util.Arrays.stream(Bukkit.getPluginManager().getPlugins())
                .sorted(Comparator.comparing(Plugin::getName, String.CASE_INSENSITIVE_ORDER))
                .map(plugin -> "- " + plugin.getName() + " " + plugin.getDescription().getVersion()
                        + (plugin.isEnabled() ? " [enabled]" : " [disabled]"))
                .collect(Collectors.joining("\n", "Installed plugins:\n", ""));
    }

    private void requirePrimaryThread() {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("Server intelligence must be queried on the server thread.");
        }
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int unit = (int) (Math.log(bytes) / Math.log(1024));
        return String.format(Locale.ROOT, "%.2f %s", bytes / Math.pow(1024, unit),
                new String[]{"B", "KB", "MB", "GB", "TB"}[Math.min(unit, 4)]);
    }
}
