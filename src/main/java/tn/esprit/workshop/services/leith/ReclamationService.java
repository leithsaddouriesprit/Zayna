package tn.esprit.workshop.services.leith;

import tn.esprit.workshop.model.leith.Reclamation;
import tn.esprit.workshop.utilis.MyBDConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Accès JDBC à {@code reclamation}.
 */
public class ReclamationService {

    public static final String STATUT_NOUVELLE = "NOUVELLE";
    public static final String STATUT_EN_COURS = "EN_COURS";
    public static final String STATUT_REPONDUE = "REPONDUE";
    public static final String STATUT_FERMEE = "FERMEE";
    public static final String STATUT_REJETEE = "REJETEE";

    public static final String FILTRE_STATUT_TOUS = "TOUS";
    public static final String FILTRE_CATEGORIE_TOUTES = "TOUTES";
    public static final String FILTRE_PRIORITE_TOUTES = "TOUTES";
    public static final String FILTRE_ROLE_CREATEUR_TOUS = "TOUS";
    public static final String DATE_PRESET_ALL = "ALL";
    public static final String DATE_PRESET_TODAY = "TODAY";
    public static final String DATE_PRESET_WEEK = "WEEK";
    public static final String DATE_PRESET_MONTH = "MONTH";
    public static final String SORT_RECENT = "RECENT";
    public static final String SORT_OLDEST = "OLDEST";
    public static final String SORT_PRIORITY_HIGH = "PRIORITY_HIGH";

    private static final String SELECT_BASE = "SELECT id, objet, description, categorie, priorite, statut, role_createur, user_id, "
            + "id_parent, id_chauffeur, id_maitresse, id_ecole, id_bus, id_trajet, date_creation, date_modification FROM reclamation ";

    private Connection getConnection() throws SQLException {
        return MyBDConnexion.getInstance().getConnection();
    }

    /** Statistiques KPI pour un périmètre (admin / école / utilisateur). */
    public static final class DashboardStats {
        public int total;
        public int nouvelles;
        public int enCours;
        public int repondues;
        public int fermees;
        public int rejetees;
        public int urgentes;
    }

