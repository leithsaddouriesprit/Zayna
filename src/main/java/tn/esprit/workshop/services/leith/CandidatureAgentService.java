package tn.esprit.workshop.services.leith;

import tn.esprit.workshop.model.Talel.Dao.DaoUser;
import tn.esprit.workshop.model.leith.CandidatureAgent;
import tn.esprit.workshop.model.leith.CandidatureAgentStatut;
import tn.esprit.workshop.utilis.MyBDConnexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CandidatureAgentService {

    private Connection getConnection() throws SQLException {
        return MyBDConnexion.getInstance().getConnection();
    }

    public static boolean isMissingTable(SQLException e) {
        return "42S02".equals(e.getSQLState())
                || (e.getMessage() != null && (e.getMessage().contains("doesn't exist")
                || e.getMessage().contains("n'existe pas") || e.getMessage().contains("Unknown table")));
    }

    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final String SELECT_BASE =
            "SELECT c.id, c.user_id, c.nom, c.prenom, c.ecole, c.adresse, c.latitude, c.longitude, c.statut, "
                    + "c.created_at, c.updated_at "
                    + "FROM candidature_agent c ";

    public CandidatureAgent findByUserId(int userId) throws SQLException {
        String sql = SELECT_BASE + " WHERE c.user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return mapRow(rs);
            }
        }
    }

    public CandidatureAgent findById(int candidatureId) throws SQLException {
        String sql = SELECT_BASE + " WHERE c.id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, candidatureId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return mapRow(rs);
            }
        }
    }

    public boolean isAccessApproved(int userId) {
        try {
            CandidatureAgent c = findByUserId(userId);
            if (c == null) {
                return true;
            }
            return c.getStatut() == CandidatureAgentStatut.APPROUVEE;
        } catch (SQLException e) {
            return isMissingTable(e);
        }
    }

    /** Met à jour uniquement {@code candidature_agent}. */
    public void updateCandidature(int userId, String nom, String prenom, String ecole,
                                  String adresse, double latitude, double longitude) throws SQLException {
        String sql = "UPDATE candidature_agent SET nom = ?, prenom = ?, ecole = ?, adresse = ?, "
                + "latitude = ?, longitude = ?, "
                + "statut = CASE WHEN statut = 'REFUSEE' THEN 'EN_ATTENTE' ELSE statut END "
                + "WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nom != null ? nom.trim() : "");
            ps.setString(2, prenom != null ? prenom.trim() : "");
            ps.setString(3, ecole != null ? ecole.trim() : "");
            ps.setString(4, adresse != null ? adresse.trim() : "");
            ps.setDouble(5, latitude);
            ps.setDouble(6, longitude);
            ps.setInt(7, userId);
            ps.executeUpdate();
        }
    }

    /**
     * Supprime d’abord la candidature, puis le compte utilisateur.
     * Aucun impact sur {@code ecole} / {@code agent_ecole} (inexistants dans ce flux avant approbation).
     */
    public void deleteCandidatureAndUser(int userId) throws SQLException {
        try (Connection conn = getConnection()) {
            try (PreparedStatement d1 = conn.prepareStatement(
                    "DELETE FROM candidature_agent WHERE user_id = ?")) {
                d1.setInt(1, userId);
                d1.executeUpdate();
            }
            new DaoUser().deleteUser(userId);
        }
    }

    public List<CandidatureAgent> findAllForAdmin() throws SQLException {
        List<CandidatureAgent> list = new ArrayList<>();
        String sql = SELECT_BASE + " ORDER BY c.statut, c.id DESC";
        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
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

    /**
     * Approbation : INSERT {@code ecole} depuis la candidature, puis {@code agent_ecole}, puis statut {@code APPROUVEE}.
     */
    public void approveAndProvision(int candidatureId) throws SQLException {
        Connection conn = getConnection();
        boolean prev = conn.getAutoCommit();
        conn.setAutoCommit(false);
        try {
            CandidatureAgent c = findByIdInConnection(conn, candidatureId);
            if (c == null) {
                throw new SQLException("Candidature introuvable.");
            }
            if (c.getStatut() == CandidatureAgentStatut.APPROUVEE) {
                conn.commit();
                return;
            }
            String nomE = c.getEcole() != null ? c.getEcole().trim() : "";
            if (nomE.isEmpty()) {
                throw new SQLException("Candidature sans nom d'établissement (ecole).");
            }
            String adr = c.getAdresse() != null ? c.getAdresse().trim() : "";
            int idEcole = insertEcole(conn, nomE, adr, c.getLatitude(), c.getLongitude());
            ensureAgentEcoleRow(conn, c.getUserId(), idEcole, c.getNom(), c.getPrenom());
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE candidature_agent SET statut = 'APPROUVEE' WHERE id = ?")) {
                ps.setInt(1, candidatureId);
                ps.executeUpdate();
            }
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(prev);
        }
    }

    private CandidatureAgent findByIdInConnection(Connection conn, int candidatureId) throws SQLException {
        String sql = SELECT_BASE + " WHERE c.id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, candidatureId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return mapRow(rs);
            }
        }
    }

    private static int insertEcole(Connection conn, String nom, String adresse, double lat, double lon) throws SQLException {
        String sql = "INSERT INTO ecole (nom, adresse, latitude, longitude) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nom);
            ps.setString(2, adresse != null && !adresse.isEmpty() ? adresse : null);
            ps.setDouble(3, lat);
            ps.setDouble(4, lon);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("École : aucun id généré.");
                }
                return keys.getInt(1);
            }
        }
    }

    private static void ensureAgentEcoleRow(Connection conn, int userId, int idEcole, String nom, String prenom) throws SQLException {
        try (PreparedStatement check = conn.prepareStatement("SELECT id FROM agent_ecole WHERE user_id = ?")) {
            check.setInt(1, userId);
            try (ResultSet rs = check.executeQuery()) {
                if (rs.next()) {
                    int rowId = rs.getInt(1);
                    try (PreparedStatement up = conn.prepareStatement(
                            "UPDATE agent_ecole SET id_ecole = ?, nom = ?, prenom = ? WHERE id = ?")) {
                        up.setInt(1, idEcole);
                        up.setString(2, nom != null ? nom.trim() : "");
                        up.setString(3, prenom != null ? prenom.trim() : "");
                        up.setInt(4, rowId);
                        up.executeUpdate();
                    }
                    return;
                }
            }
        }
        try (PreparedStatement ins = conn.prepareStatement(
                "INSERT INTO agent_ecole (user_id, id_ecole, nom, prenom) VALUES (?,?,?,?)")) {
            ins.setInt(1, userId);
            ins.setInt(2, idEcole);
            ins.setString(3, nom != null ? nom.trim() : "");
            ins.setString(4, prenom != null ? prenom.trim() : "");
            ins.executeUpdate();
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

    private static CandidatureAgent mapRow(ResultSet rs) throws SQLException {
        CandidatureAgent c = new CandidatureAgent();
        c.setId(rs.getInt("id"));
        c.setUserId(rs.getInt("user_id"));
        c.setNom(rs.getString("nom"));
        c.setPrenom(rs.getString("prenom"));
        c.setEcole(rs.getString("ecole"));
        c.setAdresse(rs.getString("adresse"));
        c.setLatitude(rs.getDouble("latitude"));
        c.setLongitude(rs.getDouble("longitude"));
        String s = rs.getString("statut");
        if (s != null) {
            c.setStatut(CandidatureAgentStatut.valueOf(s));
        }
        Timestamp ca = rs.getTimestamp("created_at");
        Timestamp ua = rs.getTimestamp("updated_at");
        c.setCreatedAt(ca == null ? "—" : ca.toLocalDateTime().format(TS_FMT));
        c.setUpdatedAt(ua == null ? "—" : ua.toLocalDateTime().format(TS_FMT));
        return c;
    }
}
