package tn.esprit.workshop.ai.service;

import org.springframework.stereotype.Service;
import tn.esprit.workshop.ai.dto.Intent;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Lightweight intent detection from user message (before calling LLM).
 */
@Service
public class IntentDetectionService {

    private static final Pattern ON_BOARD = Pattern.compile(
            "dans le bus|on board|monté|descendu|est-il (dans|sur) le bus|est-elle (dans|sur) le bus|il est dans|elle est dans",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern LOCATION = Pattern.compile(
            "où est|ou est|position|localisation|exacte|gps|coordonnées|where is|location|coordinates?",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern BUS_LOCATION = Pattern.compile(
            "où est le bus|position du bus|bus (actuellement|maintenant)|where is the bus",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern ETA = Pattern.compile(
            "quand (il|elle)?\\s*(arrive|arrivera)|eta|arrivée|dans combien|combien de (temps|minutes)|heure d'arrivée",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern SAFETY = Pattern.compile(
            "ça va|cv|en sécurité|sécurité|tout va bien|ok|tout est ok|est-ce qu'il va bien|est-ce qu'elle va bien|mon fils est|ma fille est",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern SUMMARY = Pattern.compile(
            "résumé|resume|status|situation|infos|où en est|ou en est|donne les détails|donne moi les détails|récap|recap",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    public Intent detect(String userMessage) {
        if (userMessage == null || userMessage.isBlank()) return Intent.GENERAL_HELP;
        String msg = userMessage.trim().toLowerCase(Locale.ROOT);

        if (SUMMARY.matcher(msg).find()) return Intent.SUMMARY_STATUS;
        if (ON_BOARD.matcher(msg).find()) return Intent.ON_BOARD_STATUS;
        if (BUS_LOCATION.matcher(msg).find()) return Intent.BUS_LOCATION;
        if (LOCATION.matcher(msg).find()) return Intent.CHILD_LOCATION;
        if (ETA.matcher(msg).find()) return Intent.ETA;
        if (SAFETY.matcher(msg).find()) return Intent.SAFETY_STATUS;

        return Intent.GENERAL_HELP;
    }
}
