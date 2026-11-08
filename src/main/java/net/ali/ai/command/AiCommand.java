package net.ali.ai.command;

import net.ali.ai.AliAIPlugin;
import net.ali.ai.menu.AiMenu;
import net.ali.ai.security.OperatorGuard;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class AiCommand implements CommandExecutor {
    private final AliAIPlugin plugin;

    public AiCommand(AliAIPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!OperatorGuard.allows(sender)) return true;

        if (args.length == 0) {
            if (sender instanceof Player player) {
                AiMenu.open(plugin, player);
            } else {
                sender.sendMessage("Ali AI is enabled. Use /ai help for available commands.");
            }
            return true;
        }

        switch (args[0].toLowerCase(java.util.Locale.ROOT)) {
            case "help" -> sender.sendMessage(Component.text(
                    "/ai, /ai status, /ai quick, /ai reload", NamedTextColor.AQUA));
            case "status" -> sender.sendMessage(Component.text(
                    "Ali AI is online. Provider setup is pending.", NamedTextColor.GREEN));
            case "quick" -> {
                if (sender instanceof Player player) {
                    AiMenu.openQuickAction(plugin, player);
                } else {
                    sender.sendMessage(Component.text("This action can only be used in-game.", NamedTextColor.RED));
                }
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
}
