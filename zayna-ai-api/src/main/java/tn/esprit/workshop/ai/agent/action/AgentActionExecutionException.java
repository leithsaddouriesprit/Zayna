package tn.esprit.workshop.ai.agent.action;

/**
 * Échec métier contrôlé (message affichable tel quel à l’agent).
 */
public class AgentActionExecutionException extends Exception {

    public AgentActionExecutionException(String userMessage) {
        super(userMessage);
    }
}
