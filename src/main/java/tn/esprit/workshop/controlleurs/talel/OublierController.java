package tn.esprit.workshop.controlleurs.talel;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.workshop.services.talel.ServiceOublier;

import java.io.IOException;
import java.util.Optional;

public class OublierController {

    @FXML
    private TextField txtNom;
    @FXML
    private TextField txtTelephone;
    @FXML
    private PasswordField txtNouveauMotDePasse;
    @FXML
    private PasswordField txtConfirmerMotDePasse;
    @FXML
    private Button btnReinitialiser;
    @FXML
    private Button btnRetour;
    @FXML
    private Button btnEffacer;
    @FXML
    private Label lblMessage;

    private ServiceOublier serviceOublier;
    private boolean isProcessing = false; // Protection anti-double clic

    public OublierController() {
        this.serviceOublier = new ServiceOublier();
    }

    @FXML
    public void initialize() {
        System.out.println("=== OublierController initialisé ===");

        // Vérifier que tous les champs sont injectés
        verifierChamps();

        if (lblMessage != null) {
            lblMessage.setVisible(false);
        }
    }

    private void verifierChamps() {
        System.out.println("Vérification des champs FXML:");
        System.out.println("   txtNom: " + (txtNom != null ? "✅ OK" : "❌ NULL"));
        System.out.println("   txtTelephone: " + (txtTelephone != null ? "✅ OK" : "❌ NULL"));
        System.out.println("   txtNouveauMotDePasse: " + (txtNouveauMotDePasse != null ? "✅ OK" : "❌ NULL"));
        System.out.println("   txtConfirmerMotDePasse: " + (txtConfirmerMotDePasse != null ? "✅ OK" : "❌ NULL"));
        System.out.println("   btnReinitialiser: " + (btnReinitialiser != null ? "✅ OK" : "❌ NULL"));
        System.out.println("   btnRetour: " + (btnRetour != null ? "✅ OK" : "❌ NULL"));
        System.out.println("   btnEffacer: " + (btnEffacer != null ? "✅ OK" : "❌ NULL"));
        System.out.println("   lblMessage: " + (lblMessage != null ? "✅ OK" : "❌ NULL"));
    }

