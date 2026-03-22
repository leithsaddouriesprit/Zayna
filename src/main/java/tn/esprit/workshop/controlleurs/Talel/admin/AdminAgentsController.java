package tn.esprit.workshop.controlleurs.Talel.admin;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import tn.esprit.workshop.services.admin.AdminDataService;
import tn.esprit.workshop.services.admin.AdminDataService.AgentRow;

import java.net.URL;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;

public class AdminAgentsController implements Initializable {

    @FXML private TableView<AgentRow> tableAgents;
    @FXML private TableColumn<AgentRow, Number> colId;
    @FXML private TableColumn<AgentRow, String> colNom;
    @FXML private TableColumn<AgentRow, String> colPrenom;
    @FXML private TableColumn<AgentRow, String> colEcole;
    @FXML private TableColumn<AgentRow, Void> colReclamations;
    @FXML private javafx.scene.control.Label lblTotalAgents;
    @FXML private javafx.scene.control.Label lblTotalEcoles;

    private final AdminDataService adminDataService = new AdminDataService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getId()));
        colNom.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNom()));
        colPrenom.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getPrenom()));
        colEcole.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getEcoleNom()));
        colReclamations.setCellFactory(col -> {
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
            List<AgentRow> list = adminDataService.getAgentsWithSchool();
            tableAgents.getItems().setAll(list);
            HashSet<Integer> ecoles = new HashSet<>();
            for (AgentRow r : list) ecoles.add(r.getIdEcole());
            if (lblTotalAgents != null) lblTotalAgents.setText("Agents: " + list.size());
            if (lblTotalEcoles != null) lblTotalEcoles.setText("Écoles représentées: " + ecoles.size());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
