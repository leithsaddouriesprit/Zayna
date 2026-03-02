package tn.esprit.workshop.services;

import tn.esprit.workshop.model.Trajet;
import tn.esprit.workshop.utilis.MyBDConnexion;

import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class TrajetService {

    private Connection connection;

    public TrajetService() {
        connection = MyBDConnexion.getInstance().getConnection();
    }

    // Ajouter un trajet
    public void insertTrajet(Trajet trajet) throws SQLException {
        String req = "INSERT INTO trajet (ecole_id, nom_trajet, point_depart, point_arrivee, " +
                "heure_depart, heure_arrivee, jours, prix_mensuel, description, places_disponibles) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?)";

        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, trajet.getEcoleId());
        ps.setString(2, trajet.getNomTrajet());
        ps.setString(3, trajet.getPointDepart());
        ps.setString(4, trajet.getPointArrivee());
        ps.setTime(5, Time.valueOf(trajet.getHeureDepart()));
        ps.setTime(6, Time.valueOf(trajet.getHeureArrivee()));
        ps.setString(7, trajet.getJours());
        ps.setDouble(8, trajet.getPrixMensuel());
        ps.setString(9, trajet.getDescription());
        ps.setInt(10, trajet.getPlacesDisponibles());

        ps.executeUpdate();
    }

    // Modifier un trajet
    public void updateTrajet(Trajet trajet) throws SQLException {
        String req = "UPDATE trajet SET nom_trajet=?, point_depart=?, point_arrivee=?, " +
                "heure_depart=?, heure_arrivee=?, jours=?, prix_mensuel=?, description=?, places_disponibles=? " +
                "WHERE id=?";

        PreparedStatement ps = connection.prepareStatement(req);
        ps.setString(1, trajet.getNomTrajet());
        ps.setString(2, trajet.getPointDepart());
        ps.setString(3, trajet.getPointArrivee());
        ps.setTime(4, Time.valueOf(trajet.getHeureDepart()));
        ps.setTime(5, Time.valueOf(trajet.getHeureArrivee()));
        ps.setString(6, trajet.getJours());
        ps.setDouble(7, trajet.getPrixMensuel());
        ps.setString(8, trajet.getDescription());
        ps.setInt(9, trajet.getPlacesDisponibles());
        ps.setInt(10, trajet.getId());

        ps.executeUpdate();
    }

    // Supprimer un trajet
    public void deleteTrajet(int id) throws SQLException {
        String req = "DELETE FROM trajet WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    // Lister tous les trajets
    public List<Trajet> selectAllTrajets() throws SQLException {
        List<Trajet> trajets = new ArrayList<>();
        String req = "SELECT * FROM trajet ORDER BY heure_depart";

        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            Trajet t = new Trajet();
            t.setId(rs.getInt("id"));
            t.setEcoleId(rs.getInt("ecole_id"));
            t.setNomTrajet(rs.getString("nom_trajet"));
            t.setPointDepart(rs.getString("point_depart"));
            t.setPointArrivee(rs.getString("point_arrivee"));
            t.setHeureDepart(rs.getTime("heure_depart").toLocalTime());
            t.setHeureArrivee(rs.getTime("heure_arrivee").toLocalTime());
            t.setJours(rs.getString("jours"));
            t.setPrixMensuel(rs.getDouble("prix_mensuel"));
            t.setDescription(rs.getString("description"));
            t.setPlacesDisponibles(rs.getInt("places_disponibles"));
            trajets.add(t);
        }
        return trajets;
    }

    // Lister les trajets d'une école spécifique
    public List<Trajet> selectTrajetsByEcole(int ecoleId) throws SQLException {
        List<Trajet> trajets = new ArrayList<>();
        String req = "SELECT * FROM trajet WHERE ecole_id = ? ORDER BY heure_depart";

        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, ecoleId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            Trajet t = new Trajet();
            t.setId(rs.getInt("id"));
            t.setEcoleId(rs.getInt("ecole_id"));
            t.setNomTrajet(rs.getString("nom_trajet"));
            t.setPointDepart(rs.getString("point_depart"));
            t.setPointArrivee(rs.getString("point_arrivee"));
            t.setHeureDepart(rs.getTime("heure_depart").toLocalTime());
            t.setHeureArrivee(rs.getTime("heure_arrivee").toLocalTime());
            t.setJours(rs.getString("jours"));
            t.setPrixMensuel(rs.getDouble("prix_mensuel"));
            t.setDescription(rs.getString("description"));
            t.setPlacesDisponibles(rs.getInt("places_disponibles"));
            trajets.add(t);
        }
        return trajets;
    }

    // Compter les trajets d'une école
    public int countTrajetsByEcole(int ecoleId) throws SQLException {
        String req = "SELECT COUNT(*) FROM trajet WHERE ecole_id = ?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, ecoleId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getInt(1);
        }
        return 0;
    }
}