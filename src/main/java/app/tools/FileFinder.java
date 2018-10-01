package app.tools;

import app.backend.NameEntry;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class FileFinder {
    private String _type;
    private File _chosenFile;
    public FileFinder(String type) {
        _type = type;
    }
    public void choose(Stage stage) {
        if (_type.equals("sound")) {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Directory chooser");
            _chosenFile = directoryChooser.showDialog(stage);
        } else if (_type.equals("practice")) {
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("TEXT files (*.txt)","*.txt");
            fileChooser.getExtensionFilters().add(extensionFilter);
            fileChooser.setTitle("Choose a file");
            _chosenFile = fileChooser.showOpenDialog(stage);
        }
    }

    public ObservableList<NameEntry> getContent() {
        ObservableList<NameEntry> list = FXCollections.observableArrayList();
        if (_chosenFile != null) {
            try {
                Scanner sc = new Scanner(_chosenFile);
                while (sc.hasNextLine()) {
                    for (String name: sc.nextLine().split("[ -]")) {
                        list.add(new NameEntry(name));
                    }
                }
            } catch (IOException exception) {
                System.exit(1);
            }
        }
        return list;
    }
}
