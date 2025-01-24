package pl.sim.backend;

import java.util.Arrays;
import java.util.Random;

public class MapGenerator {
    // Stałe reprezentujące różne typy terenu
    public static final int EASIEST_TERRAIN = 1;                 // Najlżejszy teren
    public static final int RIVER_TERRAIN = 715827882;           // Rzeka
    public static final int HILL_TERRAIN = 429496729;            // Pagórki
    public static final int MOUNTAIN_TERRAIN = Integer.MAX_VALUE-1;
           // 1431655765;       // Góry
    public static final int IMPASSABLE_TERRAIN = 0;
// pol przekatnej
//    public static int[][] generate(int x, int y) {
//        int[][] grid = new int[x][y];
//
//        // Generowanie mapy
//        for (int i = 0; i < x; i++) {
//            for (int j = 0; j < y; j++) {
//                // Dolna połowa przekątnej jako nieprzejezdny teren
//                if (i + j == x - 1 && i > x / 2) {
//                    grid[i][j] = IMPASSABLE_TERRAIN;
//                }
//                // Przejście w dolnej części (dodajemy przejezdny teren)
//                else if (i == x - 2 && j == 1) {
//                    grid[i][j] = EASIEST_TERRAIN; // Przejście
//                }
//                // Pozostały teren
//                else {
//                    grid[i][j] = EASIEST_TERRAIN; // Domyślnie łatwy teren
//                }
//            }
//        }
//
//        return grid;
//    }
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

        // Logowanie wygenerowanej mapy
       System.out.println("Generated map:");
       for (int i = 0; i < height; i++) {
           for (int j = 0; j < width; j++) {
               System.out.print(grid[j][i] + " "); // Zamiast grid[i][j], użyj grid[j][i]
           }
           System.out.println();
       }

        return grid;
    }

}


//    public static int[][] generate(int x, int y) {
//        int[][] grid = new int[x][y];
//
//        // Środek mapy (pozioma linia)
//        int centerX = x / 2;
//
//        for (int i = 0; i < x; i++) {
//            for (int j = 0; j < y; j++) {
//                // Jeśli wiersz to środek mapy, ustaw nieprzejezdny teren
//                if (i == centerX) {
//                    // Na środku poziomej linii dodaj przejezdne pole
//                    if (j == y / 2) {
//                        grid[i][j] = EASIEST_TERRAIN; // Przejście
//                    } else {
//                        grid[i][j] = IMPASSABLE_TERRAIN; // Nieprzejezdna pozioma linia
//                    }
//                } else {
//                    // Pozostały teren jako łatwy do przejazdu
//                    grid[i][j] = EASIEST_TERRAIN;
//                }
//            }
//        }
//
//        // Wyświetlenie siatki w konsoli (do debugowania)
//        for (int i = 0; i < grid.length; i++) {
//            System.out.println(Arrays.toString(grid[i]));
//        }
//
//        return grid;
//    }










//    public static int[][] generate(int x, int y) {
//        int[][] grid = new int[x][y];
//        Random random = new Random();

//        for (int i = 0; i < x; i++) {
//            for (int j = 0; j < y; j++) {
//                double chance = random.nextDouble();
//
//                // Przypisanie typu terenu na podstawie prawdopodobieństwa
//                if (chance < 0.3) {
//                    grid[i][j] = IMPASSABLE_TERRAIN; // Nieprzejezdny teren
//                } else if (chance < 0.2) {
//                    grid[i][j] = MOUNTAIN_TERRAIN; // Góry
//                } else if (chance < 0.1) {
//                    grid[i][j] = HILL_TERRAIN; // Pagórki
//                } else if (chance < 0.1) {
//                    grid[i][j] = EASIEST_TERRAIN; // Niziny (łatwy teren)
//                } else if (chance < 0.1) {
//                    grid[i][j] = RIVER_TERRAIN; // Rzeka
//                } else {
//                    grid[i][j] = EASIEST_TERRAIN; // Domyślny teren
//                }
//            }
//        }
//
//        return grid;
//    }