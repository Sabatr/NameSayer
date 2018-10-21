package app.controllers;

import app.backend.BashRunner;
import app.backend.NameEntry;
import app.tools.AudioPlayer;
import app.tools.Timer;
import app.views.SceneBuilder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class UserRecordingsController extends ParentController implements EventHandler<WorkerStateEvent> {

    @FXML public Label _nameDisplayed;
    @FXML public HBox _buttonsHBox;
    @FXML public Button _deleteButton;
    @FXML public Button _compareButton;
    @FXML public Button _backButton;
    @FXML public Button _dbVersionButton;
    @FXML public ProgressBar _progressBar;
    @FXML public Slider _volumeSlider;
    @FXML public ComboBox<String> _dropdown;
    @FXML public Button _userVersionButton;

    private NameEntry _name;
    private final String AUDIO_TASK_MESSAGE = "PlayAudio";
    private final String COMPARISON_MESSAGE = "CompareAudio";

    @FXML
    public void initialize() {
        _volumeSlider.valueProperty().bindBidirectional(PracticeController._volume);
    }

    /**
     * Delete the version selected by the user. Provides a confirmation warning first.
     */
    @FXML
    public void deleteUserRecording() {
        String versionToRemove = _dropdown.getSelectionModel().getSelectedItem();
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setContentText("Delete user recording");
        a.setContentText("Really delete recording " + versionToRemove + " of " + _name.getName() + "?");
        Optional<ButtonType> option = a.showAndWait();

        if(option.isPresent()) {
            if(option.get() == ButtonType.OK) {
                _name.deleteUserVersion(versionToRemove);
            }
        }
        setupDropdown();
    }

    /**
     * Play the highest ranking database version, then play the selected user version
     */
    @FXML
    public void compare() throws URISyntaxException {
        disableButtons();
        String databaseVersion = _name.getHighestRating();
        playGenericAudio(COMPARISON_MESSAGE, _name.getAudioForVersion(databaseVersion));
    }

    /**
     * Play the highest ranking database version
     */
    @FXML
    public void playDatabaseRecording() throws URISyntaxException {
        disableButtons();
        String databaseVersion = _name.getHighestRating();
        playGenericAudio(AUDIO_TASK_MESSAGE, _name.getAudioForVersion(databaseVersion));
    }

    /**
     * Play the user version of the Name, as selected in the dropdown
     */
    @FXML
    public void playUserRecording() throws URISyntaxException {
        disableButtons();
        String userVersion = _dropdown.getSelectionModel().getSelectedItem();
        if(userVersion == null || userVersion.isEmpty()) {
            return;
        }
        playGenericAudio(AUDIO_TASK_MESSAGE, _name.getAudioForVersion(userVersion));
    }

    /**
     * Plays the current name's audio.
     * Used for playing both Database audio and audio the user has just recorded
     * @param taskTitle The title to pass to the {@link javafx.concurrent.Task} for determining whether or not we were
     *                  playing recorded audio or database audio
     * @param audioFilePath The path to the audio file to play
     */
    private void playGenericAudio(String taskTitle, Path audioFilePath) throws URISyntaxException {
        File audioResource = audioFilePath.toFile();
        AudioPlayer player = new AudioPlayer(audioResource, this, taskTitle);
        BashRunner br = new BashRunner(this);
        float timeInSeconds = player.getLength();

        double max = _volumeSlider.getMax();
        double min = _volumeSlider.getMin();
        double value = _volumeSlider.getValue();

        double volume = ((value - min) / (max - min)) * 100;
        br.runPlayAudioCommand(audioFilePath, taskTitle, volume);

        _progressBar.setVisible(true);
        Timer timer = new Timer(_progressBar, this, "SomethingElse", timeInSeconds);
        Thread thread1 = new Thread(timer);
        thread1.start();
    }

    /**
     * Handles the button to go back to the practice view
     */
    @FXML
    public void goBack() {
        _switcher.switchScene(SceneBuilder.PRACTICE);
    }

    /*
     * Disable buttons for audio playback
     */
    private void disableButtons() {
        _buttonsHBox.setDisable(true);
        _dbVersionButton.setDisable(true);
        _userVersionButton.setDisable(true);
    }

    /*
     * Re-enable buttons after audio playback
     */
    private void enableButtons() {
        _buttonsHBox.setDisable(false);
        _dbVersionButton.setDisable(false);
        _userVersionButton.setDisable(false);
    }

    /**
     * Handle responses from the BashRunner
     */
    @Override
    public void handle(WorkerStateEvent event) {
        if(event.getEventType().equals(WorkerStateEvent.WORKER_STATE_SUCCEEDED)) {
            String title = event.getSource().getTitle();
            if(title.equals(AUDIO_TASK_MESSAGE)) {
                enableButtons();
                _progressBar.setVisible(false);
            } else if(title.equals(COMPARISON_MESSAGE)) {
                try {
                    playUserRecording();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /* ------------ Methods for setting up the scene -------- */

    /*
     * Set the items of the user recordings dropdown
     */
    private void setupDropdown() {
        List<String> versions = _name.getUserVersions();
        _dropdown.setItems(FXCollections.observableList(versions));
        if(versions.size() > 0) {
            _dropdown.getSelectionModel().selectFirst();
        }
    }

    /**
     * Uses the parent hook method to get the information from the list view controller.
     * This is done so the practice view knows the list that is selected from the list view.
     * @param selectedNames The selected names
     */
    @Override
    public void setInformation(SceneBuilder switcher, ObservableList<NameEntry> allNames, ObservableList<NameEntry> selectedNames) {
        super.setInformation(switcher, allNames, selectedNames);
    }

    /**
     * Upon switching to this scene, reset the components
     */
    @Override
    public void switchTo() {
        _progressBar.setVisible(false);
        _name = PracticeController._selectedName;
        _nameDisplayed.setText(_name.getName());
        setupDropdown();
    }
}
