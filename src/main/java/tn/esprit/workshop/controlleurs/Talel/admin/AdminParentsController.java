package tn.esprit.workshop.controlleurs.Talel.admin;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import tn.esprit.workshop.model.Talel.Dao.DaoParent;
import tn.esprit.workshop.model.Talel.talel2.Parent;
import tn.esprit.workshop.model.leith.CandidatureEnfant;
import tn.esprit.workshop.services.leith.CandidatureEnfantService;
import tn.esprit.workshop.services.leith.EnfantService;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class AdminParentsController implements Initializable {

    @FXML private TableView<ParentRow> tableParents;
    @FXML private TableColumn<ParentRow, Number> colId;
    @FXML private TableColumn<ParentRow, String> colNom;
    @FXML private TableColumn<ParentRow, String> colPrenom;
    @FXML private TableColumn<ParentRow, String> colEmail;
    @FXML private TableColumn<ParentRow, Number> colAccepted;
    @FXML private TableColumn<ParentRow, Number> colPending;
    @FXML private TableView<CandidatureEnfant> tablePending;
    @FXML private TableColumn<CandidatureEnfant, Number> colPId;
    @FXML private TableColumn<CandidatureEnfant, Number> colPParent;
    @FXML private TableColumn<CandidatureEnfant, String> colPEnfant;
    @FXML private TableColumn<CandidatureEnfant, String> colPStatut;
    @FXML private Label lblTotalParents;
    @FXML private Label lblTotalAccepted;
    @FXML private Label lblTotalPending;

    private final ObservableList<ParentRow> parentRows = FXCollections.observableArrayList();
    private final ObservableList<CandidatureEnfant> pendingList = FXCollections.observableArrayList();
    private final DaoParent daoParent = new DaoParent();
    private final EnfantService enfantService = new EnfantService();
    private final CandidatureEnfantService candidatureEnfantService = new CandidatureEnfantService();

    public static final class ParentRow {
        private final SimpleIntegerProperty id = new SimpleIntegerProperty();
        private final SimpleStringProperty nom = new SimpleStringProperty();
        private final SimpleStringProperty prenom = new SimpleStringProperty();
        private final SimpleStringProperty email = new SimpleStringProperty();
        private final SimpleIntegerProperty acceptedCount = new SimpleIntegerProperty();
        private final SimpleIntegerProperty pendingCount = new SimpleIntegerProperty();

        public ParentRow(int id, String nom, String prenom, String email, int acceptedCount, int pendingCount) {
            this.id.set(id);
            this.nom.set(nom != null ? nom : "");
            this.prenom.set(prenom != null ? prenom : "");
            this.email.set(email != null ? email : "");
            this.acceptedCount.set(acceptedCount);
            this.pendingCount.set(pendingCount);
        }
        public SimpleIntegerProperty idProperty() { return id; }
        public SimpleStringProperty nomProperty() { return nom; }
        public SimpleStringProperty prenomProperty() { return prenom; }
        public SimpleStringProperty emailProperty() { return email; }
        public SimpleIntegerProperty acceptedCountProperty() { return acceptedCount; }
        public SimpleIntegerProperty pendingCountProperty() { return pendingCount; }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colId.setCellValueFactory(c -> c.getValue().idProperty());
        colNom.setCellValueFactory(c -> c.getValue().nomProperty());
        colPrenom.setCellValueFactory(c -> c.getValue().prenomProperty());
        colEmail.setCellValueFactory(c -> c.getValue().emailProperty());
        colAccepted.setCellValueFactory(c -> c.getValue().acceptedCountProperty());
        colPending.setCellValueFactory(c -> c.getValue().pendingCountProperty());

        colPId.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getId()));
        colPParent.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getParentId()));
        colPEnfant.setCellValueFactory(c -> new SimpleStringProperty(
                (c.getValue().getNomEnfant() != null ? c.getValue().getNomEnfant() : "") + " " +
                (c.getValue().getPrenomEnfant() != null ? c.getValue().getPrenomEnfant() : "")));
        colPStatut.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatut() != null ? c.getValue().getStatut() : ""));

        tableParents.setItems(parentRows);
        tablePending.setItems(pendingList);
        loadData();
    }

    private void loadData() {
        parentRows.clear();
        pendingList.clear();
        int totalAccepted = 0;
        try {
            List<Parent> parents = daoParent.findAll();
            for (Parent p : parents) {
                int accepted = 0;
                try {
                    accepted = enfantService.getByParentId(p.getId()).size();
                } catch (SQLException e) { /* ignore */ }
                int pending = 0;
                try {
                    List<CandidatureEnfant> byParent = candidatureEnfantService.findByParentId(p.getId());
                    for (CandidatureEnfant ce : byParent) {
                        if (CandidatureEnfantService.STATUT_ENVOYEE.equals(ce.getStatut())) pending++;
                    }
                } catch (SQLException e) { /* ignore */ }
                totalAccepted += accepted;
                parentRows.add(new ParentRow(p.getId(), p.getNom(), p.getPrenom(), p.getEmail(), accepted, pending));
            }
            List<CandidatureEnfant> allPending = candidatureEnfantService.findAllEnVoyee();
            pendingList.addAll(allPending);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (lblTotalParents != null) lblTotalParents.setText("Parents: " + parentRows.size());
        if (lblTotalAccepted != null) lblTotalAccepted.setText("Enfants acceptés: " + totalAccepted);
        if (lblTotalPending != null) lblTotalPending.setText("En attente: " + pendingList.size());
    }

    @FXML
    private void onReclamations() {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Réclamations");
        a.setHeaderText(null);
        a.setContentText("Fonction à venir.");
        a.showAndWait();
    }
}
