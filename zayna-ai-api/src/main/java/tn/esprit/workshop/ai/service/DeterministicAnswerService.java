package tn.esprit.workshop.ai.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tn.esprit.workshop.ai.dto.*;

import java.util.*;

/**
 * Rule-based answers from tracking snapshot when we have enough data.
 * Reduces hallucinations by answering deterministically when possible.
 */
@Service
public class DeterministicAnswerService {

    @Value("${eta.staleSeconds:60}")
    private int staleSeconds;

    public StructuredChatResponse answerIfPossible(Intent intent,
                                                    TrackingContextService.TrackingContextContract contract) {
        SelectedChildDto child = contract.selectedChild;
        SelectedBusDto bus = contract.selectedBus;
        TrackingSnapshotDto snap = contract.trackingSnapshot;
        DataQualityDto dq = contract.dataQuality;

        if (intent == Intent.ON_BOARD_STATUS && child != null) {
            return answerOnBoard(child, snap);
        }
        if (intent == Intent.CHILD_LOCATION && child != null && snap != null && snap.getBusLat() != null && snap.getBusLng() != null) {
            return answerChildLocation(child, snap);
        }
        if (intent == Intent.BUS_LOCATION && snap != null && snap.getBusLat() != null && snap.getBusLng() != null) {
            return answerBusLocation(snap);
        }
        if (intent == Intent.ETA && child != null && snap != null) {
            return answerEta(child, snap);
        }
        if (intent == Intent.SAFETY_STATUS && child != null && snap != null) {
            return answerSafety(child, snap, dq);
        }
        return null;
    }

    private StructuredChatResponse answerOnBoard(SelectedChildDto child, TrackingSnapshotDto snap) {
        Boolean onBoard = child.getOnBoard();
        String name = (child.getPrenom() != null ? child.getPrenom() : "") + " " + (child.getNom() != null ? child.getNom() : "").trim();
        if (name.isBlank()) name = "Votre enfant";

        String fr;
        Map<String, Object> facts = new HashMap<>();
        facts.put("childId", child.getChildId());
        facts.put("onBoard", onBoard);
        if (snap != null && snap.getLastUpdateTime() != null) facts.put("lastUpdateTime", snap.getLastUpdateTime());

        if (onBoard == null) {
            fr = "Je n'ai pas l'information de présence dans le bus pour " + name + ". Actualisez le suivi.";
        } else if (Boolean.TRUE.equals(onBoard)) {
            fr = "Oui, " + name + " est actuellement dans le bus.";
        } else {
            fr = "Non, " + name + " n'est pas dans le bus pour le moment.";
        }
        return buildResponse(Intent.ON_BOARD_STATUS, fr, facts, null, null);
    }

    private StructuredChatResponse answerChildLocation(SelectedChildDto child, TrackingSnapshotDto snap) {
        String name = (child.getPrenom() != null ? child.getPrenom() : "").trim() + " " + (child.getNom() != null ? child.getNom() : "").trim();
        if (name.isBlank()) name = "Votre enfant";
        Map<String, Object> facts = new HashMap<>();
        facts.put("childId", child.getChildId());
        facts.put("onBoard", child.getOnBoard());
        facts.put("busLat", snap.getBusLat());
        facts.put("busLng", snap.getBusLng());
        facts.put("lastUpdateTime", snap.getLastUpdateTime());

        String fr;
        List<String> suggestedActions;
        if (Boolean.TRUE.equals(child.getOnBoard())) {
            fr = "La position de " + name + " est approchée par celle du bus (il/elle est à bord) : latitude " + snap.getBusLat() + ", longitude " + snap.getBusLng()
                    + ". Dernière mise à jour : " + (snap.getLastUpdateTime() != null ? snap.getLastUpdateTime() : "—") + ".";
            suggestedActions = null;
        } else {
            fr = "La position exacte de " + name + " n'est pas disponible car il/elle n'est pas à bord du bus. Voici la position du bus : latitude " + snap.getBusLat() + ", longitude " + snap.getBusLng()
                    + ". Dernière mise à jour : " + (snap.getLastUpdateTime() != null ? snap.getLastUpdateTime() : "—") + ".";
            suggestedActions = List.of("Contacter l'école ou le chauffeur si besoin", "Attendre la montée pour le suivi", "Actualiser le suivi");
        }
        return buildResponse(Intent.CHILD_LOCATION, fr, facts, null, suggestedActions);
    }

    private StructuredChatResponse answerBusLocation(TrackingSnapshotDto snap) {
        String fr = "Le bus est à la position : latitude " + snap.getBusLat() + ", longitude " + snap.getBusLng()
                + ". Dernière mise à jour : " + (snap.getLastUpdateTime() != null ? snap.getLastUpdateTime() : "—") + ".";
        Map<String, Object> facts = new HashMap<>();
        facts.put("busLat", snap.getBusLat());
        facts.put("busLng", snap.getBusLng());
        facts.put("lastUpdateTime", snap.getLastUpdateTime());
        facts.put("speedKmh", snap.getSpeedKmh());
        return buildResponse(Intent.BUS_LOCATION, fr, facts, null, null);
    }

