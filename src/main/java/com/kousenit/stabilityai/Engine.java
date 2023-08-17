package com.kousenit.stabilityai;

public record Engine(
        String id,
        String name,
        String description,
        EngineType type
) {}
