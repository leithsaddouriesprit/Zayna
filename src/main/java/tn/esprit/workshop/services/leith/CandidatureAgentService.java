package tn.esprit.workshop.services.leith;

import tn.esprit.workshop.model.leith.CandidatureAgent;
import tn.esprit.workshop.model.leith.CandidatureAgentStatut;
import tn.esprit.workshop.utilis.MyBDConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CandidatureAgentService {

    private Connection getConnection() throws SQLException {
        return MyBDConnexion.getInstance().getConnection();
    }

    private static boolean isMissingTable(SQLException e) {
        return "42S02".equals(e.getSQLState())
                || (e.getMessage() != null && (e.getMessage().contains("doesn't exist")
                || e.getMessage().contains("n'existe pas") || e.getMessage().contains("Unknown table")));
    }

    public void insertPending(int userId, String nom, String prenom, int idEcole, double latitude, double longitude) throws SQLException {
        String sql = "INSERT INTO candidature_agent (user_id, nom, prenom, id_ecole, latitude, longitude, statut) VALUES (?,?,?,?,?,?, 'EN_ATTENTE')";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, nom != null ? nom.trim() : "");
            ps.setString(3, prenom != null ? prenom.trim() : "");
            ps.setInt(4, idEcole);
            ps.setDouble(5, latitude);
            ps.setDouble(6, longitude);
            ps.executeUpdate();
        }
    }

    public CandidatureAgent findByUserId(int userId) throws SQLException {
        String sql = """
                SELECT c.id, c.user_id, c.nom, c.prenom, c.id_ecole, c.latitude, c.longitude, c.statut, e.nom AS nom_ecole
                FROM candidature_agent c
                JOIN ecole e ON e.id = c.id_ecole
                WHERE c.user_id = ?
                """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return map(rs);
            }
        }
    }

    public boolean isAccessApproved(int userId) {
        try {
            CandidatureAgent c = findByUserId(userId);
            if (c == null) {
                return false;
            }
            return c.getStatut() == CandidatureAgentStatut.APPROUVEE;
        } catch (SQLException e) {
            if (isMissingTable(e)) {
                return true;
            }
            e.printStackTrace();
            return false;
        }
    }

    public void updateByUserId(int userId, String nom, String prenom, int idEcole, double latitude, double longitude) throws SQLException {
        updateByUserId(userId, nom, prenom, idEcole, latitude, longitude, null);
    }

    public void updateByUserId(int userId, String nom, String prenom, int idEcole, double latitude, double longitude, String nomEcole) throws SQLException {
        String sql = """
                UPDATE candidature_agent SET nom = ?, prenom = ?, id_ecole = ?, latitude = ?, longitude = ?,
                statut = CASE WHEN statut = 'REFUSEE' THEN 'EN_ATTENTE' ELSE statut END
                WHERE user_id = ?
                """;
        Connection conn = getConnection();
        boolean prev = conn.getAutoCommit();
        conn.setAutoCommit(false);
        try {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, nom != null ? nom.trim() : "");
                ps.setString(2, prenom != null ? prenom.trim() : "");
                ps.setInt(3, idEcole);
                ps.setDouble(4, latitude);
                ps.setDouble(5, longitude);
                ps.setInt(6, userId);
                ps.executeUpdate();
            }
            String sqlAgent = "UPDATE agent_ecole SET id_ecole = ?, nom = ?, prenom = ? WHERE user_id = ?";
            try (PreparedStatement psA = conn.prepareStatement(sqlAgent)) {
                psA.setInt(1, idEcole);
                psA.setString(2, nom != null ? nom.trim() : "");
                psA.setString(3, prenom != null ? prenom.trim() : "");
                psA.setInt(4, userId);
                psA.executeUpdate();
            }
            if (nomEcole != null) {
                String sqlEcole = "UPDATE ecole SET nom = ? WHERE id = ?";
                try (PreparedStatement psE = conn.prepareStatement(sqlEcole)) {
                    psE.setString(1, nomEcole.trim());
                    psE.setInt(2, idEcole);
                    psE.executeUpdate();
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

    public void deleteByUserId(int userId) throws SQLException {
        String sql = "DELETE FROM candidature_agent WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    public List<CandidatureAgent> findAllForAdmin() throws SQLException {
        List<CandidatureAgent> list = new ArrayList<>();
        String sql = """
                SELECT c.id, c.user_id, c.nom, c.prenom, c.id_ecole, c.latitude, c.longitude, c.statut, e.nom AS nom_ecole
                FROM candidature_agent c
                JOIN ecole e ON e.id = c.id_ecole
                ORDER BY c.statut, c.id DESC
                """;
        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public void setStatut(int candidatureId, CandidatureAgentStatut statut) throws SQLException {
        String sql = "UPDATE candidature_agent SET statut = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, statut.name());
            ps.setInt(2, candidatureId);
            ps.executeUpdate();
        }
    }

    public void deleteById(int candidatureId) throws SQLException {
        String sql = "DELETE FROM candidature_agent WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, candidatureId);
            ps.executeUpdate();
        }
    }

    public int countByStatut(CandidatureAgentStatut statut) throws SQLException {
        String sql = "SELECT COUNT(*) FROM candidature_agent WHERE statut = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, statut.name());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    private static CandidatureAgent map(ResultSet rs) throws SQLException {
        CandidatureAgent c = new CandidatureAgent();
        c.setId(rs.getInt("id"));
        c.setUserId(rs.getInt("user_id"));
        c.setNom(rs.getString("nom"));
        c.setPrenom(rs.getString("prenom"));
        c.setIdEcole(rs.getInt("id_ecole"));
        c.setLatitude(rs.getDouble("latitude"));
        c.setLongitude(rs.getDouble("longitude"));
        String s = rs.getString("statut");
        if (s != null) {
            c.setStatut(CandidatureAgentStatut.valueOf(s));
        }
        c.setNomEcole(rs.getString("nom_ecole"));
        return c;
    }
}
