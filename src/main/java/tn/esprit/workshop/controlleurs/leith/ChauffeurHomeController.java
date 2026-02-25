package tn.esprit.workshop.controlleurs.leith;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import tn.esprit.workshop.model.leith.Candidature;
import tn.esprit.workshop.services.leith.BusService;
import tn.esprit.workshop.services.leith.CandidatureService;
import tn.esprit.workshop.utilis.AppSession;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Chauffeur hub: Postuler, Suivi Candidature, Espace Chauffeur.
 * Button states depend on candidature statut and bus assignment.
 */
public class ChauffeurHomeController implements Initializable {

    private static final Logger LOG = Logger.getLogger(ChauffeurHomeController.class.getName());
    private static final String EN_ATTENTE = "EN_ATTENTE";
    private static final String ACCEPTEE = "ACCEPTEE";
    private static final String REFUSEE = "REFUSEE";
    private static final String ANNULEE = "ANNULEE";

    @FXML private Button btnPostuler;
    @FXML private Button btnSuivi;
    @FXML private Button btnEspace;

    private final CandidatureService candidatureService = new CandidatureService();
    private final BusService busService = new BusService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        refreshButtonStates();
    }

    private void refreshButtonStates() {
        Integer chauffeurId = AppSession.getInstance().getChauffeurId();
        if (chauffeurId == null) {
            btnPostuler.setDisable(false);
            btnSuivi.setDisable(true);
            btnEspace.setDisable(true);
            return;
        }
        try {
            Candidature c = candidatureService.findByChauffeurId(chauffeurId);
            boolean hasBus = busService.getByChauffeurId(chauffeurId) != null;

            if (c == null) {
                btnPostuler.setDisable(false);
                btnSuivi.setDisable(true);
                btnEspace.setDisable(true);
                return;
            }
            String statut = c.getStatut();
            if (EN_ATTENTE.equals(statut)) {
                btnPostuler.setDisable(true);
                btnSuivi.setDisable(false);
                btnEspace.setDisable(true);
            } else if (REFUSEE.equals(statut) || ANNULEE.equals(statut)) {
                btnPostuler.setDisable(false);
                btnSuivi.setDisable(false);
                btnEspace.setDisable(true);
            } else if (ACCEPTEE.equals(statut)) {
                btnPostuler.setDisable(true);
                btnSuivi.setDisable(false);
                btnEspace.setDisable(!hasBus);
            } else {
                btnPostuler.setDisable(false);
                btnSuivi.setDisable(true);
                btnEspace.setDisable(true);
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "refreshButtonStates", e);
            btnPostuler.setDisable(false);
            btnSuivi.setDisable(true);
            btnEspace.setDisable(true);
        }
    }

    @FXML
    void onPostuler() {
        SceneNavigator.openPostulerChauffeur(this::refreshButtonStates);
    }

    @FXML
    void onSuiviCandidature() {
        SceneNavigator.openSuiviCandidature(this::refreshButtonStates);
    }

    @FXML
    void onEspaceChauffeur() {
        SceneNavigator.openEspaceChauffeur();
    }
}
