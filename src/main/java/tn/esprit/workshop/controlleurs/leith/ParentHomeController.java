package tn.esprit.workshop.controlleurs.leith;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import tn.esprit.workshop.model.leith.Enfant;
import tn.esprit.workshop.services.leith.EnfantService;
import tn.esprit.workshop.utilis.AppSession;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ParentHomeController {

    private static final Logger LOG = Logger.getLogger(ParentHomeController.class.getName());

    @FXML private ListView<Enfant> listEnfants;
    @FXML private Button btnTracking;
    @FXML private Button btnAskZayna;

    private Integer selectedEnfantId;
    private final EnfantService enfantService = new EnfantService();

    @FXML
    public void initialize() {
        int parentId = AppSession.getInstance().getParentId();
        loadEnfants(parentId);
        if (listEnfants.getItems().isEmpty()) {
            javafx.application.Platform.runLater(() -> {
                Alert a = new Alert(Alert.AlertType.INFORMATION);
                a.setTitle("Mes enfants");
                a.setHeaderText("Aucun enfant");
                a.setContentText("Vous n'avez pas encore d'enfant enregistré. Souhaitez-vous faire une demande de transport scolaire ?");
                a.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
                if (a.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
                    SceneNavigator.openParentDemandeTransport();
                }
            });
        }
        listEnfants.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Enfant item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getPrenom() + " " + item.getNom());
            }
        });
        listEnfants.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedEnfantId = newVal.getEnfantId();
                AppSession.getInstance().setSelectedEnfantId(selectedEnfantId);
                btnTracking.setDisable(false);
                btnAskZayna.setDisable(false);
            } else {
                selectedEnfantId = null;
                AppSession.getInstance().setSelectedEnfantId(null);
                btnTracking.setDisable(true);
                btnAskZayna.setDisable(true);
            }
        });

    }

    private void loadEnfants(int parentId) {
        try {
            List<Enfant> enfants = enfantService.getByParentId(parentId);
            listEnfants.getItems().setAll(enfants);
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Erreur chargement enfants pour parentId=" + parentId, e);
        }
    }

    @FXML
    void openTracking() {
        if (selectedEnfantId == null) return;
        SceneNavigator.openTracking(selectedEnfantId);
    }

    @FXML
    void openChatAI() {
        if (selectedEnfantId == null) return;
        SceneNavigator.openChatAI(selectedEnfantId);
    }

    @FXML
    void openDemandeTransport() {
        SceneNavigator.openParentDemandeTransport();
    }

    @FXML
    void openSuiviCandidaturesEnfant() {
        SceneNavigator.openParentSuiviCandidaturesEnfant();
    }
}
