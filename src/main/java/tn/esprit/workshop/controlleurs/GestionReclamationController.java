package tn.esprit.workshop.controlleurs;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import tn.esprit.workshop.model.Reclamation;
import tn.esprit.workshop.services.ReclamationService;

import java.sql.SQLException;

public class GestionReclamationController {

    @FXML private ChoiceBox<String> typeChoice;
    @FXML private TextArea messageField;
    @FXML private Label statusLabel;

    @FXML private TableView<Reclamation> tableReclamation;
    @FXML private TableColumn<Reclamation, Integer> colId;
    @FXML private TableColumn<Reclamation, String> colType;
    @FXML private TableColumn<Reclamation, String> colDescription;
    @FXML private TableColumn<Reclamation, String> colStatut;

    private final ReclamationService service = new ReclamationService();

    @FXML
    public void initialize() {

        typeChoice.getItems().addAll("Bus", "Ecole");

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        afficherReclamations();
    }

    @FXML
    private void envoyerReclamation() {

        if(typeChoice.getValue() == null || messageField.getText().isEmpty()){
            statusLabel.setText("Veuillez remplir tous les champs !");
            return;
        }

        try {
            Reclamation r = new Reclamation(
                    1,
                    "Parent",
                    typeChoice.getValue(),
                    messageField.getText(),
                    "EN_ATTENTE"
            );

            service.insertOne(r);
            afficherReclamations();
            messageField.clear();
            statusLabel.setText("Réclamation envoyée ✔");

        } catch (SQLException e) {
            statusLabel.setText("Erreur !");
        }
    }

    private void afficherReclamations(){
        try {
            tableReclamation.setItems(
                    FXCollections.observableArrayList(service.selectAll())
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
