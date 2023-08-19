package com.kousenit.picogen;

public record StabilityRequest(int version, String model, String command,
                               String prompt, String ratio, String style, String engine) implements ImageRequest {}
