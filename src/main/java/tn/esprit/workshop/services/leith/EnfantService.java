package tn.esprit.workshop.services.leith;

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
            list.add(e);
        }
        return list;
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
}
