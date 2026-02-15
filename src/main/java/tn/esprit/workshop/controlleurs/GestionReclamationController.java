package tn.esprit.workshop.controlleurs;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import tn.esprit.workshop.model.Reclamation;
import tn.esprit.workshop.services.ReclamationService;
import tn.esprit.workshop.services.ReponseService;

import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;

public class GestionReclamationController implements Initializable {

    @FXML private ChoiceBox<String> typeChoice;
    @FXML private TextArea messageField;
    @FXML private TextArea reponseArea;
    @FXML private TextField searchField;
    @FXML private Label statusLabel;

    @FXML private TableView<Reclamation> tableReclamation;
    @FXML private TableColumn<Reclamation, Integer> colId;
    @FXML private TableColumn<Reclamation, String> colType;
    @FXML private TableColumn<Reclamation, String> colDescription;
    @FXML private TableColumn<Reclamation, String> colStatut;
    @FXML private TableColumn<Reclamation, Timestamp> colDate;

    private final ReclamationService service = new ReclamationService();
    private final ReponseService reponseService = new ReponseService();

    private int currentUserId = 1; // plus tard → user connecté

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // Types
        typeChoice.getItems().addAll("Bus", "École", "Chauffeur", "Cantine", "Trajet", "Autre");

        // Colonnes
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateReclamation"));

        // Format date
        colDate.setCellFactory(column -> new TableCell<>() {
            private final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");

            @Override
            protected void updateItem(Timestamp item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : format.format(item));
            }
        });

        // Style statut
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

        // 🔥 Quand on sélectionne une ligne
        tableReclamation.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        typeChoice.setValue(newSelection.getType());
                        messageField.setText(newSelection.getDescription());
                        chargerReponse(newSelection.getId());
                    }
                }
        );

        afficherReclamations();
    }

    // ================= AJOUT =================

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

    // ================= MODIFIER =================

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

    // ================= SUPPRIMER =================

    @FXML
    private void supprimerReclamation() {

        Reclamation selected = tableReclamation.getSelectionModel().getSelectedItem();

        if (selected == null) {
            statusLabel.setText("❌ Sélectionnez une réclamation");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText("Confirmer suppression");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                ReclamationService.deleteOne(selected.getId());
                statusLabel.setText("✅ Supprimée");
                afficherReclamations();
                viderFormulaire();
            } catch (SQLException e) {
                statusLabel.setText("❌ Erreur suppression");
            }
        }
    }

    // ================= RECHERCHE =================

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
        }
    }

    // ================= AFFICHER =================

    private void afficherReclamations() {
        try {
            tableReclamation.setItems(
                    FXCollections.observableArrayList(service.selectAll())
            );
        } catch (SQLException e) {
            statusLabel.setText("❌ Erreur chargement");
        }
    }

    // ================= REPONSE =================

    private void chargerReponse(int reclamationId) {
        try {
            String reponse = String.valueOf(reponseService.getReponseByReclamationId(reclamationId));
            reponseArea.setText(
                    reponse != null ? reponse : "Aucune réponse pour le moment..."
            );
        } catch (SQLException e) {
            reponseArea.setText("Erreur chargement réponse");
        }
    }

    // ================= RESET =================

    @FXML
    private void viderFormulaire() {
        typeChoice.setValue(null);
        messageField.clear();
        reponseArea.clear();
        tableReclamation.getSelectionModel().clearSelection();
    }
}