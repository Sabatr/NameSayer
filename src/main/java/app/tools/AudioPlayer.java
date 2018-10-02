package app.tools;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.*;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ProgressBar;


/**
 * Parts of this class were taken from https://stackoverflow.com/questions/2416935/how-to-play-wav-files-with-java
 */
public class AudioPlayer extends Task<Void> {
    private final int BUFFER_SIZE = 128000;
    private File _soundFile;
    private AudioInputStream _audioStream;
    private AudioFormat _audioFormat;
    private SourceDataLine _sourceLine;

    public AudioPlayer(File soundFile, EventHandler<WorkerStateEvent> handler, String taskTitle) {
        _soundFile = soundFile;
        this.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, handler);
        this.updateTitle(taskTitle);
    }

    /**
     * Call {@link AudioPlayer#playSound(File)} on a background thread
     */
    @Override
    protected Void call() throws Exception {
        playSound(_soundFile);
        return null;
    }

    /**
     * Get the length of the audio in the file belonging to this AudioPlayer
     */
    public float getLength() {
        float durationInSeconds = 0;
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(_soundFile);
            AudioFormat format = audioInputStream.getFormat();
            long audioFileLength = _soundFile.length();
            int frameSize = format.getFrameSize();
            float frameRate = format.getFrameRate();
            durationInSeconds = (audioFileLength / (frameSize * frameRate));
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return durationInSeconds;
    }

    /**
     * This is the main body of code taken from StackExchange.
     * It manually spoon-feeds the audio to the Java sound system.
     *
     * Obviously it is a blocking method, so it gets called on a background thread.
     * @param resourceToPlay The file to be played
     */
    private void playSound(File resourceToPlay) {
        try {
            _audioStream = AudioSystem.getAudioInputStream(_soundFile);
        } catch (Exception e){
            e.printStackTrace();
        }

        _audioFormat = _audioStream.getFormat();

        DataLine.Info info = new DataLine.Info(SourceDataLine.class, _audioFormat);
        try {
            _sourceLine = (SourceDataLine) AudioSystem.getLine(info);
            _sourceLine.open(_audioFormat);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        _sourceLine.start();

        long totalBytes = 0;
        int bytesRead = 0;
        byte[] data = new byte[BUFFER_SIZE];
        while (bytesRead != -1) {
            try {
                bytesRead = _audioStream.read(data, 0, data.length);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (bytesRead >= 0) {
                @SuppressWarnings("unused")
                int nBytesWritten = _sourceLine.write(data, 0, bytesRead);
                totalBytes += nBytesWritten;
            }
        }

        _sourceLine.drain();
        _sourceLine.close();
    }
}
