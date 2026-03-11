package tn.esprit.workshop.ai.service;

import org.springframework.stereotype.Service;
import tn.esprit.workshop.ai.dto.*;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds the full Tracking Context Contract (selectedChild, selectedBus, trackingSnapshot, dataQuality)
 * from DB using busId and/or enfantId. Resolves busId from enfant's trajet when only enfantId is provided.
 */
@Service
public class TrackingContextService {

    private static final int STALE_SECONDS = 60;
    private static final int TEST_BUS_ID = 1;
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_DATE_TIME;

    private final DataSource dataSource;
    private final EtaService etaService;

    public TrackingContextService(DataSource dataSource, EtaService etaService) {
        this.dataSource = dataSource;
        this.etaService = etaService;
    }

    /**
     * Build contract from DB. If only enfantId is set, resolves busId via trajet.
     */
    public TrackingContextContract buildContract(Integer busId, Integer enfantId) {
        Integer effectiveBusId = busId;
        Integer trajetId = null;

        try (Connection conn = dataSource.getConnection()) {
            if (effectiveBusId == null && enfantId != null) {
                effectiveBusId = resolveBusIdFromEnfant(conn, enfantId);
            }
            if (effectiveBusId != null) {
                trajetId = getTrajetIdForBus(conn, effectiveBusId);
            }

            SelectedChildDto selectedChild = buildSelectedChild(conn, enfantId, trajetId);
            SelectedBusDto selectedBus = buildSelectedBus(conn, effectiveBusId);
            TrackingSnapshotDto snapshot = buildTrackingSnapshot(conn, effectiveBusId, trajetId);
            // ETA: client is source of truth when it sends trackingSnapshot.etaMinutes; only compute when missing.
            if (snapshot != null && snapshot.getEtaMinutes() == null) {
                etaService.computeEta(snapshot).ifPresent(r -> {
                    snapshot.setEtaMinutes(r.getEtaMinutes());
                    snapshot.setEtaSeconds(r.getEtaSeconds());
                });
            }
            DataQualityDto dataQuality = buildDataQuality(selectedChild, selectedBus, snapshot, effectiveBusId);

            return new TrackingContextContract(selectedChild, selectedBus, snapshot, dataQuality);
        } catch (SQLException e) {
            DataQualityDto dq = new DataQualityDto();
            dq.setIsTestMode(false);
            dq.setIsStale(true);
            dq.setMissingFields(List.of("db_connection_error"));
            return new TrackingContextContract(null, null, null, dq);
        }
    }

