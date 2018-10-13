package app.tools;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ProgressBar;

public class Timer extends Task<Void> {
    private final float _timeInSeconds;

    public Timer(ProgressBar progressBar, EventHandler<WorkerStateEvent> handler, String title, float timeInSeconds) {
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
        int timeInCentiseconds = (int) (_timeInSeconds * 100);
        for(progress = 1; progress <= timeInCentiseconds; progress++) {
            updateProgress(progress, timeInCentiseconds);
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
