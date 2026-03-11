package tn.esprit.workshop.ai.controller;

import org.springframework.web.bind.annotation.*;
import tn.esprit.workshop.ai.dto.*;
import tn.esprit.workshop.ai.service.*;

import java.util.Locale;
import java.util.Set;

@RestController
@RequestMapping("/ai")
public class AiController {

    private final OllamaClient ollama;
    private final ContextBuilderService contextBuilder;
    private final TrackingContextService trackingContextService;
    private final ContextValidator contextValidator;
    private final IntentDetectionService intentDetection;
    private final DeterministicAnswerService deterministicAnswer;
    private final PromptBuilderService promptBuilder;
    private final ResponseParserService responseParser;

    public AiController(OllamaClient ollama,
                        ContextBuilderService contextBuilder,
                        TrackingContextService trackingContextService,
                        ContextValidator contextValidator,
                        IntentDetectionService intentDetection,
                        DeterministicAnswerService deterministicAnswer,
                        PromptBuilderService promptBuilder,
                        ResponseParserService responseParser) {
        this.ollama = ollama;
        this.contextBuilder = contextBuilder;
        this.trackingContextService = trackingContextService;
        this.contextValidator = contextValidator;
        this.intentDetection = intentDetection;
        this.deterministicAnswer = deterministicAnswer;
        this.promptBuilder = promptBuilder;
        this.responseParser = responseParser;
    }

    /**
     * Chat endpoint: builds tracking context from DB if not provided, validates,
     * optionally answers deterministically, otherwise calls LLM with strict JSON output.
     */
    @PostMapping("/chat")
    public StructuredChatResponse chat(@RequestBody ChatRequest req) {
        String userMessage = req.userMessage != null ? req.userMessage : req.message;
        if (userMessage == null || userMessage.isBlank()) {
            return StructuredChatResponse.missingContext(
                    "Veuillez poser une question.",
                    java.util.List.of("message"),
                    java.util.List.of("Écrivez votre question."));
        }

        Integer busId = req.busId;
        Integer enfantId = req.enfantId;
        if (req.selectedChild != null && req.selectedChild.getChildId() != null) {
            enfantId = req.selectedChild.getChildId();
        }
        if (req.selectedBus != null && req.selectedBus.getBusId() != null) {
            busId = req.selectedBus.getBusId();
        }

        TrackingContextService.TrackingContextContract contract =
                (req.trackingSnapshot != null && req.selectedChild != null)
                        ? contractFromRequest(req)
                        : trackingContextService.buildContract(busId, enfantId);

        // Explicit summary request: full tracking summary (onBoard, position, ETA, etc.)
        if (isSummaryRequest(userMessage) && contract.selectedChild != null) {
            StructuredChatResponse summary = buildSummaryResponse(contract);
            if (summary != null) return summary;
        }

        // Pure greeting only: short reply, no tracking data
        if (isGreeting(userMessage) && contract.selectedChild != null) {
            StructuredChatResponse greeting = buildGreetingResponse(contract);
            if (greeting != null) return greeting;
        }

        StructuredChatResponse missing = contextValidator.validateAndReturnMissingContextIfNeeded(userMessage, contract);
        if (missing != null) {
            return missing;
        }

        var intent = intentDetection.detect(userMessage);
        if (intent == Intent.SUMMARY_STATUS && contract.selectedChild != null) {
            StructuredChatResponse summary = buildSummaryResponse(contract);
            if (summary != null) return summary;
        }
        StructuredChatResponse deterministic = deterministicAnswer.answerIfPossible(intent, contract);
        if (deterministic != null) {
            return deterministic;
        }

        String prompt = promptBuilder.buildPrompt(userMessage, contract);
        String raw = ollama.generate(prompt);
        StructuredChatResponse parsed = responseParser.parse(raw);
        if (parsed != null) {
            return parsed;
        }
        raw = ollama.generate(promptBuilder.buildRepairPrompt(raw));
        parsed = responseParser.parse(raw);
        if (parsed != null) {
            return parsed;
        }
        return responseParser.fallbackResponse(userMessage);
    }

    private static final Set<String> GREETINGS = Set.of(
            "bonjour", "salut", "hello", "bonsoir", "coucou", "hi", "hey");

    /** Pure greeting = message is only a greeting word (and optional punctuation). No extra words. */
    private boolean isGreeting(String message) {
        if (message == null) return false;
        String normalized = message.trim().toLowerCase(Locale.ROOT)
                .replaceAll("[!?.,;:]+$", "").trim();
        if (normalized.isEmpty()) return false;
        return GREETINGS.contains(normalized);
    }

