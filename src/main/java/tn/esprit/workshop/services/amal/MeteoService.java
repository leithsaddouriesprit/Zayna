package tn.esprit.workshop.services.amal;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import tn.esprit.workshop.model.amal.Meteo;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;

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
     * Météo par nom de ville (API {@code q=}). À utiliser uniquement avec un nom de ville reconnu
     * (ex. « Tunis »), pas une adresse postale complète.
     */
    public Meteo getMeteo(String ville) throws IOException, InterruptedException {
        if (ville == null || ville.trim().isEmpty()) {
            throw new IOException("Aucun lieu n’a été indiqué pour la météo.");
        }
        String villeEncoded = URLEncoder.encode(ville.trim(), StandardCharsets.UTF_8);
        String url = BASE_URL + "?q=" + villeEncoded + "&units=metric&lang=fr&appid=" + API_KEY;
        return fetchAndParse(url, ville.trim());
    }

    /**
     * Météo par coordonnées GPS (recommandé pour une école avec latitude / longitude en base).
     * OpenWeatherMap renvoie le nom de la localité dans le JSON ({@code name}).
     */
    public Meteo getMeteoByCoordinates(double latitude, double longitude) throws IOException, InterruptedException {
        String url = String.format(Locale.US,
                BASE_URL + "?lat=%f&lon=%f&units=metric&lang=fr&appid=%s",
                latitude, longitude, API_KEY);
        return fetchAndParse(url, null);
    }

    private Meteo fetchAndParse(String url, String fallbackVille) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return parseReponse(response.body(), fallbackVille);
        }
        throw new IOException(humanReadableWeatherError(response.statusCode(), response.body()));
    }

    /**
     * Message utilisateur sans corps JSON brut.
     */
    private static String humanReadableWeatherError(int statusCode, String body) {
        String snippet = body == null ? "" : body.toLowerCase(Locale.ROOT);
        if (statusCode == 404 || snippet.contains("city not found") || snippet.contains("not found")) {
            return "Lieu introuvable pour la météo. Utilisez des coordonnées GPS ou une ville reconnue (ex. Tunis).";
        }
        if (statusCode == 401 || statusCode == 403) {
            return "Accès au service météo refusé. Vérifiez la configuration de l’application.";
        }
        if (statusCode >= 500) {
            return "Le service météo est temporairement indisponible. Réessayez dans quelques instants.";
        }
        return "Impossible d’obtenir la météo pour le moment. Réessayez plus tard.";
    }

    private Meteo parseReponse(String json, String fallbackVille) {
        JsonObject root = gson.fromJson(json, JsonObject.class);

        JsonObject main = root.getAsJsonObject("main");
        JsonObject weather = root.getAsJsonArray("weather").get(0).getAsJsonObject();

        String ville = fallbackVille;
        if (root.has("name") && !root.get("name").isJsonNull()) {
            String n = root.get("name").getAsString();
            if (n != null && !n.trim().isEmpty()) {
                ville = n.trim();
            }
        }
        if (ville == null || ville.isEmpty()) {
            ville = "Localisation";
        }

        double vent = 0;
        JsonElement windEl = root.get("wind");
        if (windEl != null && windEl.isJsonObject()) {
            JsonObject wind = windEl.getAsJsonObject();
            if (wind.has("speed") && !wind.get("speed").isJsonNull()) {
                vent = wind.get("speed").getAsDouble();
            }
        }

        Meteo meteo = new Meteo();
        meteo.setVille(ville);
        meteo.setTemperature(main.get("temp").getAsDouble());
        meteo.setRessenti(main.get("feels_like").getAsDouble());
        meteo.setDescription(weather.get("description").getAsString());
        meteo.setIcone(weather.get("icon").getAsString());
        meteo.setHumidite(main.get("humidity").getAsInt());
        meteo.setVent(vent);

        return meteo;
    }
}
