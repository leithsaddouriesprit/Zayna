package tn.esprit.workshop.controlleurs.leith.agent;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import tn.esprit.workshop.controlleurs.leith.SceneNavigator;
import tn.esprit.workshop.model.leith.MaitresseEcoleRow;
import tn.esprit.workshop.services.leith.MaitresseMetierService;
import tn.esprit.workshop.utilis.AppSession;
import tn.esprit.workshop.utilis.ZaynaInputConstraints;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class AgentMaitressesListeController implements Initializable {

    @FXML private TextField fieldSearch;
    @FXML private TableView<MaitresseEcoleRow> table;
    @FXML private TableColumn<MaitresseEcoleRow, String> colNom;
    @FXML private TableColumn<MaitresseEcoleRow, String> colPrenom;
    @FXML private TableColumn<MaitresseEcoleRow, String> colEmail;
    @FXML private TableColumn<MaitresseEcoleRow, String> colBus;
    @FXML private TableColumn<MaitresseEcoleRow, Void> colActions;
    @FXML private Label lblMessage;

    private final MaitresseMetierService service = new MaitresseMetierService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colNom.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNom()));
        colPrenom.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getPrenom()));
        colEmail.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getEmail()));
        colBus.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getIdBus() != null && c.getValue().getBusLabel() != null && !c.getValue().getBusLabel().isBlank()
                        ? c.getValue().getBusLabel()
                        : "—"));

        colActions.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                MaitresseEcoleRow row = getTableRow().getItem();
                Button btnMod = new Button("Modifier");
                Button btnDel = new Button("Supprimer");
                boolean hasBus = row.getIdBus() != null && row.getIdBus() > 0;
                Button btnBus = new Button(hasBus ? "Changer bus" : "Affecter bus");
                btnMod.getStyleClass().add("btn-secondary");
                btnDel.getStyleClass().add("btn-soft");
                btnBus.getStyleClass().add("btn-primary");
                btnMod.setOnAction(e -> openEdit(row));
                btnDel.setOnAction(e -> confirmDelete(row));
                btnBus.setOnAction(e -> openBusOnly(row));
                setGraphic(new HBox(6, btnMod, btnBus, btnDel));
            }
        });

        fieldSearch.textProperty().addListener((o, a, b) -> reload());
        reload();
    }

    @FXML
    private void reload() {
        Integer ecoleId = AppSession.getInstance().getEcoleId();
        if (ecoleId == null) {
            lblMessage.setText("École non définie.");
            return;
        }
        String q = fieldSearch.getText();
        new Thread(() -> {
            try {
                List<MaitresseEcoleRow> list = service.listByEcole(ecoleId, q);
                Platform.runLater(() -> {
                    table.setItems(FXCollections.observableArrayList(list));
                    lblMessage.setText("");
                });
            } catch (SQLException e) {
                Platform.runLater(() -> lblMessage.setText(e.getMessage()));
            }
        }).start();
    }

    private void openEdit(MaitresseEcoleRow row) {
        Integer ecoleId = AppSession.getInstance().getEcoleId();
        if (ecoleId == null) return;

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modifier la maîtresse");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));

        TextField fNom = new TextField(row.getNom());
        TextField fPrenom = new TextField(row.getPrenom());
        TextField fEmail = new TextField(row.getEmail());
        ZaynaInputConstraints.apply(fNom, ZaynaInputConstraints.lettersAndSpacesOnly(ZaynaInputConstraints.LEN_MAITRESSE_NOM_PRENOM));
        ZaynaInputConstraints.apply(fPrenom, ZaynaInputConstraints.lettersAndSpacesOnly(ZaynaInputConstraints.LEN_MAITRESSE_NOM_PRENOM));
        ZaynaInputConstraints.apply(fEmail, ZaynaInputConstraints.emailInput(ZaynaInputConstraints.LEN_USERS_EMAIL));
        PasswordField fPw = new PasswordField();
        fPw.setPromptText("Laisser vide pour ne pas changer");
        ComboBox<MaitresseMetierService.BusOption> combo = new ComboBox<>();
        SceneNavigator.applyAppCssToComboBoxPopup(combo);

        try {
            List<MaitresseMetierService.BusOption> opts = new ArrayList<>();
            opts.add(new MaitresseMetierService.BusOption(0, "Aucun / Non affecté"));
            opts.addAll(service.listAvailableBusesForEcole(ecoleId, row.getMaitresseId()));
            combo.getItems().setAll(opts);
            int sel = 0;
            if (row.getIdBus() != null) {
                for (int i = 0; i < opts.size(); i++) {
                    if (opts.get(i).getId() == row.getIdBus()) {
                        sel = i;
                        break;
                    }
                }
            }
            combo.getSelectionModel().select(sel);
        } catch (SQLException e) {
            lblMessage.setText(e.getMessage());
            return;
        }

        int r = 0;
        grid.addRow(r++, new Label("Nom"), fNom);
        grid.addRow(r++, new Label("Prénom"), fPrenom);
        grid.addRow(r++, new Label("Email"), fEmail);
        grid.addRow(r++, new Label("Mot de passe"), fPw);
        grid.addRow(r++, new Label("Bus"), combo);
        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isEmpty() || res.get() != ButtonType.OK) {
            return;
        }

        String vn = fNom.getText() != null ? fNom.getText().trim() : "";
        String vp = fPrenom.getText() != null ? fPrenom.getText().trim() : "";
        String ve = fEmail.getText() != null ? fEmail.getText().trim().toLowerCase() : "";
        String e1 = ZaynaInputConstraints.validatePersonName(vn, ZaynaInputConstraints.LEN_MAITRESSE_NOM_PRENOM, "Le nom");
        if (e1 != null) {
            new Alert(Alert.AlertType.WARNING, e1).showAndWait();
            return;
        }
        String e2 = ZaynaInputConstraints.validatePersonName(vp, ZaynaInputConstraints.LEN_MAITRESSE_NOM_PRENOM, "Le prénom");
        if (e2 != null) {
            new Alert(Alert.AlertType.WARNING, e2).showAndWait();
            return;
        }
        String e3 = ZaynaInputConstraints.validateEmail(ve, ZaynaInputConstraints.LEN_USERS_EMAIL);
        if (e3 != null) {
            new Alert(Alert.AlertType.WARNING, e3).showAndWait();
            return;
        }

        MaitresseMetierService.BusOption bus = combo.getSelectionModel().getSelectedItem();
        Integer idBus = (bus == null || bus.getId() <= 0) ? null : bus.getId();
        String pw = fPw.getText();
        String newPw = (pw == null || pw.isBlank()) ? null : pw;

        new Thread(() -> {
            try {
                service.updateMaitresse(ecoleId, row.getMaitresseId(), vn, vp, ve, idBus, newPw);
                Platform.runLater(this::reload);
            } catch (Exception e) {
                Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait());
            }
        }).start();
    }

    private void openBusOnly(MaitresseEcoleRow row) {
        Integer ecoleId = AppSession.getInstance().getEcoleId();
        if (ecoleId == null) return;

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Affecter / changer le bus");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ComboBox<MaitresseMetierService.BusOption> combo = new ComboBox<>();
        SceneNavigator.applyAppCssToComboBoxPopup(combo);
        try {
            List<MaitresseMetierService.BusOption> opts = new ArrayList<>();
            opts.add(new MaitresseMetierService.BusOption(0, "Aucun / Non affecté"));
            opts.addAll(service.listAvailableBusesForEcole(ecoleId, row.getMaitresseId()));
            combo.getItems().setAll(opts);
            int sel = 0;
            if (row.getIdBus() != null) {
                for (int i = 0; i < opts.size(); i++) {
                    if (opts.get(i).getId() == row.getIdBus()) {
                        sel = i;
                        break;
                    }
                }
            }
            combo.getSelectionModel().select(sel);
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
            return;
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));
        grid.addRow(0, new Label("Bus"), combo);
        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(bt -> {
            if (bt != ButtonType.OK) return;
            MaitresseMetierService.BusOption bus = combo.getSelectionModel().getSelectedItem();
            Integer idBus = (bus == null || bus.getId() <= 0) ? null : bus.getId();
            new Thread(() -> {
                try {
                    service.updateMaitresse(ecoleId, row.getMaitresseId(), row.getNom(), row.getPrenom(), row.getEmail(), idBus, null);
                    Platform.runLater(this::reload);
                } catch (Exception e) {
                    Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait());
                }
            }).start();
        });
    }

    private void confirmDelete(MaitresseEcoleRow row) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setContentText("Supprimer cette maîtresse et son compte utilisateur ?");
        confirm.showAndWait().ifPresent(bt -> {
            if (bt != ButtonType.OK) return;
            Integer ecoleId = AppSession.getInstance().getEcoleId();
            if (ecoleId == null) return;
            new Thread(() -> {
                try {
                    service.deleteMaitresse(ecoleId, row.getMaitresseId());
                    Platform.runLater(this::reload);
                } catch (SQLException e) {
                    Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait());
                }
            }).start();
        });
    }
}
