package com.kousenit.openai.json;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public record Model(String id, long created, String ownedBy) {
    @Override
    public String toString() {
        return "Model{id='%s', created=%s, ownedBy='%s'}"
                .formatted(id, fromEpoch(created), ownedBy);
    }

    public static LocalDateTime fromEpoch(long epoch) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(epoch), ZoneId.systemDefault());
    }
}
