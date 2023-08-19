package com.kousenit.picogen;

public record MidjourneyRequest(int version, String model, String command,
                                String prompt, String engine) implements ImageRequest {}
