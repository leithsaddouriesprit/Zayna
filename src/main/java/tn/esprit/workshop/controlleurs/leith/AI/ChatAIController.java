package tn.esprit.workshop.controlleurs.leith.AI;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.esprit.workshop.model.leith.Arret;
import tn.esprit.workshop.model.leith.Enfant;
import tn.esprit.workshop.model.leith.PositionBus;
import tn.esprit.workshop.model.leith.Trajet;
import tn.esprit.workshop.services.leith.ArretService;
import tn.esprit.workshop.services.leith.BusService;
import tn.esprit.workshop.services.leith.EnfantService;
import tn.esprit.workshop.services.leith.PositionBusService;
import tn.esprit.workshop.services.leith.TrajetService;
import tn.esprit.workshop.utilis.AppSession;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Chat AI: auto-context (default child = last selected or first of parent), bus + tracking snapshot
 * in every request. Greeting line shown on load when context is resolved.
 */
public class ChatAIController {

    private static final Logger LOG = Logger.getLogger(ChatAIController.class.getName());
    private static final double MAX_ETA_DISTANCE_KM = 20.0;

    private static final String API_CHAT_URL = "http://localhost:8081/ai/chat";
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_DATE_TIME;

    @FXML private TextArea taChat;
    @FXML private TextField tfMessage;
    @FXML private Button btnSend;

    /** Resolved on open: default child (last viewed or first of parent). */
    private Integer enfantId;
    /** Cached context for every request. */
    private Enfant cachedEnfant;
    private Trajet cachedTrajet;
    private Integer cachedBusId;
    private String cachedBusPlate;
    private PositionBus cachedPosition;

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private final EnfantService enfantService = new EnfantService();
    private final TrajetService trajetService = new TrajetService();
    private final BusService busService = new BusService();
    private final PositionBusService positionBusService = new PositionBusService();
    private final ArretService arretService = new ArretService();

    @FXML
    public void initialize() {
        taChat.appendText("AI: Chargement du contexte…\n\n");
    }

    /**
     * Auto-context: resolve default child (param, else last selected, else first active for parent),
     * then bus + tracking snapshot. Update welcome message. Called when opening the chat view.
     */
    public void init(Integer enfantIdParam) {
        this.enfantId = enfantIdParam != null ? enfantIdParam : AppSession.getInstance().getSelectedEnfantId();
        if (this.enfantId == null) {
            try {
                int parentId = AppSession.getInstance().getParentId();
                List<Enfant> children = enfantService.getByParentId(parentId);
                if (children != null && !children.isEmpty()) {
                    this.enfantId = children.get(0).getEnfantId();
                    AppSession.getInstance().setSelectedEnfantId(this.enfantId);
                }
            } catch (Exception ignored) { }
        }
        loadContextAndUpdateGreeting();
    }

    /** Resolve trajet, bus, last position and update the initial chat line. */
    private void loadContextAndUpdateGreeting() {
        new Thread(() -> {
            try {
                if (enfantId == null) {
                    Platform.runLater(() -> setWelcomeMessage(
                            "Aucun enfant associé. Sélectionnez ou ajoutez un enfant depuis l'accueil pour utiliser l'assistant.", false));
                    return;
                }
                Enfant enfant = enfantService.getEnfantById(enfantId);
                if (enfant == null) {
                    Platform.runLater(() -> setWelcomeMessage("Enfant introuvable. Sélectionnez un enfant depuis l'accueil.", false));
                    return;
                }
                cachedEnfant = enfant;
                Trajet trajet = null;
                try {
                    trajet = trajetService.getTrajetByEnfant(enfantId);
                } catch (Exception ignored) { }
                cachedTrajet = trajet;
                if (trajet != null && trajet.getIdBus() > 0) {
                    cachedBusId = trajet.getIdBus();
                    try {
                        var bus = busService.getById(cachedBusId);
                        cachedBusPlate = bus != null ? bus.getMatricule() : null;
                    } catch (Exception ignored) {
                        cachedBusPlate = null;
                    }
                    try {
                        cachedPosition = positionBusService.getLastPosition(cachedBusId);
                    } catch (Exception ignored) { }
                } else {
                    cachedBusId = null;
                    cachedPosition = null;
                }
                String name = (enfant.getPrenom() != null ? enfant.getPrenom() : "").trim() + " " + (enfant.getNom() != null ? enfant.getNom() : "").trim();
                if (name.isBlank()) name = "Votre enfant";
                String trajetLabel = cachedTrajet != null && cachedTrajet.getNom() != null ? cachedTrajet.getNom() : "ce trajet";
                String greeting = "Je suis prêt. Je suis en train de suivre " + name + " (" + trajetLabel + "). Pose ta question.";
                Platform.runLater(() -> setWelcomeMessage(greeting, true));
            } catch (Exception e) {
                Platform.runLater(() -> setWelcomeMessage("Erreur de chargement du contexte. Réessayez.", false));
            }
        }).start();
    }

