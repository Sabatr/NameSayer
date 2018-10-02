package app.tools;

import app.backend.NameEntry;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Scanner;

/**
 * This class allows the user to find files in their system.
 */
public class FileFinder {
    private String _type;
    private File _chosenFile;
    public FileFinder(String type) {
        _type = type;
    }

    /**
     * Chooses whether to open a directory chooser or txt file chooser
     * @param stage
     */
    public FileFinder choose(Stage stage) {
        if (_type.equals("sound")) {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Directory chooser");
            _chosenFile = directoryChooser.showDialog(stage);
            //NOTE: nothing else is done with the directory system yet.
        } else if (_type.equals("practice")) {
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("TEXT files (*.txt)","*.txt");
            fileChooser.getExtensionFilters().add(extensionFilter);
            fileChooser.setTitle("Choose a file");
            _chosenFile = fileChooser.showOpenDialog(stage);
        }
        return this;
    }

    /**
     *
     * @return the content in a txt file. Currently, it separates the spaces and hyphens.
     */
    public ObservableList<NameEntry> getContent() throws URISyntaxException {
        if (_chosenFile != null) {
            if (_type.equals("practice")) {
                ObservableList<NameEntry> list = FXCollections.observableArrayList();
                try {
                    Scanner sc = new Scanner(_chosenFile);
                    while (sc.hasNextLine()) {
                        for (String name : sc.nextLine().split("[ -]")) {
                            list.add(new NameEntry(name));
                        }
                    }
                } catch (IOException exception) {
                    System.exit(1);
                }
                return list;
            } else if (_type.equals("sound")) {
                NameEntry.populateNames(_chosenFile.toPath());
                return FXCollections.observableArrayList(NameEntry.getNames());
            }
        }
        return null;
    }
}
