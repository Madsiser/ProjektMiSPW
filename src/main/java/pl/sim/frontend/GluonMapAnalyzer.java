package pl.sim.frontend;

import com.gluonhq.maps.MapView;
import javafx.scene.image.WritableImage;
import javafx.embed.swing.SwingFXUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class GluonMapAnalyzer {

    /**
     * Analyzes the map snapshot from Gluon MapView and generates a terrain difficulty map.
     *
     * @param snapshot   The snapshot of the MapView as a WritableImage.
     * @param gridWidth  The width of the grid for the terrain map.
     * @param gridHeight The height of the grid for the terrain map.
     * @return A 2D array representing the terrain difficulty map.
     */
    public static int[][] analyzeMapFromGluon(WritableImage snapshot, int gridWidth, int gridHeight) {
        try {
            // Convert WritableImage to BufferedImage
           // BufferedImage bufferedImage = SwingFXUtils.fromFXImage(snapshot, null);
            File tempFile = File.createTempFile("snapshot", ".png");
            ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", tempFile);

            // Read the saved file back as a BufferedImage
            BufferedImage bufferedImage = ImageIO.read(tempFile);
            // Create the terrain map
            int[][] terrainMap = new int[gridHeight][gridWidth];

            // Calculate the size of each cell in the grid
            int cellWidth = bufferedImage.getWidth() / gridWidth;
            int cellHeight = bufferedImage.getHeight() / gridHeight;

            for (int y = 0; y < gridHeight; y++) {
                for (int x = 0; x < gridWidth; x++) {
                    // Calculate the center pixel of each cell
                    int pixelX = x * cellWidth + cellWidth / 2;
                    int pixelY = y * cellHeight + cellHeight / 2;

                    // Ensure pixel positions are within the image bounds
                    pixelX = Math.min(pixelX, bufferedImage.getWidth() - 1);
                    pixelY = Math.min(pixelY, bufferedImage.getHeight() - 1);

                    // Get the RGB value of the pixel
                    int rgb = bufferedImage.getRGB(pixelX, pixelY);
                    int red = (rgb >> 16) & 0xFF;
                    int green = (rgb >> 8) & 0xFF;
                    int blue = rgb & 0xFF;

                    // Map the RGB color to a terrain difficulty value
                  //  System.out.println("RGB: (" + red + ", " + green + ", " + blue + ")");
                    terrainMap[x][y] = getTerrainDifficulty(red, green, blue);
                }
            }

            return terrainMap;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error analyzing map from Gluon snapshot.");
        }
    }

    /**
     * Maps RGB values to terrain difficulty levels.
     *
     * @param red   Red component of the pixel.
     * @param green Green component of the pixel.
     * @param blue  Blue component of the pixel.
     * @return An integer representing the terrain difficulty.
     */
    private static int getTerrainDifficulty(int red, int green, int blue) {
        // Define ranges for water detection (bluish color)
        if (blue > 210 && green > 200 && red < 180) {
            return 0; // Water (e.g., ocean, river, lake)
        } else if (red > 200 && green > 200 && blue < 50) {
            return 1; // Impassable terrain
        } else if (red > 200 && green < 50 && blue < 50) {
            return 2; // Mountain
        } else if (red < 100 && green > 200 && blue < 100) {
            return 3; // Plains
        } else {
            return 4; // Neutral terrain
        }
    }
}
