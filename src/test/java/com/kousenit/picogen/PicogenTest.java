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
    void testCreateStabilityJobRequest() {
        var request = RequestFactory.createStabilityJobRequest("A beautiful sunset", "xl-v1.0");
        assertNotNull(request);
        assertEquals("stability", request.model());
        assertEquals("A beautiful sunset", request.prompt());
        assertEquals("xl-v1.0", request.engine());
        System.out.println(request);
    }

    @Test
    void testCreateMidjourneyJobRequest() {
        var request = RequestFactory.createMidjourneyJobRequest("A beautiful sunset", "mj-5.2");
        assertNotNull(request);
        assertEquals("midjourney", request.model());
        assertEquals("A beautiful sunset", request.prompt());
        assertEquals("mj-5.2", request.engine());
        System.out.println(request);
    }

    @Test
    void testDoStabilityJob() {
        var request = RequestFactory.createStabilityJobRequest("A beautiful sunset", "xl-v1.0");
        var response = picogen.doStabilityJob(request);
        assertNotNull(response);
        System.out.println(response);
    }

    @Test
    void testDoMidjourneyJob() {
        var request = RequestFactory.createMidjourneyJobRequest("A beautiful sunset", "mj-5.2");
        var response = picogen.doMidjourneyJob(request);
        assertNotNull(response);
        System.out.println(response);
    }

    @Test
    void getResponseFromJobResponse() {
        JobResponse jobResponse = new JobResponse("12553131879563265", 16);
        var response = picogen.getResponse(jobResponse);
        assertNotNull(response);
        System.out.println(response);
    }
}