package com.kousenit.openai;

// prompt has max length of 1000 characters
// number defaults to 1, which happens if number is null. Must be between 1 and 10.
// size defaults to 1024x1024. Also valid are 256x256 and 512x512
// NOTE: response_format can be "url" or "b64_json". Default is url.
public record ImagePrompt(String prompt,
                          Integer n,
                          String size) {
}
