package tn.esprit.workshop.controlleurs.leith.agent;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import tn.esprit.workshop.model.leith.Bus;
import tn.esprit.workshop.controlleurs.leith.SceneNavigator;
import tn.esprit.workshop.model.leith.Trajet;
import tn.esprit.workshop.services.leith.BusService;
import tn.esprit.workshop.services.leith.TrajetService;
import tn.esprit.workshop.utilis.AppSession;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AgentAffecterBusTrajetController implements Initializable {

    private static final Logger LOG = Logger.getLogger(AgentAffecterBusTrajetController.class.getName());

    @FXML private ComboBox<Trajet> comboTrajet;
    @FXML private ComboBox<Bus> comboBus;
    @FXML private Label lblMessage;

    private final TrajetService trajetService = new TrajetService();
    private final BusService busService = new BusService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        comboTrajet.setConverter(new javafx.util.StringConverter<Trajet>() {
            @Override
            public String toString(Trajet t) {
                return t == null ? "" : t.getNom() + " (id=" + t.getTrajetId() + ")";
            }
            @Override
            public Trajet fromString(String s) { return null; }
        });
        comboBus.setConverter(new javafx.util.StringConverter<Bus>() {
            @Override
            public String toString(Bus b) {
                if (b == null) return "";
                String num = (b.getNumeroBus() != null && !b.getNumeroBus().isEmpty()) ? b.getNumeroBus() : ("Bus #" + b.getBusId());
                String mat = (b.getMatricule() != null && !b.getMatricule().isEmpty()) ? " - " + b.getMatricule() : "";
                return num + mat;
            }
            @Override
            public Bus fromString(String s) { return null; }
        });
        SceneNavigator.applyAppCssToComboBoxPopup(comboTrajet);
        SceneNavigator.applyAppCssToComboBoxPopup(comboBus);
        load();
    }

    private void load() {
        Integer idEcole = AppSession.getInstance().getEcoleId();
        if (idEcole == null) {
            lblMessage.setText("Aucune école en session.");
            return;
        }
        try {
            comboTrajet.getItems().setAll(trajetService.selectByEcoleId(idEcole));
            comboBus.getItems().setAll(busService.selectAll());
            lblMessage.setText("");
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "load", e);
            lblMessage.setText("Erreur: " + e.getMessage());
        }
    }

    @FXML
    void enregistrer() {
        Trajet trajet = comboTrajet.getSelectionModel().getSelectedItem();
        Bus bus = comboBus.getSelectionModel().getSelectedItem();
        if (trajet == null || bus == null) {
            new Alert(Alert.AlertType.WARNING, "Veuillez sélectionner un trajet et un bus.").showAndWait();
            return;
        }
        try {
            trajetService.updateIdBus(trajet.getTrajetId(), bus.getBusId());
            new Alert(Alert.AlertType.INFORMATION, "✅ Bus affecté au trajet.").showAndWait();
            load();
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "enregistrer", e);
            new Alert(Alert.AlertType.ERROR, "❌ Erreur: " + e.getMessage()).showAndWait();
        }
    }
}
