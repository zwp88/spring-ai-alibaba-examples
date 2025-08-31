package org.springframework.ai.chat.client.advisor;

import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.model.ChatResponse;

import java.util.function.Predicate;

/**
 * @author yingzi
 * @since 2025/9/1
 */

public final class AdvisorUtils {
    private AdvisorUtils() {
    }

    public static Predicate<ChatClientResponse> onFinishReason() {
        return (chatClientResponse) -> {
            ChatResponse chatResponse = chatClientResponse.chatResponse();
            return chatResponse != null && chatResponse.getResults() != null && chatResponse.getResults().stream().anyMatch((result) -> result != null && result.getMetadata() != null);
        };
    }
}
