package com.kousenit.openai;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

public record ModelList(String object, List<Model> data) {
    public record Model(String id, long created, String ownedBy) {
        @Override
        public String toString() {
            return "Model{" +
                   "id='" + id + '\'' +
                   ", created=" + fromEpoch(created) +
                   ", ownedBy='" + ownedBy + '\'' +
                   '}';
        }

        public static LocalDateTime fromEpoch(long epoch) {
            return LocalDateTime.ofInstant(Instant.ofEpochSecond(epoch), ZoneId.systemDefault());
        }
    }
}
