package app.views;

import app.backend.NameEntry;
import app.controllers.ParentController;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton class that manages scenes and switching between them.
 * Scenes are stored as a list.
 *
 * @author Brian Nguyen
 */
public class SceneBuilder {      //could be renamed SceneSwitcher
    private Stage _stage;

    // TODO.
    private ObservableList<NameEntry> _allNames;
    private ObservableList<NameEntry> _selectionList;

    public static final String MENU = "MainMenu.fxml";              // TODO. Changes made: added symbolic constants
    public static final String OPTIONS = "OptionsView.fxml";
    public static final String LISTVIEW = "ListView.fxml";
    public static final String PRACTICE = "Practice.fxml";
    private Map<String, Scene> _scenes;                             // TODO. Changes made: storing scenes in a map
    private Map<String, ParentController> _controllers;

    private static SceneBuilder inst;

    /**
     * Fetch the instance of the scenebuilder.
     * @param allNames The observable list of all {@link NameEntry}s in the application
     * @param stage The primary stage of the application
     * @return The single instance of the scenebuilder
     */
    public static SceneBuilder inst(ObservableList<NameEntry> allNames, Stage stage) throws IOException {
        if (inst == null) {
            inst = new SceneBuilder(allNames, stage);
        }
        return inst;
    }

    // TODO. Changes made: singletonised class

    private SceneBuilder(ObservableList<NameEntry> allNames, Stage stage) throws IOException {
        _scenes = new HashMap<>();
        _controllers = new HashMap<>();

        _selectionList = FXCollections.observableArrayList();
        _allNames = allNames;
        _stage = stage;
        switchScene(MENU);
    }

    /**
     * Change the current scene.
     */
    public void switchScene(String scene) {
        if(_scenes.containsKey(scene)) {
            _stage.setScene(_scenes.get(scene));
            _controllers.get(scene).switchTo();
        } else {
            try {
                initScene(scene);
                _controllers.get(scene).switchTo();
            } catch(IOException e) {
                e.printStackTrace();
                // don't switch the scene because the one passed in wasn't valid.
            }
        }
    }

    /**
     * Initialises the scene of a view for the first time.
     * @param sceneFXML The name of the FXML file for the view
     * @throws IOException if the FXML file is not found
     */
    private void initScene(String sceneFXML) throws IOException {
        FXMLLoader loader = new FXMLLoader(this.getClass().getResource(sceneFXML));
        Parent root = loader.load();
        ParentController controller = (ParentController) loader.getController();
        controller.setInformation(this, _allNames, _selectionList);
        Scene scene = new Scene(root);
        _scenes.put(sceneFXML, scene);
        _controllers.put(sceneFXML, controller);
        _stage.setScene(scene);
        _stage.show();
    }

    /**
     * Retrieves the stage.
     * @return
     */
    public Stage getStage() {
        return _stage;
    }
}
