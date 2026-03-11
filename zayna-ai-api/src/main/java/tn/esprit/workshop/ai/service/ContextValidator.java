package tn.esprit.workshop.ai.service;

import org.springframework.stereotype.Service;
import tn.esprit.workshop.ai.dto.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Validates tracking context against user question. Returns MISSING_CONTEXT response
 * when required data is missing; otherwise returns null (proceed to answer).
 */
@Service
public class ContextValidator {

    private static final Pattern LOCATION_PATTERN = Pattern.compile(
            "où|ou est|position|localisation|exacte|gps|where|location|coordinates?",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern ETA_PATTERN = Pattern.compile(
            "quand (il|elle)?\\s*(arrive|arrivera)|eta|arrivée|dans combien|combien de (temps|minutes)",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern ON_BOARD_PATTERN = Pattern.compile(
            "dans le bus|on board|monté|descendu|est-il (dans|sur)|est-elle (dans|sur)",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    /**
     * If the user asks for location/ETA/on-board but context is missing required data,
     * return a structured MISSING_CONTEXT response. Otherwise return null.
     */
    public StructuredChatResponse validateAndReturnMissingContextIfNeeded(
            String userMessage,
            TrackingContextService.TrackingContextContract contract) {
        if (userMessage == null || userMessage.isBlank()) {
            return StructuredChatResponse.missingContext(
                    "Veuillez poser une question.",
                    List.of("message"),
                    List.of("Écrivez votre question."));
        }

        SelectedChildDto child = contract.selectedChild;
        TrackingSnapshotDto snapshot = contract.trackingSnapshot;

        // No child resolved: only case where we ask user to select/add a child (no "test mode" in UX)
        if (child == null || child.getChildId() == null) {
            return StructuredChatResponse.missingContext(
                    "Aucun enfant associé à votre compte. Sélectionnez ou ajoutez un enfant pour utiliser l'assistant.",
                    List.of("selectedChild", "childId"),
                    List.of("Sélectionnez un enfant", "Ou ajoutez un enfant depuis l'accueil"));
        }

        String msg = userMessage.trim().toLowerCase(Locale.ROOT);

        // Asking for child location / exact position
        if (LOCATION_PATTERN.matcher(msg).find()) {
            if (child == null || child.getChildId() == null) {
                return StructuredChatResponse.missingContext(
                        "Aucun enfant sélectionné. Je ne peux pas indiquer sa position.",
                        List.of("selectedChild", "childId"),
                        List.of("Sélectionnez un enfant", "Actualisez la page"));
            }
            if (snapshot == null || snapshot.getBusLat() == null || snapshot.getBusLng() == null) {
                return StructuredChatResponse.missingContext(
                        "La position du bus n'est pas disponible. Activez le suivi GPS ou réessayez plus tard.",
                        List.of("busLat", "busLng", "trackingSnapshot"),
                        List.of("Vérifiez que le suivi est activé", "Actualisez le suivi", "Réessayez dans quelques instants"));
            }
        }

        // Asking for ETA
        if (ETA_PATTERN.matcher(msg).find()) {
            if (child == null || child.getChildId() == null) {
                return StructuredChatResponse.missingContext(
                        "Sélectionnez un enfant pour connaître l'heure d'arrivée.",
                        List.of("selectedChild"),
                        List.of("Sélectionnez un enfant"));
            }
            if (snapshot == null) {
                return StructuredChatResponse.missingContext(
                        "Données de trajet indisponibles. Impossible d'estimer l'arrivée.",
                        List.of("trackingSnapshot"),
                        List.of("Actualisez le suivi", "Vérifiez la connexion"));
            }
            // ETA can be answered with distance/next stop even if etaMinutes is null; no hard fail here
        }

        // On-board status
        if (ON_BOARD_PATTERN.matcher(msg).find()) {
            if (child == null || child.getChildId() == null) {
                return StructuredChatResponse.missingContext(
                        "Sélectionnez un enfant pour savoir s'il est dans le bus.",
                        List.of("selectedChild"),
                        List.of("Sélectionnez un enfant"));
            }
        }

        return null;
    }
}
