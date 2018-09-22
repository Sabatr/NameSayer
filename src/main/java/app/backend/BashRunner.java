package app.backend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

public class BashRunner {

    public enum CommandType {
        RECORDAUDIO, PLAYAUDIO
    }

    private FSWrapper fsMan;
    private EventHandler<WorkerStateEvent> taskCompletionHandler;
    private Task<?> currentTask;

    public BashRunner(EventHandler<WorkerStateEvent> handler, FSWrapper fsMan) {
        taskCompletionHandler = handler;
        fsMan = fsMan;
    }

    public Task<String> runRecordCommand(Path path) {
        String cmd = String.format("arecord -f cd -d 5 -q \"%s/audio.wav\"", path.toAbsolutePath().toString());

        return runCommand(cmd, CommandType.RECORDAUDIO.toString());
    }

    public Task<String> runPlayAudioCommand(Path path) {
        String cmd = String.format("ffplay -nodisp -autoexit \"$s/audio.wav\"", path.toAbsolutePath().toString()); // -loglevel quiet

        return runCommand(cmd, CommandType.PLAYAUDIO.toString());
    }

    private Task<String> runCommand(String cmd, String commandType) {
        BashCommand runProcess = new BashCommand(cmd);

        runProcess.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, taskCompletionHandler);
        runProcess.addEventHandler(WorkerStateEvent.WORKER_STATE_FAILED, taskCompletionHandler);
        runProcess.setTitle(commandType);
        currentTask = runProcess;
        runProcess.run();
        return runProcess;
    }

    public void cancel() {
        if(currentTask != null) {
            currentTask.cancel();
        }
    }

    private class BashCommand extends Task<String> {
        String cmd;

        public BashCommand(String command) {
            cmd = command;
        }

        @Override
        public String call() {
            StringBuilder commandOutBuilder = new StringBuilder();
            boolean failure = false;

            Process p = null;
            try {
                p = new ProcessBuilder("/bin/bash", "-c", cmd).start();
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
