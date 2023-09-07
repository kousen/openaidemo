package com.kousenit.openai;

import java.util.List;

public record ImageResponse(Long created,
                            List<Image> data) {
}
