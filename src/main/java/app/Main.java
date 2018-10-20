package app;

import app.backend.NameEntry;
import app.backend.filesystem.FSWrapper;
import app.views.SceneBuilder;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
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
        FSWrapper fsWrap = NameEntry.populateNames();
        ArrayList<NameEntry> names = NameEntry.getNames();
//        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
//            @Override
//            public void handle(WindowEvent event) {
//                if(!event.isConsumed()) {
//                        fsWrap.deleteFiles("compositeName");
//
//                }
//            }
//        });

        SceneBuilder sceneMan = SceneBuilder.inst(FXCollections.observableArrayList(names), stage);
        sceneMan.switchScene(SceneBuilder.MENU);
    }

    public static void main(String[] args) {
        launch(args);
    }
}