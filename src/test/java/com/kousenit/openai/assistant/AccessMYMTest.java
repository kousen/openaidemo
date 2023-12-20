package com.kousenit.openai.assistant;

import com.kousenit.openai.json.Assistant;
import com.kousenit.openai.json.Tool;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class AccessMYMTest {

    private final AccessMYM accessMYM = new AccessMYM();

    @Test
    void listAssistants() {
        String assistants = accessMYM.listAssistants();
        assertThat(assistants).isNotNull();
        System.out.println(assistants);
    }

    @Test
    void retrieveMYMAssistant() {
        Assistant assistant = accessMYM.retrieveAssistant();
        assertThat(assistant).isNotNull();
        assertAll(
                () -> assertThat(assistant.id()).isEqualTo("asst_7ttDTA3qoaaDMLeo387TPWLM"),
                () -> assertThat(assistant.object()).isEqualTo("assistant"),
                () -> assertThat(assistant.name()).isEqualTo("Managing Your Manager"),
                () -> assertThat(assistant.tools()).hasSize(1),
                () -> assertThat(assistant.tools()).extracting(Tool::type).contains("retrieval")
        );
    }

}