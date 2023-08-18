package com.kousenit.picogen;

// Wait until Java 21 to use pattern matching with switch
public sealed interface ImageRequest
        permits ImageRequest.MidjourneyRequest, ImageRequest.StabilityRequest {
    record StabilityRequest(int version, String model, String command,
                                   String prompt, String ratio, String style, String engine)
        implements ImageRequest {}
    record MidjourneyRequest(int version, String model, String command,
                                    String prompt, String engine)
        implements ImageRequest {}
}
