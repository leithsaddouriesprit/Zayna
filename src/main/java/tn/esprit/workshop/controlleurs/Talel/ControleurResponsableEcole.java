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
    private TextField txtNom;
    @FXML
    private TextField txtEmail;
    @FXML
    private PasswordField txtMotDePasse;
    @FXML
    private TextField txtTitre;
    @FXML
    private TextField txtEcole;
    @FXML
    private TextField txtTelephone;
    @FXML
    private TextArea txtAdresse;
    @FXML
    private TextField txtSalaire;
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

        // S'assurer que le label message est bien configuré
        lblMessage.setVisible(false);
        lblMessage.setManaged(true);
    }

    @FXML
    private void ajouterResponsable() {
        try {
            System.out.println("=== ControleurResponsableEcole.ajouterResponsable ===");

            // Récupération des données
            String nom = txtNom.getText().trim();
            String email = txtEmail.getText().trim().toLowerCase();
            String passwordClair = txtMotDePasse.getText();
            String titre = txtTitre.getText().trim();
            String ecole = txtEcole.getText().trim();
            String telephone = txtTelephone.getText().trim();
            String adresse = txtAdresse.getText().trim();
            String salaireText = txtSalaire.getText().trim();

            // Validation des champs obligatoires
            if (nom.isEmpty()) {
                afficherMessage("❌ Le nom est obligatoire", "error");
                txtNom.requestFocus();
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

            if (titre.isEmpty()) {
                afficherMessage("❌ Le titre est obligatoire", "error");
                txtTitre.requestFocus();
                return;
            }

            if (ecole.isEmpty()) {
                afficherMessage("❌ Le nom de l'école est obligatoire", "error");
                txtEcole.requestFocus();
                return;
            }

            // Validation email
            if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
                afficherMessage("❌ Format d'email invalide (ex: nom@domaine.com)", "error");
                txtEmail.requestFocus();
                return;
            }

            // Validation téléphone (optionnel mais doit être valide si présent)
            if (!telephone.isEmpty() && !telephone.matches("\\d{8}")) {
                afficherMessage("❌ Le téléphone doit contenir 8 chiffres", "error");
                txtTelephone.requestFocus();
                return;
            }

            // Validation mot de passe
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

            // Création de l'objet ResponsableEcole
            ResponsableEcole responsable = new ResponsableEcole();
            responsable.setNom(nom);
            responsable.setEmail(email);
            responsable.setTitre(titre);
            responsable.setEcole(ecole);
            responsable.setTelephone(telephone.isEmpty() ? null : telephone);
            responsable.setAdresse(adresse.isEmpty() ? null : adresse);

            // Validation et parsing du salaire (optionnel)
            if (!salaireText.isEmpty()) {
                try {
                    double salaire = Double.parseDouble(salaireText);
                    if (salaire < 0) {
                        afficherMessage("❌ Le salaire ne peut pas être négatif", "error");
                        txtSalaire.requestFocus();
                        return;
                    }
                    responsable.setSalaire(salaire);
                } catch (NumberFormatException e) {
                    afficherMessage("❌ Le salaire doit être un nombre valide", "error");
                    txtSalaire.requestFocus();
                    return;
                }
            }

            System.out.println("Appel du service avec email: " + email);

            // Appel au service avec le mot de passe en clair
            serviceResponsable.ajouterResponsable(responsable, passwordClair);

            afficherMessage("✅ Responsable d'école ajouté avec succès !", "success");
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
        txtNom.clear();
        txtEmail.clear();
        txtMotDePasse.clear();
        txtTitre.clear();
        txtEcole.clear();
        txtTelephone.clear();
        txtAdresse.clear();
        txtSalaire.clear();

        //afficherMessage("📋 Formulaire vidé", "info");
    }

    /**
     * Affiche un message dans le label lblMessage
     * @param message Le texte à afficher
     * @param type Le type de message (success, error, warning, info)
     */
    private void afficherMessage(String message, String type) {
        lblMessage.setText(message);
        lblMessage.setVisible(true);

        // Appliquer le style en fonction du type
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

        // Créer un thread pour cacher le message après 5 secondes
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
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Talel/ConnecterUser.fxml"));
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