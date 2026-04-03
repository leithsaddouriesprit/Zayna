package tn.esprit.workshop.controlleurs.Talel;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.esprit.workshop.model.Talel.talel2.ResponsableEcole;
import tn.esprit.workshop.model.Talel.talel2.User;
import tn.esprit.workshop.services.Talel.ServiceResponsableEcole;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.Optional;

public class ControleurResponsableEcole {

    @FXML
    private TextField nomField;
    @FXML
    private TextField prenomField;
    @FXML
    private TextField txtEmail;
    @FXML
    private PasswordField txtMotDePasse;
    @FXML
    private TextField txtNomEcole;
    @FXML
    private TextArea txtAdresseEcole;
    @FXML
    private TextField txtLatitude;
    @FXML
    private TextField txtLongitude;
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

    private ServiceResponsableEcole serviceResponsable;
    private User connectedUser;

    @FXML
    public void initialize() {
        serviceResponsable = new ServiceResponsableEcole();
        System.out.println("=== ControleurResponsableEcole initialisé ===");
        lblMessage.setVisible(false);
        lblMessage.setManaged(true);
    }

    @FXML
    private void ajouterResponsable() {
        try {
            System.out.println("=== ControleurResponsableEcole.ajouterResponsable ===");

            String nom = nomField != null ? nomField.getText().trim() : "";
            String prenom = prenomField != null ? prenomField.getText().trim() : "";
            String email = txtEmail.getText().trim().toLowerCase();
            String passwordClair = txtMotDePasse.getText();
            String nomEcole = txtNomEcole != null ? txtNomEcole.getText().trim() : "";
            String adresseEcole = txtAdresseEcole != null ? txtAdresseEcole.getText().trim() : "";
            String latText = txtLatitude != null ? txtLatitude.getText().trim() : "";
            String lngText = txtLongitude != null ? txtLongitude.getText().trim() : "";
            String telephone = txtTelephone.getText().trim();
            String adresse = txtAdresse.getText().trim();

            if (nom.isEmpty()) {
                afficherMessage("❌ Le nom est obligatoire", "error");
                if (nomField != null) nomField.requestFocus();
                return;
            }
            if (prenom.isEmpty()) {
                afficherMessage("❌ Le prénom est obligatoire", "error");
                if (prenomField != null) prenomField.requestFocus();
                return;
            }
            if (email.isEmpty()) {
                afficherMessage("❌ L'email est obligatoire", "error");
                txtEmail.requestFocus();
                return;
            }
            if (passwordClair.isEmpty()) {
                afficherMessage("❌ Le mot de passe est obligatoire", "error");
                txtMotDePasse.requestFocus();
                return;
            }
            if (nomEcole.isEmpty()) {
                afficherMessage("❌ Indiquez le nom de votre établissement (candidature).", "error");
                if (txtNomEcole != null) txtNomEcole.requestFocus();
                return;
            }
            if (nomEcole.length() > 150) {
                afficherMessage("❌ Le nom de l'établissement ne doit pas dépasser 150 caractères.", "error");
                if (txtNomEcole != null) txtNomEcole.requestFocus();
                return;
            }
            if (adresseEcole.isEmpty()) {
                afficherMessage("❌ L'adresse de l'établissement est obligatoire (candidature).", "error");
                if (txtAdresseEcole != null) txtAdresseEcole.requestFocus();
                return;
            }
            if (adresseEcole.length() > 255) {
                afficherMessage("❌ L'adresse ne doit pas dépasser 255 caractères.", "error");
                if (txtAdresseEcole != null) txtAdresseEcole.requestFocus();
                return;
            }
            if (latText.isEmpty()) {
                afficherMessage("❌ La latitude est obligatoire", "error");
                if (txtLatitude != null) txtLatitude.requestFocus();
                return;
            }
            if (lngText.isEmpty()) {
                afficherMessage("❌ La longitude est obligatoire", "error");
                if (txtLongitude != null) txtLongitude.requestFocus();
                return;
            }

            double lat;
            double lng;
            try {
                lat = Double.parseDouble(latText.replace(',', '.'));
                if (lat < -90 || lat > 90) {
                    afficherMessage("❌ La latitude doit être entre -90 et 90", "error");
                    if (txtLatitude != null) txtLatitude.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                afficherMessage("❌ Latitude invalide (nombre attendu, ex: 36.8065)", "error");
                if (txtLatitude != null) txtLatitude.requestFocus();
                return;
            }
            try {
                lng = Double.parseDouble(lngText.replace(',', '.'));
                if (lng < -180 || lng > 180) {
                    afficherMessage("❌ La longitude doit être entre -180 et 180", "error");
                    if (txtLongitude != null) txtLongitude.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                afficherMessage("❌ Longitude invalide (nombre attendu, ex: 10.1815)", "error");
                if (txtLongitude != null) txtLongitude.requestFocus();
                return;
            }

            if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
                afficherMessage("❌ Format d'email invalide (ex: nom@domaine.com)", "error");
                txtEmail.requestFocus();
                return;
            }

            if (!telephone.isEmpty() && !telephone.matches("\\d{8}")) {
                afficherMessage("❌ Le téléphone doit contenir 8 chiffres", "error");
                txtTelephone.requestFocus();
                return;
            }

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

            ResponsableEcole responsable = new ResponsableEcole();
            responsable.setNom(nom);
            responsable.setPrenom(prenom);
            responsable.setEmail(email);
            responsable.setEcole(nomEcole);
            responsable.setAdresseEcole(adresseEcole);
            responsable.setLatitude(lat);
            responsable.setLongitude(lng);
            responsable.setTelephone(telephone.isEmpty() ? null : telephone);
            responsable.setAdresse(adresse.isEmpty() ? null : adresse);

            System.out.println("Appel du service avec email: " + email);
            serviceResponsable.ajouterResponsable(responsable, passwordClair);

            afficherMessage("✅ Candidature envoyée — en attente de validation. Vous n’avez pas encore accès au tableau de bord agent.", "success");
            viderFormulaire();

        } catch (Exception e) {
            System.err.println("=== ERREUR DÉTAILLÉE ===");
            System.err.println("Type d'erreur: " + e.getClass().getName());
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();

            String errorMsg = e.getMessage() != null ? e.getMessage() : "Erreur inconnue (voir console)";
            afficherMessage("❌ Erreur : " + errorMsg, "error");
        }
    }

    @FXML
    private void viderFormulaire() {
        if (nomField != null) nomField.clear();
        if (prenomField != null) prenomField.clear();
        txtEmail.clear();
        txtMotDePasse.clear();
        if (txtNomEcole != null) txtNomEcole.clear();
        if (txtAdresseEcole != null) txtAdresseEcole.clear();
        if (txtLatitude != null) txtLatitude.clear();
        if (txtLongitude != null) txtLongitude.clear();
        txtTelephone.clear();
        txtAdresse.clear();
    }

    private void afficherMessage(String message, String type) {
        lblMessage.setText(message);
        lblMessage.setVisible(true);

        switch(type) {
            case "success":
                lblMessage.setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724; -fx-border-color: #c3e6cb;");
                break;
            case "error":
                lblMessage.setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24; -fx-border-color: #f5c6cb;");
                break;
            case "warning":
                lblMessage.setStyle("-fx-background-color: #fff3cd; -fx-text-fill: #856404; -fx-border-color: #ffeeba;");
                break;
            case "info":
            default:
                lblMessage.setStyle("-fx-background-color: #d1ecf1; -fx-text-fill: #0c5460; -fx-border-color: #bee5eb;");
                break;
        }

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
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Déconnexion");
        alert.setHeaderText("Voulez-vous vraiment vous déconnecter ?");
        alert.setContentText("Vous devrez vous reconnecter pour accéder à nouveau à l'application.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                javafx.stage.Stage currentStage = (javafx.stage.Stage) logoutButton.getScene().getWindow();
                currentStage.close();

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Talel/Connecter.fxml"));
                Parent root = loader.load();
                Stage loginStage = new Stage();
                Scene scene = new Scene(root);
                loginStage.setScene(scene);
                loginStage.setTitle("Connexion - Zayna");
                loginStage.show();
            } catch (Exception e) {
                e.printStackTrace();
                afficherMessage("❌ Erreur lors de la déconnexion", "error");
            }
        }
    }
}
