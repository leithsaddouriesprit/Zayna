package tn.esprit.workshop.controlleurs.leith.parent;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.*;
import tn.esprit.workshop.controlleurs.leith.SceneNavigator;
import tn.esprit.workshop.model.tous.Ecole;
import tn.esprit.workshop.services.leith.CandidatureEnfantService;
import tn.esprit.workshop.services.leith.EcoleService;
import tn.esprit.workshop.utilis.AppSession;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ParentDemandeTransportController implements Initializable {

    private static final Logger LOG = Logger.getLogger(ParentDemandeTransportController.class.getName());

    @FXML private ComboBox<Ecole> comboEcole;
    @FXML private TextField tfNom;
    @FXML private TextField tfPrenom;
    @FXML private Spinner<Integer> spAge;
    @FXML private TextField tfLat;
    @FXML private TextField tfLng;
    @FXML private Label lblMessage;

    private final EcoleService ecoleService = new EcoleService();
    private final CandidatureEnfantService candidatureEnfantService = new CandidatureEnfantService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        comboEcole.setPromptText("Choisir une école");
        comboEcole.setConverter(new javafx.util.StringConverter<Ecole>() {
            @Override
            public String toString(Ecole e) {
                return e == null ? "" : (e.getNomEcole() != null ? e.getNomEcole() : ("École #" + e.getId())) + " (id=" + e.getId() + ")";
            }
            @Override
            public Ecole fromString(String s) { return null; }
        });
        SceneNavigator.applyAppCssToComboBoxPopup(comboEcole);
        try {
            comboEcole.getItems().setAll(ecoleService.selectAll());
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "load ecoles", e);
            lblMessage.setText("Erreur chargement écoles.");
        }
        spAge.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 18, 6));
    }

    @FXML
    void envoyer() {
        int parentId = AppSession.getInstance().getParentId();
        Ecole ecole = comboEcole.getSelectionModel().getSelectedItem();
        String nom = tfNom.getText() != null ? tfNom.getText().trim() : "";
        String prenom = tfPrenom.getText() != null ? tfPrenom.getText().trim() : "";
        if (ecole == null) {
            new Alert(Alert.AlertType.WARNING, "❌ Veuillez sélectionner une école.").showAndWait();
            return;
        }
        if (nom.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "❌ Nom enfant obligatoire.").showAndWait();
            return;
        }
        if (prenom.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "❌ Prénom enfant obligatoire.").showAndWait();
            return;
        }
        int age = spAge.getValue();
        if (age < 1 || age > 18) {
            new Alert(Alert.AlertType.WARNING, "❌ Âge doit être entre 1 et 18.").showAndWait();
            return;
        }
        if (tfLat.getText() == null || tfLat.getText().trim().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "❌ Latitude obligatoire.").showAndWait();
            return;
        }
        if (tfLng.getText() == null || tfLng.getText().trim().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "❌ Longitude obligatoire.").showAndWait();
            return;
        }
        double lat, lng;
        try {
            lat = Double.parseDouble(tfLat.getText().trim());
            lng = Double.parseDouble(tfLng.getText().trim());
        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.WARNING, "❌ Latitude et longitude doivent être des nombres.").showAndWait();
            return;
        }
        try {
            candidatureEnfantService.insert(parentId, ecole.getId(), nom, prenom, age, lat, lng);
            new Alert(Alert.AlertType.INFORMATION, "✅ Demande envoyée avec succès.").showAndWait();
            tfNom.clear();
            tfPrenom.clear();
            spAge.getValueFactory().setValue(6);
            tfLat.clear();
            tfLng.clear();
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "envoyer", e);
            new Alert(Alert.AlertType.ERROR, "❌ Erreur: " + e.getMessage()).showAndWait();
        }
    }
}
