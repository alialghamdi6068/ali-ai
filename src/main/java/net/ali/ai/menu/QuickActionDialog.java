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
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

import java.util.List;

public final class QuickActionDialog {
    private static final Key SEND_KEY = Key.key("aliai:quick_action/send");
    private static final String DEFAULT_INPUT_KEY = "request";

    private QuickActionDialog() { }

    public static void open(AliAIPlugin plugin, Player player) {
        if (!OperatorGuard.allows(plugin, player, "quick-action")) return;
        var config = plugin.getConfig();
        String inputKey = config.getString("ui.quick-action.input.key", DEFAULT_INPUT_KEY);
        int inputWidth = clamp(config.getInt("ui.quick-action.input.width", 300), 1, 1024);
        int maxLength = clamp(config.getInt("ui.quick-action.input.max-length", 1000), 1, 1024);
        TextDialogInput.MultilineOptions multilineOptions = config.getBoolean("ui.quick-action.input.multiline.enabled", true)
                ? TextDialogInput.MultilineOptions.create(
                    clamp(config.getInt("ui.quick-action.input.multiline.max-lines", 6), 1, 64),
                    clamp(config.getInt("ui.quick-action.input.multiline.height", 120), 1, 512))
                : null;

        ActionButton send = ActionButton.create(
                colored(config.getString("ui.quick-action.send.label", "Send"), config.getString("ui.quick-action.send.color", "#20C060"), 0x20C060),
                Component.text(config.getString("ui.quick-action.send.tooltip", "Send this request to Ali AI")),
                100, DialogAction.customClick(SEND_KEY, null));
        ActionButton cancel = ActionButton.create(
                colored(config.getString("ui.quick-action.cancel.label", "Cancel"), config.getString("ui.quick-action.cancel.color", "#E04444"), 0xE04444),
                Component.text(config.getString("ui.quick-action.cancel.tooltip", "Close without sending")),
                100, null);

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(Component.text(config.getString("ui.quick-action.title", "Ali AI — Quick Action")))
                        .canCloseWithEscape(config.getBoolean("ui.quick-action.allow-escape", true))
                        .body(List.of(DialogBody.plainMessage(Component.text(config.getString(
                                "ui.quick-action.description", "Describe what you want Ali AI to do.")))))
                        .inputs(List.of(DialogInput.text(inputKey, inputWidth,
                                Component.text(config.getString("ui.quick-action.input.label", "Your request")),
                                true, "", maxLength, multilineOptions)))
                        .build())
                .type(DialogType.confirmation(send, cancel)));
        player.showDialog(dialog);
    }

    public static void handleSend(AliAIPlugin plugin, Player player, DialogResponseView response) {
        if (!OperatorGuard.allows(plugin, player, "quick-action")) return;
        if (response == null) {
            sendMessage(plugin, player, "messages.dialog-unavailable", "<red>The request could not be read. Please try again.</red>");
            return;
        }

        String inputKey = plugin.getConfig().getString("ui.quick-action.input.key", DEFAULT_INPUT_KEY);
        String request = response.getText(inputKey);
        int maxLength = clamp(plugin.getConfig().getInt("ui.quick-action.input.max-length", 1000), 1, 1024);
        if (request == null || request.isBlank()) {
            sendMessage(plugin, player, "messages.empty-request", "<red>Please enter a request before pressing Send.</red>");
            return;
        }
        if (request.length() > maxLength) {
            String message = plugin.getConfig().getString("messages.request-too-long", "<red>Your request is too long. Maximum: %maximum% characters.</red>")
                    .replace("%maximum%", Integer.toString(maxLength));
            player.sendMessage(MiniMessage.miniMessage().deserialize(message));
            return;
        }
        plugin.requestService().submit(player, request.strip());
    }

    private static void sendMessage(AliAIPlugin plugin, Player player, String path, String fallback) {
        player.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfig().getString(path, fallback)));
    }

    private static Component colored(String label, String hex, int fallback) {
        TextColor color = TextColor.fromHexString(hex);
        return Component.text(label, color == null ? TextColor.color(fallback) : color);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static Key sendKey() { return SEND_KEY; }
}
