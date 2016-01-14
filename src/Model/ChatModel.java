package Model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ChatModel
{
	private ObservableList<String> statusMessagesList = null;
	
	private ObservableList<String> chatMessagesList = null;
	
	public ChatModel()
	{
		statusMessagesList = FXCollections.observableArrayList();
		chatMessagesList =FXCollections.observableArrayList();
	}
	
	public ObservableList<String> getChatMessagesList()
	{
		return chatMessagesList;
	}

	public ObservableList<String> getStatusMessagesList()
	{
		return statusMessagesList;
	}
	
	//Les methodes .add des ObservableList<String> mettent la qui les contient listView à jour automatiquement
	//Ces observables listes du modele doivent etre palcées directement dans les listview de la vue
}
