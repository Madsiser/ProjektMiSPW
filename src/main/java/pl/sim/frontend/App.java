package pl.sim.frontend;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import pl.sim.backend.BattalionManager;
import pl.sim.backend.MapGenerator;
import pl.sim.backend.UnitManager;
import pl.simNG.*;
import pl.simNG.map.SimMap;

import java.util.List;

public class App extends Application {
    private boolean simulationRunning = false; // Flaga stanu symulacji
    private AnimationTimer timer;

    @Override
    public void start(Stage primaryStage) {
        SimCore simulation = new SimCore();
        int height =20;
        int width =50;
        int[][] terrainMap = MapGenerator.generate(width, height);
        simulation.setMap(new SimMap(terrainMap));

        // Panel symulacji z mapą terenu
        SimulationPanel panel = new SimulationPanel(width*20 , height*20, simulation.getGroups(), terrainMap);

        // Panel kontrolny
        VBox controlPanel = new VBox();
        controlPanel.setSpacing(10);

        // Elementy GUI dla dodawania grup
        TextField nameField = new TextField();
        nameField.setPromptText("Group Name");

        TextField xField = new TextField();
        xField.setPromptText("X Position");
        xField.setEditable(false); // Pole ustawiane tylko przez kliknięcie na mapę

        TextField yField = new TextField();
        yField.setPromptText("Y Position");
        yField.setEditable(false); // Pole ustawiane tylko przez kliknięcie na mapę

        ComboBox<SimForceType> forceTypeComboBox = new ComboBox<>();
        forceTypeComboBox.getItems().addAll(SimForceType.BLUFORCE, SimForceType.REDFORCE);
        forceTypeComboBox.setPromptText("Force Type");

        ComboBox<String> battalionTypeComboBox = new ComboBox<>();
        battalionTypeComboBox.getItems().addAll("Tank Battalion", "Mechanized Battalion", "Infantry Battalion", "Artillery Battalion");
        battalionTypeComboBox.setPromptText("Battalion Type");

        TextField unitCountField = new TextField();
        unitCountField.setPromptText("Unit Count");

        Button addButton = new Button("Add Group");
        Button startButton = new Button("Start Simulation");

        // Obsługa przycisku "Add Group"
        addButton.setOnAction(event -> {
            try {
                String name = nameField.getText();
                int x = Integer.parseInt(xField.getText());
                int y = Integer.parseInt(yField.getText());
                SimForceType forceType = forceTypeComboBox.getValue();
                String battalionType = battalionTypeComboBox.getValue();
                int unitCount = Integer.parseInt(unitCountField.getText());

                if (forceType == null || battalionType == null) {
                    throw new IllegalArgumentException("Force type and battalion type are required.");
                }

                // Dodajemy nową grupę na podstawie wybranego typu batalionu
                // Generowanie grupy
                SimGroup newGroup = createGroup(name, new SimPosition(x, y), forceType, battalionType, unitCount);

                simulation.addGroup(newGroup);

                // Aktualizacja panelu symulacji
                panel.updateGroups(simulation.getGroups());

                // Czyszczenie pól po dodaniu grupy
                nameField.clear();
                xField.clear();
                yField.clear();
                unitCountField.clear();
                forceTypeComboBox.setValue(null);
                battalionTypeComboBox.setValue(null);

            } catch (Exception e) {
                // Wyświetlanie błędu
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Input Error");
                alert.setHeaderText("Failed to add group");
                alert.setContentText("Please check your inputs.");
                alert.showAndWait();
            }
        });

        // Obsługa przycisku "Start Simulation"
        startButton.setOnAction(event -> {
            if (!simulationRunning) {
                simulationRunning = true;
                simulation.startSimulation();

                timer = new AnimationTimer() {
                    @Override
                    public void handle(long now) {
                        List<SimGroup> allGroups = simulation.getGroups();
                        panel.updateGroups(allGroups);
                    }
                };
                timer.start();

                // Blokujemy przycisk, aby uniemożliwić ponowne uruchomienie
                startButton.setDisable(true);
            }
        });

        // Obsługa kliknięcia myszką na mapę
        panel.setOnMouseClicked((MouseEvent event) -> {
            // Obliczamy współrzędne na podstawie klikniętej pozycji
            int x = (int) Math.floor(event.getX() / 20); // Skala 20x20 na mapie
            int y = (int) Math.floor(event.getY() / 20);

            // Ustawiamy współrzędne w polach tekstowych
            xField.setText(String.valueOf(x));
            yField.setText(String.valueOf(y));
        });

        // Dodanie elementów do panelu kontrolnego
        controlPanel.getChildren().addAll(
                new Label("Add New Group"),
                nameField,
                xField,
                yField,
                forceTypeComboBox,
                battalionTypeComboBox,
                unitCountField,
                addButton,
                startButton
        );

        // Separator pionowy
        Separator verticalSeparator = new Separator();
        verticalSeparator.setOrientation(javafx.geometry.Orientation.VERTICAL);
        verticalSeparator.setPrefHeight(800);

        // Układ aplikacji
        HBox root = new HBox();
        root.getChildren().addAll(panel, verticalSeparator, controlPanel);

        // Scena i okno główne
        Scene scene = new Scene(root, 1600, 800);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Symulacja - Pole walki");
        primaryStage.show();

        // Zamykanie aplikacji
        primaryStage.setOnCloseRequest(event -> {
            if (simulationRunning) {
                simulation.stopSimulation();
            }
            System.exit(0);
        });
    }

    private SimGroup createGroup(String name, SimPosition position, SimForceType forceType, String battalionType, int unitCount) {
        switch (battalionType) {
            case "Tank Battalion":
                return new BattalionManager.TankBattalion(name, position, forceType, unitCount);
            case "Mechanized Battalion":
                return new BattalionManager.MechanizedBattalion(name, position, forceType, unitCount);
            case "Infantry Battalion":
                return new BattalionManager.InfantryBattalion(name, position, forceType, unitCount);
            case "Artillery Battalion":
                return new BattalionManager.ArtilleryBattalion(name, position, forceType, unitCount);
            default:
                throw new IllegalArgumentException("Invalid battalion type.");
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}
