package tn.esprit.workshop.services.leith;

import tn.esprit.workshop.model.leith.Arret;
import tn.esprit.workshop.model.leith.Trajet;
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

    public List<Arret> getArretsByTrajetOrdered(int trajetId) throws SQLException {
        List<Arret> list = new ArrayList<>();
        String req = "SELECT * FROM arret WHERE id_trajet=" + trajetId + " ORDER BY ordre_arret ASC";
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

    public Trajet getTrajetByEnfant(int enfantId) throws SQLException {
        String req =
                "SELECT t.* FROM trajet t " +
                        "JOIN enfant e ON e.trajet_id = t.id " +
                        "WHERE e.id=" + enfantId + " AND e.actif=true";

        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);

        if (rs.next()) {
            Trajet t = new Trajet();
            t.setId(rs.getInt("id"));
            t.setNom(rs.getString("nom"));
            t.setIdBus(rs.getInt("id_bus"));
            t.setIdEcole(rs.getInt("id_ecole"));
            t.setHeureDepart(rs.getTime("heure_depart").toLocalTime());
            t.setActif(rs.getBoolean("actif"));
            t.setStatut(rs.getString("statut"));
            return t;
        }
        return null;
    }
    public List<Arret> getByTrajetId(int trajetId) throws SQLException {
        List<Arret> list = new ArrayList<>();
        String sql = "SELECT id, id_trajet, nom, latitude, longitude, ordre_arret, heure_prevue " +
                "FROM arret WHERE id_trajet=? ORDER BY ordre_arret ASC";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, trajetId);
            try (ResultSet rs = ps.executeQuery()) {
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
            }
        }
        return list;
    }


    public Arret getDepart(int trajetId) throws SQLException {
        String req = "SELECT * FROM arret WHERE id_trajet = " + trajetId + " ORDER BY ordre_arret ASC LIMIT 1";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);

        if (rs.next()) {
            Arret a = new Arret();
            a.setId(rs.getInt("id"));
            a.setIdTrajet(rs.getInt("id_trajet"));
            a.setNom(rs.getString("nom"));
            a.setLatitude(rs.getDouble("latitude"));
            a.setLongitude(rs.getDouble("longitude"));
            a.setOrdre(rs.getInt("ordre_arret"));
            a.setHeurePrevue(rs.getString("heure_prevue"));
            return a;
        }
        return null;
    }
    public Arret getLastArret(int trajetId) throws SQLException {

        String req =
                "SELECT * FROM arret " +
                        "WHERE id_trajet = " + trajetId + " " +
                        "ORDER BY ordre_arret DESC " +
                        "LIMIT 1";

        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);

        if (rs.next()) {
            Arret a = new Arret();
            a.setId(rs.getInt("id"));
            a.setIdTrajet(rs.getInt("id_trajet"));
            a.setNom(rs.getString("nom"));
            a.setLatitude(rs.getDouble("latitude"));
            a.setLongitude(rs.getDouble("longitude"));
            a.setOrdre(rs.getInt("ordre_arret"));

            // si ta colonne est TIME: utilise getTime
            // sinon si c'est VARCHAR: laisse getString
            try {
                a.setHeurePrevue(String.valueOf(rs.getTime("heure_prevue")));
            } catch (Exception e) {
                a.setHeurePrevue(rs.getString("heure_prevue"));
            }

            return a;
        }

        return null;
    }



}
