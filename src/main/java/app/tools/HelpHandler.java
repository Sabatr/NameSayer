package app.tools;

import com.jfoenix.controls.JFXPopup;
import com.jfoenix.controls.JFXTabPane;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;

/**
 * This class allows the help section ( '?' ) to dynamically change
 * depending on which scene has pressed it.
 *
 * @author Brian Nguyen
 */
public class HelpHandler {
    private Node _node;
    private String _help;
    public HelpHandler(Node node,String help) {
        _node = node;
        _help = help;
        create();

    }

    /**
     * The TabPane is created. We populate the tabs with HELP and ABOUT tabs.
     * The content within in the different tabs depend on which scene the help button
     * is pressed.
     */
    private void create()  {
        JFXTabPane pane = new JFXTabPane();
        pane.setPrefHeight(300);
        pane.setPrefWidth(500);

        Tab aboutTab = new Tab("ABOUT");
        Tab helpTab = new Tab("HELP");
        switch(_help) {
            case "practice":
                helpTab.setContent(new TextArea(
                        "If you have chosen more than one name, you can cycle through them by" +
                                "\nclicking the '<' and '>' buttons.\n"
                        +"To record yourself making names, make sure you have set up your mic!.\n"
                        +"You can select what you want to listen to AFTER your recording by " +
                                "\nhovering over the up arrow.\n"
                        +"Make sure to also rate the database recordings. :)"
                ));
                aboutTab.setContent(new TextArea("This page is where you practice saying names!\n "
                        +"Users can record, rate and listen to names."
                ));
                break;
            case "main":
                helpTab.setContent(new TextArea("The bottom left corner are the buttons to display the help,\n"
                         +"configure your microphone and show your achievements. \n"
                        +"You can also import your very own sound data base."
                ));
                aboutTab.setContent(new TextArea("Welcome to NameSayer!\n" +
                        "This was made by @Sabatr and @Margeobur on Github using\n"
                        +"JavaFx and SceneBuilder. External libraries were used such as\n"
                        +"controlsfx and jfoenix."
                ));
                break;
            case "achievements":
                helpTab.setContent(new TextArea("ACHIEVEMENTS!\n"
                    +"Your progress will update as your continue practicing!"
                ));
                aboutTab.setContent(new TextArea("There are currently 8 achievements you can achieve!"));
                break;
            case "list":
                helpTab.setContent(new TextArea("You can press SHIFT+CLICK to select multiple at once. Click on the names in the\n" +
                        "practice list to remove them."
                ));
                aboutTab.setContent(new TextArea("This is where you can select the names you want to practice\n"
                +" You can also order your list or randomise them."
                ));
                break;
        }

        pane.getTabs().add(helpTab);
        pane.getTabs().add(aboutTab);
        JFXPopup popup = new JFXPopup(pane);
        popup.show(_node,JFXPopup.PopupVPosition.TOP,JFXPopup.PopupHPosition.LEFT,0,-300);
    }
}
