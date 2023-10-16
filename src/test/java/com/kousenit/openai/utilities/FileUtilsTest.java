package com.kousenit.openai.utilities;

import com.kousenit.utilities.FileUtils;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FileUtilsTest {
    @Test
    void read_file() {
        String data = FileUtils.readFile("src/main/resources/graal.srt");
        assertThat(data).contains("Graal VM")
                .hasSize(9838);
    }
}