package tn.esprit.workshop.services.amal;

import com.google.gson.Gson;
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

    public Meteo getMeteo(String ville) throws IOException, InterruptedException {
        String villeEncoded = URLEncoder.encode(ville, StandardCharsets.UTF_8);
        String url = BASE_URL + "?q=" + villeEncoded + "&units=metric&lang=fr&appid=" + API_KEY;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return parseReponse(response.body(), ville);
        }
        throw new IOException("Erreur API météo: " + response.statusCode() + " - " + response.body());
    }

    /**
     * Météo à partir des coordonnées GPS (école en base). Le libellé de ville reprend le nom renvoyé par l'API.
     */
    public Meteo getMeteoByCoordinates(double latitude, double longitude) throws IOException, InterruptedException {
        String url = BASE_URL + "?lat=" + latitude + "&lon=" + longitude + "&units=metric&lang=fr&appid=" + API_KEY;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return parseReponse(response.body(), null);
        }
        throw new IOException("Erreur API météo: " + response.statusCode() + " - " + response.body());
    }

    private Meteo parseReponse(String json, String villeFallback) {
        JsonObject root = gson.fromJson(json, JsonObject.class);

        JsonObject main = root.getAsJsonObject("main");
        JsonObject weather = root.getAsJsonArray("weather").get(0).getAsJsonObject();
        JsonObject wind = root.has("wind") && root.get("wind").isJsonObject()
                ? root.getAsJsonObject("wind") : null;

        String ville = villeFallback;
        if (root.has("name") && !root.get("name").isJsonNull()) {
            ville = root.get("name").getAsString();
        }
        if (ville == null || ville.isBlank()) {
            ville = "Localisation";
        }

        Meteo meteo = new Meteo();
        meteo.setVille(ville);
        meteo.setTemperature(main.get("temp").getAsDouble());
        meteo.setRessenti(main.get("feels_like").getAsDouble());
        meteo.setDescription(weather.get("description").getAsString());
        meteo.setIcone(weather.get("icon").getAsString());
        meteo.setHumidite(main.get("humidity").getAsInt());
        double vent = 0;
        if (wind != null && wind.has("speed") && !wind.get("speed").isJsonNull()) {
            vent = wind.get("speed").getAsDouble();
        }
        meteo.setVent(vent);

        return meteo;
    }
}
