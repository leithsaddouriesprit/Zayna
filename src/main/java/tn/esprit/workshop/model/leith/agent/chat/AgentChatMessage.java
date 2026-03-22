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

    public AgentChatMessage(Role role, String text) {
        this.role = Objects.requireNonNull(role, "role");
        this.text = Objects.requireNonNull(text, "text");
    }

    public Role getRole() {
        return role;
    }

    public String getText() {
        return text;
    }
}
