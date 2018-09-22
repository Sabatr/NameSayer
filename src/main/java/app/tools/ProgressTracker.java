package app.tools;

import javafx.concurrent.Task;
import javafx.scene.control.ProgressBar;

public class ProgressTracker extends Task<Void> {
    private ProgressBar _progressBar;
    public ProgressTracker(ProgressBar progressBar) {
        _progressBar = progressBar;
    }

    /**
     * Should somehow get the length of the recording and calculate the rate using this.
     * @return
     * @throws Exception
     */
    @Override
    protected Void call() throws Exception {
        double progress=0.01;
        while (_progressBar.getProgress() <= 1) {
            _progressBar.setProgress(progress);
            progress+=0.01;
            Thread.sleep(100);
        }
        return null;
    }

    @Override
    protected void done() {
        _progressBar.setProgress(0);
        _progressBar.setVisible(false);
    }
}
