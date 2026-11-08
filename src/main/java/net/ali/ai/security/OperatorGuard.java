package net.ali.ai.security;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class OperatorGuard {
    public static final String DENIED_MESSAGE = "You don't have permission to use Ali AI.";
    private OperatorGuard() { }

    public static boolean allows(CommandSender sender) {
        if (sender instanceof Player player && player.isOp()) return true;
        sender.sendMessage(Component.text(DENIED_MESSAGE, NamedTextColor.RED));
        return false;
    }

    public static boolean allows(Player player) {
        if (player.isOp()) return true;
        player.sendMessage(Component.text(DENIED_MESSAGE, NamedTextColor.RED));
        return false;
    }
}
