package net.ali.ai.service;

import net.ali.ai.AliAIPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/** Tracks only currently running background jobs; no task history is persisted. */
public final class TaskManager {
    private final ExecutorService executor;
    private final Map<UUID, Future<?>> running = new ConcurrentHashMap<>();

    public TaskManager(AliAIPlugin plugin) {
        this.executor = Executors.newFixedThreadPool(2, runnable -> {
            Thread thread = new Thread(runnable, "AliAI-Worker");
            thread.setDaemon(true);
            thread.setUncaughtExceptionHandler((ignored, error) ->
                    plugin.getLogger().severe("An Ali AI background task failed: " + error.getClass().getSimpleName()));
            return thread;
        });
    }

    public UUID submit(Runnable task) {
        UUID id = UUID.randomUUID();
        Future<?> future = executor.submit(() -> {
            try {
                task.run();
            } finally {
                running.remove(id);
            }
        });
        running.put(id, future);
        return id;
    }

    public boolean cancel(UUID id) {
        Future<?> future = running.get(id);
        return future != null && future.cancel(true);
    }

    public int activeCount() {
        running.entrySet().removeIf(entry -> entry.getValue().isDone());
        return running.size();
    }

    public void shutdown() {
        executor.shutdownNow();
        running.clear();
    }
}
