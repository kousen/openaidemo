package com.kousenit.openai;

public record ImageResponse(Long created,
                            Image[] data) {
}
