package app.views;

import app.controllers.ParentController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * This class allows the scene to be created. This is done by
 * sending the stage to the controller and allowing the controller
 * to recreate the scene.
 *
 * @author Brian Nguyen
 */
public class SceneBuilder extends FXMLLoader {
    private Stage _stage;
    //Needs to have the primary stage in order to update it.
    public SceneBuilder(Stage stage) {
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
        Scene scene = new Scene(layout);
        //allows the correct style sheet to be applied to the fxml
        String css = this.getClass().getResource("styles/"+url.substring(0,url.length()-4)+"css").toExternalForm();
        scene.getStylesheets().add(css);
        _stage.setScene(scene);
        _stage.show();
    }

    /**
     * This function allows the controllers to receive the stage
     * @param controller: The current controller of the view
     */
    public void setStage(ParentController controller) {
        controller.setStage(_stage);
    }

}