    private static final java.util.regex.Pattern SUMMARY_KEYWORDS = java.util.regex.Pattern.compile(
            "résumé|resume|status|situation|infos|où en est|ou en est|donne les détails|donne moi les détails|récap|recap",
            java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.UNICODE_CASE);

    private boolean isSummaryRequest(String message) {
        if (message == null || message.isBlank()) return false;
        return SUMMARY_KEYWORDS.matcher(message.trim().toLowerCase(Locale.ROOT)).find();
    }

    /** Short greeting only: no tracking data (position, ETA, onBoard, last update). */
    private StructuredChatResponse buildGreetingResponse(TrackingContextService.TrackingContextContract contract) {
        String reply = "Bonjour \uD83D\uDC4B Pose-moi une question sur la position, l'ETA ou le statut à bord.";
        StructuredChatResponse r = new StructuredChatResponse();
        r.setIntent(Intent.GENERAL_HELP.name());
        r.setAnswer_fr(reply);
        r.setReply(reply);
        r.setConfidence(1.0);
        return r;
    }

    /** Full tracking summary: onBoard, bus plate, lastUpdate, position, ETA or indisponible + suggested_actions. */
    private StructuredChatResponse buildSummaryResponse(TrackingContextService.TrackingContextContract contract) {
        SelectedChildDto c = contract.selectedChild;
        if (c == null) return null;
        String name = (c.getPrenom() != null ? c.getPrenom() : "").trim() + " " + (c.getNom() != null ? c.getNom() : "").trim();
        if (name.isBlank()) name = "Votre enfant";
        String plate = contract.selectedBus != null && contract.selectedBus.getPlateNumber() != null
                ? contract.selectedBus.getPlateNumber() : null;
        boolean onBoard = Boolean.TRUE.equals(c.getOnBoard());
        TrackingSnapshotDto s = contract.trackingSnapshot;

        StringBuilder reply = new StringBuilder();
        reply.append("Je suis prêt. ");
        if (onBoard) {
            reply.append("Votre enfant est à bord.");
        } else {
            reply.append("Votre enfant n'est pas à bord ; je peux suivre le bus du trajet");
            if (plate != null) reply.append(" (").append(plate).append(")");
            reply.append(".");
        }
        reply.append(" ");
        if (s != null) {
            reply.append("Dernière mise à jour : ").append(s.getLastUpdateTime() != null ? s.getLastUpdateTime() : "—").append(". ");
            if (s.getBusLat() != null && s.getBusLng() != null) {
                reply.append("Position du bus : ").append(s.getBusLat()).append(", ").append(s.getBusLng()).append(". ");
            }
            if (s.getEtaMinutes() != null && s.getEtaMinutes() >= 0) {
                reply.append("ETA prochain arrêt : ~").append(s.getEtaMinutes()).append(" min.");
            } else {
                reply.append("ETA indisponible (données vitesse/distance manquantes).");
            }
        } else {
            reply.append("Dernière mise à jour : —. ETA indisponible (données manquantes).");
        }
        reply.append(" Pose ta question.");

        StructuredChatResponse r = new StructuredChatResponse();
        r.setIntent(Intent.SUMMARY_STATUS.name());
        r.setAnswer_fr(reply.toString());
        r.setReply(reply.toString());
        r.setConfidence(1.0);
        if (s == null || (s.getEtaMinutes() == null && s.getEtaSeconds() == null)) {
            r.setSuggested_actions(java.util.List.of(
                    "Actualiser le suivi",
                    "Vérifier la vitesse du bus",
                    "Vérifier la distance au prochain arrêt"));
        }
        return r;
    }

    private TrackingContextService.TrackingContextContract contractFromRequest(ChatRequest req) {
        DataQualityDto dq = req.dataQuality != null ? req.dataQuality : new DataQualityDto();
        return new TrackingContextService.TrackingContextContract(
                req.selectedChild,
                req.selectedBus,
                req.trackingSnapshot,
                dq);
    }

    @GetMapping("/test")
    public StructuredChatResponse test() {
        String raw = ollama.generate("Réponds en une phrase : qui es-tu ?");
        StructuredChatResponse r = new StructuredChatResponse();
        r.setReply(raw != null ? raw : "Erreur");
        r.setIntent(tn.esprit.workshop.ai.dto.Intent.GENERAL_HELP.name());
        return r;
    }
}
