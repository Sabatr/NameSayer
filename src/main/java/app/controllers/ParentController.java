package app.controllers;

import app.backend.NameEntry;
import app.tools.AchievementsManager;
import app.tools.HelpHandler;
import app.tools.MicPaneHandler;
import app.views.SceneBuilder;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.stage.Stage;

/**
 * The parent class of all controllers.
 *
 * @author Brian Nguyen
 */
public abstract class ParentController {

    protected SceneBuilder _switcher;
    protected ObservableList<NameEntry> _allNames;

    /**
     * Initialise the controller with information
     */
    public void setInformation(SceneBuilder switcher, ObservableList<NameEntry> allNames, ObservableList<NameEntry> selectedNames) {
        _switcher = switcher;
        _allNames = allNames;
    }

    /**
     * Notify a controller that the scene is being switched to.
     * App state is all set to point at the original as initalised in Main or SceneBuilder, so it does not need to
     * be passed each time.
     */
    public abstract void switchTo();

}
