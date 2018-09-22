package app.controllers;

import app.backend.NameEntry;
import app.views.SceneBuilder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

import java.io.IOException;
import java.util.ArrayList;

/**
 * This class holds the functionality of the main menu.
 */
public class MainMenuController extends ParentController {
    @FXML
    private Button _databaseButton;

    private ObservableList _selectedList;

    /**
     * Holds the selected list information.
     * @param _list
     */
    @Override
    public void setInformation(ObservableList<NameEntry> allItems, ObservableList<NameEntry> _list) {
        super.setInformation(allItems, _list);
        if (!_list.isEmpty()) {
            _selectedList = FXCollections.observableList(_list);
        } else {
            _selectedList = FXCollections.observableList(new ArrayList<>());
        }
    }

    /**
     * Allows the scene to switch the view list.
     * @throws IOException
     */
    @FXML
    private void goToList() throws IOException {
        SceneBuilder builder = new SceneBuilder(_allNames, _stage);
        builder.getList(_selectedList);
        builder.load("ListView.fxml");
    }

    /**
     * Allows the scene to switch to the options menu.
     */
    @FXML
    private void options() {

    }
}
