package app.controllers;

import app.views.SceneBuilder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

import java.io.IOException;
import java.util.ArrayList;

public class MainMenuController extends ParentController {
    @FXML
    private Button _databaseButton;

    private ObservableList _selectedList;

    public void setInformation(ObservableList _list) {
        if (!_list.isEmpty()) {
            _selectedList = FXCollections.observableList(_list);
        } else {
            _selectedList = FXCollections.observableList(new ArrayList<>());
        }
    }


    @FXML
    private void goToList() throws IOException {
        SceneBuilder builder = new SceneBuilder(_stage);
        builder.getList(_selectedList);
        builder.load("ListView.fxml");
    }
}
