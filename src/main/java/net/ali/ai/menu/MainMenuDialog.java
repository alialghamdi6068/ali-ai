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

import java.util.List;

public final class MainMenuDialog {
    private static final Key QUICK_ACTION_KEY = Key.key("aliai:main_menu/quick_action");

    private MainMenuDialog() {
    }

    public static void open(AliAIPlugin plugin, Player player) {
        if (!OperatorGuard.allows(plugin, player, "command")) {
            return;
        }

        var config = plugin.getConfig();
        String title = config.getString("ui.main-menu.title", "Ali AI | Server Administrator");
        String description = config.getString(
                "ui.main-menu.description",
                "Your Minecraft server administration assistant."
        );

        Component status = plugin.secrets().hasAnyKey()
                ? Component.text("API credentials found. Connection health is not verified.", TextColor.color(0xE5B94C))
                : Component.text("No API key configured.", TextColor.color(0xE5B94C));

        ActionButton quickAction = ActionButton.create(
                Component.text(
                        config.getString("ui.main-menu.quick-action.label", "Quick Action"),
                        color(config.getString("ui.main-menu.quick-action.color", "#20C060"), 0x20C060)
                ),
                Component.text(config.getString(
                        "ui.main-menu.quick-action.tooltip",
                        "Describe an administrative task in natural language."
                )),
                clamp(config.getInt("ui.main-menu.quick-action.width", 300), 1, 1024),
                DialogAction.customClick(QUICK_ACTION_KEY, null)
        );

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(Component.text(title))
                        .canCloseWithEscape(true)
                        .body(List.of(
                                DialogBody.plainMessage(Component.text(description)),
                                DialogBody.plainMessage(status)
                        ))
                        .build())
                .type(DialogType.multiAction(
                        List.of(quickAction),
                        null,
                        clamp(config.getInt("ui.main-menu.columns", 1), 1, 8)
                ))
        );

        player.showDialog(dialog);
    }

    public static void openQuickAction(AliAIPlugin plugin, Player player) {
        if (!OperatorGuard.allows(plugin, player, "quick-action")) {
            return;
        }
        QuickActionDialog.open(plugin, player);
    }

    public static Key quickActionKey() {
        return QUICK_ACTION_KEY;
    }

    private static TextColor color(String hex, int fallback) {
        TextColor parsed = TextColor.fromHexString(hex);
        return parsed == null ? TextColor.color(fallback) : parsed;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
