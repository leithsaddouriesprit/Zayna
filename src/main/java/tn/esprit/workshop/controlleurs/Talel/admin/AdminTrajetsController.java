package tn.esprit.workshop.controlleurs.Talel.admin;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import tn.esprit.workshop.model.tous.Ecole;
import tn.esprit.workshop.services.admin.AdminDataService;
import tn.esprit.workshop.services.admin.AdminDataService.TrajetRow;
import tn.esprit.workshop.services.leith.EcoleService;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class AdminTrajetsController implements Initializable {

    @FXML private ComboBox<Ecole> comboEcole;
    @FXML private TableView<TrajetRow> tableTrajets;
    @FXML private TableColumn<TrajetRow, Number> colId;
    @FXML private TableColumn<TrajetRow, String> colNom;
    @FXML private TableColumn<TrajetRow, String> colEcole;
    @FXML private TableColumn<TrajetRow, String> colBus;
    @FXML private TableColumn<TrajetRow, String> colChauffeur;
    @FXML private TableColumn<TrajetRow, Number> colEnfants;
    @FXML private javafx.scene.control.Label lblTotalTrajets;
    @FXML private javafx.scene.control.Label lblTotalEnfants;

    private final AdminDataService adminDataService = new AdminDataService();
    private final EcoleService ecoleService = new EcoleService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getId()));
        colNom.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNom()));
        colEcole.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getEcoleNom()));
        colBus.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getBusMatricule()));
        colChauffeur.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getChauffeurNom()));
        colEnfants.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getEnfantsCount()));

        try {
            List<Ecole> ecoles = ecoleService.selectAll();
            comboEcole.getItems().clear();
            comboEcole.getItems().add(null);
            comboEcole.getItems().addAll(ecoles);
            comboEcole.setConverter(new javafx.util.StringConverter<Ecole>() {
                @Override
                public String toString(Ecole e) {
                    return e == null ? "Toutes les écoles" : e.getNomEcole();
                }
                @Override
                public Ecole fromString(String string) { return null; }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
        comboEcole.getSelectionModel().selectedItemProperty().addListener((o, old, val) -> loadData());
        loadData();
    }

    @FXML
    void onFilter() {
        loadData();
    }

    private void loadData() {
        try {
            Integer idEcole = null;
            Ecole sel = comboEcole.getSelectionModel().getSelectedItem();
            if (sel != null) idEcole = sel.getId();
            List<TrajetRow> list = adminDataService.getTrajetsWithDetails(idEcole);
            tableTrajets.getItems().setAll(list);
            int totalEnfants = list.stream().mapToInt(TrajetRow::getEnfantsCount).sum();
            if (lblTotalTrajets != null) lblTotalTrajets.setText("Trajets: " + list.size());
            if (lblTotalEnfants != null) lblTotalEnfants.setText("Enfants (trajets affichés): " + totalEnfants);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
