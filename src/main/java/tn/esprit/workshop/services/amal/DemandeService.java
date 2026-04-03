package tn.esprit.workshop.services.amal;

import tn.esprit.workshop.model.amal.DemandeInscription;
import tn.esprit.workshop.utilis.MyBDConnexion;

import java.sql.*;

public class DemandeService {

    private final Connection connection;

    public DemandeService() throws SQLException {
        connection = MyBDConnexion.getInstance().getConnection();
    }

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
}
