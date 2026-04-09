package tn.esprit.workshop.ai.agent.action;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Exécution des actions Level 3 : requêtes paramétrées, contrôle périmètre école dans le WHERE.
 */
@Service
public class AgentChatActionExecutor {

    private final JdbcTemplate jdbc;

    public AgentChatActionExecutor(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public String execute(int ecoleId, AgentChatActionPayload p) throws AgentActionExecutionException {
        return switch (p.type()) {
            case APPROVE_CANDIDATURE_CHAUFFEUR -> approveChauffeur(ecoleId, p.candidatureChauffeurId(), p.chauffeurDisplayName());
            case REJECT_CANDIDATURE_CHAUFFEUR -> rejectChauffeur(ecoleId, p.candidatureChauffeurId(), p.chauffeurDisplayName());
            case APPROVE_CANDIDATURE_ENFANT -> approveEnfant(ecoleId, p);
            case REJECT_CANDIDATURE_ENFANT -> rejectEnfant(ecoleId, p.candidatureEnfantId(), p.enfantDisplayName());
            case ASSIGN_MAITRESSE_TO_BUS -> assignMaitresse(ecoleId, p);
            case UNASSIGN_MAITRESSE_FROM_BUS -> unassignMaitresse(ecoleId, p.maitresseId(), p.maitresseDisplayName());
        };
    }

    private String approveChauffeur(int ecoleId, Integer cid, String name) throws AgentActionExecutionException {
        if (cid == null) {
            throw new AgentActionExecutionException("Cible introuvable.");
        }
        int u = jdbc.update(
                "UPDATE candidature SET statut = 'ACCEPTEE' WHERE id = ? AND id_ecole = ? AND statut = 'ENVOYEE'",
                cid,
                ecoleId);
        if (u != 1) {
            throw new AgentActionExecutionException(
                    "Impossible d’approuver cette candidature (déjà traitée ou hors périmètre).");
        }
        return "La candidature chauffeur de « " + name + " » a été approuvée avec succès.";
    }

    private String rejectChauffeur(int ecoleId, Integer cid, String name) throws AgentActionExecutionException {
        if (cid == null) {
            throw new AgentActionExecutionException("Cible introuvable.");
        }
        int u = jdbc.update(
                "UPDATE candidature SET statut = 'REFUSEE' WHERE id = ? AND id_ecole = ? AND statut = 'ENVOYEE'",
                cid,
                ecoleId);
        if (u != 1) {
            throw new AgentActionExecutionException(
                    "Impossible de refuser cette candidature (déjà traitée ou hors périmètre).");
        }
        return "La candidature chauffeur de « " + name + " » a été refusée avec succès.";
    }

    private String approveEnfant(int ecoleId, AgentChatActionPayload p) throws AgentActionExecutionException {
        Integer cid = p.candidatureEnfantId();
        Integer trajetId = p.trajetIdForEnfantAccept();
        if (cid == null || trajetId == null) {
            throw new AgentActionExecutionException("Cible ou trajet introuvable pour l’acceptation.");
        }
        Integer ok = jdbc.queryForObject(
                "SELECT COUNT(*) FROM trajet WHERE id = ? AND id_ecole = ?",
                Integer.class,
                trajetId,
                ecoleId);
        if (ok == null || ok == 0) {
            throw new AgentActionExecutionException("Ce trajet n’appartient pas à votre école.");
        }
        int u = jdbc.update(
                """
                        UPDATE candidature_enfant SET statut = 'ACCEPTEE', trajet_id = ?
                        WHERE id = ? AND id_ecole = ? AND statut = 'ENVOYEE'
                        """,
                trajetId,
                cid,
                ecoleId);
        if (u != 1) {
            throw new AgentActionExecutionException(
                    "Impossible d’approuver cette candidature enfant (déjà traitée ou hors périmètre).");
        }
        return "✅ La candidature de " + p.enfantDisplayName() + " a été acceptée.";
    }

    private String rejectEnfant(int ecoleId, Integer cid, String name) throws AgentActionExecutionException {
        if (cid == null) {
            throw new AgentActionExecutionException("Cible introuvable.");
        }
        int u = jdbc.update(
                "UPDATE candidature_enfant SET statut = 'REFUSEE' WHERE id = ? AND id_ecole = ? AND statut = 'ENVOYEE'",
                cid,
                ecoleId);
        if (u != 1) {
            throw new AgentActionExecutionException(
                    "Impossible de refuser cette candidature (déjà traitée ou hors périmètre).");
        }
        return "La candidature enfant de « " + name + " » a été refusée avec succès.";
    }

    private String assignMaitresse(int ecoleId, AgentChatActionPayload p) throws AgentActionExecutionException {
        Integer mid = p.maitresseId();
        Integer bid = p.busId();
        if (mid == null || bid == null) {
            throw new AgentActionExecutionException("Maîtresse ou bus introuvable.");
        }
        Integer busOk = jdbc.queryForObject(
                "SELECT COUNT(*) FROM bus WHERE id = ? AND id_ecole = ? AND actif = 1",
                Integer.class,
                bid,
                ecoleId);
        if (busOk == null || busOk == 0) {
            throw new AgentActionExecutionException("Je n’ai trouvé aucun bus correspondant dans votre école.");
        }
        Integer other = jdbc.queryForObject(
                """
                        SELECT COUNT(*) FROM maitresse WHERE id_ecole = ? AND id_bus = ? AND id <> ?
                        """,
                Integer.class,
                ecoleId,
                bid,
                mid);
        if (other != null && other > 0) {
            throw new AgentActionExecutionException(
                    "Une maîtresse est déjà affectée à ce bus. Retirez d’abord l’affectation existante.");
        }
        int u = jdbc.update(
                "UPDATE maitresse SET id_bus = ? WHERE id = ? AND id_ecole = ?",
                bid,
                mid,
                ecoleId);
        if (u != 1) {
            throw new AgentActionExecutionException("Je n’ai pas trouvé de maîtresse correspondant à ce nom dans votre périmètre.");
        }
        return "La maîtresse « " + p.maitresseDisplayName() + " » a été affectée au " + p.busDisplayLabel() + ".";
    }

    private String unassignMaitresse(int ecoleId, Integer mid, String name) throws AgentActionExecutionException {
        if (mid == null) {
            throw new AgentActionExecutionException("Cible introuvable.");
        }
        int u = jdbc.update(
                "UPDATE maitresse SET id_bus = NULL WHERE id = ? AND id_ecole = ?",
                mid,
                ecoleId);
        if (u != 1) {
            throw new AgentActionExecutionException("Je n’ai pas trouvé de maîtresse correspondant à ce nom dans votre périmètre.");
        }
        return "L’affectation de la maîtresse « " + name + " » a été retirée.";
    }
}
