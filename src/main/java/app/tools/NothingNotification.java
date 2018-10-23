package app.tools;
import javafx.geometry.Pos;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

/**
 * This class is used to display error notifications.
 *
 * @author Brian Nguyen
 */
public class NothingNotification {
    private String _type;
    public NothingNotification(String type) {
        _type = type;
        makeNotification();
    }

    /**
     * Chooses what message to print out based on the type.
     */
    private void makeNotification() {
        Notifications notification = Notifications.create();
        notification.position(Pos.CENTER);
        switch (_type) {
            case "NoNames":
                notification.text("ERROR: No names selected!");
                notification.title("No Names Selected");
                break;
            case "NoMic":
                notification.text("ERROR: No mic selected!");
                notification.title("No mic Selected");
                break;
        }
        notification.hideAfter(Duration.seconds(2));
        notification.showError();
    }
}
