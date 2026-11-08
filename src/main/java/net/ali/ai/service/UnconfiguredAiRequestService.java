package net.ali.ai.service;

import net.ali.ai.security.OperatorGuard;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

/** Safe placeholder until an AI provider and tool execution engine are configured. */
public final class UnconfiguredAiRequestService implements AiRequestService {
    @Override
    public void submit(Player operator, String request) {
        if (!OperatorGuard.allows(operator)) return;
        operator.sendMessage(Component.text("Your request was received, but no AI provider is configured yet.", NamedTextColor.YELLOW));
    }
}
