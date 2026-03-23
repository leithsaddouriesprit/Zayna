package tn.esprit.workshop.controlleurs.leith.shell;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import tn.esprit.workshop.controlleurs.leith.ChauffeurHomeController;
import tn.esprit.workshop.controlleurs.leith.EspaceChauffeurController;
import tn.esprit.workshop.controlleurs.leith.SceneNavigator;
import tn.esprit.workshop.controlleurs.leith.SuiviCandidatureController;
import tn.esprit.workshop.model.leith.Candidature;
import tn.esprit.workshop.services.leith.CandidatureService;
import tn.esprit.workshop.utilis.AppSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChauffeurShellController {

    private static final Logger LOG = Logger.getLogger(ChauffeurShellController.class.getName());
    private static final String ENVOYEE = "ENVOYEE";
    private static final String ACCEPTEE = "ACCEPTEE";
    private static final String REFUSEE = "REFUSEE";

    @FXML private StackPane contentArea;
    @FXML private Label lblAccountInfo;
    @FXML private Button btnSidebarHome;
    @FXML private Button btnSidebarPostuler;
    @FXML private Button btnSidebarSuivi;
    @FXML private Button btnSidebarEspace;

    private final CandidatureService candidatureService = new CandidatureService();

    @FXML
    public void initialize() {
        SceneNavigator.registerChauffeurShell(this);
        updateAccountLabel();
        Platform.runLater(() -> {
            if (contentArea != null && contentArea.getScene() != null && contentArea.getScene().getWindow() instanceof Stage) {
                ((Stage) contentArea.getScene().getWindow()).setOnHidden(e -> SceneNavigator.unregisterChauffeurShell());
            }
        });
        loadContent("/leith/ChauffeurHome.fxml");
    }

    private void updateAccountLabel() {
        if (lblAccountInfo != null) {
            String name = AppSession.getInstance().getConnectedUserName();
            String role = AppSession.getInstance().getConnectedUserRole();
            lblAccountInfo.setText((name.isEmpty() ? "Chauffeur" : name) + " (" + (role.isEmpty() ? "Chauffeur" : role) + ")");
        }
    }

    private void markActive(Button target) {
        Button[] all = new Button[] { btnSidebarHome, btnSidebarPostuler, btnSidebarSuivi, btnSidebarEspace };
        for (Button b : all) {
            if (b == null) continue;
            b.getStyleClass().remove("admin-nav-item-active");
            if (!b.getStyleClass().contains("admin-nav-item")) {
                b.getStyleClass().add("admin-nav-item");
            }
        }
        if (target != null && !target.getStyleClass().contains("admin-nav-item-active")) {
            target.getStyleClass().add("admin-nav-item-active");
        }
    }

    /**
     * Single source of truth for Chauffeur navigation state.
     * Updates sidebar buttons and returns state so dashboard buttons can be synced.
     * Rules: no candidature → Postuler on, Suivi/Espace off; ENVOYEE → Suivi on, rest by rule; ACCEPTEE → Suivi+Espace on; REFUSEE → Postuler+Suivi on.
     */
    private ChauffeurNavState updateChauffeurNavigationState() {
        boolean postuler = true;
        boolean suivi = false;
        boolean espace = false;
        Integer chauffeurId = AppSession.getInstance().getChauffeurId();
        if (chauffeurId != null) {
            try {
                Candidature c = candidatureService.findByChauffeurId(chauffeurId);
                if (c == null) {
                    postuler = true;
                    suivi = false;
                    espace = false;
                } else {
                    String statut = c.getStatut();
                    if (ENVOYEE.equals(statut)) {
                        postuler = false;
                        suivi = true;
                        espace = false;
                    } else if (REFUSEE.equals(statut)) {
                        postuler = true;
                        suivi = true;
                        espace = false;
                    } else if (ACCEPTEE.equals(statut)) {
                        postuler = false;
                        suivi = true;
                        espace = true;
                    } else {
                        postuler = true;
                        suivi = false;
                        espace = false;
                    }
                }
            } catch (SQLException e) {
                LOG.log(Level.SEVERE, "updateChauffeurNavigationState", e);
                postuler = true;
                suivi = false;
                espace = false;
            }
        }
        if (btnSidebarPostuler != null) btnSidebarPostuler.setDisable(!postuler);
        if (btnSidebarSuivi != null) btnSidebarSuivi.setDisable(!suivi);
        if (btnSidebarEspace != null) btnSidebarEspace.setDisable(!espace);
        return new ChauffeurNavState(postuler, suivi, espace);
    }

    private static final class ChauffeurNavState {
        final boolean postuler;
        final boolean suivi;
        final boolean espace;
        ChauffeurNavState(boolean postuler, boolean suivi, boolean espace) {
            this.postuler = postuler;
            this.suivi = suivi;
            this.espace = espace;
        }
    }

    public void loadContent(String fxmlPath) {
        if (contentArea == null) return;
        try {
            ChauffeurNavState state = updateChauffeurNavigationState();
            if ("/leith/ChauffeurHome.fxml".equals(fxmlPath)) markActive(btnSidebarHome);
            else if ("/leith/PostulerChauffeur.fxml".equals(fxmlPath)) markActive(btnSidebarPostuler);
            else if ("/leith/SuiviCandidature.fxml".equals(fxmlPath)) markActive(btnSidebarSuivi);
            else if ("/leith/EspaceChauffeur.fxml".equals(fxmlPath)) markActive(btnSidebarEspace);
            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(fxmlPath));
            Parent root = loader.load();
            Object controller = loader.getController();
            if (controller instanceof ChauffeurHomeController) {
                ((ChauffeurHomeController) controller).applyNavigationState(state.postuler, state.suivi, state.espace);
            } else if (controller instanceof SuiviCandidatureController) {
                Integer id = AppSession.getInstance().getChauffeurId();
                if (id != null) ((SuiviCandidatureController) controller).init(id);
            } else if (controller instanceof EspaceChauffeurController) {
                Integer id = AppSession.getInstance().getChauffeurId();
                if (id != null) ((EspaceChauffeurController) controller).init(id);
            }
            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to load " + fxmlPath, e);
        }
    }

    @FXML
    void goHome() {
        loadContent("/leith/ChauffeurHome.fxml");
    }

    @FXML
    void openPostuler() {
        loadContent("/leith/PostulerChauffeur.fxml");
    }

    @FXML
    void openSuiviCandidature() {
        loadContent("/leith/SuiviCandidature.fxml");
    }

    @FXML
    void openEspaceChauffeur() {
        loadContent("/leith/EspaceChauffeur.fxml");
    }
    // Dans le contrôleur, ajoutez :
    @FXML
    private void openReclamation() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GestionReclamation.fxml"));
            Parent root = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void logout() {
        SceneNavigator.unregisterChauffeurShell();
        Stage stage = (Stage) contentArea.getScene().getWindow();
        if (stage != null) stage.close();
        SceneNavigator.showLoginWindow();
    }
}
