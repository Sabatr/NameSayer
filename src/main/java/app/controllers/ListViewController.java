package app.controllers;

import app.views.SceneBuilder;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class ListViewController extends ParentController{
    @FXML
    private ListView<String> _nameListView;
    @FXML
    private ListView<String> _versionListView;
    private ObservableList<String> _items;

    public void initialize() {
        _items = _nameListView.getItems();
        URL url = this.getClass().getResource("../soundfiles");
        //checks if there contains files in the directory
        if (url != null ) {
            File folder = new File(url.getPath());
            File[] folderArray = folder.listFiles();
            for (File file: folderArray) {
                _items.add(file.toString());
            }
            _nameListView.setItems(_items);
        }
    }

    @FXML
    private void practiceButton() throws IOException {
        new SceneBuilder(_stage).load("Practice.fxml");
    }
}
