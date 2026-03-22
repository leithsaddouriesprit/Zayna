package tn.esprit.workshop.controlleurs.leith.shell;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import tn.esprit.workshop.controlleurs.Talel.AdminController;
import tn.esprit.workshop.controlleurs.leith.SceneNavigator;
import tn.esprit.workshop.utilis.AppSession;

import java.util.logging.Level;
import java.util.logging.Logger;

public class AdminShellController {

    private static final Logger LOG = Logger.getLogger(AdminShellController.class.getName());
    private static final String ADMIN_FXML = "/Talel/Admin.fxml";
    private static final String ADMIN_HOME_FXML = "/Talel/admin/AdminHome.fxml";
    private static final String ADMIN_STATS_FXML = "/Talel/admin/AdminStats.fxml";

    private tn.esprit.workshop.model.Talel.talel2.User loggedAdmin;

    @FXML private StackPane contentArea;
    @FXML private Label lblAccountInfo;

    @FXML
    public void initialize() {
        SceneNavigator.registerAdminShell(this);
        // Capture admin user once so we can inject it when opening Gestion utilisateurs
        loggedAdmin = SceneNavigator.getAndClearPendingAdminUser();
        Platform.runLater(() -> {
            if (contentArea != null && contentArea.getScene() != null && contentArea.getScene().getWindow() instanceof Stage) {
                ((Stage) contentArea.getScene().getWindow()).setOnHidden(e -> SceneNavigator.unregisterAdminShell());
            }
        });
        updateAccountLabel();
        loadAdminHome();
    }

    private void updateAccountLabel() {
        if (lblAccountInfo != null) {
            String name = AppSession.getInstance().getConnectedUserName();
            String role = AppSession.getInstance().getConnectedUserRole();
            lblAccountInfo.setText((name.isEmpty() ? "Admin" : name) + " (" + (role.isEmpty() ? "Administrateur" : role) + ")");
        }
    }

    private void loadAdminContent() {
        if (contentArea == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(ADMIN_FXML));
            Parent root = loader.load();
            Object controller = loader.getController();
            if (controller instanceof AdminController) {
                if (loggedAdmin != null) {
                    ((AdminController) controller).setCurrentUser(loggedAdmin);
                }
            }
            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to load Admin content " + ADMIN_FXML, e);
        }
    }

    private void loadAdminHome() {
        loadContent(ADMIN_HOME_FXML);
    }

    public void loadContent(String fxmlPath) {
        if (contentArea == null) return;
        if (ADMIN_FXML.equals(fxmlPath)) {
            loadAdminContent();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(fxmlPath));
            Parent root = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to load " + fxmlPath, e);
        }
    }

    @FXML
    void goHome() {
        loadAdminHome();
    }

    @FXML
    void openGestionUtilisateurs() {
        loadAdminContent();
    }

    @FXML
    void openCandidatureEcole() {
        loadContent("/Talel/admin/AdminCandidatureEcole.fxml");
    }

    @FXML
    void openParents() {
        loadContent("/Talel/admin/AdminParents.fxml");
    }

    @FXML
    void openChauffeurs() {
        loadContent("/Talel/admin/AdminChauffeurs.fxml");
    }

    @FXML
    void openAgents() {
        loadContent("/Talel/admin/AdminAgents.fxml");
    }

    @FXML
    void openMaitresses() {
        loadContent("/Talel/admin/AdminMaitresses.fxml");
    }

    @FXML
    void openTrajets() {
        loadContent("/Talel/admin/AdminTrajets.fxml");
    }

    @FXML
    void openStatistiques() {
        loadContent(ADMIN_STATS_FXML);
    }

    @FXML
    void logout() {
        doLogout();
    }

    /** Single logout path for AdminShell: used by top bar, sidebar, and Admin content when in shell. */
    public void doLogout() {
        SceneNavigator.unregisterAdminShell();
        if (contentArea != null && contentArea.getScene() != null && contentArea.getScene().getWindow() instanceof Stage) {
            ((Stage) contentArea.getScene().getWindow()).close();
        }
        SceneNavigator.showLoginWindow();
    }
}
