package tn.esprit.workshop.controlleurs.leith.shell;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.workshop.controlleurs.leith.AI.ChatAIController;
import tn.esprit.workshop.controlleurs.leith.SceneNavigator;
import tn.esprit.workshop.utilis.AppSession;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ParentShellController {

    private static final Logger LOG = Logger.getLogger(ParentShellController.class.getName());
    private boolean chatLoaded;

    @FXML private StackPane contentArea;
    @FXML private Label lblAccountInfo;
    @FXML private VBox chatPanel;
    @FXML private StackPane chatContent;
    @FXML private Button btnChatFab;

    @FXML
    public void initialize() {
        SceneNavigator.registerParentShell(this);
        Platform.runLater(() -> {
            if (contentArea != null && contentArea.getScene() != null && contentArea.getScene().getWindow() instanceof Stage) {
                ((Stage) contentArea.getScene().getWindow()).setOnHidden(e -> SceneNavigator.unregisterParentShell());
            }
        });
        updateAccountLabel();
        loadContent("/leith/parent/ParentDashboard.fxml");
    }

    private void updateAccountLabel() {
        if (lblAccountInfo != null) {
            String name = AppSession.getInstance().getConnectedUserName();
            String role = AppSession.getInstance().getConnectedUserRole();
            lblAccountInfo.setText((name.isEmpty() ? "Parent" : name) + " (" + (role.isEmpty() ? "Parent" : role) + ")");
        }
    }

    /** Loads an FXML into the center content area. Called by SceneNavigator for in-window navigation. */
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
        loadContent("/leith/parent/ParentDashboard.fxml");
    }

    @FXML
    void openMesEnfants() {
        loadContent("/leith/ParentHome.fxml");
    }

    @FXML
    void openDemandeTransport() {
        loadContent("/leith/parent/ParentDemandeTransport.fxml");
    }

    @FXML
    void openSuiviCandidatures() {
        loadContent("/leith/parent/ParentSuiviCandidaturesEnfant.fxml");
    }

    @FXML
    void toggleChat() {
        if (chatPanel == null) return;
        boolean show = !chatPanel.isVisible();
        chatPanel.setVisible(show);
        chatPanel.setManaged(show);
        if (show && !chatLoaded) {
            loadChatWidget();
        }
    }

    private void loadChatWidget() {
        if (chatContent == null) return;
        chatLoaded = true;
        try {
            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource("/leith/AI/ChatAI.fxml"));
            Parent root = loader.load();
            Object controller = loader.getController();
            if (controller instanceof ChatAIController) {
                ChatAIController chat = (ChatAIController) controller;
                chat.setEmbeddedMode(this::hideChatPanel);
                chat.init(null);
            }
            chatContent.getChildren().clear();
            chatContent.getChildren().add(root);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to load chat widget", e);
            chatLoaded = false;
        }
    }

    void hideChatPanel() {
        if (chatPanel != null) {
            chatPanel.setVisible(false);
            chatPanel.setManaged(false);
        }
    }

    @FXML
    void logout() {
        SceneNavigator.unregisterParentShell();
        Stage stage = contentArea != null && contentArea.getScene() != null ? (Stage) contentArea.getScene().getWindow() : null;
        if (stage != null) stage.close();
        SceneNavigator.showLoginWindow();
    }
}
