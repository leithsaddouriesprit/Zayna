package tn.esprit.workshop.controlleurs.amal;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.workshop.controlleurs.leith.SceneNavigator;
import tn.esprit.workshop.model.amal.JourFerieApi;
import tn.esprit.workshop.model.amal.Meteo;
import tn.esprit.workshop.model.amal.Programme;
import tn.esprit.workshop.model.tous.Ecole;
import tn.esprit.workshop.services.amal.JourFerieApiService;
import tn.esprit.workshop.services.amal.MeteoService;
import tn.esprit.workshop.services.amal.ProgrammeService;
import tn.esprit.workshop.services.leith.EcoleService;
import tn.esprit.workshop.utilis.AppSession;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ParentController {

    @FXML private ListView<Ecole> listViewEcoles;
    @FXML private VBox detailsContainer;
    @FXML private VBox messageContainer;
    @FXML private VBox programmesContainer;
    @FXML private Label lblNomEcole;
    @FXML private Label lblPositionEcole;
    @FXML private Label lblPrixEcole;
    @FXML private Label lblDescriptionEcole;
    @FXML private Label lblInfosEcole;
    @FXML private FlowPane programmesFlowPane;
    @FXML private Button btnInscrire;

    private final EcoleService ecoleService = new EcoleService();
    private final ProgrammeService programmeService = new ProgrammeService();
    private final JourFerieApiService jourFerieApiService = new JourFerieApiService();
    private final MeteoService meteoService = new MeteoService();
    private Ecole ecoleSelectionnee;

    @FXML
    public void initialize() {
        setupListView();
        loadEcoles();
        setupListeners();
    }

    private void setupListView() {
        listViewEcoles.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Ecole ecole, boolean empty) {
                super.updateItem(ecole, empty);
                if (empty || ecole == null) {
                    setText(null);
                    return;
                }
                setText(ecole.getNomEcole());
            }
        });
    }

    private void loadEcoles() {
        try {
            listViewEcoles.setItems(FXCollections.observableArrayList(ecoleService.selectAll()));
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les ecoles: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void setupListeners() {
        listViewEcoles.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected == null) return;
            ecoleSelectionnee = selected;
            afficherDetailsEcole(selected);
            chargerProgrammes(selected.getId());
            detailsContainer.setVisible(true);
            detailsContainer.setManaged(true);
            programmesContainer.setVisible(true);
            programmesContainer.setManaged(true);
            messageContainer.setVisible(false);
            messageContainer.setManaged(false);
        });
    }

    private void afficherDetailsEcole(Ecole ecole) {
        lblNomEcole.setText(ecole.getNomEcole() != null ? ecole.getNomEcole() : "");

        String adresse = ecole.getAdresse();
        boolean hasAdresse = adresse != null && !adresse.isBlank();
        lblPositionEcole.setText(hasAdresse ? adresse : "");
        lblPositionEcole.setVisible(hasAdresse);
        lblPositionEcole.setManaged(hasAdresse);

        lblPrixEcole.setText("Pour consulter les lignes et les arrêts, utilisez le bouton « Trajets ».");
        lblPrixEcole.setVisible(true);
        lblPrixEcole.setManaged(true);

        lblDescriptionEcole.setText("Les programmes proposés par l'établissement sont listés ci-dessous.");
        lblDescriptionEcole.setVisible(true);
        lblDescriptionEcole.setManaged(true);

        lblInfosEcole.setText("");
        lblInfosEcole.setVisible(false);
        lblInfosEcole.setManaged(false);
    }

    private void chargerProgrammes(int ecoleId) {
        programmesFlowPane.getChildren().clear();
        try {
            List<Programme> programmes = programmeService.selectProgrammesByEcoleId(ecoleId);
            if (programmes.isEmpty()) {
                Label empty = new Label("Aucun programme pour cette ecole.");
                empty.getStyleClass().add("section-subtitle");
                programmesFlowPane.getChildren().add(empty);
                return;
            }
            for (Programme p : programmes) {
                VBox card = new VBox(6);
                card.getStyleClass().addAll("card-dark", "amal-program-card");
                Label nom = new Label(p.getNomProgramme());
                nom.getStyleClass().add("card-title");
                Label niveau = new Label("Niveau: " + (p.getNiveau() == null ? "-" : p.getNiveau()));
                Label duree = new Label("Duree: " + p.getDuree() + " mois");
                niveaux(niveau, duree);
                card.getChildren().addAll(nom, niveau, duree);
                programmesFlowPane.getChildren().add(card);
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les programmes: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void niveaux(Label... labels) {
        for (Label label : labels) {
            label.getStyleClass().add("section-subtitle");
        }
    }

    @FXML
    private void inscrireEnfant() {
        Ecole selected = ecoleSelectionnee != null ? ecoleSelectionnee : listViewEcoles.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Information", "Veuillez selectionner une ecole.", Alert.AlertType.WARNING);
            return;
        }
        AppSession.getInstance().setSelectedEcoleId(selected.getId());
        AppSession.getInstance().setSelectedEcoleName(selected.getNomEcole());
        SceneNavigator.openParentCandidatureTransport();
    }

    @FXML
    private void voirTrajets() {
        Ecole selected = ecoleSelectionnee != null ? ecoleSelectionnee : listViewEcoles.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Information", "Veuillez selectionner une ecole.", Alert.AlertType.WARNING);
            return;
        }
        AppSession.getInstance().setSelectedEcoleId(selected.getId());
        AppSession.getInstance().setSelectedEcoleName(selected.getNomEcole());
        SceneNavigator.openParentTrajetsView();
    }

    @FXML
    private void voirCalendrier() {
        try {
            int yCur = LocalDate.now().getYear();
            List<JourFerieApi> y1 = jourFerieApiService.getJoursFeries(yCur);
            List<JourFerieApi> y2 = jourFerieApiService.getJoursFeries(yCur + 1);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Calendrier transport — Tunisie");

            TableView<JourFerieApi> table = buildCalendarTable();
            table.getStyleClass().add("amal-holiday-table");
            VBox.setVgrow(table, Priority.ALWAYS);
            table.setMinHeight(320);
            table.getItems().setAll(y1);

            Button btnYearA = new Button(String.valueOf(yCur));
            Button btnYearB = new Button(String.valueOf(yCur + 1));
            btnYearA.setOnAction(e -> {
                table.getItems().setAll(y1);
                styleYearToggle(btnYearA, true);
                styleYearToggle(btnYearB, false);
            });
            btnYearB.setOnAction(e -> {
                table.getItems().setAll(y2);
                styleYearToggle(btnYearA, false);
                styleYearToggle(btnYearB, true);
            });
            styleYearToggle(btnYearA, true);
            styleYearToggle(btnYearB, false);

            HBox yearBar = new HBox(10, btnYearA, btnYearB);
            yearBar.setAlignment(Pos.CENTER_LEFT);
            yearBar.getStyleClass().add("amal-modal-year-bar");

            Label title = new Label("Jours fériés");
            title.getStyleClass().addAll("admin-page-title");
            Label subtitle = new Label("Calendrier national — basculez entre les années pour planifier les déplacements.");
            subtitle.getStyleClass().addAll("admin-page-subtitle", "text-muted");
            subtitle.setWrapText(true);

            VBox header = new VBox(8, title, subtitle);
            VBox tableWrap = new VBox();
            tableWrap.getStyleClass().add("amal-modal-table-wrap");
            tableWrap.getChildren().add(table);

            VBox.setVgrow(tableWrap, Priority.ALWAYS);

            VBox root = new VBox(16, header, yearBar, tableWrap);
            root.getStyleClass().addAll("content-area", "parent-page", "amal-page", "amal-modal-shell", "amal-modal-calendar");
            root.setPadding(new Insets(20, 22, 22, 22));

            javafx.scene.Scene scene = new javafx.scene.Scene(root, 840, 560);
            applyModalStylesheets(scene);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (Exception e) {
            showAlert("Calendrier transport", "Le calendrier transport n'est pas encore disponible dans cette version.", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    private void voirMeteo() {
        Ecole selected = ecoleSelectionnee != null ? ecoleSelectionnee : listViewEcoles.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Information", "Veuillez selectionner une ecole.", Alert.AlertType.WARNING);
            return;
        }
        try {
            Meteo meteo;
            Double lat = selected.getLatitude();
            Double lon = selected.getLongitude();
            if (lat != null && lon != null && !lat.isNaN() && !lon.isNaN()) {
                meteo = meteoService.getMeteoByCoordinates(lat, lon);
            } else {
                String ville = "Tunis";
                meteo = meteoService.getMeteo(ville);
            }
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Météo — " + meteo.getVille());

            String schoolLine = selected.getNomEcole() != null && !selected.getNomEcole().isBlank()
                    ? selected.getNomEcole()
                    : null;
            Label lblSchool = schoolLine != null ? new Label("Établissement : " + schoolLine) : null;
            if (lblSchool != null) {
                lblSchool.getStyleClass().addAll("section-subtitle", "text-muted");
                lblSchool.setWrapText(true);
            }

            Label lblCity = new Label(meteo.getVille());
            lblCity.getStyleClass().add("admin-page-title");

            String desc = meteo.getDescription() != null ? meteo.getDescription() : "";
            if (!desc.isEmpty()) {
                desc = desc.substring(0, 1).toUpperCase() + desc.substring(1);
            }
            Label lblDesc = new Label(desc);
            lblDesc.getStyleClass().add("subtitle-gold");
            lblDesc.setWrapText(true);

            Label lblTemp = new Label(String.format("%.0f°", meteo.getTemperature()));
            lblTemp.getStyleClass().add("card-value");

            String iconCode = meteo.getIcone() != null && !meteo.getIcone().isBlank() ? meteo.getIcone() : "01d";
            ImageView icon = new ImageView(new Image(
                    "https://openweathermap.org/img/wn/" + iconCode + "@2x.png",
                    true));
            icon.setFitHeight(88);
            icon.setFitWidth(88);
            icon.setPreserveRatio(true);
            icon.getStyleClass().add("amal-meteo-icon");

            VBox textCol = new VBox(6, lblCity, lblDesc, lblTemp);
            textCol.setAlignment(Pos.CENTER_LEFT);

            HBox hero = new HBox(24, icon, textCol);
            hero.setAlignment(Pos.CENTER_LEFT);
            hero.getStyleClass().add("amal-meteo-hero");

            GridPane grid = new GridPane();
            grid.getStyleClass().add("amal-meteo-details-grid");
            grid.setHgap(28);
            grid.setVgap(14);

            int row = 0;
            addMeteoStat(grid, row++, "Ressenti", String.format("%.1f °C", meteo.getRessenti()));
            addMeteoStat(grid, row++, "Humidité", meteo.getHumidite() + " %");
            addMeteoStat(grid, row++, "Vent", String.format("%.1f km/h", meteo.getVent()));

            VBox rootChildren = new VBox(12);
            if (lblSchool != null) {
                rootChildren.getChildren().add(lblSchool);
            }
            VBox meteoCard = new VBox(20, hero, grid);
            meteoCard.getStyleClass().add("surface-panel-soft");
            rootChildren.getChildren().add(meteoCard);

            VBox root = new VBox(16);
            root.getStyleClass().addAll("content-area", "parent-page", "amal-page", "amal-modal-shell", "amal-modal-meteo");
            root.setPadding(new Insets(22, 24, 26, 24));
            root.getChildren().add(rootChildren);

            javafx.scene.Scene scene = new javafx.scene.Scene(root, 460, 400);
            applyModalStylesheets(scene);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (Exception e) {
            showAlert("Meteo transport", "La meteo n'est pas disponible pour le moment. Reessayez plus tard.", Alert.AlertType.INFORMATION);
        }
    }

    private TableView<JourFerieApi> buildCalendarTable() {
        TableView<JourFerieApi> table = new TableView<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        TableColumn<JourFerieApi, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(LocalDate.parse(data.getValue().getDate()).format(formatter)));
        colDate.setPrefWidth(128);
        colDate.setMinWidth(110);
        TableColumn<JourFerieApi, String> colNom = new TableColumn<>("Jour férié");
        colNom.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getLocalName()));
        colNom.setPrefWidth(420);
        colNom.setMinWidth(200);
        TableColumn<JourFerieApi, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(data -> {
            List<String> types = data.getValue().getTypes();
            return new javafx.beans.property.SimpleStringProperty(types != null && !types.isEmpty() ? types.get(0) : "Public");
        });
        colType.setPrefWidth(140);
        colType.setMinWidth(100);
        table.getColumns().add(colDate);
        table.getColumns().add(colNom);
        table.getColumns().add(colType);
        return table;
    }

    private void applyModalStylesheets(javafx.scene.Scene scene) {
        String appCss = SceneNavigator.getAppCssUrl();
        if (appCss != null && !scene.getStylesheets().contains(appCss)) {
            scene.getStylesheets().add(appCss);
        }
        URL global = ParentController.class.getResource("/leith/design/global.css");
        if (global != null) {
            String u = global.toExternalForm();
            if (!scene.getStylesheets().contains(u)) {
                scene.getStylesheets().add(u);
            }
        }
        URL amal = ParentController.class.getResource("/amal/amal-integration.css");
        if (amal != null) {
            String u = amal.toExternalForm();
            if (!scene.getStylesheets().contains(u)) {
                scene.getStylesheets().add(u);
            }
        }
    }

    private static void styleYearToggle(Button btn, boolean selected) {
        btn.getStyleClass().removeAll("btn-primary", "btn-secondary", "btn-soft");
        btn.getStyleClass().add(selected ? "btn-primary" : "btn-soft");
    }

    private static void addMeteoStat(GridPane grid, int row, String labelText, String valueText) {
        Label l = new Label(labelText);
        l.getStyleClass().add("amal-meteo-stat-label");
        Label v = new Label(valueText);
        v.getStyleClass().add("amal-meteo-stat-value");
        v.setWrapText(true);
        grid.add(l, 0, row);
        grid.add(v, 1, row);
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}