package tn.esprit.workshop.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import tn.esprit.workshop.ai.dto.Intent;
import tn.esprit.workshop.ai.dto.StructuredChatResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses LLM raw output into StructuredChatResponse. Handles invalid JSON with repair step
 * and deterministic fallback when repair fails.
 */
@Service
public class ResponseParserService {

    private static final Pattern JSON_BLOCK = Pattern.compile("\\s*([\\{\\[].*[\\}\\]])", Pattern.DOTALL);
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Parse raw model output to StructuredChatResponse. Returns null if parsing failed
     * (caller may retry with repair prompt or use fallback).
     */
    public StructuredChatResponse parse(String rawOutput) {
        if (rawOutput == null || rawOutput.isBlank()) return null;
        String trimmed = rawOutput.trim();
        // Strip markdown code block if present
        if (trimmed.startsWith("```")) {
            int start = trimmed.indexOf("\n");
            if (start > 0) trimmed = trimmed.substring(start + 1);
            int end = trimmed.indexOf("```");
            if (end > 0) trimmed = trimmed.substring(0, end);
        }
        Matcher m = JSON_BLOCK.matcher(trimmed);
        String jsonStr = m.find() ? m.group(1).trim() : trimmed;
        try {
            JsonNode root = mapper.readTree(jsonStr);
            StructuredChatResponse r = new StructuredChatResponse();
            r.setIntent(getText(root, "intent"));
            r.setAnswer_fr(getText(root, "answer_fr"));
            r.setAnswer_en(getText(root, "answer_en"));
            r.setReply(r.getAnswer_fr() != null ? r.getAnswer_fr() : r.getAnswer_en());
            r.setConfidence(getDouble(root, "confidence"));
            r.setMissing_fields(getStringList(root, "missing_fields"));
            r.setSuggested_actions(getStringList(root, "suggested_actions"));
            if (root.has("facts_used") && root.get("facts_used").isObject()) {
                r.setFacts_used(mapper.convertValue(root.get("facts_used"), java.util.Map.class));
            }
            return r;
        } catch (Exception e) {
            return null;
        }
    }

    /** Deterministic fallback when LLM output is invalid after repair. */
    public StructuredChatResponse fallbackResponse(String userMessage) {
        StructuredChatResponse r = new StructuredChatResponse();
        r.setIntent(Intent.GENERAL_HELP.name());
        r.setAnswer_fr("Désolé, je n'ai pas pu traiter votre question. Veuillez réessayer ou reformuler.");
        r.setReply(r.getAnswer_fr());
        r.setConfidence(0.0);
        r.setSuggested_actions(List.of("Réessayez", "Reformulez votre question"));
        return r;
    }

    private String getText(JsonNode n, String key) {
        if (!n.has(key)) return null;
        JsonNode v = n.get(key);
        return v != null && v.isTextual() ? v.asText() : null;
    }

    private Double getDouble(JsonNode n, String key) {
        if (!n.has(key)) return null;
        JsonNode v = n.get(key);
        if (v == null) return null;
        if (v.isNumber()) return v.asDouble();
        if (v.isTextual()) {
            try { return Double.parseDouble(v.asText()); } catch (NumberFormatException e) { return null; }
        }
        return null;
    }

    private List<String> getStringList(JsonNode n, String key) {
        List<String> list = new ArrayList<>();
        if (!n.has(key) || !n.get(key).isArray()) return list;
        for (JsonNode item : n.get(key)) {
            if (item.isTextual()) list.add(item.asText());
        }
        return list;
    }
}
