package com.kousenit.picogen;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class ImageDownloader extends Application {
    private final Picogen picogen = new Picogen();

    public static void main(String[] args) {
        launch(args);
    }

    // NOTE: Execute this from the "./gradlew run" command
    @Override
    public void start(Stage stage) throws IOException, URISyntaxException {
        List<String> jobList = picogen.getJobList();

        // URL of image to download
        // NOTE: URL constructor is deprecated in Java 20. Replacing with
        // URI.toURL(String) to prepare for Java 21.
        URL url = new URI(jobList.get(0)).toURL();

        // Download image from URL
        Image image = new Image(url.openStream());

        // Create ImageView to display image
        ImageView imageView = new ImageView(image);

        // Create scene and display image
        StackPane root = new StackPane();
        root.getChildren().add(imageView);
        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.show();
    }
}