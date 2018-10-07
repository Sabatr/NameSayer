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
    @FXML private ProgressBar _listenFiveNamesBar;
    @FXML private Label _listenFiveNames;
    private enum Achievements{
        PRACTICE_ONE, PRACTICE_TEN,LISTEN
    }
    private Map<Achievements,Boolean> _checkIfComplete;
    public void initialize() {
        _checkIfComplete = new HashMap<>();
        _checkIfComplete.put(Achievements.PRACTICE_ONE,false);
        _checkIfComplete.put(Achievements.PRACTICE_TEN,false);
        _checkIfComplete.put(Achievements.LISTEN,false);
    }


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
            _checkIfComplete.put(Achievements.PRACTICE_TEN,true);
        } else {
            _practiceTenLabel.setText(practice + "/10");
        }
        _practiceTenBar.setProgress(value);
    }

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


}
