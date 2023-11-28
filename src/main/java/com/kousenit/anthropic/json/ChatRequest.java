package com.kousenit.anthropic.json;

public record ChatRequest(
        String model,
        String prompt,
        int maxTokensToSample,
        double temperature
) {
}