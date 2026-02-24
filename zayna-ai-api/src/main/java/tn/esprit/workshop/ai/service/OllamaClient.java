package tn.esprit.workshop.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class OllamaClient {

    @Value("${ai.model}")
    private String model;

    @Value("${ai.ollama.url}")
    private String ollamaUrl;

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public String generate(String prompt) {
        try {

            String body = mapper.writeValueAsString(java.util.Map.of(
                    "model", model,
                    "prompt", prompt,
                    "stream", false
            ));

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(ollamaUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> res =
                    http.send(req, HttpResponse.BodyHandlers.ofString());

            JsonNode json = mapper.readTree(res.body());

            if (json.has("error")) {
                return "Erreur Ollama: " + json.get("error").asText();
            }

            return json.path("response").asText("").trim();

        } catch (Exception e) {
            return "Erreur OllamaClient: " + e.getMessage();
        }
    }
}