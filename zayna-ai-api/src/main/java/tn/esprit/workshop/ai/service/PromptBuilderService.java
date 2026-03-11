package tn.esprit.workshop.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import tn.esprit.workshop.ai.dto.TrackingSnapshotDto;
import tn.esprit.workshop.ai.dto.SelectedChildDto;
import tn.esprit.workshop.ai.dto.SelectedBusDto;
import tn.esprit.workshop.ai.service.TrackingContextService.TrackingContextContract;

import java.util.HashMap;
import java.util.Map;

/**
 * Builds the system + user prompt with strict JSON output instruction.
 */
@Service
public class PromptBuilderService {

    private static final String SYSTEM_PROMPT = """
Tu es l'assistant officiel de ZAYNA (suivi d'enfants dans le bus scolaire).

RÈGLES ABSOLUES:
- Utilise UNIQUEMENT les données du JSON fourni dans CONTEXTE_ZAYNA. N'invente RIEN.
- Si une information demandée n'est pas dans le JSON, indique-le clairement et ne donne pas de valeur inventée.
- Réponds TOUJOURS par un objet JSON valide et rien d'autre (pas de markdown, pas de texte avant/après).

Format de réponse OBLIGATOIRE (un seul objet JSON):
{
  "intent": "ON_BOARD_STATUS | CHILD_LOCATION | BUS_LOCATION | ETA | SAFETY_STATUS | GENERAL_HELP | MISSING_CONTEXT",
  "answer_fr": "ta réponse en français",
  "answer_en": "optional English answer",
  "facts_used": { "childId": null, "onBoard": null, "busLat": null, "busLng": null, "speedKmh": null, "distanceToNextStopKm": null, "etaMinutes": null, "lastUpdateTime": null },
  "missing_fields": [],
  "confidence": 0.0 à 1.0,
  "suggested_actions": []
}

- intent: selon la question (où est mon enfant -> CHILD_LOCATION, est-il dans le bus -> ON_BOARD_STATUS, ETA -> ETA, ça va -> SAFETY_STATUS, etc.). Si donnée manquante -> MISSING_CONTEXT.
- facts_used: ne mets que les clés dont tu as utilisé la valeur du contexte (pas d'invention).
- missing_fields: liste des champs manquants dans le contexte pour répondre complètement.
- suggested_actions: actions conseillées (ex: "Sélectionnez un enfant", "Actualisez le suivi").
""";

    private final ObjectMapper mapper = new ObjectMapper();

    public String buildPrompt(String userMessage, TrackingContextContract contract) {
        Map<String, Object> context = new HashMap<>();
        if (contract.selectedChild != null) context.put("selectedChild", toMap(contract.selectedChild));
        if (contract.selectedBus != null) context.put("selectedBus", toMap(contract.selectedBus));
        if (contract.trackingSnapshot != null) context.put("trackingSnapshot", toMap(contract.trackingSnapshot));
        if (contract.dataQuality != null) {
            Map<String, Object> dq = new HashMap<>();
            dq.put("isTestMode", contract.dataQuality.getIsTestMode());
            dq.put("isStale", contract.dataQuality.getIsStale());
            dq.put("missingFields", contract.dataQuality.getMissingFields());
            context.put("dataQuality", dq);
        }

        String contextJson;
        try {
            contextJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(context);
        } catch (Exception e) {
            contextJson = "{}";
        }

        return SYSTEM_PROMPT + """

CONTEXTE_ZAYNA (JSON):
""" + contextJson + """

QUESTION_UTILISATEUR: """ + userMessage + """

Réponds par un seul objet JSON valide, sans aucun texte avant ou après.""" + "\n";
    }

    /** Repair prompt when model returned invalid JSON. */
    public String buildRepairPrompt(String invalidOutput) {
        return "Tu as répondu avec du texte qui n'est pas un JSON valide. Réponds UNIQUEMENT par un objet JSON valide, sans markdown, sans ```, sans texte avant ou après. Ta réponse précédente était:\n" + invalidOutput + "\nCorrige et renvoie uniquement le JSON.";
    }

    private Map<String, Object> toMap(Object dto) {
        return mapper.convertValue(dto, Map.class);
    }
}
