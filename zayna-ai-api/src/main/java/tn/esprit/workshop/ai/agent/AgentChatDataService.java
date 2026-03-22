package tn.esprit.workshop.ai.agent;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Read-only JDBC queries for agent chat. Every query MUST filter by {@code ecoleId}.
 */
@Service
public class AgentChatDataService {

    private static final int LIST_CAP = 25;

    /** Candidatures enfant à parcourir pour la résolution par nom (une école, statut en attente). */
    private static final int ENFANT_CAND_NAME_SCAN_LIMIT = 150;

    private final JdbcTemplate jdbc;

    /** Fragment SQL « LIMIT n » avec espace obligatoire (évite LIMIT26 si concaténation au text block). */
    private static String sqlLimit(int maxRows) {
        return " LIMIT " + maxRows;
    }

    public AgentChatDataService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Integer findEcoleIdForAgentUser(int userId) {
        List<Integer> rows = jdbc.query(
                "SELECT id_ecole FROM agent_ecole WHERE user_id = ? LIMIT 1",
                (rs, rowNum) -> rs.getInt("id_ecole"),
                userId);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public String getEcoleNom(int ecoleId) {
        List<String> rows = jdbc.query(
                "SELECT nom FROM ecole WHERE id = ?",
                (rs, rowNum) -> rs.getString("nom"),
                ecoleId);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public int countMaitresses(int ecoleId) {
        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM maitresse WHERE id_ecole = ?",
                Integer.class,
                ecoleId);
        return n != null ? n : 0;
    }

    public int countEnfantsForEcole(int ecoleId) {
        Integer n = jdbc.queryForObject(
                """
                        SELECT COUNT(*) FROM enfant e
                        INNER JOIN trajet t ON e.trajet_id = t.id AND t.id_ecole = ?
                        WHERE e.actif = 1
                        """,
                Integer.class,
                ecoleId);
        return n != null ? n : 0;
    }

    /** Candidatures enfant ACCEPTEE dont le trajet (si renseigné) appartient bien à la même école. */
    public int countCandidatureEnfantAccepteesScopedTrajetEcole(int ecoleId) {
        Integer n = jdbc.queryForObject(
                """
                        SELECT COUNT(*) FROM candidature_enfant ce
                        WHERE ce.id_ecole = ? AND ce.statut = 'ACCEPTEE'
                        AND (ce.trajet_id IS NULL OR EXISTS (
                            SELECT 1 FROM trajet t WHERE t.id = ce.trajet_id AND t.id_ecole = ce.id_ecole))
                        """,
                Integer.class,
                ecoleId);
        return n != null ? n : 0;
    }

    public int countCandidatureEnfantByStatut(int ecoleId, String statut) {
        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM candidature_enfant WHERE id_ecole = ? AND statut = ?",
                Integer.class,
                ecoleId,
                statut);
        return n != null ? n : 0;
    }

