package com.kousenit.openai.dalle;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DallETest {

    @Test
    public void testDallE() {
        DallE dallE = new DallE();
        long num = dallE.getImages("""
                a photorealistic image of a happy robot jumping on springs,
                thrilled that he accomplished a hard task
                """, 2);
        System.out.printf("Downloaded %d images%n", num);
        assertThat(num).isEqualTo(2);
    }

}