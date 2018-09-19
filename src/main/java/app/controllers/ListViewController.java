package app.controllers;

import app.views.SceneBuilder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This class controls the functionality of the list scene.
 *
 */
public class ListViewController extends ParentController{
    @FXML
    private ListView<String> _nameListView;
    @FXML
    private ListView<String> _versionListView;
    private File[] _folderArray;
    private ObservableList<String> _items;
    private ObservableList<String> _selectedItems;

    public void initialize() {
       _selectedItems = FXCollections.observableList(new ArrayList<>());
         _items = _nameListView.getItems();
            File folder = new File("soundfiles");
            if (folder.exists()) {
                _folderArray = folder.listFiles();
                for (File file: _folderArray) {
                    //makes sure it is a file, not a directory.
                    if (file.toString().contains(".")) {
                        String name = covertString(file.toString());
                        _items.add(name);
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
    private String covertString(String fullFileName) {
        int beginning = fullFileName.lastIndexOf('_');
        int end = fullFileName.lastIndexOf('.');
        return fullFileName.substring(beginning+1,end);
    }


    @FXML
    private void onClick() {
        ObservableList<String> _versionItems = FXCollections.observableList(new ArrayList<>());
        String name = _nameListView.getSelectionModel().getSelectedItem();
        //checks if all the files contain the name.
//        for (File file: _folderArray) {
//            if (file.toString().contains(name+".wav")) {
//            _versionItems.add(name);
//            _versionItems.addAll(getVersions(name));
//            }
//        }
        List<String> listOfFiles = new ArrayList<>();
        for (String itemName: _items) {
            listOfFiles.add(itemName+".wav");
        }
        if (listOfFiles.contains(name+".wav")) {
            _versionItems.add(name);
            _versionItems.addAll(getVersions(name));
        }
        _selectedItems = _nameListView.getSelectionModel().getSelectedItems();
        try {
            //a check, just in case the user wants to deselect when they only have one selected.
            if (!_selectedItems.contains(_versionItems.get(0))) {
                _versionItems = FXCollections.observableList(new ArrayList<>());
            }
        } catch(IndexOutOfBoundsException e) {}

        _versionListView.setItems(_versionItems);
    }

    private ObservableList<String> getVersions(String name) {
        File versionsFolder = new File("soundfiles/Versions/"+name);
        List<String> versions = new ArrayList<>();
        if (versionsFolder.exists()) {
            File[] versionFiles = versionsFolder.listFiles();
            for (File file : versionFiles) {
                versions.add(file.toString());
            }
        }
        return FXCollections.observableArrayList(versions);
    }

    @FXML
    private void practiceButton() throws IOException {
        SceneBuilder sceneBuilder = new SceneBuilder(_stage);
        sceneBuilder.getList(_selectedItems);
        sceneBuilder.load("Practice.fxml");
    }
}
