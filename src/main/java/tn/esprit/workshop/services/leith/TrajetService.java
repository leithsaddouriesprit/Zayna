package tn.esprit.workshop.services.leith;

import tn.esprit.workshop.model.leith.Trajet;
import tn.esprit.workshop.services.CRUD;
import tn.esprit.workshop.utilis.MyBDConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TrajetService implements CRUD<Trajet> {

    public TrajetService() {
    }

    private Connection getConnection() throws SQLException {
        return MyBDConnexion.getInstance().getConnection();
    }

    @Override
    public void insertOne(Trajet t) throws SQLException {
        String sql = "INSERT INTO trajet (nom, id_bus, id_ecole, heure_depart, prix, statut) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, t.getNom());
            if (t.getIdBus() == 0) ps.setNull(2, Types.INTEGER); else ps.setInt(2, t.getIdBus());
            ps.setInt(3, t.getIdEcole());
            ps.setTime(4, Time.valueOf(t.getHeureDepart()));
            ps.setDouble(5, t.getPrix());
            ps.setString(6, t.getStatut() != null ? t.getStatut() : "PLANIFIE");
            ps.executeUpdate();
        }
    }

    @Override
    public void updateOne(Trajet t) throws SQLException {
        String sql = "UPDATE trajet SET nom = ?, id_bus = ?, id_ecole = ?, heure_depart = ?, prix = ?, statut = ? WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, t.getNom());
            if (t.getIdBus() == 0) ps.setNull(2, Types.INTEGER); else ps.setInt(2, t.getIdBus());
            ps.setInt(3, t.getIdEcole());
            ps.setTime(4, Time.valueOf(t.getHeureDepart()));
            ps.setDouble(5, t.getPrix());
            ps.setString(6, t.getStatut() != null ? t.getStatut() : "PLANIFIE");
            ps.setInt(7, t.getTrajetId());
            ps.executeUpdate();
        }
    }

    @Override
    public void deleteOne(Trajet t) throws SQLException {
        String sql = "DELETE FROM trajet WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, t.getTrajetId());
            ps.executeUpdate();
        }
    }

    @Override
    public List<Trajet> selectAll() throws SQLException {
        List<Trajet> list = new ArrayList<>();
        String req = "SELECT * FROM trajet";
        Statement st = getConnection().createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            list.add(mapTrajet(rs));
        }
        return list;
    }

    public Trajet getTrajetByEnfant(int enfantId) throws SQLException {

        String sql = "SELECT t.* FROM trajet t JOIN enfant e ON e.trajet_id = t.id WHERE e.id = ? AND e.actif = true";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, enfantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapTrajet(rs);
                }
            }
        }
        return null;
    }


    public Trajet getByBusId(int busId) throws SQLException {

        String sql = "SELECT * FROM trajet WHERE id_bus = ? LIMIT 1";

       PreparedStatement ps = getConnection().prepareStatement(sql);

            ps.setInt(1, busId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapTrajet(rs);
            }


        return null;
    }

    public Trajet getById(int id) throws SQLException {
        String sql = "SELECT * FROM trajet WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapTrajet(rs);
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
                    list.add(mapTrajet(rs));
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

    private Trajet mapTrajet(ResultSet rs) throws SQLException {
        Trajet t = new Trajet();
        t.setTrajetId(rs.getInt("id"));
        t.setNom(rs.getString("nom"));
        int idBus = rs.getInt("id_bus");
        if (!rs.wasNull()) t.setIdBus(idBus);
        t.setIdEcole(rs.getInt("id_ecole"));
        Time ht = rs.getTime("heure_depart");
        if (ht != null) t.setHeureDepart(ht.toLocalTime());
        t.setPrix(rs.getDouble("prix"));
        t.setStatut(rs.getString("statut"));
        return t;
    }

}
