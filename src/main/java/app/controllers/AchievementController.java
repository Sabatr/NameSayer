package app.controllers;

import app.tools.AchievementsManager;
import app.tools.HelpHandler;
import app.tools.MicPaneHandler;
import com.jfoenix.controls.JFXButton;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

/**
 * This class controls the components of the achievements view.
 * This is viewed in the Options view.
 *
 * @author Brian Nguyen
 */
public class AchievementController extends  ParentController{
    @FXML private ProgressBar _practiceOneBar;
    @FXML private Label _practiceOneLabel;
    @FXML private ProgressBar _practiceTenBar;
    @FXML private Label _practiceTenLabel;
    @FXML private ProgressBar _listenFiveNamesBar;
    @FXML private Label _listenFiveNames;
    @FXML private ProgressBar _listenTwentyBar;
    @FXML private Label _listenTwentyLabel;
    @FXML private ProgressBar _differentTenNamesBar;
    @FXML private Label _differentTenNamesLabel;
    @FXML private ProgressBar _differentFiftyNamesBar;
    @FXML private Label _differentFiftyNamesLabel;
    @FXML private ProgressBar _importPracticeBar;
    @FXML private Label _importPracticeLabel;
    @FXML private ProgressBar _randomBar;
    @FXML private Label _randomLabel;
    @FXML private JFXButton _helpButton;
    @FXML private JFXButton _micButton;
    @FXML private JFXButton _achievementsButton;
    private final int FIFTY_ACHIEVEMENTS = 50;
    private final int TWENTY_ACHIEVEMENT = 20;
    private final int TEN_ACHIEVEMENT = 10;
    private final  int FIVE_ACHIEVEMENT = 5;
    private final int ONE_ACHIEVEMENT = 1;
    private final String COMPLETED = "COMPLETED!";

    @Override
    public void switchTo() {
        update();
    }

    /**
     * Initially set the map with not completed achievements.
     */
    public void initialize() {
        _achievementsButton.setDisable(true);
    }

    /**
     * Prevents the achievements part to keep on changing once it's completed.
     */
    public void update() {
        checkIfRandom();
        checkPracticeAchievementOne();
        checkPracticeAchievementTen();
        checkListenAchievementFive();
        checkListenAchievementTwenty();
        checkDifferentAttemptsAchievementTen();
        checkDifferentAttemptsAchievementFifty();
        checkIfImported();
    }

    /**
     * Checks if the achievement has been completed yet.
     * Updates the practice achievement accordingly.
     */
    private void checkPracticeAchievementOne() {
        int practice = AchievementsManager.getInstance().getPracticeCounter();
        if (practice >= ONE_ACHIEVEMENT) {
            _practiceOneLabel.setText(COMPLETED);
            _practiceOneBar.setProgress(ONE_ACHIEVEMENT);

        }
    }

    /**
     * Checks if the user has completed ten recordings.
     * Update the component accordingly.
     */
    private void checkPracticeAchievementTen() {
        int practice = AchievementsManager.getInstance().getPracticeCounter();
        double value =(ONE_ACHIEVEMENT/(double)TEN_ACHIEVEMENT)*practice;
        if (practice >= TEN_ACHIEVEMENT) {
            _practiceTenLabel.setText(COMPLETED);
        } else {
            _practiceTenLabel.setText(practice + "/"+TEN_ACHIEVEMENT);
        }
        _practiceTenBar.setProgress(value);
    }

    /**
     * Checks if the user has completed listening to five recordings.
     * Update the component accordingly.
     */
    private void checkListenAchievementFive() {
        int listen = AchievementsManager.getInstance().getListenCounter();
        double value = (ONE_ACHIEVEMENT/(double)FIVE_ACHIEVEMENT)*listen;
        if (listen >= FIVE_ACHIEVEMENT) {
            _listenFiveNames.setText(COMPLETED);

        } else {
            _listenFiveNames.setText(listen + "/"+FIVE_ACHIEVEMENT);
        }
        _listenFiveNamesBar.setProgress(value);
    }

    /**
     * Checks if the user has completed listening to twenty recordings.
     * Update the component accordingly.
     */
    private void checkListenAchievementTwenty() {
        int listen = AchievementsManager.getInstance().getListenCounter();
        double value = (ONE_ACHIEVEMENT/(double)TWENTY_ACHIEVEMENT)*listen;
        if (listen >= TWENTY_ACHIEVEMENT) {
            _listenTwentyLabel.setText(COMPLETED);
        } else {
            _listenTwentyLabel.setText(listen +"/" + TWENTY_ACHIEVEMENT);
        }
        _listenTwentyBar.setProgress(value);
    }

    /**
     * Checks if the user has completed practicing with ten unique names.
     * Update the component accordingly.
     */
    private void checkDifferentAttemptsAchievementTen() {
        int differentNames = AchievementsManager.getInstance().getDifferentNameAttempts();
        double value =(ONE_ACHIEVEMENT/(double)TEN_ACHIEVEMENT)*differentNames;
        if (differentNames >= TEN_ACHIEVEMENT) {
            _differentTenNamesLabel.setText(COMPLETED);
        } else {
            _differentTenNamesLabel.setText(differentNames + "/" + TEN_ACHIEVEMENT);
        }
        _differentTenNamesBar.setProgress(value);
    }

    /**
     * Checks if the user has completed practicing with fifty unique names.
     * Update the component accordingly.
     */
    private void checkDifferentAttemptsAchievementFifty() {
        int differentNames = AchievementsManager.getInstance().getDifferentNameAttempts();
        double value =(ONE_ACHIEVEMENT/(double)FIFTY_ACHIEVEMENTS)*differentNames;
        if (differentNames >= FIFTY_ACHIEVEMENTS) {
            _differentFiftyNamesLabel.setText(COMPLETED);
        } else {
            _differentFiftyNamesLabel.setText(differentNames + "/" + FIFTY_ACHIEVEMENTS);
        }
        _differentFiftyNamesBar.setProgress(value);
    }

    /**
     * Checks if the user has imported a text file with stuff in it.
     * Update the component accordingly.
     */
    private void checkIfImported() {
        boolean checkImported = AchievementsManager.getInstance().getImported();
        if (checkImported) {
            _importPracticeBar.setProgress(ONE_ACHIEVEMENT);
            _importPracticeLabel.setText(COMPLETED);
        }
    }

    /**
     * Checks if the user has randomised their list.
     * Update the component accordingly.
     */
    private void checkIfRandom() {
        boolean checkRandom = AchievementsManager.getInstance().getRandom();
        if (checkRandom) {
            _randomBar.setProgress(ONE_ACHIEVEMENT);
            _randomLabel.setText(COMPLETED);
        }
    }

    @FXML
    private void goBack() {
        _switcher.switchScene(AchievementsManager.getInstance().getMenu());
    }

    @FXML
    private void help() {
        new HelpHandler(_helpButton,"achievements");
    }

    @FXML
    private void getMic() {
        MicPaneHandler.getHandler().show(_micButton);
    }

}
