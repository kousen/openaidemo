package com.kousenit.anthropic.json;

public record ChatResponse(String completion, String stopReason, String model) {}