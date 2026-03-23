package tn.esprit.workshop.controlleurs.leith.shell;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import tn.esprit.workshop.utilis.AppSession;
import tn.esprit.workshop.controlleurs.leith.SceneNavigator;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AgentShellController {

    private static final Logger LOG = Logger.getLogger(AgentShellController.class.getName());

    @FXML private StackPane contentArea;
    @FXML private Label lblAccountInfo;

    @FXML
    public void initialize() {
        SceneNavigator.registerAgentShell(this);
        updateAccountLabel();
        Platform.runLater(() -> {
            if (contentArea != null && contentArea.getScene() != null && contentArea.getScene().getWindow() instanceof Stage) {
                ((Stage) contentArea.getScene().getWindow()).setOnHidden(e -> SceneNavigator.unregisterAgentShell());
            }
        });
        loadContent("/leith/agent/AgentDashboard.fxml");
    }

    private void updateAccountLabel() {
        if (lblAccountInfo != null) {
            String name = AppSession.getInstance().getConnectedUserName();
            String role = AppSession.getInstance().getConnectedUserRole();
            lblAccountInfo.setText((name.isEmpty() ? "Agent École" : name) + " (" + (role.isEmpty() ? "Agent École" : role) + ")");
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
        loadContent("/leith/agent/AgentDashboard.fxml");
    }

    @FXML
    void openCandidaturesChauffeur() {
        loadContent("/leith/agent/AgentCandidaturesChauffeur.fxml");
    }

    @FXML
    void openChauffeursAcceptes() {
        loadContent("/leith/agent/AgentChauffeursAcceptes.fxml");
    }

    @FXML
    void openBus() {
        loadContent("/leith/agent/AgentBus.fxml");
    }

    @FXML
    void openAffecterChauffeurBus() {
        loadContent("/leith/agent/AgentAffecterChauffeurBus.fxml");
    }

    @FXML
    void openTrajets() {
        loadContent("/leith/agent/AgentTrajets.fxml");
    }

    @FXML
    void openAffecterBusTrajet() {
        loadContent("/leith/agent/AgentAffecterBusTrajet.fxml");
    }

    @FXML
    void openArrets() {
        loadContent("/leith/agent/AgentArrets.fxml");
    }

    @FXML
    void openCandidaturesEnfant() {
        loadContent("/leith/agent/AgentCandidaturesEnfant.fxml");
    }

    @FXML
    private void openReclamation() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GestionReclamation.fxml"));
            Parent root = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openReponses() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GestionReponse.fxml"));
            Parent root = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    void logout() {
        SceneNavigator.unregisterAgentShell();
        Stage stage = (Stage) contentArea.getScene().getWindow();
        if (stage != null) stage.close();
        SceneNavigator.showLoginWindow();
    }
}
