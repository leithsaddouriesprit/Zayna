package tn.esprit.workshop.ai.agent.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Agent chat request from JavaFX. School scope is resolved server-side from {@code userId}.
 */
public class AgentChatRequestDto {

    /** User message in natural language (French). */
    public String message;

    /** users.id of the connected RESPONSABLEECOLE / agent account. */
    @JsonProperty("userId")
    public Integer userId;

    /** Optional client session id for logging only (not trusted for auth). */
    public String sessionId;

    /**
     * Level 3 : jeton renvoyé avec une proposition d'action ({@code ACTION_PROPOSAL}).
     * Avec {@link #confirmAction} obligatoire : {@code true} = exécuter, {@code false} = annuler.
     */
    @JsonProperty("confirmPendingActionId")
    public String confirmPendingActionId;

    @JsonProperty("confirmAction")
    public Boolean confirmAction;
}
