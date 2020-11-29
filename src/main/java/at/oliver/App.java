package at.oliver;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX App - Path Finding Visualization
 */
public class App extends Application {

    private static Scene scene;

    public static void main(String[] args) {
        App.launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(App.class.getResource("App.fxml"));

        App.scene = new Scene(root);

        stage.setScene(scene);
        stage.getIcons().add(new Image(App.class.getResourceAsStream("icon.png")));
        stage.setTitle("Path-Finding-Algorithm");
        stage.setResizable(false);
        root.requestFocus();
        stage.show();
    }

}