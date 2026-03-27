package tn.esprit.workshop.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import tn.esprit.workshop.model.JourFerieApi;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class JourFerieApiService {

    // URL de base de l'API Nager.Date
    private static final String BASE_URL = "https://date.nager.at/api/v3/publicholidays";

    // Client HTTP pour faire les requêtes
    private final HttpClient httpClient;

    // Gson pour convertir le JSON en objets Java
    private final Gson gson;

    // Constructeur
    public JourFerieApiService() {
        // Initialiser le client HTTP avec un timeout de 10 secondes
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        // Initialiser Gson
        this.gson = new GsonBuilder().create();
    }

    /**
     * Récupère la liste des jours fériés pour une année donnée en Tunisie
     * @param annee L'année (ex: 2025, 2026)
     * @return Liste des jours fériés
     * @throws IOException En cas d'erreur de communication
     * @throws InterruptedException Si la requête est interrompue
     */
    public List<JourFerieApi> getJoursFeries(int annee) throws IOException, InterruptedException {
        // Construire l'URL complète
        String url = BASE_URL + "/" + annee + "/TN";

        System.out.println("📡 Appel API: " + url);

        // Construire la requête HTTP
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        // Envoyer la requête et recevoir la réponse
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // Vérifier le code de statut
        if (response.statusCode() == 200) {
            // Convertir le JSON en liste d'objets JourFerieApi
            Type listType = new TypeToken<List<JourFerieApi>>(){}.getType();
            List<JourFerieApi> joursFeries = gson.fromJson(response.body(), listType);

            System.out.println("✅ " + joursFeries.size() + " jours fériés récupérés pour " + annee);
            return joursFeries;

        } else {
            throw new IOException("Erreur API: " + response.statusCode() + " - " + response.body());
        }
    }

    /**
     * Récupère les jours fériés pour plusieurs années
     * @param annees Liste des années
     * @return Liste combinée de tous les jours fériés
     */
    public List<JourFerieApi> getJoursFeriesPourAnnees(int... annees) {
        List<JourFerieApi> resultats = new ArrayList<>();

        for (int annee : annees) {
            try {
                resultats.addAll(getJoursFeries(annee));
            } catch (Exception e) {
                System.err.println("⚠️ Erreur pour l'année " + annee + ": " + e.getMessage());
            }
        }

        return resultats;
    }

    /**
     * Vérifie si l'API est accessible
     * @return true si l'API répond
     */
    public boolean testConnexion() {
        try {
            getJoursFeries(2025);
            return true;
        } catch (Exception e) {
            System.err.println("❌ API non accessible: " + e.getMessage());
            return false;
        }
    }
}