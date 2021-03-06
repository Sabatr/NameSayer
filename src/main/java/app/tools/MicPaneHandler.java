package app.tools;

import app.backend.BashRunner;
import app.views.SceneBuilder;
import com.jfoenix.controls.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class allows the user to use the access the microphone options anywhere in the application.
 *
 * @author Brian Nguyen & Marc Burgess
 */
public class MicPaneHandler implements EventHandler<WorkerStateEvent> {
    private static Node _node;
    private boolean _micToggled;
    private Map<String, String> _devices;
    private JFXComboBox<String> _deviceBox;
    private JFXProgressBar _levelIndicator;
    private JFXButton _testButton;
    private JFXButton _confirmButton;
    private HBox _hbox;
    private JFXPopup _popup;
    private static MicPaneHandler _handler = new MicPaneHandler();
    private SimpleStringProperty _selectedDevice;
    private boolean onWindows = false;

    /**
     * A private constructor prevents further instantiation.
     */
    private MicPaneHandler() {
        _selectedDevice = new SimpleStringProperty();
        setUpTestButton();

        if(System.getProperty("os.name").toLowerCase().contains("windows")) {
            setUpDeviceBox();
            onWindows = true;
        }
        setUpLevelIndicator();

        if(onWindows) {
            try {
                findMicDevices();
            } catch (URISyntaxException e) {
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setContentText("Error fetching input devices");
                a.showAndWait();
                //e.printStackTrace();
                return;
            }
        }
    }

    /**
     * Displays the microphone pane at a position.
     * @param node : the position of the microphone to be relative to the node
     */
    public void show(Node node) {
        _node = node;
        create();
    }

    /**
     * This allows the single instance to be accessed.
     * @return this handler.
     */
    public static MicPaneHandler getHandler() {
        return _handler;
    }

    /**
     * @return the current selected device.
     */
    public SimpleStringProperty getSelectedDevice() {
        return _selectedDevice;
    }

    /**
     * Sets up the button for testing the level of the microphone.
     */
    private void setUpTestButton() {
        _confirmButton = new JFXButton("CONFIRM");
        _testButton = new JFXButton("TEST MIC");
        _confirmButton.setOnMouseClicked((e) -> {
            if (_popup != null) {
                _popup.hide();
            }
        });
        _testButton.setOnMouseClicked((e) -> {
            if (onWindows && _deviceBox.getSelectionModel().getSelectedItem() == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setHeaderText(null);
                alert.setContentText("No Mic Detected");
                alert.setTitle("No Microphone");
                alert.showAndWait();
                return;
            }
            if (_micToggled) {
                _micToggled = false;
            } else {
                _micToggled = true;
                BashRunner runner = null;
                try {
                    runner = new BashRunner(this);
                } catch (URISyntaxException e1) {
                    //e1.printStackTrace();
                }
                runner.runMonitorMicCommand();
            }
        });
        _testButton.getStylesheets().add(
                SceneBuilder.class.getResource("styles/MicView.css").toExternalForm()
        );
        _testButton.getStyleClass().add("button");
        _confirmButton.getStylesheets().add(
                SceneBuilder.class.getResource("styles/MicView.css").toExternalForm()
        );
        _confirmButton.getStyleClass().add("button");
        _hbox = new HBox(_testButton, _confirmButton);
        _hbox.setLayoutX(105);
        _hbox.setLayoutY(400);
    }

    /**
     * A combo box that displays the available input devices.
     */
    private void setUpDeviceBox() {
        _deviceBox = new JFXComboBox<>();
        _deviceBox.setPrefWidth(300);
        _deviceBox.setLayoutX(250-150);
        _deviceBox.setLayoutY(250);
        _deviceBox.setOnAction((event) -> {
            _selectedDevice.set(_devices.get(_deviceBox.getValue()));
        });
        _deviceBox.getStylesheets().add(
                SceneBuilder.class.getResource("styles/MicView.css").toExternalForm()
        );
        _deviceBox.getStyleClass().add("box");

    }

