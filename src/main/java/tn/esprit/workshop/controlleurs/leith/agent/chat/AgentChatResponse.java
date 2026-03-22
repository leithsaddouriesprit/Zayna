package tn.esprit.workshop.controlleurs.leith.agent.chat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AgentChatResponse {

    private String reply;
    private String intent;
    private String errorCode;
    private Boolean requiresConfirmation;
    private String pendingActionId;

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public Boolean getRequiresConfirmation() {
        return requiresConfirmation;
    }

    public void setRequiresConfirmation(Boolean requiresConfirmation) {
        this.requiresConfirmation = requiresConfirmation;
    }

    public String getPendingActionId() {
        return pendingActionId;
    }

    public void setPendingActionId(String pendingActionId) {
        this.pendingActionId = pendingActionId;
    }
}
