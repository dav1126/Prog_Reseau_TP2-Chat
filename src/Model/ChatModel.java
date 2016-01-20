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
	
	private ObservableList<String> availableForChatIpAddressList = null;
	
	private ObservableList<String> availableNetworkInterfacesNamesList = null;
	
	private Map<String, String> userAvailableToChatMap = null;
	
	private ChatModel()
	{
		statusMessagesList = FXCollections.observableArrayList();
		chatMessagesList =FXCollections.observableArrayList();
		availableForChatIpAddressList = FXCollections.observableArrayList();
		userAvailableToChatMap = new HashMap<>();
	}
	
	public ObservableList<String> getChatMessagesList()
	{
		return chatMessagesList;
	}

	public ObservableList<String> getStatusMessagesList()
	{
		return statusMessagesList;
	}
	
	public ObservableList<String> getAvailableForChatIpAddressList()
	{
		return availableForChatIpAddressList;
	}
	
	public ObservableList<String> getAvailableNetworkInterfacesNames()
	{
		return availableNetworkInterfacesNamesList;
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
