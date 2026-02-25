package tn.esprit.workshop.controlleurs.leith;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import tn.esprit.workshop.model.leith.Candidature;
import tn.esprit.workshop.services.leith.CandidatureService;

import java.io.ByteArrayInputStream;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Suivi Candidature: display candidature and actions (Annuler / Postuler à nouveau / Aller à Espace).
 */
public class SuiviCandidatureController {

    private static final Logger LOG = Logger.getLogger(SuiviCandidatureController.class.getName());
    private static final String EN_ATTENTE = "EN_ATTENTE";
    private static final String ACCEPTEE = "ACCEPTEE";
    private static final String REFUSEE = "REFUSEE";

    @FXML private Label lblNomPrenom;
    @FXML private Label lblStatut;
    @FXML private Label lblDateEnvoi;
    @FXML private Label lblMaladie;
    @FXML private ImageView imgPermisPreview;
    @FXML private Button btnAnnuler;
    @FXML private Button btnPostulerAnouveau;
    @FXML private Button btnAllerEspace;
    @FXML private Label lblMessage;

    private Integer chauffeurId;
    private final CandidatureService candidatureService = new CandidatureService();

    public void init(Integer chauffeurId) {
        this.chauffeurId = chauffeurId;
        if (chauffeurId == null) {
            lblMessage.setText("Aucun chauffeur en session.");
            return;
        }
        loadCandidature();
    }

    private void loadCandidature() {
        try {
            Candidature c = candidatureService.findByChauffeurId(chauffeurId);
            if (c == null) {
                lblNomPrenom.setText("—");
                lblStatut.setText("Aucune candidature");
                lblDateEnvoi.setText("—");
                lblMaladie.setText("—");
                btnAnnuler.setVisible(false);
                btnPostulerAnouveau.setVisible(false);
                btnAllerEspace.setVisible(false);
                return;
            }
            lblNomPrenom.setText((c.getNom() != null ? c.getNom() : "") + ", " + (c.getPrenom() != null ? c.getPrenom() : ""));
            lblStatut.setText(c.getStatut() != null ? c.getStatut() : "—");
            lblDateEnvoi.setText(c.getDateEnvoi() != null ? c.getDateEnvoi().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "—");
            lblMaladie.setText(c.getMaladie() != null && !c.getMaladie().isEmpty() ? c.getMaladie() : "—");
            if (c.getPermisRecto() != null && c.getPermisRecto().length > 0) {
                imgPermisPreview.setImage(new Image(new ByteArrayInputStream(c.getPermisRecto())));
                imgPermisPreview.setVisible(true);
            } else {
                imgPermisPreview.setVisible(false);
            }

            btnAnnuler.setVisible(false);
            btnPostulerAnouveau.setVisible(false);
            btnAllerEspace.setVisible(false);
            if (EN_ATTENTE.equals(c.getStatut())) {
                btnAnnuler.setVisible(true);
            } else if (REFUSEE.equals(c.getStatut())) {
                btnPostulerAnouveau.setVisible(true);
            } else if (ACCEPTEE.equals(c.getStatut())) {
                btnAllerEspace.setVisible(true);
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "loadCandidature", e);
            lblMessage.setText("Erreur: " + e.getMessage());
        }
    }

    @FXML
    void onAnnuler() {
        if (chauffeurId == null) return;
        try {
            candidatureService.annuler(chauffeurId);
            lblMessage.setText("Candidature annulée.");
            javafx.stage.Stage stage = (javafx.stage.Stage) btnAnnuler.getScene().getWindow();
            if (stage != null) stage.close();
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "onAnnuler", e);
            lblMessage.setText("Erreur: " + e.getMessage());
        }
    }

    @FXML
    void onPostulerAnouveau() {
        javafx.stage.Stage stage = (javafx.stage.Stage) btnPostulerAnouveau.getScene().getWindow();
        if (stage != null) stage.close();
        SceneNavigator.openPostulerChauffeur(null);
    }

    @FXML
    void onAllerEspace() {
        javafx.stage.Stage stage = (javafx.stage.Stage) btnAllerEspace.getScene().getWindow();
        if (stage != null) stage.close();
        SceneNavigator.openEspaceChauffeur();
    }
}
