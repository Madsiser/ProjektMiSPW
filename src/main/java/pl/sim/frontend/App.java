package pl.sim.frontend;

import pl.sim.backend.MapGenerator;
import pl.sim.backend.RandomForce;
import pl.simNG.SimCore;
import pl.simNG.SimForceType;
import pl.simNG.SimGroup;
import pl.simNG.SimPosition;
import pl.simNG.map.SimMap;

import javax.swing.*;
import java.util.List;
import java.util.Random;

public class App {
    public void run(){

        SimCore simulation = new SimCore();
        simulation.setMap(new SimMap(MapGenerator.generate(501,501)));

//        simulation.addGroup(new RandomForce("Alpha Force", new SimPosition(12, 12), SimForceType.BLUFORCE));
//        simulation.addGroup(new RandomForce("Bravo Force", new SimPosition(15, 15), SimForceType.BLUFORCE));
        simulation.addGroup(new RandomForce("Charlie Force", new SimPosition(20, 20), SimForceType.BLUFORCE));
        simulation.addGroup(new RandomForce("Echo Force", new SimPosition(25, 13), SimForceType.BLUFORCE));
        simulation.addGroup(new RandomForce("Foxtrot Force", new SimPosition(13, 25), SimForceType.REDFORCE));
        simulation.addGroup(new RandomForce("Golf Force", new SimPosition(27, 15), SimForceType.REDFORCE));
        simulation.addGroup(new RandomForce("Hotel Force", new SimPosition(15, 27), SimForceType.REDFORCE));
//        simulation.addGroup(new RandomForce("India Force", new SimPosition(22, 22), SimForceType.REDFORCE));


        Random random = new Random(10);

        for (int i = 1; i <= 100; i++) {
            int x = random.nextInt(200); // Losowa wartość x w zakresie 0-500
            int y = random.nextInt(200); // Losowa wartość y w zakresie 0-500
            simulation.addGroup(new RandomForce("Ally " + i, new SimPosition(x, y), SimForceType.BLUFORCE));
        }

        for (int i = 1; i <= 100; i++) {
            int x = random.nextInt(200); // Losowa wartość x w zakresie 0-500
            int y = random.nextInt(200); // Losowa wartość y w zakresie 0-500
            simulation.addGroup(new RandomForce("Enemy " + i, new SimPosition(x, y), SimForceType.REDFORCE));
        }

        // Tworzenie okna
        JFrame frame = new JFrame("SimNG");
        SimulationPanel panel = new SimulationPanel(simulation.getGroups());
        frame.add(panel);
        frame.setSize(800, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        simulation.startSimulation();
        int counter = 1000000;
        while (counter > 0) {
            counter--;
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            List<SimGroup> allGroups = simulation.getGroups();
//            System.out.println("Current allGroups: " + allGroups);
            panel.updateGroups(allGroups); // Aktualizacja grup w panelu
        }

        simulation.stopSimulation();
    }
}
