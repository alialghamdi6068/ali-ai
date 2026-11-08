package net.ali.ai.service;

import org.bukkit.entity.Player;

public interface AiRequestService {
    void submit(Player operator, String request);
}
