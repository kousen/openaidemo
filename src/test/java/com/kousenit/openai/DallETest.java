package com.kousenit.openai;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DallETest {

    @Test
    public void testDallE() {
        DallE dallE = new DallE();
        String url = dallE.getResponse("""
                A penguin wearing a sombrero
                uses the Force to raise a sunken ship
                from the ocean floor in the Bermuda Triangle.
                """);
        System.out.println("Downloaded image from " + url +
                           " is stored in src/main/resources/image.png");
        assertThat(url).hasSizeGreaterThan(0);
    }

}