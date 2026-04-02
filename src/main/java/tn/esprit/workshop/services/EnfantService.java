package tn.esprit.workshop.services;

import tn.esprit.workshop.model.Enfant;
import tn.esprit.workshop.model.Presence;
import tn.esprit.workshop.utilis.MyBDConnexion;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class EnfantService {

    private Connection connection;

    public EnfantService() {
        connection = MyBDConnexion.getInstance().getConnection();
    }

    // ===================== CRUD ENFANT =====================

    public void insertEnfant(Enfant enfant) throws SQLException {
        String req = "INSERT INTO enfant (nom, prenom, date_naissance, parent_id, ecole_id, classe, num_telephone_parent, email_parent, statut, date_inscription) VALUES (?,?,?,?,?,?,?,?,?,?)";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setString(1, enfant.getNom());
        ps.setString(2, enfant.getPrenom());
        ps.setDate(3, Date.valueOf(enfant.getDateNaissance()));
        ps.setInt(4, enfant.getParentId());
        ps.setInt(5, enfant.getEcoleId());
        ps.setString(6, enfant.getClasse());
        ps.setString(7, enfant.getNumTelephoneParent());
        ps.setString(8, enfant.getEmailParent());
        ps.setString(9, enfant.getStatut());
        ps.setDate(10, Date.valueOf(enfant.getDateInscription()));
        ps.executeUpdate();
    }

    public void updateEnfant(Enfant enfant) throws SQLException {
        String req = "UPDATE enfant SET nom=?, prenom=?, date_naissance=?, parent_id=?, ecole_id=?, classe=?, num_telephone_parent=?, email_parent=?, statut=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setString(1, enfant.getNom());
        ps.setString(2, enfant.getPrenom());
        ps.setDate(3, Date.valueOf(enfant.getDateNaissance()));
        ps.setInt(4, enfant.getParentId());
        ps.setInt(5, enfant.getEcoleId());
        ps.setString(6, enfant.getClasse());
        ps.setString(7, enfant.getNumTelephoneParent());
        ps.setString(8, enfant.getEmailParent());
        ps.setString(9, enfant.getStatut());
        ps.setInt(10, enfant.getId());
        ps.executeUpdate();
    }

    public void deleteEnfant(int id) throws SQLException {
        String req = "DELETE FROM enfant WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    public List<Enfant> selectAllEnfants() throws SQLException {
        List<Enfant> enfants = new ArrayList<>();
        String req = "SELECT * FROM enfant ORDER BY nom, prenom";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            Enfant e = new Enfant();
            e.setId(rs.getInt("id"));
            e.setNom(rs.getString("nom"));
            e.setPrenom(rs.getString("prenom"));
            e.setDateNaissance(rs.getDate("date_naissance").toLocalDate());
            e.setParentId(rs.getInt("parent_id"));
            e.setEcoleId(rs.getInt("ecole_id"));
            e.setClasse(rs.getString("classe"));
            e.setNumTelephoneParent(rs.getString("num_telephone_parent"));
            e.setEmailParent(rs.getString("email_parent"));
            e.setStatut(rs.getString("statut"));
            e.setDateInscription(rs.getDate("date_inscription").toLocalDate());
            enfants.add(e);
        }
        return enfants;
    }

    public List<Enfant> selectEnfantsByEcole(int ecoleId) throws SQLException {
        List<Enfant> enfants = new ArrayList<>();
        String req = "SELECT * FROM enfant WHERE ecole_id = ? ORDER BY nom, prenom";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, ecoleId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            Enfant e = new Enfant();
            e.setId(rs.getInt("id"));
            e.setNom(rs.getString("nom"));
            e.setPrenom(rs.getString("prenom"));
            e.setDateNaissance(rs.getDate("date_naissance").toLocalDate());
            e.setParentId(rs.getInt("parent_id"));
            e.setEcoleId(rs.getInt("ecole_id"));
            e.setClasse(rs.getString("classe"));
            e.setNumTelephoneParent(rs.getString("num_telephone_parent"));
            e.setEmailParent(rs.getString("email_parent"));
            e.setStatut(rs.getString("statut"));
            e.setDateInscription(rs.getDate("date_inscription").toLocalDate());
            enfants.add(e);
        }
        return enfants;
    }

    // ===================== PRÉSENCE =====================

    public void ajouterPresence(Presence presence) throws SQLException {
        String req = "INSERT INTO presence (enfant_id, date, statut_matin, heure_arrivee, statut_midi, heure_depart, statut_soir, remorque_enfant, remorqueur, observations) VALUES (?,?,?,?,?,?,?,?,?,?)";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, presence.getEnfantId());
        ps.setDate(2, Date.valueOf(presence.getDate()));
        ps.setString(3, presence.getStatutMatin());
        ps.setTime(4, presence.getHeureArrivee() != null ? Time.valueOf(presence.getHeureArrivee()) : null);
        ps.setString(5, presence.getStatutMidi());
        ps.setTime(6, presence.getHeureDepart() != null ? Time.valueOf(presence.getHeureDepart()) : null);
        ps.setString(7, presence.getStatutSoir());
        ps.setBoolean(8, presence.isRemorqueEnfant());
        ps.setString(9, presence.getRemorqueur());
        ps.setString(10, presence.getObservations());
        ps.executeUpdate();
    }

    public List<Presence> selectPresenceByEnfant(int enfantId) throws SQLException {
        List<Presence> presences = new ArrayList<>();
        String req = "SELECT * FROM presence WHERE enfant_id = ? ORDER BY date DESC";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, enfantId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            Presence p = new Presence();
            p.setId(rs.getInt("id"));
            p.setEnfantId(rs.getInt("enfant_id"));
            p.setDate(rs.getDate("date").toLocalDate());
            p.setStatutMatin(rs.getString("statut_matin"));
            p.setHeureArrivee(rs.getTime("heure_arrivee") != null ? rs.getTime("heure_arrivee").toLocalTime() : null);
            p.setStatutMidi(rs.getString("statut_midi"));
            p.setHeureDepart(rs.getTime("heure_depart") != null ? rs.getTime("heure_depart").toLocalTime() : null);
            p.setStatutSoir(rs.getString("statut_soir"));
            p.setRemorqueEnfant(rs.getBoolean("remorque_enfant"));
            p.setRemorqueur(rs.getString("remorqueur"));
            p.setObservations(rs.getString("observations"));
            presences.add(p);
        }
        return presences;
    }
}
