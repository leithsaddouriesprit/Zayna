package tn.esprit.workshop.services.leith;

import tn.esprit.workshop.model.leith.Candidature;
import tn.esprit.workshop.utilis.MyBDConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Candidature: une par chauffeur.
 * Statuts utilisés : ENVOYEE, ACCEPTEE, REFUSEE.
 */
public class CandidatureService {

    public static final String STATUT_ENVOYEE = "ENVOYEE";
    public static final String STATUT_ACCEPTEE = "ACCEPTEE";
    public static final String STATUT_REFUSEE = "REFUSEE";

    private final Connection connection;

    public CandidatureService() {
        this.connection = MyBDConnexion.getInstance().getConnection();
    }

    /**
     * Retourne la candidature unique de ce chauffeur, ou null.
     * Joint la table chauffeur pour enrichir le modèle (nom/prenom) côté suivi.
     */
    public Candidature findByChauffeurId(int chauffeurId) throws SQLException {
        String sql =
                "SELECT c.id, c.chauffeur_id, c.statut, c.date_envoi, c.maladie, " +
                        "ch.nom, ch.prenom, c.permis_recto " +
                        "FROM candidature c " +
                        "JOIN chauffeur ch ON c.chauffeur_id = ch.id " +
                        "WHERE c.chauffeur_id = ?";
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
     * Liste des candidatures chauffeur à traiter par l'agent (statut = ENVOYEE).
     * Requête conforme à :
     * SELECT c.*, ch.nom, ch.prenom, ch.age, ch.nb_ans_experience
     * FROM candidature c
     * JOIN chauffeur ch ON c.chauffeur_id = ch.id
     * WHERE c.statut = 'ENVOYEE'
     */
    public List<Candidature> findAllEnVoyee() throws SQLException {
        String sql =
                "SELECT c.id, c.chauffeur_id, c.statut, c.date_envoi, c.maladie, " +
                        "ch.nom, ch.prenom, ch.age, ch.nb_ans_experience " +
                        "FROM candidature c " +
                        "JOIN chauffeur ch ON c.chauffeur_id = ch.id " +
                        "WHERE c.statut = 'ENVOYEE' " +
                        "ORDER BY c.date_envoi DESC";
        List<Candidature> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** Liste des candidatures acceptées (statut = ACCEPTEE) avec infos chauffeur. */
    public List<Candidature> findAllAcceptees() throws SQLException {
        String sql =
                "SELECT c.id, c.chauffeur_id, c.statut, c.date_envoi, c.maladie, " +
                        "ch.nom, ch.prenom, ch.age, ch.nb_ans_experience " +
                        "FROM candidature c " +
                        "JOIN chauffeur ch ON c.chauffeur_id = ch.id " +
                        "WHERE c.statut = ? " +
                        "ORDER BY c.date_envoi DESC";
        List<Candidature> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, STATUT_ACCEPTEE);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    private Candidature mapRow(ResultSet rs) throws SQLException {
        Candidature c = new Candidature();
        c.setId(rs.getInt("id"));
        c.setChauffeurId(rs.getInt("chauffeur_id"));
        c.setStatut(rs.getString("statut"));
        Timestamp ts = rs.getTimestamp("date_envoi");
        if (ts != null) c.setDateEnvoi(ts.toLocalDateTime());
        c.setMaladie(rs.getString("maladie"));
        c.setNom(rs.getString("nom"));
        c.setPrenom(rs.getString("prenom"));
        c.setAge(rs.getInt("age"));
        c.setNbAnsExperience(rs.getInt("nb_ans_experience"));
        return c;
    }

    public void accepter(int candidatureId) throws SQLException {
        String sql = "UPDATE candidature SET statut = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, STATUT_ACCEPTEE);
            ps.setInt(2, candidatureId);
            ps.executeUpdate();
        }
    }

    public void refuser(int candidatureId) throws SQLException {
        String sql = "UPDATE candidature SET statut = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, STATUT_REFUSEE);
            ps.setInt(2, candidatureId);
            ps.executeUpdate();
        }
    }

    /**
     * Charge recto + verso pour affichage permis (popup).
     * Les contrôleurs créent ensuite des Image JavaFX via ByteArrayInputStream.
     */
    public byte[][] getPermisRectoVerso(int candidatureId) throws SQLException {
        String sql = "SELECT permis_recto, permis_verso FROM candidature WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, candidatureId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new byte[][]{
                            rs.getBytes("permis_recto"),
                            rs.getBytes("permis_verso")
                    };
                }
            }
        }
        return null;
    }

    /**
     * Annulation côté chauffeur : on repasse la candidature en REFUSEE.
     * (Pas de statut ANNULEE pour respecter ENVOYEE/ACCEPTEE/REFUSEE uniquement.)
     */
    public void annuler(int chauffeurId) throws SQLException {
        String sql = "UPDATE candidature SET statut = ? WHERE chauffeur_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, STATUT_REFUSEE);
            ps.setInt(2, chauffeurId);
            ps.executeUpdate();
        }
    }

    /**
     * Réactivation : remet la candidature en ENVOYEE et met à jour les documents.
     * Utilisée quand une candidature REFUSEE est resoumise avec de nouveaux fichiers.
     */
    public void updateToEnAttenteWithDocuments(int chauffeurId,
                                               byte[] rectoBytes, String rectoName, String rectoMime,
                                               byte[] versoBytes, String versoName, String versoMime,
                                               String maladie) throws SQLException {
        String sql = "UPDATE candidature SET statut = ?, date_envoi = CURRENT_TIMESTAMP, maladie = ?, " +
                "permis_recto = ?, permis_recto_nom = ?, permis_recto_mime = ?, " +
                "permis_verso = ?, permis_verso_nom = ?, permis_verso_mime = ? WHERE chauffeur_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, STATUT_ENVOYEE);
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

