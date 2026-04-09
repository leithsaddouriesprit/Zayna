package tn.esprit.workshop.ai.agent.action;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
        if (cid == null || trajetId == null || trajetId <= 0) {
            throw new AgentActionExecutionException("Cible ou trajet introuvable pour l’acceptation.");
        }
        try {
            jdbc.execute((ConnectionCallback<Void>) conn -> {
                approveEnfantInTransaction(conn, ecoleId, cid, trajetId);
                return null;
            });
        } catch (DataAccessException e) {
            Throwable c = e.getMostSpecificCause();
            throw new AgentActionExecutionException(
                    c != null && c.getMessage() != null ? c.getMessage() : e.getMessage());
        }
        return "✅ La candidature de " + p.enfantDisplayName() + " a été acceptée et l’enfant a été enregistré.";
    }

    /**
     * Même logique métier que {@code CandidatureEnfantService.accepter} (JavaFX) : UPDATE candidature + INSERT/UPDATE enfant.
     */
    private void approveEnfantInTransaction(Connection conn, int ecoleId, int candidatureId, int trajetId)
            throws SQLException {
        boolean prev = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);
            int trajOk;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM trajet WHERE id = ? AND id_ecole = ?")) {
                ps.setInt(1, trajetId);
                ps.setInt(2, ecoleId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Ce trajet n’appartient pas à votre école.");
                    }
                    trajOk = rs.getInt(1);
                }
            }
            if (trajOk != 1) {
                throw new SQLException("Ce trajet n’appartient pas à votre école.");
            }
            int parentId;
            String nom;
            String prenom;
            try (PreparedStatement ps = conn.prepareStatement(
                    """
                            SELECT parent_id, nom_enfant, prenom_enfant FROM candidature_enfant
                            WHERE id = ? AND id_ecole = ? AND statut = 'ENVOYEE'
                            """)) {
                ps.setInt(1, candidatureId);
                ps.setInt(2, ecoleId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException(
                                "Impossible d’approuver cette candidature enfant (déjà traitée ou hors périmètre).");
                    }
                    parentId = rs.getInt("parent_id");
                    nom = rs.getString("nom_enfant");
                    prenom = rs.getString("prenom_enfant");
                }
            }
            if (nom == null) {
                nom = "";
            }
            if (prenom == null) {
                prenom = "";
            }
            nom = nom.trim();
            prenom = prenom.trim();
            if (nom.isEmpty() || prenom.isEmpty()) {
                throw new SQLException("La candidature ne contient pas de nom ou prénom enfant valide.");
            }
            int u;
            try (PreparedStatement ps = conn.prepareStatement(
                    """
                            UPDATE candidature_enfant SET statut = 'ACCEPTEE', trajet_id = ?
                            WHERE id = ? AND id_ecole = ? AND statut = 'ENVOYEE'
                            """)) {
                ps.setInt(1, trajetId);
                ps.setInt(2, candidatureId);
                ps.setInt(3, ecoleId);
                u = ps.executeUpdate();
            }
            if (u != 1) {
                throw new SQLException(
                        "Impossible d’approuver cette candidature enfant (déjà traitée ou hors périmètre).");
            }
            Integer enfantId = null;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id FROM enfant WHERE parent_id = ? AND nom = ? AND prenom = ?")) {
                ps.setInt(1, parentId);
                ps.setString(2, nom);
                ps.setString(3, prenom);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        enfantId = rs.getInt("id");
                    }
                }
            }
            if (enfantId != null) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE enfant SET trajet_id = ?, actif = 1, on_board = 0 WHERE id = ?")) {
                    ps.setInt(1, trajetId);
                    ps.setInt(2, enfantId);
                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("Mise à jour de l’enfant impossible.");
                    }
                }
            } else {
                try (PreparedStatement ps = conn.prepareStatement(
                        """
                                INSERT INTO enfant (nom, prenom, parent_id, trajet_id, actif, on_board)
                                VALUES (?, ?, ?, ?, 1, 0)
                                """)) {
                    ps.setString(1, nom);
                    ps.setString(2, prenom);
                    ps.setInt(3, parentId);
                    ps.setInt(4, trajetId);
                    ps.executeUpdate();
                }
            }
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(prev);
        }
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
