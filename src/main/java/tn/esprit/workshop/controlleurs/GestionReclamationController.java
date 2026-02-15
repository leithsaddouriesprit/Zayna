package tn.esprit.workshop.controlleurs;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import tn.esprit.workshop.model.Reclamation;
import tn.esprit.workshop.services.ReclamationService;
import tn.esprit.workshop.utilis.MyBDConnexion;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class GestionReclamationController implements Initializable {

    @FXML private ChoiceBox<String> typeChoice;
    @FXML private TextArea messageField;
    @FXML private Label statusLabel;

    @FXML private TableView<Reclamation> tableReclamation;
    @FXML private TableColumn<Reclamation, Integer> colId;
    @FXML private TableColumn<Reclamation, String> colType;
    @FXML private TableColumn<Reclamation, String> colDescription;
    @FXML private TableColumn<Reclamation, String> colStatut;

    private final ReclamationService service = new ReclamationService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        typeChoice.getItems().addAll("Bus", "Ecole");

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        afficherReclamations();
    }

    @FXML
    private void envoyerReclamation() {

        // 🔹 Lecture et nettoyage des champs
        String type = (typeChoice.getValue() == null) ? "" : typeChoice.getValue().trim();
        String message = (messageField.getText() == null) ? "" : messageField.getText().trim();

        // 🔹 Validation
        if(type.isEmpty() || message.isEmpty()){
            statusLabel.setText("Veuillez remplir tous les champs !");
            return;
        }

        try {
            // 🔹 Appel du service pour insérer et récupérer l'ID de la réclamation
            int reclamationId = service.ajouterReclamationEtRetournerId(
                    1,          // userId fixe ici, peut être dynamique
                    "Parent",   // type utilisateur ou autre logique métier
                    type,
                    message,
                    "EN_ATTENTE"
            );

            // 🔹 Message succès
            statusLabel.setText("Réclamation envoyée ✔ (ID = " + reclamationId + ")");

            // 🔹 Reset des champs
            typeChoice.setValue(null);
            messageField.clear();

            // 🔹 Actualiser la table
            afficherReclamations();

        } catch (SQLException e) {
            statusLabel.setText("Erreur lors de l'envoi ❌ (voir console)");
            e.printStackTrace();
        }
    }



    private void afficherReclamations(){
        try {
            tableReclamation.setItems(
                    FXCollections.observableArrayList(service.selectAll())
            );
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }



}
