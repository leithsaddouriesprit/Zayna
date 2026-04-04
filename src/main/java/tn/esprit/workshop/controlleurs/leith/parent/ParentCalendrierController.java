package tn.esprit.workshop.controlleurs.leith.parent;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import tn.esprit.workshop.controlleurs.leith.SceneNavigator;
import tn.esprit.workshop.model.amal.JourFerieApi;
import tn.esprit.workshop.services.amal.JourFerieApiService;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ParentCalendrierController implements Initializable {

    private static final Logger LOG = Logger.getLogger(ParentCalendrierController.class.getName());

    @FXML private VBox loadingBox;
    @FXML private VBox contentBox;
    @FXML private ToggleButton btnYear2025;
    @FXML private ToggleButton btnYear2026;

    private final JourFerieApiService jourFerieApiService = new JourFerieApiService();
    private final ToggleGroup yearGroup = new ToggleGroup();

    private List<JourFerieApi> data2025;
    private List<JourFerieApi> data2026;
    private TableView<JourFerieApi> holidayTable;
    private VBox tableWrapper;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnYear2025.setToggleGroup(yearGroup);
        btnYear2026.setToggleGroup(yearGroup);
        yearGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            if (holidayTable == null || newT == null) {
                return;
            }
            if (newT == btnYear2025) {
                holidayTable.setItems(FXCollections.observableArrayList(data2025));
            } else if (newT == btnYear2026) {
                holidayTable.setItems(FXCollections.observableArrayList(data2026));
            }
        });

        new Thread(() -> {
            try {
                Thread.sleep(200);
                List<JourFerieApi> j2025 = jourFerieApiService.getJoursFeries(2025);
                List<JourFerieApi> j2026 = jourFerieApiService.getJoursFeries(2026);
                Platform.runLater(() -> showCalendrier(j2025, j2026));
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "calendrier", e);
                Platform.runLater(() -> {
                    loadingBox.setVisible(false);
                    loadingBox.setManaged(false);
                    Label err = new Label("Impossible de charger le calendrier. Réessayez plus tard.");
                    err.getStyleClass().add("parent-card-text");
                    err.setStyle("-fx-text-fill: #fca5a5;");
                    err.setWrapText(true);
                    contentBox.getChildren().clear();
                    contentBox.getChildren().add(err);
                    contentBox.setVisible(true);
                    contentBox.setManaged(true);
                });
            }
        }).start();
    }

    private void showCalendrier(List<JourFerieApi> j2025, List<JourFerieApi> j2026) {
        loadingBox.setVisible(false);
        loadingBox.setManaged(false);

        data2025 = j2025;
        data2026 = j2026;
        btnYear2025.setText("2025 — " + j2025.size() + " jour(s)");
        btnYear2026.setText("2026 — " + j2026.size() + " jour(s)");

        if (holidayTable == null) {
            holidayTable = buildHolidayTable(j2025);
            tableWrapper = wrapHolidayTable(holidayTable);
            contentBox.getChildren().setAll(tableWrapper);
            VBox.setVgrow(tableWrapper, Priority.ALWAYS);
        } else {
            holidayTable.setItems(FXCollections.observableArrayList(j2025));
        }

        yearGroup.selectToggle(btnYear2025);

        contentBox.setVisible(true);
        contentBox.setManaged(true);
    }

    private static VBox wrapHolidayTable(TableView<JourFerieApi> table) {
        VBox box = new VBox(table);
        box.setPadding(new Insets(8, 12, 12, 12));
        VBox.setVgrow(table, Priority.ALWAYS);
        table.setMinHeight(0);
        return box;
    }

    private TableView<JourFerieApi> buildHolidayTable(List<JourFerieApi> rows) {
        TableView<JourFerieApi> table = new TableView<>();
        table.getStyleClass().add("table-view");
        table.setItems(FXCollections.observableArrayList(rows));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");

        TableColumn<JourFerieApi, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        LocalDate.parse(data.getValue().getDate()).format(formatter)));
        colDate.setMinWidth(140);

        TableColumn<JourFerieApi, String> colNomAr = new TableColumn<>("Nom (arabe)");
        colNomAr.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getLocalName()));
        colNomAr.setMinWidth(180);

        TableColumn<JourFerieApi, String> colNomEn = new TableColumn<>("Nom (anglais)");
        colNomEn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getName()));
        colNomEn.setMinWidth(160);

        TableColumn<JourFerieApi, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(data -> {
            List<String> types = data.getValue().getTypes();
            String typeStr = (types != null && !types.isEmpty()) ? types.get(0) : "Public";
            return new javafx.beans.property.SimpleStringProperty(typeStr);
        });
        colType.setMinWidth(90);

        table.getColumns().addAll(colDate, colNomAr, colNomEn, colType);
        return table;
    }

    @FXML
    private void retourDemandeTransport() {
        SceneNavigator.parentShellNavigateDemandeTransport();
    }
}
