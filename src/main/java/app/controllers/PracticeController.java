package app.controllers;

import app.views.SceneBuilder;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

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
    private ComboBox _dropdown;

    private int _currentPosition;
    private String _currentName;


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
        _nameDisplayed.setText(_practiceList.get(_currentPosition));
    }

    @FXML
    private void playAudio() {
        System.out.println(_dropdown.getSelectionModel().getSelectedItem());
    }

    @FXML
    public void initialize() {
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
                    } else if (newValue.equals(_practiceList.get(_practiceList.size()-1))){
                        _nextButton.setVisible(false);
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
        _practiceList = items;
        _currentName = _practiceList.get(_currentPosition);
        //on loading the text is initially set to whatever is on top of the list.
        _nameDisplayed.setText(_currentName);
    }
}
