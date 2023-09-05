package com.kousenit.openai;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DallETest {

    @Test
    public void testDallE() {
        DallE dallE = new DallE();
        int num = dallE.getSingleImage("""
                A photo of a penguin wearing a Batman suit
                fighting crime in the Antarctic
                """);
        assertThat(num).isPositive();
    }

}