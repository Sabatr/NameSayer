package app.backend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

/**
 * This class runs processes externally
 */
public class BashRunner {

    public enum CommandType {
        RECORDAUDIO, PLAYAUDIO, TESTMIC
    }

    private EventHandler<WorkerStateEvent> taskCompletionHandler;
    private Task<?> currentTask;

    public BashRunner(EventHandler<WorkerStateEvent> handler) {
        taskCompletionHandler = handler;
    }

    public Task<String> runRecordCommand(Path path) {
        // arecord -f cd -d 5 -q "%s/audio.wav"
        String cmdString = String.format("ffmpeg -f alsa -i  hw:0 -t 2 -acodec pcm_s16le -ar 48000 -ac 1 \"%s\"", path.toAbsolutePath().toString());

        String[] cmd = { "/bin/bash", "-c", cmdString };
        return runCommand(CommandType.RECORDAUDIO.toString(), cmd);
    }

    public Task<String> runMonitorMicCommand() {

        return runCommand(CommandType.TESTMIC.toString(), "");
    }

    public Task<String> runPlayAudioCommand(Path path) {
        System.out.println("Playing audio " + path.toAbsolutePath().toString());
        String cmdString = String.format("ffplay -autoexit \"$s\"", path.toAbsolutePath().toString()); // -loglevel quiet

        String[] cmd = { "/bin/bash", "-c", cmdString };
        return runCommand(CommandType.PLAYAUDIO.toString(), cmd);
    }

    private Task<String> runCommand(String commandType, String... cmd) {
        BashCommand runProcess = new BashCommand(cmd);

        runProcess.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, taskCompletionHandler);
        runProcess.addEventHandler(WorkerStateEvent.WORKER_STATE_FAILED, taskCompletionHandler);
        runProcess.setTitle(commandType);
        currentTask = runProcess;

        Thread thread = new Thread(runProcess);
        thread.start();
        return runProcess;
    }

    public void cancel() {
        if(currentTask != null) {
            currentTask.cancel();
        }
    }

    private class BashCommand extends Task<String> {
        String[] cmd;

        public BashCommand(String... command) {
            cmd = command;
        }

        @Override
        public String call() {
            StringBuilder commandOutBuilder = new StringBuilder();
            boolean failure = false;

            Process p = null;
            try {
                p = new ProcessBuilder(cmd).start();
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
                    } else {
                        failure = true;
                        commandOutBuilder.append(concatOutput(p.getInputStream(),"\n"));
                        commandOutBuilder.append(concatOutput(p.getErrorStream(), "\n"));
                        System.out.println(commandOutBuilder);
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
            System.out.println(commandOutBuilder.toString());
            return commandOutBuilder.toString();
        }

        private String concatOutput(InputStream stream, String delimiter) throws IOException {
            StringBuilder commandOutBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
                String str;
                while( (str = reader.readLine()) != null ) {
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
