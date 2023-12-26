package com.kousenit.openai.tts;

import javafx.application.Application;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

import java.net.URL;

public class AudioPlayer extends Application {

    @Override
    public void start(Stage stage) {
        String fileName = "audio_20231226140400.mp3";
        URL resource = getClass().getClassLoader()
                .getResource("audio/%s".formatted(fileName));
        assert resource != null;
        Media media = new Media(resource.toExternalForm());
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        mediaPlayer.totalDurationProperty().addListener(
                (obs, old, duration) -> System.out.println("Duration: " + duration));
        mediaPlayer.play();
        stage.setTitle("Playing audio");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
