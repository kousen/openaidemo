package com.kousenit.picogen;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PicogenTest {
    private final Picogen picogen = new Picogen();

    @Test
    void testGetJobList() {
        var jobList = picogen.getJobList();
        assertNotNull(jobList);
        assertFalse(jobList.isEmpty());
        System.out.println(jobList);
    }

    @Test
    void testCreateJobRequest() {
        var request = picogen.createJobRequest("stability", "A beautiful sunset", "xl-v1.0");
        assertNotNull(request);
        assertEquals("stability", request.model());
        assertEquals("A beautiful sunset", request.prompt());
        assertEquals("xl-v1.0", request.engine());
        System.out.println(request);
    }

    @Test
    void testDoJob() {
        var request = picogen.createJobRequest("stability", "A beautiful sunset", "xl-v1.0");
        var response = picogen.doJob(request);
        assertNotNull(response);
        System.out.println(response.id());
        System.out.println(response.cost());
        System.out.println(response);
    }

}