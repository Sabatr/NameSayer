package app.tools;

import app.backend.NameEntry;
import app.views.SceneBuilder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import java.awt.*;
import java.io.IOException;

/**
 * This class managers and stores most of the achievement information.
 * This class also uses the singleton pattern as only one instance of the achievement manager is needed.
 *
 * @author Brian Nguyen
 */
public class AchievementsManager {
    private final int _firstPractice = 1;
    private final int _tenPractices = 10;
    private final int _listenAttempts = 5;
    private static AchievementsManager _achievementsManager = new AchievementsManager();
    private int _practiceCounter;
    private int _listenCounter;
    private int _differentNameCounter;
    private boolean _completed = false;
    private ObservableList<NameEntry> _names;

    /**
     * Private constructor, prevents further instantiation.
     */
    private AchievementsManager() {
        _names = FXCollections.observableArrayList();
        _practiceCounter  = 0;
        _listenCounter = 0;
    }

    /**
     *
     * @return the achievement instance.
     */
    public static AchievementsManager getInstance() {
        return _achievementsManager;
    }

    /**
     * Increases the number of times the user has recorded.
     */
    public void increasePracticeAttempts() {
        _practiceCounter++;
        switch(_practiceCounter) {
            case _firstPractice:
                showFirstPracticeAchievement();
                break;
            case _tenPractices:
                showTenPracticeAchievement();
        }
    }

    /**
     * Increases the number of times the user has listened
     */
    public void increaseListenAttempts() {
        _listenCounter++;
        if (_listenCounter == _listenAttempts) {
            showFiveListenAchievement();
        }
    }

    /**
     * Increases the number of times the user has practiced with a
     * different name.
     * @param value
     */
    private void increaseDifferentNameAttempts(int value) {
        _differentNameCounter = value;
        if (_differentNameCounter >= _tenPractices && !_completed) {
            showTenDifferentNamesAchievement();
            _completed = true;
        }
    }

    /**
     * Retrieves the practice list. This is done to determine if
     * the user has practiced with this name already.
     * @param names
     */
    public void getPracticeNames(ObservableList<NameEntry> names) {
        boolean exists = false;
        for (NameEntry entry : names) {
            for (NameEntry differentName : _names) {
                if (entry.compareTo(differentName) == 0) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                _names.add(entry);
            }
            exists = false;
        }
        increaseDifferentNameAttempts(_names.size());
    }

    /**
     * Creates the pop up of the achievement.
     * @param text
     */
    private void popUp(String text) {
        ShowAchievementPopUp popUp = new ShowAchievementPopUp("Achievement Unlocked: " + text);
        popUp.setOnSucceeded(event -> {
            popUp._alert.close();
        });
        new Thread(popUp).start();
    }

    private void showFirstPracticeAchievement() {
        popUp("First practice attempt!");
    }

    private void showTenPracticeAchievement() {
        popUp("Ten practice attempts!");
    }

    private void showFiveListenAchievement() {
        popUp("Five listens!");
    }

    private void showTenDifferentNamesAchievement() {
        popUp("Ten different names!");
    }

    /**
     *
     * @return the number of times the user has recorded
     */
    public int getPracticeCounter() {
        return _practiceCounter;
    }

    /**
     *
     * @return the number of times the user has listened
     */
    public int getListenCounter() {
        return _listenCounter;
    }

    /**
     *
     * @return the number of times the user has practiced with different names
     */
    public int getDifferentNameAttempts() {
        return _differentNameCounter;
    }

    /**
     * A private class is used here to allow the achievement to be displayed in the background
     * and not cause any lag when it is displayed.
     */
    private class ShowAchievementPopUp extends Task<Void> {
        private String _showText;
        private Alert _alert;

        public ShowAchievementPopUp(String showText) {
            _showText = showText;
            createAlert();
        }

        /**
         * This method creates the alert itself.
         */
        private void createAlert() {
            try {
                _alert = new Alert( Alert.AlertType.INFORMATION );
                ButtonType button = new ButtonType("");
                //Allows us to apply css to the alert
                DialogPane dialogPane = _alert.getDialogPane();
                dialogPane.getStylesheets().add(
                        SceneBuilder.class.getResource("styles/Achievement.css").toExternalForm());
                dialogPane.getStyleClass().add("achievementPopUp");
                _alert.initOwner(SceneBuilder.inst(null,null).getStage());
                _alert.setTitle( "Achievement Unlocked!" );
                _alert.setHeaderText(null);
                _alert.setContentText(_showText);
                //Essentially allows the user to still click on the application when this is shown.
                _alert.initModality( Modality.NONE );
                _alert.getButtonTypes().setAll(button);
                _alert.setY(20);
                //Removes the top part of the dialog
                _alert.initStyle(StageStyle.TRANSPARENT);
                //Give it a cool icon
                Image image = new Image(SceneBuilder.class.getResource("images/award.png").toExternalForm());
                ImageView imageView = new ImageView(image);
                _alert.setGraphic(imageView);
                //Hard coded because the application doesn't know the size of the alert until it is shown.
                int widthOfAlert = 427;
                //Centralizes the pop up for any screen.
                _alert.setX((Toolkit.getDefaultToolkit().getScreenSize().getWidth()-widthOfAlert)/2);
                _alert.show();
            } catch (IOException exception) {
                exception.printStackTrace();
            }

        }

        /**
         * Keeps the pop up present for 2 seconds.
         * @return
         * @throws InterruptedException
         */
        @Override
        protected Void call() throws InterruptedException {
            for (int second = 0; second<2;second++) {
                Thread.sleep(1000);
            }
            return null;
        }
    }
}
