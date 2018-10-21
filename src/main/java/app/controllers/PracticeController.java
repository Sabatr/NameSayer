package app.controllers;

import app.backend.BashRunner;
import app.tools.*;
import app.backend.CompositeName;
import app.backend.NameEntry;
import app.views.SceneBuilder;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPopup;
import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXSlider;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

public class PracticeController extends ParentController implements EventHandler<WorkerStateEvent> {

    @FXML private Label _nameDisplayed;
    @FXML private JFXButton _prevButton;
    @FXML private JFXButton _nextButton;
    @FXML private JFXButton _rateButton;
    @FXML private Button _listenButton;
    @FXML private JFXButton _recordButton;
    @FXML private JFXButton _backButton;
    @FXML private Label _namePos;
    @FXML private JFXProgressBar _progressBar;
    @FXML private Label _upArrow;
    @FXML private JFXButton _helpButton;
    @FXML private JFXButton _micButton;
    @FXML private JFXButton _achievements;
    private JFXPopup _ratePopUp;
    private JFXPopup _playPopUp;
    @FXML private JFXSlider _volumeSlider;
    private ObservableList<NameEntry> _practiceList;
    private int _currentPosition;
    private NameEntry _currentName;
    private String _dateAndTime;
    private PlayBackHandler _playBackHandler;

