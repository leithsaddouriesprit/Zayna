package tn.esprit.workshop.services.amal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import tn.esprit.workshop.model.amal.JourFerieApi;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

public class JourFerieApiService {

    private static final String BASE_URL = "https://date.nager.at/api/v3/publicholidays";

    private final HttpClient httpClient;
    private final Gson gson;

    public JourFerieApiService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.gson = new GsonBuilder().create();
    }

    public List<JourFerieApi> getJoursFeries(int annee) throws IOException, InterruptedException {
        String url = BASE_URL + "/" + annee + "/TN";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            Type listType = new TypeToken<List<JourFerieApi>>(){}.getType();
            return gson.fromJson(response.body(), listType);
        }
        throw new IOException("Erreur API: " + response.statusCode() + " - " + response.body());
    }
}
