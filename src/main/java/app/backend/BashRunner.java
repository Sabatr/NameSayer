package app.backend;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

/**
 * This class runs processes externally. Its operation depends on NameSayer running on Linux.
 * All standard output from the {@link Process}es is passed to the value of the {@link Task}s used to run them.
 * {@link EventHandler}s may use this to get info from the program being run.
 */
public class BashRunner {

    public enum CommandType {
        RECORDAUDIO, PLAYAUDIO, TESTMIC, CONCAT
    }

    private EventHandler<WorkerStateEvent> _taskCompletionHandler;
    // private EventHandler<WorkerStateEvent> _externalHandler;
    private Task<?> _currentTask;

    /**
     * Construct the BashRunner
     * @param handler The EventHandler that the UI provides so that it can be notified about the completion
     *                of BashRunner processes.
     */
    public BashRunner(EventHandler<WorkerStateEvent> handler) {
        _taskCompletionHandler = handler;
    }

    /**
     * Runs ffmpeg, recording from the default hardware device through alsa.
     * @param path The filepath the audio should be recorded to
     * @return The {@link Task} running the process on a background thread
     */
    public Task<String> runRecordCommand(Path path) {
        // arecord -f cd -d 5 -q "%s/audio.wav"
        String cmdString = String.format("ffmpeg -f alsa -i  hw:0 -t 3 -acodec pcm_s16le -ar 48000 -ac 1 \"%s\"", path.toAbsolutePath().toString());

        String[] cmd = { "/bin/bash", "-c", cmdString };
        return runCommand(CommandType.RECORDAUDIO.toString(), cmd);
    }

    /**
     * Runs ffmpeg to record a very small snippet of audio. Statistics of the audio are printed to the standard out.
     * The EventHandler passed to this BashRunner can get the statistics and parse desired information, as all standard
     * out is put into the value of the {@link Task} that runs the {@link Process}.
     * @return The {@link Task} running the process on a background thread
     */
    public Task<String> runMonitorMicCommand() {

        return runCommand(CommandType.TESTMIC.toString(), "/bin/bash", "-c", "ffmpeg -f alsa -i hw:0 -t 0.03 -filter_complex \"volumedetect\" " +
                "-acodec pcm_s16le -ar 44000 -ac 1 -f null /dev/null");
    }

    /**
     * Runs ffplay to play audio. This doesn't work for some reason
     * @param path The filepath of the audio file to play
     * @return The Task running the process on a background thread
     */
    @Deprecated
    public Task<String> runPlayAudioCommand(Path path) {
        System.out.println("Playing audio " + path.toAbsolutePath().toString());
        String cmdString = String.format("ffplay -autoexit \"$s\"", path.toAbsolutePath().toString()); // -loglevel quiet

        String[] cmd = { "/bin/bash", "-c", cmdString };
        return runCommand(CommandType.PLAYAUDIO.toString(), cmd);
    }

    /**
     * Runs ffmpeg to concatenate several audio files and saves the result to the given output file.
     * @param inputs The list of input files to concatenate
     * @param output The filepath of the output file
     * @return The Task running the process on a background thread
     * @throws IOException If the creation of the temporary list file fails
     */
    public Task<String> runConcatCommands(List<Path> inputs, Path output) throws IOException {
        System.out.println("gets called");
        Path audioList = Paths.get("./tmpList.txt").toAbsolutePath();
        Files.deleteIfExists(audioList);
        Files.createFile(audioList);
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(audioList.toFile()))) {
            for(Path audioPath: inputs) {
                writer.write("file '" + audioPath.toAbsolutePath().toString() + "'\n");
            }
        }

        String cmdString = String.format("ffmpeg -f concat -safe 0 -i \"%s\" -c copy \"%s\"",
                audioList.toAbsolutePath().toString(), output.toAbsolutePath().toString());
        String[] cmd = { "/bin/bash", "-c", cmdString };
        return runCommand(CommandType.CONCAT.toString(), cmd);
    }

    /**
     * Generic method for calling methods
     * @param commandType The type of command that the {@link Task} gets flagged with. (The title is set to this parameter)
     * @param cmd The strings that make up the command
     * @return The {@link Task} being used to run the {@link Process}.
     */
    private Task<String> runCommand(String commandType, String... cmd) {
        BashCommand runProcess = new BashCommand(Paths.get("").toFile(), cmd);

        runProcess.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, _taskCompletionHandler);
        runProcess.addEventHandler(WorkerStateEvent.WORKER_STATE_FAILED, _taskCompletionHandler);
        runProcess.setTitle(commandType);
        _currentTask = runProcess;

        Thread thread = new Thread(runProcess);
        thread.start();
        return runProcess;
    }

    public void cancel() {
        if(_currentTask != null) {
            _currentTask.cancel();
        }
    }

    private class BashCommand extends Task<String> {
        String[] cmd;
        File dir;

        public BashCommand(File directory, String... command) {
            cmd = command;
            dir = directory;
        }

        @Override
        public String call() {
            StringBuilder commandOutBuilder = new StringBuilder();
            boolean failure = false;

            Process p = null;
            try {
                ProcessBuilder test = new ProcessBuilder(cmd);
                test.directory(null);
                p = test.start();
            } catch(IOException e) {
                failure = true;
                commandOutBuilder.append(e.getMessage());
            }

            if(!failure) {
                try {
                    p.waitFor();
                } catch(InterruptedException e) {
                    if (isCancelled()) {
                        updateMessage("Cancelled");
                        failure = true;
                        commandOutBuilder.append(e.getMessage());
                    }
                }
            }

            if(!failure) {
                try {
                    if(p.exitValue() == 0) {
                        commandOutBuilder.append(concatOutput(p.getInputStream(), "\n"));
                        commandOutBuilder.append(concatOutput(p.getErrorStream(), "\n"));
                    } else {
                        failure = true;
                        commandOutBuilder.append(concatOutput(p.getInputStream(),"\n"));
                        commandOutBuilder.append(concatOutput(p.getErrorStream(), "\n"));
                    }
                } catch(IOException e) {
                    failure = true;
                    commandOutBuilder.append(e.getMessage());
                }
            }

            if(failure == true) {
                commandOutBuilder.insert(0, "failure: ");
                updateValue(commandOutBuilder.toString());
                failed();
            }

            updateProgress(20, 20);
            updateValue(commandOutBuilder.toString());
            //System.out.println(commandOutBuilder.toString());
            return commandOutBuilder.toString();
        }

        private String concatOutput(InputStream stream, String delimiter) throws IOException {
            StringBuilder commandOutBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
                String str;
                while( (str = reader.readLine()) != null ) {
                    //System.out.println("output: " + str);
                    commandOutBuilder.append(str + delimiter);
                }
            } catch (IOException e) {
                throw e;
            }
            return commandOutBuilder.toString();
        }

        public void setTitle(String title) {
            this.updateTitle(title);
        }
    };
}
