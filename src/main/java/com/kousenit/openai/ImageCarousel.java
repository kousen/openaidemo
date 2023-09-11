package com.kousenit.openai;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ImageCarousel extends Application {
    private final StackPane root = new StackPane();
    private final List<Image> images = new ArrayList<>();
    private final ImageView imageView = new ImageView();
    private Iterator<Image> imageIterator;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Image Carousel");

        loadImages();

        imageView.setPreserveRatio(true);
        imageView.setFitHeight(800);
        imageView.setFitWidth(800);
        root.getChildren().add(imageView);

        imageIterator = images.iterator();

        // Change the image every 3 seconds
        Duration duration = Duration.seconds(3);
        KeyFrame keyFrame = new KeyFrame(duration, e -> changeImage());
        Timeline timeline = new Timeline(keyFrame);
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        Scene scene = new Scene(root, 800, 800);
        stage.setScene(scene);
        stage.show();
    }

    private void loadImages() {
        Path path = Paths.get("src/main/resources/images");
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path, "*.png")) {
            for (Path entry : stream) {
                try (InputStream is = new FileInputStream(entry.toString())) {
                    images.add(new Image(is));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void changeImage() {
        if (!imageIterator.hasNext()) {
            imageIterator = images.iterator();
        }
        imageView.setImage(imageIterator.next());
    }
}