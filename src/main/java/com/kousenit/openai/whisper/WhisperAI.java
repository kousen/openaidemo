package com.kousenit.openai.whisper;

import com.kousenit.openai.FileUtils;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// See docs at https://platform.openai.com/docs/api-reference/audio/createTranscription

// response_format: json (default), text, srt, verbose_json, vtt
//      "text" is used here, as it returns the transcript directly
// language: ISO-639-1 code (optional)
// Rather than use multipart form data, add the file as a binary body directly
// Optional "prompt" used to give standard word spellings whisper might miss
//      If there are multiple chunks, the prompt for subsequent chunks should be the
//      transcription of the previous one (244 tokens max)

// file must be mp3, mp4, mpeg, mpga, m4a, wav, or webm
// NOTE: only wav files are supported here (mp3 apparently is proprietary)

// max size is 25MB; otherwise need to break the file into chunks
// See the WavFileSplitter class for that

public class WhisperAI {
    private final static String URL = "https://api.openai.com/v1/audio/transcriptions";

    // Only model available as of Fall 2023 is whisper-1
    private final static String MODEL = "whisper-1";

    private final static String KEY = System.getenv("OPENAI_API_KEY");

    private final static int MAX_CHUNK_SIZE_BYTES = 20 * 1024 * 1024;

    public static final String WORD_LIST = String.join(", ",
            List.of("Kousen", "GPT-3", "GPT-4", "DALL-E",
                    "Midjourney", "AssertJ", "Mockito", "JUnit", "Java", "Kotlin", "Groovy", "Scala",
                    "IOException", "RuntimeException", "UncheckedIOException", "UnsupportedAudioFileException",
                    "assertThrows", "assertTrue", "assertEquals", "assertNull", "assertNotNull", "assertThat",
                    "Tales from the jar side", "Spring Boot", "Spring Framework", "Spring Data", "Spring Security"));

    private String transcribeChunk(String prompt, File chunkFile) {
        HttpPost httpPost = new HttpPost(URL);
        httpPost.setHeader("Authorization", "Bearer %s".formatted(KEY));

        System.out.println("Transcribing " + chunkFile.getName());
        HttpEntity entity = MultipartEntityBuilder.create()
                .addBinaryBody("file", chunkFile, ContentType.DEFAULT_BINARY, chunkFile.getName())
                .addTextBody("model", MODEL, ContentType.DEFAULT_TEXT)
                .addTextBody("response_format", "text", ContentType.DEFAULT_TEXT)
                .addTextBody("language", "en", ContentType.DEFAULT_TEXT)
                .addTextBody("prompt", prompt, ContentType.DEFAULT_TEXT)
                .build();
        httpPost.setEntity(entity);

        try (CloseableHttpClient client = HttpClients.custom().build()) {
            return client.execute(httpPost, resp -> EntityUtils.toString(resp.getEntity()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean transcribe(String fileName) {
        File file = new File(fileName);
        List<String> transcriptions = new ArrayList<>();
        if (file.length() <= MAX_CHUNK_SIZE_BYTES) {
            String transcription = transcribeChunk(WORD_LIST, file);
            transcriptions.add(transcription);
        } else {
            String prompt = WORD_LIST;
            var splitter = new WavFileSplitter();
            List<File> chunks = splitter.splitWavFileIntoChunks(file);
            for (File chunk : chunks) {
                prompt = transcribeChunk(prompt, chunk);
                transcriptions.add(prompt);
                if(!chunk.delete()) {
                    System.out.println("Failed to delete " + chunk.getName());
                }
            }
        }
        String fileNameWithoutPath = fileName.substring(fileName.lastIndexOf("/") + 1);
        FileUtils.writeTextToFile(String.join(" ", transcriptions),
                fileNameWithoutPath.replace(".wav", ".txt"));
        return true;
    }
}