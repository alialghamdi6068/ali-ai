package net.ali.ai.service;

import net.ali.ai.AliAIPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;

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
        FutureTask<Void> future = new FutureTask<>(() -> {
            try {
                task.run();
            } finally {
                running.remove(id);
            }
            return null;
        });
        running.put(id, future);
        try {
            executor.execute(future);
        } catch (RejectedExecutionException exception) {
            running.remove(id);
            throw exception;
        }
        return id;
    }

    public <T> CompletableFuture<T> submit(Callable<T> task) {
        UUID id = UUID.randomUUID();
        CompletableFuture<T> result = new CompletableFuture<>();
        FutureTask<Void> future = new FutureTask<>(() -> {
            try {
                result.complete(task.call());
            } catch (Throwable failure) {
                result.completeExceptionally(failure);
                if (failure instanceof Error error) {
                    throw error;
                }
            } finally {
                running.remove(id);
            }
            return null;
        });
        running.put(id, future);
        try {
            executor.execute(future);
        } catch (RejectedExecutionException exception) {
            running.remove(id);
            result.completeExceptionally(exception);
        }
        return result;
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
