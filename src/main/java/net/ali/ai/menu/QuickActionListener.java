package net.ali.ai.menu;

import io.papermc.paper.connection.PlayerGameConnection;
import io.papermc.paper.event.player.PlayerCustomClickEvent;
import net.ali.ai.AliAIPlugin;
import net.ali.ai.security.OperatorGuard;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class QuickActionListener implements Listener {
    private final AliAIPlugin plugin;

    public QuickActionListener(AliAIPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAliAIDialogClick(PlayerCustomClickEvent event) {
        if (!(event.getCommonConnection() instanceof PlayerGameConnection connection)) {
            return;
        }

        Player player = connection.getPlayer();
        if (!OperatorGuard.allows(plugin, player, "command")) {
            return;
        }

        if (event.getIdentifier().asString().startsWith("aliai:player_action/")) {
            plugin.playerManagement().handleClick(player, event.getIdentifier());
            return;
        }
        if (event.getIdentifier().asString().startsWith("aliai:world_action/")) {
            plugin.worldManagement().handleClick(player, event.getIdentifier());
            return;
        }

        if (event.getIdentifier().equals(MainMenuDialog.quickActionKey())) {
            MainMenuDialog.openQuickAction(plugin, player);
            return;
        }

        if (event.getIdentifier().equals(QuickActionDialog.sendKey())) {
            QuickActionDialog.handleSend(plugin, player, event.getDialogResponseView());
        }
    }
}
