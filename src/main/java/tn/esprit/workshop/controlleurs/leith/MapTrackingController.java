package tn.esprit.workshop.controlleurs.leith;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import tn.esprit.workshop.model.leith.*;
import tn.esprit.workshop.model.tous.Chauffeur;
import tn.esprit.workshop.services.leith.*;

import java.net.URL;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.sql.SQLException;
import java.util.List;

public class MapTrackingController {

    @FXML private WebView mapView;


    @FXML private Label lblTitle;
    @FXML private Label lblSubTitle;
    @FXML private Label lblChauffeur;
    @FXML private Label lblBus;
    @FXML private Label lblEtaTitle;
    @FXML private Label lblEtaValue;
    @FXML private Label lblEnfantNom;
    @FXML private Label lblEnfantStatus;


    private WebEngine engine;
    private Timeline timeline;

    // paramètres injectés
    private int busId;
    private int busMatricule;
    private TrackingMode mode;
    private Integer enfantId; // null si ECOLE
    private Integer trajetId; // optionnel (si tu veux éviter de recalculer)

    // services
    private final PositionBusService positionBusService = new PositionBusService();
    private final TrajetService trajetService = new TrajetService();
    private final ArretService arretService = new ArretService();
    private final BusService busService = new BusService();
    private final ChauffeurService chauffeurService = new ChauffeurService();
    private final EnfantService enfantService = new EnfantService();
    private static final Logger LOG = Logger.getLogger(MapTrackingController.class.getName());

    // state
    private PositionBus lastPos;

    /**
     * Appelée après chargement du FXML (SceneBuilder)
     */
    @FXML
    public void initialize() {
        engine = mapView.getEngine();

        // chemin réel chez toi : /resources/leith/map/map.html
        URL url = Objects.requireNonNull(
                getClass().getResource("/leith/map/map.html"),
                "map.html introuvable: vérifie src/main/resources/leith/map/map.html"
        );
        engine.load(url.toExternalForm());

        // IMPORTANT : on lance le refresh quand la page est prête
        engine.getLoadWorker().stateProperty().addListener((obs, o, n) -> {
            if (n == Worker.State.SUCCEEDED) {
                System.out.println(engine.executeScript("typeof fixMapSize"));
                engine.executeScript("fixMapSize()");
                try {
                    pushRouteToMap();
                } catch (Exception e) {
                    System.out.println("Erreur pushRouteToMap: " + e.getMessage());
                }


                // Si init(...) a déjà été appelé, on démarre
                if (busId != 0) startAutoRefresh();
            }
        });
    }

    private void pushRouteToMap() throws SQLException {
        if (trajetId <= 0) return;

        List<Arret> arrets = arretService.getByTrajetId(trajetId);
        if (arrets.isEmpty()) return;

        String json = toStopsJson(arrets);
        engine.executeScript("setRoute(" + json + ")");
    }

    private String toStopsJson(List<Arret> arrets) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");

        for (int i = 0; i < arrets.size(); i++) {
            Arret a = arrets.get(i);

            sb.append("{")
                    .append("\"lat\":").append(a.getLatitude()).append(",")
                    .append("\"lng\":").append(a.getLongitude()).append(",")
                    .append("\"name\":").append("\"").append(escapeJson(a.getNom())).append("\",")
                    .append("\"order\":").append(a.getOrdre())
                    .append("}");

            if (i < arrets.size() - 1) sb.append(",");
        }

