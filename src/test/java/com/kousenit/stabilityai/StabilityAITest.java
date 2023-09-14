package com.kousenit.stabilityai;

import com.kousenit.stabilityai.json.Balance;
import com.kousenit.stabilityai.json.Engines;
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

    @Test
    void sampleImage() {
        stabilityAI.generateImages("""
                A realistic photo of a happy
                robot leaping into the air
                in joy after accomplishing a
                particularly difficult task.
                """, 4);
    }
}