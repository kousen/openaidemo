package com.kousenit.openai.chat;

import com.google.gson.*;

import java.lang.reflect.Type;

public class LowercaseEnumSerializer
        implements JsonSerializer<Role>, JsonDeserializer<Role> {
    @Override
    public JsonElement serialize(Role role, Type type,
                                 JsonSerializationContext context) {
        return new JsonPrimitive(role.toString().toLowerCase());
    }

    @Override
    public Role deserialize(JsonElement json, Type typeOfT,
                            JsonDeserializationContext context)
            throws JsonParseException {
        try {
            return Role.valueOf(json.getAsString().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new JsonParseException(e);
        }
    }
}
