package tn.esprit.workshop.controlleurs;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import tn.esprit.workshop.model.Reclamation;
import tn.esprit.workshop.services.ReclamationService;

import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
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
    @FXML private TableColumn<Reclamation, Timestamp> colDate;  // Nouvelle colonne

    private final ReclamationService service = new ReclamationService();
    private int currentUserId = 1; // À remplacer par l'ID de l'utilisateur connecté

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // Ajouter les types de réclamation
        typeChoice.getItems().addAll("Bus", "École", "Chauffeur", "Cantine", "Trajet", "Autre");

        // Configurer les colonnes
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Configurer la colonne date avec formatage
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateReclamation"));
        colDate.setCellFactory(column -> new TableCell<Reclamation, Timestamp>() {
            private final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");

            @Override
            protected void updateItem(Timestamp item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(format.format(item));
                }
            }
        });

        // Ajouter un style conditionnel pour le statut
        colStatut.setCellFactory(column -> new TableCell<Reclamation, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("EN_ATTENTE".equals(item)) {
                        setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                    } else if ("TRAITEE".equals(item)) {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // Charger les réclamations
        afficherReclamations();
    }

    @FXML
    private void envoyerReclamation() {

        // Récupération et validation des champs
        String type = typeChoice.getValue();
        String message = messageField.getText();

        if (type == null || type.trim().isEmpty()) {
            statusLabel.setText("❌ Veuillez sélectionner un type de réclamation !");
            return;
        }

        if (message == null || message.trim().isEmpty()) {
            statusLabel.setText("❌ Veuillez écrire votre message !");
            return;
        }

        try {
            // Ajouter la réclamation
            int reclamationId = service.ajouterReclamationEtRetournerId(
                    currentUserId,
                    type,
                    message.trim(),
                    "EN_ATTENTE"
            );

            // Message de succès
            statusLabel.setText("✅ Réclamation envoyée (ID: " + reclamationId + ")");

            // Réinitialiser les champs
            typeChoice.setValue(null);
            messageField.clear();

            // Actualiser la table
            afficherReclamations();

        } catch (SQLException e) {
            statusLabel.setText("❌ Erreur lors de l'envoi de la réclamation");
            e.printStackTrace();
        }
    }

    private void afficherReclamations() {
        try {
            // Pour l'instant on affiche toutes les réclamations
            // Plus tard, on pourra filtrer par utilisateur
            tableReclamation.setItems(
                    FXCollections.observableArrayList(service.selectAll())
            );

            // Ajuster automatiquement la hauteur des lignes pour le texte long
            tableReclamation.setRowFactory(tv -> {
                TableRow<Reclamation> row = new TableRow<>();
                row.setPrefHeight(40);
                return row;
            });

        } catch (SQLException e) {
            statusLabel.setText("❌ Erreur lors du chargement des réclamations");
            e.printStackTrace();
        }
    }
    @FXML
    private void modifierReclamation() {

        Reclamation selected = tableReclamation.getSelectionModel().getSelectedItem();

        if (selected == null) {
            statusLabel.setText("❌ Sélectionnez une réclamation à modifier");
            return;
        }

        try {
            selected.setType(typeChoice.getValue());
            selected.setDescription(messageField.getText());

            service.updateOne(selected);

            afficherReclamations();
            statusLabel.setText("✅ Réclamation modifiée");

        } catch (SQLException e) {
            statusLabel.setText("❌ Erreur modification");
            e.printStackTrace();
        }
    }

    @FXML
    private void viderFormulaire() {
        typeChoice.setValue(null);
        messageField.clear();
        statusLabel.setText("Formulaire réinitialisé");
    }

    @FXML
    private void supprimerReclamation() {
        Reclamation selected = tableReclamation.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("❌ Veuillez sélectionner une réclamation à supprimer");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer la réclamation");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer cette réclamation ?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                service.deleteOne(selected.getId());
                afficherReclamations();
                statusLabel.setText("✅ Réclamation supprimée");
            } catch (SQLException e) {
                statusLabel.setText("❌ Erreur lors de la suppression");
                e.printStackTrace();
            }
        }
    }
    @FXML private TextField searchField;
    @FXML
    private void rechercherReclamation() {

        String keyword = searchField.getText();

        if (keyword == null || keyword.isEmpty()) {
            afficherReclamations();
            return;
        }

        try {
            tableReclamation.setItems(
                    FXCollections.observableArrayList(
                            service.rechercherParMotCle(keyword)
                    )
            );
        } catch (SQLException e) {
            statusLabel.setText("❌ Erreur recherche");
            e.printStackTrace();
        }
    }

}