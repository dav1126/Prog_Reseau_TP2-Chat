package Control;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class ControllerFXMLapplication {

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
    private ListView<?> listViewConversation;

    @FXML
    private ListView<?> listViewInfo;

    @FXML
    private ListView<?> usagersDisponiblesListView;

    @FXML
    private Button buttonConnexion;

    @FXML
    private Button buttonEnvoyerFichier;

    @FXML
    private TextField textFieldIp;

    @FXML
    private ChoiceBox<?> reseauxChoiceBox;

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

}
