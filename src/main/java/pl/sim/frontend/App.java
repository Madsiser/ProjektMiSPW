package pl.sim.frontend;

import com.gluonhq.maps.MapPoint;
import com.gluonhq.maps.MapView;
import com.gluonhq.maps.tile.TileRetriever;
import com.gluonhq.maps.tile.TileRetrieverProvider;
import com.google.gson.reflect.TypeToken;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.util.Duration;
import pl.sim.backend.BattalionManager;
import pl.sim.backend.MapGenerator;
import pl.sim.backend.UnitManager;
import pl.simNG.*;
import pl.simNG.commands.SimCommand;
import pl.simNG.commands.SimCommandType;
import pl.simNG.map.SimMap;

import javax.imageio.ImageIO;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static pl.sim.frontend.SimulationPanel.drawTerrainValues;

public class App extends Application {
    private boolean simulationRunning = false; // Flaga stanu symulacji
    public static MapPoint newCenter;
    private AnimationTimer timer;
    Map<String, MapPoint> savedCoordinates = new HashMap<>();
    File coordinatesFile = new File("saved_coordinates.json");

    private void saveCoordinatesToFile() {
        try (Writer writer = new FileWriter(coordinatesFile)) {
            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .registerTypeAdapter(MapPoint.class, new MapPointAdapter())
                    .create();
            gson.toJson(savedCoordinates, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadCoordinatesFromFile() {
        if (!coordinatesFile.exists() || coordinatesFile.length() == 0) {
            // Jeśli plik nie istnieje lub jest pusty, załaduje pysta
            System.out.println("Plik nie istnieje lub jest pusty. Inicjalizowanie pustej mapy.");
            savedCoordinates = new HashMap<>();
            return;
        }

        try (Reader reader = new FileReader(coordinatesFile)) {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(MapPoint.class, new MapPointAdapter())
                    .create();
            savedCoordinates = gson.fromJson(reader, new TypeToken<Map<String, MapPoint>>() {
            }.getType());
        } catch (Exception e) {
            System.out.println("Błąd podczas ładowania pliku: " + e.getMessage());
            savedCoordinates = new HashMap<>();
        }
    }

    public static int heightRectangle = 10;
    public static int widthRectangle = 10;
    public static int matrixWidth = 100;
    public static int matrixHeight = 100;

    @Override
    public void start(Stage primaryStage) {
        SimCore simulation = new SimCore();
        MapView mapView = new MapView();
        //MapPoint(30.2297, 21.0122);
        MapPoint warsaw = new MapPoint(30.2297, 21.0122);
        MapPoint warsaw2 = new MapPoint(3.2297, 21.0122);
        mapView.setZoom(12);
        mapView.setCenter(warsaw);
        //rozmiar pierwszej mapy
        mapView.setPrefSize(1000, 1000);
        //pozniej to przypisze
        // int matrixWidth = 50;
        // int matrixHeight=50;


        loadCoordinatesFromFile();

// Panel kontrolny po prawej stronie
        VBox rightControlPanel = new VBox();
        rightControlPanel.setSpacing(10);
        rightControlPanel.setPrefWidth(500);

        TextField latitudeField = new TextField();
        latitudeField.setPromptText("Latitude (Szerokość geograficzna)");
        latitudeField.setText(String.valueOf(warsaw.getLatitude()));

        TextField longitudeField = new TextField();
        longitudeField.setPromptText("Longitude (Długość geograficzna)");
        longitudeField.setText(String.valueOf(warsaw.getLongitude()));

        TextField locationNameField = new TextField();
        locationNameField.setPromptText("Location Name");

        ObservableList<String> savedLocationsList = FXCollections.observableArrayList(savedCoordinates.keySet());
        ComboBox<String> savedCoordinatesDropdown = new ComboBox<>(savedLocationsList);
        savedCoordinatesDropdown.setPromptText("Select Saved Location");

        Button saveCoordinatesButton = new Button("Save Coordinates");
        saveCoordinatesButton.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 12px;");

        Button setMapPositionButton = new Button("Set Map Position");
        setMapPositionButton.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 12px;");

        Button captureButton = new Button("Capture Map and Start Simulation");
        captureButton.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 12px;");

        Button goToSavedLocationButton = new Button("Go To Saved Location");
        goToSavedLocationButton.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 12px;");

        CheckBox toggleDrawingCheckBox = new CheckBox("Grid visibility");
        toggleDrawingCheckBox.setStyle("- -fx-text-fill: white; -fx-font-size: 12px;");


        saveCoordinatesButton.setOnAction(event -> {
            String locationName = locationNameField.getText().trim();
            if (locationName.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Input Error");
                alert.setHeaderText("Location Name Missing");
                alert.setContentText("Please provide a name for the location.");
                alert.showAndWait();
                return;
            }

            try {
                double latitude = Double.parseDouble(latitudeField.getText());
                double longitude = Double.parseDouble(longitudeField.getText());
                MapPoint newPoint = new MapPoint(latitude, longitude);

                savedCoordinates.put(locationName, newPoint);


                if (!savedLocationsList.contains(locationName)) {
                    savedLocationsList.add(locationName);
                }

                // Zapis danych do pliku JSON
                saveCoordinatesToFile();

                // Informacja o zapisaniu współrzędnych
                System.out.println("Saved coordinates: " + locationName + " -> " + newPoint);

            } catch (NumberFormatException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Input Error");
                alert.setHeaderText("Invalid Coordinates");
                alert.setContentText("Please enter valid latitude and longitude values.");
                alert.showAndWait();
            }
        });


        setMapPositionButton.setOnAction(event -> {

            try {
                double latitude = Double.parseDouble(latitudeField.getText());
                double longitude = Double.parseDouble(longitudeField.getText());

                // Aktualizacja pozycji mapy
                MapPoint newCenter = new MapPoint(latitude, longitude);
                //mapView.flyTo(0, newCenter, 2.0);
                mapView.setCenter(newCenter);

                // Informacja zwrotna o zmianie
                System.out.println("Map position updated to: " + latitude + ", " + longitude);
            } catch (NumberFormatException e) {

                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Input Error");
                alert.setHeaderText("Invalid Coordinates");
                alert.setContentText("Please enter valid latitude and longitude values.");
                alert.showAndWait();
            }
        });


        toggleDrawingCheckBox.setOnAction(event -> {
            boolean isSelected = toggleDrawingCheckBox.isSelected();
            SimulationPanel.drawTerrainValues = isSelected;
            System.out.println("Terrain Values Display: " + (isSelected ? "Enabled" : "Disabled"));
        });


        mapView.setOnMouseClicked(event -> {
            // Pobranie współrzędnych kliknięcia
            double clickedX = event.getX();
            double clickedY = event.getY();

            // Przekształcenie współrzędnych na rzeczywiste szerokość i wysokość geograficzną
            MapPoint clickedPoint = mapView.getMapPosition(clickedX, clickedY);

            // Aktualizacja pól tekstowych
            latitudeField.setText(String.valueOf(clickedPoint.getLatitude()));
            longitudeField.setText(String.valueOf(clickedPoint.getLongitude()));


            System.out.println("Clicked coordinates: Latitude = " + clickedPoint.getLatitude() + ", Longitude = " + clickedPoint.getLongitude());
        });


        goToSavedLocationButton.setOnAction(event -> {
            String selectedLocation = savedCoordinatesDropdown.getValue();
            if (selectedLocation == null || !savedCoordinates.containsKey(selectedLocation)) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Selection Error");
                alert.setHeaderText("No Location Selected");
                alert.setContentText("Please select a valid saved location.");
                alert.showAndWait();
                return;
            }

            MapPoint selectedPoint = savedCoordinates.get(selectedLocation);
            mapView.setCenter(selectedPoint);

            // Aktualizacja pól tekstowych
            latitudeField.setText(String.valueOf(selectedPoint.getLatitude()));
            longitudeField.setText(String.valueOf(selectedPoint.getLongitude()));

            System.out.println("Map centered on: " + selectedLocation + " -> " + selectedPoint);
        });

//        mapView.setOnScroll(event -> {
//            double deltaY = event.getDeltaY(); // Kierunek przewijania
//            int currentZoom = (int) mapView.getZoom();
//            int newZoom = currentZoom;
//
//            if (deltaY > 0) {
//                // Przybliżanie
//                newZoom = Math.min(currentZoom + 1, 20);
//            } else {
//                // Oddalanie
//                newZoom = Math.max(currentZoom - 1, 1);
//            }
//
//            // Pozycja kursora myszy w pikselach względem mapy
//            double mouseX = event.getX();
//            double mouseY = event.getY();
//
//            // Przekształcenie współrzędnych pikselowych na współrzędne geograficzne
//            MapPoint cursorPosition = mapView.getMapPosition(mouseX, mouseY);
//
//            // Zmiana zoomu
//            mapView.setZoom(newZoom);
//
//            // Ponowne ustawienie centrum mapy w taki sposób, aby miejsce pod kursorem pozostało w tym samym punkcie
//            if (cursorPosition != null) {
//                mapView.setCenter(cursorPosition);
//            }
//
//            // Opcjonalne logowanie
//            System.out.println("Zoom level: " + newZoom + ", Center: " + mapView.getCenter());
//        });


        captureButton.setOnAction(event -> {
            double latitude = Double.parseDouble(latitudeField.getText());
            double longitude = Double.parseDouble(longitudeField.getText());

            // Aktualizacja pozycji mapy

            newCenter = new MapPoint(latitude, longitude);

            mapView.setZoom(6);
            int startZoom = (int) mapView.getZoom();
            int endZoom = 12;

            // Ustawienie nowego centrum mapy
            mapView.setCenter(newCenter);
            double zoomSpeed = 0.8;

            // Animacja zoomu
            Timeline zoomAnimation = new Timeline();

            // Iteracyjna zmiana zoomu
            if (startZoom < endZoom) {
                // Przybliżanie
                for (int zoomLevel = startZoom; zoomLevel <= endZoom; zoomLevel++) {
                    int finalZoomLevel = zoomLevel;
                    zoomAnimation.getKeyFrames().add(
                            new KeyFrame(Duration.seconds((zoomLevel - startZoom) * zoomSpeed),
                                    event2 -> mapView.setZoom(finalZoomLevel)) // Zmiana zoomu
                    );
                }
            } else {
                // oddalanie
                for (int zoomLevel = startZoom; zoomLevel >= endZoom; zoomLevel--) {
                    int finalZoomLevel = zoomLevel;
                    zoomAnimation.getKeyFrames().add(
                            new KeyFrame(Duration.seconds((startZoom - zoomLevel) * zoomSpeed),
                                    event2 -> mapView.setZoom(finalZoomLevel)) // Zmiana zoomu
                    );
                }
            }

            //  dodatkowe opóźnienie
            zoomAnimation.getKeyFrames().add(
                    new KeyFrame(Duration.seconds(Math.abs(endZoom - startZoom + 1) * zoomSpeed))
            );

            // Akcja po zakończeniu animacji
            zoomAnimation.setOnFinished(event2 -> {
                System.out.println("Zoom animation completed to level: " + mapView.getZoom());


                WritableImage snapshot = mapView.snapshot(new SnapshotParameters(), null);

                try {
                    System.out.println("esa");
                    File outputFile = new File("map_snapshot.png");
                    ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", outputFile);
                    System.out.println("Snapshot saved to: " + outputFile.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                }


                int[][] terrainMap = GluonMapAnalyzer.analyzeMapFromGluon(snapshot, matrixWidth, matrixHeight);
                simulation.setMap(new SimMap(terrainMap));
                openSimulationPanel(primaryStage, simulation, terrainMap, snapshot);
            });

            // Uruchom animację
            zoomAnimation.play();


        });


        Label controlPanelLabel = new Label("Map Control Panel");
        controlPanelLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        VBox coordinatesContainer = new VBox();
        coordinatesContainer.setSpacing(10);
        coordinatesContainer.getChildren().addAll(
                latitudeField,
                longitudeField,
                locationNameField

        );


        HBox buttonContainer = new HBox();
        buttonContainer.setSpacing(10);
        buttonContainer.getChildren().addAll(
                setMapPositionButton,
                saveCoordinatesButton

        );

        HBox buttonContainer2 = new HBox();
        buttonContainer2.setSpacing(10);
        buttonContainer2.getChildren().addAll(
                savedCoordinatesDropdown,
                goToSavedLocationButton

        );

        HBox buttonContainer3 = new HBox();
        buttonContainer3.setSpacing(10);
        buttonContainer3.getChildren().addAll(
                toggleDrawingCheckBox,
                captureButton
        );


        rightControlPanel.setAlignment(Pos.TOP_CENTER);
        coordinatesContainer.setAlignment(Pos.TOP_CENTER);
        buttonContainer.setAlignment(Pos.TOP_CENTER);
        buttonContainer2.setAlignment(Pos.TOP_CENTER);
        buttonContainer3.setAlignment(Pos.TOP_CENTER);


        rightControlPanel.getChildren().addAll(
                controlPanelLabel,
                coordinatesContainer,
                buttonContainer,
                buttonContainer2,
                buttonContainer3

        );


        HBox mapContainer = new HBox();
        mapContainer.setSpacing(10);
        mapContainer.getChildren().addAll(mapView, rightControlPanel);

// Ustawienie sceny
        Scene mapScene = new Scene(mapContainer, 1400, 1030);
        primaryStage.setScene(mapScene);
        primaryStage.setTitle("Map View - Capture Simulation");
        primaryStage.show();


    }


    //int[][] terrainMap = MapGenerator.generate(width, height);
    private void openSimulationPanel(Stage primaryStage, SimCore simulation, int[][] terrainMap, WritableImage snapshot) {
        //int[][] terrainMap = GluonMapAnalyzer.analyzeMapFromGluon(mapView, 5, 5);
        simulation.setMap(new SimMap(terrainMap));

        // Panel symulacji z mapą terenu *20 czyli jden kefelek 20px
        SimulationPanel panel = new SimulationPanel(terrainMap[0].length * widthRectangle, terrainMap.length * heightRectangle,
                simulation.getGroups(), terrainMap, snapshot);

        // Panel kontrolny
        VBox controlPanel = new VBox();
        controlPanel.setSpacing(10);
        controlPanel.setAlignment(Pos.TOP_CENTER);


        String buttonStyle = "-fx-background-color: black; -fx-text-fill: white; -fx-font-size: 12px;";

        TextField nameField = new TextField();
        nameField.setPromptText("Group Name");

        TextField xField = new TextField();
        xField.setPromptText("X Position");
        xField.setEditable(false);

        TextField yField = new TextField();
        yField.setPromptText("Y Position");
        yField.setEditable(false);

        HBox nameAndPositionContainer = new HBox(10);
        nameAndPositionContainer.setAlignment(Pos.TOP_CENTER);
        nameAndPositionContainer.getChildren().addAll(nameField, xField, yField);


        ComboBox<SimForceType> forceTypeComboBox = new ComboBox<>();
        forceTypeComboBox.getItems().addAll(SimForceType.BLUFORCE, SimForceType.REDFORCE);
        forceTypeComboBox.setPromptText("Force Type");

        ComboBox<String> battalionTypeComboBox = new ComboBox<>();
        battalionTypeComboBox.getItems().addAll(
                "Abrams Tank Battalion",
                "Leopard Tank Battalion",
                "T-90 Tank Battalion",
                "Mechanized Battalion",
                "Infantry Battalion",
                "Artillery Battalion",
                "Rocket Artillery Battalion",
                "Mixed Tank Battalion",
                "Fire Support Battalion",
                "Heavy Battalion"
        );
        battalionTypeComboBox.setPromptText("Battalion Type");

        TextField unitCountField = new TextField();
        unitCountField.setPromptText("Unit Count");

        HBox typeAndUnitContainer = new HBox(10);
        typeAndUnitContainer.setAlignment(Pos.TOP_CENTER);
        typeAndUnitContainer.getChildren().addAll(forceTypeComboBox, battalionTypeComboBox, unitCountField);

        Button addButton = new Button("Add Group");
        addButton.setStyle(buttonStyle);

        Button startButton = new Button("Start Simulation");
        startButton.setStyle(buttonStyle);

        Button pauseButton = new Button("Pause Simulation");
        startButton.setStyle(buttonStyle);
        pauseButton.setDisable(true);

        Button resumeButton = new Button("Resume Simulation");
        startButton.setStyle(buttonStyle);
        resumeButton.setDisable(true);

        HBox buttonsContainer = new HBox(10);
        buttonsContainer.setAlignment(Pos.TOP_CENTER);
        buttonsContainer.getChildren().addAll(addButton, startButton);

        HBox simulationControlButtons = new HBox(10);
        simulationControlButtons.setAlignment(Pos.TOP_CENTER);
        simulationControlButtons.getChildren().addAll(pauseButton, resumeButton);

        Label ad = new Label("Add New Group");
        ad.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        controlPanel.getChildren().addAll(
                ad,
                nameAndPositionContainer,
                typeAndUnitContainer

        );


        ComboBox<String> taskDropdown = new ComboBox<>();
        taskDropdown.setPromptText("Select Task");
        taskDropdown.getItems().addAll("MOVE", "ATTACK", "DEFENCE");


        ComboBox<SimGroup> groupDropdown = new ComboBox<>();
        groupDropdown.setPromptText("Select Group");

        groupDropdown.setItems(FXCollections.observableArrayList(
                simulation.getGroups().stream()
                        .filter(group -> !group.isDestroyed())
                        .toList()
        ));
        AnimationTimer comboBoxUpdater = new AnimationTimer() {
            @Override
            public void handle(long now) {
                groupDropdown.setItems(FXCollections.observableArrayList(
                        simulation.getGroups().stream()
                                .filter(group -> !group.isDestroyed())
                                .toList()
                ));
            }
        };
        comboBoxUpdater.start();

        //groupDropdown.setItems(FXCollections.observableArrayList(simulation.getGroups()));

        HBox buttonContainer3 = new HBox();
        buttonContainer3.setSpacing(10);
        buttonContainer3.getChildren().addAll(
                taskDropdown,
                groupDropdown
        );
        buttonContainer3.setAlignment(Pos.TOP_CENTER);

        TextField taskXField = new TextField();
        taskXField.setPromptText("Target X");

        TextField taskYField = new TextField();
        taskYField.setPromptText("Target Y");

        Button addTaskButton = new Button("Add Task");
        addTaskButton.setStyle("-fx-background-color: black; -fx-text-fill: white;");

        Label speedLabel = new Label("Simulation Speed:");
        speedLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: black;");

        Slider speedSlider = new Slider(1, 500, simulation.getTimeOfOneStep());
        speedSlider.setShowTickMarks(true);
        speedSlider.setShowTickLabels(true);
        speedSlider.setMajorTickUnit(100);
        speedSlider.setMinorTickCount(4);
        speedSlider.setBlockIncrement(10);

        speedSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            int newSpeed = newValue.intValue();
            simulation.setTimeOfOneStep(newSpeed); // Zmieniamy prędkość symulacji
            System.out.println("Zmieniono prędkość symulacji na: " + newSpeed + " ms na krok");
        });

        VBox speedControl = new VBox(5);
        speedControl.setAlignment(Pos.TOP_CENTER);
        speedControl.getChildren().addAll(speedLabel, speedSlider);

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

                SimCommander commander = new SimCommander();
                // commander.addCommand(new SimCommand(SimCommandType.MOVE, new SimPosition(2,5)));
                //  commander.addCommand(new SimCommand(SimCommandType.MOVE, new SimPosition(19,23)));
                //  commander.addCommand(new SimCommand(SimCommandType.MOVE, new SimPosition(15,10)));
                commander.addGroups(newGroup);//dodaje grupe do dowodcy

                System.out.println("commander" + commander);
                simulation.addCommanders(commander);
                simulation.addGroup(newGroup);
                System.out.println(simulation.getGroups());
                // Aktualizacja panelu symulacji
                panel.updateGroups(simulation.getGroups());

                groupDropdown.getItems().clear();
                groupDropdown.getItems().addAll(simulation.getGroups());
                System.out.println("Group " + newGroup.getName() + " created with its commander.");

                // Czyszczenie pól
                nameField.clear();
                xField.clear();
                yField.clear();
                forceTypeComboBox.setValue(null);
                battalionTypeComboBox.setValue(null);
                unitCountField.clear();

            } catch (Exception e) {
                // Wyświetlanie błędu
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Input Error");
                alert.setHeaderText("Failed to add group");
                alert.setContentText("Please check your inputs.");
                alert.showAndWait();
            }
        });


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
                pauseButton.setDisable(false);
            }
        });

        pauseButton.setOnAction(event -> {
            simulation.pauseSimulation();
            pauseButton.setDisable(true);
            resumeButton.setDisable(false);
            System.out.println("Symulacja została wstrzymana.");
        });

        resumeButton.setOnAction(event -> {
            simulation.resumeSimulation();
            resumeButton.setDisable(true);
            pauseButton.setDisable(false);
            System.out.println("Symulacja została wznowiona.");
        });


        panel.setOnMouseClicked((MouseEvent event) -> {
            // Obliczamy współrzędne na podstawie klikniętej pozycji
            int x = (int) Math.floor(event.getX() / heightRectangle); // Skala 20x20 na mapie
            int y = (int) Math.floor(event.getY() / widthRectangle);


            xField.setText(String.valueOf(x));
            yField.setText(String.valueOf(y));

            taskXField.setText(String.valueOf(x));
            taskYField.setText(String.valueOf(y));


        });
        //////////////////oblsluga komend////////////////////////////////////////////////////////////////////////////////////////


        addTaskButton.setOnAction(event -> {
            SimGroup selectedGroup = groupDropdown.getValue();
            String selectedTask = taskDropdown.getValue();

            if (selectedGroup == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Input Error");
                alert.setHeaderText("No Group Selected");
                alert.setContentText("Please select a group.");
                alert.showAndWait();
                return;
            }


            if (selectedTask == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Input Error");
                alert.setHeaderText("No Task Selected");
                alert.setContentText("Please select a task.");
                alert.showAndWait();
                return;
            }

            try {
                // Pobierz współrzędne celu
                double targetX = Integer.parseInt(taskXField.getText());
                double targetY = Integer.parseInt(taskYField.getText());

                // Tworzenie komendy
                SimCommandType commandType = SimCommandType.valueOf(selectedTask);
                SimCommand newCommand = new SimCommand(commandType, new SimPosition(targetX, targetY));

                //potrzebuje sprawdzic jakiego comandera ma wybrana grupa i do niego dodac komende
                selectedGroup.commander.addCommand(newCommand);
                //   selectedGroup
                // SimCommander commadner = new SimCommander();
                //commadner.addGroups(selectedGroup);

                // Czyszczenie pól
                taskDropdown.setValue(null);
                taskXField.clear();
                taskYField.clear();

            } catch (NumberFormatException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Input Error");
                alert.setHeaderText("Invalid Coordinates");
                alert.setContentText("Please enter valid numeric coordinates.");
                alert.showAndWait();
            }
        });
        Label assignTaskLabel = new Label("Assign Task to Group");
        assignTaskLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        // Dodanie elementów do panelu kontrolnego
        controlPanel.getChildren().addAll(

                nameField,
                xField,
                yField,
                buttonsContainer,
                simulationControlButtons,
                assignTaskLabel,
                buttonContainer3,
                taskXField,
                taskYField,
                addTaskButton,
                speedControl

        );


        Separator verticalSeparator = new Separator();
        verticalSeparator.setOrientation(javafx.geometry.Orientation.VERTICAL);
        verticalSeparator.setPrefHeight(800);
        HBox root = new HBox();
        root.getChildren().addAll(panel, verticalSeparator, controlPanel);


        // Scena i okno główne rozmiar
        Scene scene = new Scene(root, 1400, 1030);
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
            case "Abrams Tank Battalion":
                return new BattalionManager.AbramsTankBattalion(name, position, forceType, unitCount);
            case "Leopard Tank Battalion":
                return new BattalionManager.LeopardTankBattalion(name, position, forceType, unitCount);
            case "T-90 Tank Battalion":
                return new BattalionManager.T90TankBattalion(name, position, forceType, unitCount);
            case "Mechanized Battalion":
                return new BattalionManager.MechanizedBattalion(name, position, forceType, unitCount);
            case "Infantry Battalion":
                return new BattalionManager.InfantryBattalion(name, position, forceType, unitCount);
            case "Artillery Battalion":
                return new BattalionManager.ArtilleryBattalion(name, position, forceType, unitCount);
            case "Rocket Artillery Battalion":
                return new BattalionManager.RocketArtilleryBattalion(name, position, forceType, unitCount);
            case "Mixed Tank Battalion":
                return new BattalionManager.MixedTankBattalion(name, position, forceType, unitCount);
            case "Fire Support Battalion":
                return new BattalionManager.FireSupportBattalion(name, position, forceType, unitCount);
            case "Heavy Battalion":
                return new BattalionManager.HeavyBattalion(name, position, forceType, unitCount);
            default:
                throw new IllegalArgumentException("Invalid battalion type: " + battalionType);
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}
