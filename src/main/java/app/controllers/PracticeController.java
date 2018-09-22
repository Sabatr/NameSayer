package app.controllers;

import app.tools.ProgressTracker;
import app.views.SceneBuilder;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.io.IOException;

public class PracticeController extends ParentController {
    private ObservableList<String> _practiceList;
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
    private ProgressBar _progressBar;
    @FXML
    private HBox _confirmationHBox;
    @FXML
    private HBox _recordHBox;

    private int _currentPosition;
    private String _currentName;
    private String _randomOrSort;
    private enum Position {FIRST,MIDDLE,LAST,ONLY}
    private Position _namePosition;

    @FXML
    public void initialize() {
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
                    if (newValue.equals(_practiceList.get(0))) {
                        _namePosition = Position.FIRST;
                    } else if (newValue.equals(_practiceList.get(_practiceList.size()-1))){
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

    private void updateVersions() {
        _currentName =_practiceList.get(_currentPosition);
        //Gets the versions of the current name
        _dropdown.setItems(FXCollections.observableArrayList(_currentName,"One","Two","Three")); //Placeholder.
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
        _nameDisplayed.setText(_currentName);
    }

    /**
     * This handles the previous name click.
     */
    @FXML
    private void prevName() {
        _currentPosition--;
        _currentName = _practiceList.get(_currentPosition);
        _nameDisplayed.setText(_currentName);
    }

    /**
     * Plays the audio of the selected audio file.
     */
    @FXML
    private void playAudio() {
        System.out.println(_dropdown.getSelectionModel().getSelectedItem());
    }

    /**
     * Allows the user to record audio.
     */
    @FXML
    private void recordAudio() {
        _progressBar.setVisible(true);
        _nextButton.setVisible(false);
        _prevButton.setVisible(false);
        _listenButton.setDisable(true);
        _dropdown.setDisable(true);
        _backButton.setDisable(true);
        _recordButton.setDisable(true);
        _rateButton.setDisable(true);
        Thread thread = new Thread(new ProgressTracker(_progressBar,_recordHBox,_confirmationHBox,_dropdown,_listenButton));
        thread.start();
    }

    /**
     * Saves the recording, automatically updates the version.
     */
    @FXML
    private void keepRecording() {
        //Does stuff to save the audio file.
        enableButtons();
    }

    /**
     * Plays the audio back for the user.
     */
    @FXML
    private void playBackSelfAudio() {
        //Plays back audio
        //Disables buttons while this happens.
        //Renables after audio is played.
    }

    /**
     * Cancel the recording. Does not save the recording.
     */
    @FXML
    private void cancelAudioRecording() {
        //Deletes the current recorded audio.
        //Switches back to the recording hbox.
        enableButtons();
    }

    private void enableButtons() {
        _confirmationHBox.setVisible(false);
        _recordHBox.setVisible(true);
        _rateButton.setDisable(false);
        _recordButton.setDisable(false);
        _backButton.setDisable(false);
        updateChangeButtons();
    }


    /**
     * A button handler which allows the user to go back to the list view.
     * @throws IOException
     */
    @FXML
    private void goBack() throws IOException {
        SceneBuilder builder = new SceneBuilder(_stage);
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
        System.out.println("Goes here");
    }


    /**
     * Uses the parent hook method to get the information from the list view controller.
     * This is done so the practice view knows the list that is selected from the list view.
     * @param items
     */
    @Override
    public void setInformation(ObservableList<String> items) {
        _randomOrSort = items.get(items.size()-1);
        items.remove(items.size()-1);
        _practiceList = items;
        _currentName = _practiceList.get(_currentPosition);
        //on loading the text is initially set to whatever is on top of the list.
        _nameDisplayed.setText(_currentName);
        updateVersions();
    }

}
