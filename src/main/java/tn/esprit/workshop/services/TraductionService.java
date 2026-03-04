package tn.esprit.workshop.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TraductionService {

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build();

    private final ObjectMapper mapper = new ObjectMapper();

    private static final String MYMEMORY_API = "https://api.mymemory.translated.net/get";
    private static final String LIBRETRANSLATE_API = "https://libretranslate.com/translate";

    private static final String EMAIL = "votre.email@example.com"; // REMPLACEZ

    private Map<String, String> cacheTraduction = new HashMap<>();

    /**
     * Traduit un texte vers une langue cible
     */
    public String traduireVersLangue(String texte, String langueCible) {
        // ✅ Validation d'entrée
        if (texte == null || texte.trim().isEmpty()) {
            return "[Texte vide]";
        }

        texte = texte.trim().replaceAll("\\s+", " ");

        if (texte.length() < 3) {
            return "[Texte trop court]";
        }

        // Vérification de la langue cible
        if (langueCible == null || langueCible.isEmpty()) {
            return "[Langue cible non spécifiée]";
        }

        String cacheKey = texte + "|" + langueCible;
        if (cacheTraduction.containsKey(cacheKey)) {
            return cacheTraduction.get(cacheKey);
        }

        // ✅ Toujours retourner une chaîne, jamais null
        String resultat = "⚠️ Service de traduction indisponible";

        // Essayer MyMemory d'abord
        try {
            resultat = traduireAvecMyMemory(texte, langueCible);
        } catch (Exception e) {
            System.out.println("MyMemory a échoué: " + e.getMessage());
            resultat = null; // Pour forcer l'essai de LibreTranslate
        }

        // Si MyMemory échoue, essayer LibreTranslate
        if (resultat == null || resultat.startsWith("[") || resultat.contains("Erreur")) {
            try {
                resultat = traduireAvecLibreTranslate(texte, langueCible);
            } catch (Exception e) {
                resultat = "[Service de traduction temporairement indisponible]";
            }
        }

        // ✅ Sécurité : si résultat est null, mettre un message par défaut
        if (resultat == null) {
            resultat = "[Erreur de traduction inconnue]";
        }

        // Mettre en cache si le résultat est valide
        if (!resultat.startsWith("[") && !resultat.contains("Erreur")) {
            cacheTraduction.put(cacheKey, resultat);
        }

        return resultat;
    }

    /**
     * Traduction avec MyMemory
     */
    private String traduireAvecMyMemory(String texte, String langueCible) throws Exception {
        String encodedText = URLEncoder.encode(texte, StandardCharsets.UTF_8.toString());

        String url = MYMEMORY_API + "?q=" + encodedText +
                "&langpair=fr|" + langueCible +
                "&de=" + EMAIL;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("User-Agent", "Mozilla/5.0")
                .build();

        try (Response response = client.newCall(request).execute()) {

            if (response.code() == 403) {
                return "[Quota MyMemory dépassé]";
            }
            if (response.code() != 200) {
                return "[Erreur MyMemory: " + response.code() + "]";
            }

            String jsonResponse = response.body().string();
            JsonNode root = mapper.readTree(jsonResponse);

            String translatedText = root.path("responseData").path("translatedText").asText();

            if (translatedText == null || translatedText.isEmpty() ||
                    translatedText.contains("INVALID") ||
                    translatedText.contains("AUTO IS AN INVALID")) {
                return null; // Provoque l'essai de LibreTranslate
            }

            return translatedText;
        }
    }

    /**
     * Traduction avec LibreTranslate
     */
    private String traduireAvecLibreTranslate(String texte, String langueCible) throws Exception {
        // Mapping des codes de langue pour LibreTranslate
        Map<String, String> langMap = new HashMap<>();
        langMap.put("fr", "fr");
        langMap.put("en", "en");
        langMap.put("es", "es");
        langMap.put("de", "de");
        langMap.put("it", "it");
        langMap.put("ar", "ar");
        langMap.put("zh", "zh");
        langMap.put("ja", "ja");
        langMap.put("ru", "ru");
        langMap.put("pt", "pt");

        String targetLang = langMap.getOrDefault(langueCible, "en");

        // Format JSON correct
        String jsonBody = "{\"q\":\"" + escapeJson(texte) + "\",\"source\":\"auto\",\"target\":\"" + targetLang + "\"}";

        RequestBody body = RequestBody.create(
                jsonBody,
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(LIBRETRANSLATE_API)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.code() != 200) {
                return "[Erreur LibreTranslate: " + response.code() + "]";
            }

            String jsonResponse = response.body().string();
            JsonNode root = mapper.readTree(jsonResponse);
            String translatedText = root.path("translatedText").asText();

            if (translatedText.isEmpty()) {
                return "[Traduction vide]";
            }

            return translatedText;
        }
    }

    /**
     * Échappe les caractères spéciaux pour JSON
     */
    private String escapeJson(String texte) {
        if (texte == null) return "";
        return texte.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public String traduireVersFrancais(String texte) {
        return traduireVersLangue(texte, "fr");
    }

    public String detecterLangue(String texte) {
        return "fr";
    }

    public void viderCache() {
        cacheTraduction.clear();
    }
}