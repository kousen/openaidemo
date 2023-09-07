package com.kousenit.openai;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DallETest {

    @Test
    public void testDallE() {
        DallE dallE = new DallE();
        int num = dallE.getImages("""
                A realistic photo of a
                robot leaping into the air
                in joy after accomplishing a
                difficult task successfully
                """, 2);
        assertThat(num).isPositive();
    }

}