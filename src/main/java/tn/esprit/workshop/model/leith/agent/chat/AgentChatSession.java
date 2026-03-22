package tn.esprit.workshop.model.leith.agent.chat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Holds agent chat messages for the current application session.
 * Cleared when the agent shell is closed or on logout (see {@link #clear()}).
 */
public final class AgentChatSession {

    private static final AgentChatSession INSTANCE = new AgentChatSession();

    private final List<AgentChatMessage> messages = new ArrayList<>();

    private AgentChatSession() {
    }

    public static AgentChatSession getInstance() {
        return INSTANCE;
    }

    public synchronized List<AgentChatMessage> getMessagesSnapshot() {
        return Collections.unmodifiableList(new ArrayList<>(messages));
    }

    public synchronized void addUserMessage(String text) {
        messages.add(new AgentChatMessage(AgentChatMessage.Role.USER, text));
    }

    public synchronized void addAssistantMessage(String text) {
        messages.add(new AgentChatMessage(AgentChatMessage.Role.ASSISTANT, text));
    }

    /** Proposition d’action (Level 3) : texte + jeton serveur pour confirmation. */
    public synchronized void addAssistantProposal(String text, String pendingActionId) {
        messages.add(new AgentChatMessage(AgentChatMessage.Role.ASSISTANT, text, pendingActionId));
    }

    /** Retire les boutons de confirmation sur une proposition déjà traitée (historique = texte seul). */
    public synchronized void clearProposalButtons(String pendingActionId) {
        if (pendingActionId == null || pendingActionId.isBlank()) {
            return;
        }
        for (int i = 0; i < messages.size(); i++) {
            AgentChatMessage m = messages.get(i);
            if (m.getRole() == AgentChatMessage.Role.ASSISTANT
                    && pendingActionId.equals(m.getPendingActionId())) {
                messages.set(i, new AgentChatMessage(AgentChatMessage.Role.ASSISTANT, m.getText()));
                return;
            }
        }
    }

    public synchronized boolean isEmpty() {
        return messages.isEmpty();
    }

    /**
     * Ensures the welcome line exists exactly once at the start of the session.
     */
    public synchronized void ensureWelcome(String welcomeText) {
        if (messages.isEmpty()) {
            messages.add(new AgentChatMessage(AgentChatMessage.Role.ASSISTANT, welcomeText));
        }
    }

    public synchronized void clear() {
        messages.clear();
    }
}
