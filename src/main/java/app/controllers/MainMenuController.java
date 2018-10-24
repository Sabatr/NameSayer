package app.controllers;

import app.backend.NameEntry;
import app.tools.AchievementsManager;
import app.tools.FileFinder;
import app.tools.HelpHandler;
import app.tools.MicPaneHandler;
import app.views.SceneBuilder;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXPopup;
import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.skins.JFXProgressBarSkin;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.net.URISyntaxException;

/**
 * This class holds the functionality of the main menu.
 */
public class MainMenuController extends ParentController {
    @FXML private JFXButton _helpButton;
    @FXML private JFXButton _micButton;

    /**
     * The main menu does not need to know anything about the app state, so there's no implementation here
     */
    @Override
    public void setInformation(SceneBuilder switcher, ObservableList<NameEntry> allNames, ObservableList<NameEntry> selectedNames) {
        super.setInformation(switcher, allNames, selectedNames);
    }

    @Override
    public void switchTo() {}

    /**
     * Allows the scene to switch the view list.
     */
    @FXML
    private void goToList() {
        _switcher.switchScene(SceneBuilder.LISTVIEW);
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

    @FXML
    private void help() {
        new HelpHandler(_helpButton,"main");
    }

    @FXML
    private void getMic() {
        MicPaneHandler.getHandler().show(_micButton);
    }

    @FXML
    private void goToAchievements() {
        AchievementsManager.getInstance().setMenu(SceneBuilder.MENU);
        _switcher.switchScene(SceneBuilder.ACHIEVEMENTS);
    }
}
