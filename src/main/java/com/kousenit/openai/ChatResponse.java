package com.kousenit.openai;

import java.util.List;

public record ChatResponse(String id,
                           String object,
                           long created,
                           String model,
                           ChatUsage usage,
                           List<ChatResponseChoice> choices) {

    public record ChatUsage(
            int promptTokens,
            int completionTokens,
            int totalTokens
    ) {}

    public record ChatResponseChoice(
            Message message,
            int index,
            String finishReason
    ) {}

}
