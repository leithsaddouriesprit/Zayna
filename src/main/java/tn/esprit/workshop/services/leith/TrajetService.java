package tn.esprit.workshop.services.leith;

import tn.esprit.workshop.model.leith.Trajet;
import tn.esprit.workshop.services.CRUD;
import tn.esprit.workshop.utilis.MyBDConnexion;

import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class TrajetService implements CRUD<Trajet> {

    public TrajetService() {
    }

    private Connection getConnection() throws SQLException {
        return MyBDConnexion.getInstance().getConnection();
    }

    private static void mapRowToTrajet(Trajet t, ResultSet rs) throws SQLException {
        t.setTrajetId(rs.getInt("id"));
        t.setNom(rs.getString("nom"));
        int idBus = rs.getInt("id_bus");
        if (!rs.wasNull()) {
            t.setIdBus(idBus);
        } else {
            t.setIdBus(0);
        }
        t.setIdEcole(rs.getInt("id_ecole"));
        Time ht = rs.getTime("heure_depart");
        if (ht != null) {
            t.setHeureDepart(ht.toLocalTime());
        }
        double px = rs.getDouble("prix");
        t.setPrix(rs.wasNull() ? 0.0 : px);
        t.setStatut(rs.getString("statut"));
    }

    @Override
    public void insertOne(Trajet t) throws SQLException {
        String idBusVal = (t.getIdBus() == 0) ? "NULL" : String.valueOf(t.getIdBus());
        String req =
                "INSERT INTO trajet (nom, id_bus, id_ecole, heure_depart, prix, statut) VALUES (" +
                        "'" + t.getNom().replace("'", "''") + "', " +
                        idBusVal + ", " +
                        t.getIdEcole() + ", " +
                        "'" + (t.getHeureDepart() != null ? t.getHeureDepart() : LocalTime.MIDNIGHT) + "', " +
                        t.getPrix() + ", " +
                        "'" + (t.getStatut() != null ? t.getStatut().replace("'", "''") : "PLANIFIE") + "'" +
                        ")";
        Statement st = getConnection().createStatement();
        st.executeUpdate(req);
    }

    @Override
    public void updateOne(Trajet t) throws SQLException {
        String idBusVal = (t.getIdBus() == 0) ? "NULL" : String.valueOf(t.getIdBus());
        String req =
                "UPDATE trajet SET " +
                        "nom='" + t.getNom().replace("'", "''") + "', " +
                        "id_bus=" + idBusVal + ", " +
                        "id_ecole=" + t.getIdEcole() + ", " +
                        "heure_depart='" + (t.getHeureDepart() != null ? t.getHeureDepart() : LocalTime.MIDNIGHT) + "', " +
                        "prix=" + t.getPrix() + ", " +
                        "statut='" + (t.getStatut() != null ? t.getStatut().replace("'", "''") : "PLANIFIE") + "'" +
                        " WHERE id=" + t.getTrajetId();

        Statement st = getConnection().createStatement();
        st.executeUpdate(req);
    }

    @Override
    public void deleteOne(Trajet t) throws SQLException {
        String req = "DELETE FROM trajet WHERE id=" + t.getTrajetId();
        Statement st = getConnection().createStatement();
        st.executeUpdate(req);
    }

    @Override
    public List<Trajet> selectAll() throws SQLException {
        List<Trajet> list = new ArrayList<>();
        String req = "SELECT * FROM trajet";
        Statement st = getConnection().createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            Trajet t = new Trajet();
            mapRowToTrajet(t, rs);
            list.add(t);
        }
        return list;
    }

    public Trajet getTrajetByEnfant(int enfantId) throws SQLException {

        String req =
                "SELECT t.* FROM trajet t " +
                        "JOIN enfant e ON e.trajet_id = t.id " +
                        "WHERE e.id = " + enfantId +
                        " AND e.actif = true";

        Statement st = getConnection().createStatement();
        ResultSet rs = st.executeQuery(req);

        if (rs.next()) {
            Trajet t = new Trajet();
            mapRowToTrajet(t, rs);
            return t;
        }

        return null;
    }


    public Trajet getByBusId(int busId) throws SQLException {

        String sql = "SELECT * FROM trajet WHERE id_bus = ? LIMIT 1";

        PreparedStatement ps = getConnection().prepareStatement(sql);

        ps.setInt(1, busId);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            Trajet t = new Trajet();
            mapRowToTrajet(t, rs);
            return t;
        }


        return null;
    }

    public Trajet getById(int id) throws SQLException {
        String sql = "SELECT * FROM trajet WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Trajet t = new Trajet();
                    mapRowToTrajet(t, rs);
                    return t;
                }
            }
        }
        return null;
    }

    /** Trajets de l'école (pour l'agent). */
    public List<Trajet> selectByEcoleId(int idEcole) throws SQLException {
        List<Trajet> list = new ArrayList<>();
        String sql = "SELECT * FROM trajet WHERE id_ecole = ? ORDER BY nom";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, idEcole);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Trajet t = new Trajet();
                    mapRowToTrajet(t, rs);
                    list.add(t);
                }
            }
        }
        return list;
    }

    /** Affecte un bus au trajet. */
    public void updateIdBus(int trajetId, Integer busId) throws SQLException {
        String sql = "UPDATE trajet SET id_bus = ? WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setObject(1, busId);
            ps.setInt(2, trajetId);
            ps.executeUpdate();
        }
    }

}
