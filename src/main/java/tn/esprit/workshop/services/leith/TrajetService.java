package tn.esprit.workshop.services.leith;

import tn.esprit.workshop.model.leith.Trajet;
import tn.esprit.workshop.services.CRUD;
import tn.esprit.workshop.utilis.MyBDConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TrajetService implements CRUD<Trajet> {

    private final Connection connection;

    public TrajetService() {
        this.connection = MyBDConnexion.getInstance().getConnection();
    }

    @Override
    public void insertOne(Trajet t) throws SQLException {
        String req =
                "INSERT INTO trajet (nom, id_bus, id_ecole, heure_depart, actif, statut) VALUES (" +
                        "'" + t.getNom() + "', " +
                        t.getIdBus() + ", " +
                        t.getIdEcole() + ", " +
                        "'" + t.getHeureDepart() + "', " +
                        t.isActif() + ", " +
                        "'" + t.getStatut() + "'" +
                        ")";
        Statement st = connection.createStatement();
        st.executeUpdate(req);
    }

    @Override
    public void updateOne(Trajet t) throws SQLException {
        String req =
                "UPDATE trajet SET " +
                        "nom='" + t.getNom() + "', " +
                        "id_bus=" + t.getIdBus() + ", " +
                        "id_ecole=" + t.getIdEcole() + ", " +
                        "heure_depart='" + t.getHeureDepart() + "', " +
                        "actif=" + t.isActif() + ", " +
                        "statut='" + t.getStatut() + "'" +
                        " WHERE id=" + t.getId();

        Statement st = connection.createStatement();
        st.executeUpdate(req);
    }

    @Override
    public void deleteOne(Trajet t) throws SQLException {
        String req = "DELETE FROM trajet WHERE id=" + t.getId();
        Statement st = connection.createStatement();
        st.executeUpdate(req);
    }

    @Override
    public List<Trajet> selectAll() throws SQLException {
        List<Trajet> list = new ArrayList<>();
        String req = "SELECT * FROM trajet";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            Trajet t = new Trajet();
            t.setId(rs.getInt("id"));
            t.setNom(rs.getString("nom"));
            t.setIdBus(rs.getInt("id_bus"));
            t.setIdEcole(rs.getInt("id_ecole"));
            t.setHeureDepart(rs.getTime("heure_depart").toLocalTime());
            t.setActif(rs.getBoolean("actif"));
            t.setStatut(rs.getString("statut"));
            list.add(t);
        }
        return list;
    }
}
