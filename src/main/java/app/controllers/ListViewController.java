package app.controllers;

import app.backend.NameEntry;
import app.tools.FileFinder;
import app.views.SceneBuilder;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;

import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class controls the functionality of the list scene.
 */
public class ListViewController extends ParentController {
    @FXML private ListView<NameEntry> _nameListView;
    @FXML private ToggleButton _sortedButton;
    @FXML private ToggleButton _randomButton;
    @FXML private ComboBox<String> _searchBox;

    private ObservableList<NameEntry> _selectedNames;

    /**
     * Initially, the sorted button is selected by default.
     * Also, this selects the files needed to be displayed on the list.
     */
    public void initialize() {
        _sortedButton.setDisable(true);
        _searchBox.setItems(FXCollections.observableArrayList("weird one", "two yeah"));
        setupSearchBox();

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

    private void setupSearchBox() {
        _searchBox.getEditor().textProperty().addListener(
                new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                        String[] comboText = newValue.split("[ -]+");
                        ArrayList<String> matchingNames = new ArrayList<>();
                        for(String word: comboText) {
                            for (NameEntry name: _allNames) {
                                if (name.getName().startsWith(word)) {
                                    matchingNames.add(name.getName());
                                }
                            }
                        }
                        _searchBox.setItems(FXCollections.observableArrayList(matchingNames));
                        int rowsToDisplay = matchingNames.size() > 10 ? 10 : matchingNames.size();
                        _searchBox.setVisibleRowCount(rowsToDisplay);
                        if(!_searchBox.isShowing()) {
                            _searchBox.show();
                        }
                    }
        });
    }

    /**
     * When the user types in the combobox at the top of the screen, this method filters out the names and
     * shows matching suggestions
     */
    @FXML
    private void onTypeInCombo() {

    }

    /**
     * Allows the user to import a .txt file containing names they want to practice.
     */
    @FXML
    private void importText() {
        FileFinder finder = new FileFinder("practice");
        finder.choose(_switcher.getStage());
        ObservableList<NameEntry> names = finder.getContent();
        selectNames(names);
    }

    /**
     * Given a list of Names, search for them in the database and select them if they exist.
     * @param names A list of dummy NameEntry objects (ones with only the name field set).
     */
    private void selectNames(ObservableList<NameEntry> names) {
        Boolean exists = false;
        int position = 0;
        for (NameEntry entry : names) {
            //Checks if the name is in the database.
            for (NameEntry name : _allNames) {
                if (entry.compareTo(name) == 0) {
                    exists = true;
                    break;
                }
                position++;
            }
            if (exists) {
                //Does not need to check if the name is selected if nothing is in the list
                if (_selectedNames.size() == 0) {
                    _nameListView.getSelectionModel().select(position);
                    _selectedNames.add(_nameListView.getSelectionModel().getSelectedItem());
//                    _nameListView.getSelectionModel().select(entry);
                } else {
                    boolean notInSelected = true;
                    //Do not want to repeat selected names.
                    for (NameEntry selectedName : _selectedNames) {
                        if (entry.compareTo(selectedName) == 0) {
                            notInSelected = false;
                            break;
                        }
                    }
                    if (notInSelected) {
                        _nameListView.getSelectionModel().select(position);
                        _selectedNames.add(_nameListView.getSelectionModel().getSelectedItem());
                    }
                }
            }
            //Reset for each name
            position  =0;
            exists = false;
        }
        _nameListView.getSelectionModel().getSelectedItems().setAll(_selectedNames);
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
