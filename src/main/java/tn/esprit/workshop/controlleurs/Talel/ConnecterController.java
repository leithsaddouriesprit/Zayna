package tn.esprit.workshop.controlleurs.Talel;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import tn.esprit.workshop.model.Talel.talel2.CategorieUser;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.application.Platform;
import tn.esprit.workshop.model.Talel.talel2.User;
import tn.esprit.workshop.services.Talel.ServiceAdmin;
import tn.esprit.workshop.utilis.AppSession;
import tn.esprit.workshop.controlleurs.leith.SceneNavigator;
import tn.esprit.workshop.controlleurs.Talel.AdminController;
import tn.esprit.workshop.utilis.MyBDConnexion;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ConnecterController implements Initializable {
    @FXML
    private Label messageLabel;

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

        // CORRECTION 1: Configuration du label d'erreur
        if (loginErrorLabel != null) {
            loginErrorLabel.setVisible(false);
            loginErrorLabel.setManaged(true);
            loginErrorLabel.setWrapText(true);
        }

        if (loadingIndicator != null) {
            loadingIndicator.setVisible(false);
        }

        System.out.println("=== ConnecterController initialisé ===");
    }

    @FXML
    private void handleLogin() {
        // Cacher les erreurs précédentes
        loginErrorLabel.setVisible(false);

        // Afficher l'indicateur de chargement
        loadingIndicator.setVisible(true);
        loginButton.setDisable(true);

        String email = emailField.getText().trim();
        String password = passwordField.getText();

        // CORRECTION 2: Validation CHAMP PAR CHAMP avec focus
        if (email.isEmpty()) {
            afficherErreur("❌ L'email est obligatoire");
            emailField.requestFocus();
            loadingIndicator.setVisible(false);
            loginButton.setDisable(false);
            return;
        }

        if (password.isEmpty()) {
            afficherErreur("❌ Le mot de passe est obligatoire");
            passwordField.requestFocus();
            loadingIndicator.setVisible(false);
            loginButton.setDisable(false);
            return;
        }

        if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            afficherErreur("❌ Format d'email invalide (ex: nom@domaine.com)");
            emailField.requestFocus();
            loadingIndicator.setVisible(false);
            loginButton.setDisable(false);
            return;
        }

        // Connexion dans un thread séparé
        new Thread(() -> {
            try {
                Thread.sleep(500); // Simulation de chargement

                User user = serviceUser.login(email, password);

                Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    loginButton.setDisable(false);

                    if (user != null) {
                        // Rediriger vers la scène appropriée selon le rôle
                        redirectToRoleScene(user);
                    } else {
                        afficherErreur("❌ Email ou mot de passe incorrect");
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    loginButton.setDisable(false);
                    afficherErreur("❌ " + e.getMessage());
                    e.printStackTrace();
                });
            }
        }).start();
    }

    // CORRECTION 3: Méthode utilitaire pour afficher les erreurs
    private void afficherErreur(String message) {
        Platform.runLater(() -> {
            if (loginErrorLabel == null) return;
            loginErrorLabel.setText(message);
            loginErrorLabel.setVisible(true);

            // Style pour les erreurs
            loginErrorLabel.setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24; -fx-border-color: #f5c6cb; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 10; -fx-font-weight: bold;");
        });
    }

    // ==================== MÉTHODES D'INSCRIPTION ====================

    /**
     * Redirige vers le formulaire d'inscription pour les parents
     */
    @FXML
    private void handleInscriptionParent() {
        ouvrirInscription("/Talel/Parent.fxml", "Inscription Parent");
    }

    /**
     * Redirige vers le formulaire d'inscription pour les chauffeurs
     */
    @FXML
    private void handleInscriptionChauffeur() {
        ouvrirInscription("/Talel/Chauffeur.fxml", "Inscription Chauffeur");
    }

    /**
     * Redirige vers le formulaire d'inscription pour les maîtresses
     */
    @FXML
    private void handleInscriptionMaitresse() {
        ouvrirInscription("/Talel/Maitresse.fxml", "Inscription Enseignant");
    }

    /**
     * Redirige vers le formulaire d'inscription pour les responsables d'école
     */
    @FXML
    private void handleInscriptionResponsable() {
        ouvrirInscription("/Talel/ResponsableEcole.fxml", "Inscription Responsable");
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

    // Ajoutez cette méthode pour afficher les erreurs
    private void showError(String message) {
        if (messageLabel != null) {
            messageLabel.setText("❌ " + message);
            messageLabel.setStyle("-fx-background-color: #fed7d7; " +
                    "-fx-text-fill: #c53030; " +
                    "-fx-padding: 10; " +
                    "-fx-background-radius: 5;");
            messageLabel.setVisible(true);

            // Cache le message après 5 secondes
            PauseTransition pause = new PauseTransition(Duration.seconds(5));
            pause.setOnFinished(e -> messageLabel.setVisible(false));
            pause.play();
        } else {
            System.out.println("Erreur: " + message);
        }
    }

    // Ajoutez aussi cette méthode pour les succès (optionnel)
    private void showSuccess(String message) {
        if (messageLabel != null) {
            messageLabel.setText("✅ " + message);
            messageLabel.setStyle("-fx-background-color: #c6f6d5; " +
                    "-fx-text-fill: #22543d; " +
                    "-fx-padding: 10; " +
                    "-fx-background-radius: 5;");
            messageLabel.setVisible(true);

            PauseTransition pause = new PauseTransition(Duration.seconds(3));
            pause.setOnFinished(e -> messageLabel.setVisible(false));
            pause.play();
        }
    }
    @FXML
    private void handleForgotPassword() {
        try {
            // Animation de transition
            Stage stage = (Stage) emailField.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/Talel/Oublier.fxml"));
            Parent root = loader.load();

            Scene currentScene = stage.getScene();
            root.setOpacity(0);
            currentScene.setRoot(root);

            // Animation de fondu
            FadeTransition fade = new FadeTransition(Duration.seconds(0.5), root);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.play();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur lors de la navigation");
        }
    }

    // ==================== REDIRECTION APRÈS CONNEXION ====================

    /**
     * Redirige vers la scène appropriée selon le rôle de l'utilisateur
     * et initialise la session applicative commune (AppSession).
     *
     * IMPORTANT :
     * - Parent et Chauffeur sont redirigés vers les flux Leith existants
     *   (tracking / transport / parent / chauffeur) via SceneNavigator.
     * - ResponsableEcole est mappé sur les écrans Agent Leith si une école
     *   correspondante est trouvée (AppSession.ecoleId).
     * - Les autres rôles (ADMIN, MAITRESSE, ...) restent sur les écrans Talel.
     */
    private void redirectToRoleScene(User user) {
        try {
            CategorieUser role = user.getCategories();
            if (role == null) {
                showAlert("Erreur", "Rôle utilisateur inconnu");
                return;
            }

            // ---------- Flux Parent (Leith) ----------
            if (role == CategorieUser.PARENT) {
                // Résolution parent.id via parent.user_id = users.id
                Integer parentId = resolveParentIdFromUser(user);
                if (parentId == null) {
                    afficherErreur("Aucun profil parent associé à ce compte. Veuillez contacter l'administrateur.");
                    return;
                }
                AppSession.getInstance().setParentId(parentId);
                setConnectedUserDisplay(user, "Parent");

                Stage stage = (Stage) loginButton.getScene().getWindow();
                if (stage != null) {
                    stage.close();
                }
                SceneNavigator.openParentShell();
                return;
            }

            // ---------- Flux Chauffeur (Leith) ----------
            if (role == CategorieUser.CHAUFFEUR) {
                // Leith attend chauffeur_id = chauffeur.id (FK candidature.chauffeur_id -> chauffeur.id).
                // On résout chauffeur.id via chauffeur.user_id = users.id.
                Integer chauffeurId = resolveChauffeurIdFromUser(user);
                if (chauffeurId == null) {
                    afficherErreur("Aucun profil chauffeur associé à ce compte. Veuillez contacter l'administrateur.");
                    return;
                }
                AppSession.getInstance().setChauffeurId(chauffeurId);
                setConnectedUserDisplay(user, "Chauffeur");

                Stage stage = (Stage) loginButton.getScene().getWindow();
                if (stage != null) {
                    stage.close();
                }
                SceneNavigator.openChauffeurShell();
                return;
            }

            // ---------- Flux ResponsableEcole -> Agent (Leith) ----------
            if (role == CategorieUser.RESPONSABLEECOLE) {
                // Résolution : SELECT id, id_ecole FROM agent_ecole WHERE user_id = ?
                int[] agentEcole = resolveAgentEcoleFromUser(user);
                if (agentEcole == null) {
                    afficherErreur("Aucun profil agent associé à ce compte. Veuillez contacter l'administrateur.");
                    return;
                }
                AppSession.getInstance().setAgentId(agentEcole[0]);
                AppSession.getInstance().setEcoleId(agentEcole[1]);
                setConnectedUserDisplay(user, "Agent École");

                Stage stage = (Stage) loginButton.getScene().getWindow();
                if (stage != null) {
                    stage.close();
                }
                SceneNavigator.openAgentShell();
                return;
            }

            // ---------- Flux Admin (shell comme les autres rôles) ----------
            if (role == CategorieUser.ADMIN) {
                setConnectedUserDisplay(user, "Administrateur");
                SceneNavigator.setPendingAdminUser(user);
                Stage stage = (Stage) loginButton.getScene().getWindow();
                if (stage != null) {
                    stage.close();
                }
                SceneNavigator.openAdminShell();
                return;
            }

            // ---------- Rôles restants : écrans Talel (MAITRESSE, etc.) ----------
            String fxmlPath = getFxmlPathForRole(role);

            if (fxmlPath == null) {
                showAlert("Erreur", "Rôle utilisateur inconnu");
                return;
            }

            if (getClass().getResource(fxmlPath) == null) {
                showAlert("Erreur", "Tableau de bord introuvable pour le rôle: " + role);
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();
            injectUserToController(controller, user);

            if (controller instanceof AdminController adminController) {
                adminController.setCurrentUser(user);
            }

            Scene scene = new Scene(root);
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(getTitleForRole(role));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la page: " + e.getMessage());
        }
    }

    /**
     * Résout parent.id à partir du user connecté (PARENT).
     * Utilise la relation parent.user_id -> users.id (source de vérité zaynaa).
     *
     * @return parent.id ou null si aucun enregistrement parent lié.
     */
    private Integer resolveParentIdFromUser(User user) {
        int userId = user.getId();
        try (Connection conn = MyBDConnexion.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id FROM parent WHERE user_id = ?")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            System.err.println("Résolution parent par user_id: " + e.getMessage());
        }
        return null;
    }

    /**
     * Résout chauffeur.id à partir du user connecté (CHAUFFEUR).
     * Utilise la relation chauffeur.user_id -> users.id (source de vérité zaynaa).
     * Ne crée jamais de ligne chauffeur : l’inscription Talel est le seul point de création.
     *
     * @return l’id de la table chauffeur, ou null si aucun enregistrement lié (échec explicite).
     */
    private Integer resolveChauffeurIdFromUser(User user) {
        int userId = user.getId();
        try (Connection conn = MyBDConnexion.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT id FROM chauffeur WHERE user_id = ?")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            System.err.println("Résolution chauffeur par user_id (échec) : " + e.getMessage());
        }
        return null;
    }

    /**
     * Résout l'enregistrement agent_ecole à partir du user connecté (RESPONSABLEECOLE).
     * Auth via users uniquement ; agent_ecole.user_id -> users.id.
     *
     * @return int[] { agent_ecole.id, id_ecole } ou null si aucun enregistrement lié.
     */
    private int[] resolveAgentEcoleFromUser(User user) {
        int userId = user.getId();
        try (Connection conn = MyBDConnexion.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT id, id_ecole FROM agent_ecole WHERE user_id = ?")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new int[]{ rs.getInt("id"), rs.getInt("id_ecole") };
                }
            }
        } catch (SQLException e) {
            System.err.println("Résolution agent_ecole par user_id (échec) : " + e.getMessage());
        }
        return null;
    }

    /**
     * Retourne le chemin FXML selon le rôle
     */
    private String getFxmlPathForRole(CategorieUser role) {
        if (role == null) return null;

        switch (role) {
            case ADMIN:
                return "/Talel/Admin.fxml";
            case CHAUFFEUR:
                return "/Talel/Chauffeur.fxml";
            case MAITRESSE:
                return "/Talel/Maitresse.fxml";
            case PARENT:
                return "/Talel/Parent.fxml";
            case RESPONSABLEECOLE:
                return "/Talel/ResponsableEcole.fxml";
            default:
                return "/Talel/ConnecterUser.fxml";
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

    /** Affiche le nom et le rôle dans les shells (barre supérieure). */
    private void setConnectedUserDisplay(User user, String roleLabel) {
        String name = (user.getNom() != null && !user.getNom().isBlank()) ? user.getNom() : user.getEmail();
        if (name == null) name = "Utilisateur";
        AppSession.getInstance().setConnectedUserName(name);
        AppSession.getInstance().setConnectedUserRole(roleLabel);
    }

    /**
     * Injecte l'utilisateur connecté dans le contrôleur approprié
     */
    private void injectUserToController(Object controller, User user) {
        try {
            // Essayer d'appeler setUser par réflexion
            controller.getClass().getMethod("setUser", User.class).invoke(controller, user);
            System.out.println("✅ Utilisateur injecté dans " + controller.getClass().getSimpleName());
        } catch (Exception e) {
            // Ignorer si la méthode n'existe pas
            System.out.println("ℹ️ Le contrôleur " + controller.getClass().getSimpleName() +
                    " n'a pas de méthode setUser");
        }
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}