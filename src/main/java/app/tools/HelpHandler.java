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
        TextArea helpText = new TextArea();
        TextArea aboutText = new TextArea();
        helpText.setWrapText(true);
        aboutText.setWrapText(true);
        Tab aboutTab = new Tab("ABOUT");
        Tab helpTab = new Tab("HELP");
        switch(_help) {
            case "practice":
                aboutText.setText("This is where you can begin practicing your names. You can listen" +
                        "to names that already exist and record your attempts. Playback and rating features are available.\n");
                helpText.setText("Before practicing: Make sure you have a microphone selected. To do so" +
                        "click on the microphone at the bottom left corner - on the right side of this button. \n" +
                        "To record: Simply press the record button. You will have a 5 second window to begin practicing" +
                        "your name. A prompt will soon appear after where you can decide what to do next. During" +
                        "this prompt, there is an option to select which audio you want to hear. Hover over the up" +
                        "arrow to decide.\n" +
                        "To rate a recording in the database: simply hover over the rate button. A popup of numbers should" +
                        "appear. You can click on any of them to rate." +
                        "You can then view what you created in the 'User Recordings' option.");
                break;
            case "main":
                aboutText.setText("Welcome to Name Sayer!\n" +
                        "This is an application made by @Margeobur and @Sabatr on Github. " +
                        "The purpose of this application is to allow students to practice the pronunciation" +
                        "of their classmates. ");
                helpText.setText("To start practicing: Click the 'View Names' section. Further help can be found" +
                        "once you are on that page. \n" +
                        "You can also import your own database (only .wav files) with the 'Import Database' button.");
                break;
            case "achievements":
                aboutText.setText("Achievements!\n" +
                        "There are currently only 8 achievements for the user.");
                helpText.setText("Your progress updates whenever you do something worth while.\n" +
                        "You will receive a notification whenever you unlock an achievement.");
                break;
            case "list":
                aboutText.setText("This is where you can select what names you can practice. Concatenating names is" +
                        "also possible by typing them in the search bar.\n" +
                        "The left-most list is the names you can select from and the right-most is the practice list," +
                        "where you see the names you have selected.");
                helpText.setText("To select names for your practice list, simply click on a name of the left-most" +
                        "list. Multiple names can be selected at once by first selecting the starting point," +
                        " holding the SHIFT key and clicking the end point.\n" +
                        "You can also remove names from the practice list by clicking on them. To clear all of them," +
                        "click the 'Clear' button.\n" +
                        "You can also import your own practice list (in a .txt file). However, names that do not exist" +
                        "in the database will not be added!\n" +
                        "You have an option before practicing to randomise your list or sort it alphabetically. " +
                        "You cannot have both simultaneously.");
                break;
            case "userRecording":
                aboutText.setText("This is the place where you can view all your recordings for a particular name.");
                helpText.setText("This menu is only useful if you have any recordings. If you don't, no options will" +
                        "be available to you.\n" +
                        "If you do have recordings you can select them with the dropdown menu in the " +
                        "centre of the screen. You can delete any of your recordings and compare them to the database" +
                        "version any time.");
                break;
        }
        helpTab.setContent(helpText);
        aboutTab.setContent(aboutText);
        pane.getTabs().add(aboutTab);
        pane.getTabs().add(helpTab);
        JFXPopup popup = new JFXPopup(pane);
        popup.show(_node,JFXPopup.PopupVPosition.TOP,JFXPopup.PopupHPosition.LEFT,0,-300);
    }
}
