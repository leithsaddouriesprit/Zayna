package tn.esprit.workshop.controlleurs.leith.agent;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import tn.esprit.workshop.model.leith.Bus;
import tn.esprit.workshop.controlleurs.leith.SceneNavigator;
import tn.esprit.workshop.model.leith.Candidature;
import tn.esprit.workshop.services.leith.BusService;
import tn.esprit.workshop.services.leith.CandidatureService;
import tn.esprit.workshop.utilis.AppSession;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AgentAffecterChauffeurBusController implements Initializable {

    private static final Logger LOG = Logger.getLogger(AgentAffecterChauffeurBusController.class.getName());

    @FXML private ComboBox<Bus> comboBus;
    @FXML private ComboBox<Candidature> comboChauffeur;
    @FXML private Label lblMessage;

    private final BusService busService = new BusService();
    private final CandidatureService candidatureService = new CandidatureService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        comboBus.setPromptText("Choisir un bus");
        comboChauffeur.setPromptText("Choisir un chauffeur");
        comboBus.setConverter(new javafx.util.StringConverter<Bus>() {
            @Override
            public String toString(Bus b) {
                if (b == null) return "";
                String num = (b.getNumeroBus() != null && !b.getNumeroBus().isEmpty()) ? b.getNumeroBus() : ("Bus #" + b.getBusId());
                String mat = (b.getMatricule() != null && !b.getMatricule().isEmpty()) ? " - " + b.getMatricule() : "";
                if (b.getIdChauffeur() != 0) {
                    return num + mat + " (chauffeur: " + b.getIdChauffeur() + ")";
                }
                return num + mat;
            }
            @Override
            public Bus fromString(String s) { return null; }
        });
        comboChauffeur.setConverter(new javafx.util.StringConverter<Candidature>() {
            @Override
            public String toString(Candidature c) {
                if (c == null) return "";
                String nom = (c.getNom() != null) ? c.getNom() : "";
                String prenom = (c.getPrenom() != null) ? c.getPrenom() : "";
                if (nom.isEmpty() && prenom.isEmpty()) return "Chauffeur #" + c.getChauffeurId();
                return (prenom + " " + nom).trim() + " (id=" + c.getChauffeurId() + ")";
            }
            @Override
            public Candidature fromString(String s) { return null; }
        });
        SceneNavigator.applyAppCssToComboBoxPopup(comboBus);
        SceneNavigator.applyAppCssToComboBoxPopup(comboChauffeur);
        load();
    }

    private void load() {
        Integer idEcole = AppSession.getInstance().getEcoleId();
        if (idEcole == null) {
            lblMessage.setText("Aucune école en session.");
            return;
        }
        try {
            comboBus.getItems().setAll(busService.selectActifs(idEcole));
            comboChauffeur.getItems().setAll(candidatureService.findAllAccepteesByEcoleId(idEcole));
            comboBus.getSelectionModel().clearSelection();
            comboChauffeur.getSelectionModel().clearSelection();
            lblMessage.setText("");
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "load", e);
            lblMessage.setText("Erreur: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les données.");
        }
    }

    @FXML
    void enregistrer() {
        Bus bus = comboBus.getSelectionModel().getSelectedItem();
        Candidature cand = comboChauffeur.getSelectionModel().getSelectedItem();
        if (bus == null || cand == null) {
            showAlert(Alert.AlertType.ERROR, "Sélection manquante", "Veuillez sélectionner un bus et un chauffeur.");
            return;
        }
        int chauffeurId = cand.getChauffeurId();
        try {
            Bus alreadyAssigned = busService.getByChauffeurId(chauffeurId);
            if (alreadyAssigned != null && alreadyAssigned.getBusId() != bus.getBusId()) {
                showAlert(Alert.AlertType.ERROR, "Chauffeur déjà affecté",
                        "Ce chauffeur est déjà affecté au bus " + (alreadyAssigned.getNumeroBus() != null ? alreadyAssigned.getNumeroBus() : ("#" + alreadyAssigned.getBusId())) + ". Un chauffeur ne peut être affecté qu'à un seul bus actif.");
                return;
            }
            int currentChauffeur = bus.getIdChauffeur();
            if (currentChauffeur != 0 && currentChauffeur != chauffeurId) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Bus déjà affecté");
                confirm.setHeaderText("Ce bus a déjà un chauffeur.");
                confirm.setContentText("Remplacer par le chauffeur sélectionné ?");
                if (confirm.showAndWait().orElse(null) != javafx.scene.control.ButtonType.OK) {
                    return;
                }
            }
            busService.updateIdChauffeur(bus.getBusId(), chauffeurId);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "✅ Affectation enregistrée.");
            load();
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "enregistrer", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'enregistrement: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(content);
        a.showAndWait();
    }
}
