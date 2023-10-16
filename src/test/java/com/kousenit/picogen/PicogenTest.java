package com.kousenit.picogen;

import com.kousenit.picogen.json.JobResponse;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Test @Disabled("Only run when you have credits")
    void testDoStabilityJob() {
        var request = RequestFactory.createStabilityJobRequest(
                "A beautiful sunset", "xl-v1.0");
        var response = picogen.doStabilityJob(request);
        assertNotNull(response);
        System.out.println(response);
    }

    @Test @Disabled("Only run when you have credits")
    void testDoMidjourneyJob() {
        var request = RequestFactory.createMidjourneyJobRequest(
                "A beautiful sunset", "mj-5.2");
        var response = picogen.doMidjourneyJob(request);
        assertNotNull(response);
        System.out.println(response);
    }

    @Test @Disabled("Replace placeholder id with your own valid id")
    void getResponseFromJobResponse() {
        JobResponse jobResponse = new JobResponse("12553131879563265", 16);
        var response = picogen.getResponse(jobResponse);
        assertNotNull(response);
        System.out.println(response);
    }

    @Test @Disabled("Working now -- no need to run it again")
    void saveImagesToFiles() {
        List<String> urls = List.of(
                "https://api.picogen.io/files/202310/15/892cd09c69baff3e7e3394adc73162da.png",
                "https://api.picogen.io/files/202310/15/e4c5e07d6b2a2ccb98b2999beff34410.png",
                "https://api.picogen.io/files/202310/15/c8d54e73e78c3fdcececf4aba76482c5.png",
                "https://api.picogen.io/files/202310/15/aa3a373e322c6c68c652c00306541a8b.png"
        );
        long count = picogen.saveImagesToFiles(urls);
        assertEquals(4, count);
        assertThat(Paths.get("src/main/resources/images").toFile()
                .listFiles(file -> file.getName().endsWith(".png")))
                .allMatch(file -> file.getName().endsWith(".png"));
    }
}