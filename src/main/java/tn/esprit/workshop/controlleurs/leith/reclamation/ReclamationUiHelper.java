package tn.esprit.workshop.controlleurs.leith.reclamation;

import tn.esprit.workshop.model.leith.Reclamation;
import tn.esprit.workshop.model.leith.Candidature;
import tn.esprit.workshop.services.leith.CandidatureService;
import tn.esprit.workshop.utilis.AppSession;

import java.util.Locale;

/**
 * Rôle affiché dans la barre shell → filtres SQL et droits UI réclamations.
 */
public final class ReclamationUiHelper {

    private ReclamationUiHelper() {
    }

    public static String sessionRoleLabel() {
        String r = AppSession.getInstance().getConnectedUserRole();
        return r != null ? r.trim() : "";
    }

    /**
     * Clé normalisée pour le périmètre SQL ({@code rechercherEtFiltrer} / stats).
     * Tolère les variantes de libellés (casse, accents partiels).
     */
    public static String filterRoleKey() {
        String raw = sessionRoleLabel();
        if (raw.isEmpty()) {
            return null;
        }
        String lower = raw.toLowerCase(Locale.FRENCH)
                .replace('î', 'i')
                .replace('é', 'e')
                .replace('è', 'e');
        if (lower.contains("admin")) {
            return "ADMIN";
        }
        if (lower.contains("agent") && lower.contains("ecole")) {
            return "AGENT_ECOLE";
        }
        if (lower.contains("parent")) {
            return "PARENT";
        }
        if (lower.contains("chauffeur")) {
            return "CHAUFFEUR";
        }
        if (lower.contains("maitresse") || lower.contains("maîtresse")) {
            return "MAITRESSE";
        }
        switch (raw) {
            case "Administrateur":
                return "ADMIN";
            case "Agent École":
                return "AGENT_ECOLE";
            case "Parent":
                return "PARENT";
            case "Chauffeur":
                return "CHAUFFEUR";
            case "Maîtresse":
                return "MAITRESSE";
            default:
                return null;
        }
    }

    public static boolean isAdmin() {
        return "ADMIN".equals(filterRoleKey());
    }

    public static boolean isAgentEcole() {
        return "AGENT_ECOLE".equals(filterRoleKey());
    }

    public static boolean canRespondOrChangeStatut() {
        return isAdmin() || isAgentEcole();
    }

    public static boolean canCreateReclamation() {
        String role = filterRoleKey();
        if (role == null) {
            return false;
        }
        if (!"CHAUFFEUR".equals(role)) {
            return true;
        }
        Integer chauffeurId = AppSession.getInstance().getChauffeurId();
        if (chauffeurId == null) {
            return false;
        }
        try {
            Candidature c = new CandidatureService().findByChauffeurId(chauffeurId);
            return c != null && "ACCEPTEE".equals(c.getStatut());
        } catch (Exception ignored) {
            return false;
        }
    }

    /** Rôle enum DB pour une nouvelle réponse (agent ou admin uniquement à l’UI). */
    public static String responderDbRole() {
        if (isAdmin()) {
            return "ADMIN";
        }
        if (isAgentEcole()) {
            return "AGENT_ECOLE";
        }
        return null;
    }

    public static boolean canView(Reclamation r) {
        if (r == null) {
            return false;
        }
        AppSession s = AppSession.getInstance();
        Integer uid = s.getConnectedUserId();
        if (uid == null) {
            return false;
        }
        String fk = filterRoleKey();
        if ("ADMIN".equals(fk)) {
            return true;
        }
        if ("AGENT_ECOLE".equals(fk)) {
            Integer eid = s.getEcoleId();
            return eid != null && eid.equals(r.getIdEcole());
        }
        if ("PARENT".equals(fk)) {
            return uid == r.getUserId() || (s.getParentId() > 0 && Integer.valueOf(s.getParentId()).equals(r.getIdParent()));
        }
        if ("CHAUFFEUR".equals(fk)) {
            Integer cid = s.getChauffeurId();
            return uid == r.getUserId() || (cid != null && cid.equals(r.getIdChauffeur()));
        }
        if ("MAITRESSE".equals(fk)) {
            Integer mid = s.getMaitresseId();
            return uid == r.getUserId() || (mid != null && mid.equals(r.getIdMaitresse()));
        }
        return uid == r.getUserId();
    }

