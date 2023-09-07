package com.kousenit.stabilityai.json;

import com.kousenit.stabilityai.EngineType;

public record Engine(
        String id,
        String name,
        String description,
        EngineType type
) {}
