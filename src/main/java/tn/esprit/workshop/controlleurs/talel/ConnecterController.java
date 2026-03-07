package tn.esprit.workshop.controlleurs.talel;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import tn.esprit.workshop.model.talel.CategorieUser;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.workshop.model.talel.User;
import tn.esprit.workshop.services.ServiceAdmin;
import tn.esprit.workshop.model.validation.UserValidation;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ConnecterController implements Initializable {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label loginErrorLabel;

    @FXML
    private Button loginButton;

    @FXML
    private ProgressIndicator loadingIndicator;

    private ServiceAdmin serviceUser;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        serviceUser = new ServiceAdmin();
        loginErrorLabel.setVisible(false);
        loadingIndicator.setVisible(false);
    }

    @FXML
    private void handleLogin() {
        loginErrorLabel.setVisible(false);
        loadingIndicator.setVisible(true);
        loginButton.setDisable(true);

        String email = emailField.getText().trim();
        String password = passwordField.getText();

        // Validation
        UserValidation.ValidationResult result = UserValidation.validateLogin(email, password);
        if (!result.isValid()) {
            showLoginErrors(result.getErrors());
            loadingIndicator.setVisible(false);
            loginButton.setDisable(false);
            return;
        }

        // Connexion dans un thread séparé
        new Thread(() -> {
            try {
                Thread.sleep(500); // Simulation de chargement

                User user = serviceUser.login(email, password);

                javafx.application.Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    loginButton.setDisable(false);

                    // Rediriger vers la scène appropriée selon le rôle
                    redirectToRoleScene(user);
                });

            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    loginButton.setDisable(false);
                    loginErrorLabel.setText("❌ " + e.getMessage());
                    loginErrorLabel.setVisible(true);
                });
            }
        }).start();
    }

    // ==================== MÉTHODES D'INSCRIPTION ====================

    /**
     * Redirige vers le formulaire d'inscription pour les parents
     */
    @FXML
    private void handleInscriptionParent() {
        ouvrirInscription("/Parent.fxml",
                "Inscription Parent");
    }

    /**
     * Redirige vers le formulaire d'inscription pour les chauffeurs
     */
    @FXML
    private void handleInscriptionChauffeur() {
        ouvrirInscription("/Chauffeur.fxml",
                "Inscription Chauffeur");
    }

    /**
     * Redirige vers le formulaire d'inscription pour les maîtresses
     */
    @FXML
    private void handleInscriptionMaitresse() {
        ouvrirInscription("/Maitresse.fxml",
                "Inscription Enseignant");
    }

    /**
     * Redirige vers le formulaire d'inscription pour les responsables d'école
     */
    @FXML
    private void handleInscriptionResponsable() {
        ouvrirInscription("/ResponsableEcole.fxml",
                "Inscription Responsable");
    }

    /**
     * Méthode générique pour ouvrir un formulaire d'inscription
     */
    private void ouvrirInscription(String fxmlPath, String titre) {
        try {
            System.out.println("Tentative d'ouverture: " + fxmlPath);

            // Vérifier que le chemin n'est pas null
            if (fxmlPath == null || fxmlPath.isEmpty()) {
                showAlert("Erreur", "Chemin FXML invalide");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));

            // Vérifier que la ressource existe
            if (getClass().getResource(fxmlPath) == null) {
                showAlert("Erreur", "Fichier introuvable: " + fxmlPath);
                return;
            }

            Parent root = loader.load();

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Zayna - " + titre);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
        } catch (NullPointerException e) {
            e.printStackTrace();
            showAlert("Erreur", "Fichier FXML introuvable: " + fxmlPath);
        }
    }

    // ==================== GESTION DU MOT DE PASSE OUBLIÉ ====================

    @FXML
    private void handleForgotPassword() {
        showAlert("Mot de passe oublié",
                "Veuillez contacter l'administrateur pour réinitialiser votre mot de passe.\n\n" +
                        "Email: admin@zayna.com\n" +
                        "Téléphone: +216 00 000 000");
    }

    // ==================== REDIRECTION APRÈS CONNEXION ====================

    /**
     * Redirige vers la scène appropriée selon le rôle de l'utilisateur
     */
    private void redirectToRoleScene(User user) {
        try {
            String fxmlPath = getFxmlPathForRole(user.getCategories());

            if (fxmlPath == null) {
                showAlert("Erreur", "Rôle utilisateur inconnu");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));

            if (getClass().getResource(fxmlPath) == null) {
                showAlert("Erreur", "Tableau de bord introuvable pour le rôle: " + user.getCategories());
                return;
            }

            Parent root = loader.load();

            // Passer l'utilisateur connecté au contrôleur de la scène
            Object controller = loader.getController();
            injectUserToController(controller, user);

            Scene scene = new Scene(root);
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(getTitleForRole(user.getCategories()));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la page: " + e.getMessage());
        }
    }

    /**
     * Retourne le chemin FXML selon le rôle
     */
    private String getFxmlPathForRole(CategorieUser role) {
        if (role == null) return null;

        switch (role) {
            case ADMIN:
                return "/Admin.fxml";     // À créer si nécessaire
            case CHAUFFEUR:
                return "/Chauffeur.fxml";          // ✅ Chemin correct
            case MAITRESSE:
                return "/Maitresse.fxml";          // ✅ Chemin correct
            case PARENT:
                return "/Parent.fxml";              // ✅ Chemin correct
            case RESPONSABLEECOLE:
                return "/ResponsableEcole.fxml";    // ✅ Chemin correct
            default:
                return "/ConnecterUser.fxml";
        }
    }

    /**
     * Retourne le titre de la fenêtre selon le rôle
     */
    private String getTitleForRole(CategorieUser role) {
        if (role == null) return "Tableau de bord";

        switch (role) {
            case ADMIN:
                return "Administration - Gestion des utilisateurs";
            case CHAUFFEUR:
                return "Espace Chauffeur - Gestion des tournées";
            case MAITRESSE:
                return "Espace Enseignant - Gestion des classes";
            case PARENT:
                return "Espace Parent - Suivi scolaire";
            case RESPONSABLEECOLE:
                return "Espace Responsable - Gestion d'établissement";
            default:
                return "Tableau de bord";
        }
    }

    /**
     * Injecte l'utilisateur connecté dans le contrôleur approprié
     */
    private void injectUserToController(Object controller, User user) {
        try {
            // Essayer d'appeler setUser par réflexion
            controller.getClass().getMethod("setUser", User.class).invoke(controller, user);
        } catch (Exception e) {
            // Ignorer si la méthode n'existe pas
            System.out.println("Le contrôleur " + controller.getClass().getSimpleName() +
                    " n'a pas de méthode setUser");
        }
    }

    @FXML
    private void handleForgotPassword(ActionEvent event) {
        try {
            System.out.println("Navigation vers la page mot de passe oublié...");

            // Vérification préalable
            java.net.URL fxmlUrl = getClass().getResource("/Oublier.fxml");
            if (fxmlUrl == null) {
                throw new NullPointerException("Fichier Oublier.fxml non trouvé dans /resources/");
            }

            // Chargement et navigation
            Parent root = FXMLLoader.load(fxmlUrl);
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setScene(scene);
            stage.setTitle("Réinitialisation du mot de passe");
            stage.show();

        } catch (IOException | NullPointerException e) {
            System.err.println("Erreur de navigation: " + e.getMessage());
            e.printStackTrace();

        }
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    private void showLoginErrors(java.util.List<String> errors) {
        StringBuilder sb = new StringBuilder();
        for (String error : errors) {
            sb.append("• ").append(error).append("\n");
        }
        loginErrorLabel.setText(sb.toString());
        loginErrorLabel.setVisible(true);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}