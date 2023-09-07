package com.kousenit.openai;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DallETest {

    @Test
    public void testDallE() {
        DallE dallE = new DallE();
        long num = dallE.getImages("""
                A realistic photo of a
                robot leaping into the air
                in joy after accomplishing a
                difficult task successfully
                """, 2);
        System.out.printf("Downloaded %d images%n", num);
        assertThat(num).isEqualTo(2);
    }

}