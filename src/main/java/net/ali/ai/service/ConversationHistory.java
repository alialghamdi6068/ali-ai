package net.ali.ai.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Keeps a bounded, in-memory conversation context for each operator.
 * Messages are deliberately not persisted to the server filesystem.
 */
public final class ConversationHistory {
    private static final int MAX_SINGLE_MESSAGE_CHARACTERS = 12000;

    private final int maximumMessages;
    private final int maximumCharacters;
    private final ConcurrentMap<UUID, Deque<Message>> conversations = new ConcurrentHashMap<>();

    public ConversationHistory(int maximumMessages, int maximumCharacters) {
        if (maximumMessages < 2) {
            throw new IllegalArgumentException("maximumMessages must be at least 2.");
        }
        if (maximumCharacters < 1024) {
            throw new IllegalArgumentException("maximumCharacters must be at least 1024.");
        }
        this.maximumMessages = maximumMessages - maximumMessages % 2;
        this.maximumCharacters = maximumCharacters;
    }

    public JsonArray snapshot(UUID operatorId) {
        Deque<Message> messages = conversations.get(operatorId);
        JsonArray snapshot = new JsonArray();
        if (messages == null) {
            return snapshot;
        }

        synchronized (messages) {
            for (Message message : messages) {
                JsonObject entry = new JsonObject();
                entry.addProperty("role", message.role());
                entry.addProperty("content", message.content());
                snapshot.add(entry);
            }
        }
        return snapshot;
    }

    public void append(UUID operatorId, String userMessage, String assistantMessage) {
        if (operatorId == null) {
            throw new IllegalArgumentException("operatorId cannot be null.");
        }
        if (userMessage == null || userMessage.isBlank()
                || assistantMessage == null || assistantMessage.isBlank()) {
            return;
        }

        Deque<Message> messages = conversations.computeIfAbsent(operatorId, ignored -> new ArrayDeque<>());
        synchronized (messages) {
            messages.addLast(new Message("user", limit(userMessage)));
            messages.addLast(new Message("assistant", limit(assistantMessage)));

            while (messages.size() > maximumMessages) {
                messages.removeFirst();
                messages.removeFirst();
            }
            trimToCharacterLimit(messages);
        }
    }

    public void clear(UUID operatorId) {
        if (operatorId != null) {
            conversations.remove(operatorId);
        }
    }

    private void trimToCharacterLimit(Deque<Message> messages) {
        int totalCharacters = 0;
        for (Message message : messages) {
            totalCharacters += message.content().length();
        }
        while (totalCharacters > maximumCharacters && messages.size() > 2) {
            totalCharacters -= messages.removeFirst().content().length();
            totalCharacters -= messages.removeFirst().content().length();
        }
        if (totalCharacters > maximumCharacters && messages.size() == 2) {
            Message user = messages.removeFirst();
            Message assistant = messages.removeLast();
            int userLimit = maximumCharacters / 2;
            messages.addLast(new Message("user", limit(user.content(), userLimit)));
            messages.addLast(new Message("assistant",
                    limit(assistant.content(), maximumCharacters - userLimit)));
        }
    }

    private String limit(String value) {
        return limit(value, MAX_SINGLE_MESSAGE_CHARACTERS);
    }

    private String limit(String value, int maximumLength) {
        return value.length() <= maximumLength ? value : value.substring(0, maximumLength);
    }

    private record Message(String role, String content) { }
}
