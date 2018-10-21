package app.backend;

import app.backend.filesystem.FSWrapper;
import app.backend.filesystem.FSWrapperFactory;

import app.backend.filesystem.FileInstance;
import app.tools.AudioProcessor;
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
import java.util.Map;

public class CompositeName extends NameEntry {

    boolean _isProcessing = false;
    private ObservableList<NameEntry> _names;
    protected Version _mainVersion;

    public CompositeName(ObservableList<NameEntry> names, String fullname) throws URISyntaxException {
        super(fullname);
        _names = names;
        USER_VERSION_STR = "userCompositeName";

        FSWrapperFactory factory = new FSWrapperFactory(FSWrapper.class.getResource("FileSystem.xml").toURI());
        _fsMan = factory.buildFSWrapper();

        List<FileInstance> ratingsFilePathElements = _fsMan.getFilesByParameter("compRatings");
        FileInstance ratingsFileInstance = ratingsFilePathElements.get(ratingsFilePathElements.size() - 1);
        _ratingsFile = ratingsFileInstance.getPath();

        List<FileInstance> userRecordings = _fsMan.getFilesByParameter("userCompositeName", fullname);
        for(FileInstance file: userRecordings) {
            if(file.getTemplate().getType().equals("userCompositeName")) {
                Map<Integer, String> params = file.getParameters();
                addUserVersionWithAudio(params.get(4), params.get(2) + "_" + params.get(3),
                        file.getPath());
            }
        }


        LocalDateTime ldt = LocalDateTime.now();
        String formattedDate = ldt.getDayOfMonth() + "-" + ldt.getMonthValue() + "-" + ldt.getYear();
        String formattedTime = ldt.getHour() + "-" + ldt.getMinute() + "-" + ldt.getSecond();
        Path pathToAudio = _fsMan.createFilePath("compositeName", fullname, formattedDate, formattedTime, "YOU");
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

    @Override
    public String getHighestRating() {
        return _mainVersion._dateTime;
    }

    public void concatenateAudio(EventHandler<WorkerStateEvent> handler) throws IOException, URISyntaxException {
        _isProcessing = true;
        List<Path> audioPaths = new ArrayList<>();
        for(NameEntry name: _names) {
            audioPaths.add(name.getAudioForVersion(name.getHighestRating()));
        }

        AudioProcessor processor = new AudioProcessor(handler, _fsMan);
        processor.process(audioPaths, _mainVersion._resource, this);
    }

    public boolean isProcessing() {
        return _isProcessing;
    }

    public void setDoneProcessing() {
        _isProcessing = false;
    }

    public boolean hasConcat() {
        return Files.exists(_mainVersion._resource);
    }

    public Path getAudio() {
        return _mainVersion._resource;
    }

    @Override
    public Path addUserVersion(String author) {
        LocalDateTime ldt = LocalDateTime.now();
        String formattedDate = ldt.getDayOfMonth() + "-" + ldt.getMonthValue() + "-" + ldt.getYear();
        String formattedTime = ldt.getHour() + "-" + ldt.getMinute() + "-" + ldt.getSecond();
        Path resource = _fsMan.createFilePath(USER_VERSION_STR, getName(), formattedDate, formattedTime, DEFAULT_AUTHOR);

        _temporaryVersion = new Version(author, formattedDate + "_" + formattedTime, resource);
        return resource;
    };

    @Override
    public Path getAudioForVersion(String dateAndTime) {
        Path audio = super.getAudioForVersion(dateAndTime);
        if(audio == null && dateAndTime.equals(_mainVersion._dateTime)) {
            return _mainVersion._resource;
        } else {
            return audio;
        }
    }
}
