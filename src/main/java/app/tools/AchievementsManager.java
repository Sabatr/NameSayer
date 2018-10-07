package app.tools;

import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public class AchievementsManager {
    private final int _firstPractice = 1;
    private final int _tenPractices = 10;
    private final int _listenAttempts = 5;
    private static AchievementsManager _achievementsManager = new AchievementsManager();
    private int _practiceCounter;
    private int _listenCounter;

    private AchievementsManager() {
        _practiceCounter  = 0;
        _listenCounter = 0;
    }
    public static AchievementsManager getInstance() {
        return _achievementsManager;
    }

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

    public void increaseListenAttempts() {
        _listenCounter++;
        if (_listenCounter == _listenAttempts) {
            showFiveListenAchievement();
        }
    }

    private void showFirstPracticeAchievement() {
        System.out.println("First achievement");
    }

    private void showTenPracticeAchievement() {
        System.out.println("Ten practices!");
    }

    private void showFiveListenAchievement() {
        System.out.println("Listened to five");
    }

    public int getPracticeCounter() {
        return _practiceCounter;
    }

    public int getListenCounter() {
        return _listenCounter;
    }


}
