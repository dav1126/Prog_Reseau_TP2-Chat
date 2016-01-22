package Control;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;

import Model.ChatModel;
import Model.Client;
import Model.Server;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.When;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.util.Duration;

public class ControllerFXMLapplication implements Initializable{
	
	Client client;
	Server server;
	ChatModel chatModel;
	
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
    protected ListView<String> usagersDisponiblesListView;

    @FXML
    protected Button buttonConnexion;

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
    void envoyerFichier() 
    {
    	client.startSendFile(textFieldFichier.getText());
    }

    @FXML
    void envoyerMessage() 
    {
    	client.sendMessage(textFieldMessageSaisie.getText());
    }

    @FXML
    void etablirConnexion() 
    {
    	String selectedRemoteUser = 
    			usagersDisponiblesListView.getSelectionModel().getSelectedItem();
    	String remoteUserIp = chatModel.getUserAvailableToChatMap().get(selectedRemoteUser);
     	
    	client.openClientSocket(remoteUserIp);
    	client.sendChatRequest(textFieldNomUtilisateur.getText());
    	
    	synchronized (client.getControllerThreadLock())
		{
			try
			{
				client.getControllerThreadLock().wait();
			} catch (InterruptedException e)
			{
				System.out.println("controllerThreadLock interrupted");
				e.printStackTrace();
			}
		}
    	
    	if (client.isChatRequestAccepted())
    	{
    		enableChat();
    		//client.stopLookingForOnlineUsers();
    	}
    	else
    	{
    		client.closeClientSocket();
    	}
    }

    private void enableChat()
	{
		textFieldMessageSaisie.setDisable(false);
		listViewConversation.setDisable(false);
		buttonChoisirFichier.setDisable(false);
		textFieldFichier.setDisable(false);	
	}
    
    private void disableChat()
    {
    	textFieldMessageSaisie.setDisable(true);
    	listViewConversation.setDisable(true);
    	buttonChoisirFichier.setDisable(true);
    	textFieldFichier.setDisable(true);
    }

	@FXML
    void quitter(ActionEvent event) {
    	System.exit(0);
    }
    
    public void initialize(URL location, ResourceBundle resources)
    {	
    	if (askForUsername())
    	{
	    	client = new Client();
	    	server = new Server();
	    	chatModel = ChatModel.getInstance();
	  
	    	//Open the server socket used to receive UDP broadcast from remote clients
	    	server.openUDPSocket();
	    	server.receiveBroadcastRequests(textFieldNomUtilisateur.getText());
	    	
	    	//Open the server socket and start to wait for message
	    	server.openServerSockets();
	    	server.startReceiveMessageThread();
	    	
	    	//Open the client socket used to send UDP broadcast to remote servers
	    	client.openUDPSocket();
	    	
	    	//Set the LAN name
	    	reseauLocalLabel.setText(client.findDnsSuffix());
	    	
	    	//Set the app messages listview
	    	listViewInfo.setItems(chatModel.getStatusMessagesList());
	    	
	    	//Set the available user listview
	    	usagersDisponiblesListView.setItems(chatModel.getAvailableForChatUsersList());
	    	client.getRemoteUserAvailableForChat();
	    	
	    	//Set a listener to bind the connexion button to the selectedProperty of the available user ListView
	    	usagersDisponiblesListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Object>() 
	    	{
	    	    @Override
	    	    public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) 
	    	    {
	    	        if (buttonConnexion.isDisabled())
	    	        	buttonConnexion.setDisable(false);
	    	        else
	    	        	buttonConnexion.setDisable(true);
	    	    }
	    	});
	    	
	    	//Set a listener to the server's chatRequestedProperty to pop an alert when it is changed to true
	    	setChatRequestAlert();
	    	
