package tn.esprit.workshop.controlleurs.leith.parent;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import tn.esprit.workshop.controlleurs.leith.SceneNavigator;
import tn.esprit.workshop.model.leith.CandidatureEnfant;
import tn.esprit.workshop.services.leith.CandidatureEnfantService;
import tn.esprit.workshop.utilis.AppSession;

import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ParentSuiviCandidaturesEnfantController implements Initializable {

    private static final Logger LOG = Logger.getLogger(ParentSuiviCandidaturesEnfantController.class.getName());

    @FXML private TableView<CandidatureEnfant> table;
    @FXML private TableColumn<CandidatureEnfant, String> colNom;
    @FXML private TableColumn<CandidatureEnfant, String> colPrenom;
    @FXML private TableColumn<CandidatureEnfant, Integer> colAge;
    @FXML private TableColumn<CandidatureEnfant, String> colStatut;
    @FXML private TableColumn<CandidatureEnfant, String> colDate;
    @FXML private TableColumn<CandidatureEnfant, Void> colActions;
    @FXML private Label lblMessage;

    private final CandidatureEnfantService candidatureEnfantService = new CandidatureEnfantService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colNom.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNomEnfant()));
        colPrenom.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getPrenomEnfant()));
        colAge.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getAge()).asObject());
        colStatut.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getStatut() != null ? c.getValue().getStatut() : ""));
        colDate.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getDateDemande() != null ? c.getValue().getDateDemande().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : ""));

        colActions.setCellFactory(tc -> new TableCell<>() {
            private final Button btnModifier = new Button("Modifier");
            private final Button btnSupprimer = new Button("Supprimer");
            private final HBox row = new HBox(8, btnModifier, btnSupprimer);

            {
                btnModifier.getStyleClass().add("btn-primary");
                btnSupprimer.getStyleClass().add("btn-soft");
                btnModifier.setMinWidth(88);
                btnSupprimer.setMinWidth(88);
                btnModifier.setOnAction(e -> {
                    CandidatureEnfant ce = getTableRow().getItem();
                    if (ce != null) {
                        ouvrirModification(ce);
                    }
                });
                btnSupprimer.setOnAction(e -> {
                    CandidatureEnfant ce = getTableRow().getItem();
                    if (ce != null) {
                        confirmerSuppression(ce);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                CandidatureEnfant ce = getTableRow().getItem();
                if (ce == null) {
                    setGraphic(null);
                    return;
                }
                boolean enAttente = CandidatureEnfantService.STATUT_ENVOYEE.equals(ce.getStatut());
                boolean peutSupprimer = !CandidatureEnfantService.STATUT_ACCEPTEE.equals(ce.getStatut());
                btnModifier.setDisable(!enAttente);
                btnSupprimer.setDisable(!peutSupprimer);
                setGraphic(row);
            }
        });

        load();
        String msg = AppSession.consumeFlashMessage();
        if (msg != null) {
            lblMessage.setStyle(null);
            lblMessage.setText(msg);
            lblMessage.getStyleClass().removeAll("error-message");
            lblMessage.getStyleClass().add("success-message");
        }
    }

    private void ouvrirModification(CandidatureEnfant ce) {
        if (!CandidatureEnfantService.STATUT_ENVOYEE.equals(ce.getStatut())) {
            lblMessage.setText("Seules les candidatures en attente peuvent être modifiées.");
            lblMessage.setStyle("-fx-text-fill: #fca5a5;");
            return;
        }
        AppSession.getInstance().setPendingEditCandidatureEnfantId(ce.getId());
        SceneNavigator.parentShellNavigateModifierCandidatureEnfant();
    }

    private void confirmerSuppression(CandidatureEnfant ce) {
        if (CandidatureEnfantService.STATUT_ACCEPTEE.equals(ce.getStatut())) {
            lblMessage.setText("Impossible de supprimer une candidature déjà acceptée.");
            lblMessage.setStyle("-fx-text-fill: #fca5a5;");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Supprimer la candidature");
        confirm.setHeaderText(null);
        String nom = (ce.getPrenomEnfant() != null ? ce.getPrenomEnfant() : "") + " "
                + (ce.getNomEnfant() != null ? ce.getNomEnfant() : "");
        confirm.setContentText("Supprimer la candidature pour « " + nom.trim() + " » ? Cette action est définitive.");
        Optional<ButtonType> res = confirm.showAndWait();
        if (!res.isPresent() || res.get() != ButtonType.OK) {
            return;
        }
        int parentId = AppSession.getInstance().getParentId();
        try {
            boolean ok = candidatureEnfantService.deleteByParent(ce.getId(), parentId);
            load();
            if (ok) {
                lblMessage.setStyle(null);
                lblMessage.setText("Suppression effectuée avec succès");
                lblMessage.getStyleClass().removeAll("error-message");
                lblMessage.getStyleClass().add("success-message");
            } else {
                lblMessage.setText("Suppression impossible (candidature introuvable ou déjà acceptée).");
                lblMessage.setStyle("-fx-text-fill: #fca5a5;");
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "delete candidature", e);
            lblMessage.setText("Erreur : " + e.getMessage());
            lblMessage.setStyle("-fx-text-fill: #fca5a5;");
        }
    }

    private void load() {
        int parentId = AppSession.getInstance().getParentId();
        try {
            table.getItems().setAll(candidatureEnfantService.findByParentId(parentId));
            lblMessage.setText("");
            lblMessage.setStyle(null);
            lblMessage.getStyleClass().removeAll("success-message", "error-message");
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "load", e);
            lblMessage.setText("Erreur: " + e.getMessage());
        }
    }
}
