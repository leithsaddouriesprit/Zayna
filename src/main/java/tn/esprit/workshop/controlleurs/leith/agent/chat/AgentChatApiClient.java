package tn.esprit.workshop.controlleurs.leith.agent.chat;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Client HTTP vers zayna-ai-api pour le chat agent (lecture + actions confirmées côté UI).
 */
public class AgentChatApiClient {

    private static final Logger LOG = Logger.getLogger(AgentChatApiClient.class.getName());

    /** Same host/port as parent ChatAI; path is dedicated to agent. */
    public static final String DEFAULT_BASE_URL = "http://localhost:8081/ai/agent/chat";

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String endpointUrl;

    public AgentChatApiClient() {
        String fromProp = System.getProperty("zayna.agent.chat.url");
        this.endpointUrl = (fromProp != null && !fromProp.isBlank()) ? fromProp.trim() : DEFAULT_BASE_URL;
    }

    public AgentChatApiClient(String endpointUrl) {
        this.endpointUrl = endpointUrl != null && !endpointUrl.isBlank() ? endpointUrl.trim() : DEFAULT_BASE_URL;
    }

    /**
     * Calls the API. On transport/parse failure throws {@link AgentChatApiException}.
     */
    public AgentChatResponse ask(AgentChatRequest request) throws AgentChatApiException {
        try {
            String json = mapper.writeValueAsString(request);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(endpointUrl))
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .timeout(Duration.ofSeconds(60))
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            String body = res.body();
            if (res.statusCode() < 200 || res.statusCode() >= 300) {
                LOG.log(Level.WARNING, "Agent chat API HTTP {0}: {1}", new Object[]{res.statusCode(), body});
                throw new AgentChatApiException("HTTP " + res.statusCode());
            }
            AgentChatResponse parsed = mapper.readValue(body, AgentChatResponse.class);
            if (parsed == null) {
                throw new AgentChatApiException("empty response");
            }
            return parsed;
        } catch (AgentChatApiException e) {
            throw e;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Agent chat API call failed", e);
            throw new AgentChatApiException(e.getMessage(), e);
        }
    }
}
