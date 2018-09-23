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
        GOOD, BAD, NONE;

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
    public Path addVersion() {
        return addVersion(DEFAULT_AUTHOR);
    }

    /**
     * Add a version to this NameEntry. After calling this, {@link NameEntry#finaliseLastVersion()} must be called
     * to add the version to the NameEntry.
     * @param author The author of this version
     * @return The filePath to use for the recording
     */
    public Path addVersion(String author) {
        LocalDateTime ldt = LocalDateTime.now();
        String formattedDate = ldt.getDayOfMonth() + "-" + ldt.getMonth() + "-" + ldt.getYear();
        String formattedTime = ldt.getHour() + "-" + ldt.getMinute() + "-" + ldt.getSecond();
        Path resource = _fsMan.createFile("soundFile", _name, formattedDate, formattedTime);
        String ratingFile = _fsMan.createFile("rating", _name, formattedDate, formattedTime).toString();

        _temporaryVersion = new Version(author, formattedDate + "_" + formattedTime, resource, ratingFile);
        return resource;
    }

    /**
     * Confirm the adding of the version that was last created.
     */
    public void finaliseLastVersion() {
        _versions.add(_temporaryVersion);
        _temporaryVersion = null;
    }

    /**
     * Used to add verions already on the filesystem
     */
    public void addVersionWithAudio(String auth, String date, Path resource, String ratingFile) {
        _versions.add(new Version(auth, date, resource, ratingFile));
    }

    /**
     * Fetch the name held by this NameEntry - same as the result of {@link NameEntry#toString()}
     * @return the name of this NameEntry
     */
    public String getName() {
        return _name;
    }

    /**
     * Fetch a string representation of this NameEntry - its name
     * @return the name of this NameEntry
     */
    public String toString() {
        return _name;
    }

    /**
     * Return a list of versions based on their ID: (at the moment a version's ID is its date)
     * @return List of version dates
     */
    public List<String> getVersions() {

        List<String> dates = new ArrayList<String>();
        dates.add(_mainVersion._dateTime);
        for(Version ver: _versions) {
            dates.add(ver._dateTime);
        }
        return dates;
    }

    /**
     * Return the filepath of the audio for the version
     * @param dateAndTime the date and time that identify the version
     * @return
     */
    public Path getAudioForVersion(String dateAndTime) {
        if(_mainVersion._dateTime.equals(dateAndTime)) {
            return _mainVersion._resource;
        }
        for(Version ver: _versions) {
            if(ver._dateTime.equals(dateAndTime)) {
                return ver._resource;
            }
        }
        return null;
    }

    /**
     * Rate the version identified by the given date and time
     * @param dateAndTime the date and time identifying the version
     * @param rating the {@link NameEntry.RATING} to give the version
     */
    public void rateVersion(String dateAndTime, RATING rating) {
        if(_mainVersion._dateTime.equals(dateAndTime)) {
            _mainVersion.rating = rating;
        }
        for(Version ver: _versions) {
            if(ver._dateTime.equals(dateAndTime)) {
                ver.rating = rating;
            }
        }
    }

    /**
     * Compare this NameEntry to another in terms of order. This is so that names can be alphabetised
     * @param o The name entry to compare with
     * @return The result of comparing the names of the NameEntry
     */
    @Override
    public int compareTo(NameEntry o) {
        return this._name.compareTo(o._name);
    }

    private class Version {

        public Version(String auth, String date, Path resource, String ratingFile) {
            _author = auth;
            _dateTime = date;
            _resource = resource;
            _ratingFile = ratingFile;
            rating = RATING.NONE;
        }

        public RATING rating;
        public String _author;
        public String _dateTime;
        public Path _resource;
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
                                pathPair.getValue(), "");
                        firstVersion = false;
                    } else {
                        addVersionWithAudio("unknown", parameters.get(2) + "_" + parameters.get(3),
                                pathPair.getValue(), "");
                    }
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
