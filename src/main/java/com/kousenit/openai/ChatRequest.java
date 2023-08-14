package com.kousenit.openai;

import java.util.List;

public record ChatRequest(String model,
                          List<Message> messages,
                          double temperature) {}
