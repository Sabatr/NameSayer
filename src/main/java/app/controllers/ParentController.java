package app.controllers;

import javafx.collections.ObservableList;
import javafx.stage.Stage;

/**
 * The parent class of all controllers.
 *
 * @author Brian Nguyen
 */
public class ParentController {
    protected Stage _stage;

    /**
     * Retrieves the stage so children controllers can modify it.
     * @param stage
     */
    public void setStage(Stage stage) {
        _stage = stage;
    }

    //Hook method
    public void getInformation(ObservableList<String> items) {}
}
