package tn.esprit.workshop.controlleurs.leith.agent.chat;

public class AgentChatApiException extends Exception {

    public AgentChatApiException(String message) {
        super(message);
    }

    public AgentChatApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
