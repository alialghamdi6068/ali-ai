package net.ali.ai.menu;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.DialogInput;
import io.papermc.paper.registry.data.dialog.action.ActionButton;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.ali.ai.AliAIPlugin;
import net.ali.ai.security.OperatorGuard;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;

public final class QuickActionDialog {
    private static final Key SEND_KEY = Key.key("aliai:quick_action/send");
    private static final String INPUT_KEY = "request";
    private QuickActionDialog() { }

    public static void open(AliAIPlugin plugin, Player player) {
        if (!OperatorGuard.allows(player)) return;
        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(Component.text("Ali AI — Quick Action", NamedTextColor.DARK_AQUA))
                        .body(java.util.List.of(DialogBody.plainMessage(Component.text(
                                "Describe what you want Ali AI to do. Arabic and English are supported.", NamedTextColor.GRAY))))
                        .inputs(java.util.List.of(DialogInput.text(INPUT_KEY, 300,
                                Component.text("Your request", NamedTextColor.WHITE), true, "", 1000, null)))
                        .build())
                .type(DialogType.confirmation(
                        ActionButton.create(Component.text("Send", TextColor.color(0x20C060)),
                                Component.text("Send this request to Ali AI", NamedTextColor.GREEN), 100,
                                DialogAction.customClick(SEND_KEY, null)),
                        ActionButton.create(Component.text("Cancel", TextColor.color(0xE04444)),
                                Component.text("Close without sending", NamedTextColor.RED), 100, null))));
        player.showDialog(dialog);
    }

    public static void handleSend(AliAIPlugin plugin, Player player, DialogResponseView response) {
        if (!OperatorGuard.allows(player)) return;
        if (response == null) {
            player.sendMessage(Component.text("The request could not be read. Please try again.", NamedTextColor.RED));
            return;
        }
        String request = response.getText(INPUT_KEY);
        if (request == null || request.isBlank()) {
            player.sendMessage(Component.text("Please enter a request before pressing Send.", NamedTextColor.RED));
            return;
        }
        if (request.length() > 1000) {
            player.sendMessage(Component.text("Your request is too long (maximum 1000 characters).", NamedTextColor.RED));
            return;
        }
        plugin.requestService().submit(player, request.strip());
    }

    public static Key sendKey() { return SEND_KEY; }
}
