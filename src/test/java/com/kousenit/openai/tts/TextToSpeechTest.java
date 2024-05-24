package com.kousenit.openai.tts;

import com.kousenit.openai.json.ResponseFormat;
import com.kousenit.openai.json.TTSRequest;
import com.kousenit.openai.json.Voice;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.assertj.core.api.Assertions.assertThat;

class TextToSpeechTest {
    private final TextToSpeech tts = new TextToSpeech();

    @Test
    void generateMp3_v2() {
        TTSRequest ttsRequest = new TTSRequest(
                TextToSpeech.TTS_1,
                "The quick brown fox jumped over the lazy dog.",
                Voice.ALLOY
        );
        tts.generateMp3_v2(ttsRequest);
    }

    @Test
    void generateFasterMp3() {
        TTSRequest ttsRequest = new TTSRequest(
                TextToSpeech.TTS_1,
                "The quick brown fox jumped over the lazy dog.",
                Voice.ALLOY,
                ResponseFormat.MP3,
                2.0
        );
        tts.generateMp3_v2(ttsRequest);
    }

    @Test
    void generateMp3V2HD() {
        TTSRequest ttsRequest = new TTSRequest(
                TextToSpeech.TTS_1_HD,
                """
                        The YouTube channel, "Tales from the jar side" is your best
                        source for learning about Java, Spring, and other open source
                        technologies, especially when combined with AI tools.
                        The companion newsletter is also a lot of fun.
                        """.replaceAll("\\s+", " ")
                        .trim(),
                Voice.FABLE
        );
        tts.generateMp3_v2(ttsRequest);
    }

    @Test
    void playMp3UsingJLayer() {
        tts.playMp3UsingJLayer("tftjs.mp3");
    }

    @Test
    void generate_and_play_mp3() {
        tts.createAndPlay("""
                        The YouTube channel, "Tales from the jar side" is your best
                        source for learning about Java, Spring, and other open source
                        technologies, especially when combined with AI tools.
                        The companion newsletter is also a lot of fun.
                        """,
                Voice.getRandomVoice());
    }

    @Test
    void generateMp3_v2_v1() throws JavaLayerException {
        byte[] bytes = tts.generateMp3_v1("tts-1",
                """
                        The YouTube channel, 'Tales from the jar side' is your best
                        source for learning about Java, Spring, and other open source
                        technologies, especially when combined with AI tools.
                        The companion newsletter is also a lot of fun.
                        """,
                "fable");
        assertThat(bytes.length).isPositive();
        System.out.println("bytes.length = " + bytes.length);
        var player = new Player(new ByteArrayInputStream(bytes));
        player.play();
    }
}