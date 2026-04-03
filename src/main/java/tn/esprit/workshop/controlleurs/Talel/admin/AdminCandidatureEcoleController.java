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
    @FXML private TableColumn<CandidatureAgent, String> colAdresse;
    @FXML private TableColumn<CandidatureAgent, String> colLat;
    @FXML private TableColumn<CandidatureAgent, String> colLon;
    @FXML private TableColumn<CandidatureAgent, String> colStatut;
    @FXML private TableColumn<CandidatureAgent, String> colDepot;
    @FXML private TableColumn<CandidatureAgent, Void> colActions;

    private final CandidatureAgentService service = new CandidatureAgentService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colNom.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNom()));
        colPrenom.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getPrenom()));
        colEcole.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getEcole() != null ? c.getValue().getEcole() : ""));
        colAdresse.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getAdresse() != null ? c.getValue().getAdresse() : ""));
        colLat.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                String.valueOf(c.getValue().getLatitude())));
        colLon.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                String.valueOf(c.getValue().getLongitude())));
        colStatut.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getStatut() != null ? c.getValue().getStatut().name() : ""));
        colDepot.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(depotLine(c.getValue())));

        wrapTextColumn(colEcole);
        wrapTextColumn(colAdresse);
        wrapTextColumn(colDepot);

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
                btnOk.getStyleClass().add("btn-primary");
                btnRef.getStyleClass().add("btn-secondary");
                btnOk.setDisable(row.getStatut() != CandidatureAgentStatut.EN_ATTENTE);
                btnRef.setDisable(row.getStatut() != CandidatureAgentStatut.EN_ATTENTE);
                btnOk.setOnAction(e -> doStatut(row.getId(), CandidatureAgentStatut.APPROUVEE));
                btnRef.setOnAction(e -> doStatut(row.getId(), CandidatureAgentStatut.REFUSEE));
                setGraphic(new HBox(8, btnOk, btnRef));
            }
        });

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        reload();
    }

    private static String depotLine(CandidatureAgent c) {
        if (c == null) return "";
        String cr = c.getCreatedAt() != null ? c.getCreatedAt() : "—";
        String up = c.getUpdatedAt() != null ? c.getUpdatedAt() : "—";
        return "Créée : " + cr + "\nMAJ : " + up;
    }

    private static void wrapTextColumn(TableColumn<CandidatureAgent, String> col) {
        col.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setWrapText(true);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                }
            }
        });
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
                if (s == CandidatureAgentStatut.APPROUVEE) {
                    service.approveAndProvision(id);
                } else {
                    service.setStatut(id, s);
                }
                Platform.runLater(this::reload);
            } catch (SQLException e) {
                Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait());
            }
        }).start();
    }
}
