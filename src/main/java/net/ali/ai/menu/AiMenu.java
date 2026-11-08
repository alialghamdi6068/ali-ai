package net.ali.ai.menu;

import net.ali.ai.AliAIPlugin;
import net.ali.ai.security.OperatorGuard;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class AiMenu {
    public static final int QUICK_ACTION_SLOT = 13;
    private AiMenu() { }

    public static void open(AliAIPlugin plugin, Player player) {
        if (!OperatorGuard.allows(player)) return;
        Inventory inventory = Bukkit.createInventory(new AiMenuHolder(), 27, Component.text("Ali AI", NamedTextColor.DARK_AQUA));
        inventory.setItem(QUICK_ACTION_SLOT, item(Material.WRITABLE_BOOK, "Quick Action", NamedTextColor.AQUA, "Write a request in natural language."));
        inventory.setItem(11, item(Material.COMPASS, "Server Status", NamedTextColor.GREEN, "Inspect current server status."));
        inventory.setItem(15, item(Material.BOOK, "Logs", NamedTextColor.YELLOW, "Read existing server log files."));
        player.openInventory(inventory);
    }

    public static void openQuickAction(AliAIPlugin plugin, Player player) {
        if (!OperatorGuard.allows(player)) return;
        QuickActionDialog.open(plugin, player);
    }

    private static ItemStack item(Material material, String name, NamedTextColor color, String lore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(Component.text(name, color));
        meta.lore(java.util.List.of(Component.text(lore, NamedTextColor.GRAY)));
        stack.setItemMeta(meta);
        return stack;
    }
}
