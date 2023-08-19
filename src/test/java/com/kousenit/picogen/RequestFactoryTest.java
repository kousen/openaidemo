package com.kousenit.picogen;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class RequestFactoryTest {

    @Test
    void getPermittedSubclasses() {
        Class<?>[] classes = ImageRequest.class.getPermittedSubclasses();
        assertAll(
                () -> assertEquals(2, classes.length),
                () -> assertTrue(Arrays.asList(classes).contains(StabilityRequest.class)),
                () -> assertTrue(Arrays.asList(classes).contains(MidjourneyRequest.class))
        );
    }


}