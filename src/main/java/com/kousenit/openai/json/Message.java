package com.kousenit.openai.json;

import com.kousenit.openai.chat.Role;

public record Message(Role role, String content) {
}
