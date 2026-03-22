package tn.esprit.workshop.controlleurs.leith.agent.chat;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.workshop.model.leith.agent.chat.AgentChatMessage;
import tn.esprit.workshop.model.leith.agent.chat.AgentChatSession;
import tn.esprit.workshop.utilis.AppSession;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Assistant agent (shell) : UI + appels zayna-ai-api. Lecture (Level 2) et actions confirmées (Level 3).
 * État : {@link AgentChatSession}.
 */
public class AgentChatWidgetController {

    private static final Logger LOG = Logger.getLogger(AgentChatWidgetController.class.getName());

    private static final String ASSISTANT_WELCOME =
            "👋 Bonjour, je suis votre assistant Zayna. Comment puis-je vous aider ?";
    private static final double BUBBLE_MAX_WIDTH = 360.0;

    private final AgentChatApiClient agentChatApiClient = new AgentChatApiClient();

    @FXML
    protected ScrollPane scrollMessages;
    @FXML
    protected VBox boxMessages;
    @FXML
    protected TextField tfMessage;
    @FXML
    protected Button btnSend;

    private Runnable embeddedModeOnClose;
    private boolean standalone;

    private HBox typingIndicatorRow;
    private Label typingLabel;
    private Timeline typingDotsTimeline;
    private final int[] typingDotStep = {0};

    public void setStandalone(boolean standalone) {
        this.standalone = standalone;
    }

    /** When embedded in AgentShell, Retour runs this instead of closing a stage. */
    public void setEmbeddedMode(Runnable onClose) {
        this.embeddedModeOnClose = onClose;
    }

    @FXML
    public void initialize() {
        AgentChatSession.getInstance().ensureWelcome(ASSISTANT_WELCOME);
        rebuildUIFromSession();
    }

    private void rebuildUIFromSession() {
        if (boxMessages == null) {
            return;
        }
        boxMessages.getChildren().clear();
        removeTypingIndicator();
        for (AgentChatMessage m : AgentChatSession.getInstance().getMessagesSnapshot()) {
            if (m.getRole() == AgentChatMessage.Role.ASSISTANT) {
                if (m.hasPendingAction()) {
                    appendAssistantProposalRowToUi(m.getText(), m.getPendingActionId());
                } else {
                    appendAssistantRowToUi(m.getText());
                }
            } else {
                appendUserRowToUi(m.getText());
            }
        }
        scrollChatToBottom();
    }

    private ImageView createAssistantAvatar() {
        java.net.URL url = AgentChatWidgetController.class.getResource("/images/top.png");
        if (url == null) {
            url = AgentChatWidgetController.class.getResource("/leith/design/top.png");
        }
        ImageView avatar = new ImageView();
        if (url != null) {
            avatar.setImage(new Image(url.toExternalForm(), 30, 30, true, true));
        }
        avatar.setFitWidth(30);
        avatar.setFitHeight(30);
        avatar.setPreserveRatio(true);
        avatar.getStyleClass().add("chat-msg-assistant-avatar");
        return avatar;
    }

    private void appendAssistantRowToUi(String text) {
        if (boxMessages == null || text == null) {
            return;
        }
        HBox row = new HBox(10);
        row.setAlignment(Pos.TOP_LEFT);
        row.setMaxWidth(Double.MAX_VALUE);
        row.getStyleClass().add("chat-msg-assistant-row");
        ImageView avatar = createAssistantAvatar();
        Label bubble = new Label(text);
        bubble.setWrapText(true);
        bubble.setMaxWidth(BUBBLE_MAX_WIDTH);
        bubble.getStyleClass().add("chat-msg-assistant-bubble");
        row.getChildren().addAll(avatar, bubble);
        boxMessages.getChildren().add(row);
    }

    /** Proposition d’action (Level 3) : même bulle assistant + Confirmer / Annuler. */
    private void appendAssistantProposalRowToUi(String text, String pendingActionId) {
        if (boxMessages == null || text == null || pendingActionId == null || pendingActionId.isBlank()) {
            return;
        }
        HBox row = new HBox(10);
        row.setAlignment(Pos.TOP_LEFT);
        row.setMaxWidth(Double.MAX_VALUE);
        row.getStyleClass().add("chat-msg-assistant-row");
        ImageView avatar = createAssistantAvatar();
        Label bubble = new Label(text);
        bubble.setWrapText(true);
        bubble.setMaxWidth(BUBBLE_MAX_WIDTH);
        bubble.getStyleClass().add("chat-msg-assistant-bubble");
        Button btnConfirm = new Button("Confirmer");
        Button btnCancel = new Button("Annuler");
        btnConfirm.getStyleClass().addAll("agent-chat-action-btn", "agent-chat-confirm-btn");
        btnCancel.getStyleClass().addAll("agent-chat-action-btn", "agent-chat-cancel-btn");
        final String pid = pendingActionId.trim();
        btnConfirm.setOnAction(ev -> confirmPendingAction(pid, true, btnConfirm, btnCancel));
        btnCancel.setOnAction(ev -> confirmPendingAction(pid, false, btnConfirm, btnCancel));
        HBox actions = new HBox(10, btnConfirm, btnCancel);
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.setPadding(new Insets(8, 0, 0, 0));
        VBox col = new VBox(bubble, actions);
        col.setMaxWidth(BUBBLE_MAX_WIDTH + 24);
        row.getChildren().addAll(avatar, col);
        boxMessages.getChildren().add(row);
    }

