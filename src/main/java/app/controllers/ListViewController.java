package app.controllers;

import app.backend.CompositeName;
import app.backend.NameEntry;
import app.tools.FileFinder;
import app.views.SceneBuilder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;

import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
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
    @FXML private ListView<NameEntry> _selectListView;

    private ObservableList<NameEntry> _selectedNames;
    private ObservableList<CompositeName> _addedComposites;

    /**
     * Initially, the sorted button is selected by default.
     * Also, this selects the files needed to be displayed on the list.
     */
    public void initialize() {
        _selectedNames = FXCollections.observableArrayList();
        _sortedButton.setDisable(true);
        _searchBox.setItems(FXCollections.observableArrayList("weird one", "two yeah"));
        setupSearchBox();
        _addedComposites = FXCollections.observableArrayList();

            //CTRL+Click to select multiple
        _nameListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    /**
     * Selects the list through an view list.
     */
    @FXML
    private void onClick() {
        boolean exists = false;
        ObservableList<NameEntry> tempSelectList = FXCollections.observableArrayList();
        for (NameEntry selectedNames: _nameListView.getSelectionModel().getSelectedItems()) {
            for (NameEntry entry : _selectedNames) {
                if (entry.compareTo(selectedNames) == 0) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                tempSelectList.add(selectedNames);
            }
            exists = false;
        }
     //   updateSelectedList(_nameListView.getSelectionModel().getSelectedItems());
        updateSelectedList(tempSelectList);
    }

    /**
     * Removes all the selected values (including the current one)
     */
    @FXML
    private void clearSelection() {
        _addedComposites.clear();
        _searchBox.setItems(FXCollections.observableArrayList());
        _selectedNames.clear();
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
     * Updates the selectedListView upon changes.
     * @param toBeAdded
     */
    private void updateSelectedList(ObservableList toBeAdded) {
        _selectedNames.addAll(toBeAdded);
        Collections.sort(_selectedNames);
        _selectListView.setItems(_selectedNames);
    }

    /**
     * Deselects names from the practiceList by clicking on them.
     */
    @FXML
    private void clearSelected() {
        if (_selectListView.getSelectionModel().getSelectedItem() != null) {
            for (NameEntry entry : _selectedNames) {
                if (entry.compareTo(_selectListView.getSelectionModel().getSelectedItem()) == 0) {
                    _selectedNames.remove(entry);
                    _selectListView.setItems(_selectedNames);
                    _nameListView.getSelectionModel().clearSelection();
                    return;
                }
            }
        }
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
     * Set up the search box in such a way that there are some recommended options.
     */
    private void setupSearchBox() {
        _searchBox.getEditor().setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                    String comboText = _searchBox.getEditor().getText();
                    //if the user deletes all the stuff, clear everything.
                    if (comboText.length() == 0) {
                        _searchBox.setItems(FXCollections.observableArrayList(new ArrayList<>(0)));
                        _searchBox.getSelectionModel().clearSelection();
                        if (_searchBox.isShowing()) {
                            _searchBox.hide();
                        }
                        return;
                    }
                    String[] words = comboText.split("[ -]");
                    StringBuilder possibleFullName = new StringBuilder();

                    if (words.length >= 2) {
                        String[] wordsLessOne = Arrays.copyOf(words, words.length - 1);
                        List<NameEntry> nameComponents = matchFullName(wordsLessOne);
                        for (NameEntry nameComp : nameComponents) {
                            possibleFullName.append(nameComp.getName() + " ");
                        }
                    }

                    ArrayList<String> matchingNames = new ArrayList<>();
                    for (NameEntry name : _allNames) {
                        if (name.getName().toLowerCase().startsWith(words[words.length - 1].toLowerCase())) {
                            matchingNames.add(possibleFullName.toString() + name.getName());
                        }
                    }
                    if (matchingNames.size() == 0) {
                        _searchBox.setItems(FXCollections.observableArrayList("Could not find a matching name."));
                    } else {
                        _searchBox.setItems(FXCollections.observableArrayList(matchingNames));
                    }

                    if (!_searchBox.isShowing()) {
                        _searchBox.show();
                    }

            }
        });
    }

    /**
     * Matches a full name to its individual components
     * @param words An array of words to match as names in the database
     * @return A List of NameEntrys corresponding to the individual names in the full name
     */
    public ObservableList<NameEntry> matchFullName(String... words) {
        boolean aWordDoesntMatch = false;
        ObservableList<NameEntry> nameComponents = FXCollections.observableArrayList();

        int i = words[0].isEmpty() ? 1 : 0;
        for(; i < words.length; i++) {
            aWordDoesntMatch = true;
            for(NameEntry name: _allNames) {
                if(name.getName().equalsIgnoreCase(words[i])) {
                    aWordDoesntMatch = false;
                    nameComponents.add(name);
                    break;
                }
            }
            if(aWordDoesntMatch) {
                return FXCollections.observableArrayList();
            }
        }
        return nameComponents;
    }

    /**
     * After the user enters a full name to practice, search for its components and package them
     * Press enter to submit the full name.
     */
    @FXML
    private void doSearch(KeyEvent event) throws URISyntaxException {
        boolean doesNotExist = true;
        if (event.getCode() == KeyCode.ENTER) {
            if(_searchBox.getValue() == null) {
                return;
            }
            //Checks if the typed name already exists in the selected list.
            for (NameEntry entry: _selectedNames) {
                if (entry.compareTo(new NameEntry(_searchBox.getValue())) == 0) {
                    doesNotExist = false;
                    break;
                }
            }
            if (doesNotExist) {
                String[] words = _searchBox.getValue().split("[ -]");

                ObservableList<NameEntry> nameComponents = matchFullName(words);
                if(nameComponents.isEmpty()) {
                    return;
                }
                CompositeName fullName = new CompositeName(nameComponents, CompositeName.fullName(nameComponents));
                _addedComposites.add(fullName);
                updateSelectedList(_addedComposites);
                _addedComposites.clear();
            }
            _searchBox.getItems().clear();
        }
    }

    /**
     * Allows the user to import a .txt file containing names they want to practice.
     */
    @FXML
    private void importText() throws URISyntaxException {
        FileFinder finder = new FileFinder("practice");
        finder.choose(_switcher.getStage());
        ObservableList<NameEntry> names = finder.getContent();
        if (!names.isEmpty()) {
            selectNames(names);
        }
        if(names == null) {
            return;
        }
       // selectNames(names);
    }

    /**
     * Given a list of Names, search for them in the database and select them if they exist.
     * @param names A list of dummy NameEntry objects (ones with only the name field set).
     * Sorry for ugly code :p
     */
    private void selectNames(ObservableList<NameEntry> names) {
        Boolean exists = false;
        int numberOfPartsInNames = 0;
        for (NameEntry entry : names) {
            ObservableList<NameEntry> foundItems = FXCollections.observableArrayList();
            String[] splitNames = entry.toString().split("[ -]");
            //Checks if the name is in the database.
            for (String part: splitNames) {
                NameEntry temp = new NameEntry(part);
                for (NameEntry name : _allNames) {
                    if (temp.compareTo(name) == 0) {
                        foundItems.add(name);
                        numberOfPartsInNames++;
                        break;
                    }
                }
            }
            if (numberOfPartsInNames == splitNames.length) {
                exists = true;
            }
            if (exists) {
                //Does not need to check if the name is selected if nothing is in the list
                if (_selectedNames.size() == 0) {
                    importHelper(splitNames,foundItems);
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
                        importHelper(splitNames,foundItems);
                    }
                }
            }
            //Reset for each name
            numberOfPartsInNames = 0;
            exists = false;

        }
    }

    /**
     * This allows both single names and two or more names to be added.
     * TODO: Show a message to notify the user that a certain name doesn't exist.
     * @param splitNames
     * @param foundItems
     */
    private void importHelper(String[] splitNames,ObservableList foundItems) {
        if (splitNames.length > 1 ) {
            try {
                CompositeName fullName = new CompositeName(foundItems, CompositeName.fullName(foundItems));
                _addedComposites.add(fullName);
                updateSelectedList(_addedComposites);
                _addedComposites.clear();
            } catch (URISyntaxException exc){
            }
        } else {
            _selectedNames.addAll(foundItems);
            _selectListView.setItems(_selectedNames);
        }
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