    /**
     * Compte les réclamations par statut et urgentes dans le même périmètre que la liste.
     */
    public DashboardStats getDashboardStats(String scopeRole, Integer userId, Integer idEcole) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) AS total, "
                        + "SUM(CASE WHEN statut = 'NOUVELLE' THEN 1 ELSE 0 END) AS n_nouv, "
                        + "SUM(CASE WHEN statut = 'EN_COURS' THEN 1 ELSE 0 END) AS n_enc, "
                        + "SUM(CASE WHEN statut = 'REPONDUE' THEN 1 ELSE 0 END) AS n_rep, "
                        + "SUM(CASE WHEN statut = 'FERMEE' THEN 1 ELSE 0 END) AS n_ferm, "
                        + "SUM(CASE WHEN statut = 'REJETEE' THEN 1 ELSE 0 END) AS n_rej, "
                        + "SUM(CASE WHEN priorite = 'URGENTE' THEN 1 ELSE 0 END) AS n_urg "
                        + "FROM reclamation WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        appendScopeClause(sql, params, scopeRole, userId, idEcole);

        DashboardStats d = new DashboardStats();
        try (PreparedStatement ps = getConnection().prepareStatement(sql.toString())) {
            bindParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    d.total = rs.getInt("total");
                    d.nouvelles = rs.getInt("n_nouv");
                    d.enCours = rs.getInt("n_enc");
                    d.repondues = rs.getInt("n_rep");
                    d.fermees = rs.getInt("n_ferm");
                    d.rejetees = rs.getInt("n_rej");
                    d.urgentes = rs.getInt("n_urg");
                }
            }
        }
        return d;
    }

    /**
     * Nombre de réponses par id de réclamation (une requête pour la liste visible).
     */
    public Map<Integer, Integer> getReplyCountsForReclamationIds(List<Integer> ids) throws SQLException {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        StringBuilder in = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) {
                in.append(',');
            }
            in.append('?');
        }
        String sql = "SELECT reclamation_id, COUNT(*) AS n FROM reponse_reclamation WHERE reclamation_id IN ("
                + in + ") GROUP BY reclamation_id";
        Map<Integer, Integer> map = new HashMap<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            for (int i = 0; i < ids.size(); i++) {
                ps.setInt(i + 1, ids.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getInt("reclamation_id"), rs.getInt("n"));
                }
            }
        }
        return map;
    }

    public int getReplyCountForReclamation(int reclamationId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM reponse_reclamation WHERE reclamation_id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, reclamationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public void ajouter(Reclamation r) throws SQLException {
        String sql = "INSERT INTO reclamation (objet, description, categorie, priorite, statut, role_createur, user_id, "
                + "id_parent, id_chauffeur, id_maitresse, id_ecole, id_bus, id_trajet) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, r.getObjet());
            ps.setString(2, r.getDescription());
            ps.setString(3, r.getCategorie());
            ps.setString(4, r.getPriorite());
            ps.setString(5, r.getStatut() != null ? r.getStatut() : STATUT_NOUVELLE);
            ps.setString(6, r.getRoleCreateur());
            ps.setInt(7, r.getUserId());
            setNullableInt(ps, 8, r.getIdParent());
            setNullableInt(ps, 9, r.getIdChauffeur());
            setNullableInt(ps, 10, r.getIdMaitresse());
            setNullableInt(ps, 11, r.getIdEcole());
            setNullableInt(ps, 12, r.getIdBus());
            setNullableInt(ps, 13, r.getIdTrajet());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    r.setId(keys.getInt(1));
                }
            }
        }
    }

    private static void setNullableInt(PreparedStatement ps, int i, Integer v) throws SQLException {
        if (v == null) {
            ps.setNull(i, Types.INTEGER);
        } else {
            ps.setInt(i, v);
        }
    }

    public boolean modifierStatut(int reclamationId, String nouveauStatut) throws SQLException {
        String sql = "UPDATE reclamation SET statut = ? WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, nouveauStatut);
            ps.setInt(2, reclamationId);
            return ps.executeUpdate() > 0;
        }
    }

    public Reclamation getById(int id) throws SQLException {
        String sql = SELECT_BASE + "WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToReclamation(rs);
                }
            }
        }
        return null;
    }

    public List<Reclamation> getAll() throws SQLException {
        List<Reclamation> list = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY date_creation DESC";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToReclamation(rs));
            }
        }
        return list;
    }

    public List<Reclamation> getByUser(int userId) throws SQLException {
        List<Reclamation> list = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE user_id = ? ORDER BY date_creation DESC";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToReclamation(rs));
                }
            }
        }
        return list;
    }

    public List<Reclamation> getByEcole(int idEcole) throws SQLException {
        List<Reclamation> list = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE id_ecole = ? ORDER BY date_creation DESC";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, idEcole);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToReclamation(rs));
                }
            }
        }
        return list;
    }

    /**
     * Version niveau 1 conservée : délègue à la recherche avancée avec tri récent et sans filtres extra.
     */
    public List<Reclamation> rechercherEtFiltrer(String motCle, String statut, String categorie,
                                                 String roleUtilisateur, Integer userId, Integer idEcole) throws SQLException {
        return rechercherEtFiltrerAvance(
                motCle,
                statut,
                categorie,
                FILTRE_PRIORITE_TOUTES,
                FILTRE_ROLE_CREATEUR_TOUS,
                DATE_PRESET_ALL,
                SORT_RECENT,
                roleUtilisateur,
                userId,
                idEcole);
    }

    /**
     * Recherche objet + description, filtres optionnels (valeurs “TOUS” / “TOUTES” ignorées), période, tri.
     */
    public List<Reclamation> rechercherEtFiltrerAvance(
            String motCle,
            String statut,
            String categorie,
            String priorite,
            String roleCreateur,
            String datePreset,
            String sortMode,
            String scopeRole,
            Integer userId,
            Integer idEcole) throws SQLException {

        StringBuilder sql = new StringBuilder(SELECT_BASE).append("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        appendScopeClause(sql, params, scopeRole, userId, idEcole);

        if (motCle != null && !motCle.isBlank()) {
            String like = "%" + motCle.trim() + "%";
            sql.append("AND (objet LIKE ? OR description LIKE ?) ");
            params.add(like);
            params.add(like);
        }

        if (statut != null && !statut.isBlank() && !FILTRE_STATUT_TOUS.equalsIgnoreCase(statut.trim())) {
            sql.append("AND statut = ? ");
            params.add(statut.trim());
        }
        if (categorie != null && !categorie.isBlank() && !FILTRE_CATEGORIE_TOUTES.equalsIgnoreCase(categorie.trim())) {
            sql.append("AND categorie = ? ");
            params.add(categorie.trim());
        }
        if (priorite != null && !priorite.isBlank() && !FILTRE_PRIORITE_TOUTES.equalsIgnoreCase(priorite.trim())) {
            sql.append("AND priorite = ? ");
            params.add(priorite.trim());
        }
        if (roleCreateur != null && !roleCreateur.isBlank() && !FILTRE_ROLE_CREATEUR_TOUS.equalsIgnoreCase(roleCreateur.trim())) {
            sql.append("AND role_createur = ? ");
            params.add(roleCreateur.trim());
        }

        appendDatePresetClause(sql, params, datePreset);

        appendSortClause(sql, sortMode);

        List<Reclamation> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql.toString())) {
            bindParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToReclamation(rs));
                }
            }
        }
        return list;
    }

    private static void bindParams(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            ps.setObject(i + 1, params.get(i));
        }
    }

    /**
     * Périmètre : ADMIN = tout ; AGENT_ECOLE = id_ecole ; sinon user_id pour créateurs.
     */
    private void appendScopeClause(StringBuilder sql, List<Object> params, String scopeRole, Integer userId, Integer idEcole) {
        if (scopeRole == null) {
            return;
        }
        if ("ADMIN".equals(scopeRole)) {
            return;
        }
        if ("AGENT_ECOLE".equals(scopeRole)) {
            if (idEcole != null) {
                sql.append("AND id_ecole = ? ");
                params.add(idEcole);
            }
            return;
        }
        if (userId != null && ("PARENT".equals(scopeRole) || "CHAUFFEUR".equals(scopeRole) || "MAITRESSE".equals(scopeRole))) {
            sql.append("AND user_id = ? ");
            params.add(userId);
        }
    }

    private void appendDatePresetClause(StringBuilder sql, List<Object> params, String preset) {
        if (preset == null || DATE_PRESET_ALL.equalsIgnoreCase(preset)) {
            return;
        }
        if (DATE_PRESET_TODAY.equalsIgnoreCase(preset)) {
            sql.append("AND DATE(date_creation) = CURDATE() ");
            return;
        }
        if (DATE_PRESET_WEEK.equalsIgnoreCase(preset)) {
            sql.append("AND date_creation >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) ");
            return;
        }
        if (DATE_PRESET_MONTH.equalsIgnoreCase(preset)) {
            sql.append("AND date_creation >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) ");
        }
    }

    private void appendSortClause(StringBuilder sql, String sortMode) {
        if (SORT_OLDEST.equalsIgnoreCase(sortMode)) {
            sql.append("ORDER BY date_creation ASC");
            return;
        }
        if (SORT_PRIORITY_HIGH.equalsIgnoreCase(sortMode)) {
            sql.append("ORDER BY CASE priorite ")
                    .append("WHEN 'URGENTE' THEN 1 ")
                    .append("WHEN 'HAUTE' THEN 2 ")
                    .append("WHEN 'MOYENNE' THEN 3 ")
                    .append("WHEN 'BASSE' THEN 4 ")
                    .append("ELSE 5 END, date_creation DESC");
            return;
        }
        sql.append("ORDER BY date_creation DESC");
    }

    public Integer findLikelyEcoleIdForParent(int parentId) throws SQLException {
        String sql = "SELECT id_ecole FROM candidature_enfant WHERE parent_id = ? AND statut = 'ACCEPTEE' "
                + "ORDER BY date_demande DESC LIMIT 1";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, parentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int e = rs.getInt("id_ecole");
                    return rs.wasNull() ? null : e;
                }
            }
        }
        return null;
    }

    public Reclamation mapResultSetToReclamation(ResultSet rs) throws SQLException {
        Reclamation r = new Reclamation();
        r.setId(rs.getInt("id"));
        r.setObjet(rs.getString("objet"));
        r.setDescription(rs.getString("description"));
        r.setCategorie(rs.getString("categorie"));
        r.setPriorite(rs.getString("priorite"));
        r.setStatut(rs.getString("statut"));
        r.setRoleCreateur(rs.getString("role_createur"));
        r.setUserId(rs.getInt("user_id"));
        int ip = rs.getInt("id_parent");
        r.setIdParent(rs.wasNull() ? null : ip);
        int ic = rs.getInt("id_chauffeur");
        r.setIdChauffeur(rs.wasNull() ? null : ic);
        int im = rs.getInt("id_maitresse");
        r.setIdMaitresse(rs.wasNull() ? null : im);
        int ie = rs.getInt("id_ecole");
        r.setIdEcole(rs.wasNull() ? null : ie);
        int ib = rs.getInt("id_bus");
        r.setIdBus(rs.wasNull() ? null : ib);
        int it = rs.getInt("id_trajet");
        r.setIdTrajet(rs.wasNull() ? null : it);
        r.setDateCreation(rs.getTimestamp("date_creation"));
        r.setDateModification(rs.getTimestamp("date_modification"));
        return r;
    }
}
