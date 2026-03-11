package tn.esprit.workshop.controlleurs.leith;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import tn.esprit.workshop.model.leith.Candidature;
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
    private static final String ENVOYEE = "ENVOYEE";
    private static final String ACCEPTEE = "ACCEPTEE";
    private static final String REFUSEE = "REFUSEE";

    @FXML private Button btnPostuler;
    @FXML private Button btnSuivi;
    @FXML private Button btnEspace;

    private final CandidatureService candidatureService = new CandidatureService();

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

            if (c == null) {
                btnPostuler.setDisable(false);
                btnSuivi.setDisable(true);
                btnEspace.setDisable(true);
                return;
            }
            String statut = c.getStatut();
            if (ENVOYEE.equals(statut)) {
                btnPostuler.setDisable(true);
                btnSuivi.setDisable(false);
                btnEspace.setDisable(true);
            } else if (REFUSEE.equals(statut)) {
                btnPostuler.setDisable(false);
                btnSuivi.setDisable(false);
                btnEspace.setDisable(true);
            } else if (ACCEPTEE.equals(statut)) {
                btnPostuler.setDisable(true);
                btnSuivi.setDisable(false);
                btnEspace.setDisable(false);
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
        Integer chauffeurId = AppSession.getInstance().getChauffeurId();
        if (chauffeurId == null) {
            new Alert(Alert.AlertType.WARNING, "Session chauffeur invalide.").showAndWait();
            return;
        }
        try {
            Candidature c = candidatureService.findByChauffeurId(chauffeurId);
            if (c == null || !ACCEPTEE.equals(c.getStatut())) {
                new Alert(Alert.AlertType.WARNING,
                        "L'accès à l'Espace Chauffeur est réservé aux candidats dont la candidature a été acceptée.").showAndWait();
                return;
            }
            SceneNavigator.openEspaceChauffeur();
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "onEspaceChauffeur", e);
            new Alert(Alert.AlertType.ERROR, "Erreur: " + e.getMessage()).showAndWait();
        }
    }
}