    private void appendUserRowToUi(String text) {
        if (boxMessages == null || text == null) {
            return;
        }
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_RIGHT);
        row.setMaxWidth(Double.MAX_VALUE);
        row.getStyleClass().add("chat-msg-user-row");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label bubble = new Label("Moi: " + text);
        bubble.setWrapText(true);
        bubble.setMaxWidth(BUBBLE_MAX_WIDTH);
        bubble.getStyleClass().add("chat-msg-user-bubble");
        row.getChildren().addAll(spacer, bubble);
        boxMessages.getChildren().add(row);
    }

    private void showTypingIndicator() {
        if (boxMessages == null) {
            return;
        }
        removeTypingIndicator();
        typingDotStep[0] = 0;
        HBox row = new HBox(10);
        row.setAlignment(Pos.TOP_LEFT);
        row.setMaxWidth(Double.MAX_VALUE);
        row.getStyleClass().add("chat-msg-assistant-row");
        ImageView avatar = createAssistantAvatar();
        typingLabel = new Label("Zayna est en train d'écrire");
        typingLabel.setWrapText(false);
        typingLabel.setMaxWidth(BUBBLE_MAX_WIDTH);
        typingLabel.getStyleClass().addAll("chat-msg-assistant-bubble", "chat-msg-typing");
        row.getChildren().addAll(avatar, typingLabel);
        typingIndicatorRow = row;
        boxMessages.getChildren().add(row);
        typingDotsTimeline = new Timeline(new KeyFrame(Duration.millis(450), ev -> {
            if (typingLabel == null) {
                return;
            }
            String[] dots = {"", ".", "..", "..."};
            typingLabel.setText("Zayna est en train d'écrire" + dots[typingDotStep[0] % dots.length]);
            typingDotStep[0]++;
        }));
        typingDotsTimeline.setCycleCount(Timeline.INDEFINITE);
        typingDotsTimeline.play();
        scrollChatToBottom();
    }

    private void removeTypingIndicator() {
        if (typingDotsTimeline != null) {
            typingDotsTimeline.stop();
            typingDotsTimeline = null;
        }
        if (typingIndicatorRow != null && boxMessages != null) {
            boxMessages.getChildren().remove(typingIndicatorRow);
        }
        typingIndicatorRow = null;
        typingLabel = null;
    }

    private void scrollChatToBottom() {
        if (scrollMessages == null) {
            return;
        }
        Platform.runLater(() -> {
            scrollMessages.applyCss();
            scrollMessages.layout();
            scrollMessages.setVvalue(1.0);
            PauseTransition settle = new PauseTransition(Duration.millis(40));
            settle.setOnFinished(e -> {
                scrollMessages.setVvalue(1.0);
                scrollMessages.requestLayout();
            });
            settle.play();
        });
    }

    private void confirmPendingAction(String pendingActionId, boolean confirm, Button btnConfirm, Button btnCancel) {
        if (pendingActionId == null || pendingActionId.isBlank()) {
            return;
        }
        if (btnConfirm != null) {
            btnConfirm.setDisable(true);
        }
        if (btnCancel != null) {
            btnCancel.setDisable(true);
        }
        Integer userId = AppSession.getInstance().getConnectedUserId();
        if (userId == null || userId <= 0) {
            if (btnConfirm != null) {
                btnConfirm.setDisable(false);
            }
            if (btnCancel != null) {
                btnCancel.setDisable(false);
            }
            appendAssistantRowToUi(AgentChatErrorHandler.userMessageForInvalidSession());
            scrollChatToBottom();
            return;
        }

        String userLine = confirm ? "Confirmer" : "Annuler";
        AgentChatSession.getInstance().addUserMessage(userLine);
        appendUserRowToUi(userLine);
        if (btnSend != null) {
            btnSend.setDisable(true);
        }
        if (tfMessage != null) {
            tfMessage.setDisable(true);
        }
        showTypingIndicator();

        final String pid = pendingActionId.trim();
        new Thread(() -> {
            String assistantText;
            boolean needsSecondConfirm = false;
            String secondPendingId = null;
            try {
                AgentChatRequest req = new AgentChatRequest();
                req.userId = userId;
                req.sessionId = "javafx-agent-confirm-" + System.currentTimeMillis();
                req.confirmPendingActionId = pid;
                req.confirmAction = confirm;
                AgentChatResponse res = agentChatApiClient.ask(req);
                needsSecondConfirm = Boolean.TRUE.equals(res.getRequiresConfirmation());
                secondPendingId = res.getPendingActionId();
                String r = res.getReply();
                if (r == null || r.isBlank()) {
                    assistantText = AgentChatErrorHandler.emptyOrUnreadableReply();
                } else {
                    assistantText = r.trim();
                }
            } catch (AgentChatApiException ex) {
                LOG.log(Level.FINE, "Agent chat API confirm error", ex);
                assistantText = AgentChatErrorHandler.userMessageForNetworkOrUnknown();
            } catch (Exception ex) {
                LOG.log(Level.WARNING, "Agent chat confirm unexpected error", ex);
                assistantText = AgentChatErrorHandler.userMessageForNetworkOrUnknown();
            }
            String finalAssistantText = assistantText;
            boolean finalNeedsSecond = needsSecondConfirm;
            String finalSecondPending = secondPendingId;
            Platform.runLater(() -> {
                removeTypingIndicator();
                AgentChatSession.getInstance().clearProposalButtons(pid);
                if (finalNeedsSecond && finalSecondPending != null && !finalSecondPending.isBlank()) {
                    AgentChatSession.getInstance().addAssistantProposal(
                            finalAssistantText, finalSecondPending.trim());
                } else {
                    AgentChatSession.getInstance().addAssistantMessage(finalAssistantText);
                }
                rebuildUIFromSession();
                if (btnSend != null) {
                    btnSend.setDisable(false);
                }
                if (tfMessage != null) {
                    tfMessage.setDisable(false);
                }
                scrollChatToBottom();
            });
        }, "agent-chat-confirm").start();
    }

    @FXML
    void send(ActionEvent event) {
        if (tfMessage == null) {
            return;
        }
        String msg = tfMessage.getText();
        if (msg == null || msg.isBlank()) {
            return;
        }
        Integer userId = AppSession.getInstance().getConnectedUserId();
        if (userId == null || userId <= 0) {
            appendAssistantRowToUi(AgentChatErrorHandler.userMessageForInvalidSession());
            return;
        }

        AgentChatSession.getInstance().addUserMessage(msg);
        appendUserRowToUi(msg);
        tfMessage.clear();
        if (btnSend != null) {
            btnSend.setDisable(true);
        }
        tfMessage.setDisable(true);
        showTypingIndicator();

        new Thread(() -> {
            String assistantText;
            boolean needsConfirm = false;
            String pendingId = null;
            try {
                AgentChatRequest req = new AgentChatRequest();
                req.message = msg;
                req.userId = userId;
                req.sessionId = "javafx-agent-" + System.currentTimeMillis();
                AgentChatResponse res = agentChatApiClient.ask(req);
                needsConfirm = Boolean.TRUE.equals(res.getRequiresConfirmation());
                pendingId = res.getPendingActionId();
                String r = res.getReply();
                if (r == null || r.isBlank()) {
                    assistantText = AgentChatErrorHandler.emptyOrUnreadableReply();
                } else {
                    assistantText = r.trim();
                }
            } catch (AgentChatApiException ex) {
                LOG.log(Level.FINE, "Agent chat API error", ex);
                assistantText = AgentChatErrorHandler.userMessageForNetworkOrUnknown();
            } catch (Exception ex) {
                LOG.log(Level.WARNING, "Agent chat unexpected error", ex);
                assistantText = AgentChatErrorHandler.userMessageForNetworkOrUnknown();
            }
            String finalAssistantText = assistantText;
            boolean finalNeedsConfirm = needsConfirm;
            String finalPendingId = pendingId;
            Platform.runLater(() -> {
                removeTypingIndicator();
                if (finalNeedsConfirm && finalPendingId != null && !finalPendingId.isBlank()) {
                    String token = finalPendingId.trim();
                    AgentChatSession.getInstance().addAssistantProposal(finalAssistantText, token);
                    appendAssistantProposalRowToUi(finalAssistantText, token);
                } else {
                    AgentChatSession.getInstance().addAssistantMessage(finalAssistantText);
                    appendAssistantRowToUi(finalAssistantText);
                }
                if (btnSend != null) {
                    btnSend.setDisable(false);
                }
                if (tfMessage != null) {
                    tfMessage.setDisable(false);
                }
                scrollChatToBottom();
            });
        }, "agent-chat-api").start();
    }

    @FXML
    void goBack(ActionEvent event) {
        if (embeddedModeOnClose != null) {
            embeddedModeOnClose.run();
        } else if (standalone && scrollMessages != null && scrollMessages.getScene() != null
                && scrollMessages.getScene().getWindow() instanceof Stage st) {
            st.close();
        } else if (scrollMessages != null && scrollMessages.getScene() != null
                && scrollMessages.getScene().getWindow() instanceof Stage st) {
            st.close();
        }
    }
}
