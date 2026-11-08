package net.ali.ai;

import net.ali.ai.command.AiCommand;
import net.ali.ai.menu.AiMenuListener;
import net.ali.ai.menu.QuickActionListener;
import net.ali.ai.service.AiRequestService;
import net.ali.ai.service.UnconfiguredAiRequestService;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class AliAIPlugin extends JavaPlugin {
    private AiRequestService requestService;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.requestService = new UnconfiguredAiRequestService();
        PluginCommand command = getCommand("ai");
        if (command == null) {
            getLogger().severe("The /ai command is missing from plugin.yml; disabling Ali AI.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        command.setExecutor(new AiCommand(this));
        getServer().getPluginManager().registerEvents(new AiMenuListener(this), this);
        getServer().getPluginManager().registerEvents(new QuickActionListener(this), this);
        getLogger().info("Ali AI enabled. AI provider is not configured yet.");
    }

    public AiRequestService requestService() { return requestService; }
}
