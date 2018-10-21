package app.tools;

import app.views.SceneBuilder;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

/**
 * This class allows dynamic notifications to be shown to the user whenever they
 * unlock an achievement.
 *
 * @author Brian Ngyuyen
 */
public class AchievementNotification {
    private AchievementsManager.Achievements _achievement;
    public AchievementNotification(AchievementsManager.Achievements achievement) {
        _achievement = achievement;
        makeNotification();
    }

    /**
     * Chooses the achievement and displays it.
     */
    private void makeNotification() {
        Notifications notification = Notifications.create();
        notification.position(Pos.TOP_CENTER);
        notification.hideCloseButton();
        ImageView imageView = new ImageView();
        imageView.setFitHeight(100);
        imageView.setFitWidth(100);
        switch (_achievement) {
            case PRACTICE_ONE:
                imageView.setImage(new Image(SceneBuilder.class.getResource("images/second.png").toExternalForm()));
                notification.text("Congratulations! You have unlocked the achievement: Just The Beginning");
                notification.title("One Recording Achievement");
                break;
            case PRACTICE_TEN:
                imageView.setImage(new Image(SceneBuilder.class.getResource("images/first.png").toExternalForm()));
                notification.text("Congratulations! You have unlocked the achievement: Elite Spokesman");
                notification.title("Ten Recording Achievement");
                break;
            case LISTEN_FIVE:
                imageView.setImage(new Image(SceneBuilder.class.getResource("images/listen1.png").toExternalForm()));
                notification.text("Congratulations! You have unlocked the achievement: Opened Ears");
                notification.title("Listen One Achievement");
                break;
            case LISTEN_TWENTY:
                imageView.setImage(new Image(SceneBuilder.class.getResource("images/dumbo.png").toExternalForm()));
                notification.text("Congratulations! You have unlocked the achievement: Dumbo");
                notification.title("Listen Ten Achievement");
                break;
            case DIFFERENT_TEN:
                imageView.setImage(new Image(SceneBuilder.class.getResource("images/bulb.png").toExternalForm()));
                notification.text("Congratulations! You have unlocked the achievement: Know It All");
                notification.title("Ten Unique Achievement");
                break;
            case DIFFERENT_FIFTY:
                imageView.setImage(new Image(SceneBuilder.class.getResource("images/globe.png").toExternalForm()));
                notification.text("Congratulations! You have unlocked the achievement: Mr WorldWide");
                notification.title("Fifty Unique Achievement");
                break;
            case IMPORT_ONE:
                imageView.setImage(new Image(SceneBuilder.class.getResource("images/new.png").toExternalForm()));
                notification.text("Congratulations! You have unlocked the achievement: Trying Something New");
                notification.title("Import Achievement");
                break;
            case RANDOM:
                imageView.setImage(new Image(SceneBuilder.class.getResource("images/dice.png").toExternalForm()));
                notification.text("Congratulations! You have unlocked the achievement: RNG!!");
                notification.title("Random Achievement");
                break;
        }
        notification.graphic(imageView);
        notification.hideAfter(Duration.seconds(3));
        notification.show();
    }
}
