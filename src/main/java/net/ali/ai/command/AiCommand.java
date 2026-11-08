package net.ali.ai.command;

import net.ali.ai.AliAIPlugin;
import net.ali.ai.menu.AiMenu;
import net.ali.ai.security.OperatorGuard;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class AiCommand implements CommandExecutor {
    private final AliAIPlugin plugin;
    public AiCommand(AliAIPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!OperatorGuard.allows(sender)) return true;
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be used in-game.", NamedTextColor.RED));
            return true;
        }
        if (args.length == 0) {
            AiMenu.open(plugin, player);
            return true;
        }
        switch (args[0].toLowerCase(java.util.Locale.ROOT)) {
            case "help" -> player.sendMessage(Component.text(
                    "/ai, /ai status, /ai logs, /ai errors, /ai diagnose, /ai files, /ai backup, /ai tasks, /ai history, /ai providers", NamedTextColor.AQUA));
            case "status" -> player.sendMessage(Component.text("Ali AI is online. Provider setup is pending.", NamedTextColor.GREEN));
            case "quick" -> AiMenu.openQuickAction(plugin, player);
            default -> player.sendMessage(Component.text("Unknown Ali AI command. Use /ai help.", NamedTextColor.RED));
        }
        return true;
    }
}
