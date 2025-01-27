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


                  //    System.out.println("RGB: (x"+x +"y"+y + red + ", " + green + ", " + blue + ")");
                    terrainMap[x][y] = getTerrainDifficulty(red, green, blue);
                }
            }

            return terrainMap;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error analyzing map from Gluon snapshot.");
        }
    }


    private static final int[] ROAD_COLOR = {128, 128, 128};
    public static final int ROAD_VALUE = 429496729;
    private static final int[] GRASS_COLOR = {215, 239, 192};
    public static final int GRASS_VALUE = 536870911;
    private static final int[] FOREST_COLOR = {34, 139, 34};
    public static final int FOREST_VALUE = 715827882;

    private static final int[] DESERT_COLOR = {237, 201, 175};
    public static final int DESERT_VALUE = 1073741823;
    private static final int[] MOUNTAIN_COLOR = {247, 237, 209};
    public static final int MOUNTAIN_VALUE = 2147483647;


    private static final int[] VALLEY_COLOR = {173, 209, 158};
    public static final int VALLEY_VALUE = 357913941; // Przykładowa wartość, możesz dostosować

    private static final int COLOR_TOLERANCE = 15;

    private static int getTerrainDifficulty(int red, int green, int blue) {
        if (blue > 210 && green > 200 && red < 180) {
            return 0; // Woda
        } else if (isColorMatch(red, green, blue, GRASS_COLOR)) {
            return GRASS_VALUE; // Trawa
        } else if (isColorMatch(red, green, blue, MOUNTAIN_COLOR)) {
            return MOUNTAIN_VALUE; // Góry
        } else if (isColorMatch(red, green, blue, FOREST_COLOR)) {
            return FOREST_VALUE; // Las
        } else if (isColorMatch(red, green, blue, ROAD_COLOR)) {
            return ROAD_VALUE; // Droga
        } else if (isColorMatch(red, green, blue, DESERT_COLOR)) {
            return DESERT_VALUE; // Pustynia
        } else if (isColorMatch(red, green, blue, VALLEY_COLOR)) {
            return VALLEY_VALUE; // Doliny
        } else {
            return ROAD_VALUE; // Przejazd (domyślny teren)
        }
    }

    private static boolean isColorMatch(int red, int green, int blue, int[] targetColor) {
        return Math.abs(red - targetColor[0]) <= COLOR_TOLERANCE &&
                Math.abs(green - targetColor[1]) <= COLOR_TOLERANCE &&
                Math.abs(blue - targetColor[2]) <= COLOR_TOLERANCE;
    }
}