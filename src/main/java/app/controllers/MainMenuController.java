package app.controllers;

import app.backend.NameEntry;
import app.tools.AchievementsManager;
import app.tools.FileFinder;
import app.views.SceneBuilder;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URISyntaxException;

/**
 * This class holds the functionality of the main menu.
 */
public class MainMenuController extends ParentController {
    @FXML private Button _databaseButton;

    /**
     * The main menu does not need to know anything about the app state, so there's no implementation here
     */
    @Override
    public void setInformation(SceneBuilder switcher, ObservableList<NameEntry> allNames, ObservableList<NameEntry> selectedNames) {
        super.setInformation(switcher, allNames, selectedNames);
    }

    // No implementation is needed.
    @Override
    public void switchTo() {}

    /**
     * Allows the scene to switch the view list.
     */
    @FXML
    private void goToList() {
        _switcher.switchScene(SceneBuilder.LISTVIEW);
    }

    // TODO. Chances made: switching scenes is now a single call

    /**
     * Allows the scene to switch to the options menu.
     */
    @FXML
    private void options() {
        _switcher.switchScene(SceneBuilder.OPTIONS);
    }

    @FXML
    private void importData() throws URISyntaxException {
        FileFinder finder = new FileFinder("sound").choose(_switcher.getStage());
        ObservableList<NameEntry> names = finder.getContent();

        for(NameEntry name: names) {
            boolean exists = false;
            for(NameEntry compareTo: _allNames) {
                if(name.compareTo(compareTo) == 0) {
                    exists = true;
                }
            }
            if(!exists) {
                _allNames.add(name);
            }
        }
    }
}
