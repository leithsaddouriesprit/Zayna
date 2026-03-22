package tn.esprit.workshop.services.leith;

import tn.esprit.workshop.model.leith.AgentEnfantEcoleRow;
import tn.esprit.workshop.model.leith.Enfant;
import tn.esprit.workshop.services.CRUD;
import tn.esprit.workshop.utilis.MyBDConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EnfantService implements CRUD<Enfant> {

    public EnfantService() {
    }

    private Connection getConnection() throws SQLException {
        return MyBDConnexion.getInstance().getConnection();
    }

    @Override
    public void insertOne(Enfant e) throws SQLException {
        String req =
                "INSERT INTO enfant (nom, prenom, parent_id, trajet_id, actif, on_board) VALUES (" +
                        "'" + e.getNom() + "', " +
                        "'" + e.getPrenom() + "', " +
                        e.getParentId() + ", " +
                        e.getTrajetId() + ", " +
                        e.isActif() + ", " +
                        (e.isOnBoard() ? 1 : 0) +
                        ")";
        Statement st = getConnection().createStatement();
        st.executeUpdate(req);
    }

    /** Insère un enfant depuis une candidature acceptée (actif=1, on_board=0). */
    public void insertFromCandidature(String nom, String prenom, int parentId, int trajetId) throws SQLException {
        String sql = "INSERT INTO enfant (nom, prenom, parent_id, trajet_id, actif, on_board) VALUES (?, ?, ?, ?, 1, 0)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, nom);
            ps.setString(2, prenom);
            ps.setInt(3, parentId);
            ps.setInt(4, trajetId);
            ps.executeUpdate();
        }
    }

    @Override
    public void updateOne(Enfant e) throws SQLException {
        String req =
                "UPDATE enfant SET " +
                        "nom='" + e.getNom() + "', " +
                        "prenom='" + e.getPrenom() + "', " +
                        "trajet_id=" + e.getTrajetId() + ", " +
                        "actif=" + e.isActif() +
                        " WHERE id=" + e.getEnfantId();
        Statement st = getConnection().createStatement();
        st.executeUpdate(req);
    }

    @Override
    public void deleteOne(Enfant e) throws SQLException {
        String req = "DELETE FROM enfant WHERE id=" + e.getEnfantId();
        Statement st = getConnection().createStatement();
        st.executeUpdate(req);
    }

    @Override
    public List<Enfant> selectAll() throws SQLException {
        List<Enfant> list = new ArrayList<>();
        String req = "SELECT * FROM enfant";
        Statement st = getConnection().createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            Enfant e = new Enfant();
            e.setEnfantId(rs.getInt("id"));
            e.setNom(rs.getString("nom"));
            e.setPrenom(rs.getString("prenom"));
            e.setParentId(rs.getInt("parent_id"));
            e.setTrajetId(rs.getInt("trajet_id"));
            e.setActif(rs.getBoolean("actif"));
            e.setOnBoard(rs.getBoolean("on_board"));
            list.add(e);
        }
        return list;
    }

    /**
     * Enfants actifs dont le trajet emprunte le bus indiqué (maîtresse).
     */
    public List<Enfant> findEnfantsByBusId(int busId, String search) throws SQLException {
        List<Enfant> list = new ArrayList<>();
        String term = search == null ? "" : search.trim();
        String like = "%" + term + "%";
        String sql = """
                SELECT e.id, e.nom, e.prenom, e.parent_id, e.trajet_id, e.actif, e.on_board
                FROM enfant e
                INNER JOIN trajet t ON e.trajet_id = t.id
                WHERE t.id_bus = ? AND e.actif = 1
                AND (e.nom LIKE ? OR e.prenom LIKE ? OR ? = '')
                ORDER BY e.nom, e.prenom
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, busId);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, term);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Enfant e = new Enfant();
                    e.setEnfantId(rs.getInt("id"));
                    e.setNom(rs.getString("nom"));
                    e.setPrenom(rs.getString("prenom"));
                    e.setParentId(rs.getInt("parent_id"));
                    e.setTrajetId(rs.getInt("trajet_id"));
                    e.setActif(rs.getBoolean("actif"));
                    e.setOnBoard(rs.getBoolean("on_board"));
                    list.add(e);
                }
            }
        }
        return list;
    }

    public void updateOnBoardIfEnfantOnBus(int enfantId, int busId, boolean onBoard) throws SQLException {
        String sql = """
                UPDATE enfant e
                INNER JOIN trajet t ON e.trajet_id = t.id AND t.id_bus = ?
                SET e.on_board = ?
                WHERE e.id = ?
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, busId);
            ps.setBoolean(2, onBoard);
            ps.setInt(3, enfantId);
            if (ps.executeUpdate() != 1) {
                throw new SQLException("Mise à jour impossible : enfant ou bus invalide");
            }
        }
    }

    public Enfant getEnfantById(int id) {

        Enfant enfant = null;
        String sql = "SELECT * FROM enfant WHERE id = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                enfant = new Enfant();

                enfant.setEnfantId(rs.getInt("id"));
                enfant.setNom(rs.getString("nom"));
                enfant.setPrenom(rs.getString("prenom"));
                enfant.setParentId(rs.getInt("parent_id"));
                enfant.setTrajetId(rs.getInt("trajet_id"));
                enfant.setActif(rs.getBoolean("actif"));
                enfant.setOnBoard(rs.getBoolean("on_board"));

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return enfant;
    }
    /**
     * Retourne la liste des enfants du parent (id, nom, prenom).
     */
    public List<Enfant> getByParentId(int parentId) throws SQLException {
        List<Enfant> list = new ArrayList<>();
        String sql = "SELECT id, nom, prenom, parent_id, trajet_id, actif FROM enfant WHERE parent_id = ? AND actif = 1";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, parentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Enfant e = new Enfant();
                e.setEnfantId(rs.getInt("id"));
                e.setNom(rs.getString("nom"));
                e.setPrenom(rs.getString("prenom"));
                e.setParentId(rs.getInt("parent_id"));
                e.setTrajetId(rs.getInt("trajet_id"));
                e.setActif(rs.getBoolean("actif"));
                list.add(e);
            }
        }
        return list;
    }

    public Enfant getEnfantByTrajetId(int trajetId) throws SQLException {
        String sql = "SELECT * FROM enfant WHERE trajet_id = ? LIMIT 1";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, trajetId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Enfant e = new Enfant();
                    e.setEnfantId(rs.getInt("id"));
                    e.setNom(rs.getString("nom"));
                    e.setPrenom(rs.getString("prenom"));
                    e.setParentId(rs.getInt("parent_id"));
                    e.setTrajetId(rs.getInt("trajet_id"));
                    e.setActif(rs.getBoolean("actif"));
                    // e.setOnBoard(rs.getBoolean("on_board")); si tu l’as ajoutée
                    return e;
                }
            }
        }
        return null;
    }

    /** Nombre d'enfants actifs dont le trajet appartient à l'école (pour dashboard agent). */
    public int countActifsByEcoleId(int idEcole) throws SQLException {
        String sql = "SELECT COUNT(*) AS n FROM enfant e JOIN trajet t ON e.trajet_id = t.id WHERE t.id_ecole = ? AND e.actif = 1";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, idEcole);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("n");
            }
        }
        return 0;
    }

    /**
     * Enfants visibles agent : uniquement ceux avec {@code enfant.trajet_id} renseigné,
     * trajet existant, et {@code trajet.id_ecole} = école de l'agent (périmètre strict, pas de lecture globale sur {@code enfant}).
     */
    public List<AgentEnfantEcoleRow> listActifsByEcoleViaTrajet(int idEcole, String search) throws SQLException {
        List<AgentEnfantEcoleRow> list = new ArrayList<>();
        String term = search == null ? "" : search.trim().toLowerCase();
        String like = "%" + term + "%";
        String sql = """
                SELECT e.id, e.nom, e.prenom, e.trajet_id, e.actif, e.on_board,
                       t.nom AS trajet_nom, t.id_bus AS traj_bus_id,
                       b.matricule AS bus_matricule, b.numero_bus AS bus_numero
                FROM enfant e
                INNER JOIN trajet t ON t.id = e.trajet_id
                LEFT JOIN bus b ON b.id = t.id_bus
                WHERE t.id_ecole = ?
                  AND e.trajet_id IS NOT NULL
                  AND e.actif = 1
                  AND (LOWER(e.nom) LIKE ? OR LOWER(e.prenom) LIKE ? OR ? = '')
                ORDER BY e.nom, e.prenom
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, idEcole);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, term);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapAgentEnfantEcoleRow(rs));
                }
            }
        }
        return list;
    }

    private static AgentEnfantEcoleRow mapAgentEnfantEcoleRow(ResultSet rs) throws SQLException {
        AgentEnfantEcoleRow r = new AgentEnfantEcoleRow();
        r.setEnfantId(rs.getInt("id"));
        r.setNom(rs.getString("nom"));
        r.setPrenom(rs.getString("prenom"));
        r.setTrajetId(rs.getInt("trajet_id"));
        r.setTrajetNom(rs.getString("trajet_nom"));
        int bid = rs.getInt("traj_bus_id");
        if (rs.wasNull() || bid == 0) {
            r.setIdBus(null);
            r.setBusLabel("—");
        } else {
            r.setIdBus(bid);
            String mat = rs.getString("bus_matricule");
            String num = rs.getString("bus_numero");
            String label = (mat != null && !mat.isBlank()) ? mat
                    : (num != null && !num.isBlank()) ? num : ("Bus " + bid);
            r.setBusLabel(label);
        }
        r.setActif(rs.getBoolean("actif"));
        r.setOnBoard(rs.getBoolean("on_board"));
        return r;
    }

    /** Vérifie que l'enfant est rattaché à un trajet de cette école (trajet obligatoire, filtre id_ecole). */
    public boolean isEnfantInEcoleScope(int enfantId, int ecoleId) throws SQLException {
        String sql = """
                SELECT 1 FROM enfant e
                INNER JOIN trajet t ON t.id = e.trajet_id
                WHERE e.id = ?
                  AND e.trajet_id IS NOT NULL
                  AND t.id_ecole = ?
                LIMIT 1
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, enfantId);
            ps.setInt(2, ecoleId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Affecte l'enfant à un trajet de l'école (changement de trajet = changement de bus via trajet.id_bus).
     * Exige un bus déjà affecté au trajet (id_bus non nul et non 0). Remet on_board à false.
     */
    public void updateEnfantTrajetForEcole(int enfantId, int newTrajetId, int ecoleId) throws SQLException {
        try (Connection conn = getConnection()) {
            int idBus;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id_bus FROM trajet WHERE id = ? AND id_ecole = ?")) {
                ps.setInt(1, newTrajetId);
                ps.setInt(2, ecoleId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Trajet introuvable ou hors de votre école.");
                    }
                    idBus = rs.getInt("id_bus");
                    if (rs.wasNull() || idBus == 0) {
                        throw new SQLException("Aucun bus sur ce trajet.");
                    }
                }
            }
            String upd = """
                    UPDATE enfant e
                    INNER JOIN trajet t ON e.trajet_id = t.id AND t.id_ecole = ?
                    SET e.trajet_id = ?, e.on_board = 0
                    WHERE e.id = ?
                    """;
            try (PreparedStatement ps = conn.prepareStatement(upd)) {
                ps.setInt(1, ecoleId);
                ps.setInt(2, newTrajetId);
                ps.setInt(3, enfantId);
                if (ps.executeUpdate() != 1) {
                    throw new SQLException("Impossible de mettre à jour l'affectation.");
                }
            }
        }
    }

    /** Suppression uniquement si l'enfant est dans le périmètre école (via trajet). */
    public void deleteEnfantIfInEcoleScope(int enfantId, int ecoleId) throws SQLException {
        if (!isEnfantInEcoleScope(enfantId, ecoleId)) {
            throw new SQLException("Enfant hors périmètre.");
        }
        Enfant e = getEnfantById(enfantId);
        if (e == null) {
            throw new SQLException("Enfant introuvable.");
        }
        deleteOne(e);
    }
}
