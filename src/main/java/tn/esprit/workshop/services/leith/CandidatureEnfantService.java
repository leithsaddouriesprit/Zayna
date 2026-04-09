package tn.esprit.workshop.services.leith;

import tn.esprit.workshop.model.leith.CandidatureEnfant;
import tn.esprit.workshop.model.leith.Trajet;
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

    /**
     * Accepte la candidature et crée ou met à jour l’enfant dans {@code enfant} (même transaction).
     * Évite l’état incohérent « candidature ACCEPTEE sans ligne enfant » si l’insert échouait après l’UPDATE.
     *
     * @param idEcole école de l’agent (contrôle de périmètre ; le trajet doit appartenir à cette école)
     */
    public void accepter(int candidatureEnfantId, int trajetId, int idEcole) throws SQLException {
        if (trajetId <= 0) {
            throw new SQLException("Un trajet valide est obligatoire pour accepter la candidature.");
        }
        CandidatureEnfant ce = getById(candidatureEnfantId);
        if (ce == null) {
            throw new SQLException("Candidature introuvable.");
        }
        if (!STATUT_ENVOYEE.equals(ce.getStatut())) {
            throw new SQLException("Cette candidature a déjà été traitée.");
        }
        if (ce.getIdEcole() != idEcole) {
            throw new SQLException("Candidature hors périmètre de votre école.");
        }
        TrajetService trajetService = new TrajetService();
        Trajet trajet = trajetService.getById(trajetId);
        if (trajet == null || trajet.getIdEcole() != idEcole) {
            throw new SQLException("Le trajet sélectionné est introuvable ou n’appartient pas à votre école.");
        }

        String nom = ce.getNomEnfant() != null ? ce.getNomEnfant().trim() : "";
        String prenom = ce.getPrenomEnfant() != null ? ce.getPrenomEnfant().trim() : "";
        if (nom.isEmpty() || prenom.isEmpty()) {
            throw new SQLException("Le nom et le prénom de l’enfant sur la candidature sont obligatoires.");
        }

        Connection conn = getConnection();
        boolean prevAuto = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE candidature_enfant SET statut = 'ACCEPTEE', trajet_id = ? WHERE id = ? AND statut = ?")) {
                ps.setInt(1, trajetId);
                ps.setInt(2, candidatureEnfantId);
                ps.setString(3, STATUT_ENVOYEE);
                if (ps.executeUpdate() != 1) {
                    throw new SQLException("Impossible d’accepter cette candidature (déjà traitée ou introuvable).");
                }
            }
            Integer existingEnfantId = null;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id FROM enfant WHERE parent_id = ? AND nom = ? AND prenom = ?")) {
                ps.setInt(1, ce.getParentId());
                ps.setString(2, nom);
                ps.setString(3, prenom);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        existingEnfantId = rs.getInt("id");
                    }
                }
            }
            if (existingEnfantId != null) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE enfant SET trajet_id = ?, actif = 1, on_board = 0 WHERE id = ?")) {
                    ps.setInt(1, trajetId);
                    ps.setInt(2, existingEnfantId);
                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("Mise à jour de l’enfant impossible.");
                    }
                }
            } else {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO enfant (nom, prenom, parent_id, trajet_id, actif, on_board) VALUES (?, ?, ?, ?, 1, 0)")) {
                    ps.setString(1, nom);
                    ps.setString(2, prenom);
                    ps.setInt(3, ce.getParentId());
                    ps.setInt(4, trajetId);
                    ps.executeUpdate();
                }
            }
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(prevAuto);
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
