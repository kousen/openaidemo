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

public class WhisperAI {
    private final static String MODEL = "whisper-1";

    // file must be mp3, mp4, mpeg, mpga, m4a, wav, or webm
    // max size is 25MB
    private final static String FILE_NAME = "src/main/resources/AssertJExceptions.wav";

    public static void main(String[] args) {
        HttpPost httpPost = new HttpPost("https://api.openai.com/v1/audio/transcriptions");

        httpPost.setHeader("Authorization", "Bearer " + System.getenv("OPENAI_API_KEY"));

        File file = new File(FILE_NAME);
        // response_format: json (default), text, srt, verbose_json, vtt
        HttpEntity entity = MultipartEntityBuilder.create()
                .addBinaryBody("file", file, ContentType.DEFAULT_BINARY, file.getName())
                .addTextBody("model", MODEL, ContentType.DEFAULT_TEXT)
                .addTextBody("response_format", "text", ContentType.DEFAULT_TEXT)
                .build();
        httpPost.setEntity(entity);

        try (CloseableHttpClient client = HttpClients.custom().build()) {
            String result = client.execute(httpPost, resp -> EntityUtils.toString(resp.getEntity()));
            System.out.println(result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}