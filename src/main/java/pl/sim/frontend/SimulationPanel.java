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

import java.util.List;
import java.util.Map;
import java.util.HashMap;

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

        double gridSize = 20; // Rozmiar jednego kafelka siatki

        if (terrainMap != null) {
            for (int i = 0; i < terrainMap.length; i++) {
                for (int j = 0; j < terrainMap[i].length; j++) {
                    // Ustaw kolor dla każdego pola na podstawie jego wartości
                    Color terrainColor = getTerrainColor(terrainMap[i][j]);
                    gc.setFill(terrainColor);
                    gc.fillRect(j * gridSize, i * gridSize, gridSize, gridSize); // Współrzędne (j, i)

                    // Rysowanie wartości logicznej z terrainMap jako tekst
                    gc.setFill(Color.BLACK); // Tekst w kolorze czarnym
                    gc.setFont(javafx.scene.text.Font.font("Arial", 10)); // Ustaw font
                    String terrainValue = String.valueOf(terrainMap[i][j]);
                    gc.fillText(terrainValue, j * gridSize + gridSize / 4.0, i * gridSize + gridSize / 1.5);
                }
            }
        }


        // Rysowanie siatki
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(0.5);

        for (double x = 0; x < getWidth(); x += gridSize) {
            gc.strokeLine(x, 0, x, getHeight());
        }

        for (double y = 0; y < getHeight(); y += gridSize) {
            gc.strokeLine(0, y, getWidth(), y);
        }

        // Rysowanie grup na mapie
        gc.setLineWidth(1.5);
        gc.setGlobalAlpha(1.0);

        for (SimGroup group : groups) {
            SimPosition pos = group.getPosition();
            int rectWidth = 15;
            int rectHeight = 15;

            Map<String, Integer> totalCurrentAmmoByName = new HashMap<>();
            Map<String, Integer> totalInitialAmmoByName = new HashMap<>();
            for (SimUnit unit : group.getUnits()) {
                String unitName = unit.getName();
                totalCurrentAmmoByName.put(unitName, totalCurrentAmmoByName.getOrDefault(unitName, 0) + unit.getCurrentAmmunition());
                totalInitialAmmoByName.put(unitName, totalInitialAmmoByName.getOrDefault(unitName, 0) + unit.getInitialAmmunition());
            }

            // Kolor w zależności od strony
            if (group.getForceType() == SimForceType.REDFORCE) {
                gc.setFill(Color.RED);
                gc.setStroke(Color.DARKRED);
            } else {
                gc.setFill(Color.BLUE);
                gc.setStroke(Color.DARKBLUE);
            }

            // Kwadrat
            double x = pos.getX() * gridSize; // Współrzędna X kafelka
            double y = pos.getY() * gridSize; // Współrzędna Y kafelka
            gc.fillRect(x, y, rectWidth, rectHeight); // Prostokąt jednostki
            gc.strokeRect(x, y, rectWidth, rectHeight); // Obrys jednostki

            // Nazwa grupy
            gc.setFill(Color.BLACK);
            gc.setFont(javafx.scene.text.Font.font("Arial", 14));
            String groupName = group.getName();
            Text textNode = new Text(groupName);
            textNode.setFont(gc.getFont());
            double groupNameWidth = textNode.getBoundsInLocal().getWidth();
            gc.fillText(groupName, x + rectWidth / 2.0 - groupNameWidth / 2.0, y - 5);

            // Wyświetlanie podsumowania amunicji dla grupy jednostek
            gc.setFont(javafx.scene.text.Font.font("Arial", 12));
            gc.setFill(Color.BLACK);

            int lineOffset = 1;
            for (String unitName : totalCurrentAmmoByName.keySet()) {
                // Liczenie aktywnej i początkowej ilości amunicji
                int totalCurrentAmmo = group.getUnits().stream()
                        .filter(u -> u.getName().equals(unitName))
                        .mapToInt(SimUnit::getTotalCurrentAmmunition)
                        .sum();

                int totalInitialAmmo = group.getUnits().stream()
                        .filter(u -> u.getName().equals(unitName))
                        .mapToInt(SimUnit::getTotalInitialAmmunition)
                        .sum();

                // Liczenie aktywnych i początkowych jednostek
                int activeUnits = group.getUnits().stream()
                        .filter(u -> u.getName().equals(unitName))
                        .mapToInt(SimUnit::getActiveUnits)
                        .sum();

                int initialUnits = group.getUnits().stream()
                        .filter(u -> u.getName().equals(unitName))
                        .mapToInt(SimUnit::getInitialUnits)
                        .sum();

                // Tworzenie tekstu z podsumowaniem
                String unitInfo = String.format("%s [%d/%d] Ammo: [%d/%d]",
                        unitName, activeUnits, initialUnits, totalCurrentAmmo, totalInitialAmmo);

                // Rysowanie tekstu
                Text unitTextNode = new Text(unitInfo);
                unitTextNode.setFont(gc.getFont());
                double unitInfoWidth = unitTextNode.getBoundsInLocal().getWidth();
                gc.fillText(unitInfo, x + rectWidth / 2.0 - unitInfoWidth / 2.0, y + rectHeight + 12 * lineOffset);
                lineOffset++;
            }

            // Zasięg strzału grupy
            int maxShotRange = group.getUnits().stream()
                    .mapToInt(SimUnit::getShotRange)
                    .max()
                    .orElse(0);
            if (maxShotRange > 0) {
                gc.setFill(new Color(1, 0, 0, 0.15));
                double rangeDiameter = maxShotRange * 2 * gridSize;
                gc.fillOval(pos.getX() * gridSize - rangeDiameter / 2,
                        pos.getY() * gridSize - rangeDiameter / 2,
                        rangeDiameter,
                        rangeDiameter);
            }

            // Zasięg widoczności grupy
            int visibilityRange = group.getUnits().stream()
                    .mapToInt(SimUnit::getViewRange)
                    .max()
                    .orElse(0);
            if (visibilityRange > 0) {
                gc.setStroke(new Color(0, 1, 0, 0.25));
                gc.setLineWidth(1.5);
                double visibilityDiameter = visibilityRange * 2 * gridSize;
                gc.strokeOval(pos.getX() * gridSize - visibilityDiameter / 2,
                        pos.getY() * gridSize - visibilityDiameter / 2,
                        visibilityDiameter,
                        visibilityDiameter);
            }
        }
    }

    private Color getTerrainColor(int terrainValue) {
        if (terrainValue == MapGenerator.IMPASSABLE_TERRAIN) {
            return Color.YELLOW; // Nieprzejezdny teren
        } else if (terrainValue == MapGenerator.MOUNTAIN_TERRAIN) {
            return Color.RED; // Góry
        } else if (terrainValue == MapGenerator.HILL_TERRAIN) {
            return Color.YELLOW; // Pagórki
        } else if (terrainValue == MapGenerator.RIVER_TERRAIN) {
            return Color.BLUE; // Rzeka
        } else if (terrainValue == MapGenerator.EASIEST_TERRAIN) {
            return Color.LIGHTGREEN; // Niziny (łatwy teren)
        } else {
            return Color.WHITE; // Domyślny kolor (np. niezidentyfikowany teren)
        }
    }
}
