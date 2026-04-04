package tn.esprit.workshop.controlleurs.leith.reclamation;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import tn.esprit.workshop.controlleurs.leith.SceneNavigator;
import tn.esprit.workshop.model.leith.Bus;
import tn.esprit.workshop.model.leith.Reclamation;
import tn.esprit.workshop.model.leith.ReclamationHistorique;
import tn.esprit.workshop.model.leith.ReponseReclamation;
import tn.esprit.workshop.model.leith.Trajet;
import tn.esprit.workshop.model.tous.Ecole;
import tn.esprit.workshop.services.leith.BusService;
import tn.esprit.workshop.services.leith.EcoleService;
import tn.esprit.workshop.services.leith.ReclamationExportUtil;
import tn.esprit.workshop.services.leith.ReclamationHistoriqueService;
import tn.esprit.workshop.services.leith.ReclamationService;
import tn.esprit.workshop.services.leith.ReponseReclamationService;
import tn.esprit.workshop.services.leith.TrajetService;
import tn.esprit.workshop.utilis.AppSession;
import tn.esprit.workshop.utilis.InputModerationUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReclamationDetailController implements Initializable {

    private static final Logger LOG = Logger.getLogger(ReclamationDetailController.class.getName());
    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private Label lblEmoji;
    @FXML private Label lblObjet;
    @FXML private HBox badgeRow;
    @FXML private Label lblResumeTraitement;
    @FXML private Label lblSynthStatut;
    @FXML private Label lblSynthPriorite;
    @FXML private Label lblSynthAssign;
    @FXML private Label lblSynthReponses;
    @FXML private Label lblSynthModif;
    @FXML private Label lblSynthDerniereAction;
    @FXML private Label lblDescription;
    @FXML private Label lblCreateur;
    @FXML private Label lblRoleCreateur;
    @FXML private Label lblDateCreation;
    @FXML private Label lblDateModification;
    @FXML private Label lblEcole;
    @FXML private Label lblBus;
    @FXML private Label lblTrajet;
    @FXML private Label lblAucuneReponse;
    @FXML private VBox reponsesBox;
    @FXML private VBox historiqueBox;
    @FXML private Label lblHistoriqueVide;
    @FXML private VBox boxTraitement;
    @FXML private Label lblAssignInfo;
    @FXML private Button btnPrendreCharge;
    @FXML private ComboBox<ReclamationService.AssignCandidate> cbAssignUser;
    @FXML private Button btnAssigner;
    @FXML private Button btnLibererAssign;
    @FXML private ComboBox<String> cbStatut;
    @FXML private TextArea taReponse;
    @FXML private Label lblTraitementMsg;

    private final ReclamationService reclamationService = new ReclamationService();
    private final ReponseReclamationService reponseReclamationService = new ReponseReclamationService();
    private final ReclamationHistoriqueService historiqueService = new ReclamationHistoriqueService();
    private final EcoleService ecoleService = new EcoleService();
    private final BusService busService = new BusService();
    private final TrajetService trajetService = new TrajetService();

    private int reclamationId;
    private Reclamation reclamation;
    private List<ReclamationHistorique> dernierHistorique = Collections.emptyList();
    /** {@code users.nom} du responsable assigné, si résolu. */
    private String resolvedAssigneeNom;
    private Map<Integer, String> historiqueUserNames = Collections.emptyMap();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cbStatut.getItems().addAll(
                ReclamationService.STATUT_NOUVELLE,
                ReclamationService.STATUT_EN_COURS,
                ReclamationService.STATUT_REPONDUE,
                ReclamationService.STATUT_FERMEE,
                ReclamationService.STATUT_REJETEE);
        SceneNavigator.applyAppCssToComboBoxPopup(cbStatut);

        if (ReclamationUiHelper.canAssignToOtherUsers() && ReclamationUiHelper.canRespondOrChangeStatut()) {
            try {
                List<ReclamationService.AssignCandidate> cands = reclamationService.listAssignableUsersForAdmin();
                cbAssignUser.getItems().setAll(cands);
                cbAssignUser.setVisible(true);
                cbAssignUser.setManaged(true);
                btnAssigner.setVisible(true);
                btnAssigner.setManaged(true);
                cbAssignUser.setCellFactory(lv -> new ListCell<>() {
                    @Override
                    protected void updateItem(ReclamationService.AssignCandidate item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty || item == null ? null : item.displayLabel);
                    }
                });
                cbAssignUser.setButtonCell(new ListCell<>() {
                    @Override
                    protected void updateItem(ReclamationService.AssignCandidate item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty || item == null ? null : item.displayLabel);
                    }
                });
                SceneNavigator.applyAppCssToComboBoxPopup(cbAssignUser);
            } catch (SQLException e) {
                LOG.log(Level.WARNING, "chargement utilisateurs assignables", e);
            }
        }

        Integer pending = AppSession.getInstance().getAndClearPendingReclamationDetailId();
        if (pending == null || pending <= 0) {
            Platform.runLater(SceneNavigator::navigateReclamationList);
            return;
        }
        reclamationId = pending;

        boolean canTreat = ReclamationUiHelper.canRespondOrChangeStatut();
        boxTraitement.setVisible(canTreat);
        boxTraitement.setManaged(canTreat);

        charger();
    }

    private String sessionRoleKeySafe() {
        String k = ReclamationUiHelper.filterRoleKey();
        return k != null ? k : "INCONNU";
    }

    private void charger() {
        hideTraitementMsg();
        try {
            reclamation = reclamationService.getById(reclamationId);
            if (reclamation == null || !ReclamationUiHelper.canView(reclamation)) {
                Platform.runLater(SceneNavigator::navigateReclamationList);
                return;
            }

            resolvedAssigneeNom = null;
            List<Integer> idsPourNoms = new ArrayList<>(2);
            idsPourNoms.add(reclamation.getUserId());
            if (reclamation.isAssignee() && reclamation.getUserIdAssigne() != null) {
                idsPourNoms.add(reclamation.getUserIdAssigne());
            }
            Map<Integer, String> nomsCharges = reclamationService.findUserNomsByIds(idsPourNoms);
            resolvedAssigneeNom = reclamation.isAssignee() && reclamation.getUserIdAssigne() != null
                    ? nomsCharges.get(reclamation.getUserIdAssigne())
                    : null;

            lblEmoji.setText(ReclamationUiHelper.categorieEmoji(reclamation.getCategorie()));
            lblObjet.setText(reclamation.getObjet() != null ? reclamation.getObjet() : "—");
            lblDescription.setText(reclamation.getDescription() != null ? reclamation.getDescription() : "—");

            lblCreateur.setText(ReclamationUiHelper.formatCreateurDisplay(
                    reclamation.getUserId(), nomsCharges.get(reclamation.getUserId())));
            lblRoleCreateur.setText(ReclamationUiHelper.roleLabelShort(reclamation.getRoleCreateur()));
            lblDateCreation.setText(reclamation.getDateCreation() != null
                    ? reclamation.getDateCreation().toLocalDateTime().format(DF)
                    : "—");
            lblDateModification.setText(reclamation.getDateModification() != null
                    ? reclamation.getDateModification().toLocalDateTime().format(DF)
                    : "—");

            lblResumeTraitement.setText(buildResumeTraitement(reclamation));

            lblEcole.setText(formatEcole(reclamation.getIdEcole()));
            lblBus.setText(formatBus(reclamation.getIdBus()));
            lblTrajet.setText(formatTrajet(reclamation.getIdTrajet()));

            badgeRow.getChildren().clear();
            badgeRow.getChildren().add(makeBadgeCategorie(reclamation.getCategorie()));
            badgeRow.getChildren().add(makeBadgeStatut(reclamation.getStatut()));
            badgeRow.getChildren().add(makeBadgePriorite(reclamation.getPriorite()));
            if (!reclamation.isAssignee()) {
                Label na = new Label("Non assignée");
                na.getStyleClass().addAll("rec-badge", "badge-assign-none");
                badgeRow.getChildren().add(na);
            } else {
                Label ab = new Label("Assignée");
                ab.getStyleClass().addAll("rec-badge", "badge-assign-ok");
                badgeRow.getChildren().add(ab);
            }

            if (ReclamationUiHelper.canRespondOrChangeStatut()) {
                cbStatut.getSelectionModel().select(reclamation.getStatut());
            }

            afficherReponses();
            afficherHistorique();
            remplirSynthese();

            if (ReclamationUiHelper.canRespondOrChangeStatut()) {
                majBoutonsAssignation();
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "detail reclamation", e);
            Platform.runLater(SceneNavigator::navigateReclamationList);
        }
    }

    private void remplirSynthese() throws SQLException {
        lblSynthStatut.setText(reclamation.getStatut() != null ? reclamation.getStatut() : "—");
        lblSynthPriorite.setText(reclamation.getPriorite() != null ? reclamation.getPriorite() : "—");
        lblSynthAssign.setText(formatAssignLong(reclamation));
        int n = reclamationService.getReplyCountForReclamation(reclamationId);
        lblSynthReponses.setText(String.valueOf(n));
        lblSynthModif.setText(reclamation.getDateModification() != null
                ? reclamation.getDateModification().toLocalDateTime().format(DF)
                : "—");
        if (dernierHistorique.isEmpty()) {
            lblSynthDerniereAction.setText("—");
        } else {
            ReclamationHistorique last = dernierHistorique.get(dernierHistorique.size() - 1);
            lblSynthDerniereAction.setText(formatHistoriqueLigne(last, historiqueUserNames));
        }
    }

    private String formatAssignLong(Reclamation r) {
        if (!r.isAssignee()) {
            return "Non assignée";
        }
        String d = r.getDateAssignation() != null ? r.getDateAssignation().toLocalDateTime().format(DF) : "—";
        return ReclamationUiHelper.formatAssignationCourte(r, resolvedAssigneeNom) + " — le " + d;
    }

    private void majBoutonsAssignation() {
        boolean ouvert = !ReclamationUiHelper.isStatutCloture(reclamation.getStatut());
        Integer uid = AppSession.getInstance().getConnectedUserId();
        boolean autre = reclamation.getUserIdAssigne() != null
                && uid != null
                && !uid.equals(reclamation.getUserIdAssigne());
        btnPrendreCharge.setVisible(ouvert);
        btnPrendreCharge.setManaged(ouvert);
        btnPrendreCharge.setDisable(autre);
        lblAssignInfo.setText(formatAssignLong(reclamation));

        boolean showLiberer = ReclamationUiHelper.canReleaseAssignment(reclamation) && ouvert;
        btnLibererAssign.setVisible(showLiberer);
        btnLibererAssign.setManaged(showLiberer);
    }

    private void afficherHistorique() throws SQLException {
        historiqueBox.getChildren().clear();
        dernierHistorique = historiqueService.listByReclamationId(reclamationId);
        historiqueUserNames = Collections.emptyMap();
        if (dernierHistorique.isEmpty()) {
            lblHistoriqueVide.setVisible(true);
            lblHistoriqueVide.setManaged(true);
            return;
        }
        Set<Integer> userIds = new HashSet<>();
        for (ReclamationHistorique h : dernierHistorique) {
            userIds.add(h.getUserIdAction());
            if (h.getAncienAssigne() != null) {
                userIds.add(h.getAncienAssigne());
            }
            if (h.getNouveauAssigne() != null) {
                userIds.add(h.getNouveauAssigne());
            }
        }
        historiqueUserNames = reclamationService.findUserNomsByIds(userIds);
        lblHistoriqueVide.setVisible(false);
        lblHistoriqueVide.setManaged(false);
        for (ReclamationHistorique h : dernierHistorique) {
            VBox row = new VBox(4);
            row.getStyleClass().add("reclamation-hist-row");
            Label dt = new Label(h.getDateAction() != null ? h.getDateAction().toLocalDateTime().format(DF) : "—");
            dt.getStyleClass().add("reclamation-hist-date");
            Label act = new Label(libelleAction(h.getActionType()));
            act.getStyleClass().add("reclamation-hist-action");
            Label det = new Label(formatHistoriqueLigne(h, historiqueUserNames));
            det.getStyleClass().add("reclamation-hist-detail");
            det.setWrapText(true);
            row.getChildren().addAll(dt, act, det);
            historiqueBox.getChildren().add(row);
        }
    }

    private static String libelleAction(String t) {
        if (t == null) {
            return "—";
        }
        switch (t) {
            case ReclamationHistoriqueService.ACTION_CREATION:
                return "Création";
            case ReclamationHistoriqueService.ACTION_REPONSE:
                return "Réponse";
            case ReclamationHistoriqueService.ACTION_CHANGEMENT_STATUT:
                return "Changement de statut";
            case ReclamationHistoriqueService.ACTION_ASSIGNATION:
                return "Assignation";
            case ReclamationHistoriqueService.ACTION_LIBERATION_ASSIGNATION:
                return "Libération d’assignation";
            default:
                return t;
        }
    }

    private static String formatUserRef(int userId, Map<Integer, String> names) {
        if (names != null) {
            String n = names.get(userId);
            if (n != null && !n.isBlank()) {
                return n.trim();
            }
        }
        return "utilisateur #" + userId;
    }

    private static String formatHistoriqueLigne(ReclamationHistorique h, Map<Integer, String> names) {
        StringBuilder sb = new StringBuilder();
        sb.append("Par ").append(formatUserRef(h.getUserIdAction(), names))
                .append(" (").append(ReclamationUiHelper.roleLabelAssignDisplay(h.getRoleAction())).append(") — ");
        if (ReclamationHistoriqueService.ACTION_CHANGEMENT_STATUT.equals(h.getActionType())) {
            sb.append(h.getAncienStatut()).append(" → ").append(h.getNouveauStatut());
        } else if (ReclamationHistoriqueService.ACTION_ASSIGNATION.equals(h.getActionType())
                || ReclamationHistoriqueService.ACTION_LIBERATION_ASSIGNATION.equals(h.getActionType())) {
            String a = h.getAncienAssigne() == null ? "—" : formatUserRef(h.getAncienAssigne(), names);
            String n = h.getNouveauAssigne() == null ? "—" : formatUserRef(h.getNouveauAssigne(), names);
            sb.append("assigne ").append(a).append(" → ").append(n);
        }
        if (h.getMessageAction() != null && !h.getMessageAction().isBlank()) {
            sb.append(" — ").append(h.getMessageAction());
        }
        return sb.toString();
    }

    private static String buildResumeTraitement(Reclamation r) {
        String st = r.getStatut();
        if ("FERMEE".equals(st)) {
            return "✓ Ticket fermé — aucune action supplémentaire n’est attendue.";
        }
        if ("REJETEE".equals(st)) {
            return "✕ Réclamation rejetée — consultez le fil ci-dessous pour le motif éventuel.";
        }
        if ("REPONDUE".equals(st)) {
            return "✉ Une réponse a été apportée — le statut peut encore évoluer (fermeture, rejet…).";
        }
        if ("EN_COURS".equals(st)) {
            return "⚙ Prise en cours — traitement en cours par l’équipe.";
        }
        return "🆕 Nouvelle réclamation — en attente de traitement.";
    }

    private String formatEcole(Integer id) throws SQLException {
        if (id == null) {
            return "Aucune école associée au ticket";
        }
        Ecole e = ecoleService.getById(id);
        if (e == null) {
            return "École n° " + id;
        }
        String nom = e.getNomEcole() != null ? e.getNomEcole() : "";
        return nom.isEmpty() ? "École n° " + id : nom + " (n° " + id + ")";
    }

    private String formatBus(Integer id) throws SQLException {
        if (id == null) {
            return "—";
        }
        Bus b = busService.getById(id);
        if (b == null) {
            return "Bus n° " + id;
        }
        String num = b.getNumeroBus() != null ? b.getNumeroBus() : "";
        String mat = b.getMatricule() != null ? b.getMatricule() : "";
        if (!num.isEmpty()) {
            return num + (!mat.isEmpty() ? " · " + mat : "") + " (id " + id + ")";
        }
        return "Bus n° " + id;
    }

    private String formatTrajet(Integer id) throws SQLException {
        if (id == null) {
            return "—";
        }
        Trajet t = trajetService.getById(id);
        if (t == null) {
            return "Trajet n° " + id;
        }
        String nom = t.getNom() != null ? t.getNom() : "Trajet";
        return nom + " (id " + id + ")";
    }

    private void afficherReponses() throws SQLException {
        reponsesBox.getChildren().clear();
        List<ReponseReclamation> list = reponseReclamationService.getByReclamationId(reclamationId);
        if (list.isEmpty()) {
            lblAucuneReponse.setVisible(true);
            lblAucuneReponse.setManaged(true);
            return;
        }
        lblAucuneReponse.setVisible(false);
        lblAucuneReponse.setManaged(false);
        for (ReponseReclamation rr : list) {
            boolean staff = "ADMIN".equals(rr.getRoleRepondeur()) || "AGENT_ECOLE".equals(rr.getRoleRepondeur());

            HBox row = new HBox(12);
            row.getStyleClass().add("reclamation-timeline-row");
            if (staff) {
                row.getStyleClass().add("timeline-align-staff");
            } else {
                row.getStyleClass().add("timeline-align-user");
            }

            Label avatar = new Label(staff ? "🛡" : "💬");
            avatar.getStyleClass().add("reclamation-timeline-avatar");

            VBox bubble = new VBox(6);
            bubble.getStyleClass().add("reclamation-reply-bubble");
            if (staff) {
                bubble.getStyleClass().add("reply-from-staff");
            } else {
                bubble.getStyleClass().add("reply-from-user");
            }

            HBox head = new HBox(8);
            head.setSpacing(8);
            Label roleBadge = new Label(ReclamationUiHelper.roleLabelShort(rr.getRoleRepondeur()));
            roleBadge.getStyleClass().addAll("rec-badge", "reply-role-badge");
            Label lm = new Label(rr.getDateReponse() != null ? rr.getDateReponse().toLocalDateTime().format(DF) : "—");
            lm.getStyleClass().add("reclamation-reply-meta");
            head.getChildren().addAll(roleBadge, lm);

            Label tx = new Label(rr.getMessage() != null ? rr.getMessage() : "");
            tx.getStyleClass().add("reclamation-reply-msg");
            tx.setWrapText(true);

            bubble.getChildren().addAll(head, tx);
            HBox.setHgrow(bubble, Priority.ALWAYS);
            row.getChildren().addAll(avatar, bubble);
            reponsesBox.getChildren().add(row);
        }
    }

    @FXML
    private void retourListe() {
        SceneNavigator.navigateReclamationList();
    }

    @FXML
    private void prendreEnCharge() {
        if (!ReclamationUiHelper.canRespondOrChangeStatut()) {
            return;
        }
        hideTraitementMsg();
        Integer uid = AppSession.getInstance().getConnectedUserId();
        if (uid == null) {
            showTraitementMsg("Session invalide.", true);
            return;
        }
        String dbRole = ReclamationUiHelper.responderDbRole();
        if (dbRole == null) {
            showTraitementMsg("Rôle non autorisé.", true);
            return;
        }
        try {
            reclamationService.prendreEnCharge(reclamation, uid, dbRole, historiqueService, sessionRoleKeySafe());
            showTraitementMsg("Prise en charge enregistrée.", false);
            charger();
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "prendre en charge", e);
            showTraitementMsg(e.getMessage() != null ? e.getMessage() : "Action impossible.", true);
        }
    }

    @FXML
    private void assignerUtilisateur() {
        if (!ReclamationUiHelper.canAssignToOtherUsers()) {
            return;
        }
        hideTraitementMsg();
        ReclamationService.AssignCandidate c = cbAssignUser.getSelectionModel().getSelectedItem();
        if (c == null) {
            showTraitementMsg("Choisissez un utilisateur dans la liste.", true);
            return;
        }
        Integer uid = AppSession.getInstance().getConnectedUserId();
        if (uid == null) {
            showTraitementMsg("Session invalide.", true);
            return;
        }
        try {
            reclamationService.assignerA(reclamation, c.userId, c.roleAssigneDb, uid, sessionRoleKeySafe(), historiqueService);
            showTraitementMsg("Assignation enregistrée.", false);
            charger();
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "assignation", e);
            showTraitementMsg("Erreur : " + e.getMessage(), true);
        }
    }

    @FXML
    private void libererAssignation() {
        if (!ReclamationUiHelper.canReleaseAssignment(reclamation)) {
            return;
        }
        hideTraitementMsg();
        Integer uid = AppSession.getInstance().getConnectedUserId();
        if (uid == null) {
            showTraitementMsg("Session invalide.", true);
            return;
        }
        try {
            reclamationService.libererAssignation(reclamation, uid, sessionRoleKeySafe(), historiqueService);
            showTraitementMsg("Assignation retirée.", false);
            charger();
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "liberer assignation", e);
            showTraitementMsg("Erreur : " + e.getMessage(), true);
        }
    }

    @FXML
    private void exporterDetailCsv() {
        try {
            List<ReponseReclamation> reps = reponseReclamationService.getByReclamationId(reclamationId);
            List<ReclamationHistorique> hist = historiqueService.listByReclamationId(reclamationId);
            FileChooser fc = new FileChooser();
            fc.setTitle("Exporter le dossier");
            fc.setInitialFileName("reclamation_" + reclamationId + "_dossier.csv");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
            File f = fc.showSaveDialog(lblObjet.getScene().getWindow());
            if (f == null) {
                return;
            }
            Path p = f.toPath();
            ReclamationExportUtil.exportDetailRapportCsv(reclamation, reps, hist, p);
            showTraitementMsg("Export enregistré.", false);
        } catch (SQLException | IOException e) {
            LOG.log(Level.SEVERE, "export detail", e);
            showTraitementMsg("Export impossible : " + e.getMessage(), true);
        }
    }

    @FXML
    private void mettreAJourStatut() {
        if (!ReclamationUiHelper.canRespondOrChangeStatut()) {
            return;
        }
        hideTraitementMsg();
        String st = cbStatut.getSelectionModel().getSelectedItem();
        if (st == null) {
            showTraitementMsg("Choisissez un statut.", true);
            return;
        }
        Integer uid = AppSession.getInstance().getConnectedUserId();
        if (uid == null) {
            showTraitementMsg("Session invalide.", true);
            return;
        }
        try {
            String old = reclamation.getStatut();
            if (old != null && old.equals(st)) {
                showTraitementMsg("Statut déjà à cette valeur.", true);
                return;
            }
            if (reclamationService.modifierStatut(reclamationId, st)) {
                historiqueService.enregistrer(reclamationId, ReclamationHistoriqueService.ACTION_CHANGEMENT_STATUT,
                        old, st, null, null, null, uid, sessionRoleKeySafe());
                showTraitementMsg("Statut mis à jour.", false);
                charger();
            } else {
                showTraitementMsg("Mise à jour impossible.", true);
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "maj statut", e);
            showTraitementMsg("Erreur : " + e.getMessage(), true);
        }
    }

    @FXML
    private void envoyerReponse() {
        if (!ReclamationUiHelper.canRespondOrChangeStatut()) {
            return;
        }
        hideTraitementMsg();
        String msg = taReponse.getText() != null ? taReponse.getText().trim() : "";
        if (msg.isEmpty()) {
            showTraitementMsg("Saisissez un message.", true);
            return;
        }
        if (InputModerationUtil.isBlocked(msg)) {
            showTraitementMsg(InputModerationUtil.MESSAGE_BLOQUE, true);
            return;
        }
        String roleDb = ReclamationUiHelper.responderDbRole();
        if (roleDb == null) {
            showTraitementMsg("Rôle non autorisé à répondre.", true);
            return;
        }
        Integer uid = AppSession.getInstance().getConnectedUserId();
        if (uid == null) {
            showTraitementMsg("Session invalide.", true);
            return;
        }

        try {
            ReponseReclamation rr = new ReponseReclamation();
            rr.setReclamationId(reclamationId);
            rr.setMessage(msg);
            rr.setRoleRepondeur(roleDb);
            rr.setUserId(uid);
            reponseReclamationService.ajouter(rr);
            String excerpt = msg.length() > 160 ? msg.substring(0, 157) + "…" : msg;
            historiqueService.enregistrer(reclamationId, ReclamationHistoriqueService.ACTION_REPONSE,
                    null, null, null, null, excerpt, uid, sessionRoleKeySafe());

            reclamation = reclamationService.getById(reclamationId);
            if (reclamation != null) {
                String cur = reclamation.getStatut();
                if (ReclamationService.STATUT_NOUVELLE.equals(cur) || ReclamationService.STATUT_EN_COURS.equals(cur)) {
                    reclamationService.modifierStatut(reclamationId, ReclamationService.STATUT_REPONDUE);
                    historiqueService.enregistrer(reclamationId, ReclamationHistoriqueService.ACTION_CHANGEMENT_STATUT,
                            cur, ReclamationService.STATUT_REPONDUE, null, null,
                            "Passage à REPONDUE après réponse", uid, sessionRoleKeySafe());
                }
            }

            taReponse.clear();
            showTraitementMsg("Réponse enregistrée.", false);
            charger();
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "envoyer reponse", e);
            showTraitementMsg("Erreur : " + e.getMessage(), true);
        }
    }

    private Label makeBadgeCategorie(String cat) {
        String c = cat != null ? cat : "—";
        Label l = new Label(c);
        l.getStyleClass().addAll("rec-badge", "badge-categorie");
        return l;
    }

    private Label makeBadgeStatut(String statut) {
        String s = statut != null ? statut : "NOUVELLE";
        Label l = new Label(s);
        l.getStyleClass().addAll("rec-badge", "badge-statut-" + s);
        return l;
    }

    private Label makeBadgePriorite(String p) {
        String key = p != null ? p : "MOYENNE";
        Label l = new Label(key);
        l.getStyleClass().addAll("rec-badge", "badge-prio-" + key);
        return l;
    }

    private void showTraitementMsg(String t, boolean error) {
        lblTraitementMsg.setText(t);
        lblTraitementMsg.setVisible(true);
        lblTraitementMsg.setManaged(true);
        lblTraitementMsg.setStyle(error ? "-fx-text-fill: #fca5a5;" : "-fx-text-fill: #86efac;");
    }

    private void hideTraitementMsg() {
        lblTraitementMsg.setVisible(false);
        lblTraitementMsg.setManaged(false);
    }
}