    // globally visible volume for syncing between the Practice controller and the UserRecordings controller
    public static final SimpleDoubleProperty _volume = new SimpleDoubleProperty();
    // This is a late addition and I haven't got the time to set it up properly
    public static NameEntry _selectedName;

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
        setUpRateButton();
        setUpPlayBack();
        _volume.setValue(_volumeSlider.getMax());
        _volumeSlider.valueProperty().bindBidirectional(_volume);
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
                } else {
                    _namePosition = Position.ONLY;
                }
                _dateAndTime = _currentName.getHighestRating();
                updateChangeButtons();
                //Dynamically updates font size
                int fontSize = 100 - (_currentName.getName().length()+10/2)*2;
                if (fontSize > 20) {
                    _nameDisplayed.setStyle("-fx-font-size: "+fontSize+"px");
                } else {
                    _nameDisplayed.setStyle("-fx-font-size: 22px");
                }

            }
        });
    }

    /**
     * Sets up the list view of the versions before hand
     */
    private void setUpPlayBack() {
        _playBackHandler = new PlayBackHandler();
        _playPopUp = _playBackHandler.create();
    }

    /**
     * Upon hover, show the different options the user has.
     */
    @FXML
    private void showReplayPopUp() {
        if (!_playPopUp.isShowing()) {
            _playPopUp.show(_upArrow,JFXPopup.PopupVPosition.TOP, JFXPopup.PopupHPosition.LEFT,0,-120);
        }
    }

    /**
     * Sets up a rating system of values from one to five inclusive.
     */
    private void setUpRateButton() {
        ButtonBar bar = new ButtonBar();
        bar.getStylesheets().add(
                SceneBuilder.class.getResource("styles/Rating.css").toExternalForm()
        );
        bar.getStyleClass().add("buttonBar");
        bar.setPrefSize(250,30);
        for (int i=1;i<=5;i++) {
            bar.getButtons().add(create(i));
        }
        _ratePopUp = new JFXPopup(bar);
    }

    /**
     * Creates the rate buttons.
     * @param value
     * @return
     */
    private JFXButton create(int value) {
        JFXButton button = new JFXButton(value +"");
        button.getStylesheets().add(
                SceneBuilder.class.getResource("styles/Rating.css").toExternalForm()
        );
        button.getStyleClass().add("button");
        button.setOnMouseClicked((e)-> {
            _currentName.rateVersion(_dateAndTime,value);
            _ratePopUp.hide();
            _dateAndTime = _currentName.getHighestRating();
        });
        return button;
    }

    /**
     * Automatically hides the rating bar when other buttons are hovered over.
     */
    @FXML
    private void hideRating() {
        if (_ratePopUp.isShowing()) {
            _ratePopUp.hide();
        }
    }
    /**
     * Displays the button bar when the rate button is hovered over.
     */
    @FXML
    private void showRating() {
        if (!_ratePopUp.isShowing()) {
            _ratePopUp.show(_rateButton,JFXPopup.PopupVPosition.TOP, JFXPopup.PopupHPosition.LEFT,-120,-60);
        }
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
     * This handles the next name click.
     */
    @FXML
    private void nextName() {
        _currentPosition++;
        _currentName =_practiceList.get(_currentPosition);
        _nameDisplayed.setText(_currentName.getName());
        _namePos.setText(_currentPosition+1+"/"+_practiceList.size());
    }

    /**
     * This handles the previous name click.
     */
    @FXML
    private void prevName() {
        _currentPosition--;
        _currentName = _practiceList.get(_currentPosition);
        _nameDisplayed.setText(_currentName.getName());
        _namePos.setText(_currentPosition+1+"/"+_practiceList.size());
    }

    /**
     * Plays the audio of the selected audio file.
     */
    @FXML
    private void playAudio() throws IOException, URISyntaxException {
        if(_currentName instanceof CompositeName) {
            CompositeName cName = (CompositeName) _currentName;
            if(cName.isProcessing()) {
                return;
            } else if(!cName.hasConcat()) {
                cName.concatenateAudio(this::handle);
                return;
            }
        }
        AchievementsManager.getInstance().increaseListenAttempts();
        disableAll();
        _progressBar.setVisible(true);
        Path audioResource = _currentName.getAudioForVersion(_dateAndTime);
        playGenericAudio("PlayAudio", audioResource);
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
        System.out.println("without this, there's duplicate code");
        double min = _volumeSlider.getMin();
        double value = _volumeSlider.getValue();

        double volume = ((value - min) / (max - min)) * 100;
        br.runPlayAudioCommand(audioFilePath, taskTitle, volume);

        Timer timer = new Timer(_progressBar, this, "SomethingElse", timeInSeconds);
        Thread thread1 = new Thread(timer);
        thread1.start();
    }

    /**
     * Allows the user to record audio.
     */
    @FXML
    private void recordAudio() throws URISyntaxException {
        if (MicPaneHandler.getHandler().getSelectedDevice().getValue() == null) {
            new NothingNotification("NoMic");
            return;
        }
        AchievementsManager.getInstance().increasePracticeAttempts();
        _nextButton.setVisible(false);
        _prevButton.setVisible(false);
        disableAll();
        Path pathToUse = _currentName.addUserVersion();
        _currentRecording = pathToUse;
        BashRunner runner = new BashRunner(this);
        runner.runRecordCommand(pathToUse);
        _progressBar.setVisible(true);
        Thread thread = new Thread(new Timer(_progressBar, this, "RecordAudio", 3));
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
                _backButton.setDisable(false);
                _achievements.setDisable(false);
            } else if(event.getSource().getTitle().equals("PlayAudio")) {
                _progressBar.progressProperty().unbind();
                _progressBar.setProgress(0);
                _progressBar.setVisible(false);
                _listenButton.setDisable(false);
                _recordHBox.setDisable(false);
                _confirmationHBox.setDisable(false);
                _backButton.setDisable(false);
                _achievements.setDisable(false);

            } else if(event.getSource().getTitle().equals(BashRunner.CommandType.CONCAT.toString())) {
                try {
                    playAudio();
                } catch (IOException | URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        } else if(event.getEventType().equals(WorkerStateEvent.WORKER_STATE_FAILED)) {
            System.out.println(event.getSource().getValue());
        }
    }

    private void disableAll() {
        _recordHBox.setDisable(true);
        _confirmationHBox.setDisable(true);
        _backButton.setDisable(true);
        _achievements.setDisable(true);

    }

    /**
     * Saves the recording, automatically updates the version.
     */
    @FXML
    private void keepRecording() {
        //Does stuff to save the audio file.
        _currentName.finaliseLastVersion();
        enableButtons();
    }

    /**
     * Plays the audio back for the user.
     */
    private void playBackAudioOfRecording() throws URISyntaxException {
        _progressBar.setVisible(true);
        Path audioResource = _currentRecording;
        playGenericAudio("RecordAudio", audioResource);
    }

    /**
     * Upon selection the state of the play back changes.
     * @throws IOException
     * @throws URISyntaxException
     */
    @FXML
    private void playBack() throws IOException, URISyntaxException{
        disableAll();
        switch (_playBackHandler.getCurrent()) {
            case DATABASE:
                playAudio();
                break;
            case USER:
                playBackAudioOfRecording();
                break;
            case BOTH:
                break;
        }
    }

    /**
     * Cancel the recording. Does not save the recording.
     */
    @FXML
    private void cancelAudioRecording() {
        //Deletes the current recorded audio.
        _currentName.throwAwayNew();
        enableButtons();
    }
    /**
     * Re-enables the disabled buttons, once recording has stopped.
     */
    private void enableButtons() {
        _confirmationHBox.setVisible(false);
        _recordHBox.setVisible(true);
        _recordHBox.setDisable(false);
        _backButton.setDisable(false);
        _achievements.setDisable(false);
        updateChangeButtons();
    }

    /**
     * Handles the button to go to the user recordings
     */
    @FXML
    private void goToUserRecordings() {
        _selectedName = _currentName;
        _switcher.switchScene(SceneBuilder.USER_RECORDINGS);
    }

    /**
     * A button handler which allows the user to go back to the list view.
     */
    @FXML
    private void goBack() {
        _switcher.switchScene(SceneBuilder.LISTVIEW);
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
        _confirmationHBox.setVisible(false);
        _progressBar.setVisible(false);
        _currentPosition = 0;
        _currentName = _practiceList.get(_currentPosition);
        //on loading the text is initially set to whatever is on top of the list.
        _nameDisplayed.setText(_currentName.getName());
        _namePos.setText("1/"+_practiceList.size());
        if (_practiceList.size() > 1) {
            _namePosition = Position.FIRST;
            updateChangeButtons();
        }
        //Gets the best version for the currentName
        _dateAndTime = _currentName.getHighestRating();
    }

    /**
     * Calls the help handler to show the pop up
     */
    @FXML
    private void help() {
        new HelpHandler(_helpButton,"practice");
    }

    /**
     * Calls the mic pane handler to show the pop up
     */
    @FXML
    private void getMic() {
        MicPaneHandler.getHandler().show(_micButton);
    }

    /**
     * Switches to the achievements scene
     */
    @FXML
    private void goToAchievements() {
        AchievementsManager.getInstance().setMenu(SceneBuilder.PRACTICE);
        _switcher.switchScene(SceneBuilder.ACHIEVEMENTS);
    }
}