        sb.append("]");
        return sb.toString();
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", " ")
                .replace("\r", " ");
    }



    /// on injecte le contexte (busId + mode + enfantId)

    public void init(int busId, TrackingMode mode, Integer enfantId, Integer trajetId) throws SQLException {
        this.busId = busMatricule;
        this.mode = mode;
        this.enfantId = enfantId;
        this.trajetId = trajetId;

loadEnfant();

        /// Texte UI selon le mode
        if (mode == TrackingMode.PARENT) {
            lblTitle.setText("Suivi du bus de votre enfant");
            lblEtaTitle.setText("Arrivée estimée :");
        } else {
            lblTitle.setText("Suivi du bus (École)");
            lblEtaTitle.setText("Prochain point :");
        }
        Bus bus = busService.getById(busId);

        if (bus != null) {

            lblBus.setText(bus.getMatricule());

            Chauffeur chauffeur = chauffeurService.getById(bus.getIdChauffeur());

            if (chauffeur != null) {
                lblChauffeur.setText(
                        chauffeur.getNom() + " " + chauffeur.getPrenom()
                );
            } else {
                lblChauffeur.setText("Non assigné");
            }

        }



        //// lblBus.setText("Bus Matricule = " + busMatricule);
         //// lblChauffeur.setText("—"); // on liera plus tard au chauffeur via bus

      /*  Bus bus = busService.getById(busId);

        if (bus != null) {

            // Affichage matricule
            lblBus.setText(bus.getMatricule());

            // Affichage chauffeur (si tu as la table chauffeur)
            if (bus.getIdChauffeur() > 0) {
                Chauffeur chauffeur = chauffeurService.getById(bus.getIdChauffeur());
                if (chauffeur != null) {
                    lblChauffeur.setText(chauffeur.getNom() + " " + chauffeur.getPrenom());
                } else {
                    lblChauffeur.setText("Chauffeur inconnu");
                }
            } else {
                lblChauffeur.setText("Non assigné");
            }

        } else {
            lblBus.setText("Bus inconnu");
            lblChauffeur.setText("—");
        }
*/

        /// Si map déjà chargée, on peut démarrer
        if (engine != null && engine.getLoadWorker().getState() == Worker.State.SUCCEEDED) {
            startAutoRefresh();
        }


    }

    private void startAutoRefresh() {
        if (timeline != null) return; // éviter double start
        timeline = new Timeline(new KeyFrame(Duration.seconds(2), e -> refresh()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void refresh() {
        try {
            lastPos = positionBusService.getLastPosition(busId);
            if (lastPos == null) {
                lblEtaValue.setText("Aucune position");
                return;
            }

            double lat = lastPos.getLatitude();
            double lng = lastPos.getLongitude();

            engine.executeScript("updateBusPosition(" + lat + "," + lng + ");");

            // ETA minimal : calcul vers prochain arrêt (ECOLE) ou arrêt “cible” (PARENT)
            // Pour l’instant, on fait v1 : prochain arrêt du trajet (si on a trajetId)
            String etaText = computeEtaText(lat, lng);
            lblEtaValue.setText(etaText);

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Erreur MapTrackingController.refresh()", ex);

        }
    }

    /**
     * ETA v1 (simple) :
     * - on récupère le trajet (si tu as enfantId -> trajet)
     * - on récupère les arrêts du trajet
     * - on prend "prochain arrêt" = le plus proche (v1)
     * - ETA = distance / 30kmh (vitesse moyenne)
     */
    private String computeEtaText(double lat, double lng) throws SQLException {

        int tId = resolveTrajetId();
        if (tId == 0) return "—";

        List<Arret> arrets = arretService.getArretsByTrajetOrdered(tId);
        if (arrets.isEmpty()) return "—";

        Arret nearest = findNearestArret(lat, lng, arrets);
        double distKm = haversineKm(lat, lng, nearest.getLatitude(), nearest.getLongitude());

        double speedKmh = 30.0; // v1 fixe
        double minutes = (distKm / speedKmh) * 60.0;

        if (mode == TrackingMode.PARENT) {
            return String.format("%.0f min (vers %s)", minutes, nearest.getNom());
        } else {
            return String.format("%.0f min (next: %s)", minutes, nearest.getNom());
        }
    }

    private int resolveTrajetId() throws SQLException {
        if (trajetId != null) return trajetId;

        // v1 : si parent -> on suppose enfantId fourni et Enfant a trajet_id
        if (mode == TrackingMode.PARENT && enfantId != null) {
            Trajet t = trajetService.getTrajetByEnfant(enfantId);
            if (t != null) {
                trajetId = t.getId();
                return trajetId;
            }
        }

        // v1 école : tu peux décider d’envoyer trajetId plus tard
        // sinon on retourne 0
        return 0;
    }

    private Arret findNearestArret(double lat, double lng, List<Arret> arrets) {
        Arret best = arrets.get(0);
        double bestD = Double.MAX_VALUE;

        for (Arret a : arrets) {
            double d = haversineKm(lat, lng, a.getLatitude(), a.getLongitude());
            if (d < bestD) {
                bestD = d;
                best = a;
            }
        }
        return best;
    }

    private void loadEnfant() {
        try {
            Enfant enfant = enfantService.getEnfantById(enfantId);
            System.out.println("DEBUG enfant = " + enfant);
            updateEnfantStatus(enfant);
        } catch (Exception e) {
            System.out.println("ERROR enfant");
        }
    }
    private void updateEnfantStatus(Enfant enfant) {

        if (enfant == null) {
            lblEnfantNom.setText("Aucun enfant");
            lblEnfantStatus.setText("—");
            lblEnfantStatus.setStyle("-fx-text-fill: #7f8c8d; -fx-font-weight: bold;");
            return;
        }

        lblEnfantNom.setText(enfant.getNom() + " " + enfant.getPrenom());

        if (enfant.isOnBoard()) {

            lblEnfantStatus.setText("ON BOARD");
            lblEnfantStatus.setStyle(
                    "-fx-text-fill: #2ecc71; " +
                            "-fx-font-weight: bold; " +
                            "-fx-background-color: rgba(46,204,113,0.15); " +
                            "-fx-padding: 4 8 4 8; " +
                            "-fx-background-radius: 8;"
            );

        } else {

            lblEnfantStatus.setText("NOT ON BOARD");
            lblEnfantStatus.setStyle(
                    "-fx-text-fill: #e74c3c; " +
                            "-fx-font-weight: bold; " +
                            "-fx-background-color: rgba(231,76,60,0.15); " +
                            "-fx-padding: 4 8 4 8; " +
                            "-fx-background-radius: 8;"
            );
        }
    }



    // Haversine distance (km)
    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a =
                Math.sin(dLat/2) * Math.sin(dLat/2) +
                        Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                                Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }

    @FXML
    private void recenter() {
        if (lastPos != null) {
            engine.executeScript("panToBus(" + lastPos.getLatitude() + "," + lastPos.getLongitude() + ");");
        }
    }

    @FXML
    private void close() {
        stop();
        // ferme la fenêtre si tu utilises un Stage dédié
        mapView.getScene().getWindow().hide();
    }

    public void stop() {
        if (timeline != null) timeline.stop();
    }

    public void forceMapResize() {
        if (engine != null) {
            engine.executeScript("fixMapSize()");
        }
    }

}
