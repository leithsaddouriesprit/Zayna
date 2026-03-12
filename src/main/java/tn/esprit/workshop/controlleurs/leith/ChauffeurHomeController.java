package tn.esprit.workshop.controlleurs.leith;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Chauffeur dashboard: central shortcut buttons use the same navigation and state as the shell sidebar.
 * State is applied by ChauffeurShellController.updateChauffeurNavigationState() so both stay in sync.
 */
public class ChauffeurHomeController implements Initializable {

    @FXML private Button btnPostuler;
    @FXML private Button btnSuivi;
    @FXML private Button btnEspace;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // State is applied by shell when this content is loaded
    }

    /**
     * Called by ChauffeurShellController so central buttons match sidebar state.
     * Same enable/disable logic as sidebar.
     */
    public void applyNavigationState(boolean postulerEnabled, boolean suiviEnabled, boolean espaceEnabled) {
        if (btnPostuler != null) btnPostuler.setDisable(!postulerEnabled);
        if (btnSuivi != null) btnSuivi.setDisable(!suiviEnabled);
        if (btnEspace != null) btnEspace.setDisable(!espaceEnabled);
    }

    @FXML
    void onPostuler() {
        SceneNavigator.openPostulerChauffeur(null);
    }

    @FXML
    void onSuiviCandidature() {
        SceneNavigator.openSuiviCandidature(null);
    }

    @FXML
    void onEspaceChauffeur() {
        SceneNavigator.openEspaceChauffeur();
    }
}