    @FXML
    private void handleResetPassword(ActionEvent event) {
        // Protection anti-double clic
        if (isProcessing) {
            System.out.println("⏳ Traitement en cours, clic ignoré");
            return;
        }

        try {
            isProcessing = true;
            desactiverBoutons(true);

            System.out.println("=== OublierController.handleResetPassword ===");

            // Vérification que les champs ne sont pas null (sécurité)
            if (txtNom == null || txtTelephone == null ||
                    txtNouveauMotDePasse == null || txtConfirmerMotDePasse == null) {
                System.err.println("❌ Erreur: Champs FXML non initialisés");
                afficherMessage("Erreur interne de l'application", "error");
                return;
            }

            // Récupération des données
            String nom = txtNom.getText().trim();
            String telephone = txtTelephone.getText().trim().replaceAll("\\s+", "");
            String nouveauPassword = txtNouveauMotDePasse.getText();
            String confirmPassword = txtConfirmerMotDePasse.getText();

            // AFFICHAGE POUR DÉBOGAGE
            System.out.println("   Nom: '" + nom + "'");
            System.out.println("   Téléphone: '" + telephone + "'");
            System.out.println("   Mot de passe: [PROTÉGÉ]");

            // ÉTAPE 1: Validation des champs
            if (nom.isEmpty() || telephone.isEmpty() || nouveauPassword.isEmpty() || confirmPassword.isEmpty()) {
                afficherMessage("Tous les champs sont obligatoires", "error");
                return;
            }

            // ÉTAPE 2: Validation téléphone
            if (!telephone.matches("\\d{8}")) {
                afficherMessage("Le téléphone doit contenir 8 chiffres", "error");
                return;
            }

            // ÉTAPE 3: Vérifier que les mots de passe correspondent
            if (!nouveauPassword.equals(confirmPassword)) {
                afficherMessage("Les mots de passe ne correspondent pas", "error");
                return;
            }

            // ÉTAPE 4: Validation du nouveau mot de passe
            if (nouveauPassword.length() < 6) {
                afficherMessage("Le mot de passe doit contenir au moins 6 caractères", "error");
                return;
            }
            if (nouveauPassword.length() > 30) {
                afficherMessage("Le mot de passe ne doit pas dépasser 30 caractères", "error");
                return;
            }

            System.out.println("🔍 Recherche de l'utilisateur: " + nom + " - " + telephone);

            // ÉTAPE 5: Vérifier si l'utilisateur existe
            if (!serviceOublier.utilisateurExiste(nom, telephone)) {
                afficherMessage("Aucun utilisateur trouvé avec ces informations", "error");
                return;
            }

            System.out.println("✅ Utilisateur trouvé, réinitialisation du mot de passe...");

            // ÉTAPE 6: Réinitialiser le mot de passe
            boolean success = serviceOublier.resetPassword(nom, telephone, nouveauPassword);

            if (success) {
                afficherMessage("✅ Mot de passe réinitialisé avec succès !", "success");
                effacerChamps();

                // Redirection automatique après 2 secondes
                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                        javafx.application.Platform.runLater(() -> {
                            try {
                                handleBackToLogin(event);
                            } catch (Exception e) {
                                System.err.println("❌ Erreur redirection: " + e.getMessage());
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            } else {
                afficherMessage("❌ Échec de la réinitialisation. Veuillez réessayer.", "error");
            }

        } catch (Exception e) {
            System.err.println("=== ERREUR DÉTAILLÉE ===");
            System.err.println("Type: " + e.getClass().getName());
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();

            String errorMsg = e.getMessage() != null ? e.getMessage() : "Erreur inconnue";
            afficherMessage("❌ Erreur : " + errorMsg, "error");
        } finally {
            isProcessing = false;
            desactiverBoutons(false);
        }
    }

    private void desactiverBoutons(boolean desactiver) {
        if (btnReinitialiser != null) {
            btnReinitialiser.setDisable(desactiver);
            btnReinitialiser.setText(desactiver ? "⏳ Traitement..." : "Réinitialiser");
        }
        if (btnRetour != null) {
            btnRetour.setDisable(desactiver);
        }
        if (btnEffacer != null) {
            btnEffacer.setDisable(desactiver);
        }
    }

    @FXML
    private void handleBackToLogin(ActionEvent event) {
        System.out.println("=== Retour à la page de connexion ===");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ConnecterUser.fxml"));
            Parent root = loader.load();

            Scene currentScene = ((Node) event.getSource()).getScene();
            Stage stage = (Stage) currentScene.getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle("Connexion - Système de Gestion");
            stage.centerOnScreen();

            System.out.println("✅ Retour à la connexion réussi");
        } catch (IOException e) {
            System.err.println("❌ Erreur lors du retour:");
            e.printStackTrace();
            afficherMessage("Erreur lors du retour à la page de connexion", "error");
        }
    }

    @FXML
    private void handleClearFields() {
        effacerChamps();
    }

    private void effacerChamps() {
        if (txtNom != null) txtNom.clear();
        if (txtTelephone != null) txtTelephone.clear();
        if (txtNouveauMotDePasse != null) txtNouveauMotDePasse.clear();
        if (txtConfirmerMotDePasse != null) txtConfirmerMotDePasse.clear();
        afficherMessage("Champs effacés", "info");
    }

    private void afficherMessage(String message, String type) {
        if (lblMessage == null) {
            System.err.println("lblMessage est null, impossible d'afficher: " + message);
            return;
        }

        lblMessage.setText(message);
        lblMessage.getStyleClass().removeAll("success", "error", "warning", "info");
        lblMessage.getStyleClass().add(type);
        lblMessage.setVisible(true);

        // Configuration des couleurs
        switch (type) {
            case "success":
                lblMessage.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                break;
            case "error":
                lblMessage.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                break;
            case "warning":
                lblMessage.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                break;
            case "info":
                lblMessage.setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
                break;
        }

        // Cache le message après 5 secondes
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                javafx.application.Platform.runLater(() -> {
                    if (lblMessage != null) {
                        lblMessage.setVisible(false);
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Méthode utilitaire pour afficher une alerte
     */
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}