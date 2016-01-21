package Model;


import java.util.HashMap;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ChatModel
{
	private static ChatModel instance;
	
	private ObservableList<String> statusMessagesList = null;
	
	private ObservableList<String> chatMessagesList = null;
	
	private ObservableList<String> availableForChatUsersList = null;
	
	private Map<String, String> userAvailableToChatMap = null;
	
	private boolean connectionEstablished = false;
	
	private String remoteUserIpAddress = null;

	private ChatModel()
	{
		statusMessagesList = FXCollections.observableArrayList();
		chatMessagesList =FXCollections.observableArrayList();
		availableForChatUsersList = FXCollections.observableArrayList();
		userAvailableToChatMap = new HashMap<>();
	}
	
	public boolean isConnectionEstablished()
	{
		return connectionEstablished;
	}
	
	public void setConnectionEstablished(boolean connectionEstablished)
	{
		this.connectionEstablished = connectionEstablished;
	}
	
	public String getRemoteUserIpAddress()
	{
		return remoteUserIpAddress;
	}

	public void setRemoteUserIpAddress(String remoteUserIpAddress)
	{
		this.remoteUserIpAddress = remoteUserIpAddress;
	}
	
	public ObservableList<String> getChatMessagesList()
	{
		return chatMessagesList;
	}

	public ObservableList<String> getStatusMessagesList()
	{
		return statusMessagesList;
	}
	
	public ObservableList<String> getAvailableForChatUsersList()
	{
		return availableForChatUsersList;
	}
	
	public Map<String, String> getUserAvailableToChatMap()
	{
		return userAvailableToChatMap;
	}
	
	public static ChatModel getInstance(){
		
		if(instance == null){
			
			instance = new ChatModel();
		}
		
		return instance;
	}
}
