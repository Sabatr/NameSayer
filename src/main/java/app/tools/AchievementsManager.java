package app.tools;

import app.backend.NameEntry;
import app.controllers.AchievementController;
import app.views.SceneBuilder;
import com.jfoenix.controls.JFXAlert;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXPopup;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

import javax.management.Notification;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class managers and stores most of the achievement information.
 * This class also uses the singleton pattern as only one instance of the achievement manager is needed.
 *
 * @author Brian Nguyen
 */
public class AchievementsManager {
    private final int _firstPractice = 1;
    private final int _tenPractices = 10;
    private final int _listenFiveAttempts = 5;
    private final int _listenTwentyAttempts = 20;
    private final int _fiftyAchievement = 50;
    private static AchievementsManager _achievementsManager = new AchievementsManager();
    private int _practiceCounter;
    private int _listenCounter;
    private int _differentNameCounter;
    private boolean _tenSuccess = false;
    private boolean _imported = false;
    private boolean _isRandom = false;
    private boolean _fiftySuccess = false;
    private ObservableList<NameEntry> _names;
    private String _statePressed;
    private Map<Achievements,Boolean> _checkIfComplete;

    public enum Achievements{
        PRACTICE_ONE, PRACTICE_TEN, LISTEN_FIVE,LISTEN_TWENTY,DIFFERENT_TEN,DIFFERENT_FIFTY,IMPORT_ONE,RANDOM
    }
    /**
     * Private constructor, prevents further instantiation.
     */
    private AchievementsManager() {
        setUp();
        _names = FXCollections.observableArrayList();
        _practiceCounter  = 0;
        _listenCounter = 0;
    }

    public Map<Achievements,Boolean> getCompleted() {
        return _checkIfComplete;
    }

    private void setUp(){
        _checkIfComplete = new HashMap<>();
        _checkIfComplete.put(Achievements.PRACTICE_ONE,false);
        _checkIfComplete.put(Achievements.PRACTICE_TEN,false);
        _checkIfComplete.put(Achievements.LISTEN_FIVE,false);
        _checkIfComplete.put(Achievements.DIFFERENT_TEN,false);
        _checkIfComplete.put(Achievements.IMPORT_ONE,false);
        _checkIfComplete.put(Achievements.RANDOM,false);
        _checkIfComplete.put(Achievements.LISTEN_TWENTY,false);
        _checkIfComplete.put(Achievements.DIFFERENT_FIFTY,false);
    }

    public void setMenu(String menu) {
        _statePressed = menu;
    }

    public String getMenu() {
        return _statePressed;
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
        if (!(_checkIfComplete.get(Achievements.PRACTICE_ONE) && _checkIfComplete.get(Achievements.PRACTICE_TEN))) {
            _practiceCounter++;
            switch(_practiceCounter) {
                case _firstPractice:
                    showFirstPracticeAchievement();
                    _checkIfComplete.put(Achievements.PRACTICE_ONE,true);
                    break;
                case _tenPractices:
                    showTenPracticeAchievement();
                    _checkIfComplete.put(Achievements.PRACTICE_TEN,true);
                    break;
            }
        }

    }

    /**
     * Increases the number of times the user has listened
     */
    public void increaseListenAttempts() {
        if (!(_checkIfComplete.get(Achievements.LISTEN_FIVE) && _checkIfComplete.get(Achievements.LISTEN_TWENTY))) {
            _listenCounter++;
            switch (_listenCounter) {
                case _listenFiveAttempts:
                    _checkIfComplete.put(Achievements.LISTEN_FIVE,true);
                    showFiveListenAchievement();
                    break;
                case _listenTwentyAttempts:
                    _checkIfComplete.put(Achievements.LISTEN_TWENTY,true);
                    showTwentyListenAchievement();
                    break;
            }
        }
    }

    /**
     * Increases the number of times the user has practiced with a
     * different name.
     * @param value
     */
    private void increaseDifferentNameAttempts(int value) {
        if (!(_checkIfComplete.get(Achievements.DIFFERENT_TEN) && _checkIfComplete.get(Achievements.DIFFERENT_FIFTY))) {
            _differentNameCounter = value;
            if (_differentNameCounter >= _tenPractices && !_tenSuccess) {
                _checkIfComplete.put(Achievements.DIFFERENT_TEN,true);
                showTenDifferentNamesAchievement();
                _tenSuccess = true;
            }
            if (_differentNameCounter >= _fiftyAchievement && !_fiftySuccess) {
                _checkIfComplete.put(Achievements.DIFFERENT_FIFTY,true);
                showFiftyDifferentNamesAchievement();
                _fiftySuccess = true;
            }
        }
    }

    /**
     * Lets the system know that the user has imported.
     * @param hasImport
     */
    public void hasImported(boolean hasImport) {
        if (!_checkIfComplete.get(Achievements.IMPORT_ONE)) {
            _imported = hasImport;
            if (_imported) {
                _checkIfComplete.put(Achievements.IMPORT_ONE,true);
                showImportAchievement();
            }
        }
    }

    /**
     * Lets the system know if the list has been randomised before
     * @param hasRandom
     */
    public void hasBeenRandomised(boolean hasRandom) {
        if (!_checkIfComplete.get(Achievements.RANDOM)) {
            _isRandom = hasRandom;
            if (_isRandom) {
                _checkIfComplete.put(Achievements.RANDOM,true);
                showRandomAchievement();
            }
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
    private void popUp(Achievements achievement) {
        new AchievementNotification(achievement);
    }

    private void showFirstPracticeAchievement() {
        popUp(Achievements.PRACTICE_ONE);
    }

    private void showTenPracticeAchievement() {
        popUp(Achievements.PRACTICE_TEN);
    }

    private void showFiveListenAchievement() {
        popUp(Achievements.LISTEN_FIVE);
    }

    private void showTwentyListenAchievement() {popUp(Achievements.LISTEN_TWENTY);}

    private void showTenDifferentNamesAchievement() { popUp(Achievements.DIFFERENT_TEN); }

    private void showFiftyDifferentNamesAchievement() {popUp(Achievements.DIFFERENT_FIFTY);}

    private void showImportAchievement() { popUp(Achievements.IMPORT_ONE);}

    private void showRandomAchievement() { popUp(Achievements.RANDOM);}


    /**
     * @return the number of times the user has recorded
     */
    public int getPracticeCounter() {
        return _practiceCounter;
    }

    /**
     * @return the number of times the user has listened
     */
    public int getListenCounter() {
        return _listenCounter;
    }

    /**
     * @return the number of times the user has practiced with different names
     */
    public int getDifferentNameAttempts() {
        return _differentNameCounter;
    }

    /**
     * @return if the user has imported something
     */
    public boolean getImported() {return _imported; }

    /**
     * @return if the user has randomised their list
     */
    public boolean getRandom() {return _isRandom; }

}
