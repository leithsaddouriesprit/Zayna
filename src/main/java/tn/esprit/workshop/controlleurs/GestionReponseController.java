package tn.esprit.workshop.controlleurs;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import tn.esprit.workshop.model.Reclamation;
import tn.esprit.workshop.services.ReclamationService;
import tn.esprit.workshop.services.ReponseService;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class GestionReponseController {

    // Table des réclamations
    @FXML private TableView<Reclamation> tableReclamation;
    @FXML private TableColumn<Reclamation, Integer> colId;
    @FXML private TableColumn<Reclamation, String> colType;
    @FXML private TableColumn<Reclamation, String> colDescription;
    @FXML private TableColumn<Reclamation, String> colStatut;
    @FXML private TableColumn<Reclamation, Timestamp> colDate;

    // Détails de la réclamation
    @FXML private Label detailIdLabel;
    @FXML private Label detailTypeLabel;
    @FXML private TextArea detailMessageArea;
    @FXML private Label detailDateLabel;

    // Section réponse existante
    @FXML private VBox reponseExistanteBox;
    @FXML private TextArea reponseExistanteArea;
    @FXML private Label reponseDateLabel;

    // Formulaire de réponse
    @FXML private TextArea reponseField;
    @FXML private TextField searchField;
    @FXML private Label statusLabel;

    private final ReclamationService reclamationService = new ReclamationService();
    private final ReponseService reponseService = new ReponseService();

    private Reclamation reclamationSelectionnee;
    private Reponse reponseExistante;

    @FXML
    public void initialize() {
        configurerColonnes();
        configurerListenerSelection();

        // ✅ CHANGEMENT 1 : Afficher TOUTES les réclamations au démarrage
        afficherToutesReclamations();
        statusLabel.setText("Affichage de toutes les réclamations");
        reponseField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 500) {
                reponseField.setText(oldValue);
                statusLabel.setText("⚠️ Réponse trop longue (max 500 caractères)");
            } else if (newValue.length() < 5 && newValue.length() > 0) {
                statusLabel.setText("⚠️ Minimum 5 caractères requis");
            } else if (newValue.length() > 0) {
                statusLabel.setText("✓ " + newValue.length() + "/500 caractères");
            } else {
                statusLabel.setText("");
            }
        });
    }
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    private void configurerColonnes() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Configuration de la colonne date
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

        // ✅ AJOUT : Style conditionnel pour le statut
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
    }

    private void configurerListenerSelection() {
        tableReclamation.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        reclamationSelectionnee = newSelection;
                        afficherDetailsReclamation(newSelection);
                        chargerReponseExistante(newSelection.getId());
                    }
                }
        );
    }

    private void afficherDetailsReclamation(Reclamation r) {
        if (detailIdLabel != null) detailIdLabel.setText(String.valueOf(r.getId()));
        if (detailTypeLabel != null) detailTypeLabel.setText(r.getType());
        if (detailMessageArea != null) detailMessageArea.setText(r.getDescription());

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        if (r.getDateReclamation() != null && detailDateLabel != null) {
            detailDateLabel.setText(format.format(r.getDateReclamation()));
        }
    }

    private void chargerReponseExistante(int reclamationId) {
        try {
            reponseExistante = reponseService.getByReclamationId(reclamationId);

            if (reponseExistante != null && reponseExistanteBox != null) {
                // Afficher la réponse existante
                if (reponseExistanteArea != null) {
                    reponseExistanteArea.setText(reponseExistante.getMessage());
                }

                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                if (reponseDateLabel != null) {
                    reponseDateLabel.setText("Répondu le: " +
                            format.format(Timestamp.valueOf(reponseExistante.getDate())));
                }

                // Rendre visible la boîte de réponse existante
                reponseExistanteBox.setManaged(true);
                reponseExistanteBox.setVisible(true);

                // Pré-remplir le champ de réponse
                if (reponseField != null) {
                    reponseField.setText(reponseExistante.getMessage());
                }

                statusLabel.setText("✅ Réponse existante chargée");

            } else if (reponseExistanteBox != null) {
                // Cacher la boîte de réponse existante
                reponseExistanteBox.setManaged(false);
                reponseExistanteBox.setVisible(false);

                if (reponseField != null) {
                    reponseField.clear();
                }

                statusLabel.setText("Aucune réponse pour cette réclamation");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("❌ Erreur lors du chargement de la réponse");
        }
    }

    @FXML
    private void repondreReclamation() {
        // ✅ Validation de la sélection
        if (reclamationSelectionnee == null) {
            showAlert("Erreur", "❌ Veuillez sélectionner une réclamation !", Alert.AlertType.WARNING);
            return;
        }

        String reponseTexte = reponseField.getText();

        // ✅ Validation du champ vide
        if (reponseTexte == null || reponseTexte.trim().isEmpty()) {
            showAlert("Erreur", "❌ Veuillez écrire une réponse !", Alert.AlertType.WARNING);
            reponseField.requestFocus();
            return;
        }

        // ✅ Validation de la longueur minimale
        if (reponseTexte.trim().length() < 5) {
            showAlert("Erreur", "❌ La réponse doit contenir au moins 5 caractères !", Alert.AlertType.WARNING);
            reponseField.requestFocus();
            return;
        }

        // ✅ Validation de la longueur maximale
        if (reponseTexte.trim().length() > 500) {
            showAlert("Erreur", "❌ La réponse ne peut pas dépasser 500 caractères !", Alert.AlertType.WARNING);
            reponseField.requestFocus();
            return;
        }

        try {
            if (reponseExistante != null) {
                // Demander confirmation pour écraser
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Confirmation");
                confirm.setHeaderText("Une réponse existe déjà");
                confirm.setContentText("Voulez-vous remplacer la réponse existante ?");

                Optional<ButtonType> result = confirm.showAndWait();
                if (result.isPresent() && result.get() != ButtonType.OK) {
                    return;
                }

                // Mettre à jour la réponse existante
                reponseExistante.setMessage(reponseTexte.trim());
                reponseExistante.setDate(LocalDateTime.now());
                reponseService.update(reponseExistante);
                showAlert("Succès", "✅ Réponse modifiée avec succès !", Alert.AlertType.INFORMATION);

            } else {
                // Créer une nouvelle réponse
                Reponse nouvelleReponse = new Reponse(
                        reclamationSelectionnee.getId(),
                        reponseTexte.trim(),
                        LocalDateTime.now()
                );
                reponseService.insertOne(nouvelleReponse);
                showAlert("Succès", "✅ Réponse envoyée avec succès !", Alert.AlertType.INFORMATION);
            }

            // Mettre à jour le statut de la réclamation
            reclamationSelectionnee.setStatut("TRAITEE");
            reclamationService.updateOne(reclamationSelectionnee);

            // Rafraîchir l'affichage
            afficherToutesReclamations();
            chargerReponseExistante(reclamationSelectionnee.getId());

        } catch (SQLException e) {
            showAlert("Erreur", "❌ Erreur lors de l'envoi : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
    @FXML
    private void modifierReponse() {
        // ✅ Validation de l'existence d'une réponse
        if (reponseExistante == null) {
            showAlert("Erreur", "❌ Aucune réponse à modifier !", Alert.AlertType.WARNING);
            return;
        }

        String reponseTexte = reponseField.getText();

        // ✅ Validation du champ vide
        if (reponseTexte == null || reponseTexte.trim().isEmpty()) {
            showAlert("Erreur", "❌ La réponse ne peut pas être vide !", Alert.AlertType.WARNING);
            reponseField.requestFocus();
            return;
        }

        // ✅ Validation de la longueur minimale
        if (reponseTexte.trim().length() < 5) {
            showAlert("Erreur", "❌ La réponse doit contenir au moins 5 caractères !", Alert.AlertType.WARNING);
            reponseField.requestFocus();
            return;
        }

        // ✅ Validation de la longueur maximale
        if (reponseTexte.trim().length() > 500) {
            showAlert("Erreur", "❌ La réponse ne peut pas dépasser 500 caractères !", Alert.AlertType.WARNING);
            reponseField.requestFocus();
            return;
        }

        // ✅ Vérifier si des modifications ont été apportées
        if (reponseExistante.getMessage().equals(reponseTexte.trim())) {
            showAlert("Information", "ℹ️ Aucune modification détectée", Alert.AlertType.INFORMATION);
            return;
        }

        // ✅ Demander confirmation
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Modifier la réponse");
        confirm.setContentText("Voulez-vous vraiment modifier cette réponse ?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                reponseExistante.setMessage(reponseTexte.trim());
                reponseExistante.setDate(LocalDateTime.now());
                reponseService.update(reponseExistante);

                showAlert("Succès", "✅ Réponse modifiée avec succès !", Alert.AlertType.INFORMATION);
                chargerReponseExistante(reclamationSelectionnee.getId());

            } catch (SQLException e) {
                showAlert("Erreur", "❌ Erreur lors de la modification : " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void supprimerReponse() {
        // ✅ Validation de l'existence d'une réponse
        if (reponseExistante == null) {
            showAlert("Erreur", "❌ Aucune réponse à supprimer !", Alert.AlertType.WARNING);
            return;
        }

        // ✅ Demander confirmation avec plus de détails
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de suppression");
        confirm.setHeaderText("Supprimer la réponse");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer cette réponse ?\nCette action est irréversible.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                reponseService.delete(reponseExistante.getId());

                // Remettre la réclamation en attente
                reclamationSelectionnee.setStatut("EN_ATTENTE");
                reclamationService.updateOne(reclamationSelectionnee);

                showAlert("Succès", "✅ Réponse supprimée avec succès !", Alert.AlertType.INFORMATION);

                // Rafraîchir
                afficherToutesReclamations();
                reponseExistante = null;

                if (reponseExistanteBox != null) {
                    reponseExistanteBox.setManaged(false);
                    reponseExistanteBox.setVisible(false);
                }

                if (reponseField != null) {
                    reponseField.clear();
                }

            } catch (SQLException e) {
                showAlert("Erreur", "❌ Erreur lors de la suppression : " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }
    @FXML
    private void rechercherReponse() {
        String keyword = searchField.getText().trim();

        // ✅ Validation du mot-clé vide
        if (keyword.isEmpty()) {
            afficherToutesReclamations();
            statusLabel.setText("Affichage de toutes les réclamations");
            return;
        }

        // ✅ Validation de la longueur minimale
        if (keyword.length() < 2) {
            showAlert("Information", "ℹ️ Le mot-clé doit contenir au moins 2 caractères", Alert.AlertType.INFORMATION);
            searchField.requestFocus();
            return;
        }

        try {
            List<Reclamation> resultats = reclamationService.rechercherParMotCle(keyword);

            // ✅ Message selon les résultats
            if (resultats.isEmpty()) {
                showAlert("Résultat", "ℹ️ Aucune réclamation trouvée pour : " + keyword, Alert.AlertType.INFORMATION);
                statusLabel.setText("🔍 Aucun résultat pour : " + keyword);
            } else {
                statusLabel.setText("🔍 " + resultats.size() + " résultat(s) pour : " + keyword);
            }

            tableReclamation.setItems(FXCollections.observableArrayList(resultats));

        } catch (SQLException e) {
            showAlert("Erreur", "❌ Erreur recherche : " + e.getMessage(), Alert.AlertType.ERROR);
            statusLabel.setText("❌ Erreur recherche");
            e.printStackTrace();
        }
    }

    @FXML
    private void reinitialiserRecherche() {
        searchField.clear();
        // ✅ CHANGEMENT 5 : Réinitialiser avec TOUTES les réclamations
        afficherToutesReclamations();
        statusLabel.setText("Affichage de toutes les réclamations");
    }

    // ✅ NOUVELLE MÉTHODE : Afficher toutes les réclamations
    private void afficherToutesReclamations() {
        try {
            List<Reclamation> reclamations = reclamationService.selectAll(); // Toutes les réclamations
            tableReclamation.setItems(FXCollections.observableArrayList(reclamations));

            if (!reclamations.isEmpty()) {
                tableReclamation.getSelectionModel().selectFirst();
            } else {
                statusLabel.setText("Aucune réclamation disponible");
                if (reponseExistanteBox != null) {
                    reponseExistanteBox.setManaged(false);
                    reponseExistanteBox.setVisible(false);
                }
            }

        } catch (SQLException e) {
            statusLabel.setText("❌ Erreur chargement des réclamations");
            e.printStackTrace();
        }
    }

    // ✅ ANCIENNE MÉTHODE (conservée mais plus utilisée)
    private void afficherReclamationsEnAttente() {
        try {
            List<Reclamation> reclamations = reclamationService.rechercherParStatut("EN_ATTENTE");
            tableReclamation.setItems(FXCollections.observableArrayList(reclamations));

            if (!reclamations.isEmpty()) {
                tableReclamation.getSelectionModel().selectFirst();
            } else {
                statusLabel.setText("Aucune réclamation en attente");
                if (reponseExistanteBox != null) {
                    reponseExistanteBox.setManaged(false);
                    reponseExistanteBox.setVisible(false);
                }
            }

        } catch (SQLException e) {
            statusLabel.setText("❌ Erreur chargement des réclamations");
            e.printStackTrace();
        }
    }

    private boolean validerSelectionEtReponse() {
        if (reclamationSelectionnee == null) {
            statusLabel.setText("❌ Veuillez sélectionner une réclamation !");
            return false;
        }

        if (reponseField.getText().trim().isEmpty()) {
            statusLabel.setText("❌ Veuillez écrire une réponse !");
            return false;
        }

        return true;
    }
}