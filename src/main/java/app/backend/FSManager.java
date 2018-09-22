package app.backend;

import javafx.util.Pair;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;


/**
 * This class wraps my FSManager so that the calls are more intuitive for Assignment 3
 */
public class FSManager extends FSWrapper {
    // OLD METHODS

    public List<String> getExistingCreations() throws IOException {
        List<String> names = new ArrayList<String>();

        List<Pair<String, Path>> creations = getAllContentOfType("creation");
        for(Pair<String, Path> pathPair: creations) {
            names.add(pathPair.getValue().getFileName().toString());
        }
        return names;
    }

    public boolean fileExistsInCreations(String ... path) {
        path[path.length - 1] = path[path.length - 1].trim();
        return Files.exists(Paths.get(rootContentDir.toString(), path));
    }

    public void createCreationsDir(String name) {
        createDirectoryStruct("creation", name);
    }

    public String getPathToCreation(String name) {
        return Paths.get(rootContentDir.toString(), name).toString();
    }

    public void deleteCreationFolder(String name) throws IOException, URISyntaxException {
        deleteFiles("creation", name);
    }
}
