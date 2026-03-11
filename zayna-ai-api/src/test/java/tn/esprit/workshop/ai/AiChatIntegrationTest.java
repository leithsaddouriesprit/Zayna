package tn.esprit.workshop.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tn.esprit.workshop.ai.dto.*;
import tn.esprit.workshop.ai.service.TrackingContextService;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end tests for Parent-facing AI chat: intent, deterministic answers, missing context, test mode.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AiChatIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private TrackingContextService trackingContextService;

    private TrackingContextService.TrackingContextContract contract(
            SelectedChildDto child,
            SelectedBusDto bus,
            TrackingSnapshotDto snapshot,
            DataQualityDto dataQuality) {
        return new TrackingContextService.TrackingContextContract(child, bus, snapshot, dataQuality);
    }

    private DataQualityDto dataQuality(boolean isTestMode, boolean isStale, List<String> missingFields) {
        DataQualityDto dq = new DataQualityDto();
        dq.setIsTestMode(isTestMode);
        dq.setIsStale(isStale);
        dq.setMissingFields(missingFields != null ? missingFields : List.of());
        return dq;
    }

    @Nested
    @DisplayName("Parent questions - expected outputs")
    class ParentQuestionTests {

        @Test
        @DisplayName("1. Mon enfant est dans le bus ? -> ON_BOARD_STATUS, answer yes/no")
        void onBoardStatus_enfantDansLeBus() throws Exception {
            SelectedChildDto child = new SelectedChildDto(10, "Dupont", "Marie", true);
            TrackingSnapshotDto snap = new TrackingSnapshotDto();
            snap.setLastUpdateTime("2025-03-05T10:00:00");
            when(trackingContextService.buildContract(isNull(), anyInt()))
                    .thenReturn(contract(child, new SelectedBusDto(2, "TN-1234"), snap, dataQuality(false, false, List.of())));

            String body = objectMapper.writeValueAsString(Map.of(
                    "userMessage", "Mon enfant est dans le bus ?",
                    "enfantId", 10));
            MvcResult result = mockMvc.perform(post("/ai/chat")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.intent").value("ON_BOARD_STATUS"))
                    .andExpect(jsonPath("$.reply").isNotEmpty())
                    .andReturn();
            String reply = objectMapper.readTree(result.getResponse().getContentAsString()).path("reply").asText();
            assertTrue(reply.toLowerCase().contains("oui") || reply.toLowerCase().contains("est") && reply.toLowerCase().contains("bus"),
                    "Reply should state child is on board: " + reply);
        }

        @Test
        @DisplayName("2. Où est mon enfant ? position exacte -> CHILD_LOCATION with coordinates or approximation note")
        void childLocation_positionExacte() throws Exception {
            SelectedChildDto child = new SelectedChildDto(10, "Dupont", "Marie", true);
            TrackingSnapshotDto snap = new TrackingSnapshotDto();
            snap.setBusLat(36.8);
            snap.setBusLng(10.1);
            snap.setLastUpdateTime("2025-03-05T10:00:00");
            when(trackingContextService.buildContract(isNull(), anyInt()))
                    .thenReturn(contract(child, new SelectedBusDto(2, "TN-1234"), snap, dataQuality(false, false, List.of())));

            MvcResult result = mockMvc.perform(post("/ai/chat")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of(
                                    "userMessage", "Où est mon enfant ? position exacte",
                                    "enfantId", 10))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.intent").value("CHILD_LOCATION"))
                    .andExpect(jsonPath("$.reply").isNotEmpty())
                    .andReturn();
            String reply = objectMapper.readTree(result.getResponse().getContentAsString()).path("reply").asText();
            assertTrue(reply.contains("36.8") && reply.contains("10.1") || reply.toLowerCase().contains("latitude") && reply.toLowerCase().contains("longitude"),
                    "Reply should include coordinates or approximation: " + reply);
        }

        @Test
        @DisplayName("3. Est-il on board ? -> ON_BOARD_STATUS")
        void onBoardStatus_estIlOnBoard() throws Exception {
            SelectedChildDto child = new SelectedChildDto(10, "Martin", "Lucas", false);
            when(trackingContextService.buildContract(isNull(), anyInt()))
                    .thenReturn(contract(child, new SelectedBusDto(2, "TN-5678"), null, dataQuality(false, true, List.of())));

            mockMvc.perform(post("/ai/chat")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of(
                                    "userMessage", "Est-il on board ?",
                                    "enfantId", 10))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.intent").value("ON_BOARD_STATUS"))
                    .andExpect(jsonPath("$.reply").isNotEmpty());
        }

        @Test
        @DisplayName("4. Quand il arrive ? -> ETA with minutes/next stop")
        void eta_quandIlArrive() throws Exception {
            SelectedChildDto child = new SelectedChildDto(10, "Bernard", "Emma", true);
            TrackingSnapshotDto snap = new TrackingSnapshotDto();
            snap.setEtaMinutes(12);
            snap.setNextStopName("Arrêt École");
            snap.setLastUpdateTime("2025-03-05T10:00:00");
            when(trackingContextService.buildContract(isNull(), anyInt()))
                    .thenReturn(contract(child, new SelectedBusDto(2, "TN-9999"), snap, dataQuality(false, false, List.of())));

            MvcResult result = mockMvc.perform(post("/ai/chat")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of(
                                    "userMessage", "Quand il arrive ?",
                                    "enfantId", 10))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.intent").value("ETA"))
                    .andExpect(jsonPath("$.reply").isNotEmpty())
                    .andReturn();
            String reply = objectMapper.readTree(result.getResponse().getContentAsString()).path("reply").asText();
            assertTrue(reply.contains("12") || reply.toLowerCase().contains("minute") || reply.contains("Arrêt"),
                    "Reply should mention ETA or next stop: " + reply);
        }

        @Test
        @DisplayName("5. Mon fils est cv ? -> SAFETY_STATUS (ça va / en sécurité)")
        void safetyStatus_monFilsEstCv() throws Exception {
            SelectedChildDto child = new SelectedChildDto(10, "Petit", "Tom", true);
            TrackingSnapshotDto snap = new TrackingSnapshotDto();
            snap.setLastUpdateTime("2025-03-05T10:00:00");
            when(trackingContextService.buildContract(isNull(), anyInt()))
                    .thenReturn(contract(child, new SelectedBusDto(2, "TN-1111"), snap, dataQuality(false, false, List.of())));

            mockMvc.perform(post("/ai/chat")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of(
                                    "userMessage", "Mon fils est cv ?",
                                    "enfantId", 10))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.intent").value("SAFETY_STATUS"))
                    .andExpect(jsonPath("$.reply").isNotEmpty());
        }

        @Test
        @DisplayName("6. ETA question with missing eta -> safe response (distance/next stop), no hallucination")
        void eta_missingEta_safeResponse() throws Exception {
            SelectedChildDto child = new SelectedChildDto(10, "Leroy", "Jade", true);
            TrackingSnapshotDto snap = new TrackingSnapshotDto();
            snap.setEtaMinutes(null);
            snap.setNextStopName("Gare");
            snap.setDistanceToNextStopKm(2.5);
            snap.setLastUpdateTime("2025-03-05T10:00:00");
            when(trackingContextService.buildContract(isNull(), anyInt()))
                    .thenReturn(contract(child, new SelectedBusDto(2, "TN-2222"), snap, dataQuality(false, false, List.of("eta"))));

            MvcResult result = mockMvc.perform(post("/ai/chat")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of(
                                    "userMessage", "Quand il arrive ?",
                                    "enfantId", 10))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.intent").value("ETA"))
                    .andReturn();
            String reply = objectMapper.readTree(result.getResponse().getContentAsString()).path("reply").asText();
            assertTrue(reply.toLowerCase().contains("pas") || reply.contains("Gare") || reply.contains("2.5") || reply.toLowerCase().contains("actualisez"),
                    "Should not invent ETA; mention next stop/distance or ask to refresh: " + reply);
        }

        @Test
        @DisplayName("7. Position exacte but missing busLat/busLng -> MISSING_CONTEXT + suggested_actions")
        void childLocation_missingCoordinates_missingContext() throws Exception {
            SelectedChildDto child = new SelectedChildDto(10, "Moreau", "Léa", true);
            TrackingSnapshotDto snap = new TrackingSnapshotDto();
            snap.setBusLat(null);
            snap.setBusLng(null);
            snap.setLastUpdateTime(null);
            DataQualityDto dq = dataQuality(false, true, List.of("busLat", "busLng"));
            when(trackingContextService.buildContract(isNull(), anyInt()))
                    .thenReturn(contract(child, new SelectedBusDto(2, "TN-3333"), snap, dq));

            mockMvc.perform(post("/ai/chat")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of(
                                    "userMessage", "Où est mon enfant ? position exacte",
                                    "enfantId", 10))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.intent").value("MISSING_CONTEXT"))
                    .andExpect(jsonPath("$.missing_fields").isArray())
                    .andExpect(jsonPath("$.suggested_actions").isArray())
                    .andExpect(jsonPath("$.reply").isNotEmpty());
        }

        @Test
        @DisplayName("8. With child present (even busId=1) -> normal answer, NO 'test mode' in reply")
        void withChild_noTestModeInReply() throws Exception {
            SelectedChildDto child = new SelectedChildDto(10, "Test", "Enfant", false);
            when(trackingContextService.buildContract(1, 10))
                    .thenReturn(contract(child, new SelectedBusDto(1, "TEST"), null, dataQuality(true, true, List.of())));

            MvcResult result = mockMvc.perform(post("/ai/chat")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of(
                                    "userMessage", "Mon enfant est dans le bus ?",
                                    "enfantId", 10,
                                    "busId", 1))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.intent").value("ON_BOARD_STATUS"))
                    .andExpect(jsonPath("$.reply").isNotEmpty())
                    .andReturn();
            String reply = objectMapper.readTree(result.getResponse().getContentAsString()).path("reply").asText();
            assertFalse(reply.toLowerCase().contains("mode test") || reply.toLowerCase().contains("vous êtes en mode test"),
                    "Reply must NOT mention test mode: " + reply);
        }
    }

    @Nested
    @DisplayName("onBoard consistency - greeting and CHILD_LOCATION")
    class OnBoardConsistencyTests {

        @Test
        @DisplayName("Pure greeting 'bonjour': reply must NOT contain Position, ETA, Dernière mise à jour, à bord")
        void greeting_pureBonjour_noTrackingData() throws Exception {
            SelectedChildDto child = new SelectedChildDto(10, "Ben Salah", "Ali", false);
            TrackingSnapshotDto snap = new TrackingSnapshotDto();
            snap.setBusLat(36.8);
            snap.setBusLng(10.1);
            snap.setLastUpdateTime("2025-03-05T10:00:00");
            when(trackingContextService.buildContract(isNull(), anyInt()))
                    .thenReturn(contract(child, new SelectedBusDto(2, "TN-5678"), snap, dataQuality(false, false, List.of())));

            MvcResult result = mockMvc.perform(post("/ai/chat")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of(
                                    "userMessage", "bonjour",
                                    "enfantId", 10))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.reply").isNotEmpty())
                    .andReturn();
            String reply = objectMapper.readTree(result.getResponse().getContentAsString()).path("reply").asText();
            assertTrue(reply.contains("Bonjour"), "Greeting must include Bonjour: " + reply);
            assertFalse(reply.contains("Position du bus") || reply.contains("Position :"), "Greeting must NOT contain position data: " + reply);
            assertFalse(reply.contains("ETA prochain") || (reply.contains("~") && reply.contains(" min.")), "Greeting must NOT contain ETA value: " + reply);
            assertFalse(reply.contains("Dernière mise à jour"), "Greeting must NOT contain Dernière mise à jour: " + reply);
            assertFalse(reply.contains("Votre enfant est à bord") || reply.contains("n'est pas à bord"), "Greeting must NOT contain onBoard status: " + reply);
        }

        @Test
        @DisplayName("Summary request when onBoard=false: must mention bus and not on board")
        void summaryRequest_onBoardFalse_mentionsBus_notOnBoard() throws Exception {
            SelectedChildDto child = new SelectedChildDto(10, "Ben Salah", "Ali", false);
            TrackingSnapshotDto snap = new TrackingSnapshotDto();
            snap.setBusLat(36.8);
            snap.setBusLng(10.1);
            snap.setLastUpdateTime("2025-03-05T10:00:00");
            when(trackingContextService.buildContract(isNull(), anyInt()))
                    .thenReturn(contract(child, new SelectedBusDto(2, "TN-5678"), snap, dataQuality(false, false, List.of())));

            MvcResult result = mockMvc.perform(post("/ai/chat")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of(
                                    "userMessage", "donne moi un résumé",
                                    "enfantId", 10))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.intent").value("SUMMARY_STATUS"))
                    .andExpect(jsonPath("$.reply").isNotEmpty())
                    .andReturn();
            String reply = objectMapper.readTree(result.getResponse().getContentAsString()).path("reply").asText();
            assertTrue(reply.toLowerCase().contains("bus"), "Summary must mention bus: " + reply);
            assertTrue(reply.toLowerCase().contains("pas à bord") || reply.contains("n'est pas à bord"),
                    "Summary must state child is not on board: " + reply);
        }

        @Test
        @DisplayName("CHILD_LOCATION when onBoard=false: must NOT say 'enfant est dans le bus', must label bus position clearly")
        void childLocation_onBoardFalse_noApproximationLabelBusPosition() throws Exception {
            SelectedChildDto child = new SelectedChildDto(10, "Martin", "Léa", false);
            TrackingSnapshotDto snap = new TrackingSnapshotDto();
            snap.setBusLat(36.9);
            snap.setBusLng(10.2);
            snap.setLastUpdateTime("2025-03-05T11:00:00");
            when(trackingContextService.buildContract(isNull(), anyInt()))
                    .thenReturn(contract(child, new SelectedBusDto(2, "TN-9999"), snap, dataQuality(false, false, List.of())));

            MvcResult result = mockMvc.perform(post("/ai/chat")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of(
                                    "userMessage", "Où est mon enfant ? position exacte",
                                    "enfantId", 10))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.intent").value("CHILD_LOCATION"))
                    .andExpect(jsonPath("$.reply").isNotEmpty())
                    .andReturn();
            String reply = objectMapper.readTree(result.getResponse().getContentAsString()).path("reply").asText();
            assertFalse(reply.toLowerCase().contains("l'enfant est dans le bus") || reply.toLowerCase().contains("approximation : l'enfant est dans le bus"),
                    "Must NOT claim child is in the bus when onBoard=false: " + reply);
            assertTrue(reply.toLowerCase().contains("position du bus") || reply.toLowerCase().contains("position du bus :"),
                    "Must clearly label bus position: " + reply);
        }

        @Test
        @DisplayName("CHILD_LOCATION when onBoard=true: may mention approximation (position via bus)")
        void childLocation_onBoardTrue_mayMentionApproximation() throws Exception {
            SelectedChildDto child = new SelectedChildDto(10, "Bernard", "Emma", true);
            TrackingSnapshotDto snap = new TrackingSnapshotDto();
            snap.setBusLat(36.7);
            snap.setBusLng(10.0);
            snap.setLastUpdateTime("2025-03-05T09:30:00");
            when(trackingContextService.buildContract(isNull(), anyInt()))
                    .thenReturn(contract(child, new SelectedBusDto(2, "TN-1111"), snap, dataQuality(false, false, List.of())));

            MvcResult result = mockMvc.perform(post("/ai/chat")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of(
                                    "userMessage", "Où est mon enfant ?",
                                    "enfantId", 10))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.intent").value("CHILD_LOCATION"))
                    .andExpect(jsonPath("$.reply").isNotEmpty())
                    .andReturn();
            String reply = objectMapper.readTree(result.getResponse().getContentAsString()).path("reply").asText();
            assertTrue(reply.toLowerCase().contains("approchée") || reply.toLowerCase().contains("à bord") || reply.toLowerCase().contains("bus"),
                    "May mention approximation / bus when on board: " + reply);
        }
    }

    @Nested
    @DisplayName("Greeting: short only, no tracking")
    class GreetingTests {

        @Test
        @DisplayName("bonjour! reply must NOT contain Position, ETA, Dernière mise à jour, à bord")
        void greeting_bonjourExclamation_shortOnlyNoTracking() throws Exception {
            SelectedChildDto child = new SelectedChildDto(10, "Ben Salah", "Ali", true);
            TrackingSnapshotDto snap = new TrackingSnapshotDto();
            snap.setLastUpdateTime("2025-03-05T10:00:00");
            snap.setBusLat(36.8);
            snap.setBusLng(10.1);
            when(trackingContextService.buildContract(isNull(), anyInt()))
                    .thenReturn(contract(child, new SelectedBusDto(2, "TN-5678"), snap, dataQuality(false, false, List.of())));

            MvcResult result = mockMvc.perform(post("/ai/chat")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of(
                                    "userMessage", "bonjour!",
                                    "enfantId", 10))))
                    .andExpect(status().isOk())
                    .andReturn();
            String reply = objectMapper.readTree(result.getResponse().getContentAsString()).path("reply").asText();
            assertTrue(reply.contains("Bonjour"), "Greeting must include Bonjour: " + reply);
            assertFalse(reply.contains("Position du bus") || reply.contains("Position :"), "Greeting must NOT contain position data: " + reply);
            assertFalse(reply.contains("ETA prochain") || (reply.contains("~") && reply.contains(" min.")), "Greeting must NOT contain ETA value: " + reply);
            assertFalse(reply.contains("Dernière mise à jour"), "Greeting must NOT contain Dernière mise à jour: " + reply);
            assertFalse(reply.contains("Votre enfant est à bord") || reply.contains("n'est pas à bord"), "Greeting must NOT contain onBoard status: " + reply);
        }
    }

    @Nested
    @DisplayName("SAFETY_STATUS: data-driven, no strong reassurance")
    class SafetyStatusTests {

        @Test
        @DisplayName("SAFETY_STATUS must NOT contain strong reassurance phrases")
        void safetyStatus_noStrongPhrases() throws Exception {
            SelectedChildDto child = new SelectedChildDto(10, "Dupont", "Marie", true);
            TrackingSnapshotDto snap = new TrackingSnapshotDto();
            snap.setLastUpdateTime("2025-03-05T10:00:00");
            snap.setSpeedKmh(45.0);
            when(trackingContextService.buildContract(isNull(), anyInt()))
                    .thenReturn(contract(child, new SelectedBusDto(2, "TN-1234"), snap, dataQuality(false, false, List.of())));

            MvcResult result = mockMvc.perform(post("/ai/chat")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of(
                                    "userMessage", "Mon fils est cv ?",
                                    "enfantId", 10))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.intent").value("SAFETY_STATUS"))
                    .andReturn();
            String reply = objectMapper.readTree(result.getResponse().getContentAsString()).path("reply").asText().toLowerCase();
            assertFalse(reply.contains("tout va bien"), "Must not contain 'tout va bien': " + reply);
            assertFalse(reply.contains("aucun danger"), "Must not contain 'aucun danger': " + reply);
            assertFalse(reply.contains("en sécurité"), "Must not contain 'en sécurité': " + reply);
            assertFalse(reply.contains("rassuré"), "Must not contain 'rassuré': " + reply);
        }

        @Test
        @DisplayName("SAFETY_STATUS when data stale must contain stale warning and suggested_actions")
        void safetyStatus_staleData_containsWarning() throws Exception {
            SelectedChildDto child = new SelectedChildDto(10, "Martin", "Lucas", true);
            TrackingSnapshotDto snap = new TrackingSnapshotDto();
            snap.setLastUpdateTime("2020-01-01T08:00:00");
            snap.setSpeedKmh(30.0);
            when(trackingContextService.buildContract(isNull(), anyInt()))
                    .thenReturn(contract(child, new SelectedBusDto(2, "TN-5678"), snap, dataQuality(false, true, List.of("stale_tracking"))));

            MvcResult result = mockMvc.perform(post("/ai/chat")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of(
                                    "userMessage", "ça va ?",
                                    "enfantId", 10))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.intent").value("SAFETY_STATUS"))
                    .andReturn();
            String reply = objectMapper.readTree(result.getResponse().getContentAsString()).path("reply").asText();
            assertTrue(reply.contains("Données anciennes") || reply.contains("actualisez le suivi"),
                    "Stale data must trigger warning: " + reply);
            JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
            assertTrue(root.has("suggested_actions") && root.get("suggested_actions").isArray() && root.get("suggested_actions").size() > 0,
                    "Must have suggested_actions when stale");
        }
    }

    @Nested
    @DisplayName("ETA pipeline: computed, missing when speed=0, stale warning")
    class EtaPipelineTests {

        @Test
        @DisplayName("ETA computed from speed + distance when available")
        void eta_computedFromSpeedAndDistance() throws Exception {
            SelectedChildDto child = new SelectedChildDto(10, "Bernard", "Emma", true);
            TrackingSnapshotDto snap = new TrackingSnapshotDto();
            snap.setBusLat(36.8);
            snap.setBusLng(10.1);
            snap.setSpeedKmh(30.0);
            snap.setDistanceToNextStopKm(2.5);
            snap.setNextStopName("École");
            snap.setEtaMinutes(5);
            snap.setLastUpdateTime("2025-03-05T10:00:00");
            when(trackingContextService.buildContract(isNull(), anyInt()))
                    .thenReturn(contract(child, new SelectedBusDto(2, "TN-9999"), snap, dataQuality(false, false, List.of())));

            MvcResult result = mockMvc.perform(post("/ai/chat")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of(
                                    "userMessage", "Quand il arrive ?",
                                    "enfantId", 10))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.intent").value("ETA"))
                    .andReturn();
            String reply = objectMapper.readTree(result.getResponse().getContentAsString()).path("reply").asText();
            assertTrue(reply.contains("ETA ~") && reply.contains("5") && reply.contains("min"),
                    "ETA reply should show 'ETA ~ X min': " + reply);
            assertTrue(reply.contains("distance") || reply.contains("vitesse"),
                    "ETA reply should include distance or vitesse: " + reply);
        }

        @Test
        @DisplayName("Given busLat/busLng + nextStop + speedKmh=60, ETA computed and reply contains ETA ~ and minutes")
        void eta_withCoordsAndSpeed_replyContainsEtaFormat() throws Exception {
            SelectedChildDto child = new SelectedChildDto(10, "Test", "Enfant", true);
            TrackingSnapshotDto snap = new TrackingSnapshotDto();
            snap.setBusLat(36.8);
            snap.setBusLng(10.1);
            snap.setNextStopLat(36.85);
            snap.setNextStopLng(10.15);
            snap.setDistanceToNextStopKm(5.0);
            snap.setSpeedKmh(60.0);
            snap.setEtaMinutes(5);
            snap.setNextStopName("École");
            snap.setLastUpdateTime("2025-03-05T10:00:00");
            when(trackingContextService.buildContract(isNull(), anyInt()))
                    .thenReturn(contract(child, new SelectedBusDto(2, "TN-1234"), snap, dataQuality(false, false, List.of())));

            MvcResult result = mockMvc.perform(post("/ai/chat")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of(
                                    "userMessage", "Quand il arrive ?",
                                    "enfantId", 10))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.intent").value("ETA"))
                    .andReturn();
            String reply = objectMapper.readTree(result.getResponse().getContentAsString()).path("reply").asText();
            assertTrue(reply.contains("ETA ~") && reply.contains("5") && reply.contains("min"),
                    "Reply must contain ETA ~ and minutes: " + reply);
        }

        @Test
        @DisplayName("ETA uses client-provided etaMinutes when JavaFX sends full trackingSnapshot")
        void eta_clientProvidesEtaMinutes_apiUsesIt() throws Exception {
            Map<String, Object> selectedChild = Map.of(
                    "childId", 10,
                    "nom", "Dupont",
                    "prenom", "Marie",
                    "onBoard", true);
            Map<String, Object> selectedBus = Map.of("busId", 2, "plateNumber", "TN-1234");
            Map<String, Object> trackingSnapshot = new java.util.HashMap<>(Map.of(
                    "lastUpdateTime", "2025-03-05T10:00:00",
                    "busLat", 36.8,
                    "busLng", 10.1,
                    "speedKmh", 40.0,
                    "etaMinutes", 7,
                    "nextStopName", "École",
                    "distanceToNextStopKm", 4.67));
            Map<String, Object> body = new java.util.HashMap<>();
            body.put("userMessage", "Quand il arrive ?");
            body.put("selectedChild", selectedChild);
            body.put("selectedBus", selectedBus);
            body.put("trackingSnapshot", trackingSnapshot);

            MvcResult result = mockMvc.perform(post("/ai/chat")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.intent").value("ETA"))
                    .andReturn();
            String reply = objectMapper.readTree(result.getResponse().getContentAsString()).path("reply").asText();
            assertTrue(reply.contains("7") && reply.contains("min"),
                    "API must use client-provided etaMinutes (7 min): " + reply);
        }

        @Test
        @DisplayName("ETA missing when speed=0 or null -> ETA indisponible + suggested_actions")
        void eta_missingWhenNoSpeed_indisponibleAndActions() throws Exception {
            SelectedChildDto child = new SelectedChildDto(10, "Leroy", "Jade", true);
            TrackingSnapshotDto snap = new TrackingSnapshotDto();
            snap.setNextStopName("Gare");
            snap.setDistanceToNextStopKm(1.0);
            snap.setSpeedKmh(0.0);
            snap.setLastUpdateTime("2025-03-05T10:00:00");
            when(trackingContextService.buildContract(isNull(), anyInt()))
                    .thenReturn(contract(child, new SelectedBusDto(2, "TN-2222"), snap, dataQuality(false, false, List.of())));

            MvcResult result = mockMvc.perform(post("/ai/chat")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of(
                                    "userMessage", "Quand il arrive ?",
                                    "enfantId", 10))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.intent").value("ETA"))
                    .andReturn();
            String reply = objectMapper.readTree(result.getResponse().getContentAsString()).path("reply").asText();
            assertTrue(reply.contains("ETA indisponible") || reply.contains("vitesse inconnue") || reply.contains("nulle"),
                    "Must say ETA indisponible when speed missing/zero: " + reply);
            JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
            assertTrue(root.has("suggested_actions") && root.get("suggested_actions").isArray(),
                    "Must have suggested_actions when ETA missing");
        }

        @Test
        @DisplayName("ETA with stale data triggers warning in reply")
        void eta_staleData_warningInReply() throws Exception {
            SelectedChildDto child = new SelectedChildDto(10, "Petit", "Tom", true);
            TrackingSnapshotDto snap = new TrackingSnapshotDto();
            snap.setEtaMinutes(10);
            snap.setNextStopName("Arrêt Centre");
            snap.setLastUpdateTime("2020-01-01T07:00:00");
            when(trackingContextService.buildContract(isNull(), anyInt()))
                    .thenReturn(contract(child, new SelectedBusDto(2, "TN-1111"), snap, dataQuality(false, true, List.of())));

            MvcResult result = mockMvc.perform(post("/ai/chat")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of(
                                    "userMessage", "Quand il arrive ?",
                                    "enfantId", 10))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.intent").value("ETA"))
                    .andReturn();
            String reply = objectMapper.readTree(result.getResponse().getContentAsString()).path("reply").asText();
            assertTrue(reply.contains("anciennes") || reply.contains("actualisez"),
                    "Stale ETA data should trigger warning: " + reply);
        }
    }
}
