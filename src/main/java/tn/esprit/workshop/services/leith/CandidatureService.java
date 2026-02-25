package tn.esprit.workshop.services.leith;

import tn.esprit.workshop.model.leith.Candidature;
import tn.esprit.workshop.utilis.MyBDConnexion;

import java.sql.*;

/**
 * Candidature: one per chauffeur. Statuts: EN_ATTENTE, ACCEPTEE, REFUSEE, ANNULEE.
 */
public class CandidatureService {

    private static final String STATUT_EN_ATTENTE = "EN_ATTENTE";
    private static final String STATUT_ANNULEE = "ANNULEE";

    private final Connection connection;

    public CandidatureService() {
        this.connection = MyBDConnexion.getInstance().getConnection();
    }

    /**
     * Returns the unique candidature for this chauffeur, or null.
     */
    public Candidature findByChauffeurId(int chauffeurId) throws SQLException {
        String sql = "SELECT c.id, c.chauffeur_id, c.statut, c.date_envoi, c.maladie, ch.nom, ch.prenom, c.permis_recto " +
                "FROM candidature c JOIN chauffeur ch ON c.chauffeur_id = ch.id WHERE c.chauffeur_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, chauffeurId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Candidature c = new Candidature();
                    c.setId(rs.getInt("id"));
                    c.setChauffeurId(rs.getInt("chauffeur_id"));
                    c.setStatut(rs.getString("statut"));
                    Timestamp ts = rs.getTimestamp("date_envoi");
                    if (ts != null) c.setDateEnvoi(ts.toLocalDateTime());
                    c.setMaladie(rs.getString("maladie"));
                    c.setNom(rs.getString("nom"));
                    c.setPrenom(rs.getString("prenom"));
                    c.setPermisRecto(rs.getBytes("permis_recto"));
                    return c;
                }
            }
        }
        return null;
    }

    /**
     * Set statut to ANNULEE. Does not delete the row.
     */
    public void annuler(int chauffeurId) throws SQLException {
        String sql = "UPDATE candidature SET statut = ? WHERE chauffeur_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, STATUT_ANNULEE);
            ps.setInt(2, chauffeurId);
            ps.executeUpdate();
        }
    }

    /**
     * Reactivate: set statut to EN_ATTENTE and update documents (for REFUSEE/ANNULEE).
     */
    public void updateToEnAttenteWithDocuments(int chauffeurId, byte[] rectoBytes, String rectoName, String rectoMime,
                                               byte[] versoBytes, String versoName, String versoMime, String maladie) throws SQLException {
        String sql = "UPDATE candidature SET statut = ?, date_envoi = CURRENT_TIMESTAMP, maladie = ?, " +
                "permis_recto = ?, permis_recto_nom = ?, permis_recto_mime = ?, " +
                "permis_verso = ?, permis_verso_nom = ?, permis_verso_mime = ? WHERE chauffeur_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, STATUT_EN_ATTENTE);
            ps.setString(2, maladie);
            ps.setBytes(3, rectoBytes);
            ps.setString(4, rectoName);
            ps.setString(5, rectoMime);
            ps.setBytes(6, versoBytes);
            ps.setString(7, versoName);
            ps.setString(8, versoMime);
            ps.setInt(9, chauffeurId);
            ps.executeUpdate();
        }
    }
}
