package tn.esprit.workshop.controlleurs.leith.agent;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import tn.esprit.workshop.model.leith.Candidature;
import tn.esprit.workshop.services.leith.CandidatureService;
import tn.esprit.workshop.utilis.AppSession;

import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AgentChauffeursAcceptesController implements Initializable {

    private static final Logger LOG = Logger.getLogger(AgentChauffeursAcceptesController.class.getName());

    @FXML private TableView<Candidature> table;
    @FXML private TableColumn<Candidature, String> colNom;
    @FXML private TableColumn<Candidature, String> colPrenom;
    @FXML private TableColumn<Candidature, Integer> colAge;
    @FXML private TableColumn<Candidature, Integer> colExperience;
    @FXML private TableColumn<Candidature, String> colDate;

    private final CandidatureService candidatureService = new CandidatureService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colNom.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNom()));
        colPrenom.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getPrenom()));
        colAge.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getAge()).asObject());
        colExperience.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getNbAnsExperience()).asObject());
        colDate.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getDateEnvoi() != null ? c.getValue().getDateEnvoi().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : ""));
        load();
    }

    private void load() {
        Integer idEcole = AppSession.getInstance().getEcoleId();
        if (idEcole == null) return;
        try {
            table.getItems().setAll(candidatureService.findAllAccepteesByEcoleId(idEcole));
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "load acceptees", e);
        }
    }
}
