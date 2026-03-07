package tn.esprit.workshop.controlleurs.talel;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.esprit.workshop.model.talel.Chauffeur;
import tn.esprit.workshop.model.talel.User;
import tn.esprit.workshop.services.talel.ServiceChauffeur;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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

    private ServiceChauffeur serviceChauffeur;
    private User connectedUser;

    @FXML
    public void initialize() {
        serviceChauffeur = new ServiceChauffeur();
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

            // AFFICHAGE DES VALEURS POUR DÉBOGAGE
            System.out.println("=== VALEURS SAISIES ===");
            System.out.println("   Nom: '" + nom + "'");
            System.out.println("   Email: '" + email + "'");
            System.out.println("   Permis: '" + permis + "'");
            System.out.println("   Date permis: " + datePermis);
            System.out.println("   Salaire: '" + salaireText + "'");
            System.out.println("   Téléphone: '" + telephone + "'");

            // Validation des champs obligatoires
            if (nom.isEmpty() || email.isEmpty() || passwordClair.isEmpty() || permis.isEmpty()) {
                afficherMessage("Veuillez remplir tous les champs obligatoires (Nom, Email, Mot de passe, Permis)", "warning");
                return;
            }

            // Validation de la date d'obtention du permis
            if (datePermis == null) {
                afficherMessage("La date d'obtention du permis est obligatoire", "warning");
                return;
            }
            if (datePermis.isAfter(LocalDate.now())) {
                afficherMessage("La date d'obtention du permis ne peut pas être dans le futur", "error");
                return;
            }

            // Validation email
            if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
                afficherMessage("Format d'email invalide (ex: nom@domaine.com)", "error");
                return;
            }

            // Validation téléphone (optionnel)
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
                afficherMessage("Le salaire est obligatoire", "warning");
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
                afficherMessage("Le salaire doit être un nombre valide (ex: 1500.00)", "error");
                return;
            }

            // Création de l'objet Chauffeur
            Chauffeur chauffeur = new Chauffeur();
            chauffeur.setNom(nom);
            chauffeur.setEmail(email);
            chauffeur.setPermis(permis);
            chauffeur.setDateObtentionPermis(datePermis);
            chauffeur.setSalaire(salaire);  // SALAIRE TOUJOURS DÉFINI

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
            System.out.println("   Salaire défini: " + chauffeur.getSalaire());

            // Appel au service
            serviceChauffeur.ajouterChauffeur(chauffeur, passwordClair);

            afficherMessage("✅ Chauffeur ajouté avec succès ! ID: " + chauffeur.getId(), "success");
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
        txtPermis.clear();
        dateObtentionPermis.setValue(null);
        txtTelephone.clear();
        txtAdresse.clear();
        txtVehicule.clear();
        txtSalaire.clear();
        txtLigne.clear();
        afficherMessage("Formulaire vidé", "info");
    }

    private void afficherMessage(String message, String type) {
        if (lblMessage == null) {
            System.err.println("lblMessage est null!");
            return;
        }
        lblMessage.setText(message);
        lblMessage.getStyleClass().removeAll("success", "error", "warning", "info");
        lblMessage.getStyleClass().add(type);
        lblMessage.setVisible(true);

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