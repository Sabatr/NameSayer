package app.controllers;

import app.tools.AchievementsManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.util.HashMap;
import java.util.Map;

/**
 * This class controls the components of the achievements view.
 * This is viewed in the Options view.
 *
 * @author Brian Nguyen
 */
public class AchievementController{
    @FXML private ProgressBar _practiceOneBar;
    @FXML private Label _practiceOneLabel;
    @FXML private ProgressBar _practiceTenBar;
    @FXML private Label _practiceTenLabel;
    @FXML private ProgressBar _listenFiveNamesBar;
    @FXML private Label _listenFiveNames;
    @FXML private ProgressBar _differentNamesBar;
    @FXML private Label _differentNamesLabel;
    private enum Achievements{
        PRACTICE_ONE, PRACTICE_TEN,LISTEN,DIFFERENT_TEN
    }
    private Map<Achievements,Boolean> _checkIfComplete;
    public void initialize() {
        _checkIfComplete = new HashMap<>();
        _checkIfComplete.put(Achievements.PRACTICE_ONE,false);
        _checkIfComplete.put(Achievements.PRACTICE_TEN,false);
        _checkIfComplete.put(Achievements.LISTEN,false);
        _checkIfComplete.put(Achievements.DIFFERENT_TEN,false);
    }

    /**
     * Prevents the achievements part to keep on changing once it's completed.
     */
    public void update() {
        if (!_checkIfComplete.get(Achievements.PRACTICE_ONE).booleanValue()) {
            checkPracticeAchievementOne();
        }
        if (!_checkIfComplete.get(Achievements.PRACTICE_TEN).booleanValue()) {
            checkPracticeAchievementTen();
        }
        if (!_checkIfComplete.get(Achievements.LISTEN).booleanValue()) {
            checkListenAchievementFive();
        }
        if (!_checkIfComplete.get(Achievements.DIFFERENT_TEN).booleanValue()) {
            checkDifferentAttemptsAchievementTen();
        }
    }

    /**
     * Checks if the achievement has been completed yet.
     * Updates the practice achievement accordingly.
     */
    private void checkPracticeAchievementOne() {
        int practice = AchievementsManager.getInstance().getPracticeCounter();
        if (practice >= 1) {
            _practiceOneLabel.setText("COMPLETED!");
            _practiceOneBar.setProgress(1);
            _checkIfComplete.put(Achievements.PRACTICE_ONE,true);
        }
    }

    /**
     * Checks if the user has completed ten recordings.
     * Update the component accordingly.
     */
    private void checkPracticeAchievementTen() {
        int practice = AchievementsManager.getInstance().getPracticeCounter();
        double value =0.1*practice;
        if (practice >= 10) {
            _practiceTenLabel.setText("COMPLETED!");
            _checkIfComplete.put(Achievements.PRACTICE_TEN,true);
        } else {
            _practiceTenLabel.setText(practice + "/10");
        }
        _practiceTenBar.setProgress(value);
    }

    /**
     * Checks if the user has completed listening to five recordings.
     * Update the component accordingly.
     */
    private void checkListenAchievementFive() {
        int listen = AchievementsManager.getInstance().getListenCounter();
        double value = 0.2*listen;
        if (listen >= 5) {
            _listenFiveNames.setText("COMPLETED!");
            _checkIfComplete.put(Achievements.LISTEN,true);
        } else {
            _listenFiveNames.setText(listen + "/5");
        }
        _listenFiveNamesBar.setProgress(value);
    }

    /**
     * Checks if the user has completed practicing with five unique names.
     * Update the component accordingly.
     */
    private void checkDifferentAttemptsAchievementTen() {
        int differentNames = AchievementsManager.getInstance().getDifferentNameAttempts();
        double value = 0.1*differentNames;
        if (differentNames >= 10) {
            _differentNamesLabel.setText("COMPLETED!");
            _checkIfComplete.put(Achievements.DIFFERENT_TEN,true);
        } else {
            _differentNamesLabel.setText(differentNames + "/10");
        }
        _differentNamesBar.setProgress(value);
    }

}
