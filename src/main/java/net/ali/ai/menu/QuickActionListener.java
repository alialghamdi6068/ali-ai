package net.ali.ai.menu;

import io.papermc.paper.connection.PlayerGameConnection;
import io.papermc.paper.event.player.PlayerCustomClickEvent;
import net.ali.ai.AliAIPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class QuickActionListener implements Listener {
    private final AliAIPlugin plugin;
    public QuickActionListener(AliAIPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onQuickActionSend(PlayerCustomClickEvent event) {
        if (!event.getIdentifier().equals(QuickActionDialog.sendKey())) return;
        if (!(event.getCommonConnection() instanceof PlayerGameConnection connection)) return;
        Player player = connection.getPlayer();
        QuickActionDialog.handleSend(plugin, player, event.getDialogResponseView());
    }
}
