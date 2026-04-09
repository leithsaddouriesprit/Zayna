package tn.esprit.workshop.services.leith;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;


public class WeatherService {


    private static final String DEFAULT_CITY = "Tunis, tn";
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    public WeatherService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.objectMapper = new ObjectMapper();
        this.apiKey = "294b3ec082a72593f1b81aa4cf071d36";
    }

    public WeatherInfo fetchForDefaultCity() {
        return fetchForCity(DEFAULT_CITY);
    }

    public String getDefaultCity() {
        return DEFAULT_CITY;
    }

    public WeatherInfo fetchForCity(String city) {
        if (apiKey == null || apiKey.isBlank() || city == null || city.isBlank()) {
            System.out.println("[WeatherService] API key absente ou ville vide, retour en mode indisponible.");
            return WeatherInfo.unavailable("Météo indisponible pour le moment");
        }
        try {
            String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);
            String url = BASE_URL + "?q=" + encodedCity + "&units=metric&lang=fr&appid=" + apiKey;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                System.out.println("[WeatherService] Appel API échoué, status=" + response.statusCode() + ", body=" + response.body());
                return WeatherInfo.unavailable("Météo indisponible pour le moment");
            }
            return parseResponse(city, response.body());
        } catch (IOException | InterruptedException e) {
            System.out.println("[WeatherService] Erreur IO/interrupt lors de l'appel météo: " + e.getMessage());
            return WeatherInfo.unavailable("Météo indisponible pour le moment");
        } catch (Exception e) {
            System.out.println("[WeatherService] Erreur inattendue lors de l'appel météo: " + e.getMessage());
            return WeatherInfo.unavailable("Météo indisponible pour le moment");
        }
    }

    private WeatherInfo parseResponse(String city, String body) throws IOException {
        JsonNode root = objectMapper.readTree(body);
        JsonNode main = root.path("main");
        JsonNode weatherArray = root.path("weather");

        double temp = main.path("temp").asDouble(Double.NaN);
        String description = weatherArray.isArray() && weatherArray.size() > 0
                ? weatherArray.get(0).path("description").asText("")
                : "";
        String condition = weatherArray.isArray() && weatherArray.size() > 0
                ? weatherArray.get(0).path("main").asText("")
                : "";

        if (Double.isNaN(temp)) {
            return WeatherInfo.unavailable("Météo indisponible pour le moment");
        }
        return WeatherInfo.available(city, temp, description, condition);
    }

    public static final class WeatherInfo {
        private final boolean available;
        private final String city;
        private final Double temperatureCelsius;
        private final String description;
        private final String condition;
        private final String message;

        private WeatherInfo(boolean available,
                            String city,
                            Double temperatureCelsius,
                            String description,
                            String condition,
                            String message) {
            this.available = available;
            this.city = city;
            this.temperatureCelsius = temperatureCelsius;
            this.description = description;
            this.condition = condition;
            this.message = message;
        }

        public static WeatherInfo unavailable(String message) {
            return new WeatherInfo(false, null, null, null, null, message);
        }

        public static WeatherInfo available(String city,
                                            double temp,
                                            String description,
                                            String condition) {
            return new WeatherInfo(true, city, temp, description, condition, null);
        }

        public boolean isAvailable() {
            return available;
        }

        public String getCity() {
            return city;
        }

        public Double getTemperatureCelsius() {
            return temperatureCelsius;
        }

        public String getDescription() {
            return description;
        }

        public String getCondition() {
            return condition;
        }

        public String getMessage() {
            return message;
        }
    }
}

