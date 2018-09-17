package app;

import app.views.SceneBuilder;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * This is where the application starts. Upon starting, the main menu is loaded.
 *
 * @author: Brian Nguyen
 */
public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        stage.setResizable(false);
        stage.setTitle("Name Sayer Practice");
        new SceneBuilder(stage).load("ListView.fxml");
    }

    public static void main(String[] args) {
        launch(args);
    }
}