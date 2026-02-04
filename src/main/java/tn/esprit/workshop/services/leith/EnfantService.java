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
}
