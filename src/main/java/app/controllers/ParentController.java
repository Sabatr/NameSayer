package app.controllers;

import javafx.stage.Stage;

/**
 * The parent class of all controllers.
 *
 * @author Brian Nguyen
 */
public class ParentController {
    protected Stage _stage;

    /**
     * Retreives the stage so children controllers can modify it.
     * @param stage
     */
    public void setStage(Stage stage) {
        _stage = stage;
    }
}
