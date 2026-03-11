package tn.esprit.workshop.controlleurs.leith.agent;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import tn.esprit.workshop.controlleurs.leith.SceneNavigator;
import tn.esprit.workshop.services.leith.EcoleService;
import tn.esprit.workshop.utilis.AppSession;

import java.sql.SQLException;

public class AgentDashboardController {

    @FXML private Label lblEcole;

    @FXML
    public void initialize() {
        Integer idEcole = AppSession.getInstance().getEcoleId();
        if (idEcole != null) {
            try {
                String nom = new EcoleService().getById(idEcole).getNomEcole();
                lblEcole.setText("École: " + nom);
            } catch (SQLException e) {
                lblEcole.setText("École id=" + idEcole);
            }
        } else {
            lblEcole.setText("École: (non définie)");
        }
    }

    @FXML void openCandidaturesChauffeur() { SceneNavigator.openAgentCandidaturesChauffeur(); }
    @FXML void openChauffeursAcceptes() { SceneNavigator.openAgentChauffeursAcceptes(); }
    @FXML void openBus() { SceneNavigator.openAgentBus(); }
    @FXML void openAffecterChauffeurBus() { SceneNavigator.openAgentAffecterChauffeurBus(); }
    @FXML void openTrajets() { SceneNavigator.openAgentTrajets(); }
    @FXML void openAffecterBusTrajet() { SceneNavigator.openAgentAffecterBusTrajet(); }
    @FXML void openArrets() { SceneNavigator.openAgentArrets(); }
    @FXML void openCandidaturesEnfant() { SceneNavigator.openAgentCandidaturesEnfant(); }
}
