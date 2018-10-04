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


    public void updatePracticeProgress(ProgressBar progress, Label progressLabel,String achievement) {
        switch (achievement) {
            case "onePractice":
                if (_practiceCounter>=1) {
                    progress.setProgress(1);
                    progressLabel.setText("COMPLETED");
                }
                break;
            case "tenPractice":

                if (_practiceCounter>=10) {
                    progress.setProgress(1);
                    progressLabel.setText("COMPLETED");
                } else  {
                    progress.setProgress((double)_practiceCounter/10);
                    progressLabel.setText(_practiceCounter + "/10");
                }
                break;
        }

    }
}
