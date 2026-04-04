package tn.esprit.workshop.services.leith;

import tn.esprit.workshop.model.leith.ReponseReclamation;
import tn.esprit.workshop.utilis.MyBDConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Accès JDBC à {@code reponse_reclamation}.
 */
public class ReponseReclamationService {

    private Connection getConnection() throws SQLException {
        return MyBDConnexion.getInstance().getConnection();
    }

    public void ajouter(ReponseReclamation reponse) throws SQLException {
        String sql = "INSERT INTO reponse_reclamation (reclamation_id, message, role_repondeur, user_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, reponse.getReclamationId());
            ps.setString(2, reponse.getMessage());
            ps.setString(3, reponse.getRoleRepondeur());
            ps.setInt(4, reponse.getUserId());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    reponse.setId(keys.getInt(1));
                }
            }
        }
    }

    /** Alias explicite (spécification module). */
    public List<ReponseReclamation> getReponsesByReclamationId(int reclamationId) throws SQLException {
        return getByReclamationId(reclamationId);
    }

    public List<ReponseReclamation> getByReclamationId(int reclamationId) throws SQLException {
        List<ReponseReclamation> list = new ArrayList<>();
        String sql = "SELECT id, reclamation_id, message, role_repondeur, user_id, date_reponse FROM reponse_reclamation "
                + "WHERE reclamation_id = ? ORDER BY date_reponse ASC";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, reclamationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToReponse(rs));
                }
            }
        }
        return list;
    }

    public ReponseReclamation mapResultSetToReponse(ResultSet rs) throws SQLException {
        ReponseReclamation r = new ReponseReclamation();
        r.setId(rs.getInt("id"));
        r.setReclamationId(rs.getInt("reclamation_id"));
        r.setMessage(rs.getString("message"));
        r.setRoleRepondeur(rs.getString("role_repondeur"));
        r.setUserId(rs.getInt("user_id"));
        r.setDateReponse(rs.getTimestamp("date_reponse"));
        return r;
    }
}
