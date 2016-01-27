package Model;

import java.util.HashMap;
import java.util.Map;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Classe singleton permettant d'entreposer les donn�es n�cessaires au
 * d�roulement du programme
 * 
 * @author 0345162
 *
 */
public class ChatModel
{
	/**
	 * Pointeur vers le singleton ChatModel
	 */
	private static ChatModel instance;

	/**
	 * Liste observable de messages de l'application � l'intention de
	 * l'utilisateur
	 */
	private ObservableList<String> statusMessagesList = null;

	/**
	 * Liste observable de message �chang�s entre les utilisateurs
	 */
	private ObservableList<String> chatMessagesList = null;

	/**
	 * Liste observable d'utilisateur online
	 */
	private ObservableList<String> availableForChatUsersList = null;

	/**
	 * Map d'utilisateur online (key) et leur addresse Ip correspondante (value)
	 */
	private Map<String, String> userAvailableToChatMap = null;

	/**
	 * Propri�t� bool�eene qui indique si une connectione st �tablie
	 */
	private BooleanProperty connectionEstablished;

	/**
	 * Nom d'utilisateur de l'utilisateur distant. Utilis� pour afficher le nom
	 * du user avant les messages qu'il a envoy�
	 */
	private String remoteUsername;

	/**
	 * Constructeur de ChatModel
	 */
	private ChatModel()
	{
		connectionEstablished = new SimpleBooleanProperty(false);
		statusMessagesList = FXCollections.observableArrayList();
		chatMessagesList = FXCollections.observableArrayList();
		availableForChatUsersList = FXCollections.observableArrayList();
		userAvailableToChatMap = new HashMap<>();
	}

	/**
	 * Retourne true si la connection est �tablie
	 * 
	 * @return boolean de connection
	 */
	public boolean isConnectionEstablished()
	{
		return connectionEstablished.get();
	}

	/**
	 * Retourne la propri�t� connectionEstablished
	 * 
	 * @return propri�t� bool�ene connectionEstablished
	 */
	public BooleanProperty getConnectionEstablishedProperty()
	{
		return this.connectionEstablished;
	}

	/**
	 * Set the connectionEstablished property
	 * 
	 * @param boolean pour connectionEstablished
	 */
	public void setConnectionEstablished(boolean connectionEstablished)
	{
		this.connectionEstablished.set(connectionEstablished);
	}

	/**
	 * Retourne la liste de messages utilisateur
	 * 
	 * @return liste de messages utilisateur
	 */
	public ObservableList<String> getChatMessagesList()
	{
		return chatMessagesList;
	}

	/**
	 * Retourne la liste de message du programme
	 * 
	 * @return liste de message du programme
	 */
	public ObservableList<String> getStatusMessagesList()
	{
		return statusMessagesList;
	}

	/**
	 * Retourne la liste des utilisateurs online
	 * 
	 * @return liste d'utilisateur online
	 */
	public ObservableList<String> getAvailableForChatUsersList()
	{
		return availableForChatUsersList;
	}

	/**
	 * Retourne la map d'utilisateur online
	 * 
	 * @return map d'utilisateur online
	 */
	public Map<String, String> getUserAvailableToChatMap()
	{
		return userAvailableToChatMap;
	}

	/**
	 * Retourne l'instance de ChatModel, apr�s l,avoir instanci� une 1ere fois
	 * 
	 * @return
	 */
	public static ChatModel getInstance()
	{

		if (instance == null)
		{

			instance = new ChatModel();
		}

		return instance;
	}

	/**
	 * Set le nom d'utilisateur distant
	 * 
	 * @param remote
	 *            username
	 */
	public void setRemoteUsername(String username)
	{
		remoteUsername = username;
	}

	/**
	 * Retourne le nom d'utilisateur distant
	 * 
	 * @return remote username
	 */
	public String getRemoteUsername()
	{
		return remoteUsername;
	}
}