	    	//Set a listener on the textFieldMessageSaisie that binds it to the buttonEnvoyerFichier
	    	setButtonEnvoyerMessageBinds();
    	}
    	else
    	{
    		System.exit(0);
    	} 	
    }
    
    /**
     * Set a listener on the server's chatRequestedProperty to pop an alert when it is changed to true
     */
    private void setChatRequestAlert()
    {
    	//Set a listener to the server's chatRequestedProperty to pop an alert when it is changed to true
    	server.getChatRequestedBooleanProperty().addListener(new ChangeListener<Object>() 
    	{
    	    @Override
    	    public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) 
    	    {
    	        if (server.getChatRequestedBooleanProperty().getValue())
    	        {
    	        	//Set the chatRequested property back to false
    	        	server.getChatRequestedBooleanProperty().setValue(false);
    	        	
    	        	//Pop an alert asking this server's user if he wants to accept the chat request
    	        	Platform.runLater(new Runnable()
    	        	{
    	        		@Override
    	        		public void run()
    	        		{	    	        		
	    	        		Alert alert = new Alert(AlertType.INFORMATION);
		    	        	alert.setTitle("Chat request");
		    	        	alert.setHeaderText(null);
		    	        	alert.setContentText(server.getChatRequestApplicantUsername() + " veux chatter avec vous!\n Vous avez 10 secondes pour répondre...");
	
		    	        	ButtonType buttonTypeAccepter = new ButtonType("Accepter");
		    	        	ButtonType buttonTypeRefuser = new ButtonType("Refuser");
		    	        	
		    	        	alert.getButtonTypes().setAll(buttonTypeAccepter, buttonTypeRefuser);
		    	        	
		    	        	Timeline idlestage = new Timeline( new KeyFrame(Duration.seconds(10), new EventHandler<ActionEvent>()
		    	        		    {
	
		    	        		        @Override
		    	        		        public void handle( ActionEvent event )
		    	        		        {
		    	        		            alert.setResult(buttonTypeRefuser);
		    	        		            alert.close();
		    	        		        }
		    	        		    } ) );
		        		    idlestage.setCycleCount( 1 );
		        		    idlestage.play();
		        		    Optional <ButtonType> choix = alert.showAndWait();
		        		    
		        		    if (choix.get() == buttonTypeAccepter)
		        		    {
		        		        client.openClientSocket(server.getChatRequestApplicantIp());
		        		        server.setChatRequestAccepted(true);
		        		        enableChat();
		        		    }
		        		    else
		        		    	server.setChatRequestAccepted(false);
		        		    synchronized (server.getReceiveMessageThreadLock())
		    				{
		        		    	server.getReceiveMessageThreadLock().notify();
		    				}
    	        		}
    	        	});
    	        }
    	    }
    	});
    }
    
    /**
     * Set the listeners used to manage the buttonEnvoyerMessage's disable property
     */
    public void setButtonEnvoyerMessageBinds()
    {
    	//When the textFieldMessage's text is changed
    	textFieldMessageSaisie.textProperty().addListener(new ChangeListener<String>()
    			{

			@Override
			public void changed(ObservableValue<? extends String> observable,
					String oldValue, String newValue)
			{		
				//If the textfield is empty or disabled
				if (textFieldMessageSaisie.getText() == null || textFieldMessageSaisie.getText().trim().isEmpty())
				{
					//Disable the buttonEnvoyer
					buttonEnvoyerMsg.setDisable(true);
				}
				else
				{
					buttonEnvoyerMsg.setDisable(true);
				}
			}
    	});
			
		//When the textFieldMessage is disabled
		textFieldMessageSaisie.disabledProperty().addListener(new ChangeListener<Boolean>()
			{

			@Override
			public void changed(ObservableValue<? extends Boolean> observable,
					Boolean oldValue, Boolean newValue)
			{						
					if (textFieldMessageSaisie.isDisabled())
					{
						//Disable the buttonEnvoyer
						buttonEnvoyerMsg.setDisable(true);		
					}
					else
					{
						//If the textfield is not empty
						if (textFieldMessageSaisie.getText() != null && !textFieldMessageSaisie.getText().trim().isEmpty())
						{
							//Disable the buttonEnvoyer
							buttonEnvoyerMsg.setDisable(false);
						}
					}
			}
		}); 	
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
