package pl.sim.frontend;

import com.gluonhq.maps.MapView;
import javafx.scene.image.WritableImage;
import javafx.embed.swing.SwingFXUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class GluonMapAnalyzer {


    public static int[][] analyzeMapFromGluon(WritableImage snapshot, int gridWidth, int gridHeight) {
        try {
           // BufferedImage bufferedImage = SwingFXUtils.fromFXImage(snapshot, null);
            File tempFile = File.createTempFile("snapshot", ".png");
            ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", tempFile);


            BufferedImage bufferedImage = ImageIO.read(tempFile);
            int[][] terrainMap = new int[gridHeight][gridWidth];

            int cellWidth = bufferedImage.getWidth() / gridWidth;
            int cellHeight = bufferedImage.getHeight() / gridHeight;

            for (int y = 0; y < gridHeight; y++) {
                for (int x = 0; x < gridWidth; x++) {
                    int pixelX = x * cellWidth + cellWidth / 2;
                    int pixelY = y * cellHeight + cellHeight / 2;


                    pixelX = Math.min(pixelX, bufferedImage.getWidth() - 1);
                    pixelY = Math.min(pixelY, bufferedImage.getHeight() - 1);


                    int rgb = bufferedImage.getRGB(pixelX, pixelY);
                    int red = (rgb >> 16) & 0xFF;
                    int green = (rgb >> 8) & 0xFF;
                    int blue = rgb & 0xFF;


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

    private static int getTerrainDifficulty(int red, int green, int blue) {
        if (blue > 210 && green > 200 && red < 180) {
            return 0; // woda
        } else {
            return 1; // przejazd
        }
    }
}