    /**
     * Sets the up a progress bar to detect the levels
     */
    private void setUpLevelIndicator() {
        _levelIndicator = new JFXProgressBar();
        _levelIndicator.setProgress(0);
        _levelIndicator.setPrefHeight(40);
        _levelIndicator.setLayoutX(100);
        _levelIndicator.setLayoutY(100);
        _levelIndicator.getStylesheets().add(
                SceneBuilder.class.getResource("styles/MicView.css").toExternalForm()
        );
        _levelIndicator.getStyleClass().add("level");
    }

    /**
     * Creates the whole pane, which the object is shown on.
     */
    private void create() {
        Pane pane = new Pane();
        pane.setPrefHeight(500);
        pane.setPrefWidth(500);
        if(onWindows) {
            pane.getChildren().addAll(_hbox, _deviceBox, _levelIndicator);
        } else {
            pane.getChildren().addAll(_hbox, _levelIndicator);
        }
        pane.getStylesheets().add(
                SceneBuilder.class.getResource("styles/MicView.css").toExternalForm()
        );
        pane.getStyleClass().add("background");
        _popup = new JFXPopup(pane);
        _popup.setOnHidden((e)->{
                _micToggled = false;
        });
        _popup.show(_node,JFXPopup.PopupVPosition.TOP,JFXPopup.PopupHPosition.LEFT,0,-500);
    }

    /**
     * This updates the progress bar based on the level of the microphone
     * @param event
     */
    @Override
    public void handle(WorkerStateEvent event) {
        if(event.getEventType().equals(WorkerStateEvent.WORKER_STATE_SUCCEEDED)) {
            if(event.getSource().getTitle().equals(BashRunner.CommandType.TESTMIC.toString())) {
                if(!_micToggled) {
                    _levelIndicator.progressProperty().unbind();
                    _levelIndicator.progressProperty().setValue(0);
                    return;
                }
                String output = (String) event.getSource().getValue();
                int foreIndex = output.indexOf("mean_volume") + 13;
                int aftIndex = output.indexOf("dB", foreIndex);
                if(!(foreIndex < 0 || aftIndex < 0)) {
                    double volume;
                    try {
                        volume = Double.parseDouble(output.substring(foreIndex, aftIndex - 1));
                    } catch (NumberFormatException e) {
                        volume = -90;
                    }
                    double progress = (100 + 1.3 * volume) / 100;
                    _levelIndicator.setProgress(progress);
                }
                BashRunner runner = null;
                try {
                    runner = new BashRunner(this);
                } catch (URISyntaxException e) {
                    //e.printStackTrace();
                }
                runner.runMonitorMicCommand();
            } else if(event.getSource().getTitle().equals(BashRunner.CommandType.LISTDEVICES.toString())) {
                parseDevices((String) event.getSource().getValue());
            }
        } else if(event.getEventType().equals(WorkerStateEvent.WORKER_STATE_FAILED)) {
            //System.out.println("Failed");
            _levelIndicator.progressProperty().unbind();
            _levelIndicator.progressProperty().setValue(0);
            _micToggled = false;
        }
    }

    /**
     * Sets the input devices to the combobox.
     * @param ffmpegOut
     */
    private void parseDevices(String ffmpegOut) {
        boolean pastAudioHeader = false;
        String[] lines = ffmpegOut.split("\n");
        for(int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if(line.contains("DirectShow audio devices")) {
                pastAudioHeader = true;
                continue;
            }

            if(pastAudioHeader && line.contains("Alternative name")) {
                String lineBefore = lines[i - 1];
                int firstQuote = lineBefore.indexOf("\"");
                int secondQuote = lineBefore.indexOf("\"", firstQuote + 1);
                String name = lineBefore.substring(firstQuote + 1, secondQuote);

                firstQuote = line.indexOf("\"");
                secondQuote = line.indexOf("\"", firstQuote + 1);
                String altName = line.substring(firstQuote + 1, secondQuote);

                _devices.put(name, altName);
            }
        }
        _deviceBox.setItems(FXCollections.observableArrayList(_devices.keySet()));
    }

    /**
     * If the device list hasn't already been populated, it attempts to scan the devices available to ffmpeg.
     */
    private void findMicDevices() throws URISyntaxException {
        if(_devices != null) {
            return;
        }
        _devices = new HashMap<>();
        BashRunner br = new BashRunner(this);
        br.runDeviceList();
    }
}
