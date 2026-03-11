package tn.esprit.workshop.ai.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tn.esprit.workshop.ai.dto.TrackingSnapshotDto;

import java.util.Optional;

/**
 * Computes ETA to next stop: speed-based (haversine distance / speed) or OSRM when enabled.
 */
@Service
public class EtaService {

    @Value("${eta.enabled:true}")
    private boolean enabled;

    @Value("${eta.strategy:speed}")
    private String strategy;

    /**
     * Compute ETA from snapshot. If snapshot already has etaMinutes, returns it.
     * Otherwise uses distance + speed (strategy=speed) when available.
     */
    public Optional<EtaResult> computeEta(TrackingSnapshotDto snapshot) {
        if (snapshot == null || !enabled) return Optional.empty();
        if (snapshot.getEtaMinutes() != null && snapshot.getEtaMinutes() >= 0) {
            return Optional.of(new EtaResult(
                    snapshot.getEtaMinutes(),
                    snapshot.getEtaSeconds(),
                    snapshot.getDistanceToNextStopKm(),
                    snapshot.getNextStopName()));
        }
        if ("speed".equalsIgnoreCase(strategy)) {
            return computeEtaSpeed(snapshot);
        }
        return Optional.empty();
    }

    private Optional<EtaResult> computeEtaSpeed(TrackingSnapshotDto snapshot) {
        Double dist = snapshot.getDistanceToNextStopKm();
        Double speed = snapshot.getSpeedKmh();
        if (dist == null || dist < 0 || speed == null || speed <= 0.1) {
            return Optional.empty();
        }
        int etaMinutes = (int) Math.ceil((dist / speed) * 60.0);
        if (etaMinutes < 0) return Optional.empty();
        EtaResult r = new EtaResult();
        r.setEtaMinutes(etaMinutes);
        r.setEtaSeconds(etaMinutes * 60);
        r.setDistanceToNextStopKm(dist);
        r.setNextStopName(snapshot.getNextStopName());
        return Optional.of(r);
    }
}