    private StructuredChatResponse answerEta(SelectedChildDto child, TrackingSnapshotDto snap) {
        String name = (child.getPrenom() != null ? child.getPrenom() : "") + " " + (child.getNom() != null ? child.getNom() : "").trim();
        if (name.isBlank()) name = "Votre enfant";

        Integer etaMin = snap.getEtaMinutes();
        String nextStop = snap.getNextStopName();
        Double dist = snap.getDistanceToNextStopKm();

        Map<String, Object> facts = new HashMap<>();
        facts.put("childId", child.getChildId());
        facts.put("etaMinutes", etaMin);
        facts.put("nextStopName", nextStop);
        facts.put("distanceToNextStopKm", dist);
        facts.put("lastUpdateTime", snap.getLastUpdateTime());

        String fr;
        List<String> missing = new ArrayList<>();
        List<String> actions = null;
        if (etaMin != null && etaMin >= 0) {
            if (nextStop != null && !nextStop.isBlank()) {
                fr = "ETA ~ " + etaMin + " min vers l'arrêt \"" + nextStop + "\"";
            } else {
                fr = "ETA ~ " + etaMin + " min";
            }
            if (dist != null || snap.getSpeedKmh() != null) {
                fr += " (";
                if (dist != null) fr += "distance ~ " + String.format("%.2f", dist) + " km";
                if (dist != null && snap.getSpeedKmh() != null) fr += ", ";
                if (snap.getSpeedKmh() != null) fr += "vitesse ~ " + String.format("%.0f", snap.getSpeedKmh()) + " km/h";
                fr += ")";
            }
            fr += ".";
            if (Boolean.TRUE.equals(snap.getLastUpdateTime() != null && isStale(snap.getLastUpdateTime()))) {
                fr += " Données un peu anciennes : actualisez le suivi pour plus de précision.";
                actions = List.of("Actualiser le suivi");
            }
        } else {
            boolean noSpeed = snap.getSpeedKmh() == null || snap.getSpeedKmh() <= 0.1;
            boolean noDist = dist == null || dist <= 0;
            if (noSpeed && noDist) {
                fr = "ETA indisponible (vitesse et distance manquantes).";
            } else if (noSpeed) {
                fr = "ETA indisponible (vitesse manquante ou nulle).";
                if (dist != null) fr += " Distance au prochain arrêt : " + String.format("%.2f", dist) + " km.";
            } else if (noDist) {
                fr = "ETA indisponible (distance au prochain arrêt manquante).";
            } else {
                fr = "ETA indisponible.";
                if (nextStop != null) fr += " Prochain arrêt : " + nextStop + ".";
            }
            fr += " Actualisez le suivi pour obtenir une ETA.";
            actions = List.of("Actualiser le suivi", "Vérifier la vitesse du bus", "Vérifier la distance au prochain arrêt");
            if (etaMin == null) missing.add("eta");
            return buildResponse(Intent.ETA, fr, facts, missing.isEmpty() ? null : missing, actions);
        }
        return buildResponse(Intent.ETA, fr, facts, null, actions);
    }

    private StructuredChatResponse answerSafety(SelectedChildDto child, TrackingSnapshotDto snap, DataQualityDto dq) {
        String name = (child.getPrenom() != null ? child.getPrenom() : "").trim() + " " + (child.getNom() != null ? child.getNom() : "").trim();
        if (name.isBlank()) name = "Votre enfant";
        Boolean onBoard = child.getOnBoard();
        String fr;
        List<String> suggestedActions = null;
        if (Boolean.TRUE.equals(onBoard)) {
            fr = "Le suivi indique que " + name + " est à bord. Dernière mise à jour : " + (snap.getLastUpdateTime() != null ? snap.getLastUpdateTime() : "—");
            if (snap.getSpeedKmh() != null) {
                fr += ". Vitesse : " + String.format("%.0f", snap.getSpeedKmh()) + " km/h";
            }
            fr += ". Aucune anomalie détectée dans les données de suivi (selon les dernières données).";
            if (snap.getLastUpdateTime() != null && isStale(snap.getLastUpdateTime())) {
                fr += " Données anciennes (plus de " + staleSeconds + " s) : actualisez le suivi.";
                suggestedActions = List.of("Actualiser le suivi");
            }
        } else if (Boolean.FALSE.equals(onBoard)) {
            fr = name + " n'est pas actuellement à bord du bus. Je ne peux pas confirmer sa situation exacte. Si vous vous inquiétez, nous vous conseillons de contacter l'école ou le chauffeur.";
            suggestedActions = List.of("Contacter l'école ou le chauffeur", "Attendre la montée", "Actualiser le suivi");
        } else {
            fr = "Je n'ai pas l'information de présence dans le bus. Actualisez le suivi pour savoir si " + name + " va bien.";
        }
        Map<String, Object> facts = new HashMap<>();
        facts.put("childId", child.getChildId());
        facts.put("onBoard", onBoard);
        facts.put("lastUpdateTime", snap.getLastUpdateTime());
        facts.put("speedKmh", snap.getSpeedKmh());
        return buildResponse(Intent.SAFETY_STATUS, fr, facts, null, suggestedActions);
    }

    private boolean isStale(String lastUpdateTime) {
        try {
            java.time.LocalDateTime t = java.time.LocalDateTime.parse(lastUpdateTime, java.time.format.DateTimeFormatter.ISO_DATE_TIME);
            return java.time.Duration.between(t, java.time.LocalDateTime.now()).getSeconds() > staleSeconds;
        } catch (Exception e) {
            return true;
        }
    }

    private StructuredChatResponse buildResponse(Intent intent, String answerFr, Map<String, Object> facts,
                                                 List<String> missingFields, List<String> suggestedActions) {
        StructuredChatResponse r = new StructuredChatResponse();
        r.setIntent(intent.name());
        r.setAnswer_fr(answerFr);
        r.setReply(answerFr);
        r.setFacts_used(facts != null ? facts : new HashMap<>());
        r.setMissing_fields(missingFields != null ? missingFields : new ArrayList<>());
        r.setSuggested_actions(suggestedActions != null ? suggestedActions : new ArrayList<>());
        r.setConfidence(1.0);
        return r;
    }
}
