package tn.esprit.workshop.ai.agent;

import org.springframework.stereotype.Service;
import tn.esprit.workshop.ai.agent.action.AgentActionExecutionException;
import tn.esprit.workshop.ai.agent.action.AgentChatActionDetector;
import tn.esprit.workshop.ai.agent.action.AgentChatActionExecutor;
import tn.esprit.workshop.ai.agent.action.AgentChatActionPayload;
import tn.esprit.workshop.ai.agent.action.AgentChatPendingActionStore;
import tn.esprit.workshop.ai.agent.dto.AgentChatRequestDto;
import tn.esprit.workshop.ai.agent.dto.AgentChatResponseDto;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class AgentChatService {

    private static final Logger LOG = Logger.getLogger(AgentChatService.class.getName());

    static final String ERR_AGENT = "AGENT_NOT_FOUND";
    static final String ERR_ECOLE = "ECOLE_NOT_FOUND";

    private static final String NO_RESULT = "Je n'ai trouvé aucun résultat pour cette demande.";
    private static final String TECHNICAL_MSG = "Le service assistant agent est momentanément indisponible.";

    private static final String UNSUPPORTED_REPLY =
            "Je n'ai pas compris cette demande. Pouvez-vous reformuler ? "
                    + "Par exemple : combien de maîtresses, chauffeurs ou enfants, liste des bus ou trajets, "
                    + "candidatures en attente, maîtresse du bus 12, chauffeur du bus 12, "
                    + "ou quel bus est affecté à [nom].";

    private final AgentChatIntentDetector intentDetector;
    private final AgentChatDataService data;
    private final AgentChatActionDetector actionDetector;
    private final AgentChatPendingActionStore pendingActionStore;
    private final AgentChatActionExecutor actionExecutor;

    public AgentChatService(
            AgentChatIntentDetector intentDetector,
            AgentChatDataService data,
            AgentChatActionDetector actionDetector,
            AgentChatPendingActionStore pendingActionStore,
            AgentChatActionExecutor actionExecutor) {
        this.intentDetector = intentDetector;
        this.data = data;
        this.actionDetector = actionDetector;
        this.pendingActionStore = pendingActionStore;
        this.actionExecutor = actionExecutor;
    }

    public AgentChatResponseDto handle(AgentChatRequestDto req) {
        try {
            if (req == null) {
                return AgentChatResponseDto.ok("Veuillez poser une question sur votre école.", "EMPTY");
            }
            if (req.userId == null || req.userId <= 0) {
                return AgentChatResponseDto.error(
                        "Session agent invalide. Reconnectez-vous.",
                        "INVALID_USER");
            }

            Integer ecoleId = data.findEcoleIdForAgentUser(req.userId);
            if (ecoleId == null) {
                return AgentChatResponseDto.error(
                        "Je ne trouve pas de compte agent associé à votre utilisateur.",
                        ERR_AGENT);
            }

            String ecoleNom = data.getEcoleNom(ecoleId);
            if (ecoleNom == null) {
                return AgentChatResponseDto.error(
                        "École introuvable pour votre compte.",
                        ERR_ECOLE);
            }

            // --- Level 3 : confirmation / annulation d'une action proposée ---
            if (req.confirmPendingActionId != null && !req.confirmPendingActionId.isBlank()) {
                if (req.confirmAction == null) {
                    return AgentChatResponseDto.ok(
                            "Indiquez si vous confirmez ou annulez l'action (boutons Confirmer / Annuler).",
                            "ACTION_CONFIRM_INCOMPLETE");
                }
                if (!Boolean.TRUE.equals(req.confirmAction)) {
                    pendingActionStore.cancelWithoutExecute(
                            req.confirmPendingActionId.trim(), req.userId, ecoleId);
                    return AgentChatResponseDto.ok("Action annulée.", "ACTION_CANCELLED");
                }
                AgentChatActionPayload payload = pendingActionStore.take(
                        req.confirmPendingActionId.trim(), req.userId, ecoleId);
                if (payload == null) {
                    return AgentChatResponseDto.ok(
                            "Cette proposition a expiré ou est invalide. Reformulez votre demande.",
                            "ACTION_EXPIRED");
                }
                try {
                    String out = actionExecutor.execute(ecoleId, payload);
                    return AgentChatResponseDto.ok(out, payload.type().name());
                } catch (AgentActionExecutionException ex) {
                    return AgentChatResponseDto.ok(ex.getMessage(), "ACTION_FAILED");
                }
            }

            String msg = req.message != null ? req.message.trim() : "";
            if (msg.isBlank()) {
                return AgentChatResponseDto.ok("Veuillez poser une question sur votre école.", "EMPTY");
            }
            String normalized = AgentChatTextNormalizer.forMatching(msg);
            LOG.log(Level.INFO, "Agent chat input: raw={0}, normalized={1}, userId={2}, ecoleId={3}",
                    new Object[]{msg, normalized, req.userId, ecoleId});

            // --- Level 3 : nouvelle détection d'action (avant lecture) ---
            AgentChatActionDetector.ActionParseResult ar = actionDetector.parse(msg, ecoleId, data);
            if (ar instanceof AgentChatActionDetector.ActionParseResult.Clarify clarify) {
                LOG.log(Level.INFO, "Agent chat action clarify: {0}", clarify.message());
                String intentTag = AgentChatActionDetector.MSG_ACTION_FORBIDDEN.equals(clarify.message())
                        ? "FORBIDDEN"
                        : "ACTION_CLARIFY";
                return AgentChatResponseDto.ok(clarify.message(), intentTag);
            }
            if (ar instanceof AgentChatActionDetector.ActionParseResult.Proposal proposal) {
                LOG.log(Level.INFO, "Agent chat action proposal detected: {0}", proposal.payload().type());
                String token = pendingActionStore.put(req.userId, ecoleId, proposal.payload());
                String text = AgentChatActionDetector.buildProposalText(proposal.payload());
                return AgentChatResponseDto.proposal(text, token, "ACTION_PROPOSAL");
            }

            // --- Level 2 : lecture ---
            AgentChatIntentDetector.Detection d = intentDetector.detect(msg);
            LOG.log(Level.INFO, "Agent chat intent detected: {0}", d.type());

            AgentChatResponseDto response = switch (d.type()) {
                case OUT_OF_SCOPE_GLOBAL -> AgentChatResponseDto.ok(
                        "Je ne peux répondre que sur les données de votre école (« " + ecoleNom + " »), pas sur toute la base.",
                        d.type().name());
                case GREETING_SMALLTALK -> AgentChatResponseDto.ok(
                        "Bonjour ! Je peux vous aider sur les effectifs, bus, trajets, maîtresses, chauffeurs, "
                                + "enfants et candidatures de votre école.",
                        d.type().name());
                case UNSUPPORTED -> AgentChatResponseDto.ok(UNSUPPORTED_REPLY, d.type().name());
                default -> {
                    String reply = executeIntent(ecoleId, ecoleNom, d);
                    yield AgentChatResponseDto.ok(nonBlankOr(reply, NO_RESULT), d.type().name());
                }
            };
            LOG.info("Agent chat query handled successfully");
            return response;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Agent chat handle failed", e);
            return AgentChatResponseDto.ok(TECHNICAL_MSG, "TECHNICAL_FAILURE");
        }
    }

    private static String nonBlankOr(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value;
    }

    private String executeIntent(int ecoleId, String ecoleNom, AgentChatIntentDetector.Detection d) {
        return switch (d.type()) {
            case COUNT_MAITRESSES -> {
                int n = data.countMaitresses(ecoleId);
                yield n == 0
                        ? "Je n'ai trouvé aucune maîtresse dans votre école (« " + ecoleNom + " »)."
                        : "Vous avez " + n + " maîtresse(s) dans votre école (« " + ecoleNom + " »).";
            }
            case COUNT_ENFANTS -> {
                int n = data.countEnfantsForEcole(ecoleId);
                yield n == 0
                        ? "Je n'ai trouvé aucun enfant actif rattaché à un trajet de votre école."
                        : "Vous avez " + n + " enfant(s) dans votre école (actifs, rattachés à un trajet).";
            }
            case COUNT_ENFANTS_ACCEPTED_CANDIDATURES -> {
                int n = data.countCandidatureEnfantAccepteesScopedTrajetEcole(ecoleId);
                yield "Candidatures enfant acceptées pour votre école : " + n + ".";
            }
            case COUNT_BUS -> {
                int n = data.countBusForEcole(ecoleId);
                yield n == 0
                        ? "Je n'ai trouvé aucun bus actif pour votre école."
                        : "Vous avez " + n + " bus actif(s) dans votre école.";
            }
            case COUNT_TRAJETS -> {
                int n = data.countTrajetsForEcole(ecoleId);
                yield "Vous avez " + n + " trajet(s) pour votre école.";
            }
            case COUNT_PENDING_ENFANT_CANDIDATURES -> {
                int n = data.countCandidatureEnfantByStatut(ecoleId, "ENVOYEE");
                yield "Candidatures enfant en attente (statut envoyée) : " + n + ".";
            }
            case COUNT_PENDING_CHAUFFEUR_CANDIDATURES -> {
                int n = data.countCandidatureChauffeurPending(ecoleId);
                yield "Candidatures chauffeur en attente (statut envoyée) : " + n + ".";
            }
            case COUNT_BUSES_WITHOUT_MAITRESSE -> {
                int n = data.countBusesWithoutMaitresse(ecoleId);
                yield n == 0
                        ? "Je n'ai trouvé aucun bus sans maîtresse dans votre école (ou aucun bus actif)."
                        : "Bus sans maîtresse assignée dans votre école : " + n + ".";
            }
            case COUNT_TRAJETS_WITHOUT_BUS -> {
                int n = data.countTrajetsWithoutBus(ecoleId);
                yield n == 0
                        ? "Je n'ai trouvé aucun trajet sans bus dans votre école."
                        : "Trajets sans bus assigné dans votre école : " + n + ".";
            }
            case COUNT_BUSES_WITH_CHAUFFEUR -> {
                int n = data.countBusesWithChauffeur(ecoleId);
                yield "Bus avec chauffeur assigné : " + n + ".";
            }
            case COUNT_BUSES_WITHOUT_CHAUFFEUR -> {
                int n = data.countBusesWithoutChauffeur(ecoleId);
                yield "Bus sans chauffeur assigné : " + n + ".";
            }
            case COUNT_CHAUFFEURS -> {
                int n = data.countChauffeursForEcole(ecoleId);
                yield n == 0
                        ? "Je n'ai trouvé aucun chauffeur lié à votre école (bus ou candidatures)."
                        : "Vous avez " + n + " chauffeur(s) lié(s) à votre école (affectation bus et/ou candidatures).";
            }
            case COUNT_CHAUFFEURS_ASSIGNED -> {
                int n = data.countChauffeursAssignedToSchoolBuses(ecoleId);
                yield n == 0
                        ? "Je n'ai trouvé aucun chauffeur affecté à un bus de votre école."
                        : "Vous avez " + n + " chauffeur(s) affecté(s) à un bus de votre école.";
            }
            case COUNT_CHAUFFEURS_UNASSIGNED -> {
                int n = data.countChauffeursUnassignedSchoolBus(ecoleId);
                yield n == 0
                        ? "Je n'ai trouvé aucun chauffeur sans affectation bus dans votre périmètre (candidatures école)."
                        : "Chauffeurs avec candidature à votre école mais sans bus assigné sur vos bus actifs : " + n + ".";
            }
            case COUNT_ENFANTS_TRANSPORTED -> {
                int n = data.countEnfantsTransportedWithBus(ecoleId);
                yield n == 0
                        ? "Je n'ai trouvé aucun enfant actif sur un trajet avec bus assigné."
                        : "Enfants actifs transportés (trajet avec bus) : " + n + ".";
            }
            case COUNT_ENFANTS_ON_BOARD -> {
                int n = data.countEnfantsOnBoard(ecoleId);
                yield n == 0
                        ? "Je n'ai trouvé aucun enfant marqué « à bord » sur vos trajets."
                        : "Vous avez " + n + " enfant(s) à bord (marqués présents sur le bus).";
            }
            case COUNT_ENFANTS_OFF_BOARD -> {
                int n = data.countEnfantsOffBoard(ecoleId);
                yield n == 0
                        ? "Je n'ai trouvé aucun enfant marqué « pas à bord »."
                        : "Enfants actifs non à bord : " + n + ".";
            }
            case LIST_MAITRESSES -> formatList(
                    "Maîtresses de votre école :",
                    data.listMaitresses(ecoleId),
                    "Je n'ai trouvé aucune maîtresse dans votre école.");
            case LIST_BUSES -> formatList(
                    "Bus de votre école :",
                    data.listBuses(ecoleId),
                    "Je n'ai trouvé aucun bus actif pour votre école.");
            case LIST_TRAJETS -> formatList(
                    "Trajets de votre école :",
                    data.listTrajets(ecoleId),
                    "Je n'ai trouvé aucun trajet pour votre école.");
            case LIST_BUSES_WITHOUT_MAITRESSE -> formatList(
                    "Bus sans maîtresse :",
                    data.listBusesWithoutMaitresse(ecoleId),
                    "Je n'ai trouvé aucun bus sans maîtresse dans votre école (tous sont assignés ou aucun bus actif).");
            case LIST_TRAJETS_WITHOUT_BUS -> formatList(
                    "Trajets sans bus :",
                    data.listTrajetsWithoutBus(ecoleId),
                    "Je n'ai trouvé aucun trajet sans bus dans votre école.");
            case LIST_BUSES_ASSIGNED_CHAUFFEUR -> formatList(
                    "Bus avec chauffeur :",
                    data.listBusesAssignedChauffeur(ecoleId),
                    "Je n'ai trouvé aucun bus avec chauffeur assigné.");
            case LIST_BUSES_UNASSIGNED_CHAUFFEUR -> formatList(
                    "Bus sans chauffeur :",
                    data.listBusesUnassignedChauffeur(ecoleId),
                    "Je n'ai trouvé aucun bus sans chauffeur dans votre école (tous ont un chauffeur ou aucun bus actif).");
            case LIST_CHAUFFEURS -> formatList(
                    "Chauffeurs liés à votre école :",
                    data.listChauffeursForEcole(ecoleId),
                    "Je n'ai trouvé aucun chauffeur lié à votre école.");
            case LIST_CHAUFFEURS_ASSIGNED -> formatList(
                    "Chauffeurs affectés à un bus :",
                    data.listChauffeursAssigned(ecoleId),
                    "Je n'ai trouvé aucun chauffeur affecté à un bus de votre école.");
            case LIST_CHAUFFEURS_UNASSIGNED -> formatList(
                    "Chauffeurs sans bus sur votre école (candidature enregistrée) :",
                    data.listChauffeursUnassigned(ecoleId),
                    "Je n'ai trouvé aucun chauffeur sans affectation bus dans ce périmètre.");
            case LIST_ENFANTS -> formatList(
                    "Enfants de votre école :",
                    data.listEnfantsForEcole(ecoleId),
                    "Je n'ai trouvé aucun enfant actif rattaché à un trajet de votre école.");
            case LIST_ENFANTS_TRANSPORTED -> formatList(
                    "Enfants sur trajet avec bus :",
                    data.listEnfantsTransportedWithBus(ecoleId),
                    "Je n'ai trouvé aucun enfant actif sur un trajet avec bus assigné.");
            case LIST_ENFANTS_ON_BOARD -> formatList(
                    "Enfants à bord :",
                    data.listEnfantsOnBoard(ecoleId),
                    "Je n'ai trouvé aucun enfant marqué à bord.");
            case LIST_ENFANTS_OFF_BOARD -> formatList(
                    "Enfants non à bord :",
                    data.listEnfantsOffBoard(ecoleId),
                    "Je n'ai trouvé aucun enfant marqué non à bord.");
            case LIST_ENFANTS_ACCEPTED_CANDIDATURES -> formatList(
                    "Candidatures enfant acceptées :",
                    data.listAcceptedEnfantCandidatures(ecoleId),
                    "Je n'ai trouvé aucune candidature enfant acceptée pour votre école.");
            case LIST_ENFANTS_WITHOUT_TRAJET -> formatList(
                    "Enfants sans trajet rattaché à votre école :",
                    data.listEnfantsWithoutTrajetForEcole(ecoleId),
                    "Je n'ai trouvé aucun enfant actif sans trajet de votre école.");
            case LIST_ENFANTS_WITHOUT_BUS -> formatList(
                    "Enfants sur trajet sans bus :",
                    data.listEnfantsWithoutBus(ecoleId),
                    "Je n'ai trouvé aucun enfant sur un trajet sans bus assigné.");
            case LIST_ENFANTS_BY_BUS -> {
                String tok = d.busToken();
                if (tok == null || tok.isBlank()) {
                    yield "Pouvez-vous préciser le numéro ou l'identifiant du bus ?";
                }
                yield formatList(
                        "Enfants pour ce bus :",
                        data.listEnfantsByBus(ecoleId, tok),
                        "Je n'ai trouvé aucun enfant actif sur ce bus dans votre école.");
            }
            case LIST_ENFANTS_BY_TRAJET -> {
                String tok = d.busToken();
                if (tok == null || tok.isBlank()) {
                    yield "Pouvez-vous préciser le trajet (n° ou nom) ?";
                }
                yield formatList(
                        "Enfants sur ce trajet :",
                        data.listEnfantsByTrajet(ecoleId, tok),
                        "Je n'ai trouvé aucun enfant actif sur ce trajet dans votre école.");
            }
            case FIND_CHAUFFEUR_BY_BUS -> {
                String tok = d.busToken();
                if (tok == null || tok.isBlank()) {
                    yield "Pouvez-vous préciser le numéro ou l'identifiant du bus ?";
                }
                String ans = data.findChauffeurForBus(ecoleId, tok);
                yield nonBlankOr(ans, NO_RESULT);
            }
            case FIND_BUS_FOR_CHAUFFEUR -> {
                String q = d.nameQuery();
                if (q == null || q.isBlank()) {
                    yield "Pouvez-vous préciser le nom du chauffeur ?";
                }
                String ans = data.findBusForChauffeurName(ecoleId, q);
                yield nonBlankOr(ans, NO_RESULT);
            }
            case FIND_ENFANT -> {
                String q = d.nameQuery();
                if (q == null || q.isBlank()) {
                    yield "Pouvez-vous préciser le nom de l'enfant ?";
                }
                List<String> rows = data.searchEnfants(ecoleId, q);
                yield formatList("Résultats enfants :", rows,
                        "Je n'ai trouvé aucun enfant correspondant à ce nom dans votre école.");
            }
            case FIND_TRAJET_FOR_ENFANT -> {
                String q = d.nameQuery();
                if (q == null || q.isBlank()) {
                    yield "Pouvez-vous préciser le nom de l'enfant ?";
                }
                String ans = data.findTrajetForEnfantName(ecoleId, q);
                yield nonBlankOr(ans, NO_RESULT);
            }
            case FIND_BUS_FOR_ENFANT -> {
                String q = d.nameQuery();
                if (q == null || q.isBlank()) {
                    yield "Pouvez-vous préciser le nom de l'enfant ?";
                }
                String ans = data.findBusForEnfantName(ecoleId, q);
                yield nonBlankOr(ans, NO_RESULT);
            }
            case LIST_PENDING_ENFANT_CANDIDATURES -> formatList(
                    "Candidatures enfant en attente :",
                    data.listPendingEnfantCandidatures(ecoleId),
                    "Je n'ai trouvé aucune candidature enfant en attente.");
            case LIST_PENDING_CHAUFFEUR_CANDIDATURES -> formatList(
                    "Candidatures chauffeur en attente :",
                    data.listPendingChauffeurCandidatures(ecoleId),
                    "Je n'ai trouvé aucune candidature chauffeur en attente.");
            case LIST_PENDING_CANDIDATURES_BOTH -> {
                List<String> e = data.listPendingEnfantCandidatures(ecoleId);
                List<String> c = data.listPendingChauffeurCandidatures(ecoleId);
                if (e.isEmpty() && c.isEmpty()) {
                    yield "Je n'ai trouvé aucune candidature en attente pour votre école.";
                }
                StringBuilder sb = new StringBuilder("Candidatures en attente pour votre école :\n");
                sb.append("Enfant :\n");
                if (e.isEmpty()) {
                    sb.append("- (aucune)\n");
                } else {
                    for (String line : e) {
                        sb.append("- ").append(line).append("\n");
                    }
                }
                sb.append("Chauffeur :\n");
                if (c.isEmpty()) {
                    sb.append("- (aucune)\n");
                } else {
                    for (String line : c) {
                        sb.append("- ").append(line).append("\n");
                    }
                }
                yield sb.toString().trim();
            }
            case FIND_MAITRESSE_BY_BUS -> {
                String tok = d.busToken();
                if (tok == null || tok.isBlank()) {
                    yield "Pouvez-vous préciser le numéro ou l'identifiant du bus ?";
                }
                String ans = data.findMaitresseForBus(ecoleId, tok);
                yield nonBlankOr(ans, NO_RESULT);
            }
            case FIND_BUS_FOR_MAITRESSE -> {
                String q = d.nameQuery();
                if (q == null || q.isBlank()) {
                    yield "Pouvez-vous préciser le nom de la maîtresse ?";
                }
                String ans = data.findBusForMaitresseName(ecoleId, q);
                yield nonBlankOr(ans, NO_RESULT);
            }
            case SEARCH_MAITRESSE -> {
                String q = d.nameQuery();
                if (q == null || q.isBlank()) {
                    yield "Pouvez-vous préciser le nom à rechercher ?";
                }
                List<String> rows = data.searchMaitresses(ecoleId, q);
                yield formatList("Résultats maîtresses :", rows,
                        "Je n'ai trouvé aucune maîtresse correspondant à ce nom dans votre école.");
            }
            case SEARCH_BUS -> {
                String q = d.busToken();
                if (q == null || q.isBlank()) {
                    q = d.nameQuery();
                }
                if (q == null || q.isBlank()) {
                    yield "Pouvez-vous préciser le numéro ou la plaque du bus ?";
                }
                List<String> rows = data.searchBuses(ecoleId, q);
                yield formatList("Résultats bus :", rows,
                        "Je n'ai trouvé aucun bus correspondant dans votre école.");
            }
            case GENERAL_SCHOOL_INFO -> nonBlankOr(data.generalSchoolInfo(ecoleId), NO_RESULT);
            default -> "Cette question n'est pas encore prise en charge.";
        };
    }

    private static String formatList(String title, List<String> lines, String emptyMsg) {
        if (lines == null || lines.isEmpty()) {
            return emptyMsg;
        }
        StringBuilder sb = new StringBuilder(title).append("\n");
        for (String line : lines) {
            if (line != null && !line.isBlank()) {
                sb.append("- ").append(line.trim()).append("\n");
            }
        }
        String out = sb.toString().trim();
        return out.isBlank() ? emptyMsg : out;
    }
}
