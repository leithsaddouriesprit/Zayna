package tn.esprit.workshop.controlleurs.leith.agent.chat;

/**
 * User-visible messages for agent chat API failures.
 */
public final class AgentChatErrorHandler {

    public static final String SERVICE_UNAVAILABLE =
            "Le service assistant agent est momentanément indisponible.";

    private AgentChatErrorHandler() {
    }

    public static String userMessageForNetworkOrUnknown() {
        return SERVICE_UNAVAILABLE;
    }

    /**
     * HTTP 200 mais corps vide ou réponse illisible alors que l’API a répondu (cas limite).
     */
    public static String emptyOrUnreadableReply() {
        return "Je n'ai pas reçu de réponse exploitable. Pouvez-vous reformuler votre question ?";
    }

    public static String userMessageForInvalidSession() {
        return "Session agent invalide. Reconnectez-vous.";
    }
}
