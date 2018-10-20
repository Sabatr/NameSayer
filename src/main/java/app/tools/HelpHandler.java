package app.tools;

import com.jfoenix.controls.JFXPopup;
import com.jfoenix.controls.JFXTabPane;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;

public class HelpHandler {
    private Node _node;
    private String _help;
    public HelpHandler(Node node,String help) {
        _node = node;
        _help = help;
        create();

    }
    private void create()  {
        JFXTabPane pane = new JFXTabPane();
        pane.setPrefHeight(300);
        pane.setPrefWidth(500);

        Tab helpTab = new Tab("HELP");
        Tab aboutTab = new Tab("ABOUT");
        switch(_help) {
            case "practice":
                helpTab.setContent(new TextArea("Practice stuff"));
                aboutTab.setContent(new TextArea("This is where you practice stuff"));
                break;
            case "main":
                helpTab.setContent(new TextArea("Placeholder"));
                aboutTab.setContent(new TextArea("This was made by @Sabatr and @Margeobur on Github."));
                break;
            case "achievements":
                helpTab.setContent(new TextArea("ACHIEVEMENTS"));
                aboutTab.setContent(new TextArea("This is the achievements stuff"));
                break;
        }

        pane.getTabs().add(helpTab);
        pane.getTabs().add(aboutTab);
        JFXPopup popup = new JFXPopup(pane);
        popup.show(_node,JFXPopup.PopupVPosition.TOP,JFXPopup.PopupHPosition.LEFT,0,-300);
    }
}
