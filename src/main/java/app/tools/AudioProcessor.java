package app.tools;

import app.backend.BashRunner;
import app.backend.CompositeName;
import app.backend.filesystem.FSWrapper;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AudioProcessor implements EventHandler<WorkerStateEvent> {

    private FSWrapper _fsMan;
    private CompositeName _name;
    private EventHandler<WorkerStateEvent> _finalHandler;
    private List<Path> _untrimmedAudio;
    private List<Path> _trimmedAudio;
    private int i = 0;

    private Path _destination;

    public AudioProcessor(EventHandler<WorkerStateEvent> handler, FSWrapper fsWrapper) {
        _finalHandler = handler;
        _trimmedAudio = new ArrayList<>();
        _fsMan = fsWrapper;
    }

    public void process(List<Path> audioPaths, Path destination, CompositeName theName) throws URISyntaxException {
        if(audioPaths.size() < 1 || destination == null) {
            return;
        }
        _destination = destination;
        _untrimmedAudio = audioPaths;
        _name = theName;

        for(Path file: audioPaths) {
            Map<Integer, String> params = _fsMan.getParametersForFile(file, "soundFile");
            _trimmedAudio.add(_fsMan.createFilePath("trimmedName", params.get(1)));
        }

        trimAFile();
    }

    public void trimAFile() throws URISyntaxException {
        BashRunner br = new BashRunner(this::handle);
        br.runTrimSilenceCommand(_untrimmedAudio.get(i), _trimmedAudio.get(i));
    }

    public void concatenate() throws URISyntaxException, IOException {
        BashRunner br = new BashRunner(this::handle);
        br.runConcatCommands(_trimmedAudio, _destination);
    }

    @Override
    public void handle(WorkerStateEvent event) {
        if(event.getEventType().equals(WorkerStateEvent.WORKER_STATE_SUCCEEDED)) {
            if(event.getSource().getTitle().equals(BashRunner.CommandType.TRIM.toString())) {
                i++;
                if(i == _trimmedAudio.size()) {
                    try {
                        concatenate();
                    } catch (URISyntaxException | IOException e) {
                        e.printStackTrace();
                        _finalHandler.handle(event);
                    }
                } else {
                    try {
                        trimAFile();
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                        _finalHandler.handle(event);
                    }
                }
            } else if(event.getSource().getTitle().equals(BashRunner.CommandType.CONCAT.toString())) {
                try {
                    for(Path file: _trimmedAudio) {
                        Files.deleteIfExists(file);
                    }
                    Files.deleteIfExists(Paths.get("./tmpList.txt"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("done processing");
                _name.setDoneProcessing();
                _finalHandler.handle(event);
            }
        } else if(event.getEventType().equals(WorkerStateEvent.WORKER_STATE_FAILED)) {
            _finalHandler.handle(event);
        }
    }
}
