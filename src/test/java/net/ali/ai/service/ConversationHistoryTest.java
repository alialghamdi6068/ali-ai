package net.ali.ai.service;

import com.google.gson.JsonArray;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConversationHistoryTest {
    @Test
    void keepsOperatorConversationsSeparate() {
        ConversationHistory history = new ConversationHistory(20, 24000);
        UUID firstOperator = UUID.randomUUID();
        UUID secondOperator = UUID.randomUUID();

        history.append(firstOperator, "first question", "first answer");
        history.append(secondOperator, "second question", "second answer");

        assertEquals("first question", history.snapshot(firstOperator)
                .get(0).getAsJsonObject().get("content").getAsString());
        assertEquals("second question", history.snapshot(secondOperator)
                .get(0).getAsJsonObject().get("content").getAsString());
    }

    @Test
    void retainsNewestCompleteExchangesWithinConfiguredMessageLimit() {
        ConversationHistory history = new ConversationHistory(4, 24000);
        UUID operatorId = UUID.randomUUID();

        history.append(operatorId, "question 1", "answer 1");
        history.append(operatorId, "question 2", "answer 2");
        history.append(operatorId, "question 3", "answer 3");

        JsonArray messages = history.snapshot(operatorId);
        assertEquals(4, messages.size());
        assertEquals("question 2", messages.get(0).getAsJsonObject().get("content").getAsString());
        assertEquals("answer 3", messages.get(3).getAsJsonObject().get("content").getAsString());
    }

    @Test
    void boundsTotalStoredCharactersWithoutDroppingHalfOfTheLatestExchange() {
        ConversationHistory history = new ConversationHistory(20, 1024);
        UUID operatorId = UUID.randomUUID();

        history.append(operatorId, "q".repeat(800), "a".repeat(800));

        JsonArray messages = history.snapshot(operatorId);
        assertEquals(2, messages.size());
        assertEquals("user", messages.get(0).getAsJsonObject().get("role").getAsString());
        assertEquals("assistant", messages.get(1).getAsJsonObject().get("role").getAsString());
        int total = 0;
        for (var element : messages) {
            total += element.getAsJsonObject().get("content").getAsString().length();
        }
        assertTrue(total <= 1024);
    }

    @Test
    void snapshotsAreIndependentAndClearRemovesOnlySelectedOperator() {
        ConversationHistory history = new ConversationHistory(20, 24000);
        UUID firstOperator = UUID.randomUUID();
        UUID secondOperator = UUID.randomUUID();
        history.append(firstOperator, "question", "answer");
        history.append(secondOperator, "other question", "other answer");

        JsonArray snapshot = history.snapshot(firstOperator);
        snapshot.get(0).getAsJsonObject().addProperty("content", "modified");
        assertNotEquals("modified", history.snapshot(firstOperator)
                .get(0).getAsJsonObject().get("content").getAsString());

        history.clear(firstOperator);
        assertEquals(0, history.snapshot(firstOperator).size());
        assertEquals(2, history.snapshot(secondOperator).size());
    }

    @Test
    void roundsOddMessageLimitDownToKeepUserAssistantPairs() {
        ConversationHistory history = new ConversationHistory(5, 24000);
        UUID operatorId = UUID.randomUUID();

        history.append(operatorId, "question 1", "answer 1");
        history.append(operatorId, "question 2", "answer 2");
        history.append(operatorId, "question 3", "answer 3");

        JsonArray messages = history.snapshot(operatorId);
        assertEquals(4, messages.size());
        assertEquals("user", messages.get(0).getAsJsonObject().get("role").getAsString());
    }
}