    /** Réponse / changement de statut rapide autorisé si la fiche est visible et le rôle est agent ou admin. */
    public static boolean canQuickTreat(Reclamation r) {
        return canRespondOrChangeStatut() && canView(r);
    }

    /** Assignation à un autre utilisateur (liste dédiée) : admin uniquement. */
    public static boolean canAssignToOtherUsers() {
        return isAdmin();
    }

    /**
     * Retirer l’assignation : admin toujours ; agent seulement si le ticket lui est assigné.
     */
    public static boolean canReleaseAssignment(Reclamation r) {
        if (r == null || !r.isAssignee() || !canRespondOrChangeStatut()) {
            return false;
        }
        if (isAdmin()) {
            return true;
        }
        Integer uid = AppSession.getInstance().getConnectedUserId();
        return uid != null && uid.equals(r.getUserIdAssigne());
    }

    /**
     * @param nomAssigneResolu {@code users.nom} si connu ; sinon passer {@code null} pour le fallback {@code utilisateur #id}.
     */
    public static String formatAssignationCourte(Reclamation r, String nomAssigneResolu) {
        if (r == null || !r.isAssignee()) {
            return "Non assignée";
        }
        String who;
        if (nomAssigneResolu != null && !nomAssigneResolu.isBlank()) {
            who = nomAssigneResolu.trim();
        } else {
            who = "utilisateur #" + r.getUserIdAssigne();
        }
        return "Responsable : " + who + " (" + roleLabelAssignDisplay(r.getRoleAssigne()) + ")";
    }

    public static String categorieEmoji(String categorie) {
        if (categorie == null) {
            return "📋";
        }
        switch (categorie) {
            case "TRANSPORT":
                return "🚌";
            case "RETARD":
                return "⏱";
            case "CHAUFFEUR":
                return "👤";
            case "SECURITE":
                return "🛡";
            case "ENFANT":
                return "🧒";
            case "ECOLE":
                return "🏫";
            case "TECHNIQUE":
                return "⚙";
            case "AUTRE":
            default:
                return "📌";
        }
    }

    public static String roleLabelShort(String dbRole) {
        if (dbRole == null) {
            return "—";
        }
        switch (dbRole) {
            case "PARENT":
                return "Parent";
            case "CHAUFFEUR":
                return "Chauffeur";
            case "MAITRESSE":
                return "Maîtresse";
            case "AGENT_ECOLE":
                return "Agent";
            case "ADMIN":
                return "Admin";
            default:
                return dbRole;
        }
    }

    /** Libellé métier pour l’assignation / l’historique (ex. Agent École). */
    public static String roleLabelAssignDisplay(String dbRole) {
        if (dbRole == null) {
            return "—";
        }
        switch (dbRole) {
            case "PARENT":
                return "Parent";
            case "CHAUFFEUR":
                return "Chauffeur";
            case "MAITRESSE":
                return "Maîtresse";
            case "AGENT_ECOLE":
            case "RESPONSABLEECOLE":
                return "Agent École";
            case "ADMIN":
                return "Admin";
            default:
                return roleLabelShort(dbRole);
        }
    }

    /**
     * Affichage du créateur à partir de {@code users.nom} si disponible.
     * @param nomResolu valeur {@code users.nom} ou {@code null}
     */
    public static String formatCreateurDisplay(int userId, String nomResolu) {
        if (nomResolu != null && !nomResolu.isBlank()) {
            return nomResolu.trim();
        }
        return "Utilisateur #" + userId;
    }

    public static boolean isStatutCloture(String statut) {
        return "FERMEE".equals(statut) || "REJETEE".equals(statut);
    }

    public static boolean aEteTraitee(String statut) {
        if (statut == null) {
            return false;
        }
        return "REPONDUE".equals(statut) || "FERMEE".equals(statut) || "REJETEE".equals(statut);
    }
}
