package app.controllers;

import app.backend.NameEntry;
import app.views.SceneBuilder;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.stage.Stage;

/**
 * The parent class of all controllers.
 *
 * @author Brian Nguyen
 */
public abstract class ParentController {

    protected SceneBuilder _switcher;
    protected ObservableList<NameEntry> _allNames;      // TODO. Changes made: changed field names

    /**
     * Initialise the controller with information
     */
    public void setInformation(ObservableList<NameEntry> allNames, ObservableList<NameEntry> selectedNames) {
        _allNames = allNames;
    }

    // TODO Controllers still need to be notified of when they get switched to, so I put this in.
    /**
     * Notify a controller that the scene is being switched to.
     * App state is all set to point at the original as initalised in Main or SceneBuilder, so it does not need to
     * be passed each time.
     */
    public abstract void switchTo();
}
