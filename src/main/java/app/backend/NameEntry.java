package app.backend;

import app.backend.filesystem.FSWrapper;
import app.backend.filesystem.FSWrapperFactory;
import app.backend.filesystem.FileInstance;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Self-managed class representing a unique name entry
 */
public class NameEntry implements Comparable<NameEntry> {

    protected String DEFAULT_AUTHOR = "You";
    protected FSWrapper _fsMan;
    private String _name;

    protected Path _ratingsFile;
    protected Version _temporaryVersion;
    protected List<Version> _versions = new ArrayList<>();
    protected List<Version> _userVersions = new ArrayList<>();

    protected String USER_VERSION_STR = "userVersion";

    /**
     * Construct a dummy NameEntry with only a name and no associated audio
     */
    public NameEntry(String name) {
        _name = name;
    }

    /**
     * Adds a version with the default author
     * @see NameEntry#addUserVersion(String author)
     * @return The filePath to use for the recording
     */
    public Path addUserVersion() {
        return addUserVersion(DEFAULT_AUTHOR);
    }

    /**
     * Add a version to this NameEntry. After calling this, {@link NameEntry#finaliseLastVersion()} must be called
     * to add the version to the NameEntry.
     * @param author The author of this version
     * @return The filePath to use for the recording
     */
    public Path addUserVersion(String author) {
        LocalDateTime ldt = LocalDateTime.now();
        String formattedDate = ldt.getDayOfMonth() + "-" + ldt.getMonthValue() + "-" + ldt.getYear();
        String formattedTime = ldt.getHour() + "-" + ldt.getMinute() + "-" + ldt.getSecond();
        Path resource = _fsMan.createFilePath(USER_VERSION_STR, _name, formattedDate, formattedTime, author);

        _temporaryVersion = new Version(author, formattedDate + "_" + formattedTime, resource);
        return resource;
    }

    /**
     * Confirm the adding of the version that was last created.
     */
    public void finaliseLastVersion() {
        if(_temporaryVersion != null) {
            _userVersions.add(_temporaryVersion);
        }
        _temporaryVersion = null;
    }

    /**
     * Deletes the temporary recording and throws away the new information
     */
    public void throwAwayNew() {
        if(_temporaryVersion != null) {
            try {
                /*
                TODO: An exception is thrown "The process cannot access the file because it is being used by another process",
                TODO: Probably need to kill the process somewhere you call a new thread.
                To replicate: Record on windows, play it back, cancel.
                 */
                Files.deleteIfExists(_temporaryVersion._resource);
            } catch (IOException e) {
                //e.printStackTrace();
            }
            _temporaryVersion = null;
        }
    }

    /**
     * Used to add versions already on the filesystem
     */
    public void addVersionWithAudio(String auth, String date, Path resource) {
        _versions.add(new Version(auth, date, resource));
    }

