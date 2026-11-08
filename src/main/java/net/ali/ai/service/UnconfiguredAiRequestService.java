package net.ali.ai.service;

import net.ali.ai.AliAIPlugin;
import net.ali.ai.security.OperatorGuard;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

/** Safe fallback until a real provider and tool execution engine are installed. */
public final class UnconfiguredAiRequestService implements AiRequestService {
    private final AliAIPlugin plugin;

    public UnconfiguredAiRequestService(AliAIPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void submit(Player operator, String request) {
        if (!OperatorGuard.allows(plugin, operator, "quick-action")) return;
        String message = plugin.getConfig().getString("messages.provider-not-configured",
                "<yellow>Ali AI has no AI provider configured yet. Set one up in config.yml.</yellow>");
        operator.sendMessage(MiniMessage.miniMessage().deserialize(message));
    }
}
