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

import java.util.List;

public final class AiMenu {
    private AiMenu() { }

    public static void open(AliAIPlugin plugin, Player player) {
        if (!OperatorGuard.allows(plugin, player, "command")) return;
        var config = plugin.getConfig();
        AiMenuHolder holder = new AiMenuHolder();
        Inventory inventory = Bukkit.createInventory(holder, 27,
                Component.text(config.getString("ui.menu.title", "Ali AI | Server Administrator")));
        holder.attach(inventory);
        inventory.setItem(slot(config.getInt("ui.menu.quick-action.slot", 13)), item(
                material(config.getString("ui.menu.quick-action.material", "WRITABLE_BOOK"), Material.WRITABLE_BOOK),
                config.getString("ui.menu.quick-action.name", "Quick Action"),
                config.getString("ui.menu.quick-action.description", "Write a request in your own words."), NamedTextColor.AQUA));
        inventory.setItem(slot(config.getInt("ui.menu.status.slot", 11)), item(
                material(config.getString("ui.menu.status.material", "COMPASS"), Material.COMPASS),
                config.getString("ui.menu.status.name", "Server Status"),
                config.getString("ui.menu.status.description", "Inspect the current server state."), NamedTextColor.GREEN));
        inventory.setItem(slot(config.getInt("ui.menu.logs.slot", 15)), item(
                material(config.getString("ui.menu.logs.material", "BOOK"), Material.BOOK),
                config.getString("ui.menu.logs.name", "Server Logs"),
                config.getString("ui.menu.logs.description", "Read and analyze existing log files."), NamedTextColor.YELLOW));
        player.openInventory(inventory);
    }

    public static void openQuickAction(AliAIPlugin plugin, Player player) {
        if (!OperatorGuard.allows(plugin, player, "quick-action")) return;
        QuickActionDialog.open(plugin, player);
    }

    private static int slot(int value) { return Math.max(0, Math.min(26, value)); }

    private static ItemStack item(Material material, String name, String description, NamedTextColor color) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(Component.text(name, color));
        meta.lore(List.of(Component.text(description, NamedTextColor.GRAY)));
        stack.setItemMeta(meta);
        return stack;
    }

    private static Material material(String name, Material fallback) {
        Material material = Material.matchMaterial(name);
        return material == null ? fallback : material;
    }
}
