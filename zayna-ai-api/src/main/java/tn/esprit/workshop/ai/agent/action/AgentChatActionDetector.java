package tn.esprit.workshop.ai.agent.action;

import org.springframework.stereotype.Component;
import tn.esprit.workshop.ai.agent.AgentChatDataService;
import tn.esprit.workshop.ai.agent.AgentChatTextNormalizer;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Détection des commandes d’action (Level 3), séparée des intents de lecture.
 */
@Component
public class AgentChatActionDetector {

    public static final String MSG_ACTION_FORBIDDEN = "Cette action n'est pas autorisée depuis l'assistant agent.";

    public sealed interface ActionParseResult permits ActionParseResult.NoAction, ActionParseResult.Clarify, ActionParseResult.Proposal {
        record NoAction() implements ActionParseResult {
            static final NoAction INSTANCE = new NoAction();
        }

        record Clarify(String message) implements ActionParseResult {
        }

        record Proposal(AgentChatActionPayload payload) implements ActionParseResult {
        }
    }

    public Optional<String> forbiddenReason(String normalized) {
        if (normalized == null || normalized.isBlank()) {
            return Optional.empty();
        }
        String n = normalized;
        if (Pattern.compile("(?i)\\b(supprimer|effacer|delete|destroy)\\b").matcher(n).find()) {
            return Optional.of(MSG_ACTION_FORBIDDEN);
        }
        if (Pattern.compile("(?i)supprimer\\s+(un\\s+)?(enfant|bus|trajet|maitresse|candidature)").matcher(n).find()) {
            return Optional.of(MSG_ACTION_FORBIDDEN);
        }
        return Optional.empty();
    }

    private static boolean hasActionVerb(String n) {
        return Pattern.compile(
                "(?i)\\b(approuve|approuver|accepte|accepter|valider|valide|refuse|refuser|rejette|rejeter|rejete|"
                        + "affecte|affecter|desaffecte|désaffecte|retire|enlever|change\\s+le\\s+bus|deplace|déplace|mets|mettre)\\b")
                .matcher(n).find();
    }

    public ActionParseResult parse(String raw, int ecoleId, AgentChatDataService data) {
        if (raw == null || raw.isBlank()) {
            return ActionParseResult.NoAction.INSTANCE;
        }
        String n = AgentChatTextNormalizer.forMatching(raw);
        if (!hasActionVerb(n)) {
            return ActionParseResult.NoAction.INSTANCE;
        }

        Optional<String> forb = forbiddenReason(n);
        if (forb.isPresent()) {
            return new ActionParseResult.Clarify(forb.get());
        }

        // --- Retrait affectation maîtresse (avant affectation / refus) ---
        ActionParseResult un = tryUnassignMaitresse(raw, n, ecoleId, data);
        if (!(un instanceof ActionParseResult.NoAction)) {
            return un;
        }

        // --- Candidatures (ordre important) ---
        if (n.contains("candidature")) {
            // Si le rôle est explicitement chauffeur, on force le flux chauffeur.
            if (n.contains("chauffeur") && !n.contains("enfant")) {
                ActionParseResult cr = tryChauffeurCand(raw, n, ecoleId, data);
                if (!(cr instanceof ActionParseResult.NoAction)) {
                    return cr;
                }
            } else {
                // Par défaut sur "candidature" sans rôle explicite, on tente enfant.
                ActionParseResult er = tryEnfantCand(raw, n, ecoleId, data);
                if (!(er instanceof ActionParseResult.NoAction)) {
                    return er;
                }
                ActionParseResult cr = tryChauffeurCand(raw, n, ecoleId, data);
                if (!(cr instanceof ActionParseResult.NoAction)) {
                    return cr;
                }
            }
        } else {
            // Compatibilité historique.
            if (isChauffeurCandidatureContext(n) && !n.contains("enfant")) {
                ActionParseResult cr = tryChauffeurCand(raw, n, ecoleId, data);
                if (!(cr instanceof ActionParseResult.NoAction)) {
                    return cr;
                }
            }
            if (isEnfantCandidatureContext(n)) {
                ActionParseResult er = tryEnfantCand(raw, n, ecoleId, data);
                if (!(er instanceof ActionParseResult.NoAction)) {
                    return er;
                }
            }
        }

        // --- Affectation maîtresse → bus ---
        return tryAssignMaitresse(raw, ecoleId, data);
    }

