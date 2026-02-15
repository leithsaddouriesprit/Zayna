package tn.esprit.workshop.controlleurs;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import tn.esprit.workshop.model.Reclamation;
import tn.esprit.workshop.model.Reponse;
import tn.esprit.workshop.services.ReclamationService;
import tn.esprit.workshop.services.ReponseService;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class GestionReponseController {

    @FXML private TableView<Reclamation> tableReclamation;
    @FXML private TableColumn<Reclamation, Integer> colId;
    @FXML private TableColumn<Reclamation, String> colType;
    @FXML private TableColumn<Reclamation, String> colDescription;
    @FXML private TableColumn<Reclamation, String> colStatut;

    @FXML private TextArea reponseField;
    @FXML private Label statusLabel;

    private final ReclamationService reclamationService = new ReclamationService();
    private final ReponseService reponseService = new ReponseService();

    @FXML
    public void initialize() {

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        afficherReclamationsEnAttente();
    }

    private void afficherReclamationsEnAttente() {
        try {
            tableReclamation.setItems(
                    FXCollections.observableArrayList(
                            reclamationService.rechercherParStatut("EN_ATTENTE")
                    )
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void repondreReclamation() {

        Reclamation selected = tableReclamation.getSelectionModel().getSelectedItem();

        if(selected == null){
            statusLabel.setText("Sélectionnez une réclamation !");
            return;
        }

        if(reponseField.getText().isEmpty()){
            statusLabel.setText("Écrivez une réponse !");
            return;
        }

        try {

            // 🔹 Ajouter réponse
            Reponse r = new Reponse(

                    selected.getId(),
                    reponseField.getText(),
                    LocalDateTime.now()
            );

            reponseService.insertOne(r);

            // 🔹 Mettre statut = TRAITEE
            selected.setStatut("TRAITEE");
            reclamationService.updateOne(selected);

            afficherReclamationsEnAttente();
            reponseField.clear();

            statusLabel.setText("Réponse envoyée ✔");

        } catch (SQLException e) {
            statusLabel.setText("Erreur !");
            e.printStackTrace();
        }
    }
}