    private void setWelcomeMessage(String message, boolean canSend) {
        taChat.clear();
        taChat.appendText("AI: " + message + "\n\n");
        btnSend.setDisable(!canSend);
        if (!canSend) tfMessage.setDisable(true);
    }

    @FXML
    void send(ActionEvent event) {
        String msg = tfMessage.getText();
        if (msg == null || msg.isBlank()) return;

        if (enfantId == null || cachedEnfant == null) {
            taChat.appendText("AI: Sélectionnez ou ajoutez un enfant depuis l'accueil.\n\n");
            return;
        }

        taChat.appendText("Moi: " + msg + "\n");
        tfMessage.clear();
        btnSend.setDisable(true);

        new Thread(() -> {
            try {
                String json = buildRequestJson(msg);
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(API_CHAT_URL))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
                String body = res.body();
                Platform.runLater(() -> {
                    displayStructuredResponse(body);
                    btnSend.setDisable(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    taChat.appendText("AI: Erreur -> " + e.getMessage() + "\n\n");
                    btnSend.setDisable(false);
                });
            }
        }).start();
    }

    /**
     * Every request includes selectedChild + selectedBus + trackingSnapshot (from cached context).
     */
    private String buildRequestJson(String userMessage) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userMessage", userMessage);
        payload.put("message", userMessage);
        payload.put("language", "FR");
        payload.put("userRole", "PARENT");
        payload.put("sessionId", "javafx-" + System.currentTimeMillis());
        payload.put("enfantId", enfantId);
        payload.put("parentId", AppSession.getInstance().getParentId());

        if (cachedEnfant != null) {
            Map<String, Object> selectedChild = new HashMap<>();
            selectedChild.put("childId", cachedEnfant.getEnfantId());
            selectedChild.put("nom", cachedEnfant.getNom());
            selectedChild.put("prenom", cachedEnfant.getPrenom());
            selectedChild.put("onBoard", cachedEnfant.isOnBoard());
            payload.put("selectedChild", selectedChild);
        }

        if (cachedBusId != null) {
            payload.put("busId", cachedBusId);
            Map<String, Object> selectedBus = new HashMap<>();
            selectedBus.put("busId", cachedBusId);
            if (cachedBusPlate != null) selectedBus.put("plateNumber", cachedBusPlate);
            payload.put("selectedBus", selectedBus);
        }

        if (cachedPosition != null) {
            Map<String, Object> trackingSnapshot = new HashMap<>();
            if (cachedPosition.getTimestamp() != null) {
                trackingSnapshot.put("lastUpdateTime", cachedPosition.getTimestamp().format(ISO));
            }
            trackingSnapshot.put("busLat", cachedPosition.getLatitude());
            trackingSnapshot.put("busLng", cachedPosition.getLongitude());
            trackingSnapshot.put("speedKmh", cachedPosition.getVitesse());
            Map<String, Object> etaFields = computeEtaFromTracking(cachedPosition.getLatitude(), cachedPosition.getLongitude(), cachedPosition.getVitesse());
            if (etaFields != null) {
                trackingSnapshot.putAll(etaFields);
            }
            payload.put("trackingSnapshot", trackingSnapshot);
        }

