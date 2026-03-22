package tn.esprit.workshop.model.leith.agent.chat;

import java.util.Objects;

/**
 * In-memory chat line for the agent assistant (session only; no persistence in prompt 1).
 */
public final class AgentChatMessage {

    public enum Role {
        USER,
        ASSISTANT
    }

    private final Role role;
    private final String text;
    /** Non-null = proposition Level 3 : l’UI affiche Confirmer / Annuler jusqu’à réponse. */
    private final String pendingActionId;

    public AgentChatMessage(Role role, String text) {
        this(role, text, null);
    }

    public AgentChatMessage(Role role, String text, String pendingActionId) {
        this.role = Objects.requireNonNull(role, "role");
        this.text = Objects.requireNonNull(text, "text");
        this.pendingActionId = pendingActionId;
    }

    public Role getRole() {
        return role;
    }

    public String getText() {
        return text;
    }

    public String getPendingActionId() {
        return pendingActionId;
    }

    public boolean hasPendingAction() {
        return pendingActionId != null && !pendingActionId.isBlank();
    }
}
