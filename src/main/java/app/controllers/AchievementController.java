package app.controllers;

import app.tools.AchievementsManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.util.HashMap;
import java.util.Map;


public class AchievementController{
    @FXML private ProgressBar _practiceOneBar;
    @FXML private Label _practiceOneLabel;
    @FXML private ProgressBar _practiceTenBar;
    @FXML private Label _practiceTenLabel;
    private enum Achievements{
        PRACTICE_ONE,PRACTICE_TWO;
    }
    private Map<Achievements,Boolean> _checkIfComplete;
    public void initialize() {
        _checkIfComplete = new HashMap<>();
        _checkIfComplete.put(Achievements.PRACTICE_ONE,false);
        _checkIfComplete.put(Achievements.PRACTICE_TWO,false);
    }


    public void update() {
        if (!_checkIfComplete.get(Achievements.PRACTICE_ONE).booleanValue()) {
            checkPracticeAchievementOne();
        }
        if (!_checkIfComplete.get(Achievements.PRACTICE_TWO).booleanValue()) {
            checkPracticeAchievementTen();
        }
    }

    private void checkPracticeAchievementOne() {
        int practice = AchievementsManager.getInstance().getPracticeCounter();
        if (practice >= 1) {
            _practiceOneLabel.setText("COMPLETED!");
            _practiceOneBar.setProgress(1);
            _checkIfComplete.put(Achievements.PRACTICE_ONE,true);
        }
    }

    private void checkPracticeAchievementTen() {
        int practice = AchievementsManager.getInstance().getPracticeCounter();
        double value =0.1*practice;
        if (practice >= 10) {
            _practiceTenLabel.setText("COMPLETED!");
            _checkIfComplete.put(Achievements.PRACTICE_TWO,true);
        } else {
            _practiceTenLabel.setText(practice + "/10");
        }
        _practiceTenBar.setProgress(value);
    }


}
