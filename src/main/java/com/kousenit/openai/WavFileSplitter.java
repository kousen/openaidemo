package com.kousenit.openai;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WavFileSplitter {
    private static final long MAX_CHUNK_SIZE_BYTES = 20 * 1024 * 1024;

    public List<byte[]> splitWavFileIntoChunks(File sourceWavFile)
            throws UnsupportedAudioFileException, IOException {

        // maximum chunk size of 25MB; use 20MB to be safe

        AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(sourceWavFile);
        AudioFormat format = fileFormat.getFormat();

        // Calculate the maximum number of frames for each chunk
        int frameSize = format.getFrameSize(); // Number of bytes in each frame
        long framesPerChunk = MAX_CHUNK_SIZE_BYTES / frameSize;

        List<byte[]> chunks = new ArrayList<>();
        try (var inputStream = AudioSystem.getAudioInputStream(sourceWavFile)) {
            long totalFrames = inputStream.getFrameLength(); // Total frames in the source wav file

            byte[] buffer = new byte[(int) (framesPerChunk * frameSize)];

            while (totalFrames > 0) {
                long framesInThisFile = Math.min(totalFrames, framesPerChunk);
                int bytesRead = inputStream.read(buffer, 0, (int) (framesInThisFile * frameSize));
                if (bytesRead > 0) {
                    try (var baos = new ByteArrayOutputStream()) {
                        try (var partStream = new AudioInputStream(
                                new ByteArrayInputStream(buffer, 0, bytesRead),
                                format,
                                framesInThisFile)) {
                            AudioSystem.write(partStream, AudioFileFormat.Type.WAVE, baos);
                        }
                        chunks.add(baos.toByteArray());
                    }
                }
                totalFrames -= framesInThisFile;
            }
        }
        return chunks; // returns the chunks as byte[]
    }
}