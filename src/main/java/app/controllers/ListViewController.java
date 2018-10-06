package app.controllers;

import app.backend.CompositeName;
import app.backend.NameEntry;
import app.tools.FileFinder;
import app.views.SceneBuilder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;

import java.net.URISyntaxException;
import java.util.*;

/**
 * This class controls the functionality of the list scene.
 */
public class ListViewController extends ParentController {
    @FXML private ListView<NameEntry> _nameListView;
    @FXML private ToggleButton _sortedButton;
    @FXML private ToggleButton _randomButton;
    @FXML private CustomTextField _searchBox;
    private AutoCompletionBinding _searchBoxItems;

    private ObservableList<NameEntry> _selectedNames;
    private List<CompositeName> _addedComposites;

    /**
     * Initially, the sorted button is selected by default.
     * Also, this selects the files needed to be displayed on the list.
     */
    public void initialize() {
        _sortedButton.setDisable(true);
                //.setItems(FXCollections.observableArrayList("weird one", "two yeah"));
        _addedComposites = FXCollections.observableArrayList();

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
        _selectedNames.addAll(_addedComposites);
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
        System.out.println("Binding search box");

        Callback<AutoCompletionBinding.ISuggestionRequest, Collection<NameEntry>> suggester =
                new Callback<AutoCompletionBinding.ISuggestionRequest, Collection<NameEntry>>() {
            @Override
            public Collection<NameEntry> call(AutoCompletionBinding.ISuggestionRequest param) {
                System.out.println("Suggesting names: ");
                ArrayList<NameEntry> nameEntries = new ArrayList<>();
                String fieldText = param.getUserText();
                System.out.println("User text: " + fieldText);
                if(fieldText.length() == 0) {
                    return nameEntries;
                }

                String[] words = fieldText.split("[ -]");
                StringBuilder possibleFullName = new StringBuilder();

                if(words.length >= 2) {
                    String[] wordsLessOne = Arrays.copyOf(words, words.length - 1);
                    List<NameEntry> nameComponents = matchFullName(wordsLessOne);
                    for(NameEntry nameComp: nameComponents) {
                        possibleFullName.append(nameComp.getName() + " ");
                    }
                }

                ArrayList<NameEntry> matchingNames = new ArrayList<>();
                System.out.println("Fullname-1: " + possibleFullName.toString());
                for(NameEntry name: _allNames) {
                    if(name.getName().toLowerCase().startsWith(words[words.length - 1].toLowerCase())) {
                        matchingNames.add(new NameEntry(possibleFullName.toString() + name.getName()));
                        System.out.println("\t\t" + possibleFullName.toString() + name.getName());
                    }
                }
                return matchingNames;
            }
        };

        StringConverter<NameEntry> converter = new StringConverter<NameEntry>() {

            @Override
            public String toString(NameEntry name) {
                return name.toString();
            }

            @Override
            public NameEntry fromString(String string) {
                return new NameEntry(string);
            }
        };

        _searchBoxItems = TextFields.<NameEntry>bindAutoCompletion(_searchBox, suggester, converter);
    }

    /**
     * Matches a full name to its individual components
     * @param words An array of words to match as names in the database
     * @return A List of NameEntrys corresponding to the individual names in the full name
     */
    private List<NameEntry> matchFullName(String... words) {
        boolean aWordDoesntMatch = false;
        List<NameEntry> nameComponents = new ArrayList<>();

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
                return new ArrayList<>();
            }
        }
        return nameComponents;
    }

    /**
     * After the user enters a full name to practice, search for its components and package them
     */
    @FXML
    private void doSearch(ActionEvent event) throws URISyntaxException {
        if(_searchBox.getText() == null) {
            return;
        }

        String[] words = _searchBox.getText().split("[ -]");
        List<NameEntry> nameComponents = matchFullName(words);
        if(nameComponents.isEmpty()) {
            return;
        }

        boolean foundMatchingName = false;
        CompositeName fullName = new CompositeName(nameComponents, CompositeName.fullName(nameComponents));
        for(CompositeName cName: _addedComposites) {
            if(cName.getName().equals(fullName.getName())) {
                foundMatchingName = true;
                break;
            }
        }
        if(!foundMatchingName) {
            _addedComposites.add(fullName);
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
        if(names == null) {
            return;
        }
        selectNames(names);
    }

    /**
     * Given a list of Names, search for them in the database and select them if they exist.
     * @param names A list of dummy NameEntry objects (ones with only the name field set).
     */
    private void selectNames(ObservableList<NameEntry> names) {
        boolean exists = false;
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

        setupSearchBox();
    }

    @Override
    public void switchTo() {
        Collections.sort(_nameListView.getItems());
    }
}
