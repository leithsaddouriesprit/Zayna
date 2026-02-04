package tn.esprit.workshop.services.leith;

import tn.esprit.workshop.model.leith.Arret;
import tn.esprit.workshop.services.CRUD;
import tn.esprit.workshop.utilis.MyBDConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ArretService implements CRUD<Arret> {

    private final Connection connection;

    public ArretService() {
        this.connection = MyBDConnexion.getInstance().getConnection();
    }

    @Override
    public void insertOne(Arret a) throws SQLException {
        String req =
                "INSERT INTO arret (id_trajet, nom, latitude, longitude, ordre_arret, heure_prevue) VALUES (" +
                        a.getIdTrajet() + ", " +
                        "'" + a.getNom() + "', " +
                        a.getLatitude() + ", " +
                        a.getLongitude() + ", " +
                        a.getOrdre() + ", " +
                        "'" + a.getHeurePrevue() + "'" +
                        ")";
        Statement st = connection.createStatement();
        st.executeUpdate(req);
    }

    @Override
    public void updateOne(Arret a) throws SQLException {
        String req =
                "UPDATE arret SET " +
                        "nom='" + a.getNom() + "', " +
                        "latitude=" + a.getLatitude() + ", " +
                        "longitude=" + a.getLongitude() + ", " +
                        "ordre_arret=" + a.getOrdre() + ", " +
                        "heure_prevue='" + a.getHeurePrevue() + "'" +
                        " WHERE id=" + a.getId();

        Statement st = connection.createStatement();
        st.executeUpdate(req);
    }

    @Override
    public void deleteOne(Arret a) throws SQLException {
        String req = "DELETE FROM arret WHERE id=" + a.getId();
        Statement st = connection.createStatement();
        st.executeUpdate(req);
    }

    @Override
    public List<Arret> selectAll() throws SQLException {
        List<Arret> list = new ArrayList<>();
        String req = "SELECT * FROM arret";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            Arret a = new Arret();
            a.setId(rs.getInt("id"));
            a.setIdTrajet(rs.getInt("id_trajet"));
            a.setNom(rs.getString("nom"));
            a.setLatitude(rs.getDouble("latitude"));
            a.setLongitude(rs.getDouble("longitude"));
            a.setOrdre(rs.getInt("ordre_arret"));
            a.setHeurePrevue(rs.getString("heure_prevue"));
            list.add(a);
        }
        return list;
    }
}
