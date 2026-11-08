package net.ali.ai.menu;

import net.ali.ai.AliAIPlugin;
import net.ali.ai.security.OperatorGuard;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

public final class AiMenuListener implements Listener {
    private final AliAIPlugin plugin;

    public AiMenuListener(AliAIPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getView().getTopInventory().getHolder(false);
        if (!(holder instanceof AiMenuHolder)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!OperatorGuard.allows(plugin, player, "command")) {
            player.closeInventory();
            return;
        }
        int quickSlot = Math.max(0, Math.min(26, plugin.getConfig().getInt("ui.menu.quick-action.slot", 13)));
        if (event.getRawSlot() == quickSlot) {
            player.closeInventory();
            AiMenu.openQuickAction(plugin, player);
        }
    }
}
