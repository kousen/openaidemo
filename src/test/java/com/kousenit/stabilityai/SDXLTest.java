package com.kousenit.stabilityai;

import com.kousenit.stabilityai.json.Balance;
import com.kousenit.stabilityai.json.Engines;
import org.junit.jupiter.api.Test;

class SDXLTest {
    private final SDXL SDXL = new SDXL();

    @Test
    void getBalance() {
        Balance balance = SDXL.getBalance();
        System.out.println(balance);
    }

    @Test
    void getEngines() {
        Engines engines = SDXL.getEngines();
        engines.engines().forEach(System.out::println);
    }

    @Test
    void sampleImage() {
        SDXL.generateImages("""
                A realistic photo of a happy
                robot leaping into the air
                in joy after accomplishing a
                particularly difficult task.
                """, 4);
    }

    @Test
    void stableImage() throws Exception {
        SDXL.requestStableImage("""
                Cats playing gin rummy
                """);
    }
}