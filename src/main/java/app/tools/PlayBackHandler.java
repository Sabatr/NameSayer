package app.tools;

import app.views.SceneBuilder;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXPopup;

/**
 * This class handles the list view when the user hovers over the replay button
 *
 * @author Brian Nguyen
 */
public class PlayBackHandler {
    private JFXPopup _popUp;
    public enum SoundStates{DATABASE,USER,BOTH}
    private SoundStates _currentState;
    public PlayBackHandler() {
        setUp();
    }

    /**
     * Creates the pop up and the list view.
     */
    private void setUp() {
        JFXListView<SoundStates> list = new JFXListView<>();
        list.getItems().addAll(SoundStates.DATABASE,SoundStates.USER,SoundStates.BOTH);
        list.setOnMouseClicked((e) -> {
            _currentState = list.getSelectionModel().getSelectedItem();
            _popUp.hide();
        });
        list.getStylesheets().add(
                SceneBuilder.class.getResource("styles/Rating.css").toExternalForm()
        );
        list.getStyleClass().add("list");
        _popUp = new JFXPopup(list);
        _currentState = SoundStates.USER;
    }

    /**
     * @return the cell that was pressed
     */
    public SoundStates getCurrent() {
        return _currentState;
    }

    public JFXPopup create() {
        return _popUp;
    }
}
