package tn.esprit.workshop.controlleurs.leith.agent.chat;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgentChatRequest {

    public String message;

    @JsonProperty("userId")
    public Integer userId;

    public String sessionId;

    /** Jeton renvoyé avec une proposition d'action ; avec {@link #confirmAction} pour confirmer ou annuler. */
    @JsonProperty("confirmPendingActionId")
    public String confirmPendingActionId;

    @JsonProperty("confirmAction")
    public Boolean confirmAction;
}
