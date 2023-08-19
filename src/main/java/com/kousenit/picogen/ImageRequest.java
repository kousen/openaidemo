package com.kousenit.picogen;

// Wait until Java 21 to use pattern matching with switch
public sealed interface ImageRequest
        permits StabilityRequest, MidjourneyRequest {}