    private static boolean isChauffeurCandidatureContext(String n) {
        return n.contains("chauffeur") || (n.contains("candidature") && !n.contains("enfant"));
    }

    private static boolean isEnfantCandidatureContext(String n) {
        return n.contains("enfant") || (n.contains("candidature") && n.contains("enfant"));
    }

    private static boolean isApproveVerb(String n) {
        return Pattern.compile("(?i)\\b(approuve|approuver|accepte|accepter|valider|valide)\\b").matcher(n).find();
    }

    private static boolean isRejectVerb(String n) {
        return Pattern.compile("(?i)\\b(refuse|refuser|rejette|rejeter|rejete)\\b").matcher(n).find();
    }

    private ActionParseResult tryUnassignMaitresse(String raw, String n, int ecoleId, AgentChatDataService data) {
        if (!Pattern.compile("(?i)\\b(désaffecte|desaffecte|retire|enlever)\\b").matcher(n).find()) {
            return ActionParseResult.NoAction.INSTANCE;
        }
        if (n.contains("chauffeur") || n.contains("candidature") || n.contains("enfant")) {
            return ActionParseResult.NoAction.INSTANCE;
        }
        String name = null;
        Matcher m = Pattern.compile("(?i)retire\\s+(?:le\\s+)?bus\\s+de\\s+(.+)").matcher(raw.trim());
        if (m.find()) {
            name = cleanName(m.group(1));
        }
        if (name == null || name.isBlank()) {
            m = Pattern.compile("(?i)(?:désaffecte|desaffecte)\\s+(.+)").matcher(raw.trim());
            if (m.find()) {
                name = cleanName(m.group(1));
            }
        }
        if (name == null || name.isBlank()) {
            m = Pattern.compile("(?i)enlever\\s+l['’]?\\s*affectation\\s+(?:de\\s+)?(.+)").matcher(raw.trim());
            if (m.find()) {
                name = cleanName(m.group(1));
            }
        }
        if (name == null || name.isBlank()) {
            return ActionParseResult.NoAction.INSTANCE;
        }
        return resolveSingleMaitresse(name, ecoleId, data, true);
    }

    private ActionParseResult resolveSingleMaitresse(String name, int ecoleId, AgentChatDataService data, boolean unassign) {
        List<AgentChatDataService.ResolvedMaitresse> list = data.findMaitressesByNameForEcole(ecoleId, name);
        if (list.isEmpty()) {
            return new ActionParseResult.Clarify("Je n'ai pas trouvé de maîtresse correspondant à ce nom.");
        }
        if (list.size() > 1) {
            return new ActionParseResult.Clarify("Plusieurs maîtresses correspondent. Précisez le nom : "
                    + String.join(" | ", list.stream().map(AgentChatDataService.ResolvedMaitresse::label).toList()));
        }
        var one = list.get(0);
        if (unassign) {
            return new ActionParseResult.Proposal(AgentChatActionPayload.unassignMaitresse(one.id(), one.label()));
        }
        return ActionParseResult.NoAction.INSTANCE;
    }

    private ActionParseResult tryChauffeurCand(String raw, String n, int ecoleId, AgentChatDataService data) {
        if (!isApproveVerb(n) && !isRejectVerb(n)) {
            return ActionParseResult.NoAction.INSTANCE;
        }
        String name = extractCandidaturePersonName(raw, n, "chauffeur");
        if (name == null || name.isBlank()) {
            return new ActionParseResult.Clarify(
                    "Je n'ai pas identifié le nom du chauffeur. Exemple : approuver la candidature chauffeur de Jean Dupont.");
        }
        List<AgentChatDataService.ResolvedChauffeurCand> list = data.findPendingChauffeurCandidaturesByName(ecoleId, name);
        if (list.isEmpty()) {
            return new ActionParseResult.Clarify("Je n'ai trouvé aucune candidature chauffeur correspondante.");
        }
        if (list.size() > 1) {
            return new ActionParseResult.Clarify("Plusieurs candidatures correspondent. Précisez : "
                    + String.join(" | ", list.stream().map(AgentChatDataService.ResolvedChauffeurCand::label).toList()));
        }
        var one = list.get(0);
        if (isApproveVerb(n)) {
            return new ActionParseResult.Proposal(AgentChatActionPayload.approveChauffeur(one.id(), one.label()));
        }
        if (isRejectVerb(n)) {
            return new ActionParseResult.Proposal(AgentChatActionPayload.rejectChauffeur(one.id(), one.label()));
        }
        return ActionParseResult.NoAction.INSTANCE;
    }

