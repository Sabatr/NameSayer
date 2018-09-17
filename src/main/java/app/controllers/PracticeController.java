package app.controllers;

import app.views.SceneBuilder;
import javafx.fxml.FXML;

import java.io.IOException;

public class PracticeController extends ParentController {

    @FXML
    private void goBack() throws IOException {
        new SceneBuilder(_stage).load("ListView.fxml");
    }
}
