package pl.sim.frontend;

import com.gluonhq.attach.storage.StorageService;
import com.gluonhq.attach.util.Services;
import com.gluonhq.maps.MapLayer;
import com.gluonhq.maps.MapPoint;
import com.gluonhq.maps.MapView;
import com.gluonhq.maps.tile.TileRetriever;
import com.gluonhq.maps.tile.TileRetrieverProvider;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class GluonApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Reszta kodu do wyświetlania mapy
        MapView mapView = new MapView();
        MapPoint warsaw = new MapPoint(52.2297, 21.0122);
        mapView.setZoom(10);
        mapView.setCenter(warsaw);
        mapView.setPrefSize(800, 600);

        //Pobieranie obrazu do generowania mapy
        TileRetriever tileRetriever = TileRetrieverProvider.getInstance().load();
        CompletableFuture<Image> future = tileRetriever.loadTile(9, 2 * 143, 2 * 83); // Okolice ostrołęki
        Image image = future.join();
        System.out.println("Obraz załadowany!");
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(800);
        imageView.setFitHeight(600);
        imageView.setPreserveRatio(true);

        // Dodaj MapView i ImageView do StackPane
//        StackPane root = new StackPane(mapView); //Wyświetla mapę
        StackPane root = new StackPane(imageView); //Wyświetla zdjęcie
        Scene scene = new Scene(root, 800, 600);


        primaryStage.setTitle("Mapa z Gluon Maps");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}