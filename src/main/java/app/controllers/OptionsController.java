package app.controllers;

import app.backend.BashRunner;
import app.backend.NameEntry;
import app.tools.AchievementsManager;
import app.views.SceneBuilder;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;


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
    private ScrollPane _achievement;
    @FXML
    private AchievementController _achievementController;
    @FXML
    private Pane _micPane;
    @FXML
    private Pane _aboutPane;
    @FXML
    private Pane _helpPane;
    @FXML
    private Pane _deviceSelectPane;
    @FXML
    private Button _deviceSelectButton;
    @FXML
    private ComboBox<String> _deviceBox;

    private boolean _micToggled;

    private enum Options {MICS,TEST,HELP,ABOUT,ACHIEVEMENTS}
    private Options _optionPicked;
    private Map<String, String> _devices;

    /**
     * This is static so that it is observable from everywhere in the program. If we didn't make this static it
     * would take a lot of effort to pass this state around. You'd have to have a getter for it, so that the SceneBuilder
     * can pass it into each other controller. Then those other controllers would have to pass it into their
     * BashRunners because that's where it would be used, in recording audio.
     */
    public static SimpleStringProperty selectedDevice;

    @FXML
    public void initialize() {
        if(System.getProperty("os.name").toLowerCase().contains("nix")) {
            _deviceSelectButton.setDisable(true);
        }
        selectedDevice = new SimpleStringProperty();
        _optionPicked = Options.TEST;
        loadPanel();
        _micToggled = false;
    }

    /**
     * Toggles the microphone selection pane.
     */
    @FXML
    private void enableChooseMic() {
        _optionPicked = Options.MICS;
        loadPanel();
        try {
            findMicDevices();
        } catch (URISyntaxException e) {
            _optionPicked = Options.TEST;
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setContentText("Error fetching input devices");
            a.showAndWait();
            e.printStackTrace();
            return;
        }
    }

    /**
     * If the device list hasn't already been populated, it attempts to scan the devices available to ffmpeg.
     */
    private void findMicDevices() throws URISyntaxException {
        if(_devices != null) {
            return;
        }

        _devices = new HashMap<>();
        BashRunner br = new BashRunner(this);
        br.runDeviceList();
    }

    /**
     * Parses the list of devices from the output of ffmpeg, then sets the choice box to contain them
     */
    private void parseDevices(String ffmpegOut) {
        if(!ffmpegOut.contains("DirectShow audio")) {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setContentText("No audio devices found");
            a.showAndWait();

            _optionPicked = Options.TEST;
            loadPanel();
            return;
        }

        boolean pastAudioHeader = false;
        String[] lines = ffmpegOut.split("\n");
        for(int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if(line.contains("DirectShow audio devices")) {
                pastAudioHeader = true;
                continue;
            }

            if(pastAudioHeader && line.contains("Alternative name")) {
                String lineBefore = lines[i - 1];
                int firstQuote = lineBefore.indexOf("\"");
                int secondQuote = lineBefore.indexOf("\"", firstQuote + 1);
                String name = lineBefore.substring(firstQuote + 1, secondQuote);

                firstQuote = line.indexOf("\"");
                secondQuote = line.indexOf("\"", firstQuote + 1);
                String altName = line.substring(firstQuote + 1, secondQuote);

                _devices.put(name, altName);
            }
        }

        _deviceBox.setItems(FXCollections.observableArrayList(_devices.keySet()));
    }

    /**
     * Selects a device as the mic to use
     */
    @FXML
    private void onDeviceSelect() {
        selectedDevice.set(_devices.get(_deviceBox.getValue()));
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

    @FXML private void help() {
        _optionPicked = Options.HELP;
        loadPanel();
    }
    @FXML private void about() {
        _optionPicked = Options.ABOUT;
        loadPanel();
    }

    @FXML private void achievements() {
        _optionPicked = Options.ACHIEVEMENTS;
        loadPanel();

    }

    private void loadPanel() {
        switch (_optionPicked) {
            case MICS:
                _deviceSelectPane.setVisible(true);
                _micPane.setVisible(false);
                _helpPane.setVisible(false);
                _aboutPane.setVisible(false);
                break;
            case TEST:
                _deviceSelectPane.setVisible(false);
                _micPane.setVisible(true);
                _helpPane.setVisible(false);
                _aboutPane.setVisible(false);
                _achievement.setVisible(false);
                break;
            case HELP:
                _deviceSelectPane.setVisible(false);
                _helpPane.setVisible(true);
                _micPane.setVisible(false);
                _aboutPane.setVisible(false);
               _achievement.setVisible(false);
                break;
            case ABOUT:
                _deviceSelectPane.setVisible(false);
                _aboutPane.setVisible(true);
                _micPane.setVisible(false);
                _helpPane.setVisible(false);
                _achievement.setVisible(false);
                break;
            case ACHIEVEMENTS:
                _deviceSelectPane.setVisible(false);
                _achievement.setVisible(true);
                _aboutPane.setVisible(false);
                _micPane.setVisible(false);
                _helpPane.setVisible(false);

        }
    }

    /**
     * If the toggle button is stll toggled, continue the chain of processes. If not, then
     * stop the chain.
     */
    @Override
    public void handle(WorkerStateEvent event) {
        if(event.getEventType().equals(WorkerStateEvent.WORKER_STATE_SUCCEEDED)) {
            if(event.getSource().getTitle().equals(BashRunner.CommandType.TESTMIC.toString())) {
                if(!_micToggled) {
                    _levelIndicator.progressProperty().unbind();
                    _levelIndicator.progressProperty().setValue(0);
                    return;
                }

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
                }
                BashRunner runner = null;
                try {
                    runner = new BashRunner(this);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                runner.runMonitorMicCommand();
            } else if(event.getSource().getTitle().equals(BashRunner.CommandType.LISTDEVICES.toString())) {
                parseDevices((String) event.getSource().getValue());
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
    public void switchTo() {
      _achievementController.update();
    }
}
