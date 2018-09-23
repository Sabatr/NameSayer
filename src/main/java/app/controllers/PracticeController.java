package app.controllers;

import app.backend.BashRunner;
import app.tools.AudioPlayer;
import app.tools.Timer;
import app.backend.NameEntry;
import app.views.SceneBuilder;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class PracticeController extends ParentController implements EventHandler<WorkerStateEvent> {
    private ObservableList<NameEntry> _practiceList;
    @FXML
    private Label _nameDisplayed;
    @FXML
    private Button _prevButton;
    @FXML
    private Button _nextButton;
    @FXML
    private Button _rateButton;
    @FXML
    private Button _listenButton;
    @FXML
    private Button _recordButton;
    @FXML
    private Button _backButton;
    @FXML
    private ComboBox _dropdown;
    @FXML
    private HBox _rateBox;
    @FXML
    private Slider _ratingSlider;


    private int _currentPosition;
    private NameEntry _currentName;

    /**
     * This handles the next name click.
     */
    @FXML
    private ProgressBar _progressBar;

    /**
     * This handles the previous name click.
     */
    @FXML
    private HBox _confirmationHBox;
    @FXML
    private HBox _recordHBox;

    private NameEntry _randomOrSort;

    private Path _currentRecording;

    private enum Position {FIRST,MIDDLE,LAST,ONLY;}
    private Position _namePosition;
    @FXML
    public void initialize() {
        _rateBox.setVisible(false);
        setUpSlider();
        _confirmationHBox.setVisible(false);
        _progressBar.setVisible(false);
        _currentPosition = 0;
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
    private void playAudio() {

        disableAll();

        _progressBar.setVisible(true);
        File audioResource = _currentName.getAudioForVersion((String) _dropdown.getSelectionModel().getSelectedItem()).toFile();
        AudioPlayer player = new AudioPlayer(audioResource, _progressBar, this, "PlayAudio");
        Thread thread = new Thread(player);
        thread.start();
    }

    /**
     * Allows the user to record audio.
     */
    @FXML
    private void recordAudio() {
        _nextButton.setVisible(false);
        _prevButton.setVisible(false);
        disableAll();

        if(System.getProperty("os.name").contains("Windows")) {
            System.out.println("on windows");
            return;
        } else {
            Path pathToUse = _currentName.addVersion();
            _currentRecording = pathToUse;
            BashRunner runner = new BashRunner(this);

            runner.runRecordCommand(pathToUse);
        }

        _progressBar.setVisible(true);
        Thread thread = new Thread(new Timer(_progressBar, this, "RecordAudio"));
        thread.start();
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

        _progressBar.setVisible(true);
        File audioResource = _currentRecording.toFile();
        AudioPlayer player = new AudioPlayer(audioResource, _progressBar, this, "RecordAudio");
        Thread thread = new Thread(player);
        thread.start();
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
     * @throws IOException
     */
    @FXML
    private void goBack() throws IOException {
        SceneBuilder builder = new SceneBuilder(_allNames, _stage);
        _practiceList.add(_randomOrSort);
        builder.getList(_practiceList);
        builder.load("ListView.fxml");
    }

    /**
     * Handles the poor audio button.
     */
    @FXML
    private void rate() {
        //Placeholder for now. Should store the bad audio message in a text file.
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
        //Store this in the .txt file somehow.
        _currentName.rateVersion((String) _dropdown.getSelectionModel().getSelectedItem(), (int) _ratingSlider.getValue());
        _rateBox.setVisible(false);
        _recordHBox.setDisable(false);
        _dropdown.setDisable(false);
        _listenButton.setDisable(false);
        updateChangeButtons();
    }


    /**
     * Uses the parent hook method to get the information from the list view controller.
     * This is done so the practice view knows the list that is selected from the list view.
     * @param items
     */
    @Override
    public void setInformation(ObservableList<NameEntry> allitems, ObservableList<NameEntry> items) {
        super.setInformation(allitems, items);
        _randomOrSort = items.get(items.size() - 1);
        items.remove(items.size() - 1);
        _practiceList = items;
        _currentName = _practiceList.get(_currentPosition);
        //on loading the text is initially set to whatever is on top of the list.
        _nameDisplayed.setText(_currentName.getName());
        updateVersions();
    }
}
