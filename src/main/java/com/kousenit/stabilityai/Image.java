package com.kousenit.stabilityai;

// base64: image data encoded in base64
// finishReason: CONTENT_FILTERED, ERROR, SUCCESS
// seed: random seed used to generate the image

public record Image(
        String base64,
        String finishReason,
        long seed
) {}