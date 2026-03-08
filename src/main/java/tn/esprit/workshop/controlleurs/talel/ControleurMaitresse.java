package tn.esprit.workshop.controlleurs.talel;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.application.Platform;
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
    private Button btnAjouter;

    private ServiceMaitresse serviceMaitresse;
    private User connectedUser;
    private boolean isProcessing = false;

    @FXML
    public void initialize() {
        serviceMaitresse = new ServiceMaitresse();

        // Configuration du label message - CORRECTION CRITIQUE
        lblMessage.setVisible(false);
        lblMessage.setManaged(true);  // ← CORRIGÉ
        lblMessage.setWrapText(true); // ← CORRIGÉ

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

            // VALIDATION CHAMP PAR CHAMP avec messages précis et focus

            // 1. Validation du nom
            if (nom.isEmpty()) {
                afficherMessage("❌ Le nom est obligatoire", "error");
                txtNom.requestFocus();
                return;
            }

            // 2. Validation de l'email
            if (email.isEmpty()) {
                afficherMessage("❌ L'email est obligatoire", "error");
                txtEmail.requestFocus();
                return;
            }

            // 3. Validation du mot de passe
            if (passwordClair.isEmpty()) {
                afficherMessage("❌ Le mot de passe est obligatoire", "error");
                txtMotDePasse.requestFocus();
                return;
            }

            // 4. Validation de la classe responsable
            if (classeResponsable.isEmpty()) {
                afficherMessage("❌ La classe responsable est obligatoire", "error");
                txtClasseResponsable.requestFocus();
                return;
            }

            // 5. Validation du format email
            if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
                afficherMessage("❌ Format d'email invalide (ex: nom@domaine.com)", "error");
                txtEmail.requestFocus();
                return;
            }

            // 6. Validation du téléphone (optionnel mais valide si présent)
            if (!telephone.isEmpty() && !telephone.matches("\\d{8}")) {
                afficherMessage("❌ Le téléphone doit contenir exactement 8 chiffres", "error");
                txtTelephone.requestFocus();
                return;
            }

            // 7. Validation de la longueur du mot de passe
            if (passwordClair.length() < 6) {
                afficherMessage("❌ Le mot de passe doit contenir au moins 6 caractères", "error");
                txtMotDePasse.requestFocus();
                return;
            }
            if (passwordClair.length() > 30) {
                afficherMessage("❌ Le mot de passe ne doit pas dépasser 30 caractères", "error");
                txtMotDePasse.requestFocus();
                return;
            }

            // 8. Validation du salaire (obligatoire)
            if (salaireText.isEmpty()) {
                afficherMessage("❌ Le salaire est obligatoire", "error");
                txtSalaire.requestFocus();
                return;
            }

            // 9. Parsing et validation du salaire
            double salaire;
            try {
                salaire = Double.parseDouble(salaireText);
                if (salaire < 0) {
                    afficherMessage("❌ Le salaire ne peut pas être négatif", "error");
                    txtSalaire.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                afficherMessage("❌ Le salaire doit être un nombre valide (ex: 1500.50)", "error");
                txtSalaire.requestFocus();
                return;
            }

            // Création de l'objet Maitresse
            Maitresse maitresse = new Maitresse();
            maitresse.setNom(nom);
            maitresse.setEmail(email);
            maitresse.setClasseResponsable(classeResponsable);
            maitresse.setDiplome(diplome.isEmpty() ? null : diplome);
            maitresse.setSalaire(salaire);
            maitresse.setTelephone(telephone.isEmpty() ? null : telephone);
            maitresse.setAdresse(adresse.isEmpty() ? null : adresse);

            System.out.println("✅ Données validées, appel du service...");

            // Appel au service
            serviceMaitresse.ajouterMaitresse(maitresse, passwordClair);

            afficherMessage("✅ Maîtresse ajoutée avec succès !", "success");
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
        if (logoutButton != null) {
            logoutButton.setDisable(desactiver);
        }
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

        //afficherMessage("📋 Formulaire réinitialisé", "info");
    }

    /**
     * Affiche un message dans le label avec le style approprié
     * CORRIGÉ : utilise setStyle() avec fond coloré
     */
    private void afficherMessage(String message, String type) {
        Platform.runLater(() -> {
            lblMessage.setText(message);
            lblMessage.setVisible(true);

            // Application des couleurs selon le type - CORRIGÉ avec fond
            switch(type) {
                case "success":
                    lblMessage.setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724; -fx-border-color: #c3e6cb; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 10; -fx-font-weight: bold;");
                    break;
                case "error":
                    lblMessage.setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24; -fx-border-color: #f5c6cb; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 10; -fx-font-weight: bold;");
                    break;
                case "warning":
                    lblMessage.setStyle("-fx-background-color: #fff3cd; -fx-text-fill: #856404; -fx-border-color: #ffeeba; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 10; -fx-font-weight: bold;");
                    break;
                case "info":
                default:
                    lblMessage.setStyle("-fx-background-color: #d1ecf1; -fx-text-fill: #0c5460; -fx-border-color: #bee5eb; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 10; -fx-font-weight: bold;");
                    break;
            }

            // Cache le message après 5 secondes
            new Thread(() -> {
                try {
                    Thread.sleep(5000);
                    Platform.runLater(() -> lblMessage.setVisible(false));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        });
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