        return mapper.writeValueAsString(payload);
    }

    /**
     * ETA source of truth: uses only stops of the child's trajet, selects nearest stop (by ordre_arret then distance),
     * caps unrealistic distances at 20 km. Returns map with etaMinutes, etaSeconds, nextStopName, distanceToNextStopKm or null.
     */
    private Map<String, Object> computeEtaFromTracking(double busLat, double busLng, double speedFromBus) {
        if (cachedTrajet == null) return null;
        try {
            int trajetId = cachedTrajet.getTrajetId();
            List<Arret> arrets = arretService.getByTrajetId(trajetId);
            if (arrets == null || arrets.isEmpty()) return null;
            // Restrict to current trajet only (getByTrajetId already does this); sort by ordre_arret for consistent next-stop selection
            arrets = arrets.stream()
                    .sorted(Comparator.comparingInt(Arret::getOrdre))
                    .toList();
            Arret next = findNearestStopInOrder(arrets, busLat, busLng);
            if (next == null) return null;
            double distanceKm = haversineKm(busLat, busLng, next.getLatitude(), next.getLongitude());
            // Safety: unrealistic for a school route
            if (distanceKm > MAX_ETA_DISTANCE_KM) {
                LOG.log(Level.FINE, "ETA computation -> distance {0} km > {1} km, treating as invalid", new Object[]{String.format("%.2f", distanceKm), MAX_ETA_DISTANCE_KM});
                return null;
            }
            double speed = (speedFromBus > 5) ? speedFromBus : 30.0;
            int etaMin = (int) Math.round((distanceKm / speed) * 60.0);
            // Cap displayed ETA at 60 min for normal routes
            if (etaMin > 60) {
                LOG.log(Level.FINE, "ETA computation -> eta {0} min > 60, treating as invalid", etaMin);
                return null;
            }
            String stopName = next.getNom() != null ? next.getNom() : "";
            double distRounded = Math.round(distanceKm * 1000.0) / 1000.0;
            LOG.log(Level.FINE, "ETA computation -> stop={0}, distance={1} km, speed={2} km/h, eta={3} min",
                    new Object[]{stopName, String.format("%.2f", distRounded), String.format("%.0f", speed), etaMin});

            Map<String, Object> out = new HashMap<>();
            out.put("etaMinutes", etaMin);
            out.put("etaSeconds", etaMin * 60);
            out.put("nextStopName", stopName);
            out.put("distanceToNextStopKm", distRounded);
            return out;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "ETA computation failed", e);
            return null;
        }
    }

    /**
     * Select the nearest stop by haversine distance among stops of the trajet (already sorted by ordre_arret).
     * Only stops of this trajet are considered; the closest one is the best candidate for "next stop".
     */
    private Arret findNearestStopInOrder(List<Arret> arrets, double lat, double lng) {
        Arret closest = null;
        double minD = Double.MAX_VALUE;
        for (Arret a : arrets) {
            double d = haversineKm(lat, lng, a.getLatitude(), a.getLongitude());
            if (d < minD) {
                minD = d;
                closest = a;
            }
        }
        return closest;
    }

    private static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private void displayStructuredResponse(String body) {
        try {
            JsonNode root = mapper.readTree(body);
            String reply = root.has("reply") ? root.path("reply").asText("") : "";
            String intent = root.has("intent") ? root.path("intent").asText("") : "";

            StringBuilder out = new StringBuilder();
            out.append("AI: ").append(reply);
            if ("MISSING_CONTEXT".equals(intent)) {
                if (root.has("missing_fields") && root.get("missing_fields").isArray()) {
                    out.append("\n  Champs manquants: ");
                    root.get("missing_fields").forEach(f -> out.append(f.asText()).append("; "));
                }
                if (root.has("suggested_actions") && root.get("suggested_actions").isArray()) {
                    out.append("\n  Actions suggérées: ");
                    root.get("suggested_actions").forEach(a -> out.append("• ").append(a.asText()).append("\n    "));
                }
            }
            if (root.has("facts_used") && root.get("facts_used").isObject()) {
                JsonNode facts = root.get("facts_used");
                if (facts.has("lastUpdateTime") && !facts.path("lastUpdateTime").isNull()) {
                    out.append("\n  Dernière mise à jour: ").append(facts.path("lastUpdateTime").asText());
                }
                if (facts.has("busLat") && facts.has("busLng") && !facts.path("busLat").isNull()) {
                    out.append("\n  Position (lat/lng): ").append(facts.path("busLat").asText()).append(", ").append(facts.path("busLng").asText());
                }
            }
            out.append("\n\n");
            taChat.appendText(out.toString());
        } catch (Exception e) {
            taChat.appendText("AI: " + body + "\n\n");
        }
    }

    @FXML
    void goBack(ActionEvent event) {
        ((Stage) taChat.getScene().getWindow()).close();
    }
}
