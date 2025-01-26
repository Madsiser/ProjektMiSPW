package pl.sim.backend;

public class MapGenerator {
    // Stałe reprezentujące różne typy terenu
    public static final int EASIEST_TERRAIN = 1;                 // Najlżejszy teren
    public static final int RIVER_TERRAIN = 715827882;           // Rzeka
    public static final int HILL_TERRAIN = 429496729;            // Pagórki
    public static final int MOUNTAIN_TERRAIN = Integer.MAX_VALUE-1;
    public static final int IMPASSABLE_TERRAIN = 0;
    public static int[][] generate(int width, int height) {
        int[][] grid = new int[width][height];

        // Wyznacz środek mapy (kolumna środka)
        int centerX = width / 2;

        // Generowanie mapy
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (i == centerX) {
                    // Nieprzejezdna linia, oprócz jednego pola na końcu
                    if (j == height - 1) {
                        grid[i][j] = EASIEST_TERRAIN; // Przejazd na końcu
                    } else {
                        grid[i][j] = IMPASSABLE_TERRAIN; // Nieprzejezdny teren
                    }
                } else {
                    grid[i][j] = EASIEST_TERRAIN; // Domyślnie przejezdny teren
                }
            }
        }
        return grid;
    }
}