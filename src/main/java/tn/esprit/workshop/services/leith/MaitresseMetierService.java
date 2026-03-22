package tn.esprit.workshop.services.leith;

import at.favre.lib.crypto.bcrypt.BCrypt;
import tn.esprit.workshop.model.Talel.Dao.DaoUser;
import tn.esprit.workshop.model.Talel.talel2.CategorieUser;
import tn.esprit.workshop.model.leith.MaitresseEcoleRow;
import tn.esprit.workshop.utilis.MyBDConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MaitresseMetierService {

    private static final int BCRYPT_COST = 12;

    private Connection getConnection() throws SQLException {
        return MyBDConnexion.getInstance().getConnection();
    }

    private void clearBusFromOtherMaitresses(Connection conn, int busId, int excludeMaitresseId) throws SQLException {
        String sql = "UPDATE maitresse SET id_bus = NULL WHERE id_bus = ? AND id <> ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, busId);
            ps.setInt(2, excludeMaitresseId);
            ps.executeUpdate();
        }
    }

    private void clearBusGlobally(Connection conn, int busId) throws SQLException {
        String sql = "UPDATE maitresse SET id_bus = NULL WHERE id_bus = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, busId);
            ps.executeUpdate();
        }
    }

    /**
     * Crée users + maitresse pour l'école indiquée. Affectation bus exclusive (désassigne l'ancienne maitresse si besoin).
     */
    public void createMaitresseForEcole(int idEcole, String nom, String prenom, String email, String passwordClair, Integer idBus) throws Exception {
        if (nom == null || nom.isBlank()) {
            throw new Exception("Le nom est obligatoire");
        }
        if (prenom == null || prenom.isBlank()) {
            throw new Exception("Le prénom est obligatoire");
        }
        if (email == null || email.isBlank()) {
            throw new Exception("L'email est obligatoire");
        }
        if (passwordClair == null || passwordClair.isBlank()) {
            throw new Exception("Le mot de passe est obligatoire");
        }
        if (passwordClair.length() < 6) {
            throw new Exception("Le mot de passe doit contenir au moins 6 caractères");
        }

        DaoUser daoUser = new DaoUser();
        if (daoUser.emailExists(email.trim())) {
            throw new Exception("Un compte avec cet email existe déjà");
        }

        String hash = BCrypt.withDefaults().hashToString(BCRYPT_COST, passwordClair.toCharArray());
        String displayNom = (prenom.trim() + " " + nom.trim()).trim();

        Connection conn = getConnection();
        boolean prevAc = conn.getAutoCommit();
        conn.setAutoCommit(false);
        try {
            try {
                if (idBus != null && idBus > 0) {
                    clearBusGlobally(conn, idBus);
                }

                int userId;
                String sqlUser = "INSERT INTO users (nom, email, mot_de_passe, categorie) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, displayNom);
                    ps.setString(2, email.trim());
                    ps.setString(3, hash);
                    ps.setString(4, CategorieUser.MAITRESSE.name());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("users : aucun id généré");
                        }
                        userId = keys.getInt(1);
                    }
                }

                String sqlM = "INSERT INTO maitresse (nom, prenom, id_ecole, id_bus, user_id) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sqlM)) {
                    ps.setString(1, nom.trim());
                    ps.setString(2, prenom.trim());
                    ps.setInt(3, idEcole);
                    if (idBus != null && idBus > 0) {
                        ps.setInt(4, idBus);
                    } else {
                        ps.setNull(4, Types.INTEGER);
                    }
                    ps.setInt(5, userId);
                    ps.executeUpdate();
                }

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } finally {
            conn.setAutoCommit(prevAc);
        }
    }

    public List<MaitresseEcoleRow> listByEcole(int idEcole, String search) throws SQLException {
        List<MaitresseEcoleRow> list = new ArrayList<>();
        String term = search == null ? "" : search.trim();
        String like = "%" + term + "%";
        String sql = """
                SELECT m.id AS mid, m.nom AS mn, m.prenom AS mp, m.id_bus, m.user_id, u.email,
                TRIM(CONCAT(COALESCE(b.numero_bus,''), ' ', COALESCE(b.matricule,''))) AS bus_label
                FROM maitresse m
                JOIN users u ON u.id = m.user_id
                LEFT JOIN bus b ON b.id = m.id_bus
                WHERE m.id_ecole = ?
                AND (m.nom LIKE ? OR m.prenom LIKE ? OR u.email LIKE ? OR ? = '')
                ORDER BY m.nom, m.prenom
                """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idEcole);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);
            ps.setString(5, term);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MaitresseEcoleRow row = new MaitresseEcoleRow();
                    row.setMaitresseId(rs.getInt("mid"));
                    row.setNom(rs.getString("mn"));
                    row.setPrenom(rs.getString("mp"));
                    int ub = rs.getInt("id_bus");
                    row.setIdBus(rs.wasNull() ? null : ub);
                    row.setUserId(rs.getInt("user_id"));
                    row.setEmail(rs.getString("email"));
                    String bl = rs.getString("bus_label");
                    row.setBusLabel(bl != null ? bl.trim() : "");
                    list.add(row);
                }
            }
        }
        return list;
    }

    public List<MaitresseEcoleRow> listAllForAdmin(String search) throws SQLException {
        List<MaitresseEcoleRow> list = new ArrayList<>();
        String term = search == null ? "" : search.trim();
        String like = "%" + term + "%";
        String sql = """
                SELECT m.id AS mid, m.nom AS mn, m.prenom AS mp, m.id_bus, m.user_id, u.email, ec.nom AS ecole_nom,
                TRIM(CONCAT(COALESCE(b.numero_bus,''), ' ', COALESCE(b.matricule,''))) AS bus_label
                FROM maitresse m
                JOIN users u ON u.id = m.user_id
                JOIN ecole ec ON ec.id = m.id_ecole
                LEFT JOIN bus b ON b.id = m.id_bus
                WHERE (m.nom LIKE ? OR m.prenom LIKE ? OR u.email LIKE ? OR ec.nom LIKE ? OR ? = '')
                ORDER BY ec.nom, m.nom, m.prenom
                """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);
            ps.setString(5, term);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MaitresseEcoleRow row = new MaitresseEcoleRow();
                    row.setMaitresseId(rs.getInt("mid"));
                    row.setNom(rs.getString("mn"));
                    row.setPrenom(rs.getString("mp"));
                    int ub = rs.getInt("id_bus");
                    row.setIdBus(rs.wasNull() ? null : ub);
                    row.setUserId(rs.getInt("user_id"));
                    row.setEmail(rs.getString("email"));
                    row.setEcoleNom(rs.getString("ecole_nom"));
                    row.setBusLabel(rs.getString("bus_label"));
                    list.add(row);
                }
            }
        }
        return list;
    }

    public List<BusOption> listAvailableBusesForEcole(int idEcole, int currentMaitresseId) throws SQLException {
        List<BusOption> list = new ArrayList<>();
        String sql = """
                SELECT b.id, b.numero_bus, b.matricule FROM bus b
                WHERE b.id_ecole = ?
                AND (
                  NOT EXISTS (SELECT 1 FROM maitresse m WHERE m.id_bus = b.id)
                  OR EXISTS (SELECT 1 FROM maitresse m2 WHERE m2.id = ? AND m2.id_bus = b.id)
                )
                ORDER BY b.numero_bus
                """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idEcole);
            ps.setInt(2, currentMaitresseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String num = rs.getString("numero_bus");
                    String mat = rs.getString("matricule");
                    String label = "Bus #" + id;
                    if (num != null && !num.isBlank()) {
                        label = num + (mat != null && !mat.isBlank() ? " (" + mat + ")" : "");
                    } else if (mat != null && !mat.isBlank()) {
                        label = mat;
                    }
                    list.add(new BusOption(id, label));
                }
            }
        }
        return list;
    }

    public void updateMaitresse(int idEcole, int maitresseId, String nom, String prenom, String email, Integer idBus, String newPasswordClairOrNull) throws Exception {
        if (nom == null || nom.isBlank()) {
            throw new Exception("Le nom est obligatoire");
        }
        if (prenom == null || prenom.isBlank()) {
            throw new Exception("Le prénom est obligatoire");
        }
        if (email == null || email.isBlank()) {
            throw new Exception("L'email est obligatoire");
        }

        int userId = getUserIdForMaitresse(maitresseId, idEcole);
        DaoUser daoUser = new DaoUser();
        if (daoUser.emailExistsExceptId(email.trim(), userId)) {
            throw new Exception("Cet email est déjà utilisé");
        }

        String displayNom = (prenom.trim() + " " + nom.trim()).trim();

        Connection conn = getConnection();
        boolean prevAc = conn.getAutoCommit();
        conn.setAutoCommit(false);
        try {
            try {
                if (idBus != null && idBus > 0) {
                    clearBusFromOtherMaitresses(conn, idBus, maitresseId);
                }

                String sqlM = "UPDATE maitresse SET nom = ?, prenom = ?, id_bus = ? WHERE id = ? AND id_ecole = ?";
                try (PreparedStatement ps = conn.prepareStatement(sqlM)) {
                    ps.setString(1, nom.trim());
                    ps.setString(2, prenom.trim());
                    if (idBus != null && idBus > 0) {
                        ps.setInt(3, idBus);
                    } else {
                        ps.setNull(3, Types.INTEGER);
                    }
                    ps.setInt(4, maitresseId);
                    ps.setInt(5, idEcole);
                    if (ps.executeUpdate() != 1) {
                        throw new Exception("Maitresse introuvable pour cette école");
                    }
                }

                if (newPasswordClairOrNull != null && !newPasswordClairOrNull.isBlank()) {
                    if (newPasswordClairOrNull.length() < 6) {
                        throw new Exception("Le mot de passe doit contenir au moins 6 caractères");
                    }
                    String hash = BCrypt.withDefaults().hashToString(BCRYPT_COST, newPasswordClairOrNull.toCharArray());
                    String sqlPw = "UPDATE users SET mot_de_passe = ? WHERE id = ?";
                    try (PreparedStatement ps = conn.prepareStatement(sqlPw)) {
                        ps.setString(1, hash);
                        ps.setInt(2, userId);
                        ps.executeUpdate();
                    }
                }

                String sqlU = "UPDATE users SET nom = ?, email = ? WHERE id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sqlU)) {
                    ps.setString(1, displayNom);
                    ps.setString(2, email.trim());
                    ps.setInt(3, userId);
                    ps.executeUpdate();
                }

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } finally {
            conn.setAutoCommit(prevAc);
        }
    }

    public void deleteMaitresse(int idEcole, int maitresseId) throws SQLException {
        int userId = getUserIdForMaitresse(maitresseId, idEcole);
        String sqlUser = "DELETE FROM users WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlUser)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    private int getUserIdForMaitresse(int maitresseId, int idEcole) throws SQLException {
        String sql = "SELECT user_id FROM maitresse WHERE id = ? AND id_ecole = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maitresseId);
            ps.setInt(2, idEcole);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Maitresse introuvable");
                }
                return rs.getInt("user_id");
            }
        }
    }

    /** Résout m.id et id_ecole pour le login MAITRESSE. */
    public int[] resolveMaitresseIdsByUserId(int userId) throws SQLException {
        String sql = "SELECT id, id_ecole, id_bus FROM maitresse WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                int mid = rs.getInt("id");
                int eid = rs.getInt("id_ecole");
                return new int[]{mid, eid};
            }
        }
    }

    public Integer getBusIdForMaitresseUser(int userId) throws SQLException {
        String sql = "SELECT id_bus FROM maitresse WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                int b = rs.getInt("id_bus");
                return rs.wasNull() ? null : b;
            }
        }
    }

    public static final class BusOption {
        private final int id;
        private final String label;

        public BusOption(int id, String label) {
            this.id = id;
            this.label = label;
        }

        public int getId() {
            return id;
        }

        public String getLabel() {
            return label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
