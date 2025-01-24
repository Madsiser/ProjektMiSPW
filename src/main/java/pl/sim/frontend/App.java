package pl.sim.frontend;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import pl.sim.backend.BattalionManager;
import pl.sim.backend.MapGenerator;
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

        simulation.addGroup(new BattalionManager.TankBattalion("Blue Tank Battalion 1", new SimPosition(16, 18), SimForceType.BLUFORCE, 10));
        simulation.addGroup(new BattalionManager.MechanizedBattalion("Blue Mechanized Battalion 1", new SimPosition(22, 32), SimForceType.BLUFORCE, 10));
        simulation.addGroup(new BattalionManager.InfantryBattalion("Blue Infantry Battalion 1", new SimPosition(22, 25), SimForceType.BLUFORCE, 50));
        simulation.addGroup(new BattalionManager.InfantryBattalion("Red Infantry Battalion 1", new SimPosition(13, 22), SimForceType.REDFORCE, 50));
        simulation.addGroup(new BattalionManager.ArtilleryBattalion("Red Artillery Battalion 1", new SimPosition(27, 28), SimForceType.REDFORCE, 10));
//        simulation.addGroup(new BattalionManager.ArtilleryBattalion("Red Enemy Battalion", new SimPosition(5, 15), SimForceType.REDFORCE, 10));
        simulation.addGroup(new BattalionManager.InfantryBattalion("Red Infantry Battalion 2", new SimPosition(25, 35), SimForceType.REDFORCE, 50));
        simulation.addGroup(new BattalionManager.ArtilleryBattalion("Red Enemy Battalion", new SimPosition(25, 25), SimForceType.REDFORCE, 10));
//        simulation.addGroup(new BaseGroup("Charlie Force", new SimPosition(26.0, 26.0), SimForceType.BLUFORCE));
//        simulation.addGroup(new BaseGroup("Echo Force", new SimPosition(25.0, 13.0), SimForceType.BLUFORCE));
//        simulation.addGroup(new BaseGroup("Foxtrot Force", new SimPosition(13.0, 25.0), SimForceType.REDFORCE));
//        simulation.addGroup(new BaseGroup("Golf Force", new SimPosition(27.0, 15.0), SimForceType.REDFORCE));
//        simulation.addGroup(new BaseGroup("Hotel Force", new SimPosition(24.0, 24.0), SimForceType.REDFORCE));

        Random random = new Random(10);

//        for (int i = 1; i <= 3; i++) {
//            int x = random.nextInt(100);
//            int y = random.nextInt(100);
//            simulation.addGroup(new BattalionManager.ArtilleryBattalion("Ally " + i, new SimPosition(x, y), SimForceType.BLUFORCE, 10));
//        }
//        for (int i = 1; i <= 3; i++) {
//            int x = random.nextInt(100);
//            int y = random.nextInt(100);
//            simulation.addGroup(new BattalionManager.InfantryBattalion("Ally " + i, new SimPosition(x, y), SimForceType.BLUFORCE, 10));
//        }
//        for (int i = 1; i <= 3; i++) {
//            int x = random.nextInt(100);
//            int y = random.nextInt(100);
//            simulation.addGroup(new BattalionManager.TankBattalion("Ally " + i, new SimPosition(x, y), SimForceType.BLUFORCE, 10));
//        }
//        for (int i = 1; i <= 3; i++) {
//            int x = random.nextInt(100);
//            int y = random.nextInt(100);
//            simulation.addGroup(new BattalionManager.MechanizedBattalion("Ally " + i, new SimPosition(x, y), SimForceType.BLUFORCE,10));
//        }
//
//
//        for (int i = 1; i <= 3; i++) {
//            int x = random.nextInt(100);
//            int y = random.nextInt(100);
//            simulation.addGroup(new BattalionManager.InfantryBattalion("Enemy " + i, new SimPosition(x, y), SimForceType.REDFORCE, 10));
//        }
//        for (int i = 1; i <= 3; i++) {
//            int x = random.nextInt(100);
//            int y = random.nextInt(100);
//            simulation.addGroup(new BattalionManager.TankBattalion("Enemy " + i, new SimPosition(x, y), SimForceType.REDFORCE, 10));
//        }
//        for (int i = 1; i <= 3; i++) {
//            int x = random.nextInt(100);
//            int y = random.nextInt(100);
//            simulation.addGroup(new BattalionManager.MechanizedBattalion("Enemy " + i, new SimPosition(x, y), SimForceType.REDFORCE, 10));
//        }
//        for (int i = 1; i <= 3; i++) {
//            int x = random.nextInt(100);
//            int y = random.nextInt(100);
//            simulation.addGroup(new BattalionManager.ArtilleryBattalion("Enemy " + i, new SimPosition(x, y), SimForceType.REDFORCE, 10));
//        }

        SimulationPanel panel = new SimulationPanel(1500, 800, simulation.getGroups());
        Pane root = new Pane(panel);

        Scene scene = new Scene(root, 1500, 800);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Symulacja - Pole walki");
        primaryStage.show();

        simulation.startSimulation();
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                List<SimGroup> allGroups = simulation.getGroups();
                panel.updateGroups(allGroups);
            }
        };
        timer.start();
        primaryStage.setOnCloseRequest(event -> {
            simulation.stopSimulation();
            System.exit(0);
        });
    }
    public static void main(String[] args) {
        launch(args);
    }
}
