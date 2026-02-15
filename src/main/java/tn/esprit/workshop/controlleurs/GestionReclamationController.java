package tn.esprit.workshop.controlleurs;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import tn.esprit.workshop.model.Reclamation;
import java.util.List;           // ← Pour List
import java.util.ArrayList;      // ← Pour ArrayList si nécessaire
import java.util.Optional;       // ← Pour Optional
import tn.esprit.workshop.model.Reponse;
import tn.esprit.workshop.services.ReclamationService;
import tn.esprit.workshop.services.ReponseService;

import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class GestionReclamationController implements Initializable {

    @FXML private ChoiceBox<String> typeChoice;
    @FXML private TextArea messageField;
    @FXML private TextArea reponseArea;
    @FXML private TextField searchField;
    @FXML private Label statusLabel;
    @FXML private Label statutReponseLabel;  // ✅ Maintenant reconnu car dans le FXML

    @FXML private TableView<Reclamation> tableReclamation;
    @FXML private TableColumn<Reclamation, Integer> colId;
    @FXML private TableColumn<Reclamation, String> colType;
    @FXML private TableColumn<Reclamation, String> colDescription;
    @FXML private TableColumn<Reclamation, String> colStatut;
    @FXML private TableColumn<Reclamation, Timestamp> colDate;

    private final ReclamationService service = new ReclamationService();
    private final ReponseService reponseService = new ReponseService();

    // ⚠️ Suggestion : Rendre 'final' ou convertir en variable locale
    private final int currentUserId = 1;  // ✅ Ajout de 'final' pour suivre la suggestion

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // Types de réclamation
        typeChoice.getItems().addAll("Bus", "École", "Chauffeur", "Cantine", "Trajet", "Autre");

        // Configuration des colonnes
        configurerColonnes();

        // Style conditionnel pour le statut
        configurerStyleStatut();

        // Listener pour la sélection
        tableReclamation.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        typeChoice.setValue(newSelection.getType());
                        messageField.setText(newSelection.getDescription());
                        chargerReponse(newSelection.getId());
                    }
                }
        );

        // Charger les réclamations
        afficherReclamations();
    }
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // ================= CONFIGURATION =================

    private void configurerColonnes() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateReclamation"));

        // Format de la date
        colDate.setCellFactory(column -> new TableCell<>() {
            private final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");

            @Override
            protected void updateItem(Timestamp item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : format.format(item));
            }
        });
    }

    private void configurerStyleStatut() {
        colStatut.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "EN_ATTENTE" ->
                                setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                        case "TRAITEE" ->
                                setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                        default ->
                                setStyle("");
                    }
                }
            }
        });
    }

    // ================= CHARGER REPONSE =================

    private void chargerReponse(int reclamationId) {
        try {
            Reponse reponse = reponseService.getReponseByReclamationId(reclamationId);

            if (reponse != null) {
                // ✅ Afficher uniquement le message
                reponseArea.setText(reponse.getMessage());

                // ✅ Mettre à jour le label de statut avec la date
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                statutReponseLabel.setText("Répondu le " + reponse.getDate().format(formatter));

                // Changer la couleur du point (optionnel)
                // Vous pouvez aussi changer la couleur du point ici si vous avez un autre label

            } else {
                reponseArea.setText("Aucune réponse pour le moment...");
                statutReponseLabel.setText("En attente de réponse");
            }
        } catch (SQLException e) {
            reponseArea.setText("Erreur chargement réponse");
            statutReponseLabel.setText("Indisponible");
            e.printStackTrace();
        }
    }

    // ================= CRUD =================

    @FXML
    private void envoyerReclamation() {
        String type = typeChoice.getValue();
        String message = messageField.getText();

        // ✅ Validation du type
        if (type == null || type.isEmpty()) {
            showAlert("Erreur de saisie", "❌ Veuillez sélectionner un type de réclamation !", Alert.AlertType.WARNING);
            return;
        }

        // ✅ Validation du message vide
        if (message == null || message.trim().isEmpty()) {
            showAlert("Erreur de saisie", "❌ Le message ne peut pas être vide !", Alert.AlertType.WARNING);
            messageField.requestFocus();
            return;
        }

        // ✅ Validation de la longueur minimale
        if (message.trim().length() < 10) {
            showAlert("Erreur de saisie", "❌ Le message doit contenir au moins 10 caractères !", Alert.AlertType.WARNING);
            messageField.requestFocus();
            return;
        }

        // ✅ Validation de la longueur maximale
        if (message.trim().length() > 500) {
            showAlert("Erreur de saisie", "❌ Le message ne peut pas dépasser 500 caractères !", Alert.AlertType.WARNING);
            messageField.requestFocus();
            return;
        }

        // ✅ Validation des caractères spéciaux (optionnel)
        if (message.matches(".*[<>{}].*")) {
            showAlert("Erreur de saisie", "❌ Le message contient des caractères non autorisés !", Alert.AlertType.WARNING);
            return;
        }

        // ✅ Alerte de confirmation avant envoi
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Envoyer la réclamation");
        confirm.setContentText("Voulez-vous vraiment envoyer cette réclamation ?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                service.ajouterReclamationEtRetournerId(
                        currentUserId,
                        type,
                        message.trim(),
                        "EN_ATTENTE"
                );

                showAlert("Succès", "✅ Réclamation envoyée avec succès !", Alert.AlertType.INFORMATION);
                viderFormulaire();
                afficherReclamations();

            } catch (SQLException e) {
                showAlert("Erreur", "❌ Erreur lors de l'envoi : " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void modifierReclamation() {
        Reclamation selected = tableReclamation.getSelectionModel().getSelectedItem();
        String type = typeChoice.getValue();
        String message = messageField.getText();

        // ✅ Validation de la sélection
        if (selected == null) {
            showAlert("Erreur", "❌ Veuillez sélectionner une réclamation à modifier !", Alert.AlertType.WARNING);
            return;
        }

        // ✅ Validation du type
        if (type == null || type.isEmpty()) {
            showAlert("Erreur", "❌ Veuillez sélectionner un type !", Alert.AlertType.WARNING);
            return;
        }

        // ✅ Validation du message
        if (message == null || message.trim().isEmpty()) {
            showAlert("Erreur", "❌ Le message ne peut pas être vide !", Alert.AlertType.WARNING);
            return;
        }

        // ✅ Validation de la longueur
        if (message.trim().length() < 10) {
            showAlert("Erreur", "❌ Le message doit contenir au moins 10 caractères !", Alert.AlertType.WARNING);
            return;
        }

        // ✅ Vérifier si des modifications ont été apportées
        if (selected.getType().equals(type) && selected.getDescription().equals(message.trim())) {
            showAlert("Information", "ℹ️ Aucune modification détectée", Alert.AlertType.INFORMATION);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Modifier la réclamation");
        confirm.setContentText("Voulez-vous vraiment modifier cette réclamation ?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                selected.setType(type);
                selected.setDescription(message.trim());

                service.updateOne(selected);

                showAlert("Succès", "✅ Réclamation modifiée avec succès !", Alert.AlertType.INFORMATION);
                afficherReclamations();

            } catch (SQLException e) {
                showAlert("Erreur", "❌ Erreur modification : " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void supprimerReclamation() {
        Reclamation selected = tableReclamation.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("Erreur", "❌ Veuillez sélectionner une réclamation à supprimer !", Alert.AlertType.WARNING);
            return;
        }

        // ✅ Alerte avec plus de détails
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de suppression");
        confirm.setHeaderText("Supprimer la réclamation #" + selected.getId());
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer cette réclamation ?\nCette action est irréversible.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                // Vérifier s'il y a une réponse associée
                Reponse reponse = reponseService.getReponseByReclamationId(selected.getId());
                if (reponse != null) {
                    Alert info = new Alert(Alert.AlertType.INFORMATION);
                    info.setTitle("Information");
                    info.setHeaderText("Réponse associée");
                    info.setContentText("Cette réclamation a une réponse qui sera également supprimée.");
                    info.showAndWait();

                    reponseService.delete(reponse.getId());
                }

                service.deleteOne(selected.getId());

                showAlert("Succès", "✅ Réclamation supprimée avec succès !", Alert.AlertType.INFORMATION);
                afficherReclamations();
                viderFormulaire();

            } catch (SQLException e) {
                showAlert("Erreur", "❌ Erreur suppression : " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    // ================= RECHERCHE =================

    @FXML
    private void rechercherReclamation() {
        String keyword = searchField.getText();

        // ✅ Validation du mot-clé
        if (keyword == null || keyword.trim().isEmpty()) {
            showAlert("Information", "ℹ️ Veuillez entrer un mot-clé pour la recherche", Alert.AlertType.INFORMATION);
            afficherReclamations();  // Affiche toutes les réclamations
            statusLabel.setText("Affichage de toutes les réclamations");
            return;
        }

        // ✅ Validation de la longueur du mot-clé
        if (keyword.trim().length() < 2) {
            showAlert("Information", "ℹ️ Le mot-clé doit contenir au moins 2 caractères", Alert.AlertType.INFORMATION);
            searchField.requestFocus();
            return;
        }

        try {
            // Récupérer les résultats de la recherche
            List<Reclamation> resultats = service.rechercherParMotCle(keyword.trim());

            // Mettre à jour le message de statut
            if (resultats.isEmpty()) {
                showAlert("Résultat", "ℹ️ Aucune réclamation trouvée pour : " + keyword, Alert.AlertType.INFORMATION);
                statusLabel.setText("🔍 Aucun résultat pour : " + keyword);
            } else {
                statusLabel.setText("🔍 " + resultats.size() + " résultat(s) pour : " + keyword);
            }

            // Afficher les résultats dans la table
            tableReclamation.setItems(FXCollections.observableArrayList(resultats));

        } catch (SQLException e) {
            showAlert("Erreur", "❌ Erreur lors de la recherche : " + e.getMessage(), Alert.AlertType.ERROR);
            statusLabel.setText("❌ Erreur recherche");
            e.printStackTrace();
        }
    }
    // ================= AFFICHAGE =================

    private void afficherReclamations() {
        try {
            tableReclamation.setItems(
                    FXCollections.observableArrayList(service.selectAll())
            );
        } catch (SQLException e) {
            statusLabel.setText("❌ Erreur chargement");
            e.printStackTrace();
        }
    }

    // ================= RESET =================

    @FXML
    private void viderFormulaire() {
        typeChoice.setValue(null);
        messageField.clear();
        reponseArea.clear();
        statutReponseLabel.setText("En attente de réponse");
        tableReclamation.getSelectionModel().clearSelection();
        searchField.clear();
        statusLabel.setText("Formulaire réinitialisé");
    }
}