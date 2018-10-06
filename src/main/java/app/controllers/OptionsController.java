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
import java.net.URISyntaxException;
import java.nio.file.Path;


/**
 * This class controls the options page for checking the level of the user's microphone.
 * When the user toggles their microphone, a Task is triggered which records a very brief segment of audio using
 * ffmpeg as a separate process. When the task notifies the controller, it fetches the value and continues until the
 * user toggles their microphone off or goes back to the main menu.
 */
public class OptionsController extends ParentController implements EventHandler<WorkerStateEvent> {

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
    private void toggleMic() throws URISyntaxException {
        if(_micToggled) {
            _micToggled = false;
        } else {
            _micToggled = true;
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
        if(!_micToggled) {
             _levelIndicator.progressProperty().unbind();
             _levelIndicator.progressProperty().setValue(0);
            return;
        }
        if(event.getEventType().equals(WorkerStateEvent.WORKER_STATE_SUCCEEDED)) {
            //System.out.println("success");
            if(event.getSource().getTitle().equals(BashRunner.CommandType.TESTMIC.toString())) {
                String output = (String) event.getSource().getValue();
                int foreIndex = output.indexOf("mean_volume") + 13;
                int aftIndex = output.indexOf("dB", foreIndex);
                if(!(foreIndex < 0 || aftIndex < 0)) {
                    double volume;
                    try {
                        volume = Double.parseDouble(output.substring(foreIndex, aftIndex - 1));
                    } catch (NumberFormatException e) {
                        volume = -90;
                    }
                    double progress = (100 + 1.3 * volume) / 100;
                    _levelIndicator.setProgress(progress);
                } //else {
                //    System.out.println(output);
                //}

                BashRunner runner = null;
                try {
                    runner = new BashRunner(this);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                runner.runMonitorMicCommand();
            }
        } else if(event.getEventType().equals(WorkerStateEvent.WORKER_STATE_FAILED)) {
            System.out.println("Failed");
            _levelIndicator.progressProperty().unbind();
            _levelIndicator.progressProperty().setValue(0);
            _micToggled = false;
        }
    }

    @FXML
    private void goBack() throws URISyntaxException {
        if(_micToggled) {
           toggleMic();
        }
        _switcher.switchScene(SceneBuilder.MENU);
    }

    @Override
    public void setInformation(SceneBuilder switcher, ObservableList<NameEntry> allItems, ObservableList<NameEntry> items) {
        super.setInformation(switcher, allItems, items);
    }

    @Override
    public void switchTo() {}
}
