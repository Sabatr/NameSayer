package app.tools;

import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public class AchievementsManager {
    private final int _firstPractice = 1;
    private final int _tenPractices = 10;
    private static AchievementsManager _achievementsManager = new AchievementsManager();
    private int _practiceCounter;

    private AchievementsManager() {
        _practiceCounter  = 0;
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

    private void showFirstPracticeAchievement() {
        System.out.println("First achievement");
    }

    private void showTenPracticeAchievement() {
        System.out.println("Ten practices!");
    }

    public int getPracticeCounter() {
        return _practiceCounter;
    }


}
