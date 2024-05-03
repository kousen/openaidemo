package com.kousenit.math;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;


// Written by ChatGPT with the code interpreter functionality
public class MandelbrotSetGenerator {
    public static void main(String[] args) {
        int width = 800; // Image width
        int height = 800; // Image height
        int maxIterations = 1000; // Maximum iterations for each pixel

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        double minReal = -2.0;
        double maxReal = 1.0;
        double minImaginary = -1.5;
        double maxImaginary = 1.5;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double real = minReal + x * (maxReal - minReal) / (width - 1);
                double imaginary = minImaginary + y * (maxImaginary - minImaginary) / (height - 1);

                int iterations = computeMandelbrotIterations(real, imaginary, maxIterations);

                Color color = Color.BLACK;
                if (iterations < maxIterations) {
                    float hue = (float) iterations / maxIterations;
                    color = Color.getHSBColor(hue, 1.0f, 1.0f);
                }

                image.setRGB(x, y, color.getRGB());
            }
        }

        try {
            File outputImage = new File("MandelbrotSet.png");
            ImageIO.write(image, "png", outputImage);
            System.out.println("Mandelbrot set prompt generated and saved as MandelbrotSet.png");
        } catch (Exception e) {
            System.err.println("Error while saving the prompt: " + e.getMessage());
        }
    }

    private static int computeMandelbrotIterations(double real, double imaginary, int maxIterations) {
        double zReal = real;
        double zImaginary = imaginary;
        int iterations = 0;

        while (iterations < maxIterations && zReal * zReal + zImaginary * zImaginary < 4.0) {
            double newReal = zReal * zReal - zImaginary * zImaginary + real;
            double newImaginary = 2.0 * zReal * zImaginary + imaginary;
            zReal = newReal;
            zImaginary = newImaginary;
            iterations++;
        }

        return iterations;
    }
}
