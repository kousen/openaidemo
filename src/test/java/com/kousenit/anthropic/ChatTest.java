package com.kousenit.anthropic;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChatTest {
    private final Chat chat = new Chat();

    @Test
    void getResponse() {
        var response = chat.getResponse(
                """
                        According to Douglas Adams, what is the Ultimate Answer
                        to the Ultimate Question of Life, the Universe, and Everything?
                        """
        );
        System.out.println(response);
        assertThat(response).contains("42");
    }
}