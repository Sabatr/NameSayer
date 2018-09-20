package app.controllers;

import app.views.SceneBuilder;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
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
    public void initialize() {
        _currentPosition = 0;
        //This listener is used to check whether the list is at the end. Buttons are disabled accordingly.
        _nameDisplayed.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                //If the practice list only contains one value, then both previous and next buttons are disabled.
                if (_practiceList.size() > 1) {
                    //Checks if the value is the first on the list.
                    if (newValue.equals(_practiceList.get(0))) {
                        _prevButton.setDisable(true);
                    } else if (newValue.equals(_practiceList.get(_practiceList.size()-1))){
                        _nextButton.setDisable(true);
                    } else {
                        //Renables button when the position is somewhere in the middle.
                        _prevButton.setDisable(false);
                        _nextButton.setDisable(false);
                    }
                } else {
                    _prevButton.setDisable(true);
                    _nextButton.setDisable(true);
                }
              //  System.out.println(_currentName);
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
