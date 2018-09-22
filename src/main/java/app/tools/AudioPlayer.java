package app.tools;

import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;

public class AudioPlayer extends Task<Void> {
    private ProgressBar _progressBar;
    private Button _listenButton;
    private HBox _recordingBox;
    private HBox _confirmationHBox;
    private String _name;

    public AudioPlayer(ProgressBar progressBar, Button listenButton, HBox recordingBox,HBox confirmationHBox,String name) {
        _progressBar = progressBar;
        _listenButton = listenButton;
        _recordingBox = recordingBox;
        _confirmationHBox = confirmationHBox;
        _name = name;
    }
    @Override
    protected Void call() throws Exception {
        System.out.println("Playing: " + _name);
        _progressBar.setVisible(true);
        double progress=0.01;
        while (_progressBar.getProgress() <= 1) {
            _progressBar.setProgress(progress);
            progress+=0.02;
            Thread.sleep(90);
        }
        return null;
    }

    public void done() {
        _progressBar.setProgress(0);
        _progressBar.setVisible(false);
        _listenButton.setDisable(false);
        _recordingBox.setDisable(false);
        _confirmationHBox.setDisable(false);
    }
}
