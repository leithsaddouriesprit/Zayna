package tn.esprit.workshop.controlleurs.leith.agent;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import tn.esprit.workshop.controlleurs.leith.SceneNavigator;
import tn.esprit.workshop.model.leith.Arret;
import tn.esprit.workshop.services.leith.ArretService;
import tn.esprit.workshop.services.leith.TrajetService;
import tn.esprit.workshop.utilis.AppSession;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AgentArretsController implements Initializable {

    private static final Logger LOG = Logger.getLogger(AgentArretsController.class.getName());

    @FXML private ComboBox<tn.esprit.workshop.model.leith.Trajet> comboTrajet;
    @FXML private TableView<Arret> table;
    @FXML private TableColumn<Arret, Number> colOrdre;
    @FXML private TableColumn<Arret, String> colNom;
    @FXML private TableColumn<Arret, Number> colLat;
    @FXML private TableColumn<Arret, Number> colLng;
    @FXML private TableColumn<Arret, String> colHeure;
    @FXML private TableColumn<Arret, Void> colActions;
    @FXML private Button btnAjouter;
    @FXML private Button btnModifier;
    @FXML private Button btnSupprimer;
    @FXML private Label lblMessage;

    private final ArretService arretService = new ArretService();
    private final TrajetService trajetService = new TrajetService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        comboTrajet.setConverter(new javafx.util.StringConverter<tn.esprit.workshop.model.leith.Trajet>() {
            @Override
            public String toString(tn.esprit.workshop.model.leith.Trajet t) {
                return t == null ? "" : t.getNom() + " (id=" + t.getTrajetId() + ")";
            }
            @Override
            public tn.esprit.workshop.model.leith.Trajet fromString(String s) { return null; }
        });
        SceneNavigator.applyAppCssToComboBoxPopup(comboTrajet);

        colOrdre.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getOrdre()));
        colNom.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNom()));
        colLat.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getLatitude()));
        colLng.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getLongitude()));
        colHeure.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getHeurePrevue() != null ? c.getValue().getHeurePrevue() : ""));

        colActions.setCellFactory(tc -> new TableCell<>() {
            private final Button up = new Button("↑");
            private final Button down = new Button("↓");
            {
                up.setOnAction(e -> moveOrdre(getTableRow().getItem(), -1));
                down.setOnAction(e -> moveOrdre(getTableRow().getItem(), 1));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    setGraphic(new javafx.scene.layout.HBox(4, up, down));
                }
            }
        });

        table.getSelectionModel().selectedItemProperty().addListener((o, old, sel) -> {
            btnModifier.setDisable(sel == null);
            btnSupprimer.setDisable(sel == null);
        });

        Integer idEcole = AppSession.getInstance().getEcoleId();
        if (idEcole != null) {
            try {
                comboTrajet.getItems().setAll(trajetService.selectByEcoleId(idEcole));
            } catch (SQLException e) {
                LOG.log(Level.SEVERE, "load trajets", e);
            }
        }
    }

    @FXML
    void onTrajetSelected() {
        btnAjouter.setDisable(comboTrajet.getSelectionModel().getSelectedItem() == null);
        loadArrets();
    }

    private void loadArrets() {
        tn.esprit.workshop.model.leith.Trajet t = comboTrajet.getSelectionModel().getSelectedItem();
        if (t == null) return;
        try {
            List<Arret> list = arretService.getArretsByTrajetOrdered(t.getTrajetId());
            table.getItems().setAll(list);
            lblMessage.setText("");
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "loadArrets", e);
            lblMessage.setText("Erreur: " + e.getMessage());
        }
    }

    private void moveOrdre(Arret arret, int delta) {
        if (arret == null) return;
        List<Arret> list = table.getItems();
        int idx = list.indexOf(arret);
        int newIdx = idx + delta;
        if (newIdx < 0 || newIdx >= list.size()) return;
        Arret other = list.get(newIdx);
        int ordreA = arret.getOrdre();
        int ordreB = other.getOrdre();
        try {
            arretService.updateOrdre(arret.getArretId(), ordreB);
            arretService.updateOrdre(other.getArretId(), ordreA);
            arret.setOrdre(ordreB);
            other.setOrdre(ordreA);
            list.sort((a, b) -> Integer.compare(a.getOrdre(), b.getOrdre()));
            table.refresh();
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "❌ Erreur: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    void ajouter() {
        tn.esprit.workshop.model.leith.Trajet t = comboTrajet.getSelectionModel().getSelectedItem();
        if (t == null) {
            new Alert(Alert.AlertType.WARNING, "Sélectionnez d'abord un trajet.").showAndWait();
            return;
        }
        Dialog<Arret> d = new Dialog<>();
        d.setTitle("Nouvel arrêt");
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        TextField tfNom = new TextField();
        TextField tfLat = new TextField();
        TextField tfLng = new TextField();
        TextField tfHeure = new TextField("08:00");
        int nextOrdre = table.getItems().isEmpty() ? 1 : table.getItems().stream().mapToInt(Arret::getOrdre).max().orElse(0) + 1;
        javafx.scene.layout.GridPane gp = new javafx.scene.layout.GridPane();
        gp.setHgap(8); gp.setVgap(8);
        gp.add(new Label("Nom (obligatoire):"), 0, 0); gp.add(tfNom, 1, 0);
        gp.add(new Label("Latitude (obligatoire, ≠0):"), 0, 1); gp.add(tfLat, 1, 1);
        gp.add(new Label("Longitude (obligatoire, ≠0):"), 0, 2); gp.add(tfLng, 1, 2);
        gp.add(new Label("Heure prévue:"), 0, 3); gp.add(tfHeure, 1, 3);
        gp.add(new Label("Ordre (auto): " + nextOrdre), 0, 4);
        d.getDialogPane().setContent(gp);
        d.setResultConverter(bt -> {
            if (bt != ButtonType.OK) return null;
            String nom = tfNom.getText() != null ? tfNom.getText().trim() : "";
            if (nom.isEmpty()) {
                javafx.application.Platform.runLater(() -> new Alert(Alert.AlertType.WARNING, "Nom obligatoire.").showAndWait());
                return null;
            }
            double lat, lng;
            try {
                lat = Double.parseDouble(tfLat.getText().trim());
                lng = Double.parseDouble(tfLng.getText().trim());
            } catch (NumberFormatException e) {
                javafx.application.Platform.runLater(() -> new Alert(Alert.AlertType.WARNING, "Latitude et longitude doivent être des nombres.").showAndWait());
                return null;
            }
            if (lat == 0.0 || lng == 0.0) {
                javafx.application.Platform.runLater(() -> new Alert(Alert.AlertType.WARNING, "Latitude et longitude doivent être différents de 0.").showAndWait());
                return null;
            }
            Arret a = new Arret();
            a.setIdTrajet(t.getTrajetId());
            a.setNom(nom);
            a.setLatitude(lat);
            a.setLongitude(lng);
            a.setOrdre(nextOrdre);
            a.setHeurePrevue(tfHeure.getText() != null && !tfHeure.getText().trim().isEmpty() ? tfHeure.getText().trim() : "08:00");
            return a;
        });
        d.showAndWait().ifPresent(a -> {
            try {
                arretService.insertOne(a);
                new Alert(Alert.AlertType.INFORMATION, "✅ Arrêt ajouté.").showAndWait();
                loadArrets();
            } catch (SQLException e) {
                new Alert(Alert.AlertType.ERROR, "❌ Erreur: " + e.getMessage()).showAndWait();
            }
        });
    }

    @FXML
    void modifier() {
        Arret sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        Dialog<Arret> d = new Dialog<>();
        d.setTitle("Modifier arrêt");
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        TextField tfNom = new TextField(sel.getNom());
        TextField tfLat = new TextField(String.valueOf(sel.getLatitude()));
        TextField tfLng = new TextField(String.valueOf(sel.getLongitude()));
        TextField tfHeure = new TextField(sel.getHeurePrevue() != null ? sel.getHeurePrevue() : "");
        Spinner<Integer> spOrdre = new Spinner<>(1, 999, sel.getOrdre());
        javafx.scene.layout.GridPane gp = new javafx.scene.layout.GridPane();
        gp.setHgap(8); gp.setVgap(8);
        gp.add(new Label("Nom:"), 0, 0); gp.add(tfNom, 1, 0);
        gp.add(new Label("Latitude:"), 0, 1); gp.add(tfLat, 1, 1);
        gp.add(new Label("Longitude:"), 0, 2); gp.add(tfLng, 1, 2);
        gp.add(new Label("Ordre:"), 0, 3); gp.add(spOrdre, 1, 3);
        gp.add(new Label("Heure prévue:"), 0, 4); gp.add(tfHeure, 1, 4);
        d.getDialogPane().setContent(gp);
        d.setResultConverter(bt -> {
            if (bt != ButtonType.OK) return null;
            sel.setNom(tfNom.getText().trim());
            try { sel.setLatitude(Double.parseDouble(tfLat.getText())); } catch (Exception e) { sel.setLatitude(0); }
            try { sel.setLongitude(Double.parseDouble(tfLng.getText())); } catch (Exception e) { sel.setLongitude(0); }
            sel.setOrdre(spOrdre.getValue());
            sel.setHeurePrevue(tfHeure.getText().trim());
            return sel;
        });
        d.showAndWait().ifPresent(a -> {
            try {
                arretService.updateOne(a);
                new Alert(Alert.AlertType.INFORMATION, "✅ Arrêt modifié.").showAndWait();
                loadArrets();
            } catch (SQLException e) {
                new Alert(Alert.AlertType.ERROR, "❌ Erreur: " + e.getMessage()).showAndWait();
            }
        });
    }

    @FXML
    void supprimer() {
        Arret sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) {
            new Alert(Alert.AlertType.WARNING, "Sélectionnez un arrêt.").showAndWait();
            return;
        }
        new Alert(Alert.AlertType.CONFIRMATION, "Supprimer cet arrêt ?").showAndWait()
                .filter(r -> r == ButtonType.OK)
                .ifPresent(r -> {
                    try {
                        arretService.deleteOne(sel);
                        new Alert(Alert.AlertType.INFORMATION, "✅ Arrêt supprimé.").showAndWait();
                        loadArrets();
                    } catch (SQLException e) {
                        new Alert(Alert.AlertType.ERROR, "❌ Erreur: " + e.getMessage()).showAndWait();
                    }
                });
    }
}
