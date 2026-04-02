package tn.esprit.workshop.services;

import tn.esprit.workshop.model.DemandeInscription;
import tn.esprit.workshop.model.Enfant;
import tn.esprit.workshop.utilis.MyBDConnexion;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DemandeService {

    private Connection connection;

    public DemandeService() {
        connection = MyBDConnexion.getInstance().getConnection();
    }

    // ===================== CRUD DEMANDES =====================

    public void ajouterDemande(DemandeInscription demande) throws SQLException {
        String req = "INSERT INTO demande_inscription (parent_nom, parent_prenom, parent_email, parent_telephone, "
                + "enfant_nom, enfant_prenom, enfant_date_naissance, niveau_scolaire, ecole_id, trajet_id, "
                + "date_demande, statut) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";

        PreparedStatement ps = connection.prepareStatement(req);
        ps.setString(1, demande.getParentNom());
        ps.setString(2, demande.getParentPrenom());
        ps.setString(3, demande.getParentEmail());
        ps.setString(4, demande.getParentTelephone());
        ps.setString(5, demande.getEnfantNom());
        ps.setString(6, demande.getEnfantPrenom());
        ps.setDate(7, Date.valueOf(demande.getEnfantDateNaissance()));
        ps.setString(8, demande.getNiveauScolaire());
        ps.setInt(9, demande.getEcoleId());

        if (demande.getTrajetId() > 0) {
            ps.setInt(10, demande.getTrajetId());
        } else {
            ps.setNull(10, Types.INTEGER);
        }

        ps.setTimestamp(11, Timestamp.valueOf(demande.getDateDemande()));
        ps.setString(12, demande.getStatut());

        ps.executeUpdate();
    }

    public List<DemandeInscription> selectDemandesEnAttente() throws SQLException {
        List<DemandeInscription> demandes = new ArrayList<>();
        String req = "SELECT * FROM demande_inscription WHERE statut = 'EN_ATTENTE' ORDER BY date_demande ASC";

        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            demandes.add(extractDemande(rs));
        }
        return demandes;
    }

    public List<DemandeInscription> selectDemandesByEcole(int ecoleId) throws SQLException {
        List<DemandeInscription> demandes = new ArrayList<>();
        String req = "SELECT * FROM demande_inscription WHERE ecole_id = ? AND statut = 'EN_ATTENTE' ORDER BY date_demande ASC";

        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, ecoleId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            demandes.add(extractDemande(rs));
        }
        return demandes;
    }

    public List<DemandeInscription> selectDemandesByEcoleAndStatut(int ecoleId, String statut) throws SQLException {
        List<DemandeInscription> demandes = new ArrayList<>();
        String req = "SELECT * FROM demande_inscription WHERE ecole_id = ? AND statut = ? ORDER BY date_demande DESC";

        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, ecoleId);
        ps.setString(2, statut);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            demandes.add(extractDemande(rs));
        }
        return demandes;
    }

    public List<DemandeInscription> selectDemandesByStatut(String statut) throws SQLException {
        List<DemandeInscription> demandes = new ArrayList<>();
        String req = "SELECT * FROM demande_inscription WHERE statut = ? ORDER BY date_demande DESC";

        PreparedStatement ps = connection.prepareStatement(req);
        ps.setString(1, statut);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            demandes.add(extractDemande(rs));
        }
        return demandes;
    }

    public void accepterDemande(int demandeId, String commentaire, EnfantService enfantService) throws SQLException {
        // Récupérer la demande
        String selectReq = "SELECT * FROM demande_inscription WHERE id = ?";
        PreparedStatement psSelect = connection.prepareStatement(selectReq);
        psSelect.setInt(1, demandeId);
        ResultSet rs = psSelect.executeQuery();

        if (rs.next()) {
            DemandeInscription demande = extractDemande(rs);

            // Créer l'enfant dans la table enfant
            Enfant enfant = new Enfant();
            enfant.setNom(demande.getEnfantNom());
            enfant.setPrenom(demande.getEnfantPrenom());
            enfant.setDateNaissance(demande.getEnfantDateNaissance());
            enfant.setEcoleId(demande.getEcoleId());
            enfant.setClasse(demande.getNiveauScolaire());
            enfant.setNumTelephoneParent(demande.getParentTelephone());
            enfant.setEmailParent(demande.getParentEmail());
            enfant.setStatut("ACTIF");
            enfant.setDateInscription(LocalDate.now());

            enfantService.insertEnfant(enfant);

            // Mettre à jour le statut de la demande
            String updateReq = "UPDATE demande_inscription SET statut = 'ACCEPTEE', commentaire_agent = ?, date_traitement = ? WHERE id = ?";
            PreparedStatement psUpdate = connection.prepareStatement(updateReq);
            psUpdate.setString(1, commentaire);
            psUpdate.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            psUpdate.setInt(3, demandeId);
            psUpdate.executeUpdate();
        }
    }

    public void refuserDemande(int demandeId, String commentaire) throws SQLException {
        String req = "UPDATE demande_inscription SET statut = 'REFUSEE', commentaire_agent = ?, date_traitement = ? WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setString(1, commentaire);
        ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
        ps.setInt(3, demandeId);
        ps.executeUpdate();
    }

    private DemandeInscription extractDemande(ResultSet rs) throws SQLException {
        DemandeInscription demande = new DemandeInscription();
        demande.setId(rs.getInt("id"));
        demande.setParentNom(rs.getString("parent_nom"));
        demande.setParentPrenom(rs.getString("parent_prenom"));
        demande.setParentEmail(rs.getString("parent_email"));
        demande.setParentTelephone(rs.getString("parent_telephone"));
        demande.setEnfantNom(rs.getString("enfant_nom"));
        demande.setEnfantPrenom(rs.getString("enfant_prenom"));
        demande.setEnfantDateNaissance(rs.getDate("enfant_date_naissance").toLocalDate());
        demande.setNiveauScolaire(rs.getString("niveau_scolaire"));
        demande.setEcoleId(rs.getInt("ecole_id"));
        demande.setTrajetId(rs.getInt("trajet_id"));
        demande.setDateDemande(rs.getTimestamp("date_demande").toLocalDateTime());
        demande.setStatut(rs.getString("statut"));
        demande.setCommentaireAgent(rs.getString("commentaire_agent"));
        if (rs.getTimestamp("date_traitement") != null) {
            demande.setDateTraitement(rs.getTimestamp("date_traitement").toLocalDateTime());
        }
        return demande;
    }
}