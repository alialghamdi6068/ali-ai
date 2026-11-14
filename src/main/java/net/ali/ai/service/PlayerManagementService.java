package net.ali.ai.service;

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
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/** Player inspection and confirmation-gated actions. Bukkit access is restricted to the server thread. */
public final class PlayerManagementService {
    private final AliAIPlugin plugin;
    private final Map<UUID, PendingKick> pendingKicks = new ConcurrentHashMap<>();

    public PlayerManagementService(AliAIPlugin plugin) {
        this.plugin = plugin;
    }

    public String inspectOnlinePlayer(String name) {
        requirePrimaryThread();
        Player target = findOnlinePlayer(name);
        if (target == null) return "No online player matched that exact name.";

        return """
                Player details
                Name: %s
                UUID: %s
                OP: %s
                Ping: %d ms
                World: %s
                Location: %.1f, %.1f, %.1f
                Game mode: %s
                Health: %.1f / %.1f
                Food: %d / 20
                Level: %d
                """.formatted(
                target.getName(), target.getUniqueId(), target.isOp(), target.getPing(),
                target.getWorld().getName(), target.getX(), target.getY(), target.getZ(),
                target.getGameMode(), target.getHealth(), target.getMaxHealth(),
                target.getFoodLevel(), target.getLevel());
    }

    public CompletableFuture<String> requestKick(Player operator, String targetName, String reason) {
        requirePrimaryThread();
        if (!operator.isOnline() || !OperatorGuard.allows(plugin, operator, "command")
                || !OperatorGuard.allows(plugin, operator, "manage-players")) {
            return CompletableFuture.completedFuture("{\"ok\":false,\"error\":\"Operator authorization failed.\"}");
        }

        Player target = findOnlinePlayer(targetName);
        if (target == null) {
            return CompletableFuture.completedFuture("{\"ok\":false,\"error\":\"No online player matched that exact name.\"}");
        }
        if (target.getUniqueId().equals(operator.getUniqueId())) {
            return CompletableFuture.completedFuture("{\"ok\":false,\"error\":\"You cannot kick yourself through Ali AI.\"}");
        }
        if (target.isOp()) {
            return CompletableFuture.completedFuture("{\"ok\":false,\"error\":\"Ali AI will not kick an OP player.\"}");
        }

        String safeReason = reason == null || reason.isBlank() ? "Removed by a server administrator." : reason.strip();
        if (safeReason.length() > 160) {
            return CompletableFuture.completedFuture("{\"ok\":false,\"error\":\"Kick reason must be 160 characters or fewer.\"}");
        }

        UUID id = UUID.randomUUID();
        CompletableFuture<String> result = new CompletableFuture<>();
        PendingKick pending = new PendingKick(operator.getUniqueId(), target.getUniqueId(),
                target.getName(), safeReason, result);
        pendingKicks.put(id, pending);
        showConfirmation(operator, id, pending);
        Bukkit.getScheduler().runTaskLater(plugin, () -> expire(id), 20L * 120L);
        return result;
    }

    public void handleClick(Player operator, Key identifier) {
        requirePrimaryThread();
        String prefix = "aliai:player_action/";
        String raw = identifier.asString();
        if (!raw.startsWith(prefix)) return;

        String[] parts = raw.substring(prefix.length()).split("/", 2);
        if (parts.length != 2) return;
        UUID id;
        try {
            id = UUID.fromString(parts[1]);
        } catch (IllegalArgumentException ignored) {
            return;
        }

        PendingKick pending = pendingKicks.get(id);
        if (pending == null || !pending.operatorId().equals(operator.getUniqueId())) return;
        if (!OperatorGuard.allows(plugin, operator, "command")
                || !OperatorGuard.allows(plugin, operator, "manage-players")) {
            complete(id, pending, "{\"ok\":false,\"error\":\"Operator authorization failed before confirmation.\"}");
            return;
        }

        if (parts[0].equals("cancel")) {
            complete(id, pending, "{\"ok\":false,\"cancelled\":true,\"message\":\"Kick cancelled by operator.\"}");
            return;
        }
        if (!parts[0].equals("confirm")) return;

        Player target = Bukkit.getPlayer(pending.targetId());
        if (target == null || !target.isOnline()) {
            complete(id, pending, "{\"ok\":false,\"error\":\"Target player is no longer online; no action was taken.\"}");
            return;
        }
        if (target.isOp() || target.getUniqueId().equals(operator.getUniqueId())) {
            complete(id, pending, "{\"ok\":false,\"error\":\"Target safety check failed; no action was taken.\"}");
            return;
        }

        target.kick(Component.text(pending.reason()));
        complete(id, pending, "{\"ok\":true,\"action\":\"kick\",\"player\":\"" + escapeJson(pending.targetName())
                + "\",\"message\":\"Player was kicked after operator confirmation.\"}");
    }

    public void shutdown() {
        for (Map.Entry<UUID, PendingKick> entry : pendingKicks.entrySet()) {
            complete(entry.getKey(), entry.getValue(),
                    "{\"ok\":false,\"error\":\"Ali AI stopped before the action was confirmed.\"}");
        }
    }

    private void showConfirmation(Player operator, UUID id, PendingKick pending) {
        var config = plugin.getConfig();
        String title = config.getString("ui.player-confirmation.title", "Confirm player action");
        String description = config.getString("ui.player-confirmation.description",
                "Ali AI is requesting permission to kick %player%.").replace("%player%", pending.targetName());
        String reason = config.getString("ui.player-confirmation.reason-label", "Reason: %reason%")
                .replace("%reason%", pending.reason());

        ActionButton confirm = ActionButton.create(
                Component.text(config.getString("ui.player-confirmation.confirm.label", "Confirm")),
                Component.text(config.getString("ui.player-confirmation.confirm.tooltip", "Confirm this kick")),
                120, DialogAction.customClick(Key.key("aliai:player_action/confirm/" + id), null));
        ActionButton cancel = ActionButton.create(
                Component.text(config.getString("ui.player-confirmation.cancel.label", "Cancel")),
                Component.text(config.getString("ui.player-confirmation.cancel.tooltip", "Do not kick this player")),
                120, DialogAction.customClick(Key.key("aliai:player_action/cancel/" + id), null));

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(Component.text(title))
                        .canCloseWithEscape(true)
                        .body(List.of(
                                DialogBody.plainMessage(Component.text(description)),
                                DialogBody.plainMessage(Component.text(reason))))
                        .build())
                .type(DialogType.confirmation(confirm, cancel)));
        operator.showDialog(dialog);
    }

    private void expire(UUID id) {
        PendingKick pending = pendingKicks.get(id);
        if (pending != null) {
            complete(id, pending, "{\"ok\":false,\"cancelled\":true,\"message\":\"Confirmation expired; no action was taken.\"}");
        }
    }

    private void complete(UUID id, PendingKick pending, String message) {
        if (pendingKicks.remove(id, pending)) pending.result().complete(message);
    }

    private Player findOnlinePlayer(String name) {
        if (name == null || name.isBlank() || name.length() > 16) return null;
        return Bukkit.getOnlinePlayers().stream()
                .filter(player -> player.getName().equalsIgnoreCase(name))
                .findFirst().orElse(null);
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private void requirePrimaryThread() {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("Player management must run on the server thread.");
        }
    }

    private record PendingKick(UUID operatorId, UUID targetId, String targetName,
                              String reason, CompletableFuture<String> result) { }
}
