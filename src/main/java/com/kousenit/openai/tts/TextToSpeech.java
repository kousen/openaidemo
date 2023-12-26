package com.kousenit.openai.tts;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kousenit.openai.chat.LowercaseEnumSerializer;
import com.kousenit.openai.chat.Role;
import com.kousenit.openai.json.TTSRequest;
import com.kousenit.openai.json.Voice;
import com.kousenit.utilities.FileUtils;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

public class TextToSpeech {
    private final Logger logger = LoggerFactory.getLogger(TextToSpeech.class);

    private static final String TTS_URL = "https://api.openai.com/v1/audio/speech";
    private static final String OPENAI_API_KEY = System.getenv("OPENAI_API_KEY");

    public final static String TTS_1 = "tts-1";
    public final static String TTS_1_HD = "tts-1-hd";

    private final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapter(Role.class, new LowercaseEnumSerializer())
            .create();

    private final HttpClient client = HttpClient.newHttpClient();

    public byte[] generateMp3(TTSRequest ttsRequest) {
        String postBody = gson.toJson(ttsRequest);
        logger.debug(postBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TTS_URL))
                .header("Authorization", "Bearer %s".formatted(OPENAI_API_KEY))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(postBody))
                .build();
        try {
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            logger.debug(response.headers().toString());
            byte[] body = response.body();
            FileUtils.writeSoundBytesToFile(body);
            return body;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void playMp3UsingJLayer(String fileName) {
        BufferedInputStream buffer = new BufferedInputStream(
                Objects.requireNonNull(getClass().getClassLoader()
                        .getResourceAsStream("audio/%s".formatted(fileName))));
        try {
            Player player = new Player(buffer);
            player.play();
        } catch (JavaLayerException e) {
            throw new RuntimeException(e);
        }
    }

    public void createAndPlay(String text, Voice voice) {
        TTSRequest ttsRequest = new TTSRequest(TTS_1_HD, text, voice);
        byte[] bytes = generateMp3(ttsRequest);
        var bufferedInputStream = new BufferedInputStream(new ByteArrayInputStream(bytes));
        try {
            new Player(bufferedInputStream).play();
        } catch (JavaLayerException e) {
            throw new RuntimeException(e);
        }
    }

}
