package com.kousenit.openai.json;

// prompt has max length of 1000 characters
// n defaults to 1, which happens if n is null. Otherwise must be between 1 and 10.
//   Note: for dall-e-3, only n = 1 is supported
// size defaults to 1024x1024. Also valid are 256x256 and 512x512 (dall-e-2 only)
// model is "dall-e-2" or "dall-e-3"
// quality is "standard" (default) or "hd" (dall-e-3 only)
// NOTE: response_format can be "url" or "b64_json". Default is url.
public record ImageRequest(
        String model,
        String prompt,
        Integer n,
        String quality,
        String size,
        String responseFormat) {

    public ImageRequest {
        if (n != null && (n < 1 || n > 10)) {
            throw new IllegalArgumentException("n must be between 1 and 10");
        }
        if (!model.equals("dall-e-2") && !model.equals("dall-e-3")) {
            throw new IllegalArgumentException("model must be dall-e-2 or dall-e-3");
        }
        if (!quality.equals("standard") && !quality.equals("hd")) {
            throw new IllegalArgumentException("quality must be standard or hd");
        }
        if (!size.equals("256x256") && !size.equals("512x512") && !size.equals("1024x1024")) {
            throw new IllegalArgumentException("size must be 256x256, 512x512, or 1024x1024");
        }
        if (!responseFormat.equals("url") && !responseFormat.equals("b64_json")) {
            throw new IllegalArgumentException("responseFormat must be url or b64_json");
        }
    }
}
