package com.kousenit.utilities;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileUtils {
    private static int counter;
    public static final String TEXT_RESOURCES_PATH = "src/main/resources/text";
    public static final String IMAGE_RESOURCES_PATH = "src/main/resources/images";
    public static final String AUDIO_RESOURCES_PATH = "src/main/resources/audio";

    static {
        try {
            Files.createDirectories(Paths.get(TEXT_RESOURCES_PATH));
            Files.createDirectories(Paths.get(IMAGE_RESOURCES_PATH));
        } catch (IOException e) {
            throw new ExceptionInInitializerError("Error creating directory");
        }
    }

    public static String readFile(String fileName) {
        try {
            return Files.readString(Path.of(fileName));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean writeImageToFile(String imageData) {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String fileName = String.format("image_%s_%d.png", timestamp, counter++);
        Path directory = Paths.get(IMAGE_RESOURCES_PATH);
        Path filePath = directory.resolve(fileName);
        try {
            byte[] bytes = Base64.getDecoder()
                    .decode(imageData);
            Files.write(filePath, bytes, StandardOpenOption.CREATE_NEW);
            System.out.printf("Saved %s to %s%n", fileName, IMAGE_RESOURCES_PATH);
            return true;
        } catch (IOException e) {
            throw new UncheckedIOException("Error writing image to file", e);
        }
    }

    public static boolean writeByteArrayToFile(byte[] bytes) {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String fileName = String.format("image_%s_%d.png", timestamp, counter++);
        Path directory = Paths.get(IMAGE_RESOURCES_PATH);
        Path filePath = directory.resolve(fileName);
        try {
            Files.write(filePath, bytes, StandardOpenOption.CREATE_NEW);
            System.out.printf("Saved %s to %s%n", fileName, IMAGE_RESOURCES_PATH);
            return true;
        } catch (IOException e) {
            throw new UncheckedIOException("Error writing image to file", e);
        }
    }

    public static void writeTextToFile(String textData, String fileName) {
        Path directory = Paths.get(TEXT_RESOURCES_PATH);
        Path filePath = directory.resolve(fileName);
        try {
            Files.deleteIfExists(filePath);
            Files.writeString(filePath, textData, StandardOpenOption.CREATE_NEW);
            System.out.printf("Saved %s to %s%n", fileName, IMAGE_RESOURCES_PATH);
        } catch (IOException e) {
            throw new UncheckedIOException("Error writing text to file", e);
        }
    }

    public static void writeWordDocument(Map<String, String> data) {
        try (XWPFDocument document = new XWPFDocument()) {
            data.forEach((key, value) -> {
                String title = transformKeyToTitle(key);
                addTitleToDocument(document, title);
                addTextToDocument(document, value);
            });

            writeDocumentToFile(document);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String transformKeyToTitle(String key) {
        return Stream.of(key.split("_"))
                .map(word -> word.substring(0, 1).toUpperCase() +
                             word.substring(1))
                .collect(Collectors.joining(" "));
    }

    private static void addTitleToDocument(XWPFDocument document, String title) {
        XWPFRun titleRun = document.createParagraph().createRun();
        titleRun.setBold(true);
        titleRun.setFontSize(16);
        titleRun.setText(title);
    }

    private static void addTextToDocument(XWPFDocument document, String text) {
        document.createParagraph()
                .createRun()
                .setText(text);
    }

    private static void writeDocumentToFile(XWPFDocument document) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(
                TEXT_RESOURCES_PATH + "/meeting_minutes.docx")) {
            document.write(fos);
        }
    }
}
