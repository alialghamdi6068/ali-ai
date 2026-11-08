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
        if (!OperatorGuard.allows(player)) { player.closeInventory(); return; }
        if (event.getRawSlot() == AiMenu.QUICK_ACTION_SLOT) {
            player.closeInventory();
            AiMenu.openQuickAction(plugin, player);
        }
    }
}
