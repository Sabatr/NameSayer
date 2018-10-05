package app.backend;

import app.backend.filesystem.FSWrapper;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CompositeName extends NameEntry {

    private ObservableList<NameEntry> _names;

    public CompositeName(ObservableList<NameEntry> names, String fullname) throws URISyntaxException {
        super(fullname);
        _names = names;
        _fsMan = new FSWrapper(FSWrapper.class.getResource("FileSystem.xml").toURI());

        List<Path> ratingsFilePathElements = _fsMan.getFilesByParameter("compRatings");
        _ratingsFile = ratingsFilePathElements.get(ratingsFilePathElements.size() - 1);

        LocalDateTime ldt = LocalDateTime.now();
        String formattedDate = ldt.getDayOfMonth() + "-" + ldt.getMonthValue() + "-" + ldt.getYear();
        String formattedTime = ldt.getHour() + "-" + ldt.getMinute() + "-" + ldt.getSecond();
        Path pathToAudio = _fsMan.createFile("compositeName", formattedDate, formattedTime, fullname);
        _mainVersion = new Version("YOU", formattedDate + "_" + formattedTime, pathToAudio);
    }

    public ObservableList<NameEntry> get() {
        return _names;
    }
    public static String fullName(List<NameEntry> nameComponents) {
        StringBuilder fullNameStr = new StringBuilder();
        int i;
        for(i = 0; i < nameComponents.size() - 1; i++) {
            fullNameStr.append(nameComponents.get(i) + " ");
        }
        fullNameStr.append(nameComponents.get(i));
        return fullNameStr.toString();
    }

    public void concatenateAudio(EventHandler<WorkerStateEvent> handler) throws IOException {
        System.out.println(_names);
        List<Path> audioPaths = new ArrayList<>();
        for(NameEntry name: _names) {
            audioPaths.add(name.getAudioForVersion(name.getHighestRating()));
        }
//        System.out.println(_names);
//        System.out.println(audioPaths);
        BashRunner br = new BashRunner(handler);
        br.runConcatCommands(audioPaths, _mainVersion._resource);
    }

    public boolean hasConcat() {
        return Files.exists(_mainVersion._resource);
    }

    public Path getAudio() {
        return _mainVersion._resource;
    }

    @Override
    public Path addVersion(String author) {
        LocalDateTime ldt = LocalDateTime.now();
        String formattedDate = ldt.getDayOfMonth() + "-" + ldt.getMonthValue() + "-" + ldt.getYear();
        String formattedTime = ldt.getHour() + "-" + ldt.getMinute() + "-" + ldt.getSecond();
        Path resource = _fsMan.createFile("compositeName", formattedDate, formattedTime, getName());

        _temporaryVersion = new Version(author, formattedDate + "_" + formattedTime, resource);
        return resource;
    };
}
