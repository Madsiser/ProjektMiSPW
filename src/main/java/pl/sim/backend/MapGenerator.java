package pl.sim.backend;

import java.util.Random;

public class MapGenerator {
    public static int[][] generate(int x, int y) {
        int[][] grid = new int[x][y];
        Random random = new Random(13);

        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                // Możesz dostosować zakres wartości, które chcesz umieścić w mapie
//                if (random.nextDouble() < 0.1) {
//                    grid[i][j] = 5; // Przykładowa wartość specjalna
//                } else if (random.nextDouble() < 0.1) {
//                    grid[i][j] = 10; // Inna wartość specjalna
//                } else if (random.nextDouble() < 0.1) {
//                    grid[i][j] = 9; // Jeszcze inna wartość specjalna
//                } else {
//                    grid[i][j] = 1; // Domyślna wartość
//                }
                grid[i][j] = 1;
            }
        }

        return grid;
    }
}
