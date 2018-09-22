package app.tools;

import app.controllers.PracticeController;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;

public class ProgressTracker extends Task<Void> {
    private ProgressBar _progressBar;
    private HBox _recordingHBox;
    private HBox _confirmationHBox;
    private ComboBox _dropdown;
    private Button _audioButton;
    public ProgressTracker(ProgressBar progressBar, HBox recordingHBox, HBox confirmationHBox, ComboBox dropdown, Button audioButton) {
        _progressBar = progressBar;
        _recordingHBox = recordingHBox;
        _confirmationHBox = confirmationHBox;
        _dropdown = dropdown;
        _audioButton = audioButton;
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
            Thread.sleep(50);
        }
        return null;
    }

    @Override
    protected void done() {
        _progressBar.setProgress(0);
        _progressBar.setVisible(false);
        _confirmationHBox.setVisible(true);
        _recordingHBox.setVisible(false);
        _audioButton.setDisable(false);
        _dropdown.setDisable(false);
    }
}