    public int countCandidatureChauffeurPending(int ecoleId) {
        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM candidature WHERE id_ecole = ? AND statut = 'ENVOYEE'",
                Integer.class,
                ecoleId);
        return n != null ? n : 0;
    }

    public int countBusForEcole(int ecoleId) {
        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM bus WHERE id_ecole = ? AND actif = 1",
                Integer.class,
                ecoleId);
        return n != null ? n : 0;
    }

    public int countTrajetsForEcole(int ecoleId) {
        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM trajet WHERE id_ecole = ?",
                Integer.class,
                ecoleId);
        return n != null ? n : 0;
    }

    public int countBusesWithoutMaitresse(int ecoleId) {
        Integer n = jdbc.queryForObject(
                """
                        SELECT COUNT(*) FROM bus b
                        WHERE b.id_ecole = ? AND b.actif = 1
                        AND NOT EXISTS (SELECT 1 FROM maitresse m WHERE m.id_bus = b.id)
                        """,
                Integer.class,
                ecoleId);
        return n != null ? n : 0;
    }

    public int countTrajetsWithoutBus(int ecoleId) {
        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM trajet WHERE id_ecole = ? AND (id_bus IS NULL OR id_bus = 0)",
                Integer.class,
                ecoleId);
        return n != null ? n : 0;
    }

    public int countBusesWithChauffeur(int ecoleId) {
        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM bus WHERE id_ecole = ? AND actif = 1 AND id_chauffeur IS NOT NULL",
                Integer.class,
                ecoleId);
        return n != null ? n : 0;
    }

    public int countBusesWithoutChauffeur(int ecoleId) {
        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM bus WHERE id_ecole = ? AND actif = 1 AND id_chauffeur IS NULL",
                Integer.class,
                ecoleId);
        return n != null ? n : 0;
    }

    public List<String> listMaitresses(int ecoleId) {
        return jdbc.query(
                "SELECT nom, prenom, id_bus FROM maitresse WHERE id_ecole = ? ORDER BY nom, prenom" + sqlLimit(LIST_CAP + 1),
                (rs, rowNum) -> {
                    String nom = rs.getString("nom");
                    String prenom = rs.getString("prenom");
                    Object bid = rs.getObject("id_bus");
                    String busInfo = bid == null ? "sans bus" : ("bus id " + ((Number) bid).intValue());
                    return (prenom != null ? prenom : "") + " " + (nom != null ? nom : "") + " (" + busInfo + ")";
                },
                ecoleId);
    }

    public List<String> listBuses(int ecoleId) {
        return jdbc.query(
                """
                        SELECT id, numero_bus, matricule,
                        (id_chauffeur IS NOT NULL) AS has_chauffeur,
                        EXISTS (SELECT 1 FROM maitresse m WHERE m.id_bus = bus.id) AS has_maitresse
                        FROM bus bus WHERE id_ecole = ? AND actif = 1 ORDER BY id""" + sqlLimit(LIST_CAP + 1),
                (rs, rowNum) -> {
                    String num = rs.getString("numero_bus");
                    String mat = rs.getString("matricule");
                    boolean hc = rs.getBoolean("has_chauffeur");
                    boolean hm = rs.getBoolean("has_maitresse");
                    return "Bus #" + rs.getInt("id") + " — n° " + nullToDash(num) + " / " + nullToDash(mat)
                            + " — chauffeur: " + (hc ? "oui" : "non")
                            + ", maîtresse: " + (hm ? "oui" : "non");
                },
                ecoleId);
    }

    public List<String> listBusesWithoutMaitresse(int ecoleId) {
        return jdbc.query(
                """
                        SELECT b.id, b.numero_bus, b.matricule FROM bus b
                        WHERE b.id_ecole = ? AND b.actif = 1
                        AND NOT EXISTS (SELECT 1 FROM maitresse m WHERE m.id_bus = b.id)
                        ORDER BY b.id""" + sqlLimit(LIST_CAP + 1),
                (rs, rowNum) -> "Bus #" + rs.getInt("id") + " — n° " + nullToDash(rs.getString("numero_bus"))
                        + " / " + nullToDash(rs.getString("matricule")),
                ecoleId);
    }

    public List<String> listTrajets(int ecoleId) {
        return jdbc.query(
                """
                        SELECT t.id, t.nom, t.id_bus FROM trajet t
                        WHERE t.id_ecole = ? ORDER BY t.id""" + sqlLimit(LIST_CAP + 1),
                (rs, rowNum) -> {
                    int bid = rs.getInt("id_bus");
                    String busPart = rs.wasNull() || bid == 0 ? "sans bus" : ("bus " + bid);
                    return "Trajet #" + rs.getInt("id") + " — " + nullToDash(rs.getString("nom")) + " — " + busPart;
                },
                ecoleId);
    }

    public List<String> listTrajetsWithoutBus(int ecoleId) {
        return jdbc.query(
                """
                        SELECT id, nom FROM trajet
                        WHERE id_ecole = ? AND (id_bus IS NULL OR id_bus = 0) ORDER BY id""" + sqlLimit(LIST_CAP + 1),
                (rs, rowNum) -> "Trajet #" + rs.getInt("id") + " — " + nullToDash(rs.getString("nom")),
                ecoleId);
    }

    public List<String> listBusesAssignedChauffeur(int ecoleId) {
        return jdbc.query(
                """
                        SELECT id, numero_bus, matricule FROM bus
                        WHERE id_ecole = ? AND actif = 1 AND id_chauffeur IS NOT NULL ORDER BY id""" + sqlLimit(LIST_CAP + 1),
                (rs, rowNum) -> "Bus #" + rs.getInt("id") + " — n° " + nullToDash(rs.getString("numero_bus"))
                        + " / " + nullToDash(rs.getString("matricule")),
                ecoleId);
    }

    public List<String> listBusesUnassignedChauffeur(int ecoleId) {
        return jdbc.query(
                """
                        SELECT id, numero_bus, matricule FROM bus
                        WHERE id_ecole = ? AND actif = 1 AND id_chauffeur IS NULL ORDER BY id""" + sqlLimit(LIST_CAP + 1),
                (rs, rowNum) -> "Bus #" + rs.getInt("id") + " — n° " + nullToDash(rs.getString("numero_bus"))
                        + " / " + nullToDash(rs.getString("matricule")),
                ecoleId);
    }

    public List<String> listPendingEnfantCandidatures(int ecoleId) {
        return jdbc.query(
                """
                        SELECT id, prenom_enfant, nom_enfant, statut, date_demande FROM candidature_enfant
                        WHERE id_ecole = ? AND statut = 'ENVOYEE' ORDER BY date_demande DESC""" + sqlLimit(LIST_CAP + 1),
                (rs, rowNum) -> "#" + rs.getInt("id") + " — " + nullToDash(rs.getString("prenom_enfant")) + " "
                        + nullToDash(rs.getString("nom_enfant")) + " — " + nullToDash(rs.getString("statut")),
                ecoleId);
    }

    public List<String> listPendingChauffeurCandidatures(int ecoleId) {
        return jdbc.query(
                """
                        SELECT c.id, ch.nom, ch.prenom, c.statut FROM candidature c
                        JOIN chauffeur ch ON ch.id = c.chauffeur_id
                        WHERE c.id_ecole = ? AND c.statut = 'ENVOYEE' ORDER BY c.date_envoi DESC""" + sqlLimit(LIST_CAP + 1),
                (rs, rowNum) -> "#" + rs.getInt("id") + " — " + nullToDash(rs.getString("prenom")) + " "
                        + nullToDash(rs.getString("nom")) + " — " + nullToDash(rs.getString("statut")),
                ecoleId);
    }

    public String findMaitresseForBus(int ecoleId, String busToken) {
        if (busToken == null || busToken.isBlank()) {
            return null;
        }
        String tok = busToken.trim();
        List<String> rows = new ArrayList<>();
        // Match by bus id (numeric) or numero_bus / matricule
        try {
            int bid = Integer.parseInt(tok);
            rows = jdbc.query(
                    """
                            SELECT m.prenom, m.nom, b.id, b.numero_bus, b.matricule FROM bus b
                            LEFT JOIN maitresse m ON m.id_bus = b.id AND m.id_ecole = b.id_ecole
                            WHERE b.id_ecole = ? AND b.id = ? AND b.actif = 1
                            """,
                    (rs, rowNum) -> formatMaitresseBusRow(rs),
                    ecoleId,
                    bid);
        } catch (NumberFormatException ignored) {
            // not a plain integer id
        }
        if (rows.isEmpty()) {
            String like = "%" + tok + "%";
            rows = jdbc.query(
                    """
                            SELECT m.prenom, m.nom, b.id, b.numero_bus, b.matricule FROM bus b
                            LEFT JOIN maitresse m ON m.id_bus = b.id AND m.id_ecole = b.id_ecole
                            WHERE b.id_ecole = ? AND b.actif = 1
                            AND (b.numero_bus = ? OR b.numero_bus LIKE ? OR b.matricule LIKE ?)
                            LIMIT 3
                            """,
                    (rs, rowNum) -> formatMaitresseBusRow(rs),
                    ecoleId,
                    tok,
                    like,
                    like);
        }
        if (rows.isEmpty()) {
            return null;
        }
        if (rows.size() > 1) {
            return "Plusieurs bus correspondent à « " + tok + " » dans votre école. Précisez le numéro ou l'identifiant du bus.";
        }
        return rows.get(0);
    }

    private static String formatMaitresseBusRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        String pn = rs.getString("prenom");
        String nm = rs.getString("nom");
        int bid = rs.getInt("id");
        String num = rs.getString("numero_bus");
        String mat = rs.getString("matricule");
        if (pn == null && nm == null) {
            return "Le bus #" + bid + " (n° " + nullToDash(num) + ") n'a pas de maîtresse assignée dans votre école.";
        }
        return "Pour le bus #" + bid + " (n° " + nullToDash(num) + ", " + nullToDash(mat) + "), la maîtresse est "
                + nullToDash(pn) + " " + nullToDash(nm) + ".";
    }

    public String findBusForMaitresseName(int ecoleId, String nameQuery) {
        if (nameQuery == null || nameQuery.isBlank()) {
            return null;
        }
        String like = "%" + nameQuery.trim().toLowerCase(Locale.ROOT) + "%";
        List<String> rows = jdbc.query(
                """
                        SELECT m.prenom, m.nom, b.id AS bus_id, b.numero_bus, b.matricule FROM maitresse m
                        LEFT JOIN bus b ON b.id = m.id_bus AND b.id_ecole = m.id_ecole
                        WHERE m.id_ecole = ?
                        AND (LOWER(CONCAT(m.prenom,' ',m.nom)) LIKE ? OR LOWER(m.nom) LIKE ? OR LOWER(m.prenom) LIKE ?)
                        ORDER BY m.nom LIMIT 5
                        """,
                (rs, rowNum) -> formatBusForMaitresseRow(rs),
                ecoleId,
                like,
                like,
                like);
        if (rows.isEmpty()) {
            return null;
        }
        if (rows.size() > 1) {
            return "Plusieurs maîtresses correspondent. Précisez le nom : " + String.join(" | ", rows);
        }
        return rows.get(0);
    }

    private static String formatBusForMaitresseRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        String pn = rs.getString("prenom");
        String nm = rs.getString("nom");
        Object bidObj = rs.getObject("bus_id");
        if (bidObj == null) {
            return (nullToDash(pn) + " " + nullToDash(nm)).trim() + " : aucun bus assigné.";
        }
        int bid = ((Number) bidObj).intValue();
        if (bid == 0) {
            return (nullToDash(pn) + " " + nullToDash(nm)).trim() + " : aucun bus assigné.";
        }
        String num = rs.getString("numero_bus");
        String mat = rs.getString("matricule");
        return (nullToDash(pn) + " " + nullToDash(nm)).trim() + " est sur le bus #" + bid
                + " (n° " + nullToDash(num) + ", " + nullToDash(mat) + ").";
    }

    public List<String> searchMaitresses(int ecoleId, String q) {
        if (q == null || q.isBlank()) {
            return List.of();
        }
        String like = "%" + q.trim().toLowerCase(Locale.ROOT) + "%";
        return jdbc.query(
                """
                        SELECT nom, prenom, id_bus FROM maitresse
                        WHERE id_ecole = ?
                        AND (LOWER(CONCAT(prenom,' ',nom)) LIKE ? OR LOWER(nom) LIKE ? OR LOWER(prenom) LIKE ?)
                        ORDER BY nom""" + sqlLimit(LIST_CAP + 1),
                (rs, rowNum) -> (rs.getString("prenom") + " " + rs.getString("nom")).trim()
                        + " (bus: " + (rs.getObject("id_bus") == null ? "—" : String.valueOf(rs.getInt("id_bus"))) + ")",
                ecoleId,
                like,
                like,
                like);
    }

    public List<String> searchBuses(int ecoleId, String q) {
        if (q == null || q.isBlank()) {
            return List.of();
        }
        String tok = q.trim();
        String like = "%" + tok.toLowerCase(Locale.ROOT) + "%";
        List<String> rows = new ArrayList<>();
        try {
            int bid = Integer.parseInt(tok);
            rows = jdbc.query(
                    "SELECT id, numero_bus, matricule FROM bus WHERE id_ecole = ? AND actif = 1 AND id = ?",
                    (rs, rowNum) -> "Bus #" + rs.getInt("id") + " — n° " + nullToDash(rs.getString("numero_bus"))
                            + " / " + nullToDash(rs.getString("matricule")),
                    ecoleId,
                    bid);
        } catch (NumberFormatException ignored) {
            // continue
        }
        if (rows.isEmpty()) {
            rows = jdbc.query(
                    """
                            SELECT id, numero_bus, matricule FROM bus WHERE id_ecole = ? AND actif = 1
                            AND (LOWER(numero_bus) LIKE ? OR LOWER(matricule) LIKE ?) ORDER BY id""" + sqlLimit(LIST_CAP + 1),
                    (rs, rowNum) -> "Bus #" + rs.getInt("id") + " — n° " + nullToDash(rs.getString("numero_bus"))
                            + " / " + nullToDash(rs.getString("matricule")),
                    ecoleId,
                    like,
                    like);
        }
        return rows;
    }

    // --- Chauffeurs (périmètre école : bus actifs + candidatures) ---

    public int countChauffeursForEcole(int ecoleId) {
        Integer n = jdbc.queryForObject(
                """
                        SELECT COUNT(*) FROM (
                            SELECT DISTINCT c.id FROM chauffeur c
                            WHERE EXISTS (
                                SELECT 1 FROM bus b WHERE b.id_ecole = ? AND b.actif = 1 AND b.id_chauffeur = c.id)
                            UNION
                            SELECT DISTINCT c.id FROM chauffeur c
                            INNER JOIN candidature ca ON ca.chauffeur_id = c.id AND ca.id_ecole = ?
                        ) t
                        """,
                Integer.class,
                ecoleId,
                ecoleId);
        return n != null ? n : 0;
    }

    public int countChauffeursAssignedToSchoolBuses(int ecoleId) {
        Integer n = jdbc.queryForObject(
                """
                        SELECT COUNT(DISTINCT c.id) FROM chauffeur c
                        INNER JOIN bus b ON b.id_chauffeur = c.id
                        WHERE b.id_ecole = ? AND b.actif = 1
                        """,
                Integer.class,
                ecoleId);
        return n != null ? n : 0;
    }

    public int countChauffeursUnassignedSchoolBus(int ecoleId) {
        Integer n = jdbc.queryForObject(
                """
                        SELECT COUNT(DISTINCT c.id) FROM chauffeur c
                        INNER JOIN candidature ca ON ca.chauffeur_id = c.id AND ca.id_ecole = ?
                        WHERE NOT EXISTS (
                            SELECT 1 FROM bus b WHERE b.id_ecole = ? AND b.actif = 1 AND b.id_chauffeur = c.id)
                        """,
                Integer.class,
                ecoleId,
                ecoleId);
        return n != null ? n : 0;
    }

    public List<String> listChauffeursForEcole(int ecoleId) {
        return jdbc.query(
                """
                        SELECT c.id, c.nom, c.prenom,
                        (SELECT MIN(b.id) FROM bus b
                         WHERE b.id_ecole = ? AND b.actif = 1 AND b.id_chauffeur = c.id) AS any_bus_id
                        FROM chauffeur c
                        WHERE EXISTS (
                            SELECT 1 FROM bus b WHERE b.id_ecole = ? AND b.actif = 1 AND b.id_chauffeur = c.id)
                           OR EXISTS (
                            SELECT 1 FROM candidature ca WHERE ca.chauffeur_id = c.id AND ca.id_ecole = ?)
                        ORDER BY c.nom, c.prenom""" + sqlLimit(LIST_CAP + 1),
                (rs, rowNum) -> formatChauffeurListRow(rs),
                ecoleId,
                ecoleId,
                ecoleId);
    }

    private static String formatChauffeurListRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        String pn = rs.getString("prenom");
        String nm = rs.getString("nom");
        Object bidObj = rs.getObject("any_bus_id");
        if (bidObj == null) {
            return (nullToDash(pn) + " " + nullToDash(nm)).trim() + " — sans bus assigné dans votre école";
        }
        int bid = ((Number) bidObj).intValue();
        return (nullToDash(pn) + " " + nullToDash(nm)).trim() + " — bus #" + bid;
    }

    public List<String> listChauffeursAssigned(int ecoleId) {
        return jdbc.query(
                """
                        SELECT DISTINCT c.id, c.nom, c.prenom, b.id AS bus_id, b.numero_bus, b.matricule
                        FROM chauffeur c
                        INNER JOIN bus b ON b.id_chauffeur = c.id AND b.id_ecole = ? AND b.actif = 1
                        ORDER BY c.nom, c.prenom, b.id""" + sqlLimit(LIST_CAP + 1),
                (rs, rowNum) -> (nullToDash(rs.getString("prenom")) + " " + nullToDash(rs.getString("nom"))).trim()
                        + " — bus #" + rs.getInt("bus_id") + " (n° " + nullToDash(rs.getString("numero_bus"))
                        + ", " + nullToDash(rs.getString("matricule")) + ")",
                ecoleId);
    }

    public List<String> listChauffeursUnassigned(int ecoleId) {
        return jdbc.query(
                """
                        SELECT DISTINCT c.id, c.nom, c.prenom FROM chauffeur c
                        INNER JOIN candidature ca ON ca.chauffeur_id = c.id AND ca.id_ecole = ?
                        WHERE NOT EXISTS (
                            SELECT 1 FROM bus b WHERE b.id_ecole = ? AND b.actif = 1 AND b.id_chauffeur = c.id)
                        ORDER BY c.nom, c.prenom""" + sqlLimit(LIST_CAP + 1),
                (rs, rowNum) -> (nullToDash(rs.getString("prenom")) + " " + nullToDash(rs.getString("nom"))).trim()
                        + " — aucun bus assigné dans votre école",
                ecoleId,
                ecoleId);
    }

    public String findChauffeurForBus(int ecoleId, String busToken) {
        if (busToken == null || busToken.isBlank()) {
            return null;
        }
        String tok = busToken.trim();
        List<String> rows = new ArrayList<>();
        try {
            int bid = Integer.parseInt(tok);
            rows = jdbc.query(
                    """
                            SELECT ch.prenom, ch.nom, b.id, b.numero_bus, b.matricule FROM bus b
                            LEFT JOIN chauffeur ch ON ch.id = b.id_chauffeur
                            WHERE b.id_ecole = ? AND b.id = ? AND b.actif = 1
                            """,
                    (rs, rowNum) -> formatChauffeurBusRow(rs),
                    ecoleId,
                    bid);
        } catch (NumberFormatException ignored) {
            // not numeric id
        }
        if (rows.isEmpty()) {
            String like = "%" + tok + "%";
            rows = jdbc.query(
                    """
                            SELECT ch.prenom, ch.nom, b.id, b.numero_bus, b.matricule FROM bus b
                            LEFT JOIN chauffeur ch ON ch.id = b.id_chauffeur
                            WHERE b.id_ecole = ? AND b.actif = 1
                            AND (b.numero_bus = ? OR b.numero_bus LIKE ? OR b.matricule LIKE ?)
                            LIMIT 3
                            """,
                    (rs, rowNum) -> formatChauffeurBusRow(rs),
                    ecoleId,
                    tok,
                    like,
                    like);
        }
        if (rows.isEmpty()) {
            return null;
        }
        if (rows.size() > 1) {
            return "Plusieurs bus correspondent à « " + tok + " » dans votre école. Précisez le numéro ou l'identifiant du bus.";
        }
        return rows.get(0);
    }

    private static String formatChauffeurBusRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        String pn = rs.getString("prenom");
        String nm = rs.getString("nom");
        int bid = rs.getInt("id");
        String num = rs.getString("numero_bus");
        String mat = rs.getString("matricule");
        if (pn == null && nm == null) {
            return "Le bus #" + bid + " (n° " + nullToDash(num) + ") n'a pas de chauffeur assigné dans votre école.";
        }
        return "Pour le bus #" + bid + " (n° " + nullToDash(num) + ", " + nullToDash(mat) + "), le chauffeur est "
                + nullToDash(pn) + " " + nullToDash(nm) + ".";
    }

    public String findBusForChauffeurName(int ecoleId, String nameQuery) {
        if (nameQuery == null || nameQuery.isBlank()) {
            return null;
        }
        String like = "%" + nameQuery.trim().toLowerCase(Locale.ROOT) + "%";
        List<String> rows = jdbc.query(
                """
                        SELECT ch.prenom, ch.nom, b.id AS bus_id, b.numero_bus, b.matricule FROM chauffeur ch
                        LEFT JOIN bus b ON b.id_chauffeur = ch.id AND b.id_ecole = ? AND b.actif = 1
                        WHERE (
                            EXISTS (SELECT 1 FROM bus b2 WHERE b2.id_ecole = ? AND b2.actif = 1 AND b2.id_chauffeur = ch.id)
                            OR EXISTS (SELECT 1 FROM candidature ca WHERE ca.chauffeur_id = ch.id AND ca.id_ecole = ?)
                        )
                        AND (LOWER(CONCAT(ch.prenom,' ',ch.nom)) LIKE ? OR LOWER(ch.nom) LIKE ? OR LOWER(ch.prenom) LIKE ?)
                        ORDER BY ch.nom LIMIT 5
                        """,
                (rs, rowNum) -> formatBusForChauffeurRow(rs),
                ecoleId,
                ecoleId,
                ecoleId,
                like,
                like,
                like);
        if (rows.isEmpty()) {
            return null;
        }
        if (rows.size() > 1) {
            return "Plusieurs chauffeurs correspondent. Précisez le nom : " + String.join(" | ", rows);
        }
        return rows.get(0);
    }

    private static String formatBusForChauffeurRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        String pn = rs.getString("prenom");
        String nm = rs.getString("nom");
        Object bidObj = rs.getObject("bus_id");
        String who = (nullToDash(pn) + " " + nullToDash(nm)).trim();
        if (bidObj == null) {
            return who + " : aucun bus assigné dans votre école.";
        }
        int bid = ((Number) bidObj).intValue();
        if (bid == 0) {
            return who + " : aucun bus assigné dans votre école.";
        }
        String num = rs.getString("numero_bus");
        String mat = rs.getString("matricule");
        return "Le chauffeur " + who + " est affecté au bus #" + bid + " (n° " + nullToDash(num) + ", " + nullToDash(mat) + ").";
    }

    // --- Enfants (trajet de l'école) ---

    public int countEnfantsTransportedWithBus(int ecoleId) {
        Integer n = jdbc.queryForObject(
                """
                        SELECT COUNT(*) FROM enfant e
                        INNER JOIN trajet t ON e.trajet_id = t.id AND t.id_ecole = ?
                        WHERE e.actif = 1
                        AND t.id_bus IS NOT NULL AND t.id_bus <> 0
                        """,
                Integer.class,
                ecoleId);
        return n != null ? n : 0;
    }

    public int countEnfantsOnBoard(int ecoleId) {
        Integer n = jdbc.queryForObject(
                """
                        SELECT COUNT(*) FROM enfant e
                        INNER JOIN trajet t ON e.trajet_id = t.id AND t.id_ecole = ?
                        WHERE e.actif = 1 AND e.on_board <> 0
                        """,
                Integer.class,
                ecoleId);
        return n != null ? n : 0;
    }

    public int countEnfantsOffBoard(int ecoleId) {
        Integer n = jdbc.queryForObject(
                """
                        SELECT COUNT(*) FROM enfant e
                        INNER JOIN trajet t ON e.trajet_id = t.id AND t.id_ecole = ?
                        WHERE e.actif = 1 AND (e.on_board = 0 OR e.on_board IS NULL)
                        """,
                Integer.class,
                ecoleId);
        return n != null ? n : 0;
    }

    public List<String> listEnfantsForEcole(int ecoleId) {
        return jdbc.query(
                """
                        SELECT e.id, e.prenom, e.nom, t.id AS trajet_id, t.nom AS trajet_nom, t.id_bus,
                        b.numero_bus, b.matricule
                        FROM enfant e
                        INNER JOIN trajet t ON e.trajet_id = t.id AND t.id_ecole = ?
                        LEFT JOIN bus b ON b.id = t.id_bus AND b.id_ecole = t.id_ecole AND b.actif = 1
                        WHERE e.actif = 1
                        ORDER BY e.nom, e.prenom, e.id""" + sqlLimit(LIST_CAP + 1),
                (rs, rowNum) -> formatEnfantListRow(rs),
                ecoleId);
    }

    public List<String> listEnfantsTransportedWithBus(int ecoleId) {
        return jdbc.query(
                """
                        SELECT e.id, e.prenom, e.nom, t.id AS trajet_id, t.nom AS trajet_nom, t.id_bus,
                        b.numero_bus, b.matricule
                        FROM enfant e
                        INNER JOIN trajet t ON e.trajet_id = t.id AND t.id_ecole = ?
                        LEFT JOIN bus b ON b.id = t.id_bus AND b.id_ecole = t.id_ecole AND b.actif = 1
                        WHERE e.actif = 1 AND t.id_bus IS NOT NULL AND t.id_bus <> 0
                        ORDER BY e.nom, e.prenom, e.id""" + sqlLimit(LIST_CAP + 1),
                (rs, rowNum) -> formatEnfantListRow(rs),
                ecoleId);
    }

    private static String formatEnfantListRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        int tid = rs.getInt("trajet_id");
        String tnom = nullToDash(rs.getString("trajet_nom"));
        Object bidObj = rs.getObject("id_bus");
        int bid = bidObj == null ? 0 : ((Number) bidObj).intValue();
        boolean hasBus = bid != 0;
        String busPart = hasBus
                ? ("bus #" + bid + " (n° " + nullToDash(rs.getString("numero_bus")) + ")")
                : "sans bus (trajet)";
        return "Enfant #" + rs.getInt("id") + " — " + nullToDash(rs.getString("prenom")) + " "
                + nullToDash(rs.getString("nom")) + " — trajet #" + tid + " (" + tnom + ") — " + busPart;
    }

    public List<String> listEnfantsOnBoard(int ecoleId) {
        return jdbc.query(
                """
                        SELECT e.id, e.prenom, e.nom, t.id AS trajet_id, t.nom AS trajet_nom, t.id_bus,
                        b.numero_bus, b.matricule
                        FROM enfant e
                        INNER JOIN trajet t ON e.trajet_id = t.id AND t.id_ecole = ?
                        LEFT JOIN bus b ON b.id = t.id_bus AND b.id_ecole = t.id_ecole AND b.actif = 1
                        WHERE e.actif = 1 AND e.on_board <> 0
                        ORDER BY e.nom, e.prenom, e.id""" + sqlLimit(LIST_CAP + 1),
                (rs, rowNum) -> formatEnfantListRow(rs),
                ecoleId);
    }

    public List<String> listEnfantsOffBoard(int ecoleId) {
        return jdbc.query(
                """
                        SELECT e.id, e.prenom, e.nom, t.id AS trajet_id, t.nom AS trajet_nom, t.id_bus,
                        b.numero_bus, b.matricule
                        FROM enfant e
                        INNER JOIN trajet t ON e.trajet_id = t.id AND t.id_ecole = ?
                        LEFT JOIN bus b ON b.id = t.id_bus AND b.id_ecole = t.id_ecole AND b.actif = 1
                        WHERE e.actif = 1 AND (e.on_board = 0 OR e.on_board IS NULL)
                        ORDER BY e.nom, e.prenom, e.id""" + sqlLimit(LIST_CAP + 1),
                (rs, rowNum) -> formatEnfantListRow(rs),
                ecoleId);
    }

    public List<String> listAcceptedEnfantCandidatures(int ecoleId) {
        return jdbc.query(
                """
                        SELECT ce.id, ce.prenom_enfant, ce.nom_enfant FROM candidature_enfant ce
                        WHERE ce.id_ecole = ? AND ce.statut = 'ACCEPTEE'
                        AND (ce.trajet_id IS NULL OR EXISTS (
                            SELECT 1 FROM trajet t WHERE t.id = ce.trajet_id AND t.id_ecole = ce.id_ecole))
                        ORDER BY ce.nom_enfant, ce.prenom_enfant""" + sqlLimit(LIST_CAP + 1),
                (rs, rowNum) -> "#" + rs.getInt("id") + " — " + nullToDash(rs.getString("prenom_enfant")) + " "
                        + nullToDash(rs.getString("nom_enfant")) + " (candidature acceptée)",
                ecoleId);
    }

    /**
     * Périmètre agent = enfant lié à un trajet de l’école uniquement. Un enfant sans trajet de cette école
     * n’appartient pas au périmètre, donc cette liste est toujours vide (pas de lecture globale sur {@code enfant}).
     */
    public List<String> listEnfantsWithoutTrajetForEcole(int ecoleId) {
        return List.of();
    }

    public List<String> listEnfantsWithoutBus(int ecoleId) {
        return jdbc.query(
                """
                        SELECT e.id, e.prenom, e.nom, t.id AS trajet_id, t.nom AS trajet_nom
                        FROM enfant e
                        INNER JOIN trajet t ON e.trajet_id = t.id AND t.id_ecole = ?
                        WHERE e.actif = 1 AND (t.id_bus IS NULL OR t.id_bus = 0)
                        ORDER BY e.nom, e.prenom""" + sqlLimit(LIST_CAP + 1),
                (rs, rowNum) -> "Enfant #" + rs.getInt("id") + " — " + nullToDash(rs.getString("prenom")) + " "
                        + nullToDash(rs.getString("nom")) + " — trajet #" + rs.getInt("trajet_id")
                        + " (" + nullToDash(rs.getString("trajet_nom")) + ") sans bus",
                ecoleId);
    }

    public List<String> searchEnfants(int ecoleId, String q) {
        if (q == null || q.isBlank()) {
            return List.of();
        }
        String like = "%" + q.trim().toLowerCase(Locale.ROOT) + "%";
        return jdbc.query(
                """
                        SELECT e.id, e.prenom, e.nom, t.id AS trajet_id, t.nom AS trajet_nom, t.id_bus,
                        b.numero_bus, b.matricule
                        FROM enfant e
                        INNER JOIN trajet t ON e.trajet_id = t.id AND t.id_ecole = ?
                        LEFT JOIN bus b ON b.id = t.id_bus AND b.id_ecole = t.id_ecole AND b.actif = 1
                        WHERE e.actif = 1
                        AND (LOWER(CONCAT(e.prenom,' ',e.nom)) LIKE ? OR LOWER(e.nom) LIKE ? OR LOWER(e.prenom) LIKE ?)
                        ORDER BY e.nom, e.prenom""" + sqlLimit(LIST_CAP + 1),
                (rs, rowNum) -> formatEnfantListRow(rs),
                ecoleId,
                like,
                like,
                like);
    }

    public String findTrajetForEnfantName(int ecoleId, String nameQuery) {
        if (nameQuery == null || nameQuery.isBlank()) {
            return null;
        }
        String like = "%" + nameQuery.trim().toLowerCase(Locale.ROOT) + "%";
        List<String> rows = jdbc.query(
                """
                        SELECT e.prenom, e.nom, t.id AS trajet_id, t.nom AS trajet_nom FROM enfant e
                        INNER JOIN trajet t ON e.trajet_id = t.id AND t.id_ecole = ?
                        WHERE e.actif = 1
                        AND (LOWER(CONCAT(e.prenom,' ',e.nom)) LIKE ? OR LOWER(e.nom) LIKE ? OR LOWER(e.prenom) LIKE ?)
                        ORDER BY e.nom LIMIT 5
                        """,
                (rs, rowNum) -> {
                    String pn = rs.getString("prenom");
                    String nm = rs.getString("nom");
                    int tid = rs.getInt("trajet_id");
                    String tnom = nullToDash(rs.getString("trajet_nom"));
                    String who = (nullToDash(pn) + " " + nullToDash(nm)).trim();
                    return "L'enfant " + who + " est sur le trajet #" + tid + " (« " + tnom + " »).";
                },
                ecoleId,
                like,
                like,
                like);
        if (rows.isEmpty()) {
            return null;
        }
        if (rows.size() > 1) {
            return "Plusieurs enfants correspondent. Précisez le nom : " + String.join(" | ", rows);
        }
        return rows.get(0);
    }

    public String findBusForEnfantName(int ecoleId, String nameQuery) {
        if (nameQuery == null || nameQuery.isBlank()) {
            return null;
        }
        String like = "%" + nameQuery.trim().toLowerCase(Locale.ROOT) + "%";
        List<String> rows = jdbc.query(
                """
                        SELECT e.prenom, e.nom, t.id AS trajet_id, t.nom AS trajet_nom, t.id_bus,
                        b.numero_bus, b.matricule FROM enfant e
                        INNER JOIN trajet t ON e.trajet_id = t.id AND t.id_ecole = ?
                        LEFT JOIN bus b ON b.id = t.id_bus AND b.id_ecole = t.id_ecole AND b.actif = 1
                        WHERE e.actif = 1
                        AND (LOWER(CONCAT(e.prenom,' ',e.nom)) LIKE ? OR LOWER(e.nom) LIKE ? OR LOWER(e.prenom) LIKE ?)
                        ORDER BY e.nom LIMIT 5
                        """,
                (rs, rowNum) -> formatBusForEnfantRow(rs),
                ecoleId,
                like,
                like,
                like);
        if (rows.isEmpty()) {
            return null;
        }
        if (rows.size() > 1) {
            return "Plusieurs enfants correspondent. Précisez le nom : " + String.join(" | ", rows);
        }
        return rows.get(0);
    }

    private static String formatBusForEnfantRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        String pn = rs.getString("prenom");
        String nm = rs.getString("nom");
        int tid = rs.getInt("trajet_id");
        String tnom = nullToDash(rs.getString("trajet_nom"));
        Object bidObj = rs.getObject("id_bus");
        int bid = bidObj == null ? 0 : ((Number) bidObj).intValue();
        String who = (nullToDash(pn) + " " + nullToDash(nm)).trim();
        if (bid == 0) {
            return "L'enfant " + who + " est sur le trajet #" + tid + " (« " + tnom + " ») sans bus assigné.";
        }
        String num = rs.getString("numero_bus");
        String mat = rs.getString("matricule");
        return "L'enfant " + who + " est transporté par le bus #" + bid + " (n° " + nullToDash(num) + ", " + nullToDash(mat)
                + ") via le trajet #" + tid + " (« " + tnom + " »).";
    }

    public List<String> listEnfantsByBus(int ecoleId, String busToken) {
        if (busToken == null || busToken.isBlank()) {
            return List.of();
        }
        String tok = busToken.trim();
        List<String> rows = new ArrayList<>();
        try {
            int bid = Integer.parseInt(tok);
            rows = jdbc.query(
                    """
                            SELECT e.id, e.prenom, e.nom, t.id AS trajet_id, t.nom AS trajet_nom, t.id_bus,
                            b.numero_bus, b.matricule
                            FROM enfant e
                            INNER JOIN trajet t ON e.trajet_id = t.id AND t.id_ecole = ?
                            INNER JOIN bus b ON b.id = t.id_bus AND b.id_ecole = t.id_ecole AND b.actif = 1
                            WHERE e.actif = 1 AND b.id = ?
                            ORDER BY e.nom, e.prenom""" + sqlLimit(LIST_CAP + 1),
                    (rs, rowNum) -> formatEnfantListRow(rs),
                    ecoleId,
                    bid);
        } catch (NumberFormatException ignored) {
            // continue
        }
        if (rows.isEmpty()) {
            String like = "%" + tok.toLowerCase(Locale.ROOT) + "%";
            rows = jdbc.query(
                    """
                            SELECT e.id, e.prenom, e.nom, t.id AS trajet_id, t.nom AS trajet_nom, t.id_bus,
                            b.numero_bus, b.matricule
                            FROM enfant e
                            INNER JOIN trajet t ON e.trajet_id = t.id AND t.id_ecole = ?
                            INNER JOIN bus b ON b.id = t.id_bus AND b.id_ecole = t.id_ecole AND b.actif = 1
                            WHERE e.actif = 1 AND (b.numero_bus = ? OR LOWER(b.numero_bus) LIKE ? OR LOWER(b.matricule) LIKE ?)
                            ORDER BY e.nom, e.prenom""" + sqlLimit(LIST_CAP + 1),
                    (rs, rowNum) -> formatEnfantListRow(rs),
                    ecoleId,
                    tok,
                    like,
                    like);
        }
        return rows;
    }

    public List<String> listEnfantsByTrajet(int ecoleId, String trajetToken) {
        if (trajetToken == null || trajetToken.isBlank()) {
            return List.of();
        }
        String tok = trajetToken.trim();
        List<String> rows = new ArrayList<>();
        try {
            int tid = Integer.parseInt(tok);
            rows = jdbc.query(
                    """
                            SELECT e.id, e.prenom, e.nom, t.id AS trajet_id, t.nom AS trajet_nom, t.id_bus,
                            b.numero_bus, b.matricule
                            FROM enfant e
                            INNER JOIN trajet t ON e.trajet_id = t.id AND t.id_ecole = ?
                            LEFT JOIN bus b ON b.id = t.id_bus AND b.id_ecole = t.id_ecole AND b.actif = 1
                            WHERE e.actif = 1 AND t.id = ?
                            ORDER BY e.nom, e.prenom""" + sqlLimit(LIST_CAP + 1),
                    (rs, rowNum) -> formatEnfantListRow(rs),
                    ecoleId,
                    tid);
        } catch (NumberFormatException ignored) {
            // continue
        }
        if (rows.isEmpty()) {
            String like = "%" + tok.toLowerCase(Locale.ROOT) + "%";
            rows = jdbc.query(
                    """
                            SELECT e.id, e.prenom, e.nom, t.id AS trajet_id, t.nom AS trajet_nom, t.id_bus,
                            b.numero_bus, b.matricule
                            FROM enfant e
                            INNER JOIN trajet t ON e.trajet_id = t.id AND t.id_ecole = ?
                            LEFT JOIN bus b ON b.id = t.id_bus AND b.id_ecole = t.id_ecole AND b.actif = 1
                            WHERE e.actif = 1 AND LOWER(t.nom) LIKE ?
                            ORDER BY e.nom, e.prenom""" + sqlLimit(LIST_CAP + 1),
                    (rs, rowNum) -> formatEnfantListRow(rs),
                    ecoleId,
                    like);
        }
        return rows;
    }

    // --- Level 3 : résolution des cibles d’action (lecture, périmètre école) ---

    public record ResolvedChauffeurCand(int id, String label) {
    }

    public record ResolvedEnfantCand(int id, String label) {
    }

    public record ResolvedMaitresse(int id, String label) {
    }

    public record ResolvedBus(int id, String label) {
    }

    public List<ResolvedChauffeurCand> findPendingChauffeurCandidaturesByName(int ecoleId, String nameQuery) {
        if (nameQuery == null || nameQuery.isBlank()) {
            return List.of();
        }
        String like = "%" + nameQuery.trim().toLowerCase(Locale.ROOT) + "%";
        return jdbc.query(
                """
                        SELECT c.id, ch.prenom, ch.nom FROM candidature c
                        INNER JOIN chauffeur ch ON ch.id = c.chauffeur_id
                        WHERE c.id_ecole = ? AND c.statut = 'ENVOYEE'
                        AND (LOWER(CONCAT(ch.prenom,' ',ch.nom)) LIKE ? OR LOWER(ch.nom) LIKE ? OR LOWER(ch.prenom) LIKE ?)
                        ORDER BY ch.nom, ch.prenom LIMIT 8
                        """,
                (rs, rowNum) -> new ResolvedChauffeurCand(rs.getInt("id"),
                        (nullToEmpty(rs.getString("prenom")) + " " + nullToEmpty(rs.getString("nom"))).trim()),
                ecoleId, like, like, like);
    }

    /**
     * Résout les candidatures enfant en attente ({@code ENVOYEE}) pour l’école, par nom / prénom / les deux,
     * ordre nom prénom ou prénom nom, avec la même normalisation que le chat agent (accents, casse, espaces).
     */
    public List<ResolvedEnfantCand> findPendingEnfantCandidaturesByName(int ecoleId, String nameQuery) {
        if (nameQuery == null || nameQuery.isBlank()) {
            return List.of();
        }
        String cleaned = stripEnfantCandidatureNameQueryNoise(nameQuery);
        String qNorm = AgentChatTextNormalizer.forMatching(cleaned);
        if (qNorm.isBlank()) {
            return List.of();
        }
        List<EnfantCandScanRow> rows = jdbc.query(
                """
                        SELECT id, prenom_enfant, nom_enfant FROM candidature_enfant
                        WHERE id_ecole = ? AND statut = 'ENVOYEE'
                        ORDER BY date_demande DESC
                        """ + sqlLimit(ENFANT_CAND_NAME_SCAN_LIMIT),
                (rs, rowNum) -> new EnfantCandScanRow(
                        rs.getInt("id"), rs.getString("prenom_enfant"), rs.getString("nom_enfant")),
                ecoleId);
        List<ResolvedEnfantCand> matches = new ArrayList<>();
        for (EnfantCandScanRow row : rows) {
            if (matchesPendingEnfantCandidatureNameFields(row.prenom(), row.nom(), qNorm)) {
                String label = (nullToEmpty(row.prenom()) + " " + nullToEmpty(row.nom())).trim();
                matches.add(new ResolvedEnfantCand(row.id(), label));
            }
        }
        return matches;
    }

    /**
     * Retire des mots parasites souvent dictés avec le nom (« nom », « prénom », etc.) sans toucher au reste.
     */
    private static String stripEnfantCandidatureNameQueryNoise(String raw) {
        String t = nullToEmpty(raw).trim();
        if (t.isEmpty()) {
            return "";
        }
        t = t.replaceAll("(?i)\\b(nom|prenom|prénom|firstname|lastname|name)\\b", " ");
        return t.replaceAll("\\s+", " ").trim();
    }

    private record EnfantCandScanRow(int id, String prenom, String nom) {
    }

    private static boolean matchesPendingEnfantCandidatureNameFields(String prenomRaw, String nomRaw, String qNorm) {
        String pn = AgentChatTextNormalizer.forMatching(prenomRaw);
        String nm = AgentChatTextNormalizer.forMatching(nomRaw);
        if (pn.isEmpty() && nm.isEmpty()) {
            return false;
        }
        String fullPn = (pn + " " + nm).trim();
        String fullNp = (nm + " " + pn).trim();
        if (!fullPn.isEmpty() && (fullPn.contains(qNorm) || qNorm.contains(fullPn))) {
            return true;
        }
        if (!fullNp.isEmpty() && (fullNp.contains(qNorm) || qNorm.contains(fullNp))) {
            return true;
        }
        String[] tokens = qNorm.split("\\s+");
        List<String> tokList = new ArrayList<>();
        for (String t : tokens) {
            if (t != null && !t.isBlank()) {
                tokList.add(t.trim());
            }
        }
        if (tokList.isEmpty()) {
            return false;
        }
        if (tokList.size() == 1) {
            String t = tokList.get(0);
            return nm.contains(t) || pn.contains(t);
        }
        for (String t : tokList) {
            if (!nm.contains(t) && !pn.contains(t)) {
                return false;
            }
        }
        return true;
    }

    public List<ResolvedMaitresse> findMaitressesByNameForEcole(int ecoleId, String nameQuery) {
        if (nameQuery == null || nameQuery.isBlank()) {
            return List.of();
        }
        String like = "%" + nameQuery.trim().toLowerCase(Locale.ROOT) + "%";
        return jdbc.query(
                """
                        SELECT id, prenom, nom FROM maitresse
                        WHERE id_ecole = ?
                        AND (LOWER(CONCAT(prenom,' ',nom)) LIKE ? OR LOWER(nom) LIKE ? OR LOWER(prenom) LIKE ?)
                        ORDER BY nom, prenom LIMIT 8
                        """,
                (rs, rowNum) -> new ResolvedMaitresse(rs.getInt("id"),
                        (nullToEmpty(rs.getString("prenom")) + " " + nullToEmpty(rs.getString("nom"))).trim()),
                ecoleId, like, like, like);
    }

    public Integer getEnfantCandidatureTrajetId(int candidatureId, int ecoleId) {
        List<Integer> rows = jdbc.query(
                "SELECT trajet_id FROM candidature_enfant WHERE id = ? AND id_ecole = ?",
                (rs, rowNum) -> {
                    Object o = rs.getObject("trajet_id");
                    if (o == null) {
                        return null;
                    }
                    int v = ((Number) o).intValue();
                    return v == 0 ? null : v;
                },
                candidatureId,
                ecoleId);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public boolean trajetBelongsToEcole(int trajetId, int ecoleId) {
        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM trajet WHERE id = ? AND id_ecole = ?",
                Integer.class,
                trajetId,
                ecoleId);
        return n != null && n > 0;
    }

    /**
     * Résout un bus actif de l’école par id numérique ou numéro / matricule.
     */
    public List<ResolvedBus> resolveBusesInEcole(int ecoleId, String busToken) {
        if (busToken == null || busToken.isBlank()) {
            return List.of();
        }
        String tok = busToken.trim();
        List<ResolvedBus> out = new ArrayList<>();
        try {
            int bid = Integer.parseInt(tok);
            out.addAll(jdbc.query(
                    "SELECT id, numero_bus, matricule FROM bus WHERE id_ecole = ? AND actif = 1 AND id = ?",
                    (rs, rowNum) -> toResolvedBus(rs),
                    ecoleId,
                    bid));
        } catch (NumberFormatException ignored) {
            // continue
        }
        if (out.isEmpty()) {
            String like = "%" + tok.toLowerCase(Locale.ROOT) + "%";
            out.addAll(jdbc.query(
                    """
                            SELECT id, numero_bus, matricule FROM bus WHERE id_ecole = ? AND actif = 1
                            AND (numero_bus = ? OR LOWER(numero_bus) LIKE ? OR LOWER(matricule) LIKE ?) LIMIT 4
                            """,
                    (rs, rowNum) -> toResolvedBus(rs),
                    ecoleId,
                    tok,
                    like,
                    like));
        }
        return out;
    }

    private static ResolvedBus toResolvedBus(java.sql.ResultSet rs) throws java.sql.SQLException {
        int id = rs.getInt("id");
        String num = rs.getString("numero_bus");
        String mat = rs.getString("matricule");
        String lab = "Bus #" + id + " (n° " + nullToDash(num) + ")";
        if (mat != null && !mat.isBlank()) {
            lab += " / " + mat.trim();
        }
        return new ResolvedBus(id, lab);
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    public String generalSchoolInfo(int ecoleId) {
        String nom = getEcoleNom(ecoleId);
        String adresse = jdbc.query(
                "SELECT adresse FROM ecole WHERE id = ?",
                (rs, rowNum) -> rs.getString("adresse"),
                ecoleId).stream().findFirst().orElse(null);
        int cm = countMaitresses(ecoleId);
        int cb = countBusForEcole(ecoleId);
        int ct = countTrajetsForEcole(ecoleId);
        int ce = countEnfantsForEcole(ecoleId);
        return "École : " + nullToDash(nom) + (adresse != null && !adresse.isBlank() ? (" — " + adresse) : "")
                + ". En bref pour votre périmètre : " + cm + " maîtresse(s), " + cb + " bus, " + ct + " trajet(s), "
                + ce + " enfant(s) actif(s) rattachés à un trajet de l'école.";
    }

    private static String nullToDash(String s) {
        return s == null || s.isBlank() ? "—" : s.trim();
    }

    public static String capList(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return "";
        }
        if (lines.size() > LIST_CAP) {
            return String.join("\n", lines.subList(0, LIST_CAP))
                    + "\n… (liste tronquée, " + lines.size() + " éléments au total)";
        }
        return String.join("\n", lines);
    }
}
