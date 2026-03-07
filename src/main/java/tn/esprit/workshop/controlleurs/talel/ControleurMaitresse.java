package tn.esprit.workshop.controlleurs.talel;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.workshop.model.talel.Maitresse;
import tn.esprit.workshop.model.talel.User;
import tn.esprit.workshop.services.talel.ServiceMaitresse;

import java.util.Optional;

public class ControleurMaitresse {

    @FXML
    private TextField txtNom;
    @FXML
    private TextField txtEmail;
    @FXML
    private PasswordField txtMotDePasse;
    @FXML
    private TextField txtClasseResponsable;
    @FXML
    private TextField txtDiplome;
    @FXML
    private TextField txtSalaire;
    @FXML
    private TextField txtTelephone;
    @FXML
    private TextArea txtAdresse;
    @FXML
    private Label lblMessage;
    @FXML
    private Button logoutButton;
    @FXML
    private Button btnAjouter; // Ajoutez ceci dans votre FXML

    private ServiceMaitresse serviceMaitresse;
    private User connectedUser;
    private boolean isProcessing = false; // Protection anti-double clic

    @FXML
    public void initialize() {
        serviceMaitresse = new ServiceMaitresse();
        System.out.println("=== ControleurMaitresse initialisé ===");
    }

    @FXML
    private void ajouterMaitresse() {
        // Protection contre les clics multiples
        if (isProcessing) {
            System.out.println("⏳ Traitement en cours, clic ignoré");
            return;
        }

        try {
            isProcessing = true;
            desactiverBoutons(true);

            System.out.println("=== ControleurMaitresse.ajouterMaitresse ===");

            // Récupération des données
            String nom = txtNom.getText().trim();
            String email = txtEmail.getText().trim().toLowerCase();
            String passwordClair = txtMotDePasse.getText();
            String classeResponsable = txtClasseResponsable.getText().trim();
            String diplome = txtDiplome.getText().trim();
            String telephone = txtTelephone.getText().trim();
            String adresse = txtAdresse.getText().trim();
            String salaireText = txtSalaire.getText().trim();

            // AFFICHAGE DES VALEURS POUR DÉBOGAGE
            System.out.println("=== VALEURS SAISIES ===");
            System.out.println("   Nom: '" + nom + "'");
            System.out.println("   Email: '" + email + "'");
            System.out.println("   Classe: '" + classeResponsable + "'");
            System.out.println("   Diplôme: '" + diplome + "'");
            System.out.println("   Salaire: '" + salaireText + "'");

            // Validation des champs obligatoires
            if (nom.isEmpty() || email.isEmpty() || passwordClair.isEmpty() || classeResponsable.isEmpty()) {
                afficherMessage("Veuillez remplir tous les champs obligatoires", "warning");
                return;
            }

            // Validation email
            if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
                afficherMessage("Format d'email invalide", "error");
                return;
            }

            // Validation téléphone
            if (!telephone.isEmpty() && !telephone.matches("\\d{8}")) {
                afficherMessage("Le téléphone doit contenir 8 chiffres", "error");
                return;
            }

            // Validation mot de passe
            if (passwordClair.length() < 6) {
                afficherMessage("Le mot de passe doit contenir au moins 6 caractères", "error");
                return;
            }
            if (passwordClair.length() > 30) {
                afficherMessage("Le mot de passe ne doit pas dépasser 30 caractères", "error");
                return;
            }

            // VALIDATION DU SALAIRE - MAINTENANT OBLIGATOIRE
            if (salaireText.isEmpty()) {
                afficherMessage("Le salaire est obligatoire", "error");
                return;
            }

            double salaire;
            try {
                salaire = Double.parseDouble(salaireText);
                if (salaire < 0) {
                    afficherMessage("Le salaire ne peut pas être négatif", "error");
                    return;
                }
            } catch (NumberFormatException e) {
                afficherMessage("Le salaire doit être un nombre valide", "error");
                return;
            }

            // Création de l'objet Maitresse
            Maitresse maitresse = new Maitresse();
            maitresse.setNom(nom);
            maitresse.setEmail(email);
            maitresse.setClasseResponsable(classeResponsable);
            maitresse.setDiplome(diplome.isEmpty() ? null : diplome);
            maitresse.setSalaire(salaire); // SALAIRE TOUJOURS DÉFINI
            maitresse.setTelephone(telephone.isEmpty() ? null : telephone);
            maitresse.setAdresse(adresse.isEmpty() ? null : adresse);

            System.out.println("✅ Données validées, appel du service...");
            System.out.println("   Salaire: " + maitresse.getSalaire());

            // Appel au service
            serviceMaitresse.ajouterMaitresse(maitresse, passwordClair);

            afficherMessage("✅ Maîtresse ajoutée avec succès ! ID: " + maitresse.getId(), "success");
            viderFormulaire();

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
        if (btnAjouter != null) {
            btnAjouter.setDisable(desactiver);
            btnAjouter.setText(desactiver ? "Traitement..." : "Ajouter");
        }
        logoutButton.setDisable(desactiver);
    }

    @FXML
    private void viderFormulaire() {
        txtNom.clear();
        txtEmail.clear();
        txtMotDePasse.clear();
        txtClasseResponsable.clear();
        txtDiplome.clear();
        txtTelephone.clear();
        txtAdresse.clear();
        txtSalaire.clear();
        afficherMessage("Formulaire vidé", "info");
    }

    private void afficherMessage(String message, String type) {
        lblMessage.setText(message);
        lblMessage.getStyleClass().removeAll("success", "error", "warning", "info");
        lblMessage.getStyleClass().add(type);

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

        lblMessage.setVisible(true);

        // Cache le message après 5 secondes
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                javafx.application.Platform.runLater(() -> lblMessage.setVisible(false));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void setUser(User user) {
        this.connectedUser = user;
        System.out.println("Utilisateur connecté: " + (user != null ? user.getEmail() : "null"));
    }

    @FXML
    private void handleLogoutButton() {
        System.out.println("=== Déconnexion ===");

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Déconnexion");
        confirmation.setHeaderText("Confirmation de déconnexion");
        confirmation.setContentText("Êtes-vous sûr de vouloir vous déconnecter ?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ConnecterUser.fxml"));
                Parent root = loader.load();

                Scene currentScene = logoutButton.getScene();
                Stage stage = (Stage) currentScene.getWindow();

                stage.setScene(new Scene(root));
                stage.setTitle("Connexion - Système de Gestion");
                stage.centerOnScreen();

                System.out.println("✅ Déconnexion réussie");
            } catch (Exception e) {
                System.err.println("❌ Erreur lors de la déconnexion:");
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Erreur lors de la déconnexion",
                        "Impossible de retourner à la page de connexion: " + e.getMessage());
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}