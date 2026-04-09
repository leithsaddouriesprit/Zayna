package tn.esprit.workshop.services.leith;

import tn.esprit.workshop.model.leith.CandidatureEnfant;
import tn.esprit.workshop.utilis.MyBDConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CandidatureEnfantService {

    public static final String STATUT_ENVOYEE = "ENVOYEE";
    public static final String STATUT_ACCEPTEE = "ACCEPTEE";
    public static final String STATUT_REFUSEE = "REFUSEE";

    public CandidatureEnfantService() {
    }

    private Connection getConnection() throws SQLException {
        return MyBDConnexion.getInstance().getConnection();
    }

    public void insert(int parentId, int idEcole, String nomEnfant, String prenomEnfant, int age, double latitude, double longitude) throws SQLException {
        insert(parentId, idEcole, nomEnfant, prenomEnfant, age, latitude, longitude, null);
    }

    /**
     * Insère une candidature enfant ; {@code trajetId} optionnel (enregistré en base si non null et &gt; 0).
     */
    public void insert(int parentId, int idEcole, String nomEnfant, String prenomEnfant, int age, double latitude, double longitude, Integer trajetId) throws SQLException {
        String sql = "INSERT INTO candidature_enfant (parent_id, id_ecole, nom_enfant, prenom_enfant, age, latitude, longitude, statut, trajet_id, date_demande) VALUES (?, ?, ?, ?, ?, ?, ?, 'ENVOYEE', ?, CURRENT_TIMESTAMP)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, parentId);
            ps.setInt(2, idEcole);
            ps.setString(3, nomEnfant);
            ps.setString(4, prenomEnfant);
            ps.setInt(5, age);
            ps.setDouble(6, latitude);
            ps.setDouble(7, longitude);
            if (trajetId != null && trajetId > 0) {
                ps.setInt(8, trajetId);
            } else {
                ps.setNull(8, Types.INTEGER);
            }
            ps.executeUpdate();
        }
    }

    public List<CandidatureEnfant> findByParentId(int parentId) throws SQLException {
        String sql = "SELECT id, parent_id, id_ecole, nom_enfant, prenom_enfant, age, latitude, longitude, statut, trajet_id, date_demande FROM candidature_enfant WHERE parent_id = ? ORDER BY date_demande DESC";
        List<CandidatureEnfant> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, parentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /** Toutes les candidatures ENVOYEE (pour admin). */
    public List<CandidatureEnfant> findAllEnVoyee() throws SQLException {
        String sql = "SELECT id, parent_id, id_ecole, nom_enfant, prenom_enfant, age, latitude, longitude, statut, trajet_id, date_demande FROM candidature_enfant WHERE statut = 'ENVOYEE' ORDER BY date_demande DESC";
        List<CandidatureEnfant> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    /** Candidatures ENVOYEE pour l'école de l'agent. */
    public List<CandidatureEnfant> findEnVoyeeByEcoleId(int idEcole) throws SQLException {
        String sql = """
                SELECT ce.id, ce.parent_id, ce.id_ecole, ce.nom_enfant, ce.prenom_enfant, ce.age,
                       ce.latitude, ce.longitude, ce.statut, ce.trajet_id, ce.date_demande
                FROM candidature_enfant ce
                LEFT JOIN trajet t ON t.id = ce.trajet_id
                WHERE ce.statut = 'ENVOYEE'
                  AND (t.id_ecole = ? OR (ce.trajet_id IS NULL AND ce.id_ecole = ?))
                ORDER BY ce.date_demande DESC
                """;
        List<CandidatureEnfant> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, idEcole);
            ps.setInt(2, idEcole);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public void refuser(int candidatureEnfantId) throws SQLException {
        String sql = "UPDATE candidature_enfant SET statut = 'REFUSEE' WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, candidatureEnfantId);
            ps.executeUpdate();
        }
    }

    public void accepter(int candidatureEnfantId, int trajetId) throws SQLException {
        String sql = "UPDATE candidature_enfant SET statut = 'ACCEPTEE', trajet_id = ? WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, trajetId);
            ps.setInt(2, candidatureEnfantId);
            ps.executeUpdate();
        }
    }

    public CandidatureEnfant getById(int id) throws SQLException {
        String sql = "SELECT id, parent_id, id_ecole, nom_enfant, prenom_enfant, age, latitude, longitude, statut, trajet_id, date_demande FROM candidature_enfant WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    /**
     * Supprime la candidature si elle appartient au parent et n’est pas acceptée
     * (une candidature acceptée est liée à un enfant créé côté agent).
     */
    public boolean deleteByParent(int candidatureId, int parentId) throws SQLException {
        String sql = "DELETE FROM candidature_enfant WHERE id = ? AND parent_id = ? AND statut <> ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, candidatureId);
            ps.setInt(2, parentId);
            ps.setString(3, STATUT_ACCEPTEE);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Met à jour une candidature encore en attente (ENVOYEE) pour le parent concerné.
     *
     * @return {@code true} si une ligne a été mise à jour
     */
    public boolean updateEnVoyeeByParent(int candidatureId, int parentId,
                                         String nomEnfant, String prenomEnfant, int age,
                                         double latitude, double longitude, Integer trajetId) throws SQLException {
        String sql = "UPDATE candidature_enfant SET nom_enfant = ?, prenom_enfant = ?, age = ?, latitude = ?, longitude = ?, trajet_id = ? "
                + "WHERE id = ? AND parent_id = ? AND statut = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, nomEnfant);
            ps.setString(2, prenomEnfant);
            ps.setInt(3, age);
            ps.setDouble(4, latitude);
            ps.setDouble(5, longitude);
            if (trajetId != null && trajetId > 0) {
                ps.setInt(6, trajetId);
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            ps.setInt(7, candidatureId);
            ps.setInt(8, parentId);
            ps.setString(9, STATUT_ENVOYEE);
            return ps.executeUpdate() > 0;
        }
    }

    private CandidatureEnfant mapRow(ResultSet rs) throws SQLException {
        CandidatureEnfant c = new CandidatureEnfant();
        c.setId(rs.getInt("id"));
        c.setParentId(rs.getInt("parent_id"));
        c.setIdEcole(rs.getInt("id_ecole"));
        c.setNomEnfant(rs.getString("nom_enfant"));
        c.setPrenomEnfant(rs.getString("prenom_enfant"));
        c.setAge(rs.getInt("age"));
        c.setLatitude(rs.getDouble("latitude"));
        c.setLongitude(rs.getDouble("longitude"));
        c.setStatut(rs.getString("statut"));
        int trajetId = rs.getInt("trajet_id");
        c.setTrajetId(rs.wasNull() ? null : trajetId);
        Timestamp ts = rs.getTimestamp("date_demande");
        if (ts != null) c.setDateDemande(ts.toLocalDateTime());
        return c;
    }
}
