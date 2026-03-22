package tn.esprit.workshop.controlleurs.leith.agent.chat;

import javafx.fxml.FXML;

/**
 * Standalone window entry point for the agent chat (same UI as {@link AgentChatWidgetController}).
 * Open with a new {@link javafx.stage.Stage} loading {@code AgentChatWindow.fxml} when needed.
 */
public class AgentChatWindowController extends AgentChatWidgetController {

    @FXML
    @Override
    public void initialize() {
        setStandalone(true);
        super.initialize();
    }
}
