package tn.esprit.workshop.ai.service;
import org.springframework.stereotype.Service;
import javax.sql.DataSource;
import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;



@Service
public class ContextBuilderService {

    private final DataSource dataSource;

    public ContextBuilderService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public String build(Integer busId, Integer enfantId) {

        ObjectMapper mapper = new ObjectMapper();

        Map<String, Object> root = new HashMap<>();
        root.put("source", "db");

        Map<String, Object> bus = new HashMap<>();
        Map<String, Object> trajet = new HashMap<>();
        Map<String, Object> position = new HashMap<>();
        Map<String, Object> nextStop = new HashMap<>();
        Map<String, Object> enfant = new HashMap<>();

        // anomalies as array
        java.util.List<String> anomalies = new java.util.ArrayList<>();

        Integer trajetId = null;

        Double busLat = null, busLng = null, speedKmh = null;
        LocalDateTime posTs = null;

        Double stopLat = null, stopLng = null;

        try (Connection conn = dataSource.getConnection()) {

            // 1) BUS
            if (busId != null) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT id, numero_bus, matricule, capacite, id_chauffeur, actif FROM bus WHERE id=?"
                )) {
                    ps.setInt(1, busId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            bus.put("id", rs.getInt("id"));
                            bus.put("numero_bus", rs.getString("numero_bus"));
                            bus.put("matricule", rs.getString("matricule"));
                            bus.put("capacite", rs.getInt("capacite"));
                            bus.put("id_chauffeur", rs.getInt("id_chauffeur"));
                            bus.put("actif", rs.getInt("actif") == 1);
                        } else {
                            bus.put("error", "introuvable");
                        }
                    }
                } catch (SQLException e) {
                    bus.put("error", "sql:" + e.getMessage());
                }
            } else {
                bus.put("id", null);
            }
            root.put("bus", bus);

            // 2) TRAJET
            if (busId != null) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT id, nom, id_bus, id_ecole, heure_depart, prix, statut " +
                                "FROM trajet WHERE id_bus=? LIMIT 1"
                )) {
                    ps.setInt(1, busId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            trajetId = rs.getInt("id");
                            trajet.put("id", trajetId);
                            trajet.put("nom", rs.getString("nom"));
                            trajet.put("statut", rs.getString("statut"));
                            trajet.put("heure_depart", rs.getString("heure_depart"));
                            trajet.put("prix", rs.getBigDecimal("prix"));
                        } else {
                            trajet.put("id", null);
                            trajet.put("error", "aucun_trajet");
                            anomalies.add("aucun_trajet");
                        }
                    }
                } catch (SQLException e) {
                    trajet.put("error", "sql:" + e.getMessage());
                    anomalies.add("trajet_sql_error");
                }
            }
            root.put("trajet", trajet);

            // 3) DERNIÈRE POSITION
            if (busId != null) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT latitude, longitude, vitesse, timestamp " +
                                "FROM position_bus WHERE id_bus=? ORDER BY timestamp DESC LIMIT 1"
                )) {
                    ps.setInt(1, busId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            busLat = rs.getDouble("latitude");
                            busLng = rs.getDouble("longitude");
                            speedKmh = rs.getDouble("vitesse");

                            Timestamp ts = rs.getTimestamp("timestamp");
                            posTs = (ts != null) ? ts.toLocalDateTime() : null;

                            position.put("latitude", busLat);
                            position.put("longitude", busLng);
                            position.put("vitesse_kmh", speedKmh);
                            position.put("timestamp", posTs != null ? posTs.toString() : null);

                            if (posTs != null) {
                                long minutes = Duration.between(posTs, LocalDateTime.now()).toMinutes();
                                position.put("age_minutes", minutes);
                                if (minutes > 10) anomalies.add("position_trop_ancienne");
                            }
                            if (speedKmh != null && speedKmh > 130) anomalies.add("vitesse_aberrante");
                            if (speedKmh != null && speedKmh <= 0.1) anomalies.add("bus_probablement_arrete");
                        } else {
                            position.put("error", "aucune_position");
                            anomalies.add("aucune_position");
                        }
                    }
                } catch (SQLException e) {
                    position.put("error", "sql:" + e.getMessage());
                    anomalies.add("position_sql_error");
                }
            }
            root.put("position", position);

            // 4) NEXT STOP
            if (trajetId != null) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT nom, latitude, longitude, ordre_arret, heure_prevue " +
                                "FROM arret WHERE id_trajet=? ORDER BY ordre_arret ASC"
                )) {
                    ps.setInt(1, trajetId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            nextStop.put("nom", rs.getString("nom"));
                            stopLat = rs.getDouble("latitude");
                            stopLng = rs.getDouble("longitude");
                            nextStop.put("latitude", stopLat);
                            nextStop.put("longitude", stopLng);
                            nextStop.put("ordre", rs.getInt("ordre_arret"));
                            nextStop.put("heure_prevue", rs.getString("heure_prevue"));
                        } else {
                            nextStop.put("error", "aucun_arret");
                            anomalies.add("aucun_arret");
                        }
                    }
                } catch (SQLException e) {
                    nextStop.put("error", "sql:" + e.getMessage());
                    anomalies.add("arret_sql_error");
                }
            } else {
                nextStop.put("error", "trajet_inconnu");
            }
            root.put("nextStop", nextStop);

            // 5) ETA
            if (busLat != null && busLng != null && stopLat != null && stopLng != null && speedKmh != null && speedKmh > 0.1) {
                double distanceKm = haversine(busLat, busLng, stopLat, stopLng);
                int etaMinutes = (int) Math.round((distanceKm / speedKmh) * 60.0);

                root.put("distance_km", round(distanceKm, 3));
                root.put("eta_minutes", etaMinutes);

                if (etaMinutes > 120) anomalies.add("eta_tres_eleve");
            } else {
                root.put("eta_minutes", null);
            }

            // 6) ENFANT + DANS BUS
            if (enfantId != null) {
                Integer enfantTrajetId = null;
                Integer onBoard = null;

                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT id, nom, prenom, trajet_id, on_board FROM enfant WHERE id=?"
                )) {
                    ps.setInt(1, enfantId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            enfantTrajetId = rs.getInt("trajet_id");
                            onBoard = rs.getInt("on_board");

                            enfant.put("id", rs.getInt("id"));
                            enfant.put("nom", rs.getString("nom"));
                            enfant.put("prenom", rs.getString("prenom"));
                            enfant.put("trajet_id", enfantTrajetId);
                            enfant.put("on_board", onBoard);

                            boolean affecteAuTrajet =
                                    enfantTrajetId != null &&
                                            trajetId != null &&
                                            enfantTrajetId.equals(trajetId);

                            boolean estMonte = onBoard != null && onBoard == 1;

                            boolean estDansLeBus = affecteAuTrajet && estMonte;

                            enfant.put("affecte_trajet", affecteAuTrajet);
                            enfant.put("monte", estMonte);
                            enfant.put("dans_bus", estDansLeBus);

                        } else {
                            enfant.put("error", "introuvable");
                            anomalies.add("enfant_introuvable");
                        }
                    }
                } catch (SQLException e) {
                    enfant.put("error", "sql:" + e.getMessage());
                    anomalies.add("enfant_sql_error");
                }
            }
            root.put("enfant", enfant);

        } catch (SQLException e) {
            root.put("db_error", e.getMessage());
            anomalies.add("db_connection_error");
        }

        root.put("anomalies", anomalies);

        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
        } catch (Exception e) {
            return "{\"error\":\"json_generation_failed\"}";
        }
    }

    private static void safeAppend(StringBuilder sb, String key, Object value) {
        if (value == null) return;
        sb.append(key).append("=").append(value).append("\n");
    }

    private static double round(double x, int digits) {
        double p = Math.pow(10, digits);
        return Math.round(x * p) / p;
    }

    private static double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}