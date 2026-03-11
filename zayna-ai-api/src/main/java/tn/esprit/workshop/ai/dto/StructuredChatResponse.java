package tn.esprit.workshop.ai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Strict JSON structure returned by the AI pipeline.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StructuredChatResponse {
    private String intent;
    private String answer_fr;
    private String answer_en;
    private String answer_ar;
    private Map<String, Object> facts_used = new HashMap<>();
    private List<String> missing_fields = new ArrayList<>();
    private Double confidence;
    private List<String> suggested_actions = new ArrayList<>();

    /** Backward compatibility: primary answer (answer_fr or answer_en). */
    private String reply;

    public StructuredChatResponse() {}

    public static StructuredChatResponse missingContext(String messageFr, List<String> missingFields, List<String> suggestedActions) {
        StructuredChatResponse r = new StructuredChatResponse();
        r.setIntent(Intent.MISSING_CONTEXT.name());
        r.setAnswer_fr(messageFr);
        r.setMissing_fields(missingFields != null ? missingFields : new ArrayList<>());
        r.setSuggested_actions(suggestedActions != null ? suggestedActions : new ArrayList<>());
        r.setConfidence(1.0);
        r.setReply(messageFr);
        return r;
    }

    public static StructuredChatResponse testMode(String messageFr, List<String> suggestedActions) {
        StructuredChatResponse r = new StructuredChatResponse();
        r.setIntent(Intent.MISSING_CONTEXT.name());
        r.setAnswer_fr(messageFr);
        r.setSuggested_actions(suggestedActions != null ? suggestedActions : new ArrayList<>());
        r.setConfidence(1.0);
        r.setReply(messageFr);
        return r;
    }

    public String getIntent() { return intent; }
    public void setIntent(String intent) { this.intent = intent; }
    public String getAnswer_fr() { return answer_fr; }
    public void setAnswer_fr(String answer_fr) { this.answer_fr = answer_fr; this.reply = answer_fr; }
    public String getAnswer_en() { return answer_en; }
    public void setAnswer_en(String answer_en) { this.answer_en = answer_en; }
    public String getAnswer_ar() { return answer_ar; }
    public void setAnswer_ar(String answer_ar) { this.answer_ar = answer_ar; }
    public Map<String, Object> getFacts_used() { return facts_used; }
    public void setFacts_used(Map<String, Object> facts_used) { this.facts_used = facts_used != null ? facts_used : new HashMap<>(); }
    public List<String> getMissing_fields() { return missing_fields; }
    public void setMissing_fields(List<String> missing_fields) { this.missing_fields = missing_fields != null ? missing_fields : new ArrayList<>(); }
    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }
    public List<String> getSuggested_actions() { return suggested_actions; }
    public void setSuggested_actions(List<String> suggested_actions) { this.suggested_actions = suggested_actions != null ? suggested_actions : new ArrayList<>(); }
    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }
}
