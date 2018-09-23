package app.controllers;

import app.backend.BashRunner;
import app.backend.NameEntry;
import app.views.SceneBuilder;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Pane;

import java.io.IOException;


/**
 * This class controls the options page for checking the level of the user's microphone.
 * When the user toggles their microphone, a Task is triggered which records a very brief segment of audio using
 * ffmpeg as a separate process. When the task notifies the controller, it fetches the value and continues until the
 * user toggles their microphone off or goes back to the main menu.
 */
public class OptionsController extends ParentController implements EventHandler<WorkerStateEvent> {

    private ObservableList _selections;
    @FXML   // a progress bar is used as the visual indicator of microphone level
    private ProgressBar _levelIndicator;
    @FXML
    private Button _toggleButton;
    @FXML
    private Pane _micPane;
    @FXML
    private Pane _aboutPane;
    @FXML
    private Pane _helpPane;
    private boolean _micToggled;
    private enum Options {TEST,HELP,ABOUT}
    private Options _optionPicked;

    boolean micToggled;

    @FXML
    public void initialize() {
        _optionPicked = Options.TEST;
        loadPanel();
        _micToggled = false;
    }

    /**
     * Toggle the chain of BashRunner processes
     */
    @FXML
    private void toggleMic() {
        if(_micToggled) {
            _toggleButton.setVisible(false);
            _micToggled = false;
        } else {
            micToggled = true;
            BashRunner runner = new BashRunner(this);
            runner.runMonitorMicCommand();
        }

    }

    @FXML
    private void testButton() {
        _optionPicked = Options.TEST;
        loadPanel();
    }

    @FXML void help() {
        _optionPicked = Options.HELP;
        loadPanel();
    }
    @FXML void about() {
        _optionPicked = Options.ABOUT;
        loadPanel();
    }

    private void loadPanel() {
        switch (_optionPicked) {
            case TEST:
                _micPane.setVisible(true);
                _helpPane.setVisible(false);
                _aboutPane.setVisible(false);
                break;
            case HELP:
                _helpPane.setVisible(true);
                _micPane.setVisible(false);
                _aboutPane.setVisible(false);
                break;
            case ABOUT:
                _aboutPane.setVisible(true);
                _micPane.setVisible(false);
                _helpPane.setVisible(false);
                break;
        }

    }

    /**
     * If the toggle button is stll toggled, continue the chain of processes. If not, then
     * stop the chain.
     */
    @Override
    public void handle(WorkerStateEvent event) {
        if(!micToggled) {
             levelIndicator.progressProperty().unbind();
             levelIndicator.progressProperty().setValue(0);
            return;
        }
        if(event.getEventType().equals(WorkerStateEvent.WORKER_STATE_SUCCEEDED)) {
            if(event.getSource().getTitle().equals(BashRunner.CommandType.TESTMIC.toString())) {
                event.getSource().getValue();

                BashRunner runner = new BashRunner(this);
            }
        } else if(event.getEventType().equals(WorkerStateEvent.WORKER_STATE_FAILED)) {
            levelIndicator.progressProperty().unbind();
            levelIndicator.progressProperty().setValue(0);
            micToggled = false;
        }
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
