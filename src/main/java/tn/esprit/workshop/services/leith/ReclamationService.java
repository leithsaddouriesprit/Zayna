package tn.esprit.workshop.services.leith;

import tn.esprit.workshop.model.leith.Bus;
import tn.esprit.workshop.model.leith.Candidature;
import tn.esprit.workshop.model.leith.Reclamation;
import tn.esprit.workshop.model.leith.Trajet;
import tn.esprit.workshop.utilis.MyBDConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
            + "user_id_assigne, role_assigne, date_assignation, "
            + "id_parent, id_chauffeur, id_maitresse, id_ecole, id_bus, id_trajet, date_creation, date_modification FROM reclamation ";

    /** Candidat à l’assignation (admin) — users ADMIN ou agent école (RESPONSABLEECOLE). */
    public static final class AssignCandidate {
        public final int userId;
        public final String displayLabel;
        /** Valeur pour colonne {@code role_assigne} (ADMIN ou AGENT_ECOLE). */
        public final String roleAssigneDb;

        public AssignCandidate(int userId, String displayLabel, String roleAssigneDb) {
            this.userId = userId;
            this.displayLabel = displayLabel;
            this.roleAssigneDb = roleAssigneDb;
        }
    }

    /** Compteurs pour bandeau d’alertes (scope appliqué). */
    public static final class AlertCounts {
        public int nouvelles;
        /** URGENTE encore en NOUVELLE ou EN_COURS. */
        public int urgentNonTraite;
        /** Ouvertes (non fermées/rejetées) sans responsable. */
        public int nonAssignees;
    }

    /** Statistiques avancées + répartitions. */
    public static final class AdvancedStats {
        public int total;
        public int traitees; // FERMEE + REJETEE
        public int avecReponse;
        public final Map<String, Integer> parCategorie = new LinkedHashMap<>();
        public final Map<String, Integer> parPriorite = new LinkedHashMap<>();
        public final Map<String, Integer> parRoleCreateur = new LinkedHashMap<>();
        /** Clé = id école ou "Non renseigné". */
        public final Map<String, Integer> parEcole = new LinkedHashMap<>();
        public double tauxTraitementPct;
        public double tauxReponsePct;
    }

    /** École proposée au parent lors de la création d’une réclamation (liste dérivée des enfants / candidatures). */
    public static final class ParentEcoleChoice {
        public final int idEcole;
        public final String nomEcole;

        public ParentEcoleChoice(int idEcole, String nomEcole) {
            this.idEcole = idEcole;
            this.nomEcole = nomEcole;
        }

        @Override
        public String toString() {
            String n = nomEcole != null && !nomEcole.isBlank() ? nomEcole.trim() : "École";
            return n + " (#" + idEcole + ")";
        }
    }

    /** Résolution école / bus / trajet pour une réclamation créée par un chauffeur. */
    public static final class ChauffeurReclamationContext {
        public final Integer idEcole;
        public final Integer idBus;
        public final Integer idTrajet;

        public ChauffeurReclamationContext(Integer idEcole, Integer idBus, Integer idTrajet) {
            this.idEcole = idEcole;
            this.idBus = idBus;
            this.idTrajet = idTrajet;
        }
    }

    /** École avec le plus de réclamations (périmètre global admin). */
    public static final class TopEcoleReclamations {
        public final int idEcole;
        public final String nomEcole;
        public final int nombreReclamations;

        public TopEcoleReclamations(int idEcole, String nomEcole, int nombreReclamations) {
            this.idEcole = idEcole;
            this.nomEcole = nomEcole;
            this.nombreReclamations = nombreReclamations;
        }
    }

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

    /**
     * Noms d’affichage ({@code users.nom}) pour une liste d’ids (une requête).
     */
    public Map<Integer, String> findUserNomsByIds(Collection<Integer> ids) throws SQLException {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        Set<Integer> unique = new HashSet<>();
        for (Integer id : ids) {
            if (id != null) {
                unique.add(id);
            }
        }
        if (unique.isEmpty()) {
            return Collections.emptyMap();
        }
        String placeholders = String.join(",", Collections.nCopies(unique.size(), "?"));
        String sql = "SELECT id, nom FROM users WHERE id IN (" + placeholders + ")";
        Map<Integer, String> map = new HashMap<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            int i = 1;
            for (Integer id : unique) {
                ps.setInt(i++, id);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getInt("id"), rs.getString("nom"));
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

    /** Met à jour l’assignation (sans toucher au statut). */
    public void updateAssignation(int reclamationId, Integer newUserId, String newRoleDb) throws SQLException {
        String sql = "UPDATE reclamation SET user_id_assigne = ?, role_assigne = ?, date_assignation = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            setNullableInt(ps, 1, newUserId);
            if (newRoleDb == null) {
                ps.setNull(2, Types.VARCHAR);
            } else {
                ps.setString(2, newRoleDb);
            }
            ps.setInt(3, reclamationId);
            ps.executeUpdate();
        }
    }

    public void clearAssignation(int reclamationId) throws SQLException {
        String sql = "UPDATE reclamation SET user_id_assigne = NULL, role_assigne = NULL, date_assignation = NULL WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, reclamationId);
            ps.executeUpdate();
        }
    }

    /**
     * Agent/admin : prend en charge. Si déjà assignée à un autre utilisateur, lève une exception métier simple.
     * Passe automatiquement NOUVELLE → EN_COURS (statut existant).
     */
    public void prendreEnCharge(Reclamation r, int actorUserId, String assignRoleDb,
                                ReclamationHistoriqueService hist, String sessionRoleKey) throws SQLException {
        if (r == null) {
            throw new SQLException("Réclamation introuvable.");
        }
        Integer cur = r.getUserIdAssigne();
        if (cur != null && cur.equals(actorUserId)) {
            return;
        }
        if (cur != null && cur != actorUserId) {
            throw new SQLException("Déjà assignée à un autre utilisateur.");
        }
        Integer oldAssign = r.getUserIdAssigne();
        String oldStatut = r.getStatut();
        updateAssignation(r.getId(), actorUserId, assignRoleDb);
        hist.enregistrer(r.getId(), ReclamationHistoriqueService.ACTION_ASSIGNATION,
                oldStatut, oldStatut, oldAssign, actorUserId,
                "Prise en charge par utilisateur n° " + actorUserId,
                actorUserId, sessionRoleKey);
        if (STATUT_NOUVELLE.equals(oldStatut)) {
            modifierStatut(r.getId(), STATUT_EN_COURS);
            hist.enregistrer(r.getId(), ReclamationHistoriqueService.ACTION_CHANGEMENT_STATUT,
                    oldStatut, STATUT_EN_COURS, null, null,
                    "Passage automatique en EN_COURS lors de la prise en charge",
                    actorUserId, sessionRoleKey);
        }
    }

    /** Admin : assigne à un utilisateur cible. */
    public void assignerA(Reclamation r, int targetUserId, String targetRoleDb,
                          int actorUserId, String sessionRoleKey, ReclamationHistoriqueService hist) throws SQLException {
        Integer oldAssign = r.getUserIdAssigne();
        String st = r.getStatut();
        updateAssignation(r.getId(), targetUserId, targetRoleDb);
        hist.enregistrer(r.getId(), ReclamationHistoriqueService.ACTION_ASSIGNATION,
                st, st, oldAssign, targetUserId,
                "Assignation à utilisateur n° " + targetUserId + " (" + targetRoleDb + ")",
                actorUserId, sessionRoleKey);
    }

    public void libererAssignation(Reclamation r, int actorUserId, String sessionRoleKey,
                                   ReclamationHistoriqueService hist) throws SQLException {
        Integer old = r.getUserIdAssigne();
        String st = r.getStatut();
        clearAssignation(r.getId());
        hist.enregistrer(r.getId(), ReclamationHistoriqueService.ACTION_LIBERATION_ASSIGNATION,
                st, st, old, null, "Responsable retiré", actorUserId, sessionRoleKey);
    }

    public List<AssignCandidate> listAssignableUsersForAdmin() throws SQLException {
        List<AssignCandidate> list = new ArrayList<>();
        String sql = "SELECT id, nom, email, categorie FROM users WHERE categorie IN ('ADMIN','RESPONSABLEECOLE') ORDER BY nom, id";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String nom = rs.getString("nom");
                String email = rs.getString("email");
                String cat = rs.getString("categorie");
                String roleDb = "RESPONSABLEECOLE".equals(cat) ? "AGENT_ECOLE" : "ADMIN";
                String label = (nom != null ? nom : "?") + " · " + (email != null ? email : "") + " (#" + id + ")";
                list.add(new AssignCandidate(id, label.trim(), roleDb));
            }
        }
        return list;
    }

    public AlertCounts getAlertCounts(String scopeRole, Integer userId, Integer idEcole) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT "
                        + "SUM(CASE WHEN statut = 'NOUVELLE' THEN 1 ELSE 0 END) AS n_nouv, "
                        + "SUM(CASE WHEN priorite = 'URGENTE' AND statut IN ('NOUVELLE','EN_COURS') THEN 1 ELSE 0 END) AS n_urg, "
                        + "SUM(CASE WHEN user_id_assigne IS NULL AND statut NOT IN ('FERMEE','REJETEE') THEN 1 ELSE 0 END) AS n_na "
                        + "FROM reclamation WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        appendScopeClause(sql, params, scopeRole, userId, idEcole);
        AlertCounts a = new AlertCounts();
        try (PreparedStatement ps = getConnection().prepareStatement(sql.toString())) {
            bindParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    a.nouvelles = rs.getInt("n_nouv");
                    a.urgentNonTraite = rs.getInt("n_urg");
                    a.nonAssignees = rs.getInt("n_na");
                }
            }
        }
        return a;
    }

    public AdvancedStats getAdvancedStats(String scopeRole, Integer userId, Integer idEcole) throws SQLException {
        AdvancedStats s = new AdvancedStats();
        DashboardStats dash = getDashboardStats(scopeRole, userId, idEcole);
        s.total = dash.total;
        s.traitees = dash.fermees + dash.rejetees;
        s.tauxTraitementPct = s.total > 0 ? (100.0 * s.traitees / s.total) : 0;
        s.avecReponse = countAvecReponseDansScope(scopeRole, userId, idEcole);
        s.tauxReponsePct = s.total > 0 ? (100.0 * s.avecReponse / s.total) : 0;
        fillGroupMap(s.parCategorie, "categorie", scopeRole, userId, idEcole);
        fillGroupMap(s.parPriorite, "priorite", scopeRole, userId, idEcole);
        fillGroupMap(s.parRoleCreateur, "role_createur", scopeRole, userId, idEcole);
        fillGroupMapEcole(s.parEcole, scopeRole, userId, idEcole);
        return s;
    }

    private int countAvecReponseDansScope(String scopeRole, Integer userId, Integer idEcole) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(DISTINCT r.id) AS n FROM reclamation r "
                        + "INNER JOIN reponse_reclamation rr ON rr.reclamation_id = r.id WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        appendScopeClause(sql, params, scopeRole, userId, idEcole, "r.");
        try (PreparedStatement ps = getConnection().prepareStatement(sql.toString())) {
            bindParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("n");
                }
            }
        }
        return 0;
    }

    private void fillGroupMap(Map<String, Integer> target, String column, String scopeRole, Integer userId, Integer idEcole)
            throws SQLException {
        if (!"categorie".equals(column) && !"priorite".equals(column) && !"role_createur".equals(column)) {
            throw new IllegalArgumentException(column);
        }
        StringBuilder sql = new StringBuilder("SELECT ").append(column).append(" AS k, COUNT(*) AS n FROM reclamation WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        appendScopeClause(sql, params, scopeRole, userId, idEcole);
        sql.append(" GROUP BY ").append(column).append(" ORDER BY n DESC");
        try (PreparedStatement ps = getConnection().prepareStatement(sql.toString())) {
            bindParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String k = rs.getString("k");
                    if (k == null) {
                        k = "—";
                    }
                    target.put(k, rs.getInt("n"));
                }
            }
        }
    }

    /** Clés : id école en texte, ou {@code "Non renseigné"} si {@code id_ecole} est NULL. Valeurs : effectifs. */
    private void fillGroupMapEcole(Map<String, Integer> target, String scopeRole, Integer userId, Integer idEcole)
            throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT IFNULL(id_ecole, -1) AS kid, COUNT(*) AS n FROM reclamation WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        appendScopeClause(sql, params, scopeRole, userId, idEcole);
        sql.append(" GROUP BY kid ORDER BY n DESC");
        try (PreparedStatement ps = getConnection().prepareStatement(sql.toString())) {
            bindParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int kid = rs.getInt("kid");
                    String key = kid < 0 ? "Non renseigné" : String.valueOf(kid);
                    target.put(key, rs.getInt("n"));
                }
            }
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
        appendScopeClause(sql, params, scopeRole, userId, idEcole, "");
    }

    /** Variante avec alias de table (ex. {@code r.}) pour les requêtes avec JOIN. */
    private void appendScopeClause(StringBuilder sql, List<Object> params, String scopeRole, Integer userId, Integer idEcole,
                                   String tableAlias) {
        String a = tableAlias != null ? tableAlias : "";
        if (scopeRole == null) {
            return;
        }
        if ("ADMIN".equals(scopeRole)) {
            return;
        }
        if ("AGENT_ECOLE".equals(scopeRole)) {
            if (idEcole != null) {
                sql.append("AND ").append(a).append("id_ecole = ? ");
                params.add(idEcole);
            }
            return;
        }
        if (userId != null && ("PARENT".equals(scopeRole) || "CHAUFFEUR".equals(scopeRole) || "MAITRESSE".equals(scopeRole))) {
            sql.append("AND ").append(a).append("user_id = ? ");
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

    /**
     * Écoles distinctes liées au parent : candidatures enfant {@code ACCEPTEE} (école visée),
     * union enfants actifs rattachés à un trajet dont {@code trajet.id_ecole} est renseigné.
     */
    public List<ParentEcoleChoice> listEcolesLieesAuParent(int parentId) throws SQLException {
        String sql = "SELECT DISTINCT e.id, e.nom FROM ecole e WHERE e.id IN ("
                + "SELECT ce.id_ecole FROM candidature_enfant ce WHERE ce.parent_id = ? AND ce.statut = 'ACCEPTEE' "
                + "UNION "
                + "SELECT t.id_ecole FROM enfant en INNER JOIN trajet t ON en.trajet_id = t.id "
                + "WHERE en.parent_id = ? AND en.actif = 1 AND t.id_ecole IS NOT NULL AND t.id_ecole > 0"
                + ") ORDER BY e.nom ASC, e.id ASC";
        List<ParentEcoleChoice> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, parentId);
            ps.setInt(2, parentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new ParentEcoleChoice(rs.getInt("id"), rs.getString("nom")));
                }
            }
        }
        return list;
    }

    /** Comptage global des réclamations par {@code id_ecole} (non null), pour stats admin. */
    public Map<Integer, Integer> countReclamationsByEcoleIdGlobale() throws SQLException {
        String sql = "SELECT id_ecole, COUNT(*) AS n FROM reclamation WHERE id_ecole IS NOT NULL GROUP BY id_ecole";
        Map<Integer, Integer> map = new LinkedHashMap<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getInt("id_ecole"), rs.getInt("n"));
            }
        }
        return map;
    }

    /**
     * École ayant le plus de réclamations (toutes réclamations, {@code id_ecole} non null).
     * Égalité : plus petit {@code id_ecole}. {@code null} si aucune ligne pertinente.
     */
    public TopEcoleReclamations getTopEcoleByReclamationsGlobale() throws SQLException {
        String sql = "SELECT r.id_ecole, e.nom AS nom_ecole, COUNT(*) AS n "
                + "FROM reclamation r INNER JOIN ecole e ON e.id = r.id_ecole "
                + "GROUP BY r.id_ecole, e.nom ORDER BY n DESC, r.id_ecole ASC LIMIT 1";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return new TopEcoleReclamations(rs.getInt("id_ecole"), rs.getString("nom_ecole"), rs.getInt("n"));
            }
        }
        return null;
    }

    /**
     * Réclamations encore actives (non {@code FERMEE} / non {@code REJETEE}), aligné sur
     * {@link tn.esprit.workshop.controlleurs.leith.reclamation.ReclamationUiHelper#isStatutCloture(String)}.
     * Pour l’accueil admin « en attente ».
     */
    public int countReclamationsOuvertesGlobale() throws SQLException {
        String sql = "SELECT COUNT(*) FROM reclamation WHERE statut NOT IN ('FERMEE', 'REJETEE')";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Déduit l’école la plus probable pour un parent : {@code id_ecole} de la dernière candidature enfant
     * au statut {@code ACCEPTEE} (plus récente par {@code date_demande}). Sinon {@code null}.
     */
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

    /**
     * Déduit l’école via le bus actuellement affecté au chauffeur ({@code bus.id_chauffeur}, {@code bus.id_ecole}).
     * Si aucun bus ou pas d’{@code id_ecole} sur le bus → {@code null}. Un seul bus est considéré (premier trouvé).
     */
    public Integer findLikelyEcoleIdForChauffeur(int chauffeurId) throws SQLException {
        BusService busService = new BusService();
        Bus bus = busService.getByChauffeurId(chauffeurId);
        if (bus == null) {
            return null;
        }
        return bus.getIdEcole();
    }

    public Integer findLikelyBusIdForChauffeur(int chauffeurId) throws SQLException {
        BusService busService = new BusService();
        Bus bus = busService.getByChauffeurId(chauffeurId);
        return bus != null ? bus.getBusId() : null;
    }

    /**
     * Résout {@code id_ecole}, {@code id_bus} et {@code id_trajet} pour un chauffeur :
     * bus affecté puis trajet lié au bus ; si {@code id_ecole} manque sur le bus, repli sur
     * l’école du trajet puis sur la candidature {@code ACCEPTEE} ({@code candidature.id_ecole}).
     */
    public ChauffeurReclamationContext resolveChauffeurReclamationContext(int chauffeurId) throws SQLException {
        BusService busService = new BusService();
        TrajetService trajetService = new TrajetService();
        CandidatureService candidatureService = new CandidatureService();

        Integer idEcole = null;
        Integer idBus = null;
        Integer idTrajet = null;

        Bus bus = busService.getByChauffeurId(chauffeurId);
        if (bus != null) {
            idBus = bus.getBusId();
            Integer be = bus.getIdEcole();
            if (be != null && be > 0) {
                idEcole = be;
            }
            Trajet t = trajetService.getByBusId(bus.getBusId());
            if (t != null) {
                idTrajet = t.getTrajetId();
                if ((idEcole == null || idEcole <= 0) && t.getIdEcole() > 0) {
                    idEcole = t.getIdEcole();
                }
            }
        }

        if (idEcole == null || idEcole <= 0) {
            Candidature cand = candidatureService.findByChauffeurId(chauffeurId);
            if (cand != null && CandidatureService.STATUT_ACCEPTEE.equals(cand.getStatut())) {
                int ce = cand.getIdEcole();
                if (ce > 0) {
                    idEcole = ce;
                }
            }
        }

        return new ChauffeurReclamationContext(idEcole, idBus, idTrajet);
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
        int ua = rs.getInt("user_id_assigne");
        r.setUserIdAssigne(rs.wasNull() ? null : ua);
        r.setRoleAssigne(rs.getString("role_assigne"));
        r.setDateAssignation(rs.getTimestamp("date_assignation"));
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
