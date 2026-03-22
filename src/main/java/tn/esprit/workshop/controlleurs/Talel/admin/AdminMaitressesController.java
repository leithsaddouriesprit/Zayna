package tn.esprit.workshop.controlleurs.Talel.admin;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import tn.esprit.workshop.model.leith.MaitresseEcoleRow;
import tn.esprit.workshop.services.leith.MaitresseMetierService;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class AdminMaitressesController implements Initializable {

    @FXML private TextField fieldSearch;
    @FXML private TableView<MaitresseEcoleRow> tableMaitresses;
    @FXML private TableColumn<MaitresseEcoleRow, Number> colId;
    @FXML private TableColumn<MaitresseEcoleRow, String> colNom;
    @FXML private TableColumn<MaitresseEcoleRow, String> colPrenom;
    @FXML private TableColumn<MaitresseEcoleRow, String> colEmail;
    @FXML private TableColumn<MaitresseEcoleRow, String> colEcole;
    @FXML private TableColumn<MaitresseEcoleRow, String> colBus;
    @FXML private TableColumn<MaitresseEcoleRow, Void> colReclamations;
    @FXML private Label lblMaitressesTotal;
    @FXML private Label lblMaitressesAvecBus;

    private final MaitresseMetierService maitresseMetierService = new MaitresseMetierService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getMaitresseId()));
        colNom.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(str(c.getValue().getNom())));
        colPrenom.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(str(c.getValue().getPrenom())));
        colEmail.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(str(c.getValue().getEmail())));
        colEcole.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(str(c.getValue().getEcoleNom())));
        colBus.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getIdBus() != null && c.getValue().getBusLabel() != null && !c.getValue().getBusLabel().isBlank()
                        ? c.getValue().getBusLabel() : "—"));
        colReclamations.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                Button btn = new Button("Réclamation");
                btn.setOnAction(e -> showReclamationsPlaceholder());
                setGraphic(btn);
            }
        });
        fieldSearch.textProperty().addListener((o, a, b) -> loadData());
        loadData();
    }

    private static String str(String s) {
        return s != null ? s : "";
    }

    @FXML
    private void reload() {
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
            String q = fieldSearch != null ? fieldSearch.getText() : "";
            List<MaitresseEcoleRow> list = maitresseMetierService.listAllForAdmin(q);
            tableMaitresses.getItems().setAll(list);
            long avecBus = list.stream()
                    .filter(m -> m.getIdBus() != null)
                    .count();
            if (lblMaitressesTotal != null) {
                lblMaitressesTotal.setText("Maîtresses : " + list.size());
            }
            if (lblMaitressesAvecBus != null) {
                lblMaitressesAvecBus.setText("Maîtresses affectées à un bus : " + avecBus);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            if (lblMaitressesTotal != null) lblMaitressesTotal.setText("Maîtresses : —");
            if (lblMaitressesAvecBus != null) {
                lblMaitressesAvecBus.setText("Erreur SQL (table maitresse créée ?)");
            }
        }
    }
}
