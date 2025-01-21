package pl.sim.frontend;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import pl.simNG.SimForceType;
import pl.simNG.SimGroup;
import pl.simNG.SimPosition;
import pl.simNG.SimUnit;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class SimulationPanel extends Canvas {
    private List<SimGroup> groups;

    public SimulationPanel(double width, double height, List<SimGroup> groups) {
        super(width, height);
        this.groups = groups;
    }

    public void updateGroups(List<SimGroup> groups) {
        this.groups = groups;
        drawComponents();
    }

    public void drawComponents() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());

        gc.setLineWidth(1.5);
        gc.setGlobalAlpha(1.0);

        //Rysowanie grup na mapie
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

            //Kolor w zależności od strony
            if (group.getForceType() == SimForceType.REDFORCE) {
                gc.setFill(Color.RED);
                gc.setStroke(Color.DARKRED);
            } else {
                gc.setFill(Color.BLUE);
                gc.setStroke(Color.DARKBLUE);
            }

            //Kwadrat
            double x = pos.getX() * 20 - rectWidth / 2.0;
            double y = pos.getY() * 20 - rectHeight / 2.0;
            gc.fillRect(x, y, rectWidth, rectHeight);
            gc.strokeRect(x, y, rectWidth, rectHeight);

            //Nazwa grupy
            gc.setFill(Color.BLACK);
            gc.setFont(javafx.scene.text.Font.font("Arial", 14));
            String groupName = group.getName();
            Text textNode = new Text(groupName);
            textNode.setFont(gc.getFont());
            double groupNameWidth = textNode.getBoundsInLocal().getWidth();
            gc.fillText(groupName, x + rectWidth / 2.0 - groupNameWidth / 2.0, y - 5);

//            //Jednostki w grupie
//            List<SimUnit> units = group.getUnits();
//            gc.setFont(javafx.scene.text.Font.font("Arial", 12));
//            gc.setFill(Color.BLACK);
//            for (int i = 0; i < units.size(); i++) {
//                SimUnit unit = units.get(i);
//                String unitInfo = String.format("%s [%d/%d] Ammo: [%d/%d]",
//                        unit.getName(),
//                        unit.getActiveUnits(),
//                        unit.getInitialUnits(),
//                        unit.getActiveUnits() * unit.getCurrentAmmunition(),
//                        unit.getInitialUnits() * unit.getInitialAmmunition()
//                );
//
//                Text unitTextNode = new Text(unitInfo);
//                unitTextNode.setFont(gc.getFont());
//                double unitInfoWidth = unitTextNode.getBoundsInLocal().getWidth();
//                gc.fillText(unitInfo, x + rectWidth / 2.0 - unitInfoWidth / 2.0, y + rectHeight + 12 * (i + 1));
//            }

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

            //Zasięg strzału grupy
            int maxShotRange = group.getUnits().stream()
                    .mapToInt(SimUnit::getShotRange)
                    .max()
                    .orElse(0);
            if (maxShotRange > 0) {
                gc.setFill(new Color(1, 0, 0, 0.15));
                double rangeDiameter = maxShotRange * 2 * 20;
                gc.fillOval(pos.getX() * 20 - rangeDiameter / 2,
                        pos.getY() * 20 - rangeDiameter / 2,
                        rangeDiameter,
                        rangeDiameter);
            }

            //Zasięg widoczności grupy
            int visibilityRange = group.getUnits().stream()
                    .mapToInt(SimUnit::getViewRange)
                    .max()
                    .orElse(0);
            if (visibilityRange > 0) {
                gc.setStroke(new Color(0, 1, 0, 0.25));
                gc.setLineWidth(1.5);
                double visibilityDiameter = visibilityRange * 2 * 20;
                gc.strokeOval(pos.getX() * 20 - visibilityDiameter / 2,
                        pos.getY() * 20 - visibilityDiameter / 2,
                        visibilityDiameter,
                        visibilityDiameter);
            }

        }
    }
}
