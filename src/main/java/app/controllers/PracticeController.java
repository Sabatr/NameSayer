package app.controllers;

import app.backend.BashRunner;
import app.backend.CompositeName;
import app.tools.AudioPlayer;
import app.tools.Timer;
import app.backend.NameEntry;
import app.views.SceneBuilder;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.media.AudioClip;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class PracticeController extends ParentController implements EventHandler<WorkerStateEvent> {

    @FXML private Label _nameDisplayed;
    @FXML private Button _prevButton;
    @FXML private Button _nextButton;
    @FXML private Button _rateButton;
    @FXML private Button _listenButton;
    @FXML private Button _recordButton;
    @FXML private Button _backButton;
    @FXML private ComboBox _dropdown;
    @FXML private HBox _rateBox;
    @FXML private Slider _ratingSlider;
    @FXML private Label _warningLabel;

    private ObservableList<NameEntry> _practiceList;
    private int _currentPosition;
    private NameEntry _currentName;

    /**
     * This handles the next name click.
     */
    @FXML private ProgressBar _progressBar;

    /**
     * This handles the previous name click.
     */
    @FXML private HBox _confirmationHBox;
    @FXML private HBox _recordHBox;

    private Path _currentRecording;

    private enum Position {FIRST,MIDDLE,LAST,ONLY;}
    private Position _namePosition;
    @FXML
    public void initialize() {
        setUpSlider();
        //This listener is used to check whether the list is at the end. Buttons are disabled accordingly.
        _nameDisplayed.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                //If the practice list only contains one value, then both previous and next buttons are disabled.
                if (_practiceList.size() > 1) {
                    //Checks if the value is the first on the list.
                    if (newValue.equals(_practiceList.get(0).getName())) {
                        _namePosition = Position.FIRST;
                    } else if (newValue.equals(_practiceList.get(_practiceList.size()-1).getName())){
                        _namePosition = Position.LAST;
                    } else {
                        //Renables button when the position is somewhere in the middle.
                        _namePosition = Position.MIDDLE;
                    }
                    updateVersions();
                } else {
                    _namePosition = Position.ONLY;
                }
                updateChangeButtons();
            }
        });
    }

    /**
     * These are the arrow buttons, which changes the names.
     * When at the beginning of the name, there shouldn't be an option
     * to go back. Likewise, for at the end. If the selected name is
     * only for one name, then it shouldn't have any arrows.
     */
    private void updateChangeButtons() {
        switch (_namePosition) {
            case FIRST:
                _prevButton.setVisible(false);
                _nextButton.setVisible(true);
                break;
            case MIDDLE:
                _prevButton.setVisible(true);
                _nextButton.setVisible(true);
                break;
            case LAST:
                _prevButton.setVisible(true);
                _nextButton.setVisible(false);
                break;
            case ONLY:
                _prevButton.setVisible(false);
                _nextButton.setVisible(false);
                break;
        }
    }

    /**
     * This updates the version dropdown lists.
     */
    private void updateVersions() {
        _currentName =_practiceList.get(_currentPosition);
        //Gets the versions of the current name
        _dropdown.setItems(FXCollections.observableArrayList(_currentName.getVersions()));
        //Automatically select the default value.
        _dropdown.getSelectionModel().selectFirst();
    }

    /**
     * This handles the next name click.
     */
    @FXML
    private void nextName() {
        _currentPosition++;
        _currentName =_practiceList.get(_currentPosition);
        _nameDisplayed.setText(_currentName.getName());
    }

    /**
     * This handles the previous name click.
     */
    @FXML
    private void prevName() {
        _currentPosition--;
        _currentName = _practiceList.get(_currentPosition);
        _nameDisplayed.setText(_currentName.getName());
    }

    /**
     * Plays the audio of the selected audio file.
     */
    @FXML
    private void playAudio() throws IOException {

        if(_currentName instanceof CompositeName) {
            CompositeName cName = (CompositeName) _currentName;
            System.out.println("testing existence of " + _currentName.getName());
            if(!cName.hasConcat()) {
                cName.concateanteAudio(this::handle);
            }
            return;
        }

        disableAll();

        _progressBar.setVisible(true);
        File audioResource = _currentName.getAudioForVersion((String) _dropdown.getSelectionModel().getSelectedItem()).toFile();
        AudioPlayer player = new AudioPlayer(audioResource, this, "PlayAudio");
        float timeInSeconds = player.getLength();
        Thread thread = new Thread(player);
        thread.start();

        Timer timer = new Timer(_progressBar, this, "PlayAud", timeInSeconds);
        Thread thread1 = new Thread(timer);
        thread1.start();
    }

    /**
     * Allows the user to record audio.
     */
    @FXML
    private void recordAudio() {
        if(System.getProperty("os.name").contains("Windows")) {
            System.out.println("on windows");
        } else {
            _nextButton.setVisible(false);
            _prevButton.setVisible(false);
            disableAll();

            Path pathToUse = _currentName.addVersion();
            _currentRecording = pathToUse;
            BashRunner runner = new BashRunner(this);
            runner.runRecordCommand(pathToUse);

            _progressBar.setVisible(true);
            Thread thread = new Thread(new Timer(_progressBar, this, "RecordAudio", 3));
            thread.start();
        }
    }

    /**
     * Handle a change in the state of a Task that has been set to notify this Controller
     */
    @Override
    public void handle(WorkerStateEvent event) {
        if(event.getEventType().equals(WorkerStateEvent.WORKER_STATE_SUCCEEDED))
        {
            if(event.getSource().getTitle().equals("RecordAudio")) {
                _progressBar.progressProperty().unbind();
                _progressBar.setProgress(0);
                _progressBar.setVisible(false);
                _confirmationHBox.setVisible(true);
                _confirmationHBox.setDisable(false);
                _recordHBox.setVisible(false);
                _recordHBox.setDisable(true);
                _listenButton.setDisable(false);
                _dropdown.setDisable(false);
            } else if(event.getSource().getTitle().equals("PlayAudio")) {
                _progressBar.progressProperty().unbind();
                _progressBar.setProgress(0);
                _progressBar.setVisible(false);
                _listenButton.setDisable(false);
                _recordHBox.setDisable(false);
                _confirmationHBox.setDisable(false);
                _dropdown.setDisable(false);
            } else if(event.getSource().getTitle().equals(BashRunner.CommandType.PLAYAUDIO.toString())) {
                System.out.println(event.getSource().getValue());
            } else if(event.getSource().getTitle().equals(BashRunner.CommandType.CONCAT.toString())) {
                try {
                    playAudio();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if(event.getEventType().equals(WorkerStateEvent.WORKER_STATE_FAILED)) {
            System.out.println(event.getSource().getValue());
        }
    }

    private void disableAll() {
        _listenButton.setDisable(true);
        _dropdown.setDisable(true);
        _recordHBox.setDisable(true);
        _confirmationHBox.setDisable(true);
    }

    /**
     * Saves the recording, automatically updates the version.
     */
    @FXML
    private void keepRecording() {
        //Does stuff to save the audio file.
        _currentName.finaliseLastVersion();
        _dropdown.setItems(FXCollections.observableArrayList(_currentName.getVersions()));
        enableButtons();
    }

    /**
     * Plays the audio back for the user.
     */
    @FXML
    private void playBackAudioOfRecording() {
        //Plays back audio
        //Disables buttons while this happens.
        //Renables after audio is played.

        disableAll();
        _progressBar.setVisible(true);
        File audioResource = _currentRecording.toFile();
        AudioPlayer player = new AudioPlayer(audioResource, this, "RAudio");
        Thread thread = new Thread(player);
        thread.start();

        Timer timer = new Timer(_progressBar, this, "RecordAudio", 5);
        Thread thread1 = new Thread(timer);
        thread1.start();
    }

    /**
     * Cancel the recording. Does not save the recording.
     */
    @FXML
    private void cancelAudioRecording() {
        //Deletes the current recorded audio.
        //Switches back to the recording hbox.
        _currentName.throwAwayNew();
        enableButtons();
    }

    private void enableButtons() {
        _confirmationHBox.setVisible(false);
        _recordHBox.setVisible(true);
        _recordHBox.setDisable(false);
        updateChangeButtons();
    }

    /**
     * A button handler which allows the user to go back to the list view.
     */
    @FXML
    private void goBack() {
        _switcher.switchScene(SceneBuilder.LISTVIEW);
    }

    /**
     * Handles the poor audio button.
     */
    @FXML
    private void rate() {
        _rateBox.setVisible(true);
        _nextButton.setVisible(false);
        _prevButton.setVisible(false);
        disableAll();
    }
    private void setUpSlider() {
        _ratingSlider.setMin(0);
        _ratingSlider.setMax(10);
        _ratingSlider.setBlockIncrement(1);
        _ratingSlider.setShowTickLabels(true);
        _ratingSlider.setShowTickMarks(true);
        _ratingSlider.setMajorTickUnit(5);
        _ratingSlider.setMinorTickCount(4);
        _ratingSlider.setSnapToTicks(true);
    }
    @FXML
    private void getRating() {
        _currentName.rateVersion((String) _dropdown.getSelectionModel().getSelectedItem(), (int) _ratingSlider.getValue());
        dropMenuAction();
        _rateBox.setVisible(false);
        _recordHBox.setDisable(false);
        _dropdown.setDisable(false);
        _listenButton.setDisable(false);
        updateChangeButtons();
    }

    @FXML
    private void dropMenuAction() {
        String date = (String) _dropdown.getSelectionModel().getSelectedItem();
        int rating = _currentName.getRating(date);
        if (0 <= rating && rating < 5) {
            _warningLabel.setVisible(true);
        } else {
            _warningLabel.setVisible(false);
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
        _practiceList = selectedNames;
    }

    /**
     * Upon switching to this scene, reset the components
     */
    @Override
    public void switchTo() {
        _rateBox.setVisible(false);
        _warningLabel.setVisible(false);
        _confirmationHBox.setVisible(false);
        _progressBar.setVisible(false);
        _currentPosition = 0;
        _currentName = _practiceList.get(_currentPosition);
        //on loading the text is initially set to whatever is on top of the list.
        _nameDisplayed.setText(_currentName.getName());
        updateVersions();
        dropMenuAction();
    }
}
