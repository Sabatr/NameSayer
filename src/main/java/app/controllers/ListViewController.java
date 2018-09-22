package app.controllers;

import app.views.SceneBuilder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;

import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

/**
 * This class controls the functionality of the list scene.
 *
 */
public class ListViewController extends ParentController{
    @FXML
    private ListView<String> _nameListView;
    @FXML
    private ToggleButton _sortedButton;
    @FXML
    private ToggleButton _randomButton;


    private File[] _folderArray;
    private ObservableList<String> _allNames;
    private ObservableList<String> _selectedNames;

    public void initialize() {
        _sortedButton.setDisable(true);
        _selectedNames = FXCollections.observableArrayList();
         _allNames = _nameListView.getItems();
            File folder = new File("soundfiles");
            if (folder.exists()) {
                _folderArray = folder.listFiles();
                for (File file: _folderArray) {
                    //makes sure it is a file, not a directory.
                  //  System.out.println("File: "+file.toString() +", list: " + _allNames+", contains: " + _allNames.contains(file.toString()));
                    if (file.toString().contains(".")) {
                        String name = convertString(file.toString());
                        //Checks for multiples
                        // if (!_allNames.contains(name)) {
                            _allNames.add(name);
                      //  }

                    }
                }
                Collections.sort(_allNames);
                _nameListView.setItems(_allNames);
            }
            //CTRL+Click to select multiple
        _nameListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    /**
     * Not sure if they will always be formatted like this.
     * @param fullFileName
     * @return
     */
    private String convertString(String fullFileName) {
        int beginning = fullFileName.lastIndexOf('_');
        int end = fullFileName.lastIndexOf('.');
        return fullFileName.substring(beginning+1,end);
    }

    @FXML
    private void onClick() {
        // String name = _nameListView.getSelectionModel().getSelectedItem();
        //FXCollections was used so that selected items could be modified.
        _selectedNames = FXCollections.observableArrayList(_nameListView.getSelectionModel().getSelectedItems());
       // System.out.println(_selectedNames);
    }

    @FXML
    private void clearSelection() {
        _selectedNames = FXCollections.observableArrayList();
        _nameListView.getSelectionModel().clearSelection();
    }

    @FXML
    private void onSort() {
        _sortedButton.setDisable(true);
        _randomButton.setDisable(false);
    }

    @FXML
    private void onRandom() {
        _randomButton.setDisable(true);
        _sortedButton.setDisable(false);
    }

    @FXML
    private void practiceButton() throws IOException {
        if (_selectedNames.size() == 0) {
            alertNothingSelected();
        } else {
                if (_sortedButton.isDisabled()) {
                    Collections.sort(_selectedNames);
                } else {
                    Collections.shuffle(_selectedNames);
                }
                SceneBuilder sceneBuilder = new SceneBuilder(_stage);
                //Determines if the random button is disabled. So when we switch back views, it's still there.
                _selectedNames.add(_randomButton.isDisable()+"");
                sceneBuilder.getList(_selectedNames);
                sceneBuilder.load("Practice.fxml");
        }
    }

    private void alertNothingSelected() {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText("Error: No names selected.");
        alert.showAndWait();
    }

    @FXML
    private void goBack() throws IOException{
        SceneBuilder builder = new SceneBuilder(_stage);
        _selectedNames.add(_randomButton.isDisable()+"");
        builder.getList(_selectedNames);
        builder.load("MainMenu.fxml");

    }


    @Override
    public void setInformation(ObservableList<String> _list) {
        if (!_list.isEmpty()) {
            String randomOrNot = _list.get(_list.size()-1);
            _list.remove(_list.size()-1);
            if (randomOrNot.equals("true")) {
                onRandom();
            } else {
                onSort();
            }
        }
        //Reselects the chosen list.
        if (_list.size() != 0) {
            _selectedNames = _list;
            for (String name: _selectedNames) {
                _nameListView.getSelectionModel().select(name);
            }
        }
    }
}
