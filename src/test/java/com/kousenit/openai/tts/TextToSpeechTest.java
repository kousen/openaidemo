package com.kousenit.openai.tts;

import com.kousenit.openai.json.ResponseFormat;
import com.kousenit.openai.json.TTSRequest;
import com.kousenit.openai.json.Voice;
import org.junit.jupiter.api.Test;

class TextToSpeechTest {
    private final TextToSpeech tts = new TextToSpeech();

    @Test
    void generateMp3() {
        TTSRequest ttsRequest = new TTSRequest(
                TextToSpeech.TTS_1,
                "The quick brown fox jumped over the lazy dog.",
                Voice.ALLOY
        );
        tts.generateMp3(ttsRequest);
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
        tts.generateMp3(ttsRequest);
    }

    @Test
    void generateMp3HD() {
        TTSRequest ttsRequest = new TTSRequest(
                TextToSpeech.TTS_1_HD,
                """
                      The YouTube channel, "Tales from the jar side" is your best
                      source for learning about Java, Spring, and other open source
                      technologies, especially when combined with AI tools.
                      The companion newsletter is also a lot of fun.
                      """.replaceAll("\\s+", " ").trim(),
                Voice.FABLE
        );
        tts.generateMp3(ttsRequest);
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
}