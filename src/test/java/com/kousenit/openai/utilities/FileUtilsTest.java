package com.kousenit.openai.utilities;

import com.kousenit.utilities.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FileUtilsTest {
    @Test
    void read_file() {
        String data = FileUtils.readFile("src/main/resources/graal.srt");
        assertThat(data).contains("Graal VM")
                .hasSize(9838);
    }

    @Test
    void writeWordDocument() throws IOException {
        String[] fileNames = {"action_items", "key_points", "summarize", "sentiment"};
        Map<String, String> fileContents = new HashMap<>();

        for (String fileName : fileNames) {
            String content = Files.readString(
                    Paths.get(FileUtils.TEXT_RESOURCES_PATH + "/" + fileName + ".txt"));
            fileContents.put(fileName, content);
        }

        FileUtils.writeWordDocument(fileContents);
        System.out.println("Wrote meeting_minutes.docx to " + FileUtils.TEXT_RESOURCES_PATH);
    }
}