package app.controllers;

import app.views.SceneBuilder;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;

import java.io.IOException;

public class PracticeController extends ParentController {
    private ObservableList<String> _practiceList;

    @FXML
    public void initialize() {

    }

    @FXML
    private void goBack() throws IOException {
        new SceneBuilder(_stage).load("ListView.fxml");
    }

    @Override
    public void getInformation(ObservableList<String> items) {
        _practiceList = items;
        for (String s: _practiceList) {
            System.out.println(s);
        }
    }
}
