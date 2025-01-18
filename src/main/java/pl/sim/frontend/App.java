package pl.sim.frontend;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import pl.sim.backend.MapGenerator;
import pl.sim.backend.BaseGroup;
import pl.simNG.SimCore;
import pl.simNG.SimForceType;
import pl.simNG.SimGroup;
import pl.simNG.SimPosition;
import pl.simNG.map.SimMap;

import java.util.List;
import java.util.Random;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) {
        SimCore simulation = new SimCore();
        simulation.setMap(new SimMap(MapGenerator.generate(501, 501)));

        simulation.addGroup(new BaseGroup("Charlie Force", new SimPosition(20.0, 20.0), SimForceType.BLUFORCE));
//        simulation.addGroup(new BaseGroup("Echo Force", new SimPosition(25.0, 13.0), SimForceType.BLUFORCE));
//        simulation.addGroup(new BaseGroup("Foxtrot Force", new SimPosition(13.0, 25.0), SimForceType.REDFORCE));
//        simulation.addGroup(new BaseGroup("Golf Force", new SimPosition(27.0, 15.0), SimForceType.REDFORCE));
        simulation.addGroup(new BaseGroup("Hotel Force", new SimPosition(24.0, 24.0), SimForceType.REDFORCE));

        Random random = new Random(10);

//        for (int i = 1; i <= 10; i++) {
//            int x = random.nextInt(100);
//            int y = random.nextInt(100);
//            simulation.addGroup(new BaseGroup("Ally " + i, new SimPosition(x, y), SimForceType.BLUFORCE));
//        }
//
//        for (int i = 1; i <= 10; i++) {
//            int x = random.nextInt(100);
//            int y = random.nextInt(100);
//            simulation.addGroup(new BaseGroup("Enemy " + i, new SimPosition(x, y), SimForceType.REDFORCE));
//        }

        SimulationPanel panel = new SimulationPanel(800, 800, simulation.getGroups());
        Pane root = new Pane(panel);

        Scene scene = new Scene(root, 800, 800);
        primaryStage.setScene(scene);
        primaryStage.setTitle("SimNG");
        primaryStage.show();

        simulation.startSimulation();
        new Thread(() -> {
            int counter = 1000000;
            while (counter > 0) {
                counter--;
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                List<SimGroup> allGroups = simulation.getGroups();
                javafx.application.Platform.runLater(() -> panel.updateGroups(allGroups));
            }
            simulation.stopSimulation();
        }).start();
    }
}
