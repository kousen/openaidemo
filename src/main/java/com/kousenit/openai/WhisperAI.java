package com.kousenit.openai;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

// See docs at https://platform.openai.com/docs/api-reference/audio/createTranscription

// response_format: json (default), text, srt, verbose_json, vtt
//      "text" is used here, as it returns the transcript directly
// language: ISO-639-1 code (optional)
// Rather than use multipart form data, add the file as a binary body directly
// Optional "prompt" used to give standard word spellings whisper might miss
//      If there are multiple chunks, the prompt for subsequent chunks should be the
//      transcription of the previous one (244 tokens max)
public class WhisperAI {
    private final static String URL = "https://api.openai.com/v1/audio/transcriptions";

    // Only model available as of Fall 2023 is whisper-1
    private final static String MODEL = "whisper-1";
    private final static String KEY = System.getenv("OPENAI_API_KEY");

    private final List<String> words = List.of("Kousen", "GPT-3", "GPT-4", "DALL-E",
            "Midjourney", "AssertJ", "Mockito", "JUnit", "Java", "Kotlin", "Groovy", "Scala",
            "IOException", "RuntimeException", "UncheckedIOException", "UnsupportedAudioFileException",
            "assertThrows", "assertTrue", "assertEquals");

    // file must be mp3, mp4, mpeg, mpga, m4a, wav, or webm
    // NOTE: only wav files are supported here (mp3 apparently is proprietary)

    // max size is 25MB; otherwise need to break the file into chunks
    // See the WavFileSplitter class for that
    public final static String SAMPLE_WAV_FILE = "src/main/resources/AssertJExceptions.wav";

    private final HttpPost httpPost = new HttpPost(URL);

    public boolean transcribe(String fileName) {
        File file = new File(fileName);
        httpPost.setHeader("Authorization", "Bearer %s".formatted(KEY));

        HttpEntity entity = MultipartEntityBuilder.create()
                .addBinaryBody("file", file, ContentType.DEFAULT_BINARY, file.getName())
                .addTextBody("model", MODEL, ContentType.DEFAULT_TEXT)
                .addTextBody("response_format", "text", ContentType.DEFAULT_TEXT)
                .addTextBody("language", "en", ContentType.DEFAULT_TEXT)
                .addTextBody("prompt", String.join(", ", words), ContentType.DEFAULT_TEXT)
                .build();
        httpPost.setEntity(entity);

        try (CloseableHttpClient client = HttpClients.custom().build()) {
            String result = client.execute(httpPost, resp -> EntityUtils.toString(resp.getEntity()));
            FileUtils.writeTextToFile(result, file.getName().replace(".wav", ".txt"));
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}