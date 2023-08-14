package com.kousenit.openai;

public enum Role {
    USER, SYSTEM, ASSISTANT;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}