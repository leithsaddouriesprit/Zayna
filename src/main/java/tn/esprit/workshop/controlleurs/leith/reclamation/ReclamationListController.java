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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import tn.esprit.workshop.controlleurs.leith.SceneNavigator;
import tn.esprit.workshop.model.leith.Reclamation;
import tn.esprit.workshop.model.leith.ReponseReclamation;
import tn.esprit.workshop.services.leith.ReclamationService;
import tn.esprit.workshop.services.leith.ReponseReclamationService;
import tn.esprit.workshop.utilis.AppSession;

import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
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
    @FXML private FlowPane cardsPane;
    @FXML private Label lblFlash;
    @FXML private Label lblError;
    @FXML private Label lblEmpty;

    private final ReclamationService reclamationService = new ReclamationService();
    private final ReponseReclamationService reponseReclamationService = new ReponseReclamationService();

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
                    return;
                }
                list = reclamationService.rechercherEtFiltrerAvance(
                        mot, st, cat, prio, roleCr, datePreset, sortMode, "AGENT_ECOLE", uid, ecoleId);
            } else {
                list = reclamationService.rechercherEtFiltrerAvance(
                        mot, st, cat, prio, roleCr, datePreset, sortMode, roleKey, uid, null);
            }

            ReclamationService.DashboardStats stats;
            if ("ADMIN".equals(roleKey)) {
                stats = reclamationService.getDashboardStats("ADMIN", null, null);
            } else if ("AGENT_ECOLE".equals(roleKey)) {
                stats = reclamationService.getDashboardStats("AGENT_ECOLE", uid, ecoleId);
            } else {
                stats = reclamationService.getDashboardStats(roleKey, uid, null);
            }
            renderKpi(stats);

            if (list.isEmpty()) {
                lblEmpty.setVisible(true);
                lblEmpty.setManaged(true);
                lblEmpty.setText("Aucun résultat pour ces critères. Élargissez les filtres ou créez une nouvelle réclamation.");
                return;
            }

            List<Integer> ids = list.stream().map(Reclamation::getId).collect(Collectors.toList());
            Map<Integer, Integer> replyCounts = reclamationService.getReplyCountsForReclamationIds(ids);

            for (Reclamation r : list) {
                int n = replyCounts.getOrDefault(r.getId(), 0);
                cardsPane.getChildren().add(buildCard(r, n));
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "liste réclamations", e);
            showError("Impossible de charger les réclamations : " + e.getMessage());
            kpiBar.getChildren().clear();
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

    @FXML
    private void nouvelleReclamation() {
        SceneNavigator.navigateReclamationForm();
    }

    private VBox buildCard(Reclamation r, int replyCount) {
        VBox card = new VBox(10);
        card.getStyleClass().add("reclamation-card");
        String prio = r.getPriorite() != null ? r.getPriorite() : "MOYENNE";
        card.getStyleClass().add("reclamation-card-prio-" + prio);
        if (ReclamationUiHelper.isStatutCloture(r.getStatut())) {
            card.getStyleClass().add("reclamation-card-closed");
        }

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);
        Label emoji = new Label(ReclamationUiHelper.categorieEmoji(r.getCategorie()));
        emoji.getStyleClass().add("reclamation-card-emoji");
        Label titre = new Label(r.getObjet() != null ? r.getObjet() : "—");
        titre.getStyleClass().add("reclamation-card-title");
        titre.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(titre, Priority.ALWAYS);
        Label prioSigle = new Label(prioSigle(prio));
        prioSigle.getStyleClass().addAll("rec-prio-sigle", "rec-prio-sigle-" + prio);
        top.getChildren().addAll(emoji, titre, prioSigle);

        String desc = r.getDescription() != null ? r.getDescription() : "";
        if (desc.length() > 140) {
            desc = desc.substring(0, 137) + "…";
        }
        Label excerpt = new Label(desc.isEmpty() ? "—" : desc);
        excerpt.getStyleClass().add("reclamation-card-desc");

        HBox badges = new HBox(8);
        badges.setAlignment(Pos.CENTER_LEFT);
        badges.getChildren().add(badgeStatut(r.getStatut()));
        badges.getChildren().add(badgePriorite(prio));
        Label catBadge = new Label(r.getCategorie() != null ? r.getCategorie() : "—");
        catBadge.getStyleClass().addAll("rec-badge", "badge-categorie");
        badges.getChildren().add(catBadge);

        Label replyBadge = new Label(replyCount == 0 ? "0 réponse" : replyCount + " rép.");
        replyBadge.getStyleClass().add("reclamation-reply-count");

        String dateStr = r.getDateCreation() != null
                ? r.getDateCreation().toLocalDateTime().format(DF)
                : "—";
        Label meta = new Label(dateStr + " · " + ReclamationUiHelper.roleLabelShort(r.getRoleCreateur()));
        meta.getStyleClass().add("reclamation-card-meta");

        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.getStyleClass().add("reclamation-card-actions");

        Button voir = new Button("Détail");
        voir.getStyleClass().add("btn-soft");
        voir.setOnAction(e -> {
            AppSession.getInstance().setPendingReclamationDetailId(r.getId());
            SceneNavigator.navigateReclamationDetail();
        });
        actions.getChildren().add(voir);

        if (ReclamationUiHelper.canQuickTreat(r)) {
            boolean ouverte = !ReclamationUiHelper.isStatutCloture(r.getStatut());
            if (ouverte) {
                Button repondre = new Button("Répondre");
                repondre.getStyleClass().add("btn-soft");
                repondre.setOnAction(e -> quickReply(r));
                actions.getChildren().add(repondre);

                if ("NOUVELLE".equals(r.getStatut()) || "REPONDUE".equals(r.getStatut())) {
                    Button enCours = new Button("En cours");
                    enCours.getStyleClass().add("btn-soft");
                    enCours.setOnAction(e -> quickStatut(r.getId(), ReclamationService.STATUT_EN_COURS));
                    actions.getChildren().add(enCours);
                }
                Button fermer = new Button("Fermer");
                fermer.getStyleClass().add("btn-soft");
                fermer.setOnAction(e -> quickStatut(r.getId(), ReclamationService.STATUT_FERMEE));
                actions.getChildren().add(fermer);

                Button rejeter = new Button("Rejeter");
                rejeter.getStyleClass().add("btn-danger-soft");
                rejeter.setOnAction(e -> quickStatut(r.getId(), ReclamationService.STATUT_REJETEE));
                actions.getChildren().add(rejeter);
            }
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox metaRow = new HBox(10);
        metaRow.setAlignment(Pos.CENTER_LEFT);
        metaRow.getChildren().addAll(meta, spacer, replyBadge);

        card.getChildren().addAll(top, excerpt, badges, metaRow, actions);
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
            reclamationService.modifierStatut(reclamationId, nouveau);
            appliquerFiltres();
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, "quick statut", ex);
            showError("Action impossible : " + ex.getMessage());
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

                Reclamation cur = reclamationService.getById(r.getId());
                if (cur != null) {
                    String st = cur.getStatut();
                    if (ReclamationService.STATUT_NOUVELLE.equals(st) || ReclamationService.STATUT_EN_COURS.equals(st)) {
                        reclamationService.modifierStatut(r.getId(), ReclamationService.STATUT_REPONDUE);
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
