package tn.esprit.workshop.controlleurs.leith.parent;

import javafx.fxml.FXML;
import tn.esprit.workshop.controlleurs.leith.SceneNavigator;

public class ParentDashboardController {

    @FXML
    void openMesEnfants() {
        SceneNavigator.openParentMesEnfants();
    }

    @FXML
    void openDemandeTransport() {
        SceneNavigator.openParentDemandeTransport();
    }

    @FXML
    void openSuiviCandidatures() {
        SceneNavigator.openParentSuiviCandidaturesEnfant();
    }
}
