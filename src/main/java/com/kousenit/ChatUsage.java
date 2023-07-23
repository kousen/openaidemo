package com.kousenit;

public record ChatUsage(
        int promptTokens,
        int completionTokens,
        int totalTokens
) {
}
