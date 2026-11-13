package net.ali.ai.command;

import net.ali.ai.AliAIPlugin;
import net.ali.ai.menu.MainMenuDialog;
import net.ali.ai.security.OperatorGuard;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

public final class AiCommand implements CommandExecutor {
    private final AliAIPlugin plugin;

    public AiCommand(AliAIPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!OperatorGuard.allows(plugin, sender, "command")) return true;

        if (args.length == 0) {
            if (sender instanceof Player player) MainMenuDialog.open(plugin, player);
            else sender.sendMessage("Ali AI is enabled. Use /ai help for available commands.");
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "help" -> sendLines(sender, """
                    Ali AI commands:
                    /ai status
                    /ai players
                    /ai worlds
                    /ai plugins
                    /ai performance
                    /ai security
                    /ai files list [path]
                    /ai files read <path>
                    /ai files search <name>
                    /ai search <query>
                    /ai quick
                    /ai reload
                    """);
            case "status" -> sendLines(sender, plugin.serverIntelligence().serverStatus());
            case "players" -> sendLines(sender, plugin.serverIntelligence().onlinePlayers());
            case "worlds" -> sendLines(sender, plugin.serverIntelligence().worlds());
            case "plugins" -> sendLines(sender, plugin.serverIntelligence().plugins());
            case "performance" -> sendLines(sender, plugin.serverIntelligence().serverStatus());
            case "security" -> sendSecuritySummary(sender);
            case "files" -> handleFiles(sender, args);
            case "search" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(Component.text("Web search can only be used in-game.", NamedTextColor.RED));
                    break;
                }
                if (args.length < 2) {
                    sender.sendMessage(Component.text("Usage: /ai search <query>", NamedTextColor.YELLOW));
                    break;
                }
                String query = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                if (plugin.requestService() instanceof net.ali.ai.service.HttpAiRequestService service) {
                    service.search(player, query);
                } else {
                    sender.sendMessage(Component.text("Web search is not available.", NamedTextColor.RED));
                }
            }
            case "quick" -> {
                if (sender instanceof Player player) MainMenuDialog.openQuickAction(plugin, player);
                else sender.sendMessage(Component.text("This action can only be used in-game.", NamedTextColor.RED));
            }
            case "reload" -> {
                plugin.reloadSettings();
                String message = plugin.getConfig().getString("messages.config-reloaded", "<green>Ali AI configuration reloaded.</green>");
                sender.sendMessage(MiniMessage.miniMessage().deserialize(message));
            }
            default -> sender.sendMessage(Component.text("Unknown Ali AI command. Use /ai help.", NamedTextColor.RED));
        }
        return true;
    }

    private void handleFiles(CommandSender sender, String[] args) {
        if (!OperatorGuard.allows(plugin, sender, "manage-files")) return;
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /ai files <list|read|search> [path or name]", NamedTextColor.YELLOW));
            return;
        }
        String operation = args[1].toLowerCase(Locale.ROOT);
        String target = args.length > 2 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : ".";
        try {
            String result = switch (operation) {
                case "list" -> plugin.fileIntelligence().list(target);
                case "read" -> {
                    if (args.length < 3) throw new IOException("Usage: /ai files read <relative-path>");
                    yield plugin.fileIntelligence().read(target);
                }
                case "search" -> {
                    if (args.length < 3) throw new IOException("Usage: /ai files search <file-name>");
                    yield plugin.fileIntelligence().search(target);
                }
                default -> throw new IOException("Unknown file operation. Use list, read or search.");
            };
            sendLines(sender, result);
        } catch (IOException | SecurityException exception) {
            sender.sendMessage(Component.text("File operation rejected: " + exception.getMessage(), NamedTextColor.RED));
        } catch (RuntimeException exception) {
            plugin.getLogger().warning("File inspection failed: " + exception.getClass().getSimpleName());
            sender.sendMessage(Component.text("File inspection failed. Check the server log for details.", NamedTextColor.RED));
        }
    }

    private void sendSecuritySummary(CommandSender sender) {
        long onlineOperators = Bukkit.getOnlinePlayers().stream().filter(Player::isOp).count();
        String operators = Bukkit.getOnlinePlayers().stream().filter(Player::isOp)
                .map(Player::getName).sorted().reduce((left, right) -> left + ", " + right).orElse("none");
        sendLines(sender, """
                Security snapshot (limited to data exposed by Paper):
                Online OP players: %d
                Online OP names: %s
                Plugin count: %d
                File inspection: confined to server root; symbolic links rejected
                Note: this is not a complete permission/plugin vulnerability audit.
                """.formatted(onlineOperators, operators, Bukkit.getPluginManager().getPlugins().length));
    }

    private void sendLines(CommandSender sender, String output) {
        if (output == null || output.isBlank()) {
            sender.sendMessage(Component.text("No information available.", NamedTextColor.GRAY));
            return;
        }
        for (String line : output.split("\\R", -1)) {
            if (!line.isEmpty()) sender.sendMessage(Component.text(line, NamedTextColor.GRAY));
        }
    }
}
