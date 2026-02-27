package tn.esprit.workshop.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class TraductionService {

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private static final String API_URL = "https://api.mymemory.translated.net/get";

    /**
     * Traduit un texte de n'importe quelle langue vers le français
     */
    public String traduireVersFrancais(String texte) throws Exception {
        if (texte == null || texte.trim().isEmpty()) {
            return texte;
        }

        // Nettoyer le texte (enlever les espaces multiples)
        texte = texte.trim().replaceAll("\\s+", " ");

        // Vérifier la longueur du texte
        if (texte.length() < 3) {
            return texte; // Retourne le texte original si trop court
        }

        // Encoder l'URL correctement
        String encodedText = URLEncoder.encode(texte, StandardCharsets.UTF_8.toString());
        String url = API_URL + "?q=" + encodedText + "&langpair=auto|fr";

        System.out.println("URL de traduction: " + url);

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("User-Agent", "Mozilla/5.0")
                .build();

        try (Response response = client.newCall(request).execute()) {
            String jsonResponse = response.body().string();
            System.out.println("Réponse JSON: " + jsonResponse);

            JsonNode root = mapper.readTree(jsonResponse);

            // Vérifier le code de retour
            int responseStatus = root.path("responseStatus").asInt(200);
            if (responseStatus != 200) {
                return "[Erreur API: " + responseStatus + "]";
            }

            String translatedText = root.path("responseData").path("translatedText").asText();

            // Si le texte traduit contient des messages d'erreur
            if (translatedText.contains("AUTO IS AN INVALID") ||
                    translatedText.contains("INVALID SOURCE") ||
                    translatedText.isEmpty() ||
                    translatedText.equals(texte)) {

                // Essayer avec une paire de langues explicite (français -> anglais)
                return traduireAvecLangueExplicite(texte, "fr", "en");
            }

            return translatedText;
        } catch (Exception e) {
            System.out.println("Erreur de traduction: " + e.getMessage());
            return "[Erreur: " + e.getMessage() + "]";
        }
    }

    /**
     * Traduit avec une paire de langues explicite
     */
    private String traduireAvecLangueExplicite(String texte, String source, String cible) throws Exception {
        String encodedText = URLEncoder.encode(texte.trim(), StandardCharsets.UTF_8.toString());
        String url = API_URL + "?q=" + encodedText + "&langpair=" + source + "|" + cible;

        System.out.println("URL explicite: " + url);

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("User-Agent", "Mozilla/5.0")
                .build();

        try (Response response = client.newCall(request).execute()) {
            String jsonResponse = response.body().string();
            JsonNode root = mapper.readTree(jsonResponse);
            String translatedText = root.path("responseData").path("translatedText").asText();

            if (translatedText.isEmpty() || translatedText.contains("INVALID")) {
                return "[Traduction non disponible]";
            }
            return translatedText;
        }
    }

    /**
     * Traduit spécifiquement du français vers l'anglais
     */
    public String traduireFrancaisVersAnglais(String texte) throws Exception {
        return traduireAvecLangueExplicite(texte, "fr", "en");
    }

    /**
     * Détecte la langue d'un texte
     */
    public String detecterLangue(String texte) throws Exception {
        if (texte == null || texte.trim().isEmpty() || texte.trim().length() < 3) {
            return "inconnue";
        }

        String encodedText = URLEncoder.encode(texte.trim(), StandardCharsets.UTF_8.toString());
        String url = API_URL + "?q=" + encodedText + "&langpair=auto|fr";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("User-Agent", "Mozilla/5.0")
                .build();

        try (Response response = client.newCall(request).execute()) {
            String jsonResponse = response.body().string();
            JsonNode root = mapper.readTree(jsonResponse);

            // Récupérer la langue détectée depuis responseData
            JsonNode responseData = root.path("responseData");
            if (responseData.has("detectedLanguage")) {
                return responseData.path("detectedLanguage").asText();
            }

            // Alternative : chercher dans les matches
            JsonNode matches = root.path("matches");
            if (matches.isArray() && matches.size() > 0) {
                JsonNode firstMatch = matches.get(0);
                if (firstMatch.has("source")) {
                    return firstMatch.path("source").asText();
                }
            }

            return "inconnue";
        } catch (Exception e) {
            return "inconnue";
        }
    }
}