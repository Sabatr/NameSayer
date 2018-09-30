package app.controllers;

import app.backend.NameEntry;
import app.views.SceneBuilder;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;

import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;

import java.util.Collections;

/**
 * This class controls the functionality of the list scene.
 */
public class ListViewController extends ParentController {
    @FXML private ListView<NameEntry> _nameListView;
    @FXML private ToggleButton _sortedButton;
    @FXML private ToggleButton _randomButton;

    private ObservableList<NameEntry> _selectedNames;

    /**
     * Initially, the sorted button is selected by default.
     * Also, this selects the files needed to be displayed on the list.
     */
    public void initialize() {
        _sortedButton.setDisable(true);

            //CTRL+Click to select multiple
        _nameListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    /**
     * Selects the list through an view list.
     */
    @FXML
    private void onClick() {
        _selectedNames.setAll(_nameListView.getSelectionModel().getSelectedItems());
    }

    /**
     * Removes all the selected values (including the current one)
     */
    @FXML
    private void clearSelection() {
        _selectedNames = FXCollections.observableArrayList();
        _nameListView.getSelectionModel().clearSelection();
    }

    /**
     * When the user decides to sort, the sort button becomes disabled.
     */
    @FXML
    private void onSort() {
        _sortedButton.setDisable(true);
        _randomButton.setDisable(false);
    }

    /**
     * When the user decides to randomize, the random button becomes disabled.
     */
    @FXML
    private void onRandom() {
        _randomButton.setDisable(true);
        _sortedButton.setDisable(false);
    }

    /**
     * A listener for the practice button.
     */
    @FXML
    private void practiceButton() {
        if (_selectedNames.size() == 0) {
            alertNothingSelected();
        } else {
            if (_sortedButton.isDisabled()) {
                Collections.sort(_selectedNames);
            } else {
                Collections.shuffle(_selectedNames);
            }
            _switcher.switchScene(SceneBuilder.PRACTICE);
        }
    }

    /**
     * When nothing is selected, the user is warned through an alert.
     */
    private void alertNothingSelected() {
        Alert alert = new Alert(AlertType.ERROR);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
                SceneBuilder.class.getResource("styles/NoneSelected.css").toExternalForm());
        dialogPane.getStyleClass().add("noSelectDialogue");
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText("Error: No names selected.");
        alert.showAndWait();
    }

    /**
     * A listener for the back button.
     */
    @FXML
    private void goBack() {
        _switcher.switchScene(SceneBuilder.MENU);
    }

    /**
     * When the information is passed back to the controller,
     * we keep the previous state of the before the switch.
     * @param allNames The complete list of NameEntries
     * @param selectedNames, the selected values + the state of the sorting at the end.
     */
    @Override
    public void setInformation(SceneBuilder switcher, ObservableList<NameEntry> allNames, ObservableList<NameEntry> selectedNames) {
        super.setInformation(switcher, allNames, selectedNames);
        _nameListView.setItems(allNames);
        _selectedNames = selectedNames;
    }

    @Override
    public void switchTo() {
        Collections.sort(_nameListView.getItems());
    }
}
