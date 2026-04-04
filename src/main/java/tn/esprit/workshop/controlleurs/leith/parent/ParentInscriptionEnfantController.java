package tn.esprit.workshop.controlleurs.leith.parent;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.function.UnaryOperator;
import javafx.scene.layout.Priority;
import tn.esprit.workshop.controlleurs.leith.SceneNavigator;
import tn.esprit.workshop.model.leith.Trajet;
import tn.esprit.workshop.model.tous.Ecole;
import tn.esprit.workshop.services.leith.CandidatureEnfantService;
import tn.esprit.workshop.services.leith.EcoleService;
import tn.esprit.workshop.services.leith.TrajetService;
import tn.esprit.workshop.utilis.AppSession;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Formulaire d’inscription enfant dans le shell Parent (pas de dialogue modal).
 */
public class ParentInscriptionEnfantController implements Initializable {

    private static final Logger LOG = Logger.getLogger(ParentInscriptionEnfantController.class.getName());

    @FXML private Label lblEcole;
    @FXML private TextField tfNom;
    @FXML private TextField tfPrenom;
    @FXML private TextField tfAge;
    @FXML private TextField tfLat;
    @FXML private TextField tfLng;
    @FXML private ComboBox<Trajet> comboTrajet;
    @FXML private Label lblMessage;
    @FXML private Button btnEnvoyer;
    @FXML private GridPane gridForm;

    private final EcoleService ecoleService = new EcoleService();
    private final TrajetService trajetService = new TrajetService();
    private final CandidatureEnfantService candidatureEnfantService = new CandidatureEnfantService();

    private int idEcole;
    private Ecole ecole;

