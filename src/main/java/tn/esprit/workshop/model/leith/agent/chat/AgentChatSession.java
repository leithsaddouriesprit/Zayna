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
