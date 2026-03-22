package tn.esprit.workshop.ai.agent.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgentChatResponseDto {

    private String reply;
    private String intent;
    private String errorCode;

    public static AgentChatResponseDto ok(String reply, String intent) {
        AgentChatResponseDto r = new AgentChatResponseDto();
        r.reply = reply;
        r.intent = intent;
        return r;
    }

    public static AgentChatResponseDto error(String reply, String errorCode) {
        AgentChatResponseDto r = new AgentChatResponseDto();
        r.reply = reply;
        r.errorCode = errorCode;
        return r;
    }

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
}
