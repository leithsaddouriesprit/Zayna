package tn.esprit.workshop.utilis;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Modération de saisie locale pour réclamations / réponses : normalisation + liste de termes interdits.
 * Ne remplace pas une modération humaine ; visée pédagogique et filtre grossier.
 */
public final class InputModerationUtil {

    public static final String MESSAGE_BLOQUE =
            "Votre message contient un langage inapproprié. Merci de reformuler.";

    private static final boolean REMOTE_CHECK_ENABLED =
            Boolean.parseBoolean(System.getProperty("zayna.profanity.remote", "false"));

    /**
     * Mots / séquences à détecter après {@link #normalizeForProfanityCheck(String)} sur la chaîne « lettres compactes »
     * (toutes les lettres du texte, accents retirés, casse ignorée, séparateurs supprimés).
     * Éviter les sous-chaînes trop courtes ambiguës (ex. « ass » dans « class »).
     */
    private static final String[] COMPACT_BLACKLIST = {
            // Anglais (éviter sous-chaînes trop courtes : pas « cock » seul → cocktail, etc.)
            "fuck", "fucking", "fucker", "motherfucker", "shit", "bitch", "bastard", "whore", "slut",
            "dickhead", "piss", "wanker", "twat", "arsehole", "asshole", "nigger", "faggot", "retard",
            // Français
            "merde", "putain", "pute", "salope", "connard", "connasse", "conard", "encule", "enculer",
            "niquer", "nique", "fdp", "filsdepute", "tagueule", "fermetagueule", "bordel", "foutre",
            "branleur", "branle", "salaud", "salauds", "bite", "couille", "couilles", "tapette",
            // Translittération / argot (échantillon)
            "zebi", "wahch", "nikmok", "kahba", "sharmuta", "manyak"
    };

    private InputModerationUtil() {
    }

    /**
     * Chaîne normalisée pour contrôle : minuscules, sans accents, uniquement lettres Unicode concaténées
     * (espaces, ponctuation, chiffres, symboles ignorés). Ex. {@code "F U-C.k"} → {@code "fuck"}.
     */
    public static String normalizeForProfanityCheck(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        String noMarks = stripCombiningMarks(Normalizer.normalize(text, Normalizer.Form.NFD));
        String lower = noMarks.toLowerCase(Locale.ROOT);
        StringBuilder sb = new StringBuilder(lower.length());
        for (int i = 0; i < lower.length(); ) {
            int cp = lower.codePointAt(i);
            if (Character.isLetter(cp)) {
                sb.appendCodePoint(cp);
            }
            i += Character.charCount(cp);
        }
        return sb.toString();
    }

    private static String stripCombiningMarks(String nfd) {
        StringBuilder b = new StringBuilder(nfd.length());
        for (int i = 0; i < nfd.length(); ) {
            int cp = nfd.codePointAt(i);
            if (Character.getType(cp) != Character.NON_SPACING_MARK) {
                b.appendCodePoint(cp);
            }
            i += Character.charCount(cp);
        }
        return b.toString();
    }

    /**
     * Vrai si le texte, une fois normalisé en forme compacte, contient une entrée de la liste locale.
     */
    public static boolean containsForbiddenLanguage(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        String compact = normalizeForProfanityCheck(text);
        if (compact.isEmpty()) {
            return false;
        }
        for (String w : COMPACT_BLACKLIST) {
            if (w != null && !w.isEmpty() && compact.contains(w)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Première séquence reconnue (forme normalisée liste), pour logs internes uniquement — ne pas afficher à l’utilisateur.
     */
    public static String findMatchedForbiddenWord(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String compact = normalizeForProfanityCheck(text);
        for (String w : COMPACT_BLACKLIST) {
            if (w != null && !w.isEmpty() && compact.contains(w)) {
                return w;
            }
        }
        return null;
    }

    /**
     * Contrôle local + optionnellement API distante (si activée). Un seul texte suffit à bloquer.
     */
    public static boolean isBlocked(String... textParts) {
        if (textParts == null) {
            return false;
        }
        for (String part : textParts) {
            if (part != null && containsForbiddenLanguage(part)) {
                return true;
            }
        }
        if (!REMOTE_CHECK_ENABLED) {
            return false;
        }
        String joined = joinNonBlank(textParts);
        if (joined.isEmpty()) {
            return false;
        }
        return OptionalPurgoMalumClient.containsProfanityRemote(joined);
    }

    private static String joinNonBlank(String[] parts) {
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p != null && !p.isBlank()) {
                if (sb.length() > 0) {
                    sb.append(' ');
                }
                sb.append(p.trim());
            }
        }
        return sb.toString();
    }
}
