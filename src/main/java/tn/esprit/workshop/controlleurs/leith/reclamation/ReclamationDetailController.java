package tn.esprit.workshop.controlleurs.leith.reclamation;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import tn.esprit.workshop.controlleurs.leith.SceneNavigator;
import tn.esprit.workshop.model.leith.Bus;
import tn.esprit.workshop.model.leith.Reclamation;
import tn.esprit.workshop.model.leith.ReponseReclamation;
import tn.esprit.workshop.model.leith.Trajet;
import tn.esprit.workshop.model.tous.Ecole;
import tn.esprit.workshop.services.leith.BusService;
import tn.esprit.workshop.services.leith.EcoleService;
import tn.esprit.workshop.services.leith.ReclamationService;
import tn.esprit.workshop.services.leith.ReponseReclamationService;
import tn.esprit.workshop.services.leith.TrajetService;
import tn.esprit.workshop.utilis.AppSession;

import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReclamationDetailController implements Initializable {

    private static final Logger LOG = Logger.getLogger(ReclamationDetailController.class.getName());
    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private Label lblEmoji;
    @FXML private Label lblObjet;
    @FXML private HBox badgeRow;
    @FXML private Label lblResumeTraitement;
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
    @FXML private VBox boxTraitement;
    @FXML private ComboBox<String> cbStatut;
    @FXML private TextArea taReponse;
    @FXML private Label lblTraitementMsg;

    private final ReclamationService reclamationService = new ReclamationService();
    private final ReponseReclamationService reponseReclamationService = new ReponseReclamationService();
    private final EcoleService ecoleService = new EcoleService();
    private final BusService busService = new BusService();
    private final TrajetService trajetService = new TrajetService();

    private int reclamationId;
    private Reclamation reclamation;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cbStatut.getItems().addAll(
                ReclamationService.STATUT_NOUVELLE,
                ReclamationService.STATUT_EN_COURS,
                ReclamationService.STATUT_REPONDUE,
                ReclamationService.STATUT_FERMEE,
                ReclamationService.STATUT_REJETEE);
        SceneNavigator.applyAppCssToComboBoxPopup(cbStatut);

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

    private void charger() {
        hideTraitementMsg();
        try {
            reclamation = reclamationService.getById(reclamationId);
            if (reclamation == null || !ReclamationUiHelper.canView(reclamation)) {
                Platform.runLater(SceneNavigator::navigateReclamationList);
                return;
            }

            lblEmoji.setText(ReclamationUiHelper.categorieEmoji(reclamation.getCategorie()));
            lblObjet.setText(reclamation.getObjet() != null ? reclamation.getObjet() : "—");
            lblDescription.setText(reclamation.getDescription() != null ? reclamation.getDescription() : "—");

            lblCreateur.setText("Utilisateur n° " + reclamation.getUserId());
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

            if (ReclamationUiHelper.canRespondOrChangeStatut()) {
                cbStatut.getSelectionModel().select(reclamation.getStatut());
            }

            afficherReponses();
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "detail reclamation", e);
            Platform.runLater(SceneNavigator::navigateReclamationList);
        }
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
            return "—";
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
        try {
            if (reclamationService.modifierStatut(reclamationId, st)) {
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

            reclamation = reclamationService.getById(reclamationId);
            if (reclamation != null) {
                String cur = reclamation.getStatut();
                if (ReclamationService.STATUT_NOUVELLE.equals(cur) || ReclamationService.STATUT_EN_COURS.equals(cur)) {
                    reclamationService.modifierStatut(reclamationId, ReclamationService.STATUT_REPONDUE);
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
