package com.kousenit.utilities;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ImageCarousel extends Application {
    private final StackPane root = new StackPane();
    private List<Image> images = loadImages();
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

        // Change the prompt every 3 seconds
        Duration duration = Duration.seconds(3);
        KeyFrame keyFrame = new KeyFrame(duration, e -> changeImage());
        Timeline timeline = new Timeline(keyFrame);
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        Scene scene = new Scene(root, 800, 800);
        stage.setScene(scene);
        stage.show();
    }

    private List<Image> loadImages() {
        Path path = Paths.get("src/main/resources/images");
        try (Stream<Path> paths = Files.walk(path)) {
            images = paths
                    .filter(p -> p.toString().endsWith(".png"))
                    .map(this::pathToImage)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return images;
    }

    private Image pathToImage(Path path) {
        try (InputStream is = Files.newInputStream(path)) {
            return new Image(is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void changeImage() {
        if (!imageIterator.hasNext()) {
            imageIterator = images.iterator();
        }
        imageView.setImage(imageIterator.next());
    }
}