    private Integer resolveBusIdFromEnfant(Connection conn, int enfantId) throws SQLException {
        String sql = "SELECT trajet_id FROM enfant WHERE id = ? AND actif = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, enfantId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int tid = rs.getInt("trajet_id");
                if (!rs.wasNull()) {
                    return getBusIdFromTrajet(conn, tid);
                }
            }
        }
        return null;
    }

    private Integer getBusIdFromTrajet(Connection conn, int trajetId) throws SQLException {
        String sql = "SELECT id_bus FROM trajet WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, trajetId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int bid = rs.getInt("id_bus");
                return rs.wasNull() ? null : bid;
            }
        }
        return null;
    }

    private Integer getTrajetIdForBus(Connection conn, int busId) throws SQLException {
        String sql = "SELECT id FROM trajet WHERE id_bus = ? AND actif = 1 LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, busId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id");
        }
        return null;
    }

    private SelectedChildDto buildSelectedChild(Connection conn, Integer enfantId, Integer trajetActifId) throws SQLException {
        if (enfantId == null) return null;
        String sql = "SELECT id, nom, prenom, trajet_id, on_board FROM enfant WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, enfantId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                SelectedChildDto dto = new SelectedChildDto();
                dto.setChildId(rs.getInt("id"));
                dto.setNom(rs.getString("nom"));
                dto.setPrenom(rs.getString("prenom"));
                int ob = rs.getInt("on_board");
                dto.setOnBoard(rs.wasNull() ? null : (ob == 1));
                return dto;
            }
        }
        return null;
    }

    private SelectedBusDto buildSelectedBus(Connection conn, Integer busId) throws SQLException {
        if (busId == null) return null;
        String sql = "SELECT id, matricule FROM bus WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, busId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                SelectedBusDto dto = new SelectedBusDto();
                dto.setBusId(rs.getInt("id"));
                dto.setPlateNumber(rs.getString("matricule"));
                return dto;
            }
        }
        return null;
    }

    private TrackingSnapshotDto buildTrackingSnapshot(Connection conn, Integer busId, Integer trajetId) throws SQLException {
        if (busId == null) return null;
        TrackingSnapshotDto dto = new TrackingSnapshotDto();
        dto.setTrajetId(trajetId);
        dto.setRouteId(trajetId);

        String posSql = "SELECT latitude, longitude, vitesse, timestamp FROM position_bus WHERE id_bus = ? ORDER BY timestamp DESC LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(posSql)) {
            ps.setInt(1, busId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                dto.setBusLat(rs.getDouble("latitude"));
                dto.setBusLng(rs.getDouble("longitude"));
                double vit = rs.getDouble("vitesse");
                dto.setSpeedKmh(rs.wasNull() ? null : vit);
                Timestamp ts = rs.getTimestamp("timestamp");
                if (ts != null) {
                    LocalDateTime ldt = ts.toLocalDateTime();
                    dto.setLastUpdateTime(ldt.format(ISO));
                }
            }
        }

        if (trajetId != null) {
            String stopSql = "SELECT nom, latitude, longitude FROM arret WHERE id_trajet = ? ORDER BY ordre_arret ASC LIMIT 1";
            try (PreparedStatement ps = conn.prepareStatement(stopSql)) {
                ps.setInt(1, trajetId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    dto.setNextStopName(rs.getString("nom"));
                    double stopLat = rs.getDouble("latitude");
                    double stopLng = rs.getDouble("longitude");
                    dto.setNextStopLat(stopLat);
                    dto.setNextStopLng(stopLng);
                    if (dto.getBusLat() != null && dto.getBusLng() != null) {
                        double dist = haversine(dto.getBusLat(), dto.getBusLng(), stopLat, stopLng);
                        dto.setDistanceToNextStopKm(round(dist, 3));
                        if (dto.getSpeedKmh() != null && dto.getSpeedKmh() > 0.1) {
                            int etaMin = (int) Math.ceil((dist / dto.getSpeedKmh()) * 60.0);
                            dto.setEtaMinutes(etaMin);
                            dto.setEtaSeconds(etaMin * 60);
                        }
                    }
                }
            }
        }
        return dto;
    }

    private DataQualityDto buildDataQuality(SelectedChildDto child, SelectedBusDto bus, TrackingSnapshotDto snapshot, Integer busId) {
        DataQualityDto dq = new DataQualityDto();
        List<String> missing = new ArrayList<>();

        boolean testMode = (busId != null && busId == TEST_BUS_ID) || busId == null;
        dq.setIsTestMode(testMode);
        if (busId == null) missing.add("busId");
        if (child == null) missing.add("selectedChild");
        else if (child.getChildId() == null) missing.add("childId");

        if (snapshot != null) {
            if (snapshot.getLastUpdateTime() == null) missing.add("lastUpdateTime");
            if (snapshot.getBusLat() == null) missing.add("busLat");
            if (snapshot.getBusLng() == null) missing.add("busLng");
            if (snapshot.getEtaMinutes() == null && snapshot.getEtaSeconds() == null) missing.add("eta");

            long ageSeconds = Long.MAX_VALUE;
            if (snapshot.getLastUpdateTime() != null) {
                try {
                    LocalDateTime last = LocalDateTime.parse(snapshot.getLastUpdateTime(), ISO);
                    ageSeconds = Duration.between(last, LocalDateTime.now()).getSeconds();
                } catch (Exception ignored) {}
            }
            dq.setIsStale(ageSeconds > STALE_SECONDS);
            if (ageSeconds > STALE_SECONDS) missing.add("stale_tracking");
        } else {
            dq.setIsStale(true);
            missing.add("trackingSnapshot");
        }

        dq.setMissingFields(missing);
        return dq;
    }

    private static double round(double x, int digits) {
        double p = Math.pow(10, digits);
        return Math.round(x * p) / p;
    }

    private static double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    /** Holder for the full contract. */
    public static class TrackingContextContract {
        public final SelectedChildDto selectedChild;
        public final SelectedBusDto selectedBus;
        public final TrackingSnapshotDto trackingSnapshot;
        public final DataQualityDto dataQuality;

        public TrackingContextContract(SelectedChildDto selectedChild, SelectedBusDto selectedBus,
                                       TrackingSnapshotDto trackingSnapshot, DataQualityDto dataQuality) {
            this.selectedChild = selectedChild;
            this.selectedBus = selectedBus;
            this.trackingSnapshot = trackingSnapshot;
            this.dataQuality = dataQuality;
        }
    }
}
