package net.ali.ai.menu;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.ali.ai.AliAIPlugin;
import net.ali.ai.security.OperatorGuard;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class MainMenuDialog {
    private static final Key QUICK_ACTION_KEY = Key.key("aliai:main_menu/quick_action");
    private static final String SECTION_PREFIX = "aliai:main_menu/section/";

    private static final List<Section> SECTIONS = List.of(
            new Section("ai-assistant", "AI Assistant", "Chat with Ali AI and request server tasks.", "#20C060"),
            new Section("server-intelligence", "Server Intelligence", "Inspect live server information.", "#38BDF8"),
            new Section("file-intelligence", "File Intelligence", "Browse and inspect server files.", "#60A5FA"),
            new Section("plugin-intelligence", "Plugin Intelligence", "Inspect installed plugins.", "#A78BFA"),
            new Section("player-management", "Player Management", "Inspect online players.", "#F59E0B"),
            new Section("world-management", "World Management", "Inspect loaded worlds.", "#34D399"),
            new Section("performance-analyzer", "Performance Analyzer", "Inspect TPS, MSPT and JVM memory.", "#F97316"),
            new Section("security-center", "Security Center", "View the available security snapshot.", "#EF4444"),
            new Section("task-manager", "Task Manager", "View currently running Ali AI tasks.", "#94A3B8"),
            new Section("settings", "Settings", "Inspect current provider and AI settings.", "#EAB308")
    );

    private MainMenuDialog() { }

    public static void open(AliAIPlugin plugin, Player player) {
        if (!OperatorGuard.allows(plugin, player, "command")) return;

        var config = plugin.getConfig();
        String title = config.getString("ui.main-menu.title", "Ali AI | Server Administrator");
        String description = config.getString("ui.main-menu.description",
                "Your Minecraft server administration assistant.");
        Component status = plugin.secrets().hasAnyKey()
                ? Component.text("API credentials found. Connection health is not verified.", TextColor.color(0xE5B94C))
                : Component.text("No API key configured.", TextColor.color(0xE5B94C));

        List<ActionButton> actions = new ArrayList<>();
        actions.add(ActionButton.create(
                Component.text(
                        config.getString("ui.main-menu.quick-action.label", "Quick Action"),
                        color(config.getString("ui.main-menu.quick-action.color", "#20C060"), 0x20C060)),
                Component.text(config.getString("ui.main-menu.quick-action.tooltip",
                        "Describe an administrative task in natural language.")),
                clamp(config.getInt("ui.main-menu.quick-action.width", 300), 1, 1024),
                DialogAction.customClick(QUICK_ACTION_KEY, null)));

        for (Section section : SECTIONS) {
            String path = "ui.main-menu.sections." + section.id();
            actions.add(ActionButton.create(
                    Component.text(config.getString(path + ".label", section.label()),
                            color(config.getString(path + ".color", section.color()), 0x38BDF8)),
                    Component.text(config.getString(path + ".tooltip", section.tooltip())),
                    clamp(config.getInt(path + ".width", 300), 1, 1024),
                    DialogAction.customClick(sectionKey(section.id()), null)));
        }

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(Component.text(title))
                        .canCloseWithEscape(true)
                        .body(List.of(
                                DialogBody.plainMessage(Component.text(description)),
                                DialogBody.plainMessage(status)))
                        .build())
                .type(DialogType.multiAction(
                        actions,
                        null,
                        clamp(config.getInt("ui.main-menu.columns", 2), 1, 8))));

        player.showDialog(dialog);
    }

    public static void openQuickAction(AliAIPlugin plugin, Player player) {
        if (!OperatorGuard.allows(plugin, player, "quick-action")) return;
        QuickActionDialog.open(plugin, player);
    }

    public static Key quickActionKey() {
        return QUICK_ACTION_KEY;
    }

    public static boolean isSectionKey(String identifier) {
        return identifier != null && identifier.startsWith(SECTION_PREFIX);
    }

    public static String sectionId(String identifier) {
        return isSectionKey(identifier) ? identifier.substring(SECTION_PREFIX.length()) : "";
    }

    private static Key sectionKey(String id) {
        return Key.key(SECTION_PREFIX + id);
    }

    private static TextColor color(String hex, int fallback) {
        TextColor parsed = TextColor.fromHexString(hex);
        return parsed == null ? TextColor.color(fallback) : parsed;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private record Section(String id, String label, String tooltip, String color) { }
}
