package Control;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import Model.ChatModel;
import Model.Client;
import Model.Server;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;

public class ControllerFXMLapplication implements Initializable{
	
	Client client;
	Server server;
	
    @FXML
    private TextField textFieldNomUtilisateur;

    @FXML
    private TextField textFieldFichier;

    @FXML
    private Button buttonChoisirFichier;

    @FXML
    private Button Quitter;

    @FXML
    private TextField textFieldMessageSaisie;

    @FXML
    private Button buttonEnvoyerMsg;

    @FXML
    private ListView<String> listViewConversation;

    @FXML
    private ListView<String> listViewInfo;

    @FXML
    private ListView<String> usagersDisponiblesListView;

    @FXML
    private Button buttonConnexion;

    @FXML
    private Button buttonEnvoyerFichier;

    @FXML
    private TextField textFieldIp;

    @FXML
    private Label reseauLocalLabel;

    @FXML
    void choisirFichier(ActionEvent event) {

    }

    @FXML
    void envoyerFichier(ActionEvent event) {

    }

    @FXML
    void envoyerMessage(ActionEvent event) {

    }

    @FXML
    void etablirConnexion(ActionEvent event) {

    }

    @FXML
    void quitter(ActionEvent event) {

    }
    
    public void initialize(URL location, ResourceBundle resources)
    {	
    	if (askForUsername())
    	{
	    	client = new Client();
	    	server = new Server();
	  
	    	//Open the server socket used to receive UDP broadcast from remote clients
	    	server.openUDPSocket();
	    	server.receiveBroadcastRequests(textFieldNomUtilisateur.getText());
	    	
	    	//Set the LAN name
	    	reseauLocalLabel.setText(client.findDnsSuffix());
	    	
	    	//Set the app messages listview
	    	listViewInfo.setItems(ChatModel.getInstance().getStatusMessagesList());
	    	
	    	//Set the available user listview
	    	usagersDisponiblesListView.setItems(ChatModel.getInstance().getAvailableForChatIpAddressList());
	    	client.getRemoteUserAvailableForChat();
    	}
    	else
    	{
    		System.exit(0);
    	}
    	
    }
    
    /**
     * Ask the user to enter a username
     * @return true if the user entered a username
     */
    public boolean askForUsername()
    {
    	boolean nameEntered = true;
    	
    	TextInputDialog dialog = new TextInputDialog();
    	dialog.setTitle("Nom d'utilisateur");
    	dialog.setHeaderText(null);
    	dialog.setContentText("Entrer un nom d'utilisateur:");
    	Optional<String> result = dialog.showAndWait();
    	if (result.isPresent())
    	{
    		textFieldNomUtilisateur.setText(result.get());
    	}
    	else
    	{
    		nameEntered = false;
    	}
    	
    	return nameEntered;
    }

}
