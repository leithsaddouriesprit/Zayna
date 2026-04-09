package tn.esprit.workshop.controlleurs.leith.reclamation;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.VBox;
import tn.esprit.workshop.controlleurs.leith.SceneNavigator;
import tn.esprit.workshop.model.leith.Reclamation;
import tn.esprit.workshop.services.leith.ReclamationHistoriqueService;
import tn.esprit.workshop.services.leith.ReclamationService;
import tn.esprit.workshop.services.leith.ReclamationService.ChauffeurReclamationContext;
import tn.esprit.workshop.services.leith.ReclamationService.ParentEcoleChoice;
import tn.esprit.workshop.services.leith.MaitresseMetierService;
import tn.esprit.workshop.utilis.AppSession;
import tn.esprit.workshop.utilis.InputModerationUtil;

import java.net.URL;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class ReclamationFormController implements Initializable {

    private static final Logger LOG = Logger.getLogger(ReclamationFormController.class.getName());

    @FXML private TextField tfObjet;
    @FXML private TextArea taDescription;
    @FXML private ComboBox<String> cbCategorie;
    @FXML private ComboBox<String> cbPriorite;
    @FXML private VBox boxParentEcole;
    @FXML private ComboBox<ParentEcoleChoice> cbEcoleParent;
    @FXML private Label lblParentEcoleHint;
    @FXML private Label lblMessage;

    private final ReclamationService reclamationService = new ReclamationService();
    private final ReclamationHistoriqueService historiqueService = new ReclamationHistoriqueService();
    private final MaitresseMetierService maitresseMetierService = new MaitresseMetierService();
    private static final Pattern OBJET_PATTERN = Pattern.compile("^[\\p{L}\\p{N} ]+$");
    private static final int OBJET_MAX_LEN = 255;

    /** Écoles proposées au parent connecté (vide si autre rôle ou erreur). */
    private List<ParentEcoleChoice> parentEcolesCharges = Collections.emptyList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cbCategorie.getItems().addAll(
                "TRANSPORT", "RETARD", "CHAUFFEUR", "SECURITE", "ENFANT", "ECOLE", "TECHNIQUE", "AUTRE");
        cbPriorite.getItems().addAll("BASSE", "MOYENNE", "HAUTE", "URGENTE");
        cbCategorie.getSelectionModel().selectFirst();
        cbPriorite.getSelectionModel().selectFirst();
        SceneNavigator.applyAppCssToComboBoxPopup(cbCategorie);
        SceneNavigator.applyAppCssToComboBoxPopup(cbPriorite);

        tfObjet.setTextFormatter(new TextFormatter<>(change -> {
            String t = change.getControlNewText();
            if (t == null) {
                return null;
            }
            if (t.length() > OBJET_MAX_LEN) {
                return null;
            }
            return t.matches("^[\\p{L}\\p{N} ]*$") ? change : null;
        }));

        if ("PARENT".equals(ReclamationUiHelper.filterRoleKey())) {
            AppSession s = AppSession.getInstance();
            if (s.getParentId() > 0) {
                try {
                    parentEcolesCharges = reclamationService.listEcolesLieesAuParent(s.getParentId());
                } catch (SQLException e) {
                    LOG.log(Level.WARNING, "écoles liées au parent", e);
                    parentEcolesCharges = Collections.emptyList();
                }
                setupParentEcoleUi();
            }
        }
    }

    private void setupParentEcoleUi() {
        boxParentEcole.setVisible(true);
        boxParentEcole.setManaged(true);
        cbEcoleParent.getItems().setAll(parentEcolesCharges);
        SceneNavigator.applyAppCssToComboBoxPopup(cbEcoleParent);
        lblParentEcoleHint.setVisible(false);
        lblParentEcoleHint.setManaged(false);

        if (parentEcolesCharges.isEmpty()) {
            lblParentEcoleHint.setText(
                    "Aucune école n’est liée à vos enfants (candidature acceptée ou enfant actif sur un trajet d’école). "
                            + "La réclamation sera enregistrée sans école rattachée.");
            lblParentEcoleHint.setVisible(true);
            lblParentEcoleHint.setManaged(true);
            cbEcoleParent.setDisable(true);
            cbEcoleParent.getSelectionModel().clearSelection();
            return;
        }

        cbEcoleParent.setDisable(false);
        lblParentEcoleHint.setVisible(false);
        lblParentEcoleHint.setManaged(false);

        if (parentEcolesCharges.size() == 1) {
            cbEcoleParent.getSelectionModel().selectFirst();
            cbEcoleParent.setDisable(true);
        } else {
            cbEcoleParent.setPromptText("Choisir l’école concernée…");
            cbEcoleParent.getSelectionModel().clearSelection();
        }
    }

    @FXML
    private void enregistrer() {
        lblMessage.setVisible(false);
        lblMessage.setManaged(false);

        String objet = tfObjet.getText() != null ? tfObjet.getText().trim() : "";
        String desc = taDescription.getText() != null ? taDescription.getText().trim() : "";
        String cat = cbCategorie.getSelectionModel().getSelectedItem();
        String prio = cbPriorite.getSelectionModel().getSelectedItem();

        if (objet.isEmpty()) {
            showMsg("L’objet est obligatoire.", true);
            return;
        }
        if (objet.length() > OBJET_MAX_LEN) {
            showMsg("Objet invalide : lettres, chiffres et espaces uniquement.", true);
            return;
        }
        if (!OBJET_PATTERN.matcher(objet).matches()) {
            showMsg("Objet invalide : lettres, chiffres et espaces uniquement.", true);
            return;
        }
        if (desc.isEmpty()) {
            showMsg("La description est obligatoire.", true);
            return;
        }
        if (cat == null) {
            showMsg("La catégorie est obligatoire.", true);
            return;
        }
        if (prio == null) {
            showMsg("La priorité est obligatoire.", true);
            return;
        }
        if (InputModerationUtil.isBlocked(objet, desc)) {
            showMsg(InputModerationUtil.MESSAGE_BLOQUE, true);
            return;
        }

        AppSession s = AppSession.getInstance();
        Integer uid = s.getConnectedUserId();
        if (uid == null) {
            showMsg("Session invalide.", true);
            return;
        }

        String roleKey = ReclamationUiHelper.filterRoleKey();
        if (roleKey == null) {
            showMsg("Rôle non pris en charge.", true);
            return;
        }
        Reclamation r = new Reclamation();
        r.setObjet(objet);
        r.setDescription(desc);
        r.setCategorie(cat);
        r.setPriorite(prio);
        r.setStatut(ReclamationService.STATUT_NOUVELLE);
        r.setRoleCreateur(roleKey);
        r.setUserId(uid);

        try {
            switch (roleKey) {
                case "PARENT":
                    if (s.getParentId() <= 0) {
                        showMsg("Profil parent introuvable.", true);
                        return;
                    }
                    r.setIdParent(s.getParentId());
                    if (parentEcolesCharges.size() > 1) {
                        ParentEcoleChoice ch = cbEcoleParent.getSelectionModel().getSelectedItem();
                        if (ch == null) {
                            showMsg("Veuillez sélectionner l’école concernée par cette réclamation.", true);
                            return;
                        }
                        r.setIdEcole(ch.idEcole);
                    } else if (parentEcolesCharges.size() == 1) {
                        r.setIdEcole(parentEcolesCharges.get(0).idEcole);
                    } else {
                        r.setIdEcole(null);
                    }
                    break;
                case "CHAUFFEUR":
                    if (s.getChauffeurId() == null) {
                        showMsg("Profil chauffeur introuvable.", true);
                        return;
                    }
                    if (!ReclamationUiHelper.canCreateReclamation()) {
                        showMsg("Accès refusé : chauffeur non accepté.", true);
                        return;
                    }
                    r.setIdChauffeur(s.getChauffeurId());
                    ChauffeurReclamationContext ctx =
                            reclamationService.resolveChauffeurReclamationContext(s.getChauffeurId());
                    if (ctx.idEcole == null || ctx.idEcole <= 0) {
                        showMsg(
                                "Impossible d'enregistrer : aucune école n'a pu être déterminée pour votre compte chauffeur. "
                                        + "Vérifiez votre affectation bus/trajet ou votre candidature acceptée, ou contactez l'agent.",
                                true);
                        return;
                    }
                    r.setIdEcole(ctx.idEcole);
                    r.setIdBus(ctx.idBus);
                    r.setIdTrajet(ctx.idTrajet);
                    break;
                case "MAITRESSE":
                    if (s.getMaitresseId() == null) {
                        showMsg("Profil maîtresse introuvable.", true);
                        return;
                    }
                    if (s.getEcoleId() == null) {
                        showMsg("École introuvable pour votre session. Impossible de rattacher la réclamation à une école.",
                                true);
                        return;
                    }
                    r.setIdMaitresse(s.getMaitresseId());
                    // Une maîtresse est rattachée à une école via la session (même source que les écrans agent).
                    r.setIdEcole(s.getEcoleId());
                    try {
                        Integer sessionUid = s.getConnectedUserId();
                        Integer busId = sessionUid != null ? maitresseMetierService.getBusIdForMaitresseUser(sessionUid) : null;
                        r.setIdBus((busId != null && busId > 0) ? busId : null);
                    } catch (SQLException ex) {
                        LOG.log(Level.FINE, "bus maitresse introuvable", ex);
                    }
                    break;
                case "AGENT_ECOLE":
                    if (s.getEcoleId() == null) {
                        showMsg("École agent introuvable.", true);
                        return;
                    }
                    r.setIdEcole(s.getEcoleId());
                    break;
                case "ADMIN":
                default:
                    // Pas d’école par défaut : compte non rattaché à une école précise.
                    break;
            }

            reclamationService.ajouter(r);
            String rk = ReclamationUiHelper.filterRoleKey();
            if (rk != null) {
                historiqueService.enregistrer(
                        r.getId(),
                        ReclamationHistoriqueService.ACTION_CREATION,
                        null,
                        ReclamationService.STATUT_NOUVELLE,
                        null,
                        null,
                        "Création : " + (r.getObjet() != null ? r.getObjet() : ""),
                        uid,
                        rk);
            }
            AppSession.setFlashMessage("Réclamation enregistrée.");
            SceneNavigator.navigateReclamationList();
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "ajouter réclamation", e);
            showMsg("Enregistrement impossible : " + e.getMessage(), true);
        }
    }

    @FXML
    private void annuler() {
        SceneNavigator.navigateReclamationList();
    }

    private void showMsg(String text, boolean error) {
        lblMessage.setText(text);
        lblMessage.setVisible(true);
        lblMessage.setManaged(true);
        lblMessage.setStyle(error ? "-fx-text-fill: #fca5a5;" : "-fx-text-fill: #86efac;");
    }
}
