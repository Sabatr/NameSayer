package app.controllers;

import app.tools.ProgressTracker;
import app.views.SceneBuilder;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.util.Optional;

public class PracticeController extends ParentController {
    private ObservableList<String> _practiceList;
    @FXML
    private Label _nameDisplayed;
    @FXML
    private Button _prevButton;
    @FXML
    private Button _nextButton;
    @FXML
    private ComboBox _dropdown;
    @FXML
    private Button _rateButton;
    @FXML
    private ProgressBar _progressBar;

    private int _currentPosition;
    private String _currentName;
    private String _randomOrSort;


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

    @FXML
    private void playAudio() {
        System.out.println(_dropdown.getSelectionModel().getSelectedItem());
    }

    @FXML
    private void recordAudio() {
        _progressBar.setVisible(true);
        Thread thread = new Thread(new ProgressTracker(_progressBar));
        thread.start();
    }

    @FXML
    public void initialize() {
        _progressBar.setVisible(false);
        _dropdown.setItems(FXCollections.observableArrayList("One","Two","Three"));
        //Automatically select the default value.
        _dropdown.getSelectionModel().selectFirst();
        _currentPosition = 0;
        //This listener is used to check whether the list is at the end. Buttons are disabled accordingly.
        _nameDisplayed.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                //If the practice list only contains one value, then both previous and next buttons are disabled.
                if (_practiceList.size() > 1) {
                    //Checks if the value is the first on the list.
                    if (newValue.equals(_practiceList.get(0))) {
                        _prevButton.setVisible(false);
                        _nextButton.setVisible(true);
                    } else if (newValue.equals(_practiceList.get(_practiceList.size()-1))){
                        _nextButton.setVisible(false);
                        _prevButton.setVisible(true);
                    } else {
                        //Renables button when the position is somewhere in the middle.
                        _prevButton.setVisible(true);
                        _nextButton.setVisible(true);
                    }
                } else {
                    _prevButton.setVisible(false);
                    _nextButton.setVisible(false);
                }
            }
        });
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
    }

    @FXML
    private void rate() {
//        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
//        ButtonType goodButton = new ButtonType("Good");
//        ButtonType badButton = new ButtonType("Bad");
//        ButtonType cancelButton = new ButtonType("Cancel");
//        alert.getButtonTypes().setAll(goodButton,badButton,cancelButton);
//        Optional<ButtonType> result = alert.showAndWait();
//        if (result.get() == goodButton){
//            //Do some bash stuff
//        } else if (result.get() == badButton) {
//            //Do some bash stuff
//        } else {
//            //Do nothing
//        }
        System.out.println("Goes here");
    }
}
