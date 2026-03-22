package tn.esprit.workshop.controlleurs.Talel.admin;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import tn.esprit.workshop.model.leith.Bus;
import tn.esprit.workshop.model.leith.Candidature;
import tn.esprit.workshop.services.leith.BusService;
import tn.esprit.workshop.services.leith.CandidatureService;
import tn.esprit.workshop.services.leith.EcoleService;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class AdminChauffeursController implements Initializable {

    @FXML private TableView<Candidature> tableCandidatures;
    @FXML private TableColumn<Candidature, Number> colCId;
    @FXML private TableColumn<Candidature, String> colCChauffeur;
    @FXML private TableColumn<Candidature, String> colCStatut;
    @FXML private TableColumn<Candidature, Void> colCReclamations;
    @FXML private TableView<AcceptedChauffeurRow> tableAccepted;
    @FXML private TableColumn<AcceptedChauffeurRow, Number> colAId;
    @FXML private TableColumn<AcceptedChauffeurRow, String> colAChauffeur;
    @FXML private TableColumn<AcceptedChauffeurRow, String> colABus;
    @FXML private TableColumn<AcceptedChauffeurRow, String> colAEcole;
    @FXML private TableColumn<AcceptedChauffeurRow, Void> colAReclamations;
    @FXML private javafx.scene.control.Label lblCandidatures;
    @FXML private javafx.scene.control.Label lblAccepted;
    @FXML private javafx.scene.control.Label lblAssigned;

    private final CandidatureService candidatureService = new CandidatureService();
    private final BusService busService = new BusService();
    private final EcoleService ecoleService = new EcoleService();

    public static final class AcceptedChauffeurRow {
        private final int chauffeurId;
        private final String chauffeurNom;
        private final String busMatricule;
        private final String ecoleNom;

        public AcceptedChauffeurRow(int chauffeurId, String chauffeurNom, String busMatricule, String ecoleNom) {
            this.chauffeurId = chauffeurId;
            this.chauffeurNom = chauffeurNom != null ? chauffeurNom : "";
            this.busMatricule = busMatricule != null ? busMatricule : "—";
            this.ecoleNom = ecoleNom != null ? ecoleNom : "—";
        }
        public int getChauffeurId() { return chauffeurId; }
        public String getChauffeurNom() { return chauffeurNom; }
        public String getBusMatricule() { return busMatricule; }
        public String getEcoleNom() { return ecoleNom; }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colCId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCChauffeur.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                (c.getValue().getNom() != null ? c.getValue().getNom() : "") + " " + (c.getValue().getPrenom() != null ? c.getValue().getPrenom() : "")));
        colCStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colCReclamations.setCellFactory(col -> {
            Button btn = new Button("Réclamations");
            btn.setOnAction(e -> showReclamationsPlaceholder());
            return new javafx.scene.control.TableCell<>() {
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : btn);
                }
            };
        });

        colAId.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getChauffeurId()));
        colAChauffeur.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getChauffeurNom()));
        colABus.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getBusMatricule()));
        colAEcole.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getEcoleNom()));
        colAReclamations.setCellFactory(col -> {
            Button btn = new Button("Réclamations");
            btn.setOnAction(e -> showReclamationsPlaceholder());
            return new javafx.scene.control.TableCell<>() {
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : btn);
                }
            };
        });

        loadData();
    }

    private void showReclamationsPlaceholder() {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Réclamations");
        a.setHeaderText(null);
        a.setContentText("Fonctionnalité à venir");
        a.showAndWait();
    }

    private void loadData() {
        try {
            List<Candidature> pending = candidatureService.findAllEnVoyee();
            tableCandidatures.getItems().setAll(pending);

            List<Candidature> accepted = candidatureService.findAllAcceptees();
            List<AcceptedChauffeurRow> rows = new ArrayList<>();
            int assignedCount = 0;
            for (Candidature c : accepted) {
                String busMatricule = "—";
                Bus bus = busService.getByChauffeurId(c.getChauffeurId());
                if (bus != null) {
                    busMatricule = bus.getMatricule() != null ? bus.getMatricule() : "—";
                    assignedCount++;
                }
                String ecoleNom = "—";
                if (c.getIdEcole() != 0) {
                    try {
                        var e = ecoleService.getById(c.getIdEcole());
                        if (e != null) ecoleNom = e.getNomEcole();
                    } catch (SQLException ignored) {}
                }
                String nom = (c.getNom() != null ? c.getNom() : "") + " " + (c.getPrenom() != null ? c.getPrenom() : "");
                rows.add(new AcceptedChauffeurRow(c.getChauffeurId(), nom.trim(), busMatricule, ecoleNom));
            }
            tableAccepted.getItems().setAll(rows);

            if (lblCandidatures != null) lblCandidatures.setText("Candidatures: " + pending.size());
            if (lblAccepted != null) lblAccepted.setText("Acceptés: " + accepted.size());
            if (lblAssigned != null) lblAssigned.setText("Affectés à un bus: " + assignedCount);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
