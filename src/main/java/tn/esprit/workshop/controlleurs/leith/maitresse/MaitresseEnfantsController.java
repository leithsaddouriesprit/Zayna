package tn.esprit.workshop.controlleurs.leith.maitresse;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import tn.esprit.workshop.model.leith.Enfant;
import tn.esprit.workshop.model.leith.Trajet;
import tn.esprit.workshop.services.leith.EnfantService;
import tn.esprit.workshop.services.leith.MaitresseMetierService;
import tn.esprit.workshop.services.leith.TrajetService;
import tn.esprit.workshop.utilis.AppSession;

import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class MaitresseEnfantsController implements Initializable {

    @FXML private Label lblEmpty;
    @FXML private TextField fieldSearch;
    @FXML private Button btnReclamation;
    @FXML private TableView<Enfant> table;
    @FXML private TableColumn<Enfant, String> colNom;
    @FXML private TableColumn<Enfant, String> colPrenom;
    @FXML private TableColumn<Enfant, String> colTrajet;
    @FXML private TableColumn<Enfant, String> colOnBoard;
    @FXML private TableColumn<Enfant, Void> colActions;
    @FXML private Label lblMessage;

    private final EnfantService enfantService = new EnfantService();
    private final TrajetService trajetService = new TrajetService();
    private final MaitresseMetierService maitresseMetierService = new MaitresseMetierService();

    private Integer busId;
    private final Map<Integer, String> trajetNames = new HashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Integer uid = AppSession.getInstance().getConnectedUserId();
        try {
            busId = uid != null ? maitresseMetierService.getBusIdForMaitresseUser(uid) : null;
        } catch (SQLException e) {
            busId = null;
        }

        if (busId == null || busId <= 0) {
            lblEmpty.setText("Aucun bus ne vous est actuellement affecté.");
            lblEmpty.setVisible(true);
            lblEmpty.setManaged(true);
            table.setVisible(false);
            table.setManaged(false);
            fieldSearch.setDisable(true);
            return;
        }

        colNom.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getNom() != null ? c.getValue().getNom() : ""));
        colPrenom.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getPrenom() != null ? c.getValue().getPrenom() : ""));
        colTrajet.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                trajetNames.getOrDefault(c.getValue().getTrajetId(), "—")));
        colOnBoard.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().isOnBoard() ? "Oui" : "Non"));

        colActions.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                Enfant enfant = getTableRow().getItem();
                Button btnOn = new Button("On Board");
                Button btnOff = new Button("Not On Board");
                btnOn.getStyleClass().add("btn-primary");
                btnOff.getStyleClass().add("btn-secondary");
                if (enfant.isOnBoard()) {
                    btnOn.setStyle("-fx-opacity: 1;");
                    btnOff.setStyle("-fx-opacity: 0.55;");
                } else {
                    btnOn.setStyle("-fx-opacity: 0.55;");
                    btnOff.setStyle("-fx-opacity: 1;");
                }
                btnOn.setOnAction(ev -> setBoard(enfant, true));
                btnOff.setOnAction(ev -> setBoard(enfant, false));
                HBox box = new HBox(8, btnOn, btnOff);
                setGraphic(box);
            }
        });

        fieldSearch.textProperty().addListener((o, a, b) -> reload());
        reload();
    }

    @FXML
    private void onReclamation() {
        new Alert(Alert.AlertType.INFORMATION, "À venir", ButtonType.OK).showAndWait();
    }

    private void setBoard(Enfant enfant, boolean on) {
        if (busId == null) return;
        new Thread(() -> {
            try {
                enfantService.updateOnBoardIfEnfantOnBus(enfant.getEnfantId(), busId, on);
                Platform.runLater(() -> {
                    lblMessage.setText("");
                    reload();
                });
            } catch (SQLException ex) {
                Platform.runLater(() -> lblMessage.setText("Erreur : " + ex.getMessage()));
            }
        }).start();
    }

    private void reload() {
        if (busId == null) return;
        new Thread(() -> {
            try {
                String q = fieldSearch != null ? fieldSearch.getText() : "";
                List<Enfant> list = enfantService.findEnfantsByBusId(busId, q);
                trajetNames.clear();
                for (Enfant e : list) {
                    int tid = e.getTrajetId();
                    if (!trajetNames.containsKey(tid)) {
                        try {
                            Trajet t = trajetService.getById(tid);
                            trajetNames.put(tid, t != null && t.getNom() != null ? t.getNom() : "Trajet #" + tid);
                        } catch (SQLException ex) {
                            trajetNames.put(tid, "Trajet #" + tid);
                        }
                    }
                }
                ObservableList<Enfant> obs = FXCollections.observableArrayList(list);
                Platform.runLater(() -> {
                    table.setItems(obs);
                    table.refresh();
                });
            } catch (SQLException e) {
                Platform.runLater(() -> lblMessage.setText("Erreur chargement : " + e.getMessage()));
            }
        }).start();
    }
}
