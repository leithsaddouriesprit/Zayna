package tn.esprit.workshop.controlleurs.Talel.admin;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import tn.esprit.workshop.model.leith.CandidatureAgent;
import tn.esprit.workshop.model.leith.CandidatureAgentStatut;
import tn.esprit.workshop.services.leith.CandidatureAgentService;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AdminCandidatureEcoleController implements Initializable {

    @FXML private TableView<CandidatureAgent> table;
    @FXML private TableColumn<CandidatureAgent, String> colNom;
    @FXML private TableColumn<CandidatureAgent, String> colPrenom;
    @FXML private TableColumn<CandidatureAgent, String> colEcole;
    @FXML private TableColumn<CandidatureAgent, String> colLat;
    @FXML private TableColumn<CandidatureAgent, String> colLon;
    @FXML private TableColumn<CandidatureAgent, String> colStatut;
    @FXML private TableColumn<CandidatureAgent, Void> colActions;

    private final CandidatureAgentService service = new CandidatureAgentService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colNom.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNom()));
        colPrenom.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getPrenom()));
        colEcole.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getNomEcole() != null ? c.getValue().getNomEcole() : ""));
        colLat.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                String.valueOf(c.getValue().getLatitude())));
        colLon.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                String.valueOf(c.getValue().getLongitude())));
        colStatut.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getStatut() != null ? c.getValue().getStatut().name() : ""));

        colActions.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                CandidatureAgent row = getTableRow().getItem();
                Button btnOk = new Button("Accepter");
                Button btnRef = new Button("Refuser");
                Button btnDel = new Button("Supprimer");
                btnOk.getStyleClass().add("btn-primary");
                btnRef.getStyleClass().add("btn-secondary");
                btnDel.getStyleClass().add("btn-soft");
                btnOk.setDisable(row.getStatut() == CandidatureAgentStatut.APPROUVEE);
                btnOk.setOnAction(e -> doStatut(row.getId(), CandidatureAgentStatut.APPROUVEE));
                btnRef.setOnAction(e -> doStatut(row.getId(), CandidatureAgentStatut.REFUSEE));
                btnDel.setOnAction(e -> doDelete(row.getId()));
                setGraphic(new HBox(6, btnOk, btnRef, btnDel));
            }
        });

        reload();
    }

    @FXML
    private void reload() {
        new Thread(() -> {
            try {
                List<CandidatureAgent> list = service.findAllForAdmin().stream()
                        .filter(c -> c.getStatut() == CandidatureAgentStatut.EN_ATTENTE)
                        .collect(Collectors.toList());
                Platform.runLater(() -> table.setItems(FXCollections.observableArrayList(list)));
            } catch (SQLException e) {
                Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait());
            }
        }).start();
    }

    private void doStatut(int id, CandidatureAgentStatut s) {
        new Thread(() -> {
            try {
                service.setStatut(id, s);
                Platform.runLater(this::reload);
            } catch (SQLException e) {
                Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait());
            }
        }).start();
    }

    private void doDelete(int id) {
        Alert c = new Alert(Alert.AlertType.CONFIRMATION);
        c.setContentText("Supprimer cette candidature ?");
        c.showAndWait().ifPresent(bt -> {
            if (bt != ButtonType.OK) return;
            new Thread(() -> {
                try {
                    service.deleteById(id);
                    Platform.runLater(this::reload);
                } catch (SQLException e) {
                    Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait());
                }
            }).start();
        });
    }
}
