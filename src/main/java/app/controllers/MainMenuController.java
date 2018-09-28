package app.controllers;

import app.backend.NameEntry;
import app.views.SceneBuilder;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

import java.io.IOException;
import java.util.ArrayList;

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
}