    private ActionParseResult tryEnfantCand(String raw, String n, int ecoleId, AgentChatDataService data) {
        if (!isApproveVerb(n) && !isRejectVerb(n)) {
            return ActionParseResult.NoAction.INSTANCE;
        }
        Integer candidatureId = parseCandidatureId(raw);
        if (candidatureId != null) {
            AgentChatDataService.ResolvedEnfantCand one = data.findPendingEnfantCandidatureById(ecoleId, candidatureId);
            if (one == null) {
                return new ActionParseResult.Clarify("Je n'ai trouvé aucune candidature enfant en attente avec cet identifiant.");
            }
            if (isRejectVerb(n)) {
                return new ActionParseResult.Proposal(AgentChatActionPayload.rejectEnfant(one.id(), one.label()));
            }
            Integer trajetFromMsg = parseTrajetId(raw);
            Integer trajetFromRow = data.getEnfantCandidatureTrajetId(one.id(), ecoleId);
            Integer trajet = trajetFromMsg != null ? trajetFromMsg : trajetFromRow;
            if (trajet == null) {
                return new ActionParseResult.Clarify(
                        "Pour accepter cette candidature enfant, indiquez le numéro de trajet de votre école "
                                + "(ex. « accepter la candidature 5 sur le trajet 3 »).");
            }
            if (!data.trajetBelongsToEcole(trajet, ecoleId)) {
                return new ActionParseResult.Clarify("Ce trajet n'appartient pas à votre école.");
            }
            return new ActionParseResult.Proposal(AgentChatActionPayload.approveEnfant(one.id(), one.label(), trajet));
        }
        String name = extractCandidaturePersonName(raw, n, "enfant");
        if (name == null || name.isBlank()) {
            name = extractNameAfterCandidature(raw);
        }
        if (name == null || name.isBlank()) {
            return new ActionParseResult.Clarify(
                    "Je n'ai pas identifié le nom de l'enfant. Exemple : accepter la candidature enfant de Sara Ben Ali.");
        }
        List<AgentChatDataService.ResolvedEnfantCand> list = data.findPendingEnfantCandidaturesByName(ecoleId, name);
        if (list.isEmpty()) {
            return new ActionParseResult.Clarify("Je n'ai trouvé aucune candidature enfant correspondante.");
        }
        if (list.size() > 1) {
            return new ActionParseResult.Clarify(
                    "Plusieurs candidatures enfant correspondent à ce nom. Veuillez préciser : "
                            + String.join(" | ", list.stream().map(AgentChatDataService.ResolvedEnfantCand::label).toList()));
        }
        var one = list.get(0);
        if (isRejectVerb(n)) {
            return new ActionParseResult.Proposal(AgentChatActionPayload.rejectEnfant(one.id(), one.label()));
        }
        Integer trajetFromMsg = parseTrajetId(raw);
        Integer trajetFromRow = data.getEnfantCandidatureTrajetId(one.id(), ecoleId);
        Integer trajet = trajetFromMsg != null ? trajetFromMsg : trajetFromRow;
        if (trajet == null) {
            return new ActionParseResult.Clarify(
                    "Pour accepter cette candidature enfant, indiquez le numéro de trajet de votre école "
                            + "(ex. « accepter la candidature enfant de X sur le trajet 3 »).");
        }
        if (!data.trajetBelongsToEcole(trajet, ecoleId)) {
            return new ActionParseResult.Clarify("Ce trajet n'appartient pas à votre école.");
        }
        return new ActionParseResult.Proposal(AgentChatActionPayload.approveEnfant(one.id(), one.label(), trajet));
    }

