package tn.esprit.workshop.controlleurs;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import tn.esprit.workshop.model.Reclamation;
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

        if (type == null || type.isEmpty()) {
            statusLabel.setText("❌ Sélectionnez un type !");
            return;
        }

        if (message == null || message.trim().isEmpty()) {
            statusLabel.setText("❌ Message vide !");
            return;
        }

        try {
            service.ajouterReclamationEtRetournerId(
                    currentUserId,
                    type,
                    message.trim(),
                    "EN_ATTENTE"
            );

            statusLabel.setText("✅ Réclamation envoyée");
            viderFormulaire();
            afficherReclamations();

        } catch (SQLException e) {
            statusLabel.setText("❌ Erreur ajout");
            e.printStackTrace();
        }
    }

    @FXML
    private void modifierReclamation() {
        Reclamation selected = tableReclamation.getSelectionModel().getSelectedItem();

        if (selected == null) {
            statusLabel.setText("❌ Sélectionnez une réclamation");
            return;
        }

        try {
            selected.setType(typeChoice.getValue());
            selected.setDescription(messageField.getText());

            service.updateOne(selected);

            statusLabel.setText("✅ Réclamation modifiée");
            afficherReclamations();

        } catch (SQLException e) {
            statusLabel.setText("❌ Erreur modification");
            e.printStackTrace();
        }
    }

    @FXML
    private void supprimerReclamation() {
        Reclamation selected = tableReclamation.getSelectionModel().getSelectedItem();

        if (selected == null) {
            statusLabel.setText("❌ Sélectionnez une réclamation");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer la réclamation");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer cette réclamation ?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                // Vérifier s'il y a une réponse associée
                Reponse reponse = reponseService.getReponseByReclamationId(selected.getId());
                if (reponse != null) {
                    reponseService.delete(reponse.getId());
                }

                service.deleteOne(selected.getId());
                statusLabel.setText("✅ Réclamation supprimée");
                afficherReclamations();
                viderFormulaire();

            } catch (SQLException e) {
                statusLabel.setText("❌ Erreur suppression");
                e.printStackTrace();
            }
        }
    }

    // ================= RECHERCHE =================

    @FXML
    private void rechercherReclamation() {
        String keyword = searchField.getText();

        if (keyword == null || keyword.trim().isEmpty()) {
            afficherReclamations();
            return;
        }

        try {
            tableReclamation.setItems(
                    FXCollections.observableArrayList(
                            service.rechercherParMotCle(keyword.trim())
                    )
            );
            statusLabel.setText("🔍 Résultats pour : " + keyword);
        } catch (SQLException e) {
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