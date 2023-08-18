package com.kousenit.stabilityai;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class StabilityAITest {
    private final StabilityAI stabilityAI = new StabilityAI();

    @Test
    void getBalance() {
        Balance balance = stabilityAI.getBalance();
        System.out.println(balance);
    }

    @Test
    void getEngines() {
        Engines engines = stabilityAI.getEngines();
        engines.engines().forEach(System.out::println);
    }

    @Test @Disabled("Requires credits to run")
    void sampleImage() {
        stabilityAI.generateImages("""
                Captain Kirk fights Darth Vader with a lightsaber
                on the bridge of the Starship Enterprise.
                """);
    }
}