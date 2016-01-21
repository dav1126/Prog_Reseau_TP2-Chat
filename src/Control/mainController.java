package Control;

import Control.ControllerFXMLapplication;
import javafx.application.Application;
import javafx.beans.binding.When;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;


public class mainController extends Application
{

public static void main(String[] args) {
		
		Application.launch(args);		
	}
	
	public void start(Stage pStage) throws Exception
	{
		FXMLLoader loader = new FXMLLoader(getClass().getResource("../View/FXMLapplication.fxml"));
		AnchorPane root = (AnchorPane) loader.load();
		//ControllerFXMLapplication mainViewController = (ControllerFXMLapplication)loader.getController();
		
		Scene scene = new Scene(root);
		pStage.setScene(scene);
		pStage.setTitle("Chat Application");
		pStage.show();
	}

}
