package tn.esprit.workshop.controlleurs.leith.shell;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import tn.esprit.workshop.controlleurs.leith.SceneNavigator;
import tn.esprit.workshop.utilis.AppSession;

import java.util.logging.Level;
import java.util.logging.Logger;

public class MaitresseShellController {

    private static final Logger LOG = Logger.getLogger(MaitresseShellController.class.getName());

    @FXML private StackPane contentArea;
    @FXML private Label lblAccountInfo;

    @FXML
    public void initialize() {
        SceneNavigator.registerMaitresseShell(this);
        Platform.runLater(() -> {
            if (contentArea != null && contentArea.getScene() != null && contentArea.getScene().getWindow() instanceof Stage) {
                ((Stage) contentArea.getScene().getWindow()).setOnHidden(e -> SceneNavigator.unregisterMaitresseShell());
            }
        });
        updateAccountLabel();
        loadContent("/leith/maitresse/MaitresseHome.fxml");
    }

    private void updateAccountLabel() {
        if (lblAccountInfo != null) {
            String name = AppSession.getInstance().getConnectedUserName();
            String role = AppSession.getInstance().getConnectedUserRole();
            lblAccountInfo.setText((name.isEmpty() ? "Maîtresse" : name) + " (" + (role.isEmpty() ? "Maîtresse" : role) + ")");
        }
    }

    public void loadContent(String fxmlPath) {
        if (contentArea == null) return;
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
        loadContent("/leith/maitresse/MaitresseHome.fxml");
    }

    @FXML
    void openEnfants() {
        loadContent("/leith/maitresse/MaitresseEnfants.fxml");
    }

    @FXML
    void logout() {
        SceneNavigator.unregisterMaitresseShell();
        Stage stage = contentArea != null && contentArea.getScene() != null ? (Stage) contentArea.getScene().getWindow() : null;
        if (stage != null) stage.close();
        SceneNavigator.showLoginWindow();
    }
}
