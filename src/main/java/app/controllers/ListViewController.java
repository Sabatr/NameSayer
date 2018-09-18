package app.controllers;

import app.views.SceneBuilder;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ListViewController extends ParentController{
    @FXML
    private ListView<String> _nameListView;
    @FXML
    private ListView<String> _versionListView;
    private ObservableList<String> _items;
    private List<String> _selectedList;

    public void initialize() {
        _selectedList = new ArrayList<String>();
        _items = _nameListView.getItems();
            File folder = new File("soundfiles");
            if (folder.exists()) {
                File[] folderArray = folder.listFiles();
                for (File file: folderArray) {
                    _items.add(file.toString());
                }
                _nameListView.setItems(_items);
            }
    }

    @FXML
    private void onClick() {
        String selected = _nameListView.getFocusModel().getFocusedItem();
        if (_selectedList.contains(selected)) {
            _selectedList.remove(selected);
        } else {
            _selectedList.add(selected);
        }
    }


    @FXML
    private void practiceButton() throws IOException {
        new SceneBuilder(_stage).load("Practice.fxml");
    }
}
