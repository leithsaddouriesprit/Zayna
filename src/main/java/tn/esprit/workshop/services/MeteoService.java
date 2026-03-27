package tn.esprit.workshop.services;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import tn.esprit.workshop.model.Meteo;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class MeteoService {

    private static final String API_KEY = "5778444690641509185980fc5dd2d7ad";
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";
    private final HttpClient httpClient;
    private final Gson gson;

    public MeteoService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.gson = new Gson();
    }

    /**
     * Récupère la météo pour une ville donnée
     * @param ville Nom de la ville (ex: "Tunis", "La marsa")
     * @return Objet Meteo avec les informations
     */
    public Meteo getMeteo(String ville) throws IOException, InterruptedException {
        // 🔧 CORRECTION : Encoder la ville pour gérer les espaces
        String villeEncoded = URLEncoder.encode(ville, StandardCharsets.UTF_8.toString());

        // Construire l'URL avec la ville encodée
        String url = BASE_URL + "?q=" + villeEncoded + "&units=metric&lang=fr&appid=" + API_KEY;

        System.out.println("📡 Appel API météo: " + url);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Réponse reçue - Code: " + response.statusCode());

        if (response.statusCode() == 200) {
            return parseReponse(response.body(), ville);
        } else {
            throw new IOException("Erreur API météo: " + response.statusCode() + " - " + response.body());
        }
    }

    private Meteo parseReponse(String json, String ville) {
        JsonObject root = gson.fromJson(json, JsonObject.class);

        JsonObject main = root.getAsJsonObject("main");
        JsonObject weather = root.getAsJsonArray("weather").get(0).getAsJsonObject();
        JsonObject wind = root.getAsJsonObject("wind");

        Meteo meteo = new Meteo();
        meteo.setVille(ville);
        meteo.setTemperature(main.get("temp").getAsDouble());
        meteo.setRessenti(main.get("feels_like").getAsDouble());
        meteo.setDescription(weather.get("description").getAsString());
        meteo.setIcone(weather.get("icon").getAsString());
        meteo.setHumidite(main.get("humidity").getAsInt());
        meteo.setVent(wind.get("speed").getAsDouble());

        System.out.println("✅ Météo récupérée: " + meteo.getTemperature() + "°C - " + meteo.getDescription());

        return meteo;
    }

    public boolean testerConnexion() {
        try {
            getMeteo("Tunis");
            return true;
        } catch (Exception e) {
            System.err.println("❌ API météo inaccessible: " + e.getMessage());
            return false;
        }
    }
}