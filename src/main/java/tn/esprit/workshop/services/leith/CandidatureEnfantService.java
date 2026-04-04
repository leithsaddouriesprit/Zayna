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
        String sql = "SELECT id, parent_id, id_ecole, nom_enfant, prenom_enfant, age, latitude, longitude, statut, trajet_id, date_demande FROM candidature_enfant WHERE id_ecole = ? AND statut = 'ENVOYEE' ORDER BY date_demande DESC";
        List<CandidatureEnfant> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, idEcole);
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
