package com.kousenit.openai.json;

// prompt has max length of 1000 characters
// n defaults to 1, which happens if n is null. Otherwise must be between 1 and 10.
//   Note: for dall-e-3, only n = 1 is supported
// size defaults to 1024x1024. Also valid are 256x256 and 512x512 (dall-e-2 only)
// model is "dall-e-2" or "dall-e-3"
// quality is "standard" (default) or "hd" (dall-e-3 only)
// NOTE: response_format can be "url" or "b64_json". Default is url.
public record ImageRequest(String model,
                           String prompt,
                           Integer n,
                           String quality,
                           String size,
                           String responseFormat) {
}
