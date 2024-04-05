package com.kousenit.openai.json;

import java.util.List;

public record ModelList(String object, List<Model> data) {
}
