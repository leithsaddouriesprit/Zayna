package tn.esprit.workshop.services;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class FiltrageService {

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .build();

    // API PurgoMalum - Détection de grossièretés
    private static final String API_CONTAINS = "https://www.purgomalum.com/service/containsprofanity";
    private static final String API_FILTER = "https://www.purgomalum.com/service/json";

    /**
     * Vérifie si un texte contient des mots grossiers
     * @return true si des grossièretés sont détectées
     */
    public boolean contientGrossieretes(String texte) {
        if (texte == null || texte.trim().isEmpty()) {
            return false;
        }

        try {
            String encodedText = URLEncoder.encode(texte, StandardCharsets.UTF_8.toString());
            String url = API_CONTAINS + "?text=" + encodedText;

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

            try (Response response = client.newCall(request).execute()) {
                String resultat = response.body().string().trim();
                return "true".equals(resultat);
            }
        } catch (Exception e) {
            System.err.println("Erreur filtrage: " + e.getMessage());
            return false; // En cas d'erreur, on laisse passer par sécurité
        }
    }

    /**
     * Filtre un texte en remplaçant les mots grossiers par des étoiles
     * @return Texte filtré
     */
    public String filtrerTexte(String texte) {
        if (texte == null || texte.trim().isEmpty()) {
            return texte;
        }

        try {
            String encodedText = URLEncoder.encode(texte, StandardCharsets.UTF_8.toString());
            String url = API_FILTER + "?text=" + encodedText + "&fillchar=*";

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

            try (Response response = client.newCall(request).execute()) {
                String jsonResponse = response.body().string();

                // Parse manuel simple (sans Jackson)
                if (jsonResponse.contains("\"result\":")) {
                    int start = jsonResponse.indexOf("\"result\":\"") + 10;
                    int end = jsonResponse.indexOf("\"", start);
                    return jsonResponse.substring(start, end);
                }
                return texte;
            }
        } catch (Exception e) {
            System.err.println("Erreur filtrage: " + e.getMessage());
            return texte;
        }
    }

    /**
     * Version qui retourne le texte censuré et un booléen
     */
    public ResultatFiltrage analyser(String texte) {
        if (texte == null || texte.trim().isEmpty()) {
            return new ResultatFiltrage(texte, false);
        }

        boolean contientGrossieretes = contientGrossieretes(texte);
        String texteFiltre = contientGrossieretes ? filtrerTexte(texte) : texte;

        return new ResultatFiltrage(texteFiltre, contientGrossieretes);
    }

    /**
     * Classe interne pour le résultat
     */
    public static class ResultatFiltrage {
        private final String texteFiltre;
        private final boolean contientGrossieretes;

        public ResultatFiltrage(String texteFiltre, boolean contientGrossieretes) {
            this.texteFiltre = texteFiltre;
            this.contientGrossieretes = contientGrossieretes;
        }

        public String getTexteFiltre() { return texteFiltre; }
        public boolean contientGrossieretes() { return contientGrossieretes; }
    }
}