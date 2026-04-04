package tn.esprit.workshop.controlleurs.leith.reclamation;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Tooltip;
import javafx.stage.FileChooser;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import tn.esprit.workshop.controlleurs.leith.SceneNavigator;
import tn.esprit.workshop.model.leith.Reclamation;
import tn.esprit.workshop.model.leith.ReponseReclamation;
import tn.esprit.workshop.model.tous.Ecole;
import tn.esprit.workshop.services.leith.EcoleService;
import tn.esprit.workshop.services.leith.ReclamationExportUtil;
import tn.esprit.workshop.services.leith.ReclamationHistoriqueService;
import tn.esprit.workshop.services.leith.ReclamationService;
import tn.esprit.workshop.services.leith.ReponseReclamationService;
import tn.esprit.workshop.utilis.AppSession;
import tn.esprit.workshop.utilis.InputModerationUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.HashSet;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ReclamationListController implements Initializable {

    private static final Logger LOG = Logger.getLogger(ReclamationListController.class.getName());
    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private TextField searchField;
    @FXML private ComboBox<String> cbStatut;
    @FXML private ComboBox<String> cbCategorie;
    @FXML private ComboBox<String> cbPriorite;
    @FXML private ComboBox<String> cbRoleCreateur;
    @FXML private ComboBox<String> cbPeriode;
    @FXML private ComboBox<String> cbTri;
    @FXML private Button btnNouvelle;
    @FXML private HBox kpiBar;
    @FXML private VBox alertBanner;
    @FXML private HBox statsDistributionBar;
    @FXML private FlowPane cardsPane;
    @FXML private Label lblFlash;
    @FXML private Label lblError;
    @FXML private Label lblEmpty;

    private final ReclamationService reclamationService = new ReclamationService();
    private final ReponseReclamationService reponseReclamationService = new ReponseReclamationService();
    private final ReclamationHistoriqueService historiqueService = new ReclamationHistoriqueService();
    private final EcoleService ecoleService = new EcoleService();

    private List<Reclamation> lastLoadedList = Collections.emptyList();
    private Map<Integer, Integer> lastReplyCounts = Collections.emptyMap();
    private ReclamationService.AdvancedStats lastAdvancedStats = new ReclamationService.AdvancedStats();
    private ReclamationService.AlertCounts lastAlertCounts = new ReclamationService.AlertCounts();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cbStatut.getItems().addAll("TOUS", "NOUVELLE", "EN_COURS", "REPONDUE", "FERMEE", "REJETEE");
        cbStatut.getSelectionModel().selectFirst();

        cbCategorie.getItems().addAll(
                "TOUTES", "TRANSPORT", "RETARD", "CHAUFFEUR", "SECURITE", "ENFANT", "ECOLE", "TECHNIQUE", "AUTRE");
        cbCategorie.getSelectionModel().selectFirst();

        cbPriorite.getItems().addAll("TOUTES", "BASSE", "MOYENNE", "HAUTE", "URGENTE");
        cbPriorite.getSelectionModel().selectFirst();

        cbRoleCreateur.getItems().addAll("TOUS", "PARENT", "CHAUFFEUR", "MAITRESSE", "AGENT_ECOLE", "ADMIN");
        cbRoleCreateur.getSelectionModel().selectFirst();

        cbPeriode.getItems().addAll(
                "Toutes les dates",
                "Aujourd’hui",
                "7 derniers jours",
                "30 derniers jours");
        cbPeriode.getSelectionModel().selectFirst();

        cbTri.getItems().addAll(
                "Plus récentes",
                "Plus anciennes",
                "Priorité (haute d’abord)");
        cbTri.getSelectionModel().selectFirst();

        applyComboPopupCss(cbStatut, cbCategorie, cbPriorite, cbRoleCreateur, cbPeriode, cbTri);

        if (AppSession.getInstance().getConnectedUserId() == null) {
            showError("Session invalide : reconnectez-vous.");
            return;
        }
        String flash = AppSession.consumeFlashMessage();
        if (flash != null && lblFlash != null) {
            lblFlash.setText(flash);
            lblFlash.setVisible(true);
            lblFlash.setManaged(true);
        }
        appliquerFiltres();
    }

    private static void applyComboPopupCss(ComboBox<?>... combos) {
        for (ComboBox<?> c : combos) {
            SceneNavigator.applyAppCssToComboBoxPopup(c);
        }
    }

    private String mapPeriodeToPreset() {
        String p = item(cbPeriode, "Toutes les dates");
        if ("Aujourd’hui".equals(p)) {
            return ReclamationService.DATE_PRESET_TODAY;
        }
        if ("7 derniers jours".equals(p)) {
            return ReclamationService.DATE_PRESET_WEEK;
        }
        if ("30 derniers jours".equals(p)) {
            return ReclamationService.DATE_PRESET_MONTH;
        }
        return ReclamationService.DATE_PRESET_ALL;
    }

    private String mapTriToSort() {
        String t = item(cbTri, "Plus récentes");
        if ("Plus anciennes".equals(t)) {
            return ReclamationService.SORT_OLDEST;
        }
        if ("Priorité (haute d’abord)".equals(t)) {
            return ReclamationService.SORT_PRIORITY_HIGH;
        }
        return ReclamationService.SORT_RECENT;
    }

    private static String item(ComboBox<String> cb, String fallback) {
        String v = cb.getSelectionModel().getSelectedItem();
        if (v == null && !cb.getItems().isEmpty()) {
            v = cb.getItems().get(0);
        }
        return v != null ? v : fallback;
    }

    @FXML
    private void appliquerFiltres() {
        hideError();
        lblEmpty.setVisible(false);
        lblEmpty.setManaged(false);
        cardsPane.getChildren().clear();

        AppSession s = AppSession.getInstance();
        Integer uid = s.getConnectedUserId();
        if (uid == null) {
            showError("Session invalide.");
            return;
        }

        String mot = searchField.getText() != null ? searchField.getText().trim() : "";
        String st = item(cbStatut, "TOUS");
        String cat = item(cbCategorie, "TOUTES");
        String prio = item(cbPriorite, "TOUTES");
        String roleCr = item(cbRoleCreateur, "TOUS");
        String datePreset = mapPeriodeToPreset();
        String sortMode = mapTriToSort();

        String roleKey = ReclamationUiHelper.filterRoleKey();
        Integer ecoleId = s.getEcoleId();

        if (roleKey == null) {
            showError("Rôle non pris en charge pour le module Réclamations.");
            return;
        }

        try {
            List<Reclamation> list;
            if ("ADMIN".equals(roleKey)) {
                list = reclamationService.rechercherEtFiltrerAvance(
                        mot, st, cat, prio, roleCr, datePreset, sortMode, "ADMIN", null, null);
            } else if ("AGENT_ECOLE".equals(roleKey)) {
                if (ecoleId == null) {
                    showError("Aucune école associée à votre compte agent.");
                    kpiBar.getChildren().clear();
                    alertBanner.getChildren().clear();
                    statsDistributionBar.getChildren().clear();
                    lastLoadedList = Collections.emptyList();
                    return;
                }
                list = reclamationService.rechercherEtFiltrerAvance(
                        mot, st, cat, prio, roleCr, datePreset, sortMode, "AGENT_ECOLE", uid, ecoleId);
            } else {
                list = reclamationService.rechercherEtFiltrerAvance(
                        mot, st, cat, prio, roleCr, datePreset, sortMode, roleKey, uid, null);
            }

            ReclamationService.DashboardStats stats;
            ReclamationService.AdvancedStats adv;
            ReclamationService.AlertCounts alerts;
            if ("ADMIN".equals(roleKey)) {
                stats = reclamationService.getDashboardStats("ADMIN", null, null);
                adv = reclamationService.getAdvancedStats("ADMIN", null, null);
                alerts = reclamationService.getAlertCounts("ADMIN", null, null);
            } else if ("AGENT_ECOLE".equals(roleKey)) {
                stats = reclamationService.getDashboardStats("AGENT_ECOLE", uid, ecoleId);
                adv = reclamationService.getAdvancedStats("AGENT_ECOLE", uid, ecoleId);
                alerts = reclamationService.getAlertCounts("AGENT_ECOLE", uid, ecoleId);
            } else {
                stats = reclamationService.getDashboardStats(roleKey, uid, null);
                adv = reclamationService.getAdvancedStats(roleKey, uid, null);
                alerts = reclamationService.getAlertCounts(roleKey, uid, null);
            }
            lastAdvancedStats = adv;
            lastAlertCounts = alerts;
            renderKpi(stats);
            renderAlerts(alerts);
            renderDistributionBars(adv);

            if (list.isEmpty()) {
                lastLoadedList = Collections.emptyList();
                lastReplyCounts = Collections.emptyMap();
                lblEmpty.setVisible(true);
                lblEmpty.setManaged(true);
                lblEmpty.setText("Aucun résultat pour ces critères. Élargissez les filtres ou créez une nouvelle réclamation.");
                return;
            }

            lastLoadedList = new ArrayList<>(list);
            List<Integer> ids = list.stream().map(Reclamation::getId).collect(Collectors.toList());
            Map<Integer, Integer> replyCounts = reclamationService.getReplyCountsForReclamationIds(ids);
            lastReplyCounts = replyCounts;

            Set<Integer> userIdsPourNoms = new HashSet<>();
            for (Reclamation r : list) {
                userIdsPourNoms.add(r.getUserId());
                if (r.getUserIdAssigne() != null) {
                    userIdsPourNoms.add(r.getUserIdAssigne());
                }
            }
            Map<Integer, String> displayUserNames = reclamationService.findUserNomsByIds(userIdsPourNoms);

            for (Reclamation r : list) {
                int n = replyCounts.getOrDefault(r.getId(), 0);
                cardsPane.getChildren().add(buildCard(r, n, displayUserNames));
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "liste réclamations", e);
            showError("Impossible de charger les réclamations : " + e.getMessage());
            kpiBar.getChildren().clear();
            alertBanner.getChildren().clear();
            statsDistributionBar.getChildren().clear();
        }
    }

    private void renderKpi(ReclamationService.DashboardStats d) {
        kpiBar.getChildren().clear();
        kpiBar.setSpacing(10);
        kpiBar.getChildren().addAll(
                kpiCard("📊 Total", d.total, "kpi-neutral"),
                kpiCard("🆕 Nouvelles", d.nouvelles, "kpi-info"),
                kpiCard("⚙ En cours", d.enCours, "kpi-warn"),
                kpiCard("✉ Répondues", d.repondues, "kpi-success"),
                kpiCard("✓ Fermées", d.fermees, "kpi-muted"),
                kpiCard("✕ Rejetées", d.rejetees, "kpi-danger"),
                kpiCard("⚠ Urgentes", d.urgentes, "kpi-urgent"));
    }

    private VBox kpiCard(String title, int value, String style) {
        VBox box = new VBox(4);
        box.getStyleClass().addAll("reclamation-kpi-card", style);
        box.setPadding(new Insets(10, 14, 10, 14));
        Label t = new Label(title);
        t.getStyleClass().add("reclamation-kpi-title");
        Label v = new Label(String.valueOf(value));
        v.getStyleClass().add("reclamation-kpi-value");
        box.getChildren().addAll(t, v);
        return box;
    }

    private void renderAlerts(ReclamationService.AlertCounts ac) {
        alertBanner.getChildren().clear();
        if (!ReclamationUiHelper.canRespondOrChangeStatut()) {
            alertBanner.setVisible(false);
            alertBanner.setManaged(false);
            return;
        }
        alertBanner.setVisible(true);
        alertBanner.setManaged(true);
        boolean any = false;
        if (ac.urgentNonTraite > 0) {
            alertBanner.getChildren().add(alertLine(
                    "⚠ " + ac.urgentNonTraite + " réclamation(s) urgente(s) nécessitent une action (NOUVELLE ou EN_COURS).",
                    "reclamation-alert-urgent"));
            any = true;
        }
        if (ac.nouvelles > 0) {
            alertBanner.getChildren().add(alertLine(
                    "🆕 " + ac.nouvelles + " réclamation(s) au statut NOUVELLE dans votre périmètre.",
                    "reclamation-alert-info"));
            any = true;
        }
        if (ac.nonAssignees > 0) {
            alertBanner.getChildren().add(alertLine(
                    "📌 " + ac.nonAssignees + " réclamation(s) ouverte(s) sans responsable assigné.",
                    "reclamation-alert-warn"));
            any = true;
        }
        if (!any) {
            Label ok = new Label("✓ Aucune alerte active dans votre périmètre.");
            ok.getStyleClass().add("reclamation-alert-ok");
            ok.setWrapText(true);
            alertBanner.getChildren().add(ok);
        }
    }

    private static Label alertLine(String text, String styleClass) {
        Label l = new Label(text);
        l.getStyleClass().add(styleClass);
        l.setWrapText(true);
        l.setMaxWidth(920);
        return l;
    }

    private void renderDistributionBars(ReclamationService.AdvancedStats adv) {
        statsDistributionBar.getChildren().clear();
        if (adv.total <= 0) {
            Label l = new Label("Répartition : pas encore de données dans ce périmètre.");
            l.getStyleClass().add("reclamation-hint");
            statsDistributionBar.getChildren().add(l);
            return;
        }
        statsDistributionBar.getChildren().add(buildDistributionColumn("Catégories", adv.parCategorie, adv.total));
        statsDistributionBar.getChildren().add(buildDistributionColumn("Priorités", adv.parPriorite, adv.total));
        statsDistributionBar.getChildren().add(buildEcoleDistributionColumn(adv.parEcole, adv.total));
        VBox rates = new VBox(8);
        rates.getStyleClass().add("reclamation-rate-column");
        Label t = new Label("Indicateurs");
        t.getStyleClass().add("reclamation-stats-col-title");
        Label lr = new Label(String.format(java.util.Locale.FRANCE, "Taux de traitement : %.1f %%", adv.tauxTraitementPct));
        lr.getStyleClass().add("reclamation-rate-label");
        ProgressBar pb1 = new ProgressBar(Math.min(1, adv.tauxTraitementPct / 100.0));
        pb1.getStyleClass().addAll("reclamation-dist-bar", "reclamation-rate-bar");
        pb1.setPrefWidth(180);
        Label lrep = new Label(String.format(java.util.Locale.FRANCE, "Taux de réponse : %.1f %%", adv.tauxReponsePct));
        lrep.getStyleClass().add("reclamation-rate-label");
        ProgressBar pb2 = new ProgressBar(Math.min(1, adv.tauxReponsePct / 100.0));
        pb2.getStyleClass().addAll("reclamation-dist-bar", "reclamation-rate-bar");
        pb2.setPrefWidth(180);
        rates.getChildren().addAll(t, lr, pb1, lrep, pb2);
        statsDistributionBar.getChildren().add(rates);
    }

    private VBox buildDistributionColumn(String title, Map<String, Integer> map, int total) {
        VBox col = new VBox(6);
        col.getStyleClass().add("reclamation-stats-column");
        Label head = new Label(title);
        head.getStyleClass().add("reclamation-stats-col-title");
        col.getChildren().add(head);
        if (map.isEmpty()) {
            col.getChildren().add(new Label("—"));
            return col;
        }
        map.entrySet().stream().limit(5).forEach(e -> {
            double p = total > 0 ? (double) e.getValue() / total : 0;
            Label rowLabel = new Label(e.getKey() + " · " + e.getValue());
            rowLabel.getStyleClass().add("reclamation-dist-label");
            ProgressBar pb = new ProgressBar(p);
            pb.getStyleClass().add("reclamation-dist-bar");
            pb.setPrefWidth(180);
            col.getChildren().add(new VBox(2, rowLabel, pb));
        });
        return col;
    }

    /** Libellés explicites : nom d’école + id + effectif (même scope SQL que la liste). */
    private VBox buildEcoleDistributionColumn(Map<String, Integer> map, int total) {
        VBox col = new VBox(6);
        col.getStyleClass().add("reclamation-stats-column");
        Label head = new Label("Répartition par école");
        head.getStyleClass().add("reclamation-stats-col-title");
        col.getChildren().add(head);
        if (map.isEmpty()) {
            col.getChildren().add(new Label("—"));
            return col;
        }
        map.entrySet().stream().limit(5).forEach(e -> {
            double p = total > 0 ? (double) e.getValue() / total : 0;
            String line = formatEcoleDistributionLine(e.getKey(), e.getValue());
            Label rowLabel = new Label(line);
            rowLabel.getStyleClass().add("reclamation-dist-label");
            rowLabel.setWrapText(true);
            ProgressBar pb = new ProgressBar(p);
            pb.getStyleClass().add("reclamation-dist-bar");
            pb.setPrefWidth(180);
            col.getChildren().add(new VBox(2, rowLabel, pb));
        });
        return col;
    }

    private String formatEcoleDistributionLine(String mapKey, int count) {
        String unit = count == 1 ? "réclamation" : "réclamations";
        if ("Non renseigné".equals(mapKey)) {
            return "Aucune école associée : " + count + " " + unit;
        }
        try {
            int id = Integer.parseInt(mapKey);
            try {
                Ecole ecole = ecoleService.getById(id);
                if (ecole != null) {
                    String nom = ecole.getNomEcole();
                    if (nom != null && !nom.isBlank()) {
                        return nom + " (#" + id + ") : " + count + " " + unit;
                    }
                }
            } catch (SQLException ex) {
                LOG.log(Level.FINE, "nom ecole stats", ex);
            }
            return "École n° " + id + " : " + count + " " + unit;
        } catch (NumberFormatException ex) {
            return mapKey + " : " + count + " " + unit;
        }
    }

    @FXML
    private void exporterListe() {
        if (lastLoadedList == null || lastLoadedList.isEmpty()) {
            showError("Aucune réclamation à exporter (filtrez ou vérifiez la liste).");
            return;
        }
        FileChooser fc = new FileChooser();
        fc.setTitle("Exporter la liste");
        fc.setInitialFileName("reclamations_export.csv");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File f = fc.showSaveDialog(searchField.getScene().getWindow());
        if (f == null) {
            return;
        }
        try {
            Path p = f.toPath();
            ReclamationExportUtil.exportReclamationsCsv(lastLoadedList, p, lastReplyCounts);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "export liste", ex);
            showError("Export impossible : " + ex.getMessage());
        }
    }

    @FXML
    private void exporterStats() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Exporter les statistiques");
        fc.setInitialFileName("reclamations_statistiques.csv");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File f = fc.showSaveDialog(searchField.getScene().getWindow());
        if (f == null) {
            return;
        }
        try {
            ReclamationExportUtil.exportStatsCsv(lastAdvancedStats, lastAlertCounts, f.toPath());
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "export stats", ex);
            showError("Export impossible : " + ex.getMessage());
        }
    }

    private String sessionRoleKeySafe() {
        String k = ReclamationUiHelper.filterRoleKey();
        return k != null ? k : "INCONNU";
    }

    @FXML
    private void nouvelleReclamation() {
        SceneNavigator.navigateReclamationForm();
    }

    private static void setCardActionTooltip(Button b, String text) {
        Tooltip t = new Tooltip(text);
        t.setWrapText(true);
        t.setMaxWidth(280);
        b.setTooltip(t);
    }

    /** Gabarit fixe des cartes (liste) : hauteur identique, corps sans scroll (scroll global de la page). */
    private static final double RECL_CARD_FIXED_HEIGHT = 424;
    private static final double RECL_CARD_HEADER_MAX_HEIGHT = 68;
    private static final double RECL_CARD_MIDDLE_HEIGHT = 204;
    private static final double RECL_CARD_FOOTER_MIN_HEIGHT = 108;
    private static final int CARD_DESC_MAX_LINES = 6;
    private static final int CARD_DESC_MAX_CHARS = 220;

    /**
     * Aperçu carte : limite lignes + caractères, sans scroll interne (détail pour le texte complet).
     */
    private static String[] buildCardDescriptionPreview(String full) {
        if (full == null || full.isBlank()) {
            return new String[] { "—", "false" };
        }
        String work = full.trim();
        boolean truncated = false;
        String[] lines = work.split("\\R", -1);
        if (lines.length > CARD_DESC_MAX_LINES) {
            work = String.join("\n", Arrays.copyOfRange(lines, 0, CARD_DESC_MAX_LINES)).trim() + "…";
            truncated = true;
        }
        if (work.length() > CARD_DESC_MAX_CHARS) {
            work = work.substring(0, CARD_DESC_MAX_CHARS - 1).trim() + "…";
            truncated = true;
        }
        return new String[] { work, Boolean.toString(truncated) };
    }

    private VBox buildCard(Reclamation r, int replyCount, Map<Integer, String> displayUserNames) {
        VBox card = new VBox(8);
        card.getStyleClass().add("reclamation-card");
        card.setMinHeight(RECL_CARD_FIXED_HEIGHT);
        card.setPrefHeight(RECL_CARD_FIXED_HEIGHT);
        card.setMaxHeight(RECL_CARD_FIXED_HEIGHT);

        String prio = r.getPriorite() != null ? r.getPriorite() : "MOYENNE";
        card.getStyleClass().add("reclamation-card-prio-" + prio);
        if (ReclamationUiHelper.isStatutCloture(r.getStatut())) {
            card.getStyleClass().add("reclamation-card-closed");
        }

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);
        top.setMaxHeight(RECL_CARD_HEADER_MAX_HEIGHT);
        top.setMinHeight(Region.USE_PREF_SIZE);
        Label emoji = new Label(ReclamationUiHelper.categorieEmoji(r.getCategorie()));
        emoji.getStyleClass().add("reclamation-card-emoji");
        Label titre = new Label(r.getObjet() != null ? r.getObjet() : "—");
        titre.getStyleClass().add("reclamation-card-title");
        titre.setWrapText(true);
        titre.setMaxWidth(230);
        titre.setMaxHeight(52);
        HBox.setHgrow(titre, Priority.ALWAYS);
        Label prioSigle = new Label(prioSigle(prio));
        prioSigle.getStyleClass().addAll("rec-prio-sigle", "rec-prio-sigle-" + prio);
        top.getChildren().addAll(emoji, titre, prioSigle);

        String fullDesc = r.getDescription() != null ? r.getDescription() : "";
        String[] preview = buildCardDescriptionPreview(fullDesc);
        String excerptText = preview[0];
        boolean descTruncated = Boolean.parseBoolean(preview[1]);

        Label excerpt = new Label(excerptText);
        excerpt.getStyleClass().addAll("reclamation-card-desc", "reclamation-card-desc-clamp");
        excerpt.setWrapText(true);
        excerpt.setMaxWidth(Double.MAX_VALUE);
        if (descTruncated) {
            Tooltip tt = new Tooltip(fullDesc.length() > 800 ? fullDesc.substring(0, 797) + "…" : fullDesc);
            tt.setWrapText(true);
            tt.setMaxWidth(360);
            excerpt.setTooltip(tt);
        }

        VBox descBlock = new VBox(4);
        descBlock.getChildren().add(excerpt);
        if (descTruncated) {
            Hyperlink voirPlus = new Hyperlink("Voir plus");
            voirPlus.getStyleClass().add("reclamation-card-voir-plus");
            voirPlus.setOnAction(e -> {
                AppSession.getInstance().setPendingReclamationDetailId(r.getId());
                SceneNavigator.navigateReclamationDetail();
            });
            descBlock.getChildren().add(voirPlus);
        }

        FlowPane badgesFlow = new FlowPane(8, 6);
        badgesFlow.getStyleClass().add("reclamation-card-badges-flow");
        badgesFlow.setPrefWrapLength(300);
        badgesFlow.getChildren().add(badgeStatut(r.getStatut()));
        badgesFlow.getChildren().add(badgePriorite(prio));
        Label catBadge = new Label(r.getCategorie() != null ? r.getCategorie() : "—");
        catBadge.getStyleClass().addAll("rec-badge", "badge-categorie");
        badgesFlow.getChildren().add(catBadge);
        if (!r.isAssignee()) {
            Label na = new Label("Non assignée");
            na.getStyleClass().addAll("rec-badge", "badge-assign-none");
            badgesFlow.getChildren().add(na);
        } else {
            String nomA = displayUserNames != null ? displayUserNames.get(r.getUserIdAssigne()) : null;
            String badgeText = (nomA != null && !nomA.isBlank())
                    ? nomA.trim()
                    : "utilisateur #" + r.getUserIdAssigne();
            Label as = new Label(badgeText);
            as.getStyleClass().addAll("rec-badge", "badge-assign-ok");
            badgesFlow.getChildren().add(as);
        }

        String nomAssigne = (r.isAssignee() && displayUserNames != null)
                ? displayUserNames.get(r.getUserIdAssigne())
                : null;
        Label assignLbl = new Label(ReclamationUiHelper.formatAssignationCourte(r, nomAssigne));
        assignLbl.getStyleClass().add("reclamation-card-assign");
        assignLbl.setWrapText(true);
        assignLbl.setMaxWidth(Double.MAX_VALUE);
        if (!r.isAssignee() && !ReclamationUiHelper.isStatutCloture(r.getStatut())) {
            assignLbl.getStyleClass().add("reclamation-card-unassigned");
        }

        Label replyBadge = new Label(replyCount == 0 ? "0 réponse" : replyCount + " rép.");
        replyBadge.getStyleClass().add("reclamation-reply-count");

        String dateStr = r.getDateCreation() != null
                ? r.getDateCreation().toLocalDateTime().format(DF)
                : "—";
        String nomCreateur = displayUserNames != null ? displayUserNames.get(r.getUserId()) : null;
        String ligneCreateur = ReclamationUiHelper.formatCreateurDisplay(r.getUserId(), nomCreateur);
        Label meta = new Label(dateStr + " · " + ligneCreateur + " · " + ReclamationUiHelper.roleLabelShort(r.getRoleCreateur()));
        meta.getStyleClass().add("reclamation-card-meta");
        meta.setWrapText(true);
        meta.setMaxWidth(Double.MAX_VALUE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox metaRow = new HBox(10);
        metaRow.setAlignment(Pos.CENTER_LEFT);
        metaRow.getChildren().addAll(meta, spacer, replyBadge);

        VBox middle = new VBox(8);
        middle.setFillWidth(true);
        middle.getStyleClass().add("reclamation-card-middle");
        middle.setMinHeight(RECL_CARD_MIDDLE_HEIGHT);
        middle.setPrefHeight(RECL_CARD_MIDDLE_HEIGHT);
        middle.setMaxHeight(RECL_CARD_MIDDLE_HEIGHT);
        middle.getChildren().addAll(descBlock, badgesFlow, assignLbl, metaRow);
        Rectangle middleClip = new Rectangle();
        middleClip.widthProperty().bind(middle.widthProperty());
        middleClip.heightProperty().bind(middle.heightProperty());
        middle.setClip(middleClip);

        FlowPane actionsFlow = new FlowPane(8, 8);
        actionsFlow.setAlignment(Pos.TOP_LEFT);
        actionsFlow.setPrefWrapLength(310);
        actionsFlow.getStyleClass().add("reclamation-card-actions-flow");

        Button voir = new Button("Détail");
        voir.getStyleClass().addAll("btn-soft", "reclamation-card-action-btn");
        setCardActionTooltip(voir, "Afficher le détail de la réclamation");
        voir.setOnAction(e -> {
            AppSession.getInstance().setPendingReclamationDetailId(r.getId());
            SceneNavigator.navigateReclamationDetail();
        });
        actionsFlow.getChildren().add(voir);

        Integer curUid = AppSession.getInstance().getConnectedUserId();
        if (ReclamationUiHelper.canQuickTreat(r)) {
            boolean ouverte = !ReclamationUiHelper.isStatutCloture(r.getStatut());
            if (ouverte && curUid != null && (r.getUserIdAssigne() == null || curUid.equals(r.getUserIdAssigne()))) {
                Button pc = new Button("Prendre en charge");
                pc.getStyleClass().addAll("btn-soft", "reclamation-card-action-btn");
                setCardActionTooltip(pc, "Prendre en charge");
                pc.setOnAction(e -> quickPrendreCharge(r.getId()));
                actionsFlow.getChildren().add(pc);
            }
            if (ouverte) {
                Button repondre = new Button("Répondre");
                repondre.getStyleClass().addAll("btn-soft", "reclamation-card-action-btn");
                setCardActionTooltip(repondre, "Répondre rapidement");
                repondre.setOnAction(e -> quickReply(r));
                actionsFlow.getChildren().add(repondre);

                if ("NOUVELLE".equals(r.getStatut()) || "REPONDUE".equals(r.getStatut())) {
                    Button enCours = new Button("En cours");
                    enCours.getStyleClass().addAll("btn-soft", "reclamation-card-action-btn");
                    setCardActionTooltip(enCours, "Mettre en cours");
                    enCours.setOnAction(e -> quickStatut(r.getId(), ReclamationService.STATUT_EN_COURS));
                    actionsFlow.getChildren().add(enCours);
                }
                Button fermer = new Button("Fermer");
                fermer.getStyleClass().addAll("btn-soft", "reclamation-card-action-btn");
                setCardActionTooltip(fermer, "Fermer la réclamation");
                fermer.setOnAction(e -> quickStatut(r.getId(), ReclamationService.STATUT_FERMEE));
                actionsFlow.getChildren().add(fermer);

                Button rejeter = new Button("Rejeter");
                rejeter.getStyleClass().addAll("btn-danger-soft", "reclamation-card-action-btn");
                setCardActionTooltip(rejeter, "Rejeter la réclamation");
                rejeter.setOnAction(e -> quickStatut(r.getId(), ReclamationService.STATUT_REJETEE));
                actionsFlow.getChildren().add(rejeter);
            }
        }

        VBox footer = new VBox();
        footer.getStyleClass().add("reclamation-card-footer");
        footer.setMinHeight(RECL_CARD_FOOTER_MIN_HEIGHT);
        footer.setPrefHeight(RECL_CARD_FOOTER_MIN_HEIGHT);
        footer.setMaxHeight(RECL_CARD_FOOTER_MIN_HEIGHT);
        footer.getChildren().add(actionsFlow);

        card.getChildren().addAll(top, middle, footer);
        return card;
    }

    private static String prioSigle(String p) {
        switch (p) {
            case "BASSE":
                return "B";
            case "MOYENNE":
                return "M";
            case "HAUTE":
                return "H";
            case "URGENTE":
                return "!";
            default:
                return "·";
        }
    }

    private void quickStatut(int reclamationId, String nouveau) {
        try {
            Reclamation r = reclamationService.getById(reclamationId);
            if (r == null || !ReclamationUiHelper.canQuickTreat(r)) {
                return;
            }
            if (ReclamationUiHelper.isStatutCloture(r.getStatut())) {
                return;
            }
            String old = r.getStatut();
            if (!reclamationService.modifierStatut(reclamationId, nouveau)) {
                return;
            }
            Integer uid = AppSession.getInstance().getConnectedUserId();
            if (uid != null) {
                historiqueService.enregistrer(reclamationId, ReclamationHistoriqueService.ACTION_CHANGEMENT_STATUT,
                        old, nouveau, null, null, null, uid, sessionRoleKeySafe());
            }
            appliquerFiltres();
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, "quick statut", ex);
            showError("Action impossible : " + ex.getMessage());
        }
    }

    private void quickPrendreCharge(int reclamationId) {
        try {
            Reclamation r = reclamationService.getById(reclamationId);
            if (r == null || !ReclamationUiHelper.canQuickTreat(r)) {
                return;
            }
            Integer uid = AppSession.getInstance().getConnectedUserId();
            if (uid == null) {
                return;
            }
            String dbRole = ReclamationUiHelper.responderDbRole();
            if (dbRole == null) {
                return;
            }
            reclamationService.prendreEnCharge(r, uid, dbRole, historiqueService, sessionRoleKeySafe());
            appliquerFiltres();
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, "quick prendre en charge", ex);
            showError(ex.getMessage() != null ? ex.getMessage() : "Prise en charge impossible.");
        }
    }

    private void quickReply(Reclamation r) {
        if (!ReclamationUiHelper.canQuickTreat(r) || ReclamationUiHelper.isStatutCloture(r.getStatut())) {
            return;
        }
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Réponse rapide");
        dialog.setHeaderText("Réclamation : " + (r.getObjet() != null ? r.getObjet() : "#" + r.getId()));

        TextArea ta = new TextArea();
        ta.setPromptText("Votre message…");
        ta.setWrapText(true);
        ta.setPrefRowCount(5);
        ta.setMinWidth(420);
        ta.getStyleClass().add("reclamation-field");

        DialogPane pane = dialog.getDialogPane();
        pane.setContent(ta);
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        ObservableList<String> sheets = pane.getStylesheets();
        sheets.clear();
        addStylesheet(sheets, getClass().getResource("/leith/design/global.css"));
        addStylesheet(sheets, getClass().getResource("/leith/app.css"));
        addStylesheet(sheets, getClass().getResource("/leith/reclamation/reclamation.css"));

        dialog.showAndWait().filter(bt -> bt == ButtonType.OK).ifPresent(bt -> {
            String msg = ta.getText() != null ? ta.getText().trim() : "";
            if (msg.isEmpty()) {
                return;
            }
            if (InputModerationUtil.isBlocked(msg)) {
                showError(InputModerationUtil.MESSAGE_BLOQUE);
                return;
            }
            String roleDb = ReclamationUiHelper.responderDbRole();
            if (roleDb == null) {
                return;
            }
            Integer uid = AppSession.getInstance().getConnectedUserId();
            if (uid == null) {
                return;
            }
            try {
                ReponseReclamation rr = new ReponseReclamation();
                rr.setReclamationId(r.getId());
                rr.setMessage(msg);
                rr.setRoleRepondeur(roleDb);
                rr.setUserId(uid);
                reponseReclamationService.ajouter(rr);
                String excerpt = msg.length() > 160 ? msg.substring(0, 157) + "…" : msg;
                historiqueService.enregistrer(r.getId(), ReclamationHistoriqueService.ACTION_REPONSE,
                        null, null, null, null, excerpt, uid, sessionRoleKeySafe());

                Reclamation cur = reclamationService.getById(r.getId());
                if (cur != null) {
                    String st = cur.getStatut();
                    if (ReclamationService.STATUT_NOUVELLE.equals(st) || ReclamationService.STATUT_EN_COURS.equals(st)) {
                        reclamationService.modifierStatut(r.getId(), ReclamationService.STATUT_REPONDUE);
                        historiqueService.enregistrer(r.getId(), ReclamationHistoriqueService.ACTION_CHANGEMENT_STATUT,
                                st, ReclamationService.STATUT_REPONDUE, null, null,
                                "Passage à REPONDUE après réponse rapide", uid, sessionRoleKeySafe());
                    }
                }
                appliquerFiltres();
            } catch (SQLException ex) {
                LOG.log(Level.SEVERE, "quick reply", ex);
                showError("Envoi impossible : " + ex.getMessage());
            }
        });
    }

    private Label badgeStatut(String statut) {
        Label l = new Label(statut != null ? statut : "—");
        l.getStyleClass().addAll("rec-badge", "badge-statut-" + (statut != null ? statut : "NOUVELLE"));
        return l;
    }

    private Label badgePriorite(String p) {
        String key = p != null ? p : "MOYENNE";
        Label l = new Label(key);
        l.getStyleClass().addAll("rec-badge", "badge-prio-" + key);
        return l;
    }

    private void showError(String msg) {
        lblError.setText(msg);
        lblError.setVisible(true);
        lblError.setManaged(true);
    }

    private void hideError() {
        lblError.setVisible(false);
        lblError.setManaged(false);
    }

    private static void addStylesheet(ObservableList<String> sheets, URL url) {
        if (url != null) {
            sheets.add(url.toExternalForm());
        }
    }
}
