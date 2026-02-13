package tn.esprit.workshop.services.leith;

import tn.esprit.workshop.model.leith.Enfant;
import tn.esprit.workshop.services.CRUD;
import tn.esprit.workshop.utilis.MyBDConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EnfantService implements CRUD<Enfant> {

    private final Connection connection;

    public EnfantService() {
        this.connection = MyBDConnexion.getInstance().getConnection();
    }

    @Override
    public void insertOne(Enfant e) throws SQLException {
        String req =
                "INSERT INTO enfant (nom, prenom, parent_id, trajet_id, actif) VALUES (" +
                        "'" + e.getNom() + "', " +
                        "'" + e.getPrenom() + "', " +
                        e.getParentId() + ", " +
                        e.getTrajetId() + ", " +
                        e.isActif() +
                        ")";
        Statement st = connection.createStatement();
        st.executeUpdate(req);
    }

    @Override
    public void updateOne(Enfant e) throws SQLException {
        String req =
                "UPDATE enfant SET " +
                        "nom='" + e.getNom() + "', " +
                        "prenom='" + e.getPrenom() + "', " +
                        "trajet_id=" + e.getTrajetId() + ", " +
                        "actif=" + e.isActif() +
                        " WHERE id=" + e.getId();
        Statement st = connection.createStatement();
        st.executeUpdate(req);
    }

    @Override
    public void deleteOne(Enfant e) throws SQLException {
        String req = "DELETE FROM enfant WHERE id=" + e.getId();
        Statement st = connection.createStatement();
        st.executeUpdate(req);
    }

    @Override
    public List<Enfant> selectAll() throws SQLException {
        List<Enfant> list = new ArrayList<>();
        String req = "SELECT * FROM enfant";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            Enfant e = new Enfant();
            e.setId(rs.getInt("id"));
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

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                enfant = new Enfant();

                enfant.setId(rs.getInt("id"));
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
    public Enfant getEnfantByTrajetId(int trajetId) throws SQLException {
        String sql = "SELECT * FROM enfant WHERE trajet_id = ? LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, trajetId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Enfant e = new Enfant();
                    e.setId(rs.getInt("id"));
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


}
