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
        String idBusVal = (t.getIdBus() == 0) ? "NULL" : String.valueOf(t.getIdBus());
        String req =
                "INSERT INTO trajet (nom, id_bus, id_ecole, heure_depart, actif, statut) VALUES (" +
                        "'" + t.getNom().replace("'", "''") + "', " +
                        idBusVal + ", " +
                        t.getIdEcole() + ", " +
                        "'" + t.getHeureDepart() + "', " +
                        t.isActif() + ", " +
                        "'" + (t.getStatut() != null ? t.getStatut() : "PLANIFIE") + "'" +
                        ")";
        Statement st = connection.createStatement();
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
                        "heure_depart='" + t.getHeureDepart() + "', " +
                        "actif=" + t.isActif() + ", " +
                        "statut='" + (t.getStatut() != null ? t.getStatut() : "PLANIFIE") + "'" +
                        " WHERE id=" + t.getTrajetId();

        Statement st = connection.createStatement();
        st.executeUpdate(req);
    }

    @Override
    public void deleteOne(Trajet t) throws SQLException {
        String req = "DELETE FROM trajet WHERE id=" + t.getTrajetId();
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
            t.setTrajetId(rs.getInt("id"));
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

    public Trajet getTrajetByEnfant(int enfantId) throws SQLException {

        String req =
                "SELECT t.* FROM trajet t " +
                        "JOIN enfant e ON e.trajet_id = t.id " +
                        "WHERE e.id = " + enfantId +
                        " AND e.actif = true";

        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);

        if (rs.next()) {
            Trajet t = new Trajet();
            t.setTrajetId(rs.getInt("id"));
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


    public Trajet getByBusId(int busId) throws SQLException {

        String sql = "SELECT * FROM trajet WHERE id_bus = ? AND actif = 1 LIMIT 1";

       PreparedStatement ps = connection.prepareStatement(sql);

            ps.setInt(1, busId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Trajet t = new Trajet();
                t.setTrajetId(rs.getInt("id"));
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

    public Trajet getById(int id) throws SQLException {
        String sql = "SELECT * FROM trajet WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Trajet t = new Trajet();
                    t.setTrajetId(rs.getInt("id"));
                    t.setNom(rs.getString("nom"));
                    int idBus = rs.getInt("id_bus");
                    if (!rs.wasNull()) t.setIdBus(idBus);
                    t.setIdEcole(rs.getInt("id_ecole"));
                    java.sql.Time ht = rs.getTime("heure_depart");
                    if (ht != null) t.setHeureDepart(ht.toLocalTime());
                    t.setActif(rs.getBoolean("actif"));
                    t.setStatut(rs.getString("statut"));
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
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idEcole);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Trajet t = new Trajet();
                    t.setTrajetId(rs.getInt("id"));
                    t.setNom(rs.getString("nom"));
                    int idBus = rs.getInt("id_bus");
                    if (!rs.wasNull()) t.setIdBus(idBus);
                    t.setIdEcole(rs.getInt("id_ecole"));
                    java.sql.Time ht = rs.getTime("heure_depart");
                    if (ht != null) t.setHeureDepart(ht.toLocalTime());
                    t.setActif(rs.getBoolean("actif"));
                    t.setStatut(rs.getString("statut"));
                    list.add(t);
                }
            }
        }
        return list;
    }

    /** Affecte un bus au trajet. */
    public void updateIdBus(int trajetId, Integer busId) throws SQLException {
        String sql = "UPDATE trajet SET id_bus = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, busId);
            ps.setInt(2, trajetId);
            ps.executeUpdate();
        }
    }

}
