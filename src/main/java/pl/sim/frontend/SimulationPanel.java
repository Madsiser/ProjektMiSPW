package pl.sim.frontend;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
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
    private Image backgroundImage;

    public SimulationPanel(double width, double height, List<SimGroup> groups, int[][] terrainMap,Image backgroundImage) {
        super(width, height);
        this.groups = groups;
        this.terrainMap = terrainMap;
        this.backgroundImage = backgroundImage;
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
        if (backgroundImage != null) {
            gc.drawImage(backgroundImage, 0, 0, getWidth(), getHeight());
        }

        if (terrainMap != null) {
            for (int i = 0; i < terrainMap.length; i++) {
                for (int j = 0; j < terrainMap[i].length; j++) {

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

            // Kolor w zależności od strony
            if (group.getForceType() == SimForceType.REDFORCE) {
                gc.setFill(Color.RED);
                gc.setStroke(Color.DARKRED);
            } else {
                gc.setFill(Color.BLUE);
                gc.setStroke(Color.DARKBLUE);
            }

            // Kwadrat reprezentujący grupę
            double x = pos.getX() * gridWidth;
            double y = pos.getY() * gridHeight;
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

            // Wyświetlanie podsumowania amunicji dla grupy jednostek
            gc.setFont(javafx.scene.text.Font.font("Arial", 12));
            gc.setFill(Color.BLACK);

            int lineOffset = 1;
            for (String unitName : totalCurrentAmmoByName.keySet()) {
                String unitInfo = String.format("%s Ammo: [%d/%d]",
                        unitName,
                        totalCurrentAmmoByName.get(unitName),
                        totalInitialAmmoByName.get(unitName));
                gc.fillText(unitInfo, x + rectWidth / 2.0, y + rectHeight + 12 * lineOffset);
                lineOffset++;
            }
        }
    }


}
