package tn.esprit.workshop.controlleurs.Talel;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.esprit.workshop.model.Talel.talel2.Chauffeur;
import tn.esprit.workshop.model.Talel.talel2.User;
import tn.esprit.workshop.services.Talel.ServiceChauffeur;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.application.Platform;
import java.time.LocalDate;
import java.util.Optional;

public class ControleurChauffeur {

    @FXML
    private TextField txtNom;
    @FXML
    private TextField txtEmail;
    @FXML
    private PasswordField txtMotDePasse;
    @FXML
    private TextField txtPermis;
    @FXML
    private DatePicker dateObtentionPermis;
    @FXML
    private TextField txtTelephone;
    @FXML
    private TextArea txtAdresse;
    @FXML
    private TextField txtVehicule;
    @FXML
    private TextField txtSalaire;
    @FXML
    private TextField txtLigne;
    @FXML
    private Label lblMessage;
    @FXML
    private Button logoutButton;
    @FXML
    private Button btnAjouter; // À ajouter dans le FXML

    private ServiceChauffeur serviceChauffeur;
    private User connectedUser;

    @FXML
    public void initialize() {
        serviceChauffeur = new ServiceChauffeur();

        // CORRECTION 1: Configuration du label message
        if (lblMessage != null) {
            lblMessage.setVisible(false);
            lblMessage.setManaged(true);  // ← CORRIGÉ (était false par défaut)
            lblMessage.setWrapText(true); // ← CORRIGÉ
        }

        System.out.println("=== ControleurChauffeur initialisé ===");
    }

    @FXML
    private void ajouterChauffeur() {
        try {
            System.out.println("=== ControleurChauffeur.ajouterChauffeur ===");

            // Récupération des données
            String nom = txtNom.getText().trim();
            String email = txtEmail.getText().trim().toLowerCase();
            String passwordClair = txtMotDePasse.getText();
            String permis = txtPermis.getText().trim().toUpperCase();
            LocalDate datePermis = dateObtentionPermis.getValue();
            String telephone = txtTelephone.getText().trim();
            String adresse = txtAdresse.getText().trim();
            String vehicule = txtVehicule.getText().trim();
            String salaireText = txtSalaire.getText().trim();

            // CORRECTION 2: Validation CHAMP PAR CHAMP avec focus
            // 1. Validation du nom
            if (nom.isEmpty()) {
                afficherMessage("❌ Le nom est obligatoire", "error");
                txtNom.requestFocus();  // ← CORRIGÉ
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

            // 4. Validation du permis
            if (permis.isEmpty()) {
                afficherMessage("❌ Le numéro de permis est obligatoire", "error");
                txtPermis.requestFocus();
                return;
            }

            // 5. Validation de la date d'obtention du permis
            if (datePermis == null) {
                afficherMessage("❌ La date d'obtention du permis est obligatoire", "error");
                dateObtentionPermis.requestFocus();
                return;
            }
            if (datePermis.isAfter(LocalDate.now())) {
                afficherMessage("❌ La date d'obtention du permis ne peut pas être dans le futur", "error");
                dateObtentionPermis.requestFocus();
                return;
            }

            // 6. Validation du format email
            if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
                afficherMessage("❌ Format d'email invalide (ex: nom@domaine.com)", "error");
                txtEmail.requestFocus();
                return;
            }

            // 7. Validation du téléphone (optionnel)
            if (!telephone.isEmpty() && !telephone.matches("\\d{8}")) {
                afficherMessage("❌ Le téléphone doit contenir 8 chiffres", "error");
                txtTelephone.requestFocus();
                return;
            }

            // 8. Validation du mot de passe
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

            // 9. Validation du salaire
            if (salaireText.isEmpty()) {
                afficherMessage("❌ Le salaire est obligatoire", "error");
                txtSalaire.requestFocus();
                return;
            }

            // 10. Parsing du salaire
            double salaire;
            try {
                salaire = Double.parseDouble(salaireText);
                if (salaire < 0) {
                    afficherMessage("❌ Le salaire ne peut pas être négatif", "error");
                    txtSalaire.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                afficherMessage("❌ Le salaire doit être un nombre valide (ex: 1500.00)", "error");
                txtSalaire.requestFocus();
                return;
            }

            // Création de l'objet Chauffeur
            Chauffeur chauffeur = new Chauffeur();
            chauffeur.setNom(nom);
            chauffeur.setEmail(email);
            chauffeur.setPermis(permis);
            chauffeur.setDateObtentionPermis(datePermis);
            chauffeur.setSalaire(salaire);

            // Champs optionnels
            if (!telephone.isEmpty()) {
                chauffeur.setTelephone(telephone);
            }
            if (!adresse.isEmpty()) {
                chauffeur.setAdresse(adresse);
            }
            if (!vehicule.isEmpty()) {
                chauffeur.setVehicule(vehicule);
            }

            System.out.println("✅ Données validées, appel du service...");

            // Appel au service
            serviceChauffeur.ajouterChauffeur(chauffeur, passwordClair);

            afficherMessage("✅ Chauffeur ajouté avec succès !", "success");
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
        txtEmail.clear();
        txtMotDePasse.clear();
        txtPermis.clear();
        dateObtentionPermis.setValue(null);
        txtTelephone.clear();
        txtAdresse.clear();
        txtVehicule.clear();
        txtSalaire.clear();
        txtLigne.clear();

        //afficherMessage("📋 Formulaire vidé", "info");
    }

    // CORRECTION 3: Méthode afficherMessage avec setStyle() au lieu de getStyleClass()
    private void afficherMessage(String message, String type) {
        Platform.runLater(() -> {
            if (lblMessage == null) {
                System.err.println("lblMessage est null!");
                return;
            }

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
                    Platform.runLater(() -> {
                        if (lblMessage != null) {
                            lblMessage.setVisible(false);
                        }
                    });
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