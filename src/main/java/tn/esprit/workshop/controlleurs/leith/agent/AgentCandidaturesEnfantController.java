package tn.esprit.workshop.controlleurs.leith.agent;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.*;
import tn.esprit.workshop.model.leith.CandidatureEnfant;
import tn.esprit.workshop.model.leith.Trajet;
import tn.esprit.workshop.services.leith.CandidatureEnfantService;
import tn.esprit.workshop.services.leith.EnfantService;
import tn.esprit.workshop.services.leith.TrajetService;
import tn.esprit.workshop.utilis.AppSession;

import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AgentCandidaturesEnfantController implements Initializable {

    private static final Logger LOG = Logger.getLogger(AgentCandidaturesEnfantController.class.getName());

    @FXML private TableView<CandidatureEnfant> table;
    @FXML private TableColumn<CandidatureEnfant, String> colNom;
    @FXML private TableColumn<CandidatureEnfant, String> colPrenom;
    @FXML private TableColumn<CandidatureEnfant, Integer> colAge;
    @FXML private TableColumn<CandidatureEnfant, String> colDate;
    @FXML private TableColumn<CandidatureEnfant, Void> colActions;
    @FXML private Label lblMessage;

    private final CandidatureEnfantService candidatureEnfantService = new CandidatureEnfantService();
    private final TrajetService trajetService = new TrajetService();
    private final EnfantService enfantService = new EnfantService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colNom.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNomEnfant()));
        colPrenom.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getPrenomEnfant()));
        colAge.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getAge()).asObject());
        colDate.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getDateDemande() != null ? c.getValue().getDateDemande().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : ""));

        colActions.setCellFactory(tc -> new TableCell<>() {
            private final Button btnRefuser = new Button("Refuser");
            private final Button btnAccepter = new Button("Accepter");

            {
                btnRefuser.setOnAction(e -> {
                    CandidatureEnfant ce = getTableRow().getItem();
                    if (ce != null) refuser(ce.getId());
                });
                btnAccepter.setOnAction(e -> {
                    CandidatureEnfant ce = getTableRow().getItem();
                    if (ce != null) accepter(ce);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(new javafx.scene.layout.HBox(6, btnRefuser, btnAccepter));
            }
        });

        load();
    }

    private void load() {
        Integer idEcole = AppSession.getInstance().getEcoleId();
        if (idEcole == null) {
            lblMessage.setText("Aucune école en session.");
            return;
        }
        try {
            table.getItems().setAll(candidatureEnfantService.findEnVoyeeByEcoleId(idEcole));
            lblMessage.setText("");
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "load", e);
            lblMessage.setText("Erreur: " + e.getMessage());
        }
    }

    private void refuser(int id) {
        try {
            candidatureEnfantService.refuser(id);
            new Alert(Alert.AlertType.INFORMATION, "✅ Candidature refusée.").showAndWait();
            load();
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "❌ Erreur: " + e.getMessage()).showAndWait();
        }
    }

    private void accepter(CandidatureEnfant ce) {
        Integer idEcole = AppSession.getInstance().getEcoleId();
        if (idEcole == null) return;
        try {
            java.util.List<Trajet> trajets = trajetService.selectByEcoleId(idEcole);
            if (trajets.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Aucun trajet disponible pour cette école.").showAndWait();
                return;
            }
            ChoiceDialog<Trajet> d = new ChoiceDialog<>(trajets.get(0), trajets);
            d.setTitle("Choisir le trajet");
            d.setHeaderText("Sélectionnez le trajet pour cet enfant");
            d.showAndWait().ifPresent(trajet -> {
                try {
                    candidatureEnfantService.accepter(ce.getId(), trajet.getTrajetId());
                    enfantService.insertFromCandidature(ce.getNomEnfant(), ce.getPrenomEnfant(), ce.getParentId(), trajet.getTrajetId());
                    new Alert(Alert.AlertType.INFORMATION, "✅ Candidature acceptée, enfant créé.").showAndWait();
                    load();
                } catch (SQLException ex) {
                    LOG.log(Level.SEVERE, "accepter", ex);
                    new Alert(Alert.AlertType.ERROR, "❌ Erreur: " + ex.getMessage()).showAndWait();
                }
            });
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "❌ Erreur: " + e.getMessage()).showAndWait();
        }
    }
}
