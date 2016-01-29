package Control;

import Control.ControllerFXMLapplication;
import javafx.application.Application;
import javafx.beans.binding.When;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * Classe permettant de faire le liena avec la vue et de d�marrer le programme
 * @author 0345162
 *
 */
public class mainController extends Application
{

	/**
	 * Gestionnaire d'�v�nements utilis� pour g�rer la fermeture du stage
	 * principale
	 */
	EventHandler<WindowEvent> windowEventHandler;

	public static void main(String[] args)
	{

		Application.launch(args);
	}

	/**
	 * D�marre l'application
	 */
	public void start(Stage pStage) throws Exception
	{
		FXMLLoader loader = new FXMLLoader(getClass().getResource(
				"../View/FXMLapplication.fxml"));
		AnchorPane root = (AnchorPane) loader.load();
		ControllerFXMLapplication subController = (ControllerFXMLapplication) loader
				.getController();

		Scene scene = new Scene(root);
		pStage.setScene(scene);
		pStage.setTitle("Chat Application");
		
		pStage.getIcons().add(new Image("/View/51537.png"));
		
		pStage.show();
		addSubControllerWindowEventHandler(pStage, subController);
	}

	/**
	 * Ajoute un gestionnaire de fen�tre au stage principal pour g�rer sa
	 * fermeture
	 * 
	 * @param stage
	 * @param mainController
	 */
	private void addSubControllerWindowEventHandler(Stage stage,
			ControllerFXMLapplication subController)
	{
		windowEventHandler = new EventHandler<WindowEvent>()
		{
			@Override
			public void handle(WindowEvent event)
			{
				event.consume();
				subController.quitter();
			}
		};
		stage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST,
				windowEventHandler);
	}

}
