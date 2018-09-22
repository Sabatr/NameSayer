package app.backend;

import javafx.util.Pair;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Self-managed class representing a unique name entry
 */
public class NameEntry implements Comparable<NameEntry> {

    public enum RATING {
        GOOD, BAD, NONE
    }
    private String DEFAULT_AUTHOR = "You";
    private FSWrapper _fsMan;

    private String _name;
    private Version _mainVersion;
    private Version _temporaryVersion;
    private List<Version> _versions = new ArrayList<>();

    public NameEntry(String name) {
        _name = name;
    }

    /**
     * Adds a version with the default author
     * @see NameEntry#addVersion(String author)
     * @return The filePath to use for the recording
     */
    public String addVersion() {
        return addVersion(DEFAULT_AUTHOR);
    }

    /**
     * Add a version to this NameEntry. After calling this, {@link NameEntry#finaliseLastVersion()} must be called
     * to add the version to the NameEntry.
     * @param author The author of this version
     * @return The filePath to use for the recording
     */
    public String addVersion(String author) {
        LocalDateTime ldt = LocalDateTime.now();
        String formattedDate = ldt.getDayOfMonth() + "-" + ldt.getMonth() + "-" + ldt.getYear();
        String formattedTime = ldt.getHour() + "-" + ldt.getMinute() + "-" + ldt.getSecond();
        String resource = _fsMan.createFile("soundFile", _name, formattedDate, formattedTime).toString();
        String ratingFile = _fsMan.createFile("rating", _name, formattedDate, formattedTime).toString();

        _temporaryVersion = new Version(author, formattedDate + "_" + formattedTime, resource, ratingFile);
        return resource;
    }

    /**
     *
     */
    public void finaliseLastVersion() {
        _versions.add(_temporaryVersion);
        _temporaryVersion = null;
    }

    /**
     * Used to add verions already on the filesystem
     */
    public void addVersionWithAudio(String auth, String date, String resource, String ratingFile) {
        _versions.add(new Version(auth, date, resource, ratingFile));
    }

    public String getName() {
        return _name;
    }

    public String toString() {
        return _name;
    }

    @Override
    public int compareTo(NameEntry o) {
        return this._name.compareTo(o._name);
    }

    private class Version {

        public Version(String auth, String date, String resource, String ratingFile) {
            _author = auth;
            _dateTime = date;
            _resource = resource;
            _ratingFile = ratingFile;
        }

        public String _author;
        public String _dateTime;
        public String _resource;
        public String _ratingFile;
    }

    // ********** Extracting names from the filesystem **********

    /**
     * Extract names from a filesystem. I will epxlain why this is so complicated later on.
     */
    public static ArrayList<NameEntry> populateNames() throws URISyntaxException {
        FSWrapper fsWrapOne = new FSWrapper(FSWrapper.class.getResource("StartFS.xml").toURI());
        FSWrapper fsWrapTwo = new FSWrapper(FSWrapper.class.getResource("FileSystem.xml").toURI());
        try {
            fsWrapOne.copyTo(fsWrapTwo);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ArrayList<NameEntry> names = new ArrayList<>();
        List<Pair<String, Path>> paths = fsWrapTwo.getAllContentOfType("nameEntry");

        List<Pair<String, Path>> pathsForSingleEntry = new ArrayList<>();
        for(Pair<String, Path> pathPair: paths) {
            if(pathPair.getKey().equals("nameEntry")) {
                pathsForSingleEntry.add(pathPair);
                names.add(new NameEntry(fsWrapTwo, pathsForSingleEntry));
                pathsForSingleEntry.clear();
            } else {
                pathsForSingleEntry.add(pathPair);
            }
        }
        return names;
    }

    /**
     * Extract a NameEntry from the filesystem. I will epxlain why this is so complicated later on.
     */
    private NameEntry(FSWrapper fsManager, List<Pair<String, Path>> paths) {
        boolean firstVersion = true;
        Map<Integer, String> parameters;
        for(Pair<String, Path> pathPair: paths) {
            switch (pathPair.getKey()) {
                case "soundFile":
                    parameters = fsManager.getParamsForFile(pathPair.getValue(), pathPair.getKey());
                    if(firstVersion) {
                        _mainVersion = new Version("unknown", parameters.get(2) + "_" + parameters.get(3),
                                pathPair.getValue().toString(), "");
                        firstVersion = false;
                    }
                    addVersionWithAudio("unknown", parameters.get(2) + "_" + parameters.get(3),
                            pathPair.getValue().toString(), "");
                    break;
                case "nameEntry":
                    parameters = fsManager.getParamsForFile(pathPair.getValue(), pathPair.getKey());
                    _name = parameters.get(1);
                    break;
                default:
                    break;
            }
        }

        _fsMan = fsManager;
    }
}
