package com.kousenit.openai.json;

import com.kousenit.openai.Role;

public record Message(Role role, String content) {
}
