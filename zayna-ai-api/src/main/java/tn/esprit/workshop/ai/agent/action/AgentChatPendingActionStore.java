package tn.esprit.workshop.ai.agent.action;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Propositions d'actions en attente de confirmation (mémoire processus, TTL court).
 */
@Component
public class AgentChatPendingActionStore {

    private static final long TTL_MS = 15 * 60 * 1000L;

    private record Entry(int userId, int ecoleId, AgentChatActionPayload payload, Instant created) {
    }

    private final Map<String, Entry> byToken = new ConcurrentHashMap<>();

    public String put(int userId, int ecoleId, AgentChatActionPayload payload) {
        purgeExpired();
        String token = UUID.randomUUID().toString();
        byToken.put(token, new Entry(userId, ecoleId, payload, Instant.now()));
        return token;
    }

    /**
     * Récupère et supprime l'entrée si utilisateur et école correspondent.
     */
    public AgentChatActionPayload take(String token, int userId, int ecoleId) {
        purgeExpired();
        if (token == null || token.isBlank()) {
            return null;
        }
        Entry e = byToken.remove(token.trim());
        if (e == null) {
            return null;
        }
        if (e.userId != userId || e.ecoleId != ecoleId) {
            return null;
        }
        if (Instant.now().toEpochMilli() - e.created.toEpochMilli() > TTL_MS) {
            return null;
        }
        return e.payload;
    }

    public void cancelWithoutExecute(String token, int userId, int ecoleId) {
        take(token, userId, ecoleId);
    }

    private void purgeExpired() {
        long now = Instant.now().toEpochMilli();
        byToken.entrySet().removeIf(en -> now - en.getValue().created.toEpochMilli() > TTL_MS);
    }
}
