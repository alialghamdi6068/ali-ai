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
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public final class AdminSectionDialog {
    private static final Key BACK_KEY = Key.key("aliai:section/back");

    private AdminSectionDialog() { }

    public static void open(AliAIPlugin plugin, Player player, String section) {
        String feature = featureFor(section);
        if (feature == null || !OperatorGuard.allows(plugin, player, feature)) return;
        if (section.equals("ai-assistant")) {
            QuickActionDialog.open(plugin, player);
            return;
        }

        String title = plugin.getConfig().getString("ui.sections." + section + ".title", displayName(section));
        String description = plugin.getConfig().getString("ui.sections." + section + ".description",
                "Current information exposed by the running Paper server.");
        String content;
        try {
            content = contentFor(plugin, section);
        } catch (IOException | RuntimeException exception) {
            plugin.getLogger().warning("Unable to open Ali AI section " + section + ": "
                    + exception.getClass().getSimpleName());
            content = "This section could not retrieve its current information.";
        }

        if (content.length() > 12000) {
            content = content.substring(0, 11997) + "...";
        }
        String dialogContent = content;

        var config = plugin.getConfig();
        ActionButton back = ActionButton.create(
                Component.text(config.getString("ui.sections.back.label", "Back"),
                        color(config.getString("ui.sections.back.color", "#38BDF8"), 0x38BDF8)),
                Component.text(config.getString("ui.sections.back.tooltip", "Return to the Ali AI main menu")),
                clamp(config.getInt("ui.sections.back.width", 120), 1, 1024),
                DialogAction.customClick(BACK_KEY, null));

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(Component.text(title))
                        .canCloseWithEscape(true)
                        .body(List.of(
                                DialogBody.plainMessage(Component.text(description)),
                                DialogBody.plainMessage(Component.text(dialogContent))))
                        .build())
                .type(DialogType.notice(back)));

        player.showDialog(dialog);
    }

    public static Key backKey() {
        return BACK_KEY;
    }

    private static String contentFor(AliAIPlugin plugin, String section) throws IOException {
        return switch (section) {
            case "server-intelligence" -> plugin.serverIntelligence().serverStatus();
            case "file-intelligence" -> plugin.fileIntelligence().list(".");
            case "plugin-intelligence" -> plugin.serverIntelligence().plugins();
            case "player-management" -> plugin.serverIntelligence().onlinePlayers();
            case "world-management" -> plugin.serverIntelligence().worlds();
            case "performance-analyzer" -> plugin.serverIntelligence().performance();
            case "security-center" -> securitySnapshot();
            case "task-manager" -> "Currently tracked background tasks: " + plugin.taskManager().activeCount()
                    + "\nTask history is intentionally not persisted.";
            case "settings" -> settingsSnapshot(plugin);
            default -> throw new IllegalArgumentException("Unknown Ali AI section.");
        };
    }

    private static String securitySnapshot() {
        long onlineOperators = Bukkit.getOnlinePlayers().stream().filter(Player::isOp).count();
        String operators = Bukkit.getOnlinePlayers().stream().filter(Player::isOp)
                .map(Player::getName).sorted().reduce((left, right) -> left + ", " + right).orElse("none");
        return """
                Security snapshot (limited to data exposed by Paper):
                Online OP players: %d
                Online OP names: %s
                Plugin count: %d
                File inspection: confined to server root; symbolic links rejected
                Note: this is not a complete permission/plugin vulnerability audit.
                """.formatted(onlineOperators, operators, Bukkit.getPluginManager().getPlugins().length);
    }

    private static String settingsSnapshot(AliAIPlugin plugin) {
        StringBuilder result = new StringBuilder("AI provider settings (API keys are hidden):\n");
        for (String provider : List.of("gemini", "openai", "groq")) {
            boolean enabled = plugin.getConfig().getBoolean("ai.providers." + provider + ".enabled", false);
            String model = plugin.getConfig().getString("ai.providers." + provider + ".model", "").trim();
            result.append("- ").append(provider.toUpperCase(Locale.ROOT))
                    .append(": ").append(enabled ? "enabled" : "disabled")
                    .append(" | model: ").append(model.isEmpty() ? "not selected" : model).append('\n');
        }
        result.append("Default provider: ")
                .append(plugin.getConfig().getString("ai.default-provider", "not configured"));
        result.append("\nConversation history: in-memory, per OP");
        return result.toString();
    }

    private static String featureFor(String section) {
        return switch (section) {
            case "file-intelligence" -> "manage-files";
            case "plugin-intelligence" -> "manage-plugins";
            case "player-management" -> "manage-players";
            case "world-management" -> "manage-worlds";
            case "ai-assistant" -> "quick-action";
            case "server-intelligence", "performance-analyzer", "security-center", "task-manager" -> "view-status";
            case "settings" -> "command";
            default -> null;
        };
    }

    private static String displayName(String section) {
        String[] words = section.split("-");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!result.isEmpty()) result.append(' ');
            result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return result.toString();
    }

    private static TextColor color(String hex, int fallback) {
        TextColor parsed = TextColor.fromHexString(hex);
        return parsed == null ? TextColor.color(fallback) : parsed;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
