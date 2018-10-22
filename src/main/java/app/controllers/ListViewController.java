package app.controllers;

import app.backend.CompositeName;
import app.backend.NameEntry;
import app.tools.*;
import app.views.SceneBuilder;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
    @FXML private JFXListView<NameEntry> _nameListView;
    @FXML private ToggleButton _sortedButton;
    @FXML private ToggleButton _randomButton;
    @FXML private ListView<NameEntry> _selectListView;
    @FXML private ImageView _randomImage;
    @FXML private JFXButton _helpButton;
    @FXML private JFXButton _micButton;
    @FXML private CustomTextField _searchBox;
    private AutoCompletionBinding _searchBoxItems;

    private ObservableList<NameEntry> _selectedNames;
    private ObservableList<CompositeName> _addedComposites;
    private ObservableList<CompositeName> _allComposites;

    /**
     * Initially, the sorted button is selected by default.
     * Also, this selects the files needed to be displayed on the list.
     */
    public void initialize() {
        _randomImage.setImage(new Image(SceneBuilder.class.getResource("images/shuffle.png").toExternalForm()));
        _selectedNames = FXCollections.observableArrayList();
        _sortedButton.setDisable(true);
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
        updateSelectedList(tempSelectList);
    }

    /**
     * Removes all the selected values (including the current one)
     */
    @FXML
    private void clearSelection() {
        _addedComposites.clear();
        _searchBox.clear();
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
            AchievementsManager.getInstance().getPracticeNames(_selectedNames);
            if (_sortedButton.isDisabled()) {
                Collections.sort(_selectedNames);
            } else {
                AchievementsManager.getInstance().hasBeenRandomised(true);
                Collections.shuffle(_selectedNames);
            }
            _switcher.switchScene(SceneBuilder.PRACTICE);
        }
    }

    /**
     * When nothing is selected, the user is warned through an alert.
     */
    private void alertNothingSelected() {
        new NothingNotification("NoNames");
    }

    /**
     * Set up the search box in such a way that there are some recommended options.
     */
    private void setupSearchBox() {
        Callback<AutoCompletionBinding.ISuggestionRequest, Collection<NameEntry>> suggester =
                new Callback<AutoCompletionBinding.ISuggestionRequest, Collection<NameEntry>>() {
            @Override
            public Collection<NameEntry> call(AutoCompletionBinding.ISuggestionRequest param) {
                ArrayList<NameEntry> nameEntries = new ArrayList<>();
                String fieldText = param.getUserText();
                if(fieldText.length() == 0) {
                    return nameEntries;
                }

                String[] words = fieldText.split("[ -]");
                String[] separators = fieldText.split("([^ -])\\w+|([^ -])");
                int i = 1;
                StringBuilder possibleFullName = new StringBuilder();
                if (words.length >= 2) {
                    String[] wordsLessOne = Arrays.copyOf(words, words.length - 1);
                    List<NameEntry> nameComponents = matchFullName(wordsLessOne);
                    if(nameComponents.size() > 0 && nameComponents.get(0).compareTo(new NameEntry("##FAILED##")) == 0) {
                        nameComponents = FXCollections.observableArrayList();
                    }
                    for (NameEntry nameComp : nameComponents) {
                        possibleFullName.append(nameComp.getName() + separators[i++]);
                    }
                }

                ArrayList<NameEntry> matchingNames = new ArrayList<>();
                for(NameEntry name: _allNames) {
                    if(name.getName().toLowerCase().startsWith(words[words.length - 1].toLowerCase())) {
                        matchingNames.add(new NameEntry(possibleFullName.toString() + name.getName()));
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
    public ObservableList<NameEntry> matchFullName(String... words) {
        boolean aWordDoesntMatch = false;
        ObservableList<NameEntry> nameComponents = FXCollections.observableArrayList();

        int i = words[0].isEmpty() ? 1 : 0;
        for(; i < words.length; i++) {
            boolean thisWordDoesntMatch = true;
            for(NameEntry name: _allNames) {
                if(name.getName().equalsIgnoreCase(words[i])) {
                    if(!aWordDoesntMatch) {
                        nameComponents.add(name);
                    }
                    thisWordDoesntMatch = false;
                    break;
                }
            }

            // if this word doesn't match a name and we've seen a non-matching name before
            if(thisWordDoesntMatch && aWordDoesntMatch) {
                nameComponents.add(new NameEntry(words[i]));

            // if this word doesn't match and we haven't
            } else if(thisWordDoesntMatch && !aWordDoesntMatch) {
                aWordDoesntMatch = true;
                nameComponents = FXCollections.observableArrayList(new NameEntry("##FAILED##"), new NameEntry(words[i]));
            }
        }
        return nameComponents;
    }

    /**
     * After the user enters a full name to practice, search for its components and package them
     * Press enter to submit the full name.
     */
    @FXML
    private void doSearch() throws URISyntaxException {
        if(_searchBox.getText() == null || _searchBox.getText().isEmpty()) {
            return;
        }

        String fullNameStr = _searchBox.getText();
        String[] words = fullNameStr.split("[ -]");
        ObservableList<NameEntry> nameComponents = matchFullName(words);
        if(nameComponents.get(0).compareTo(new NameEntry("##FAILED##")) == 0) {
            StringBuilder nameList = new StringBuilder();
            int i;
            for(i = 1; i < nameComponents.size() - 1; i++) {
                NameEntry dummyEntry = nameComponents.get(i);
                nameList.append(dummyEntry.getName()).append(", ");
            }
            nameList.append(nameComponents.get(i).getName()).append(".");

            Alert a = new Alert(AlertType.INFORMATION);
            a.setTitle("Name(s) not found");
            a.setContentText("The following names were not matched:\n" + nameList.toString());
            a.showAndWait();
            return;
        }

        boolean notAlreadySelected = true;
        for(NameEntry entry: _selectedNames) {
            if(entry.compareTo(new NameEntry(_searchBox.getText())) == 0) {
                notAlreadySelected = false;
                break;
            }
        }
        if(notAlreadySelected) {
            CompositeName fullName = null;

            boolean notAlreadyExists = true;
            for(CompositeName entry: _addedComposites) {
                if(entry.compareTo(new NameEntry(_searchBox.getText())) == 0) {
                    notAlreadyExists = false;
                    fullName = entry;
                    break;
                }
            }

            if(notAlreadyExists) {
                fullName = new CompositeName(nameComponents, NameEntry.capitaliseNames(fullNameStr));
            }

            _addedComposites.add(fullName);
            updateSelectedList(_addedComposites);
            _addedComposites.clear();
        }
        _searchBox.clear();
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
            AchievementsManager.getInstance().hasImported(true);
            selectNames(names);
        }
        if(names == null) {
            return;
        }
    }

    /**
     * Given a list of Names, search for them in the database and select them if they exist.
     * @param names A list of dummy NameEntry objects (ones with only the name field set).
     * Sorry for ugly code :p
     */
    private void selectNames(ObservableList<NameEntry> names) {
        boolean exists = false;
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
        setupSearchBox();
    }

    @Override
    public void switchTo() {
        Collections.sort(_nameListView.getItems());
    }

    /**
     * Calls the help handler to show the pop up
     */
    @FXML
    private void help() {
        new HelpHandler(_helpButton,"list");
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
        AchievementsManager.getInstance().setMenu(SceneBuilder.LISTVIEW);
        _switcher.switchScene(SceneBuilder.ACHIEVEMENTS);
    }
}
