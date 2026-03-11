package tn.esprit.workshop.controlleurs.leith;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import tn.esprit.workshop.model.leith.Arret;
import tn.esprit.workshop.model.leith.Bus;
import tn.esprit.workshop.model.leith.Trajet;
import tn.esprit.workshop.services.leith.ArretService;
import tn.esprit.workshop.services.leith.BusService;
import tn.esprit.workshop.services.leith.TrajetService;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Espace Chauffeur: bus info, active trajet, list of arrets. Réclamer = placeholder.
 */
public class EspaceChauffeurController {

    private static final Logger LOG = Logger.getLogger(EspaceChauffeurController.class.getName());

    @FXML private VBox boxNoBus;
    @FXML private VBox boxBusContent;
    @FXML private Label lblNumeroBus;
    @FXML private Label lblMatricule;
    @FXML private Label lblCapacite;
    @FXML private Label lblTrajetNom;
    @FXML private Label lblHeureDepart;
    @FXML private TableView<Arret> tableArrets;
    @FXML private TableColumn<Arret, Integer> colOrdre;
    @FXML private TableColumn<Arret, String> colNom;
    @FXML private TableColumn<Arret, String> colHeure;
    @FXML private TableColumn<Arret, Double> colLat;
    @FXML private TableColumn<Arret, Double> colLng;
    @FXML private javafx.scene.control.Button btnReclamer;

    private final BusService busService = new BusService();
    private final TrajetService trajetService = new TrajetService();
    private final ArretService arretService = new ArretService();

    public void init(Integer chauffeurId) {
        if (chauffeurId == null) return;
        colOrdre.setCellValueFactory(new PropertyValueFactory<>("ordre"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colHeure.setCellValueFactory(new PropertyValueFactory<>("heurePrevue"));
        colLat.setCellValueFactory(new PropertyValueFactory<>("latitude"));
        colLng.setCellValueFactory(new PropertyValueFactory<>("longitude"));

        try {
            Bus bus = busService.getByChauffeurId(chauffeurId);
            if (bus == null) {
                if (boxNoBus != null) {
                    boxNoBus.setVisible(true);
                    boxNoBus.setManaged(true);
                }
                if (boxBusContent != null) {
                    boxBusContent.setVisible(false);
                    boxBusContent.setManaged(false);
                }
                lblNumeroBus.setText("—");
                lblMatricule.setText("—");
                lblCapacite.setText("—");
                lblTrajetNom.setText("—");
                lblHeureDepart.setText("—");
                tableArrets.getItems().clear();
                return;
            }
            if (boxNoBus != null) {
                boxNoBus.setVisible(false);
                boxNoBus.setManaged(false);
            }
            if (boxBusContent != null) {
                boxBusContent.setVisible(true);
                boxBusContent.setManaged(true);
            }
            lblNumeroBus.setText("Numéro: " + (bus.getNumeroBus() != null ? bus.getNumeroBus() : "—"));
            lblMatricule.setText("Matricule: " + (bus.getMatricule() != null ? bus.getMatricule() : "—"));
            lblCapacite.setText("Capacité: " + bus.getCapacite());

            Trajet trajet = trajetService.getByBusId(bus.getBusId());
            if (trajet == null) {
                lblTrajetNom.setText("—");
                lblHeureDepart.setText("—");
                tableArrets.getItems().clear();
                return;
            }
            lblTrajetNom.setText("Nom: " + (trajet.getNom() != null ? trajet.getNom() : "—"));
            lblHeureDepart.setText("Heure départ: " + (trajet.getHeureDepart() != null ? trajet.getHeureDepart().format(DateTimeFormatter.ISO_LOCAL_TIME) : "—"));

            List<Arret> arrets = arretService.getByTrajetId(trajet.getTrajetId());
            tableArrets.getItems().setAll(arrets != null ? arrets : Collections.emptyList());
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "init EspaceChauffeur", e);
            lblNumeroBus.setText("Erreur: " + e.getMessage());
        }
    }

    @FXML
    void onReclamer() {
        // Placeholder: no backend logic required
        btnReclamer.getScene().getWindow();
    }
}
