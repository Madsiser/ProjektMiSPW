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
            savedCoordinates = gson.fromJson(reader, new TypeToken<Map<String, MapPoint>>() {}.getType());
        } catch (Exception e) {
            System.out.println("Błąd podczas ładowania pliku: " + e.getMessage());
            savedCoordinates = new HashMap<>();
        }
    }
    public static int heightRectangle =10;
    public static int widthRectangle =10;
    public static int matrixWidth = 100;
    public static int matrixHeight=100;

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



// Wywołanie ładowania danych przy uruchomieniu programu
        loadCoordinatesFromFile();

// Panel kontrolny po prawej stronie
        VBox rightControlPanel = new VBox();
        rightControlPanel.setSpacing(10);
        rightControlPanel.setPrefWidth(300);

// Pola tekstowe do wpisywania współrzędnych
        TextField latitudeField = new TextField();
        latitudeField.setPromptText("Latitude (Szerokość geograficzna)");
        latitudeField.setText(String.valueOf(warsaw.getLatitude())); // Ustaw wartość początkową

        TextField longitudeField = new TextField();
        longitudeField.setPromptText("Longitude (Długość geograficzna)");
        longitudeField.setText(String.valueOf(warsaw.getLongitude())); // Ustaw wartość początkową

        TextField locationNameField = new TextField();
        locationNameField.setPromptText("Location Name");

// Lista rozwijana do wyboru zapisanych współrzędnych
        ObservableList<String> savedLocationsList = FXCollections.observableArrayList(savedCoordinates.keySet());
        ComboBox<String> savedCoordinatesDropdown = new ComboBox<>(savedLocationsList);
        savedCoordinatesDropdown.setPromptText("Select Saved Location");

// Przycisk do zapisywania współrzędnych
        Button saveCoordinatesButton = new Button("Save Coordinates");
        saveCoordinatesButton.setStyle("-fx-background-color: #28A745; -fx-text-fill: white; -fx-font-size: 14px;");

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

                // Dodanie lokalizacji do listy rozwijanej, jeśli jej jeszcze nie ma
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

        // Przycisk do ustawienia pozycji mapy
        Button setMapPositionButton = new Button("Set Map Position");
        setMapPositionButton.setStyle("-fx-background-color: #007BFF; -fx-text-fill: white; -fx-font-size: 14px;");

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
                // Obsługa błędu, jeśli współrzędne są nieprawidłowe
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Input Error");
                alert.setHeaderText("Invalid Coordinates");
                alert.setContentText("Please enter valid latitude and longitude values.");
                alert.showAndWait();
            }
        });

        //widocznosc siatki
        Button toggleDrawingButton = new Button("Toggle Terrain Values");
        toggleDrawingButton.setOnAction(event -> {
            drawTerrainValues = !drawTerrainValues; // Przełączanie stanu
            System.out.println("Drawing terrain values: " + (drawTerrainValues ? "ON" : "OFF"));

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

            // Informacja w konsoli (opcjonalnie)
            System.out.println("Clicked coordinates: Latitude = " + clickedPoint.getLatitude() + ", Longitude = " + clickedPoint.getLongitude());
        });
