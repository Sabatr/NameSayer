package app.tools;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ProgressBar;

public class Timer extends Task<Void> {
    private final int _timeInSeconds;

    public Timer(ProgressBar progressBar, EventHandler<WorkerStateEvent> handler, String title, int timeInSeconds) {
        _timeInSeconds = timeInSeconds;
        this.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, handler);
        progressBar.progressProperty().bind(this.progressProperty());
        this.updateTitle(title);
    }

    /**
     * Should somehow get the length of the recording and calculate the rate using this.
     * @return
     * @throws Exception
     */
    @Override
    protected Void call() {
        int progress;
        for(progress = 1; progress <= _timeInSeconds * 100; progress++) {
            updateProgress(progress, _timeInSeconds * 100);
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                if(isCancelled()) {
                    updateMessage("cancelled");
                    break;
                }
            }
        }
        return null;
    }

    @Override
    protected void done() {
        succeeded();
    }
}
