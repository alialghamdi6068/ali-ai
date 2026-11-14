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
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/** Inspects loaded worlds and gates world-time changes behind explicit operator confirmation. */
public final class WorldManagementService {
    private final AliAIPlugin plugin;
    private final Map<UUID, PendingTimeChange> pendingChanges = new ConcurrentHashMap<>();

    public WorldManagementService(AliAIPlugin plugin) {
        this.plugin = plugin;
    }

    public String inspectWorld(String name) {
        requirePrimaryThread();
        World world = findLoadedWorld(name);
        if (world == null) return "No loaded world matched that exact name.";

        return """
                World details
                Name: %s
                UUID: %s
                Environment: %s
                Difficulty: %s
                Time: %d ticks
                Weather: %s
                Thundering: %s
                Players: %d
                Spawn: %d, %d, %d
                """.formatted(
                world.getName(), world.getUID(), world.getEnvironment(), world.getDifficulty(),
                world.getTime(), world.hasStorm() ? "rain" : "clear", world.isThundering(),
                world.getPlayers().size(), world.getSpawnLocation().getBlockX(),
                world.getSpawnLocation().getBlockY(), world.getSpawnLocation().getBlockZ());
    }

    public CompletableFuture<String> requestTimeChange(Player operator, String worldName, long time) {
        requirePrimaryThread();
        if (!operator.isOnline() || !OperatorGuard.allows(plugin, operator, "command")
                || !OperatorGuard.allows(plugin, operator, "manage-worlds")) {
            return CompletableFuture.completedFuture(error("Operator authorization failed."));
        }
        if (time < 0 || time >= 24_000) {
            return CompletableFuture.completedFuture(error("World time must be between 0 and 23999 ticks."));
        }

        World world = findLoadedWorld(worldName);
        if (world == null) {
            return CompletableFuture.completedFuture(error("No loaded world matched that exact name."));
        }

        long operatorPending = pendingChanges.values().stream()
                .filter(pending -> pending.operatorId().equals(operator.getUniqueId()))
                .count();
        if (pendingChanges.size() >= 20 || operatorPending >= 3) {
            return CompletableFuture.completedFuture(error("Too many pending world confirmations. Resolve an existing dialog first."));
        }

        UUID id = UUID.randomUUID();
        CompletableFuture<String> result = new CompletableFuture<>();
        PendingTimeChange pending = new PendingTimeChange(operator.getUniqueId(), world.getUID(),
                world.getName(), time, result);
        pendingChanges.put(id, pending);
        showConfirmation(operator, id, pending);
        Bukkit.getScheduler().runTaskLater(plugin, () -> expire(id), 20L * 120L);
        return result;
    }

    public void handleClick(Player operator, Key identifier) {
        requirePrimaryThread();
        String prefix = "aliai:world_action/";
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

        PendingTimeChange pending = pendingChanges.get(id);
        if (pending == null || !pending.operatorId().equals(operator.getUniqueId())) return;
        if (!OperatorGuard.allows(plugin, operator, "command")
                || !OperatorGuard.allows(plugin, operator, "manage-worlds")) {
            complete(id, pending, error("Operator authorization failed before confirmation."));
            return;
        }

        if (parts[0].equals("cancel")) {
            complete(id, pending, cancelled("World time change cancelled by operator."));
            return;
        }
        if (!parts[0].equals("confirm")) return;

        World world = Bukkit.getWorld(pending.worldId());
        if (world == null) {
            complete(id, pending, error("World is no longer loaded; no change was made."));
            return;
        }
        world.setTime(pending.time());
        complete(id, pending, success(pending));
    }

    public void shutdown() {
        for (Map.Entry<UUID, PendingTimeChange> entry : pendingChanges.entrySet()) {
            complete(entry.getKey(), entry.getValue(),
                    error("Ali AI stopped before the world change was confirmed."));
        }
    }

    private void showConfirmation(Player operator, UUID id, PendingTimeChange pending) {
        var config = plugin.getConfig();
        String title = config.getString("ui.world-confirmation.title", "Confirm world change");
        String description = config.getString("ui.world-confirmation.description",
                "Set the time in %world% to %time% ticks?")
                .replace("%world%", pending.worldName())
                .replace("%time%", Long.toString(pending.time()));
        String hint = config.getString("ui.world-confirmation.hint",
                "0 = sunrise, 6000 = noon, 12000 = sunset, 18000 = midnight.");

        ActionButton confirm = ActionButton.create(
                Component.text(config.getString("ui.world-confirmation.confirm.label", "Confirm")),
                Component.text(config.getString("ui.world-confirmation.confirm.tooltip", "Apply this world time")),
                120, DialogAction.customClick(Key.key("aliai:world_action/confirm/" + id), null));
        ActionButton cancel = ActionButton.create(
                Component.text(config.getString("ui.world-confirmation.cancel.label", "Cancel")),
                Component.text(config.getString("ui.world-confirmation.cancel.tooltip", "Do not change the world time")),
                120, DialogAction.customClick(Key.key("aliai:world_action/cancel/" + id), null));

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(Component.text(title))
                        .canCloseWithEscape(true)
                        .body(List.of(DialogBody.plainMessage(Component.text(description)),
                                DialogBody.plainMessage(Component.text(hint))))
                        .build())
                .type(DialogType.confirmation(confirm, cancel)));
        operator.showDialog(dialog);
    }

    private World findLoadedWorld(String name) {
        if (name == null || name.isBlank() || name.length() > 64) return null;
        return Bukkit.getWorlds().stream()
                .filter(world -> world.getName().equalsIgnoreCase(name))
                .findFirst().orElse(null);
    }

    private void expire(UUID id) {
        PendingTimeChange pending = pendingChanges.get(id);
        if (pending != null) complete(id, pending, cancelled("Confirmation expired; no world change was made."));
    }

    private void complete(UUID id, PendingTimeChange pending, String response) {
        if (pendingChanges.remove(id, pending)) pending.result().complete(response);
    }

    private String success(PendingTimeChange pending) {
        com.google.gson.JsonObject result = new com.google.gson.JsonObject();
        result.addProperty("ok", true);
        result.addProperty("action", "set_world_time");
        result.addProperty("world", pending.worldName());
        result.addProperty("time", pending.time());
        result.addProperty("message", "World time changed after operator confirmation.");
        return result.toString();
    }

    private String cancelled(String message) {
        com.google.gson.JsonObject result = new com.google.gson.JsonObject();
        result.addProperty("ok", false);
        result.addProperty("cancelled", true);
        result.addProperty("message", message);
        return result.toString();
    }

    private String error(String message) {
        com.google.gson.JsonObject result = new com.google.gson.JsonObject();
        result.addProperty("ok", false);
        result.addProperty("error", message);
        return result.toString();
    }

    private void requirePrimaryThread() {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("World management must run on the server thread.");
        }
    }

    private record PendingTimeChange(UUID operatorId, UUID worldId, String worldName, long time,
                                     CompletableFuture<String> result) { }
}
