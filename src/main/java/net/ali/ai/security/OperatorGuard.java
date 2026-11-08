package net.ali.ai.security;

import net.ali.ai.AliAIPlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class OperatorGuard {
    private OperatorGuard() { }

    public static boolean allows(AliAIPlugin plugin, CommandSender sender, String feature) {
        if (!(sender instanceof Player player) || !player.isOp()) {
            deny(plugin, sender);
            return false;
        }

        String permission = plugin.getConfig().getString("permissions." + feature, "").trim();
        if (!permission.isEmpty() && !player.hasPermission(permission)) {
            deny(plugin, sender);
            return false;
        }
        return true;
    }

    public static boolean allows(AliAIPlugin plugin, Player player, String feature) {
        return allows(plugin, (CommandSender) player, feature);
    }

    private static void deny(AliAIPlugin plugin, CommandSender sender) {
        String message = plugin.getConfig().getString(
                "security.denied-message", "You don't have permission to use Ali AI.");
        sender.sendMessage(MiniMessage.miniMessage().deserialize(message));
    }
}
