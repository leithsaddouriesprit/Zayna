package tn.esprit.workshop.services.leith;

import tn.esprit.workshop.model.leith.Reclamation;
import tn.esprit.workshop.model.leith.ReclamationHistorique;
import tn.esprit.workshop.model.leith.ReponseReclamation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Export CSV (UTF-8 avec BOM pour Excel) — périmètre déjà filtré par l’appelant.
 */
public final class ReclamationExportUtil {

    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private ReclamationExportUtil() {
    }

    private static String csvCell(Object o) {
        if (o == null) {
            return "";
        }
        String s = String.valueOf(o).replace("\"", "\"\"");
        if (s.contains(";") || s.contains("\n") || s.contains("\"")) {
            return "\"" + s + "\"";
        }
        return s;
    }

    public static void exportReclamationsCsv(List<Reclamation> list, Path path, Map<Integer, Integer> replyCounts)
            throws IOException {
        StringBuilder sb = new StringBuilder("\uFEFF");
        sb.append("id;objet;statut;priorite;categorie;role_createur;user_id;user_id_assigne;role_assigne;date_assignation;")
                .append("id_ecole;date_creation;date_modification;nb_reponses\n");
        for (Reclamation r : list) {
            int nb = replyCounts != null ? replyCounts.getOrDefault(r.getId(), 0) : 0;
            sb.append(csvCell(r.getId())).append(';');
            sb.append(csvCell(r.getObjet())).append(';');
            sb.append(csvCell(r.getStatut())).append(';');
            sb.append(csvCell(r.getPriorite())).append(';');
            sb.append(csvCell(r.getCategorie())).append(';');
            sb.append(csvCell(r.getRoleCreateur())).append(';');
            sb.append(csvCell(r.getUserId())).append(';');
            sb.append(csvCell(r.getUserIdAssigne())).append(';');
            sb.append(csvCell(r.getRoleAssigne())).append(';');
            sb.append(csvCell(r.getDateAssignation() != null ? r.getDateAssignation().toLocalDateTime().format(DF) : ""))
                    .append(';');
            sb.append(csvCell(r.getIdEcole())).append(';');
            sb.append(csvCell(r.getDateCreation() != null ? r.getDateCreation().toLocalDateTime().format(DF) : ""))
                    .append(';');
            sb.append(csvCell(r.getDateModification() != null ? r.getDateModification().toLocalDateTime().format(DF) : ""))
                    .append(';');
            sb.append(nb).append('\n');
        }
        Files.writeString(path, sb.toString(), StandardCharsets.UTF_8);
    }

    public static void exportStatsCsv(
            ReclamationService.AdvancedStats adv,
            ReclamationService.AlertCounts alerts,
            Path path) throws IOException {
        StringBuilder sb = new StringBuilder("\uFEFF");
        sb.append("indicateur;valeur\n");
        sb.append("total;").append(adv.total).append('\n');
        sb.append("traitees_fermees_ou_rejetees;").append(adv.traitees).append('\n');
        sb.append("taux_traitement_pct;").append(String.format(java.util.Locale.FRANCE, "%.2f", adv.tauxTraitementPct))
                .append('\n');
        sb.append("avec_reponse;").append(adv.avecReponse).append('\n');
        sb.append("taux_reponse_pct;").append(String.format(java.util.Locale.FRANCE, "%.2f", adv.tauxReponsePct))
                .append('\n');
        sb.append("alertes_nouvelles;").append(alerts.nouvelles).append('\n');
        sb.append("alertes_urgentes_non_traitees;").append(alerts.urgentNonTraite).append('\n');
        sb.append("alertes_non_assignees;").append(alerts.nonAssignees).append('\n');
        sb.append("\n# par_categorie\n");
        sb.append("categorie;count\n");
        for (Map.Entry<String, Integer> e : adv.parCategorie.entrySet()) {
            sb.append(csvCell(e.getKey())).append(';').append(e.getValue()).append('\n');
        }
        sb.append("\n# par_priorite\n");
        sb.append("priorite;count\n");
        for (Map.Entry<String, Integer> e : adv.parPriorite.entrySet()) {
            sb.append(csvCell(e.getKey())).append(';').append(e.getValue()).append('\n');
        }
        sb.append("\n# par_role_createur\n");
        sb.append("role;count\n");
        for (Map.Entry<String, Integer> e : adv.parRoleCreateur.entrySet()) {
            sb.append(csvCell(e.getKey())).append(';').append(e.getValue()).append('\n');
        }
        sb.append("\n# par_ecole\n");
        sb.append("id_ecole_ou_libelle;count\n");
        for (Map.Entry<String, Integer> e : adv.parEcole.entrySet()) {
            sb.append(csvCell(e.getKey())).append(';').append(e.getValue()).append('\n');
        }
        Files.writeString(path, sb.toString(), StandardCharsets.UTF_8);
    }

    /** Rapport texte CSV multi-sections : fiche + réponses + historique. */
    public static void exportDetailRapportCsv(
            Reclamation r,
            List<ReponseReclamation> reponses,
            List<ReclamationHistorique> historique,
            Path path) throws IOException {
        StringBuilder sb = new StringBuilder("\uFEFF");
        sb.append("# RECLAMATION\n");
        sb.append("id;").append(r.getId()).append('\n');
        sb.append("objet;").append(csvCell(r.getObjet())).append('\n');
        sb.append("statut;").append(csvCell(r.getStatut())).append('\n');
        sb.append("priorite;").append(csvCell(r.getPriorite())).append('\n');
        sb.append("categorie;").append(csvCell(r.getCategorie())).append('\n');
        sb.append("user_id_assigne;").append(csvCell(r.getUserIdAssigne())).append('\n');
        sb.append("role_assigne;").append(csvCell(r.getRoleAssigne())).append('\n');
        sb.append("\n# REPONSES\n");
        sb.append("date;role;user_id;message\n");
        for (ReponseReclamation rr : reponses) {
            sb.append(csvCell(rr.getDateReponse() != null ? rr.getDateReponse().toLocalDateTime().format(DF) : ""))
                    .append(';');
            sb.append(csvCell(rr.getRoleRepondeur())).append(';');
            sb.append(rr.getUserId()).append(';');
            sb.append(csvCell(rr.getMessage())).append('\n');
        }
        sb.append("\n# HISTORIQUE\n");
        sb.append("date;action;ancien_statut;nouveau_statut;ancien_assigne;nouveau_assigne;acteur_user;role_acteur;detail\n");
        for (ReclamationHistorique h : historique) {
            sb.append(csvCell(h.getDateAction() != null ? h.getDateAction().toLocalDateTime().format(DF) : ""))
                    .append(';');
            sb.append(csvCell(h.getActionType())).append(';');
            sb.append(csvCell(h.getAncienStatut())).append(';');
            sb.append(csvCell(h.getNouveauStatut())).append(';');
            sb.append(csvCell(h.getAncienAssigne())).append(';');
            sb.append(csvCell(h.getNouveauAssigne())).append(';');
            sb.append(h.getUserIdAction()).append(';');
            sb.append(csvCell(h.getRoleAction())).append(';');
            sb.append(csvCell(h.getMessageAction())).append('\n');
        }
        Files.writeString(path, sb.toString(), StandardCharsets.UTF_8);
    }
}
