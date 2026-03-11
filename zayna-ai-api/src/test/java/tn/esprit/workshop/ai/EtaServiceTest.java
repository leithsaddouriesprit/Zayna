package tn.esprit.workshop.ai;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import tn.esprit.workshop.ai.dto.TrackingSnapshotDto;
import tn.esprit.workshop.ai.service.EtaResult;
import tn.esprit.workshop.ai.service.EtaService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {"eta.enabled=true", "eta.strategy=speed"})
class EtaServiceTest {

    @Autowired
    private EtaService etaService;

    @Test
    @DisplayName("computeEta returns ETA when distance and speed present")
    void computeEta_withDistanceAndSpeed_returnsEta() {
        TrackingSnapshotDto snap = new TrackingSnapshotDto();
        snap.setDistanceToNextStopKm(5.0);
        snap.setSpeedKmh(60.0);
        snap.setNextStopName("Gare");

        Optional<EtaResult> result = etaService.computeEta(snap);
        assertTrue(result.isPresent());
        assertEquals(5, result.get().getEtaMinutes());
        assertEquals(300, result.get().getEtaSeconds());
        assertEquals(5.0, result.get().getDistanceToNextStopKm());
        assertEquals("Gare", result.get().getNextStopName());
    }

    @Test
    @DisplayName("computeEta returns empty when speed is 0")
    void computeEta_speedZero_returnsEmpty() {
        TrackingSnapshotDto snap = new TrackingSnapshotDto();
        snap.setDistanceToNextStopKm(2.0);
        snap.setSpeedKmh(0.0);

        assertTrue(etaService.computeEta(snap).isEmpty());
    }

    @Test
    @DisplayName("computeEta returns empty when speed is null")
    void computeEta_speedNull_returnsEmpty() {
        TrackingSnapshotDto snap = new TrackingSnapshotDto();
        snap.setDistanceToNextStopKm(2.0);

        assertTrue(etaService.computeEta(snap).isEmpty());
    }

    @Test
    @DisplayName("computeEta returns existing etaMinutes when already set")
    void computeEta_alreadySet_returnsExisting() {
        TrackingSnapshotDto snap = new TrackingSnapshotDto();
        snap.setEtaMinutes(12);
        snap.setEtaSeconds(720);

        Optional<EtaResult> result = etaService.computeEta(snap);
        assertTrue(result.isPresent());
        assertEquals(12, result.get().getEtaMinutes());
    }
}
