package net.ali.ai.service;

import net.ali.ai.AliAIPlugin;
import net.ali.ai.security.OperatorGuard;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

/**
 * Keeps Ali AI usable before provider integration is configured.
 */
public final class UnconfiguredAiRequestService implements AiRequestService {
    private final AliAIPlugin plugin;

    public UnconfiguredAiRequestService(AliAIPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void submit(Player operator, String request) {
        if (!OperatorGuard.allows(plugin, operator, "quick-action")) {
            return;
        }

        String path = plugin.secrets().hasAnyKey()
                ? "messages.provider-unavailable"
                : "messages.no-api-key";
        String fallback = plugin.secrets().hasAnyKey()
                ? "<yellow>API credentials were found, but AI provider requests are not available yet.</yellow>"
                : "<yellow>No API key is configured. Add a key to plugins/AliAI/secrets.yml.</yellow>";

        String message = plugin.getConfig().getString(path, fallback);
        operator.sendMessage(MiniMessage.miniMessage().deserialize(message));
    }
}
