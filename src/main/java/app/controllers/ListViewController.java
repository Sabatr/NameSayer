package app.controllers;

import app.views.SceneBuilder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.ButtonBar.ButtonData;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

/**
 * This class controls the functionality of the list scene.
 *
 */
public class ListViewController extends ParentController{
    @FXML
    private ListView<String> _nameListView;

    private File[] _folderArray;
    private ObservableList<String> _items;
    private ObservableList<String> _selectedItems;
    private enum Options {YES,NO,CANCEL}
    private Options _options;

    public void initialize() {
        _selectedItems = FXCollections.observableArrayList();
         _items = _nameListView.getItems();
            File folder = new File("soundfiles");
            if (folder.exists()) {
                _folderArray = folder.listFiles();
                for (File file: _folderArray) {
                    //makes sure it is a file, not a directory.
                  //  System.out.println("File: "+file.toString() +", list: " + _items+", contains: " + _items.contains(file.toString()));
                    if (file.toString().contains(".")) {
                        String name = convertString(file.toString());
                        //Checks for multiples
                        // if (!_items.contains(name)) {
                            _items.add(name);
                      //  }

                    }
                }
                Collections.sort(_items);
                _nameListView.setItems(_items);
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
        _selectedItems = FXCollections.observableArrayList(_nameListView.getSelectionModel().getSelectedItems());
       // System.out.println(_selectedItems);
    }

    @FXML
    private void clearSelection() {
        _selectedItems = FXCollections.observableArrayList();
        _nameListView.getSelectionModel().clearSelection();
    }


    @FXML
    private void practiceButton() throws IOException {
        if (_selectedItems.size() == 0) {
            alertNothingSelected();
        } else {
            alertUserConfirmation();
            if (_options != Options.CANCEL) {
                System.out.println(_selectedItems);
                //Currently it doesn't seem to work.
                if (_options == Options.YES) {
                    Collections.shuffle(_selectedItems);
                } else {
                    Collections.sort(_selectedItems);
                }
                SceneBuilder sceneBuilder = new SceneBuilder(_stage);
                sceneBuilder.getList(_selectedItems);
                sceneBuilder.load("Practice.fxml");
            }
        }
    }

    private void alertUserConfirmation() {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Randomize confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Do you wish to randomize your list?");
        ButtonType yesButton = new ButtonType("Yes");
        ButtonType noButton = new ButtonType("No");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(yesButton,noButton,cancelButton);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == yesButton){
            _options = Options.YES;
        } else if (result.get() == noButton) {
            _options = Options.NO;
        } else {
            _options = Options.CANCEL;
        }
    }


    private void alertNothingSelected() {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText("Error: No names selected.");
        alert.showAndWait();
    }



    @Override
    public void setInformation(ObservableList<String> _list) {
        //Reselects the chosen list.
        if (_list.size() != 0) {
            _selectedItems = _list;
            for (String name: _selectedItems) {
                _nameListView.getSelectionModel().select(name);
            }
        }
    }
}
