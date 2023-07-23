package com.kousenit;

import java.util.List;

public record ChatResponse(String id,
                           String object,
                           long created,
                           String model,
                           ChatUsage usage,
                           List<ChatResponseChoice> choices) {
}
