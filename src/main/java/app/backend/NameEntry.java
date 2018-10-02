package app.backend;

import app.backend.filesystem.FSWrapper;
import javafx.util.Pair;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Self-managed class representing a unique name entry
 */
public class NameEntry implements Comparable<NameEntry> {

    private String DEFAULT_AUTHOR = "You";
    protected FSWrapper _fsMan;
    private String _name;
    protected Version _mainVersion;
    protected Path _ratingsFile;
    protected Version _temporaryVersion;
    private List<Version> _versions = new ArrayList<>();

    /**
     * Construct a dummy NameEntry with only a name and no associated audio
     */
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
        String formattedDate = ldt.getDayOfMonth() + "-" + ldt.getMonthValue() + "-" + ldt.getYear();
        String formattedTime = ldt.getHour() + "-" + ldt.getMinute() + "-" + ldt.getSecond();
        Path resource = _fsMan.createFile("soundFile", _name, formattedDate, formattedTime);

        _temporaryVersion = new Version(author, formattedDate + "_" + formattedTime, resource);
        return resource;
    }

    /**
     * Get temporary version audio
     */

    /**
     * Confirm the adding of the version that was last created.
     */
    public void finaliseLastVersion() {
        if(_temporaryVersion != null) {
            _versions.add(_temporaryVersion);
        }
        _temporaryVersion = null;
    }

    /**
     * Deletes the temporary recording and throws away the new information
     */
    public void throwAwayNew() {
        if(_temporaryVersion != null) {
            try {
                Files.deleteIfExists(_temporaryVersion._resource);
            } catch (IOException e) {
                e.printStackTrace();
            }
            _temporaryVersion = null;
        }
    }

    /**
     * Used to add verions already on the filesystem
     */
    public void addVersionWithAudio(String auth, String date, Path resource) {
        _versions.add(new Version(auth, date, resource));
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
        if(_temporaryVersion != null && _temporaryVersion._dateTime.equals(dateAndTime)) {
            return _temporaryVersion._resource;
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
     * @param rating the rating to give the version, out of 10
     */
    public void rateVersion(String dateAndTime, int rating) {
        if(_mainVersion._dateTime.equals(dateAndTime)) {
            _mainVersion.rating = rating;
            saveRating(dateAndTime, rating);
        }
        if(_temporaryVersion != null && _temporaryVersion._dateTime.equals(dateAndTime)) {
            _temporaryVersion.rating = rating;
            saveRating(dateAndTime, rating);
        }
        for(Version ver: _versions) {
            if(ver._dateTime.equals(dateAndTime)) {
                ver.rating = rating;
                saveRating(dateAndTime, rating);
            }
        }
    }

    /**
     * Save the rating to the file
     */
    private void saveRating(String dateAndTime, int rating) {
        List<String> fileContents = new ArrayList<>();
        int lineNumber = 0;
        boolean found = false;

        try(BufferedReader reader = new BufferedReader(new FileReader(_ratingsFile.toFile()))) {
            String line;
            while((line = reader.readLine()) != null) {
                fileContents.add(line);
                if(!found && line.contains(dateAndTime)) {
                    found = true;
                } else {
                    lineNumber++;
                }
            }
        } catch (FileNotFoundException e) {
            try {
                Files.createFile(_ratingsFile);
            } catch (IOException e1) {
                throw new RuntimeException("Error trying to create file for ratings", e1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(found) {
            fileContents.set(lineNumber, dateAndTime + ": " + rating);
        } else {
            fileContents.add(dateAndTime + ": " + rating);
        }

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(_ratingsFile.toFile()))) {
            for(String line: fileContents) {
                writer.write(line + "\n");
            }
        } catch (FileNotFoundException e) {
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Fetch the rating for a particular version
     * @return an integer rating out of 10, or -1 if the version has never been rated
     */
    public int getRating(String dateAndTime) {
        if(_mainVersion._dateTime.equals(dateAndTime)) {
            if(_mainVersion.rating != -1) {
                return _mainVersion.rating;
            } else {
                return getRatingFromFile(dateAndTime);
            }
        }
        for(Version ver: _versions) {
            if(ver._dateTime.equals(dateAndTime)) {
                if(ver.rating != -1) {
                    return ver.rating;
                } else {
                    return getRatingFromFile(dateAndTime);
                }
            }
        }
        return -1;
    }

    /**
     * Get rating from file
     */
    private int getRatingFromFile(String dateAndTime) {

        try(BufferedReader reader = new BufferedReader(new FileReader(_ratingsFile.toFile()))) {
            String line;
            while((line = reader.readLine()) != null) {
                if(line.contains(dateAndTime)) {
                    int rating = Integer.parseInt(line.substring(line.indexOf(": ") + 2));
                    return rating;
                }
            }
        } catch (FileNotFoundException e) {
            try {
                Files.createFile(_ratingsFile);
            } catch (IOException e1) {
                throw new RuntimeException("Error trying to create file for ratings", e1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public double averageRating() {
        int sum = 0;
        int n = 0;
        if(_mainVersion.rating != 1) {
            sum += _mainVersion.rating;
            n++;
        }
        for(Version ver: _versions) {
            int rating = getRating(ver._dateTime);
            if(rating != 1) {
                sum += rating;
                n++;
            }
        }

        if(n == 0) {
            return -1;
        }
        return ((double) sum / (double) n);
    }

    /**
     * Compare this NameEntry to another in terms of order. This is so that names can be alphabetised
     * @param o The name entry to compare with
     * @return The result of comparing the names of the NameEntry
     */
    @Override
    public int compareTo(NameEntry o) {
        //UPDATED by Brian, toLowerCase() was added because of case insensitive stuff.
        return this._name.toLowerCase().compareTo(o._name.toLowerCase());
    }

    protected class Version {

        Version(String auth, String date, Path resource) {
            _author = auth;
            _dateTime = date;
            _resource = resource;
            rating = getRatingFromFile(_dateTime);
        }

        int rating;
        String _author;
        String _dateTime;
        Path _resource;
    }

    // ********** Extracting names from the filesystem **********

    /**
     * Extract names from the default folder and copy them to the main filesystem
     */
    public static void populateNames() throws URISyntaxException {
        populateNames(null);
    }

    /**
     * Extract names from a folder and copy them to the main filesystem
     */
    public static void populateNames(Path soundfilesFolder) throws URISyntaxException {
        FSWrapper fsWrapOne = new FSWrapper(FSWrapper.class.getResource("StartFS.xml").toURI(), soundfilesFolder);
        FSWrapper fsWrapTwo = new FSWrapper(FSWrapper.class.getResource("FileSystem.xml").toURI());
        try {
            fsWrapOne.copyTo(fsWrapTwo);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Having populated the names database, get all the names present
     */
    public static ArrayList<NameEntry> getNames() throws URISyntaxException {
        FSWrapper fsWrapTwo = new FSWrapper(FSWrapper.class.getResource("FileSystem.xml").toURI());

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
     * Extract a NameEntry from the filesystem
     */
    private NameEntry(FSWrapper fsManager, List<Pair<String, Path>> paths) {
        boolean firstVersion = true;
        List<Pair<String, Path>> revList = paths;
        Collections.reverse(revList);
        Map<Integer, String> parameters;
        for(Pair<String, Path> pathPair: paths) {
            switch (pathPair.getKey()) {
                case "soundFile":
                    parameters = fsManager.getParamsForFile(pathPair.getValue(), pathPair.getKey());
                    if(firstVersion) {
                        _mainVersion = new Version(parameters.get(4), parameters.get(2) + "_" + parameters.get(3),
                                pathPair.getValue());
                        firstVersion = false;
                    } else {
                        addVersionWithAudio(parameters.get(4), parameters.get(2) + "_" + parameters.get(3),
                                pathPair.getValue());
                    }
                    break;
                case "nameEntry":
                    parameters = fsManager.getParamsForFile(pathPair.getValue(), pathPair.getKey());
                    StringBuilder formattedName = new StringBuilder();
                    String name = parameters.get(1);
                    char[] chars = new char[name.length()];
                    name.getChars(0, name.length(), chars, 0);
                    boolean newWord = true;

                    for(char ch: chars) {
                        if(Character.isAlphabetic(ch)) {
                            if(newWord) {
                                formattedName.append(Character.toUpperCase(ch));
                                newWord = false;
                            } else {
                                formattedName.append(ch);
                            }
                        } else if(Character.isWhitespace(ch)) {
                            newWord = true;
                            formattedName.append(ch);
                        }
                    }
                    _name = formattedName.toString();
                    break;
                case "rating":
                    _ratingsFile = pathPair.getValue();
                default:
                    break;
            }
        }

        _fsMan = fsManager;
    }
}
