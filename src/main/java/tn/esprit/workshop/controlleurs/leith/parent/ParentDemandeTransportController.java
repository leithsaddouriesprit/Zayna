package tn.esprit.workshop.controlleurs.leith.parent;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import tn.esprit.workshop.controlleurs.leith.SceneNavigator;
import tn.esprit.workshop.model.leith.Trajet;
import tn.esprit.workshop.model.tous.Ecole;
import tn.esprit.workshop.services.leith.CandidatureEnfantService;
import tn.esprit.workshop.services.leith.EcoleService;
import tn.esprit.workshop.services.leith.TrajetService;
import tn.esprit.workshop.utilis.AppSession;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ParentDemandeTransportController implements Initializable {

    private static final Logger LOG = Logger.getLogger(ParentDemandeTransportController.class.getName());

    @FXML private ComboBox<Ecole> comboEcole;
    @FXML private ComboBox<Trajet> comboTrajet;
    @FXML private TextField tfNom;
    @FXML private TextField tfPrenom;
    @FXML private Spinner<Integer> spAge;
    @FXML private TextField tfLat;
    @FXML private TextField tfLng;
    @FXML private Label lblMessage;

    private final EcoleService ecoleService = new EcoleService();
    private final TrajetService trajetService = new TrajetService();
    private final CandidatureEnfantService candidatureEnfantService = new CandidatureEnfantService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        comboEcole.setPromptText("Choisir une école");
        comboEcole.setConverter(new StringConverter<>() {
            @Override
            public String toString(Ecole e) {
                if (e == null) return "";
                return e.getNomEcole() != null && !e.getNomEcole().isBlank() ? e.getNomEcole() : "École";
            }
            @Override
            public Ecole fromString(String s) { return null; }
        });
        SceneNavigator.applyAppCssToComboBoxPopup(comboEcole);

        comboTrajet.setConverter(new StringConverter<>() {
            @Override
            public String toString(Trajet t) {
                if (t == null) return "";
                String h = t.getHeureDepart() != null ? t.getHeureDepart().toString() : "—";
                return t.getNom() + " — départ " + h;
            }
            @Override
            public Trajet fromString(String s) { return null; }
        });
        SceneNavigator.applyAppCssToComboBoxPopup(comboTrajet);

        try {
            comboEcole.getItems().setAll(ecoleService.selectAll());
            Integer selectedEcoleId = AppSession.getInstance().getSelectedEcoleId();
            if (selectedEcoleId != null) {
                for (Ecole ecole : comboEcole.getItems()) {
                    if (ecole.getId() == selectedEcoleId) {
                        comboEcole.getSelectionModel().select(ecole);
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "load ecoles", e);
            lblMessage.setText("Erreur chargement écoles.");
        }

        comboEcole.getSelectionModel().selectedItemProperty().addListener((obs, oldE, newE) -> reloadTrajets(newE));
        reloadTrajets(comboEcole.getSelectionModel().getSelectedItem());

        spAge.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 18, 6));
    }

    private void reloadTrajets(Ecole ecole) {
        comboTrajet.getItems().clear();
        comboTrajet.getSelectionModel().clearSelection();
        if (ecole == null) {
            return;
        }
        try {
            comboTrajet.getItems().setAll(trajetService.selectByEcoleId(ecole.getId()));
            if (comboTrajet.getItems().isEmpty()) {
                lblMessage.setText("Aucun trajet proposé pour cet établissement pour le moment.");
            } else if (lblMessage != null && lblMessage.getText() != null
                    && lblMessage.getText().startsWith("Aucun trajet proposé")) {
                lblMessage.setText("");
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "load trajets", e);
            lblMessage.setText("Impossible de charger les trajets.");
        }
    }

    /** Retour au choix d'établissement (shell parent) sans perdre la présélection en session. */
    @FXML
    void retourChoixEcole() {
        SceneNavigator.openParentDemandeTransport();
    }

    @FXML
    void envoyer() {
        int parentId = AppSession.getInstance().getParentId();
        Ecole ecole = comboEcole.getSelectionModel().getSelectedItem();
        Trajet trajet = comboTrajet.getSelectionModel().getSelectedItem();
        String nom = tfNom.getText() != null ? tfNom.getText().trim() : "";
        String prenom = tfPrenom.getText() != null ? tfPrenom.getText().trim() : "";
        if (ecole == null) {
            new Alert(Alert.AlertType.WARNING, "Veuillez sélectionner une école.").showAndWait();
            return;
        }
        if (trajet == null) {
            new Alert(Alert.AlertType.WARNING, "Veuillez sélectionner un trajet pour cet établissement.").showAndWait();
            return;
        }
        if (nom.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Nom enfant obligatoire.").showAndWait();
            return;
        }
        if (prenom.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Prénom enfant obligatoire.").showAndWait();
            return;
        }
        int age = spAge.getValue();
        if (age < 1 || age > 18) {
            new Alert(Alert.AlertType.WARNING, "L'âge doit être entre 1 et 18.").showAndWait();
            return;
        }
        if (tfLat.getText() == null || tfLat.getText().trim().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Latitude obligatoire.").showAndWait();
            return;
        }
        if (tfLng.getText() == null || tfLng.getText().trim().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Longitude obligatoire.").showAndWait();
            return;
        }
        double lat, lng;
        try {
            lat = Double.parseDouble(tfLat.getText().trim());
            lng = Double.parseDouble(tfLng.getText().trim());
        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.WARNING, "Latitude et longitude doivent être des nombres.").showAndWait();
            return;
        }
        try {
            candidatureEnfantService.insert(parentId, ecole.getId(), nom, prenom, age, lat, lng, trajet.getTrajetId());
            new Alert(Alert.AlertType.INFORMATION, "Demande envoyée avec succès.").showAndWait();
            tfNom.clear();
            tfPrenom.clear();
            spAge.getValueFactory().setValue(6);
            tfLat.clear();
            tfLng.clear();
            comboTrajet.getSelectionModel().clearSelection();
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "envoyer", e);
            new Alert(Alert.AlertType.ERROR, "Erreur: " + e.getMessage()).showAndWait();
        }
    }
}
