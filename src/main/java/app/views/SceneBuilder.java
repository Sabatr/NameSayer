package app.views;

import app.backend.FSWrapper;
import app.backend.NameEntry;
import app.controllers.ParentController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.naming.Name;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This class allows the scene to be created. This is done by
 * sending the stage to the controller and allowing the controller
 * to recreate the scene.
 *
 * @author Brian Nguyen
 */
public class SceneBuilder extends FXMLLoader {
    private Stage _stage;
    private ObservableList<NameEntry> _allNames;
    private ObservableList<NameEntry> _list;

    //Needs to have the primary stage in order to update it.
    public SceneBuilder(ObservableList<NameEntry> all, Stage stage) {
        _list = FXCollections.observableList(new ArrayList<>());
        _allNames = all;
        _stage = stage;
    }

    /**
     * This function allows the stage to load a scene.
     * @param url: The path to the view
     * @throws IOException
     */
    public void load(String url) throws IOException{
        this.setLocation(this.getClass().getResource(url));
        Parent layout = this.load();
        setStage(getController());
        ((ParentController) getController()).setInformation(_allNames, _list);
        Scene scene = new Scene(layout);
        //allows the correct style sheet to be applied to the fxml
        String css = this.getClass().getResource("styles/"+url.substring(0,url.length()-4)+"css").toExternalForm();
        scene.getStylesheets().add(css);
        _stage.setScene(scene);
        _stage.show();
    }

    public void getList(ObservableList<NameEntry> list) {
        _list = list;
    }

    /**
     * This function allows the controllers to receive the stage
     * @param controller: The current controller of the view
     */
    public void setStage(ParentController controller) {
        controller.setStage(_stage);
    }
}