    private static Integer parseCandidatureId(String raw) {
        Matcher m = Pattern.compile("(?i)candidature\\s*(?:enfant\\s*)?(?:#|id\\s*)?\\s*([0-9]+)").matcher(raw);
        if (m.find()) {
            try {
                return Integer.parseInt(m.group(1).trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private static String extractNameAfterCandidature(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        Matcher m = Pattern.compile("(?i)candidature\\s+(?:enfant\\s+)?(.+)").matcher(raw.trim());
        if (!m.find()) {
            return null;
        }
        String tail = cleanName(m.group(1));
        tail = tail.replaceAll("(?i)^(de|du|d')\\s+", "").trim();
        if (tail.matches("^[0-9]+$")) {
            return null;
        }
        return tail.isBlank() ? null : tail;
    }

    private ActionParseResult tryAssignMaitresse(String raw, int ecoleId, AgentChatDataService data) {
        if (!Pattern.compile("(?i)\\b(affecte|affecter|mets|mettre|change\\s+le\\s+bus|deplace|déplace)\\b")
                .matcher(AgentChatTextNormalizer.forMatching(raw)).find()) {
            return ActionParseResult.NoAction.INSTANCE;
        }
        String mName = null;
        String busTok = null;
        Matcher m = Pattern.compile(
                "(?i)(?:affecte|affecter|mets|mettre)\\s+(.+?)\\s+(?:au|à)\\s+bus\\s*(?:n°|#|num(?:e|é)ro)?\\s*([A-Za-z0-9\\-]+)")
                .matcher(raw.trim());
        if (m.find()) {
            mName = cleanName(m.group(1));
            busTok = m.group(2).trim();
        }
        if (mName == null) {
            m = Pattern.compile(
                    "(?i)change\\s+le\\s+bus\\s+de\\s+(.+?)\\s+vers\\s+(?:le\\s+)?bus\\s*(?:n°|#|num(?:e|é)ro)?\\s*([A-Za-z0-9\\-]+)")
                    .matcher(raw.trim());
            if (m.find()) {
                mName = cleanName(m.group(1));
                busTok = m.group(2).trim();
            }
        }
        if (mName == null) {
            m = Pattern.compile(
                    "(?i)(?:déplace|deplace)\\s+(.+?)\\s+vers\\s+(?:le\\s+)?bus\\s*(?:n°|#|num(?:e|é)ro)?\\s*([A-Za-z0-9\\-]+)")
                    .matcher(raw.trim());
            if (m.find()) {
                mName = cleanName(m.group(1));
                busTok = m.group(2).trim();
            }
        }
        if (mName == null || mName.isBlank() || busTok == null || busTok.isBlank()) {
            return ActionParseResult.NoAction.INSTANCE;
        }
        String normalizedName = AgentChatTextNormalizer.forMatching(mName);
        if (normalizedName.equals("maitresse")
                || normalizedName.equals("la maitresse")
                || normalizedName.equals("une maitresse")
                || normalizedName.equals("les maitresses")) {
            return new ActionParseResult.Clarify(
                    "Pour affecter une maîtresse, précisez son nom (ex. « affecter [Prénom Nom] au bus 12 »).");
        }
        List<AgentChatDataService.ResolvedMaitresse> mlist = data.findMaitressesByNameForEcole(ecoleId, mName);
        if (mlist.isEmpty()) {
            return new ActionParseResult.Clarify("Je n'ai pas trouvé de maîtresse correspondant à ce nom.");
        }
        if (mlist.size() > 1) {
            return new ActionParseResult.Clarify("Plusieurs maîtresses correspondent. Précisez : "
                    + String.join(" | ", mlist.stream().map(AgentChatDataService.ResolvedMaitresse::label).toList()));
        }
        List<AgentChatDataService.ResolvedBus> blist = data.resolveBusesInEcole(ecoleId, busTok);
        if (blist.isEmpty()) {
            return new ActionParseResult.Clarify("Je n'ai trouvé aucun bus correspondant.");
        }
        if (blist.size() > 1) {
            return new ActionParseResult.Clarify("Plusieurs bus correspondent. Précisez le numéro ou l'identifiant : "
                    + String.join(" | ", blist.stream().map(AgentChatDataService.ResolvedBus::label).toList()));
        }
        var ms = mlist.get(0);
        var bs = blist.get(0);
        return new ActionParseResult.Proposal(
                AgentChatActionPayload.assignMaitresse(ms.id(), ms.label(), bs.id(), bs.label()));
    }

    private static Integer parseTrajetId(String raw) {
        Matcher m = Pattern.compile("(?i)trajet\\s*(?:n°|#|num(?:e|é)ro)?\\s*([0-9]+)").matcher(raw);
        if (m.find()) {
            try {
                return Integer.parseInt(m.group(1).trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private static String extractCandidaturePersonName(String raw, String n, String roleHint) {
        Matcher m = Pattern.compile("(?i)(?:de|du)\\s+([\\p{L}0-9'’\\-\\s]{2,60})(?:\\s*[?.!])?$").matcher(raw.trim());
        if (m.find()) {
            return cleanName(m.group(1));
        }
        m = Pattern.compile("(?i)(?:de|du)\\s+([\\p{L}0-9'’\\-\\s]{2,60})").matcher(raw);
        String last = null;
        while (m.find()) {
            last = cleanName(m.group(1));
        }
        if (last != null && !last.isBlank()) {
            return last;
        }
        if ("chauffeur".equals(roleHint)) {
            m = Pattern.compile("(?i)chauffeur\\s+([\\p{L}0-9'’\\-\\s]{2,50})").matcher(raw);
            if (m.find()) {
                return cleanName(m.group(1));
            }
            m = Pattern.compile("(?i)valider?\\s+chauffeur\\s+([\\p{L}0-9'’\\-\\s]{2,50})").matcher(raw);
            if (m.find()) {
                return cleanName(m.group(1));
            }
        }
        if ("enfant".equals(roleHint)) {
            m = Pattern.compile("(?i)enfant\\s+([\\p{L}0-9'’\\-\\s]{2,50})").matcher(raw);
            if (m.find()) {
                return cleanName(m.group(1));
            }
            m = Pattern.compile("(?i)valider?\\s+enfant\\s+([\\p{L}0-9'’\\-\\s]{2,50})").matcher(raw);
            if (m.find()) {
                return cleanName(m.group(1));
            }
        }
        return null;
    }

    private static String cleanName(String s) {
        if (s == null) {
            return "";
        }
        String t = s.trim().replaceAll("[?!.:;]+$", "").trim();
        t = t.replaceAll("(?i)^(la|le|les|un|une)\\s+", "").trim();
        if (t.length() > 80) {
            t = t.substring(0, 80).trim();
        }
        return t;
    }

    public static String buildProposalText(AgentChatActionPayload p) {
        return switch (p.type()) {
            case APPROVE_CANDIDATURE_CHAUFFEUR -> "Action détectée : approuver la candidature chauffeur de « "
                    + p.chauffeurDisplayName() + " ».\nVoulez-vous confirmer ?";
            case REJECT_CANDIDATURE_CHAUFFEUR -> "Action détectée : refuser la candidature chauffeur de « "
                    + p.chauffeurDisplayName() + " ».\nVoulez-vous confirmer ?";
            case APPROVE_CANDIDATURE_ENFANT -> "Action détectée : approuver la candidature enfant de « "
                    + p.enfantDisplayName() + " » (trajet #" + p.trajetIdForEnfantAccept() + ").\nVoulez-vous confirmer ?";
            case REJECT_CANDIDATURE_ENFANT -> "Action détectée : refuser la candidature enfant de « "
                    + p.enfantDisplayName() + " ».\nVoulez-vous confirmer ?";
            case ASSIGN_MAITRESSE_TO_BUS -> "Action détectée : affecter la maîtresse « " + p.maitresseDisplayName()
                    + " » au " + p.busDisplayLabel() + ".\nVoulez-vous confirmer ?";
            case UNASSIGN_MAITRESSE_FROM_BUS -> "Action détectée : retirer l'affectation bus de la maîtresse « "
                    + p.maitresseDisplayName() + " ».\nVoulez-vous confirmer ?";
        };
    }
}
