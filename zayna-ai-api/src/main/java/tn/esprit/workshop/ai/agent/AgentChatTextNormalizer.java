package tn.esprit.workshop.ai.agent;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Normalisation des questions pour le matching d'intentions.
 */
public final class AgentChatTextNormalizer {

    private AgentChatTextNormalizer() {
    }

    /**
     * Remplace quelques caractères fréquents, supprime les accents, trim + lowercase,
     * apostrophes typographiques → ', puis espaces condensés.
     */
    public static String forMatching(String raw) {
        if (raw == null) {
            return "";
        }
        String t = raw.trim();
        if (t.isEmpty()) {
            return "";
        }
        t = t.replace('\u2019', '\'')
                .replace('’', '\'')
                .replace('`', '\'')
                .replace('\u2010', '-')
                .replace('\u2013', '-');
        t = t.toLowerCase(Locale.ROOT);
        // é → e, à → a, etc. (complément à la décomposition NFD)
        t = t.replace('é', 'e').replace('è', 'e').replace('ê', 'e').replace('ë', 'e')
                .replace('à', 'a').replace('â', 'a').replace('ä', 'a')
                .replace('ù', 'u').replace('û', 'u').replace('ü', 'u')
                .replace('ô', 'o').replace('ö', 'o')
                .replace('î', 'i').replace('ï', 'i')
                .replace('ç', 'c');
        t = Normalizer.normalize(t, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        t = t.replace('\'', ' ');
        t = t.replaceAll("[-_]+", " ");
        t = t.replaceAll("\\s+", " ").trim();
        t = t.replaceAll("[?!.:;]+$", "").trim();
        return t;
    }
}
