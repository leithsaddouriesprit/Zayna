package tn.esprit.workshop.controlleurs;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import tn.esprit.workshop.model.Reclamation;
import tn.esprit.workshop.services.ReclamationService;
import tn.esprit.workshop.services.ReponseService;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class GestionReponseController {

    @FXML private TableView<Reclamation> tableReclamation;
    @FXML private TableColumn<Reclamation, Integer> colId;
    @FXML private TableColumn<Reclamation, String> colType;
    @FXML private TableColumn<Reclamation, String> colDescription;
    @FXML private TableColumn<Reclamation, String> colStatut;

    @FXML private TextArea reponseField;
    @FXML private Label statusLabel;

    @FXML private TextField searchField;

    private final ReclamationService reclamationService = new ReclamationService();
    private final ReponseService reponseService = new ReponseService();

    @FXML
    public void initialize() {
        // Colonnes de la TableView
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Afficher les réclamations en attente au démarrage
        afficherReclamationsEnAttente();
    }

    // 🔹 Afficher toutes les réclamations en attente
    private void afficherReclamationsEnAttente() {
        try {
            List<Reclamation> reclamations = reclamationService.rechercherParStatut("EN_ATTENTE");
            tableReclamation.setItems(FXCollections.observableArrayList(reclamations));
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Erreur lors de l'affichage !");
        }
    }

    // 🔹 Répondre à une réclamation
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
            // Créer la réponse
            Reponse r = new Reponse(
                    selected.getId(),  // reclamationId
                    reponseField.getText(),
                    LocalDateTime.now()
            );
            reponseService.insertOne(r);

            // Mettre la réclamation à TRAITEE
            selected.setStatut("TRAITEE");
            reclamationService.updateOne(selected);

            afficherReclamationsEnAttente();
            reponseField.clear();
            statusLabel.setText("Réponse envoyée ✔");
        } catch (SQLException e) {
            statusLabel.setText("Erreur lors de l'envoi !");
            e.printStackTrace();
        }
    }

    // 🔹 Modifier une réponse existante
    @FXML
    private void modifierReponse() {
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
            Reponse r = reponseService.getByReclamationId(selected.getId());
            if(r != null){
                r.setMessage(reponseField.getText());
                r.setDate(LocalDateTime.now());
                reponseService.update(r);
                statusLabel.setText("Réponse modifiée ✔");
                reponseField.clear();
                afficherReclamationsEnAttente();
            } else {
                statusLabel.setText("Aucune réponse existante pour cette réclamation !");
            }
        } catch (SQLException e) {
            statusLabel.setText("Erreur modification !");
            e.printStackTrace();
        }
    }

    // 🔹 Supprimer une réponse existante
    @FXML
    private void supprimerReponse() {
        Reclamation selected = tableReclamation.getSelectionModel().getSelectedItem();
        if(selected == null){
            statusLabel.setText("Sélectionnez une réclamation !");
            return;
        }
        try {
            Reponse r = reponseService.getByReclamationId(selected.getId());
            if(r != null){
                reponseService.delete(r.getId());
                statusLabel.setText("Réponse supprimée ✔");
                reponseField.clear();
                afficherReclamationsEnAttente();
            } else {
                statusLabel.setText("Aucune réponse à supprimer !");
            }
        } catch (SQLException e) {
            statusLabel.setText("Erreur suppression !");
            e.printStackTrace();
        }
    }

    // 🔹 Rechercher une réclamation par mot-clé
    @FXML
    private void rechercherReponse() {
        String keyword = searchField.getText();
        if(keyword.isEmpty()){
            afficherReclamationsEnAttente();
            return;
        }
        try {
            tableReclamation.setItems(
                    FXCollections.observableArrayList(
                            reclamationService.rechercherParMotCle(keyword)
                    )
            );
            statusLabel.setText("Résultats pour : " + keyword);
        } catch (SQLException e) {
            statusLabel.setText("Erreur recherche !");
            e.printStackTrace();
        }
    }
}
