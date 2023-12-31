package com.kousenit.stabilityai.json;

// base64: image data encoded in base64
// finishReason: CONTENT_FILTERED, ERROR, SUCCESS
// seed: random seed used to generate the image

import com.google.gson.annotations.SerializedName;
import com.kousenit.stabilityai.FinishReason;

public record Image(
        String base64,
        @SerializedName("finishReason")
        FinishReason finishReason,
        long seed
) {}