    public void addUserVersionWithAudio(String auth, String date, Path resource) {
        _userVersions.add(new Version(auth, date, resource));
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
     * Return the filepath of the audio for the version
     * @param dateAndTime the date and time that identify the version
     * @return
     */
    public Path getAudioForVersion(String dateAndTime) {
        if(_temporaryVersion != null && _temporaryVersion._dateTime.equals(dateAndTime)) {
            return _temporaryVersion._resource;
        }
        for(Version ver: _versions) {
            if(ver._dateTime.equals(dateAndTime)) {
                return ver._resource;
            }
        }
        for(Version ver: _userVersions) {
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

    public String getHighestRating() {
        String dateAndTime = _versions.get(0)._dateTime;
        int highestRating = _versions.get(0).rating;
        for (Version version : _versions) {
            if (version.rating > highestRating) {
                highestRating = version.rating;
                dateAndTime = version._dateTime;
            }
        }
        return dateAndTime;
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
        //defaults to 5
        return 5;
    }

    /**
     * Retrieves the dates of all the user-recorded versions. (The dates can be used as identifiers).
     * The output is sorted by date.
     */
    public List<String> getUserVersions() {
        SimpleDateFormat format = new SimpleDateFormat("d-M-y_H-m-s");
        List<Date> dates = new ArrayList<>();
        List<String> versionDates = new ArrayList<>();
        for(Version ver: _userVersions) {
            try {
                dates.add(format.parse(ver._dateTime));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        Collections.sort(dates);
        for(Date date: dates) {
            versionDates.add(format.format(date));
        }
        return versionDates;
    }

    /**
     * Deletes a user version
     */
    public void deleteUserVersion(String date) {
        Version toDelete = null;
        for(Version ver: _userVersions) {
            if(ver._dateTime.equals(date)) {
                String[] dateComponents = date.split("_");
                _fsMan.deleteFiles(USER_VERSION_STR, _name, dateComponents[0], dateComponents[1], ver._author);
                toDelete = ver;
            }
        }
        if(toDelete != null) {
            _userVersions.remove(toDelete);
        }
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
    public static FSWrapper populateNames() throws URISyntaxException {
        return populateNames(null);
    }

    /**
     * Extract names from a folder and copy them to the main filesystem
     */
    public static FSWrapper populateNames(Path soundfilesFolder) throws URISyntaxException {
        FSWrapperFactory factoryOne = new FSWrapperFactory(FSWrapperFactory.class.getResource("StartFS.xml").toURI(), soundfilesFolder);
        FSWrapperFactory factoryTwo = new FSWrapperFactory(FSWrapperFactory.class.getResource("FileSystem.xml").toURI());

        FSWrapper fsWrapOne = factoryOne.buildFSWrapper();
        FSWrapper fsWrapTwo = factoryTwo.buildFSWrapper();

        fsWrapTwo.createDirectoryStruct("compRatings");
        try {
            fsWrapOne.copyTo(fsWrapTwo);
        } catch (IOException e) {
            e.printStackTrace();
        }

        fsWrapTwo.createDirectoryStruct("userComposites");

        return fsWrapTwo;
    }

    /**
     * Having populated the names database, get all the names present
     */
    public static ArrayList<NameEntry> getNames() throws URISyntaxException {
        FSWrapperFactory factoryTwo = new FSWrapperFactory(FSWrapperFactory.class.getResource("FileSystem.xml").toURI());
        FSWrapper fsWrapTwo = factoryTwo.buildFSWrapper();

        ArrayList<NameEntry> names = new ArrayList<>();
        List<FileInstance> files = fsWrapTwo.getAllContentOfType("nameEntry");

        List<FileInstance> pathsForSingleEntry = new ArrayList<>();
        for(FileInstance file: files) {
            if(file.getTemplate().getType().equals("nameEntry")) {
                pathsForSingleEntry.add(file);
                names.add(new NameEntry(fsWrapTwo, pathsForSingleEntry));
                pathsForSingleEntry.clear();
            } else {
                pathsForSingleEntry.add(file);
            }
        }
        return names;
    }

    /**
     * Extract a NameEntry from the filesystem
     */
    private NameEntry(FSWrapper fsManager, List<FileInstance> files) {
        Collections.reverse(files);
        Map<Integer, String> parameters;
        for(FileInstance file: files) {
            switch (file.getTemplate().getType()) {
                case "userVersion":
                    parameters = file.getParameters();
                    addUserVersionWithAudio(parameters.get(4), parameters.get(2) + "_" + parameters.get(3),
                            file.getPath());
                    break;
                case "soundFile":
                    parameters = file.getParameters();
                    addVersionWithAudio(parameters.get(4), parameters.get(2) + "_" + parameters.get(3),
                                file.getPath());
                    break;
                case "nameEntry":
                    parameters = file.getParameters();
                    _name = capitaliseNames(parameters.get(1));
                    break;
                case "rating":
                    _ratingsFile = file.getPath();
                default:
                    break;
            }
        }

        _fsMan = fsManager;
    }

    public static String capitaliseNames(String name) {
        StringBuilder formattedName = new StringBuilder();
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
            } else {
                newWord = true;
                formattedName.append(ch);
            }
        }
        return formattedName.toString();
    }
}
