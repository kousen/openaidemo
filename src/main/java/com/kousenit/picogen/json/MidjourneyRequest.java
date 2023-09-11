package com.kousenit.picogen.json;

public record MidjourneyRequest(int version, String model, String command,
                                String prompt, String engine) {}
