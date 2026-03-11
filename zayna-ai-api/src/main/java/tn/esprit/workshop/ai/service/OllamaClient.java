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

    @Value("${ai.temperature:0.2}")
    private double temperature;

    @Value("${ai.top_p:0.9}")
    private double topP;

    @Value("${ai.max_tokens:512}")
    private int maxTokens;

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public String generate(String prompt) {
        try {
            java.util.Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("model", model);
            payload.put("prompt", prompt);
            payload.put("stream", false);
            java.util.Map<String, Object> options = new java.util.HashMap<>();
            options.put("temperature", temperature);
            options.put("top_p", topP);
            options.put("num_predict", maxTokens);
            payload.put("options", options);
            String body = mapper.writeValueAsString(payload);

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