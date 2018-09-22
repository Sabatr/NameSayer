package app.controllers;

import app.backend.BashRunner;
import app.backend.NameEntry;
import app.views.SceneBuilder;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;

import java.io.IOException;


/**
 * This class controls the options page for checking the level of the user's microphone.
 * When the user toggles their microphone, a Task is triggered which records a very brief segment of audio using
 * ffmpeg as a separate process. When the task notifies the controller, it fetches the value and continues until the
 * user toggles their microphone off or goes back to the main menu.
 */
public class OptionsController extends ParentController implements EventHandler<WorkerStateEvent> {

    ObservableList _selections;
    @FXML   // a progress bar is used as the visual indicator of microphone level
    ProgressBar levelIndicator;
    @FXML
    Button toggleButton;
    boolean micToggled;

    @FXML
    public void initialise() {
        micToggled = false;
    }

    @FXML
    private void toggleMic() {
        if(micToggled) {
            toggleButton.setVisible(false);
            micToggled = false;
        } else {
            BashRunner runner = new BashRunner(this);
        }

    }

    @Override
    public void handle(WorkerStateEvent event) {

    }

    @FXML
    private void goBack() throws IOException {
        SceneBuilder builder = new SceneBuilder(_allNames, _stage);
        builder.getList(_selections);
        builder.load("MainMenu.fxml");
    }

    @Override
    public void setInformation(ObservableList<NameEntry> allItems, ObservableList<NameEntry> items) {
        super.setInformation(allItems, items);
        _selections = items;
    }
}
