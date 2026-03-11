package tn.esprit.workshop.controlleurs.leith.parent;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import tn.esprit.workshop.model.leith.CandidatureEnfant;
import tn.esprit.workshop.services.leith.CandidatureEnfantService;
import tn.esprit.workshop.utilis.AppSession;

import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
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
        load();
    }

    private void load() {
        int parentId = AppSession.getInstance().getParentId();
        try {
            table.getItems().setAll(candidatureEnfantService.findByParentId(parentId));
            lblMessage.setText("");
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "load", e);
            lblMessage.setText("Erreur: " + e.getMessage());
        }
    }
}
