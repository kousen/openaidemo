package com.kousenit.math;

public class Buddhabrot {

    public static void main(String[] args) {
        int width = 400, height = 400;
        int maxIter = 500;
        double[][] histogram = new double[width][height];

        // Iterate through each pixel
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                double zx = x * (3.5 / width) - 2.5;
                double zy = y * (2.0 / height) - 1.0;
                double c_x = zx, c_y = zy;
                double[][] trajectory = new double[maxIter][2];
                int iter = 0;

                // Check if the point escapes
                for (; iter < maxIter; iter++) {
                    if (zx * zx + zy * zy > 4.0) {
                        break;
                    }
                    trajectory[iter][0] = zx;
                    trajectory[iter][1] = zy;
                    double tmp = zx * zx - zy * zy + c_x;
                    zy = 2.0 * zx * zy + c_y;
                    zx = tmp;
                }

                // If the point escapes, add the trajectory to the histogram
                if (iter < maxIter) {
                    for (int i = 0; i < iter; i++) {
                        int tx = (int) ((trajectory[i][0] + 2.5) * width / 3.5);
                        int ty = (int) ((trajectory[i][1] + 1.0) * height / 2.0);
                        if (tx >= 0 && tx < width && ty >= 0 && ty < height) {
                            histogram[tx][ty] += 1;
                        }
                    }
                }
            }
        }

        // Normalize the histogram (optional, depending on your rendering method)
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                histogram[x][y] = Math.log1p(histogram[x][y]);
            }
        }

        // Code to render the histogram goes here (e.g., using JavaFX or JFreeChart)
    }
}
