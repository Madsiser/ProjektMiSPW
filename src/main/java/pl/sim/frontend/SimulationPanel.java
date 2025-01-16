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

        //Rysowanie grup na mapie
        for (SimGroup group : groups) {
            SimPosition pos = group.getPosition();
            int rectWidth = 15;
            int rectHeight = 15;

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
            gc.setLineWidth(1.5);
            gc.strokeRect(x, y, rectWidth, rectHeight);

            //Nazwa grupy
            gc.setFill(Color.BLACK);
            gc.setFont(javafx.scene.text.Font.font("Arial", 14));
            String groupName = group.getName();
            Text textNode = new Text(groupName);
            textNode.setFont(gc.getFont());
            double groupNameWidth = textNode.getBoundsInLocal().getWidth();
            gc.fillText(groupName, x + rectWidth / 2.0 - groupNameWidth / 2.0, y - 5);

            //Jednostki w grupie
            List<SimUnit> units = group.getUnits();
            gc.setFont(javafx.scene.text.Font.font("Arial", 12));
            gc.setFill(Color.BLACK);
            for (int i = 0; i < units.size(); i++) {
                SimUnit unit = units.get(i);
                String unitInfo = String.format("%s [%d/%d] Ammo: [%d/%d]",
                        unit.getName(),
                        unit.getActiveUnits(),
                        unit.getInitialUnits(),
                        unit.getCurrentAmmunition(),
                        unit.getInitialAmmunition()
                );

                Text unitTextNode = new Text(unitInfo);
                unitTextNode.setFont(gc.getFont());
                double unitInfoWidth = unitTextNode.getBoundsInLocal().getWidth();
                gc.fillText(unitInfo, x + rectWidth / 2.0 - unitInfoWidth / 2.0, y + rectHeight + 12 * (i + 1));
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