    private static final int AGE_MIN = 3;
    private static final int AGE_MAX = 18;
    private static final String MSG_AGE_INVALID = "L'âge doit être un nombre entre 3 et 18";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupAgeTextField();
        SceneNavigator.applyAppCssToComboBoxPopup(comboTrajet);
        comboTrajet.setCellFactory(lv -> new ListCell<Trajet>() {
            @Override
            protected void updateItem(Trajet item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String h = item.getHeureDepart() != null ? item.getHeureDepart().toString() : "—";
                    setText((item.getNom() != null ? item.getNom() : ("#" + item.getTrajetId()))
                            + " — départ " + h + " — " + String.format("%.2f", item.getPrix()) + " DT");
                }
            }
        });
        comboTrajet.setButtonCell(new ListCell<Trajet>() {
            @Override
            protected void updateItem(Trajet item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNom() != null ? item.getNom() : ("Trajet #" + item.getTrajetId()));
                }
            }
        });

        Integer pending = AppSession.getInstance().getAndClearPendingInscriptionEcoleId();
        if (pending == null || pending <= 0) {
            Platform.runLater(SceneNavigator::parentShellNavigateDemandeTransport);
            return;
        }
        idEcole = pending;
        try {
            ecole = ecoleService.getById(idEcole);
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "load ecole", e);
            ecole = null;
        }
        if (ecole == null) {
            Platform.runLater(SceneNavigator::parentShellNavigateDemandeTransport);
            return;
        }

        lblEcole.setText("École : " + (ecole.getNomEcole() != null ? ecole.getNomEcole() : ("#" + idEcole)));

        try {
            List<Trajet> trajets = trajetService.selectByEcoleId(idEcole);
            comboTrajet.setItems(FXCollections.observableArrayList(trajets));
            comboTrajet.setPromptText("Choisir un trajet");
            if (trajets.isEmpty()) {
                comboTrajet.setDisable(true);
                btnEnvoyer.setDisable(true);
                showMessage("Aucun trajet n’est disponible pour cette école. Impossible d’envoyer une candidature.", true);
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "load trajets", e);
            comboTrajet.setDisable(true);
            btnEnvoyer.setDisable(true);
            showMessage("Impossible de charger les trajets.", true);
        }

        GridPane.setHgrow(tfNom, Priority.ALWAYS);
        GridPane.setHgrow(tfPrenom, Priority.ALWAYS);
        GridPane.setHgrow(tfAge, Priority.ALWAYS);
        GridPane.setHgrow(tfLat, Priority.ALWAYS);
        GridPane.setHgrow(tfLng, Priority.ALWAYS);
        GridPane.setHgrow(comboTrajet, Priority.ALWAYS);
    }

    /** Chiffres uniquement, au plus 2 caractères (3–18). */
    private void setupAgeTextField() {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String t = change.getControlNewText();
            if (t == null) {
                return null;
            }
            if (t.isEmpty()) {
                return change;
            }
            if (t.length() > 2 || !t.matches("\\d+")) {
                return null;
            }
            return change;
        };
        tfAge.setTextFormatter(new TextFormatter<>(filter));
    }

    /**
     * Lit et valide l’âge : obligatoire, entier, entre 3 et 18.
     *
     * @return l’âge si valide, sinon {@code null} (message d’erreur déjà affiché)
     */
    private Integer parseAndValidateAge() {
        String raw = tfAge.getText() != null ? tfAge.getText().trim() : "";
        if (raw.isEmpty()) {
            showMessage(MSG_AGE_INVALID, true);
            return null;
        }
        int age;
        try {
            age = Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            showMessage(MSG_AGE_INVALID, true);
            return null;
        }
        if (age < AGE_MIN || age > AGE_MAX) {
            showMessage(MSG_AGE_INVALID, true);
            return null;
        }
        return age;
    }

    @FXML
    private void retourDemandeTransport() {
        AppSession.getInstance().setPendingDemandeTransportEcoleRestoreId(idEcole);
        SceneNavigator.parentShellNavigateDemandeTransport();
    }

    @FXML
    private void envoyer() {
        hideMessage();
        int parentId = AppSession.getInstance().getParentId();
        if (parentId <= 0) {
            showMessage("Session parent invalide. Reconnectez-vous.", true);
            return;
        }

        String nom = tfNom.getText() != null ? tfNom.getText().trim() : "";
        String prenom = tfPrenom.getText() != null ? tfPrenom.getText().trim() : "";
        if (nom.isEmpty() || prenom.isEmpty()) {
            showMessage("Le nom et le prénom sont obligatoires.", true);
            return;
        }

        Integer ageBoxed = parseAndValidateAge();
        if (ageBoxed == null) {
            return;
        }
        int age = ageBoxed;
        if (tfLat.getText() == null || tfLat.getText().trim().isEmpty()
                || tfLng.getText() == null || tfLng.getText().trim().isEmpty()) {
            showMessage("La latitude et la longitude sont obligatoires.", true);
            return;
        }
        double lat;
        double lng;
        try {
            lat = Double.parseDouble(tfLat.getText().trim().replace(',', '.'));
            lng = Double.parseDouble(tfLng.getText().trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            showMessage("Coordonnées invalides (nombres attendus).", true);
            return;
        }

        Trajet t = comboTrajet.getSelectionModel().getSelectedItem();
        if (t == null || comboTrajet.isDisable()) {
            showMessage("Veuillez sélectionner un trajet de transport.", true);
            return;
        }

        try {
            candidatureEnfantService.insert(parentId, idEcole, nom, prenom, age, lat, lng, t.getTrajetId());
            resetFormAfterSuccess();
            showMessage("Candidature envoyée avec succès", false);
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "insert candidature", e);
            showMessage("Enregistrement impossible. Réessayez plus tard.", true);
        }
    }

    private void showMessage(String text, boolean error) {
        lblMessage.setText(text);
        lblMessage.setVisible(true);
        lblMessage.setManaged(true);
        lblMessage.setStyle(error
                ? "-fx-text-fill: #fca5a5;"
                : "-fx-text-fill: #86efac;");
    }

    private void hideMessage() {
        lblMessage.setVisible(false);
        lblMessage.setManaged(false);
    }

    private void resetFormAfterSuccess() {
        tfNom.clear();
        tfPrenom.clear();
        tfAge.clear();
        tfLat.clear();
        tfLng.clear();
        comboTrajet.getSelectionModel().clearSelection();
    }
}
