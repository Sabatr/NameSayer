package app.controllers;

import app.backend.FSWrapper;
import app.backend.NameEntry;
import javafx.collections.ObservableList;
import javafx.stage.Stage;

/**
 * The parent class of all controllers.
 *
 * @author Brian Nguyen
 */
public class ParentController {
    protected Stage _stage;
    protected ObservableList<NameEntry> _allNames;

    /**
     * Retrieves the stage so children controllers can modify it.
     * @param stage
     */
    public void setStage(Stage stage) {
        _stage = stage;
    }

    //Hook method
    public void setInformation(ObservableList<NameEntry> allItems, ObservableList<NameEntry> items) {
        _allNames = allItems;
    }
}
