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
import tn.esprit.workshop.model.leith.AgentEnfantEcoleRow;
import tn.esprit.workshop.model.leith.Bus;
import tn.esprit.workshop.model.leith.Trajet;
import tn.esprit.workshop.services.leith.BusService;
import tn.esprit.workshop.services.leith.EnfantService;
import tn.esprit.workshop.services.leith.TrajetService;
import tn.esprit.workshop.utilis.AppSession;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class AgentEnfantsController implements Initializable {

    private static final class TrajetChoice {
        final int id;
        final String label;
        final int idBus;

        TrajetChoice(int id, String label, int idBus) {
            this.id = id;
            this.label = label;
            this.idBus = idBus;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private static final class BusChoice {
        final int id;
        final String label;

        BusChoice(int id, String label) {
            this.id = id;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    @FXML private TextField fieldSearch;
    @FXML private TableView<AgentEnfantEcoleRow> table;
    @FXML private TableColumn<AgentEnfantEcoleRow, String> colNom;
    @FXML private TableColumn<AgentEnfantEcoleRow, String> colPrenom;
    @FXML private TableColumn<AgentEnfantEcoleRow, String> colTrajet;
    @FXML private TableColumn<AgentEnfantEcoleRow, String> colBus;
    @FXML private TableColumn<AgentEnfantEcoleRow, String> colOnBoard;
    @FXML private TableColumn<AgentEnfantEcoleRow, Void> colActions;
    @FXML private Label lblTotal;
    @FXML private Label lblMessage;

    private final EnfantService enfantService = new EnfantService();
    private final TrajetService trajetService = new TrajetService();
    private final BusService busService = new BusService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colNom.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(str(c.getValue().getNom())));
        colPrenom.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(str(c.getValue().getPrenom())));
        colTrajet.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(str(c.getValue().getTrajetNom())));
        colBus.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(str(c.getValue().getBusLabel())));
        colOnBoard.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().isOnBoard() ? "Oui" : "Non"));

        colActions.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                AgentEnfantEcoleRow row = getTableRow().getItem();
                boolean hasBus = row.getIdBus() != null && row.getIdBus() > 0;
                Button btnBus = new Button(hasBus ? "Changer bus" : "Affecter bus");
                Button btnDel = new Button("Supprimer");
                btnBus.getStyleClass().add("btn-primary");
                btnDel.getStyleClass().add("btn-soft");
                btnBus.setOnAction(e -> openAffecterTrajetBus(row));
                btnDel.setOnAction(e -> confirmDelete(row));
                setGraphic(new HBox(8, btnBus, btnDel));
            }
        });

        fieldSearch.textProperty().addListener((o, a, b) -> reload());
        reload();
    }

    private static String str(String s) {
        return s != null ? s : "";
    }

    @FXML
    private void reload() {
        Integer ecoleId = AppSession.getInstance().getEcoleId();
        if (ecoleId == null) {
            lblMessage.setText("École non définie pour votre session.");
            return;
        }
        String q = fieldSearch.getText();
        new Thread(() -> {
            try {
                List<AgentEnfantEcoleRow> list = enfantService.listActifsByEcoleViaTrajet(ecoleId, q);
                Platform.runLater(() -> {
                    table.setItems(FXCollections.observableArrayList(list));
                    if (lblTotal != null) {
                        lblTotal.setText("Enfants : " + list.size());
                    }
                    lblMessage.setText(list.isEmpty() ? "Aucun enfant ne correspond à votre recherche ou au périmètre trajet de l'école." : "");
                });
            } catch (SQLException e) {
                Platform.runLater(() -> lblMessage.setText(e.getMessage()));
            }
        }).start();
    }

    private void openAffecterTrajetBus(AgentEnfantEcoleRow row) {
        Integer ecoleId = AppSession.getInstance().getEcoleId();
        if (ecoleId == null) {
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Affectation trajet et bus");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ComboBox<TrajetChoice> comboTrajet = new ComboBox<>();
        ComboBox<BusChoice> comboBus = new ComboBox<>();
        SceneNavigator.applyAppCssToComboBoxPopup(comboTrajet);
        SceneNavigator.applyAppCssToComboBoxPopup(comboBus);

        List<TrajetChoice> trajetChoices = new ArrayList<>();
        try {
            for (Trajet t : trajetService.selectByEcoleId(ecoleId)) {
                if (!t.isActif()) {
                    continue;
                }
                String lab = t.getNom() != null ? t.getNom() : ("Trajet #" + t.getTrajetId());
                trajetChoices.add(new TrajetChoice(t.getTrajetId(), lab, t.getIdBus()));
            }
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
            return;
        }
        comboTrajet.getItems().setAll(trajetChoices);

        Runnable refreshBus = () -> {
            comboBus.getItems().clear();
            TrajetChoice tc = comboTrajet.getSelectionModel().getSelectedItem();
            if (tc == null || tc.idBus <= 0) {
                return;
            }
            try {
                Bus b = busService.getById(tc.idBus);
                if (b == null) {
                    return;
                }
                if (b.getIdEcole() != null && b.getIdEcole() != ecoleId) {
                    return;
                }
                String bl = (b.getMatricule() != null && !b.getMatricule().isBlank())
                        ? b.getMatricule()
                        : (b.getNumeroBus() != null && !b.getNumeroBus().isBlank() ? b.getNumeroBus() : ("Bus " + b.getBusId()));
                comboBus.getItems().add(new BusChoice(b.getBusId(), bl));
                comboBus.getSelectionModel().select(0);
            } catch (SQLException ignored) {
            }
        };

        comboTrajet.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> refreshBus.run());

        for (int i = 0; i < trajetChoices.size(); i++) {
            if (trajetChoices.get(i).id == row.getTrajetId()) {
                comboTrajet.getSelectionModel().select(i);
                break;
            }
        }
        if (comboTrajet.getSelectionModel().getSelectedIndex() < 0 && !trajetChoices.isEmpty()) {
            comboTrajet.getSelectionModel().select(0);
        }
        refreshBus.run();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));
        int r = 0;
        grid.addRow(r++, new Label("1. Trajet (école)"), comboTrajet);
        grid.addRow(r++, new Label("2. Bus du trajet"), comboBus);
        Label hint = new Label("Le bus affiché est celui affecté au trajet choisi. Pour un autre bus, choisissez un autre trajet.");
        hint.setWrapText(true);
        hint.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 12px;");
        grid.add(hint, 0, r++, 2, 1);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isEmpty() || res.get() != ButtonType.OK) {
            return;
        }

        TrajetChoice selT = comboTrajet.getSelectionModel().getSelectedItem();
        if (selT == null) {
            new Alert(Alert.AlertType.WARNING, "Veuillez sélectionner un trajet.").showAndWait();
            return;
        }
        BusChoice selB = comboBus.getSelectionModel().getSelectedItem();
        if (selB == null || comboBus.getItems().isEmpty()) {
            new Alert(Alert.AlertType.WARNING,
                    selT.idBus <= 0 ? "Aucun bus disponible pour ce trajet." : "Veuillez sélectionner un bus.").showAndWait();
            return;
        }
        if (selB.id != selT.idBus) {
            new Alert(Alert.AlertType.WARNING, "Le bus ne correspond pas au trajet sélectionné.").showAndWait();
            return;
        }

        new Thread(() -> {
            try {
                enfantService.updateEnfantTrajetForEcole(row.getEnfantId(), selT.id, ecoleId);
                Platform.runLater(() -> {
                    new Alert(Alert.AlertType.INFORMATION, "Affectation mise à jour avec succès.").showAndWait();
                    reload();
                });
            } catch (SQLException e) {
                Platform.runLater(() -> new Alert(Alert.AlertType.ERROR,
                        "Impossible de mettre à jour l'affectation.\n" + e.getMessage()).showAndWait());
            }
        }).start();
    }

    private void confirmDelete(AgentEnfantEcoleRow row) {
        Integer ecoleId = AppSession.getInstance().getEcoleId();
        if (ecoleId == null) {
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Voulez-vous vraiment supprimer cet enfant ?");
        confirm.showAndWait().ifPresent(bt -> {
            if (bt != ButtonType.OK) {
                return;
            }
            new Thread(() -> {
                try {
                    enfantService.deleteEnfantIfInEcoleScope(row.getEnfantId(), ecoleId);
                    Platform.runLater(() -> {
                        new Alert(Alert.AlertType.INFORMATION, "Enfant supprimé avec succès.").showAndWait();
                        reload();
                    });
                } catch (SQLException e) {
                    Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Impossible de supprimer cet enfant.").showAndWait());
                }
            }).start();
        });
    }
}
