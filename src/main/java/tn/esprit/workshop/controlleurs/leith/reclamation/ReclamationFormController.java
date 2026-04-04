package tn.esprit.workshop.controlleurs.leith.reclamation;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import tn.esprit.workshop.controlleurs.leith.SceneNavigator;
import tn.esprit.workshop.model.leith.Reclamation;
import tn.esprit.workshop.services.leith.ReclamationService;
import tn.esprit.workshop.utilis.AppSession;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReclamationFormController implements Initializable {

    private static final Logger LOG = Logger.getLogger(ReclamationFormController.class.getName());

    @FXML private TextField tfObjet;
    @FXML private TextArea taDescription;
    @FXML private ComboBox<String> cbCategorie;
    @FXML private ComboBox<String> cbPriorite;
    @FXML private Label lblMessage;

    private final ReclamationService reclamationService = new ReclamationService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cbCategorie.getItems().addAll(
                "TRANSPORT", "RETARD", "CHAUFFEUR", "SECURITE", "ENFANT", "ECOLE", "TECHNIQUE", "AUTRE");
        cbPriorite.getItems().addAll("BASSE", "MOYENNE", "HAUTE", "URGENTE");
        cbCategorie.getSelectionModel().selectFirst();
        cbPriorite.getSelectionModel().selectFirst();
        SceneNavigator.applyAppCssToComboBoxPopup(cbCategorie);
        SceneNavigator.applyAppCssToComboBoxPopup(cbPriorite);
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
                    r.setIdEcole(reclamationService.findLikelyEcoleIdForParent(s.getParentId()));
                    break;
                case "CHAUFFEUR":
                    if (s.getChauffeurId() == null) {
                        showMsg("Profil chauffeur introuvable.", true);
                        return;
                    }
                    r.setIdChauffeur(s.getChauffeurId());
                    break;
                case "MAITRESSE":
                    if (s.getMaitresseId() == null) {
                        showMsg("Profil maîtresse introuvable.", true);
                        return;
                    }
                    r.setIdMaitresse(s.getMaitresseId());
                    r.setIdEcole(s.getEcoleId());
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
                    break;
            }

            reclamationService.ajouter(r);
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