// Przycisk do ustawiania pozycji na podstawie wybranych współrzędnych
        Button goToSavedLocationButton = new Button("Go To Saved Location");
        goToSavedLocationButton.setStyle("-fx-background-color: #FFC107; -fx-text-fill: black; -fx-font-size: 14px;");

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

        mapView.setOnScroll(event -> {
            double deltaY = event.getDeltaY(); // Kierunek przewijania
            int currentZoom = (int) mapView.getZoom();
            int newZoom = currentZoom;

            if (deltaY > 0) {
                // Przybliżanie
                newZoom = Math.min(currentZoom + 1, 20);
            } else {
                // Oddalanie
                newZoom = Math.max(currentZoom - 1, 1);
            }

            // Pozycja kursora myszy w pikselach względem mapy
            double mouseX = event.getX();
            double mouseY = event.getY();

            // Przekształcenie współrzędnych pikselowych na współrzędne geograficzne
            MapPoint cursorPosition = mapView.getMapPosition(mouseX, mouseY);

            // Zmiana zoomu
            mapView.setZoom(newZoom);

            // Ponowne ustawienie centrum mapy w taki sposób, aby miejsce pod kursorem pozostało w tym samym punkcie
            if (cursorPosition != null) {
                mapView.setCenter(cursorPosition);
            }

            // Opcjonalne logowanie
            System.out.println("Zoom level: " + newZoom + ", Center: " + mapView.getCenter());
        });



        // Przycisk do przejścia do symulacji
        Button captureButton = new Button("Capture Map and Start Simulation");
        captureButton.setStyle("-fx-background-color: red; -fx-text-fill: white; -fx-font-size: 16px;");

        captureButton.setOnAction(event -> {
            double latitude = Double.parseDouble(latitudeField.getText());
            double longitude = Double.parseDouble(longitudeField.getText());

            // Aktualizacja pozycji mapy

            newCenter = new MapPoint(latitude,longitude);


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

            // Dodaj dodatkowe opóźnienie
            zoomAnimation.getKeyFrames().add(
                    new KeyFrame(Duration.seconds(Math.abs(endZoom - startZoom + 1) * zoomSpeed))
            );

            // Akcja po zakończeniu animacji
            zoomAnimation.setOnFinished(event2 -> {
                System.out.println("Zoom animation completed to level: " + mapView.getZoom());

                // Dalsze kroki po zakończeniu animacji
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



// Dodanie elementów do panelu kontrolnego
        rightControlPanel.getChildren().addAll(
                new Label("Control Panel (Right)"),
                latitudeField,
                longitudeField,
                locationNameField,
                saveCoordinatesButton,
                savedCoordinatesDropdown,
                goToSavedLocationButton,
                setMapPositionButton,
                captureButton,
                toggleDrawingButton
        );

// Główny kontener (HBox) dla mapy i panelu kontrolnego
        HBox mapContainer = new HBox();
        mapContainer.setSpacing(10);
        mapContainer.getChildren().addAll(mapView, rightControlPanel);

// Ustawienie sceny
        Scene mapScene = new Scene(mapContainer, 1600, 1030);
        primaryStage.setScene(mapScene);
        primaryStage.setTitle("Map View - Capture Simulation");
        primaryStage.show();


    }


    //int[][] terrainMap = MapGenerator.generate(width, height);
    private void openSimulationPanel(Stage primaryStage, SimCore simulation, int[][] terrainMap,WritableImage snapshot)
    {
        //int[][] terrainMap = GluonMapAnalyzer.analyzeMapFromGluon(mapView, 5, 5);
        simulation.setMap(new SimMap(terrainMap));

        // Panel symulacji z mapą terenu *20 czyli jden kefelek 20px
        SimulationPanel panel = new SimulationPanel(terrainMap[0].length *widthRectangle ,terrainMap.length * heightRectangle,
                simulation.getGroups(), terrainMap,snapshot);

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

                SimCommander commander = new SimCommander();
                commander.addCommand(new SimCommand(SimCommandType.MOVE, new SimPosition(2,5)));
                commander.addCommand(new SimCommand(SimCommandType.MOVE, new SimPosition(19,23)));
                commander.addCommand(new SimCommand(SimCommandType.MOVE, new SimPosition(15,10)));
                commander.addGroups(newGroup);
                simulation.addCommanders(commander);
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
            int x = (int) Math.floor(event.getX() / heightRectangle); // Skala 20x20 na mapie
            int y = (int) Math.floor(event.getY() / widthRectangle);

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

    //seperator i tworznie okna
        Separator verticalSeparator = new Separator();
        verticalSeparator.setOrientation(javafx.geometry.Orientation.VERTICAL);
        verticalSeparator.setPrefHeight(800);
        HBox root = new HBox();
        root.getChildren().addAll(panel, verticalSeparator, controlPanel);



        // Scena i okno główne rozmiar
        Scene scene = new Scene(root, 1600, 1030);
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
