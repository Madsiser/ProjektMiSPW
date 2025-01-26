package pl.sim.frontend;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import pl.sim.backend.MapGenerator;
import pl.simNG.SimForceType;
import pl.simNG.SimGroup;
import pl.simNG.SimPosition;
import pl.simNG.SimUnit;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimulationPanel extends Canvas {
    private List<SimGroup> groups;
    private int[][] terrainMap; // Mapa terenu

    public SimulationPanel(double width, double height, List<SimGroup> groups, int[][] terrainMap) {
        super(width, height);
        this.groups = groups;
        this.terrainMap = terrainMap;
        drawComponents();
    }

    public void updateGroups(List<SimGroup> groups) {
        this.groups = groups;
        drawComponents();
    }

    public void drawComponents() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());

        // Dynamiczny rozmiar kafelka na podstawie rozmiaru mapy
        double gridWidth = getWidth() / terrainMap.length;
        double gridHeight = getHeight() / terrainMap[0].length;

        if (terrainMap != null) {
            for (int i = 0; i < terrainMap.length; i++) {
                for (int j = 0; j < terrainMap[i].length; j++) {
                    // Ustaw kolor dla każdego pola na podstawie jego wartości
                    Color terrainColor = getTerrainColor(terrainMap[i][j]);
                    gc.setFill(terrainColor);
                    gc.fillRect(i * gridWidth, j * gridHeight, gridWidth, gridHeight);

                    // Rysowanie wartości logicznej z terrainMap jako tekst
                    gc.setFill(Color.BLACK);
                    gc.setFont(javafx.scene.text.Font.font("Arial", 10));
                    String terrainValue = String.valueOf(terrainMap[i][j]);
                    gc.fillText(terrainValue, i * gridWidth + gridWidth / 4.0, j * gridHeight + gridHeight / 1.5);
                }
            }
        }

        // Rysowanie siatki
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(0.5);

        for (int i = 0; i <= terrainMap.length; i++) {
            gc.strokeLine(i * gridWidth, 0, i * gridWidth, getHeight());
        }
        for (int j = 0; j <= terrainMap[0].length; j++) {
            gc.strokeLine(0, j * gridHeight, getWidth(), j * gridHeight);
        }

        // Rysowanie grup na mapie
        gc.setLineWidth(1.5);
        gc.setGlobalAlpha(1.0);

        for (SimGroup group : groups) {
            SimPosition pos = group.getPosition();
            int rectWidth = (int) (gridWidth * 0.8);
            int rectHeight = (int) (gridHeight * 0.8);

            Map<String, Integer> totalCurrentAmmoByName = new HashMap<>();
            Map<String, Integer> totalInitialAmmoByName = new HashMap<>();
            for (SimUnit unit : group.getUnits()) {
                String unitName = unit.getName();
                totalCurrentAmmoByName.put(unitName, totalCurrentAmmoByName.getOrDefault(unitName, 0) + unit.getCurrentAmmunition());
                totalInitialAmmoByName.put(unitName, totalInitialAmmoByName.getOrDefault(unitName, 0) + unit.getInitialAmmunition());
            }

            double x = pos.getX() * gridWidth;
            double y = pos.getY() * gridHeight;

            for (SimUnit unit : group.getUnits()) {
                double visibilityRadius = unit.getVisibilityRange() * gridWidth; // Zasięg widoczności
                double shootingRadius = unit.getShootingRange() * gridWidth;   // Zasięg strzału

                // Zasięg widoczności (zielony okrąg)
                gc.setStroke(Color.GREEN);
                gc.setLineWidth(1.0);
                gc.strokeOval(x - visibilityRadius + rectWidth / 2.0,
                        y - visibilityRadius + rectHeight / 2.0,
                        visibilityRadius * 2,
                        visibilityRadius * 2);

                // Zasięg strzału (czerwony okrąg)
                gc.setStroke(Color.RED);
                gc.setLineWidth(1.0);
                gc.strokeOval(x - shootingRadius + rectWidth / 2.0,
                        y - shootingRadius + rectHeight / 2.0,
                        shootingRadius * 2,
                        shootingRadius * 2);
            }

            // Kolor w zależności od strony
            if (group.getForceType() == SimForceType.REDFORCE) {
                gc.setFill(Color.RED);
                gc.setStroke(Color.DARKRED);
            } else {
                gc.setFill(Color.BLUE);
                gc.setStroke(Color.DARKBLUE);
            }

            // Kwadrat reprezentujący grupę
//            double x = pos.getX() * gridWidth;
//            double y = pos.getY() * gridHeight;
            gc.fillRect(x, y, rectWidth, rectHeight);
            gc.strokeRect(x, y, rectWidth, rectHeight);

            // Nazwa grupy
            gc.setFill(Color.BLACK);
            gc.setFont(javafx.scene.text.Font.font("Arial", 14));
            String groupName = group.getName();
            Text textNode = new Text(groupName);
            textNode.setFont(gc.getFont());
            double groupNameWidth = textNode.getBoundsInLocal().getWidth();
            gc.fillText(groupName, x + rectWidth / 2.0 - groupNameWidth / 2.0, y - 5);

            //Wyświetlanie podsumowania amunicji dla grupy jednostek
            gc.setFont(javafx.scene.text.Font.font("Arial", 12));
            gc.setFill(Color.BLACK);

            int lineOffset = 1;
            for (String unitName : totalCurrentAmmoByName.keySet()) {
                //Liczenie aktywnej i początkowej ilości amunicji
                int totalCurrentAmmo = group.getUnits().stream()
                        .filter(u -> u.getName().equals(unitName))
                        .mapToInt(SimUnit::getTotalCurrentAmmunition)
                        .sum();

                int totalInitialAmmo = group.getUnits().stream()
                        .filter(u -> u.getName().equals(unitName))
                        .mapToInt(SimUnit::getTotalInitialAmmunition)
                        .sum();

                //Liczenie aktywnych i początkowych jednostek
                int activeUnits = group.getUnits().stream()
                        .filter(u -> u.getName().equals(unitName))
                        .mapToInt(SimUnit::getActiveUnits)
                        .sum();

                int initialUnits = group.getUnits().stream()
                        .filter(u -> u.getName().equals(unitName))
                        .mapToInt(SimUnit::getInitialUnits)
                        .sum();

                //Tworzenie tekstu z podsumowaniem
                String unitInfo = String.format("%s [%d/%d] Ammo: [%d/%d]",
                        unitName, activeUnits, initialUnits, totalCurrentAmmo, totalInitialAmmo);

                //Rysowanie tekstu
                Text unitTextNode = new Text(unitInfo);
                unitTextNode.setFont(gc.getFont());
                double unitInfoWidth = unitTextNode.getBoundsInLocal().getWidth();
                gc.fillText(unitInfo, x + rectWidth / 2.0 - unitInfoWidth / 2.0, y + rectHeight + 12 * lineOffset);
                lineOffset++;
            }
        }
    }

    private Color getTerrainColor(int terrainValue) {
        if (terrainValue == MapGenerator.IMPASSABLE_TERRAIN) {
            return Color.YELLOW; // Nieprzejezdny teren
        } else if (terrainValue == MapGenerator.MOUNTAIN_TERRAIN) {
            return Color.RED; // Góry
        } else if (terrainValue == MapGenerator.HILL_TERRAIN) {
            return Color.ORANGE; // Pagórki
        } else if (terrainValue == MapGenerator.RIVER_TERRAIN) {
            return Color.BLUE; // Rzeka
        } else if (terrainValue == MapGenerator.EASIEST_TERRAIN) {
            return Color.LIGHTGREEN; // Niziny (łatwy teren)
        } else {
            return Color.WHITE; // Domyślny kolor (np. niezidentyfikowany teren)
        }
    }
}
