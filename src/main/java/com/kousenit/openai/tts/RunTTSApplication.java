package com.kousenit.openai.tts;

import com.kousenit.openai.json.Voice;

public class RunTTSApplication {
    public static void main(String[] args) {
        TextToSpeech tts = new TextToSpeech();
        try (var scanner = new java.util.Scanner(System.in)) {
            System.out.println("Enter text to convert to speech: ");
            String text = scanner.nextLine();
            tts.createAndPlay(text, Voice.getRandomVoice());
        }
    }
}
