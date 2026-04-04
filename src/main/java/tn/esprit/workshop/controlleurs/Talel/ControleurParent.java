package tn.esprit.workshop.controlleurs.Talel;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.esprit.workshop.model.Talel.talel2.Parent;
import tn.esprit.workshop.model.Talel.talel2.User;
import tn.esprit.workshop.services.Talel.ServiceParent;
import tn.esprit.workshop.utilis.ZaynaInputConstraints;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.application.Platform;

import java.util.Optional;

public class ControleurParent {

    @FXML
    private TextField txtNom;
    @FXML
    private TextField txtPrenom;
    @FXML
    private TextField txtEmail;
    @FXML
    private PasswordField txtMotDePasse;
    @FXML
    private TextField txtTelephone;
    @FXML
    private TextArea txtAdresse;
    @FXML
    private TextField txtProfession;
    @FXML
    private Label lblMessage;
    @FXML
    private Button logoutButton;
    @FXML
    private Button btnAjouterParent;

    private ServiceParent serviceParent;
    private User connectedUser;

    @FXML
    public void initialize() {
        serviceParent = new ServiceParent();
        lblMessage.setVisible(false);
        lblMessage.setManaged(true);
        lblMessage.setWrapText(true);
        ZaynaInputConstraints.apply(txtNom, ZaynaInputConstraints.lettersAndSpacesOnly(ZaynaInputConstraints.LEN_PARENT_NOM_PRENOM));
        ZaynaInputConstraints.apply(txtPrenom, ZaynaInputConstraints.lettersAndSpacesOnly(ZaynaInputConstraints.LEN_PARENT_NOM_PRENOM));
        ZaynaInputConstraints.apply(txtEmail, ZaynaInputConstraints.emailInput(ZaynaInputConstraints.LEN_USERS_EMAIL));
        ZaynaInputConstraints.apply(txtTelephone, ZaynaInputConstraints.digitsOnly(8));
    }

    @FXML
    private void ajouterParent() {
        try {
            // Récupération des données
            String nom = txtNom.getText().trim();
            String prenom = txtPrenom.getText().trim();
            String email = txtEmail.getText().trim().toLowerCase();
            String passwordClair = txtMotDePasse.getText();
            String telephone = txtTelephone.getText().trim();
            String adresse = txtAdresse.getText().trim();
            String profession = txtProfession.getText().trim();

            // VALIDATION CHAMP PAR CHAMP avec messages précis et focus

            String errNom = ZaynaInputConstraints.validatePersonName(nom, ZaynaInputConstraints.LEN_PARENT_NOM_PRENOM, "Le nom");
            if (errNom != null) {
                afficherMessage("❌ " + errNom, "error");
                txtNom.requestFocus();
                return;
            }

            String errPrenom = ZaynaInputConstraints.validatePersonName(prenom, ZaynaInputConstraints.LEN_PARENT_NOM_PRENOM, "Le prénom");
            if (errPrenom != null) {
                afficherMessage("❌ " + errPrenom, "error");
                txtPrenom.requestFocus();
                return;
            }

            String errEmail = ZaynaInputConstraints.validateEmail(email, ZaynaInputConstraints.LEN_USERS_EMAIL);
            if (errEmail != null) {
                afficherMessage("❌ " + errEmail, "error");
                txtEmail.requestFocus();
                return;
            }

            // 4. Validation du mot de passe
            if (passwordClair.isEmpty()) {
                afficherMessage("❌ Le mot de passe est obligatoire", "error");
                txtMotDePasse.requestFocus();
                return;
            }

            String errTel = ZaynaInputConstraints.validatePhone8(telephone, true);
            if (errTel != null) {
                afficherMessage("❌ " + errTel, "error");
                txtTelephone.requestFocus();
                return;
            }

            // 8. Validation de la longueur du mot de passe
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

            // Création de l'objet Parent (aucun enfant créé à l'inscription)
            Parent parent = new Parent();
            parent.setNom(nom);
            parent.setPrenom(prenom);
            parent.setEmail(email);
            parent.setTelephone(telephone);
            parent.setAdresse(adresse.isEmpty() ? null : adresse);
            parent.setProfession(profession.isEmpty() ? null : profession);

            // Appel au service
            serviceParent.ajouterParent(parent, passwordClair);

            afficherMessage("✅ Parent ajouté avec succès !", "success");
            viderFormulaire();

        } catch (Exception e) {
            System.err.println("=== ERREUR DÉTAILLÉE ===");
            System.err.println("Type d'erreur: " + e.getClass().getName());
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();

            String errorMsg = e.getMessage() != null ? e.getMessage() : "Erreur inconnue";
            afficherMessage("❌ Erreur : " + errorMsg, "error");
        }
    }

    @FXML
    private void viderFormulaire() {
        txtNom.clear();
        txtPrenom.clear();
        txtEmail.clear();
        txtMotDePasse.clear();
        txtTelephone.clear();
        txtAdresse.clear();
        txtProfession.clear();
    }

    /**
     * Affiche un message dans le label avec le style approprié
     * CORRIGÉ : utilise setStyle() au lieu de getStyleClass()
     */
    private void afficherMessage(String message, String type) {
        lblMessage.setText(message);
        lblMessage.setVisible(true);

        // Application des couleurs selon le type - CORRIGÉ
        switch(type) {
            case "success":
                lblMessage.setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724; -fx-border-color: #c3e6cb; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 10;");
                break;
            case "error":
                lblMessage.setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24; -fx-border-color: #f5c6cb; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 10;");
                break;
            case "warning":
                lblMessage.setStyle("-fx-background-color: #fff3cd; -fx-text-fill: #856404; -fx-border-color: #ffeeba; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 10;");
                break;
            case "info":
            default:
                lblMessage.setStyle("-fx-background-color: #d1ecf1; -fx-text-fill: #0c5460; -fx-border-color: #bee5eb; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 10;");
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
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Talel/ConnecterUser.fxml"));
                javafx.scene.Parent root = loader.load();

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