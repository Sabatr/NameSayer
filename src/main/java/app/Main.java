package app;

import app.backend.NameEntry;
import app.views.SceneBuilder;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * This is where the application starts. Upon starting, the main menu is loaded.
 *
 * @author: Brian Nguyen
 */
public class Main extends Application {

    @Override
    public void start(Stage stage) throws URISyntaxException, IOException {
        stage.setResizable(false);
        stage.setTitle("Name Sayer Practice");
        ArrayList<NameEntry> names = NameEntry.populateNames();
        SceneBuilder sceneMan = SceneBuilder.inst(FXCollections.observableArrayList(names), stage);
        sceneMan.switchScene(SceneBuilder.MENU);
    }

    public static void main(String[] args) {
        launch(args);
    }
}