package pl.sim.frontend;

import pl.simNG.SimForceType;
import pl.simNG.SimGroup;
import pl.simNG.SimPosition;
import pl.simNG.SimUnit;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class SimulationPanel extends JPanel {
    private List<SimGroup> groups;

    public SimulationPanel(List<SimGroup> groups) {
        this.groups = groups;
    }

    public void updateGroups(List<SimGroup> groups) {
        this.groups = groups;
        repaint(); // Odświeżenie panelu
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Ustal maksymalną liczbę grup do wyświetlenia
        int maxGroupsToDisplay = groups.size(); // Użyj tylko rozmiaru listy

        for (SimGroup group:groups) {
//            SimGroup group = groups.get(i);
            SimPosition pos = group.getPosition();
            int rectWidth = 15;
            int rectHeight = 15;

            // Ustaw kolor w zależności od typu siły
            if (group.getForceType() == SimForceType.REDFORCE) {
                g.setColor(Color.RED);
            } else {
                g.setColor(Color.BLUE);
            }

            // Rysuj kwadracik
            int x = (int) pos.getX() * 20 - rectWidth / 2;
            int y = (int) pos.getY() * 20 - rectHeight / 2;
            g.fillRect(x, y, rectWidth, rectHeight);

            // Rysuj nazwę grupy
            g.drawString(group.getName(), x - 10, y - 10);

            // Rysowanie zasięgu strzału grupy
            int maxShotRange = group.getUnits().stream()
                    .mapToInt(SimUnit::getShotRange)
                    .max()
                    .orElse(0);
            if (maxShotRange > 0) {
                g.setColor(new Color(255, 0, 0, 20));
                int rangeDiameter = maxShotRange * 2 * 20;
                g.fillOval((int) pos.getX() * 20 - rangeDiameter / 2,
                        (int) pos.getY() * 20 - rangeDiameter / 2,
                        rangeDiameter,
                        rangeDiameter);
            }


            // Ustaw kolor dla widocznych grup
            if (group.getVisibleGroups().isEmpty()) {
                g.setColor(Color.GREEN);
            } else {
                g.setColor(Color.BLUE);
            }

            // Rysuj jednostki i ich informacje
            for (SimUnit unit : group.getUnits()) {
                int range = unit.getViewRange() * 20 - 10;
                g.drawOval((int) pos.getX() * 20 - range, (int) pos.getY() * 20 - range, range * 2, range * 2);

                // Rysuj nazwę jednostki i jej ilość
                Color mem = g.getColor();
                g.setColor(Color.BLACK);
                String unitInfo = unit.getName() + " (" + unit.getAmount() + ")";
                g.drawString(unitInfo, x - 10, y + rectHeight + 15);
                g.setColor(mem);
            }
        }
    }



}
