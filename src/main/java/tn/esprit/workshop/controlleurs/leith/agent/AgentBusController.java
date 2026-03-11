package tn.esprit.workshop.controlleurs.leith.agent;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import tn.esprit.workshop.model.leith.Bus;
import tn.esprit.workshop.services.leith.BusService;

import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AgentBusController implements Initializable {

    private static final Logger LOG = Logger.getLogger(AgentBusController.class.getName());

    @FXML private TableView<Bus> table;
    @FXML private TableColumn<Bus, Number> colId;
    @FXML private TableColumn<Bus, String> colNumero;
    @FXML private TableColumn<Bus, String> colMatricule;
    @FXML private TableColumn<Bus, Number> colCapacite;
    @FXML private TableColumn<Bus, Number> colChauffeur;
    @FXML private TableColumn<Bus, Boolean> colActif;
    @FXML private Button btnModifier;
    @FXML private Button btnSupprimer;
    @FXML private Label lblMessage;

    private final BusService busService = new BusService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getBusId()));
        colNumero.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNumeroBus()));
        colMatricule.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getMatricule()));
        colCapacite.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getCapacite()));
        colChauffeur.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getIdChauffeur()));
        colActif.setCellValueFactory(c -> new javafx.beans.property.SimpleBooleanProperty(c.getValue().isActif()));

        table.getSelectionModel().selectedItemProperty().addListener((o, old, sel) -> {
            boolean has = sel != null;
            btnModifier.setDisable(!has);
            btnSupprimer.setDisable(!has);
        });

        load();
    }

    private void load() {
        try {
            table.getItems().setAll(busService.selectAll());
            lblMessage.setText("");
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "load bus", e);
            lblMessage.setText("Erreur: " + e.getMessage());
        }
    }

    @FXML
    void ajouter() {
        Dialog<Bus> d = dialogBus(null);
        d.showAndWait().ifPresent(bus -> {
            try {
                busService.insertOne(bus);
                new Alert(Alert.AlertType.INFORMATION, "✅ Bus ajouté.").showAndWait();
                load();
            } catch (SQLException e) {
                new Alert(Alert.AlertType.ERROR, "❌ Erreur: " + e.getMessage()).showAndWait();
            }
        });
    }

    @FXML
    void modifier() {
        Bus sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) {
            new Alert(Alert.AlertType.WARNING, "Sélectionnez un bus.").showAndWait();
            return;
        }
        Dialog<Bus> d = dialogBus(sel);
        d.showAndWait().ifPresent(bus -> {
            try {
                bus.setBusId(sel.getBusId());
                busService.updateOne(bus);
                new Alert(Alert.AlertType.INFORMATION, "✅ Bus modifié.").showAndWait();
                load();
            } catch (SQLException e) {
                new Alert(Alert.AlertType.ERROR, "❌ Erreur: " + e.getMessage()).showAndWait();
            }
        });
    }

    @FXML
    void supprimer() {
        Bus sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) {
            new Alert(Alert.AlertType.WARNING, "Sélectionnez un bus.").showAndWait();
            return;
        }
        Optional<ButtonType> r = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer ce bus ? Les trajets seront désaffectés.").showAndWait();
        if (r.isEmpty() || r.get() != ButtonType.OK) return;
        try {
            busService.deleteOne(sel);
            new Alert(Alert.AlertType.INFORMATION, "✅ Bus supprimé.").showAndWait();
            load();
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Erreur: " + e.getMessage()).showAndWait();
        }
    }

    private Dialog<Bus> dialogBus(Bus existing) {
        Dialog<Bus> d = new Dialog<>();
        d.setTitle(existing == null ? "Nouveau bus" : "Modifier bus");
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField tfNumero = new TextField(existing != null ? existing.getNumeroBus() : "");
        TextField tfMatricule = new TextField(existing != null ? existing.getMatricule() : "");
        Spinner<Integer> spCapacite = new Spinner<>(1, 100, existing != null ? existing.getCapacite() : 30);
        TextField tfChauffeur = new TextField(existing != null && existing.getIdChauffeur() != 0 ? String.valueOf(existing.getIdChauffeur()) : "");
        CheckBox chkActif = new CheckBox("Actif");
        chkActif.setSelected(existing == null || existing.isActif());

        javafx.scene.layout.GridPane gp = new javafx.scene.layout.GridPane();
        gp.setHgap(8); gp.setVgap(8);
        gp.add(new Label("Numéro bus:"), 0, 0); gp.add(tfNumero, 1, 0);
        gp.add(new Label("Matricule:"), 0, 1); gp.add(tfMatricule, 1, 1);
        gp.add(new Label("Capacité:"), 0, 2); gp.add(spCapacite, 1, 2);
        gp.add(new Label("Id Chauffeur (0=vide):"), 0, 3); gp.add(tfChauffeur, 1, 3);
        gp.add(chkActif, 1, 4);
        d.getDialogPane().setContent(gp);

        d.setResultConverter(bt -> {
            if (bt != ButtonType.OK) return null;
            String numero = tfNumero.getText() != null ? tfNumero.getText().trim() : "";
            String matricule = tfMatricule.getText() != null ? tfMatricule.getText().trim() : "";
            if (numero.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "❌ Numéro bus obligatoire.").showAndWait();
                return null;
            }
            if (matricule.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "❌ Matricule obligatoire.").showAndWait();
                return null;
            }
            int cap = spCapacite.getValue();
            if (cap < 1 || cap > 100) {
                new Alert(Alert.AlertType.WARNING, "❌ Capacité doit être entre 1 et 100.").showAndWait();
                return null;
            }
            Bus b = new Bus();
            b.setNumeroBus(numero);
            b.setMatricule(matricule);
            b.setCapacite(cap);
            try {
                b.setIdChauffeur(tfChauffeur.getText().trim().isEmpty() ? 0 : Integer.parseInt(tfChauffeur.getText().trim()));
            } catch (NumberFormatException e) {
                b.setIdChauffeur(0);
            }
            b.setActif(chkActif.isSelected());
            return b;
        });
        return d;
    }
}
