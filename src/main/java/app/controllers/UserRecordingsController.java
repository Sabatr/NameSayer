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

    @FXML
    public void initialize() {
        _volumeSlider.valueProperty().bindBidirectional(PracticeController._volume);
    }

    @FXML
    public void deleteUserRecording() {

    }

    @FXML
    public void compare() {
    }

    @FXML
    public void playDatabaseRecording() {
    }

    @FXML
    public void playUserRecording() {

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
        _dbVersionButton.setDisable(true);
        _userVersionButton.setDisable(true);
    }

    /*
     * Set the items of the user recordings dropdown
     */
    private void setupDropdown() {
        List<String> versions = _name.getUserVersions();
        _dropdown.setItems(FXCollections.observableList(versions));
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
    }

    @Override
    public void handle(WorkerStateEvent event) {

    }
}
