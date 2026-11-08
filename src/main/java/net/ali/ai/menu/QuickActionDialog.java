package net.ali.ai.menu;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.ActionButton;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.ali.ai.AliAIPlugin;
import net.ali.ai.security.OperatorGuard;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;

import java.util.List;

public final class QuickActionDialog {
    private static final Key SEND_KEY = Key.key("aliai:quick_action/send");
    private static final String INPUT_KEY = "request";

    private QuickActionDialog() { }

    public static void open(AliAIPlugin plugin, Player player) {
        if (!OperatorGuard.allows(player)) return;

        var config = plugin.getConfig();
        String inputKey = config.getString("ui.quick-action.input.key", INPUT_KEY);
        int inputWidth = clamp(config.getInt("ui.quick-action.input.width", 300), 1, 1024);
        int maxLength = clamp(config.getInt("ui.quick-action.input.max-length", 1000), 1, 1024);
        boolean multiline = config.getBoolean("ui.quick-action.input.multiline.enabled", true);

        TextDialogInput.MultilineOptions multilineOptions = multiline
                ? TextDialogInput.MultilineOptions.create(
                        clamp(config.getInt("ui.quick-action.input.multiline.max-lines", 6), 1, 64),
                        clamp(config.getInt("ui.quick-action.input.multiline.height", 120), 1, 512))
                : null;

        Component title = Component.text(config.getString("ui.quick-action.title", "Ali AI — Quick Action"));
        Component description = Component.text(config.getString("ui.quick-action.description", "Describe what you want Ali AI to do."));
        Component inputLabel = Component.text(config.getString("ui.quick-action.input.label", "Your request"));

        ActionButton sendButton = ActionButton.create(
                colored(config.getString("ui.quick-action.send.label", "Send"), config.getString("ui.quick-action.send.color", "#20C060"), 0x20C060),
                Component.text(config.getString("ui.quick-action.send.tooltip", "Send this request to Ali AI")),
                100,
                DialogAction.customClick(SEND_KEY, null));

        ActionButton cancelButton = ActionButton.create(
                colored(config.getString("ui.quick-action.cancel.label", "Cancel"), config.getString("ui.quick-action.cancel.color", "#E04444"), 0xE04444),
                Component.text(config.getString("ui.quick-action.cancel.tooltip", "Close without sending")),
                100,
                null);

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .canCloseWithEscape(config.getBoolean("ui.quick-action.allow-escape", true))
                        .body(List.of(DialogBody.plainMessage(description)))
                        .inputs(List.of(DialogInput.text(inputKey, inputWidth, inputLabel, true, "", maxLength, multilineOptions)))
                        .build())
                .type(DialogType.confirmation(sendButton, cancelButton)));

        player.showDialog(dialog);
    }

    public static void handleSend(AliAIPlugin plugin, Player player, DialogResponseView response) {
        if (!OperatorGuard.allows(player)) return;
        if (response == null) {
            sendConfiguredMessage(plugin, player, "messages.dialog-unavailable", "The request could not be read. Please try again.");
            return;
        }

        String inputKey = plugin.getConfig().getString("ui.quick-action.input.key", INPUT_KEY);
        String request = response.getText(inputKey);
        int maxLength = clamp(plugin.getConfig().getInt("ui.quick-action.input.max-length", 1000), 1, 1024);
        if (request == null || request.isBlank()) {
            sendConfiguredMessage(plugin, player, "messages.empty-request", "Please enter a request before pressing Send.");
            return;
        }
        if (request.length() > maxLength) {
            String message = plugin.getConfig().getString("messages.request-too-long", "Your request is too long. Maximum: %maximum% characters.")
                    .replace("%maximum%", Integer.toString(maxLength));
            player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(message));
            return;
        }

        plugin.requestService().submit(player, request.strip());
    }

    private static void sendConfiguredMessage(AliAIPlugin plugin, Player player, String path, String fallback) {
        String message = plugin.getConfig().getString(path, fallback);
        player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(message));
    }

    private static Component colored(String label, String hex, int fallback) {
        TextColor color = TextColor.fromHexString(hex);
        return Component.text(label, color == null ? TextColor.color(fallback) : color);
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    public static Key sendKey() { return SEND_KEY; }
}
