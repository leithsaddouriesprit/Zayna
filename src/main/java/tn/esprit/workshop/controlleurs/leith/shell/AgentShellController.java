package tn.esprit.workshop.controlleurs.leith.shell;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.workshop.controlleurs.leith.SceneNavigator;
import tn.esprit.workshop.controlleurs.leith.agent.chat.AgentChatWidgetController;
import tn.esprit.workshop.model.leith.agent.chat.AgentChatSession;
import tn.esprit.workshop.services.leith.CandidatureAgentService;
import tn.esprit.workshop.utilis.AppSession;

import java.util.logging.Level;
import java.util.logging.Logger;

public class AgentShellController {

    private static final Logger LOG = Logger.getLogger(AgentShellController.class.getName());

    private boolean agentChatLoaded;

    @FXML private StackPane contentArea;
    @FXML private Label lblAccountInfo;
    @FXML private VBox agentNavBody;
    @FXML private VBox agentChatPanel;
    @FXML private StackPane agentChatContent;

    @FXML
    public void initialize() {
        SceneNavigator.registerAgentShell(this);
        updateAccountLabel();
        Platform.runLater(() -> {
            if (contentArea != null && contentArea.getScene() != null && contentArea.getScene().getWindow() instanceof Stage) {
                ((Stage) contentArea.getScene().getWindow()).setOnHidden(e -> {
                    AgentChatSession.getInstance().clear();
                    SceneNavigator.unregisterAgentShell();
                });
            }
        });
        Integer uid = AppSession.getInstance().getConnectedUserId();
        if (uid != null && !new CandidatureAgentService().isAccessApproved(uid)) {
            if (agentNavBody != null) {
                agentNavBody.setVisible(false);
                agentNavBody.setManaged(false);
            }
            loadContent("/leith/agent/AgentCandidatureEcolePending.fxml");
            return;
        }
        if (agentNavBody != null) {
            agentNavBody.setVisible(true);
            agentNavBody.setManaged(true);
        }
        loadContent("/leith/agent/AgentDashboard.fxml");
    }

    @FXML
    void toggleAgentChat() {
        if (agentChatPanel == null) {
            return;
        }
        boolean show = !agentChatPanel.isVisible();
        agentChatPanel.setVisible(show);
        agentChatPanel.setManaged(show);
        if (show && !agentChatLoaded) {
            loadAgentChatWidget();
        }
    }

    private void loadAgentChatWidget() {
        if (agentChatContent == null) {
            return;
        }
        agentChatLoaded = true;
        try {
            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource("/leith/agent/chat/AgentChatWidget.fxml"));
            Parent root = loader.load();
            Object controller = loader.getController();
            if (controller instanceof AgentChatWidgetController) {
                AgentChatWidgetController chat = (AgentChatWidgetController) controller;
                chat.setEmbeddedMode(this::hideAgentChatPanel);
            }
            agentChatContent.getChildren().clear();
            agentChatContent.getChildren().add(root);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to load agent chat widget", e);
            agentChatLoaded = false;
        }
    }

    private void hideAgentChatPanel() {
        if (agentChatPanel != null) {
            agentChatPanel.setVisible(false);
            agentChatPanel.setManaged(false);
        }
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
    void openProgrammes() {
        loadContent("/amal/Programme.fxml");
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
    void openEnfants() {
        loadContent("/leith/agent/AgentEnfants.fxml");
    }

    @FXML
    void openNouvelleMaitresse() {
        loadContent("/leith/agent/AgentNouvelleMaitresse.fxml");
    }

    @FXML
    void openMaitressesExistantes() {
        loadContent("/leith/agent/AgentMaitressesListe.fxml");
    }

    @FXML
    void openGestionReclamations() {
        loadContent("/leith/agent/AgentReclamationsPlaceholder.fxml");
    }

    @FXML
    void logout() {
        AgentChatSession.getInstance().clear();
        SceneNavigator.unregisterAgentShell();
        Stage stage = (Stage) contentArea.getScene().getWindow();
        if (stage != null) stage.close();
        SceneNavigator.showLoginWindow();
    }
}
