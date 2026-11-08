package net.ali.ai;

import net.ali.ai.command.AiCommand;
import net.ali.ai.menu.QuickActionListener;
import net.ali.ai.service.AiRequestService;
import net.ali.ai.service.UnconfiguredAiRequestService;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.LinkedHashSet;
import java.util.Set;

public final class AliAIPlugin extends JavaPlugin {
    private AiRequestService requestService;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfig();
        registerConfiguredPermissions();
        requestService = new UnconfiguredAiRequestService(this);

        PluginCommand command = getCommand("ai");
        if (command == null) {
            getLogger().severe("The /ai command is missing from plugin.yml; disabling Ali AI.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        command.setExecutor(new AiCommand(this));
        getServer().getPluginManager().registerEvents(new QuickActionListener(this), this);
        getLogger().info("Ali AI enabled. Configuration loaded; AI provider is not configured yet.");
    }

    public void reloadSettings() {
        reloadConfig();
        registerConfiguredPermissions();
    }

    private void registerConfiguredPermissions() {
        var section = getConfig().getConfigurationSection("permissions");
        if (section == null) return;

        Set<String> nodes = new LinkedHashSet<>();
        for (String feature : section.getKeys(false)) {
            String node = getConfig().getString("permissions." + feature, "").trim();
            if (!node.isEmpty()) nodes.add(node);
        }
        for (String node : nodes) {
            if (Bukkit.getPluginManager().getPermission(node) != null) continue;
            try {
                Bukkit.getPluginManager().addPermission(new Permission(node, PermissionDefault.OP));
            } catch (IllegalArgumentException exception) {
                getLogger().warning("Invalid permission node in config.yml: " + node);
            }
        }
    }

    public AiRequestService requestService() {
        return requestService;
    }
}
