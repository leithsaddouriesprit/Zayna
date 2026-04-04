package tn.esprit.workshop.services.leith;

import tn.esprit.workshop.model.leith.ReclamationHistorique;
import tn.esprit.workshop.utilis.MyBDConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Persistance de l’historique des actions sur les réclamations.
 */
public class ReclamationHistoriqueService {

    public static final String ACTION_CREATION = "CREATION";
    public static final String ACTION_REPONSE = "REPONSE";
    public static final String ACTION_CHANGEMENT_STATUT = "CHANGEMENT_STATUT";
    public static final String ACTION_ASSIGNATION = "ASSIGNATION";
    public static final String ACTION_LIBERATION_ASSIGNATION = "LIBERATION_ASSIGNATION";

    private Connection getConnection() throws SQLException {
        return MyBDConnexion.getInstance().getConnection();
    }

    public void enregistrer(
            int reclamationId,
            String actionType,
            String ancienStatut,
            String nouveauStatut,
            Integer ancienAssigne,
            Integer nouveauAssigne,
            String messageAction,
            int userIdAction,
            String roleAction) throws SQLException {
        String sql = "INSERT INTO reclamation_historique (reclamation_id, action_type, ancien_statut, nouveau_statut, "
                + "ancien_assigne, nouveau_assigne, message_action, user_id_action, role_action) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, reclamationId);
            ps.setString(2, actionType);
            ps.setString(3, ancienStatut);
            ps.setString(4, nouveauStatut);
            if (ancienAssigne == null) {
                ps.setNull(5, Types.INTEGER);
            } else {
                ps.setInt(5, ancienAssigne);
            }
            if (nouveauAssigne == null) {
                ps.setNull(6, Types.INTEGER);
            } else {
                ps.setInt(6, nouveauAssigne);
            }
            ps.setString(7, messageAction);
            ps.setInt(8, userIdAction);
            ps.setString(9, roleAction);
            ps.executeUpdate();
        }
    }

    public List<ReclamationHistorique> listByReclamationId(int reclamationId) throws SQLException {
        List<ReclamationHistorique> list = new ArrayList<>();
        String sql = "SELECT id, reclamation_id, action_type, ancien_statut, nouveau_statut, ancien_assigne, "
                + "nouveau_assigne, message_action, user_id_action, role_action, date_action "
                + "FROM reclamation_historique WHERE reclamation_id = ? ORDER BY date_action ASC, id ASC";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, reclamationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    private static ReclamationHistorique map(ResultSet rs) throws SQLException {
        ReclamationHistorique h = new ReclamationHistorique();
        h.setId(rs.getInt("id"));
        h.setReclamationId(rs.getInt("reclamation_id"));
        h.setActionType(rs.getString("action_type"));
        h.setAncienStatut(rs.getString("ancien_statut"));
        h.setNouveauStatut(rs.getString("nouveau_statut"));
        int aa = rs.getInt("ancien_assigne");
        h.setAncienAssigne(rs.wasNull() ? null : aa);
        int na = rs.getInt("nouveau_assigne");
        h.setNouveauAssigne(rs.wasNull() ? null : na);
        h.setMessageAction(rs.getString("message_action"));
        h.setUserIdAction(rs.getInt("user_id_action"));
        h.setRoleAction(rs.getString("role_action"));
        h.setDateAction(rs.getTimestamp("date_action"));
        return h;
    }
}
