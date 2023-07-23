package com.kousenit;

public record ChatResponseChoice(
        Message message,
        int index,
        String finishReason
) {}
