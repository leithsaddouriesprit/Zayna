package tn.esprit.workshop.controlleurs.leith.parent;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.stage.Window;
import tn.esprit.workshop.controlleurs.leith.SceneNavigator;
import tn.esprit.workshop.model.leith.CandidatureEnfant;
import tn.esprit.workshop.model.leith.Trajet;
import tn.esprit.workshop.model.tous.Ecole;
import tn.esprit.workshop.services.leith.CandidatureEnfantService;
import tn.esprit.workshop.services.leith.EcoleService;
import tn.esprit.workshop.services.leith.TrajetService;
import tn.esprit.workshop.utilis.AppSession;
import tn.esprit.workshop.utilis.ZaynaInputConstraints;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Édition d’une candidature enfant ENVOYEE dans le shell Parent (UPDATE, pas INSERT).
 */
public class ParentModifierCandidatureEnfantController implements Initializable {

    private static final Logger LOG = Logger.getLogger(ParentModifierCandidatureEnfantController.class.getName());

    @FXML private Label lblEcole;
    @FXML private TextField tfNom;
    @FXML private TextField tfPrenom;
    @FXML private TextField tfAge;
    @FXML private TextField tfLat;
    @FXML private TextField tfLng;
    @FXML private ComboBox<Trajet> comboTrajet;
    @FXML private Label lblMessage;
    @FXML private Button btnEnregistrer;
    @FXML private GridPane gridForm;

    private final EcoleService ecoleService = new EcoleService();
    private final TrajetService trajetService = new TrajetService();
    private final CandidatureEnfantService candidatureEnfantService = new CandidatureEnfantService();

    private int candidatureId;
    private int idEcole;
    private Ecole ecole;
    private CandidatureEnfant candidature;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupAgeTextField();
        ZaynaInputConstraints.apply(tfNom, ZaynaInputConstraints.lettersAndSpacesOnly(ZaynaInputConstraints.LEN_ENFANT_NOM_PRENOM));
        ZaynaInputConstraints.apply(tfPrenom, ZaynaInputConstraints.lettersAndSpacesOnly(ZaynaInputConstraints.LEN_ENFANT_NOM_PRENOM));
        ZaynaInputConstraints.apply(tfLat, ZaynaInputConstraints.signedDecimalCoordinate());
        ZaynaInputConstraints.apply(tfLng, ZaynaInputConstraints.signedDecimalCoordinate());
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

        Integer pendingId = AppSession.getInstance().getAndClearPendingEditCandidatureEnfantId();
        int parentId = AppSession.getInstance().getParentId();
        if (pendingId == null || pendingId <= 0 || parentId <= 0) {
            abortToSuivi();
            return;
        }
        candidatureId = pendingId;
        try {
            candidature = candidatureEnfantService.getById(candidatureId);
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "load candidature", e);
            candidature = null;
        }
        if (candidature == null || candidature.getParentId() != parentId) {
            abortToSuivi();
            return;
        }
        if (!CandidatureEnfantService.STATUT_ENVOYEE.equals(candidature.getStatut())) {
            abortToSuivi();
            return;
        }

        idEcole = candidature.getIdEcole();
        try {
            ecole = ecoleService.getById(idEcole);
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "load ecole", e);
            ecole = null;
        }
        if (ecole == null) {
            abortToSuivi();
            return;
        }

        lblEcole.setText("École : " + (ecole.getNomEcole() != null ? ecole.getNomEcole() : ("#" + idEcole)));

        tfNom.setText(candidature.getNomEnfant() != null ? candidature.getNomEnfant() : "");
        tfPrenom.setText(candidature.getPrenomEnfant() != null ? candidature.getPrenomEnfant() : "");
        if (candidature.getAge() > 0) {
            tfAge.setText(String.valueOf(candidature.getAge()));
        }
        tfLat.setText(formatCoord(candidature.getLatitude()));
        tfLng.setText(formatCoord(candidature.getLongitude()));

        try {
            List<Trajet> trajets = trajetService.selectByEcoleId(idEcole);
            comboTrajet.setItems(FXCollections.observableArrayList(trajets));
            Integer tid = candidature.getTrajetId();
            if (tid != null && tid > 0) {
                for (Trajet t : trajets) {
                    if (t.getTrajetId() == tid) {
                        comboTrajet.getSelectionModel().select(t);
                        break;
                    }
                }
            }
            if (trajets.isEmpty()) {
                comboTrajet.setDisable(true);
                btnEnregistrer.setDisable(true);
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "load trajets", e);
            comboTrajet.setDisable(true);
            btnEnregistrer.setDisable(true);
        }

        GridPane.setHgrow(tfNom, Priority.ALWAYS);
        GridPane.setHgrow(tfPrenom, Priority.ALWAYS);
        GridPane.setHgrow(tfAge, Priority.ALWAYS);
        GridPane.setHgrow(tfLat, Priority.ALWAYS);
        GridPane.setHgrow(tfLng, Priority.ALWAYS);
        GridPane.setHgrow(comboTrajet, Priority.ALWAYS);
    }

    private static String formatCoord(double v) {
        if (Double.isNaN(v) || Double.isInfinite(v)) {
            return "";
        }
        String s = Double.toString(v);
        if (s.endsWith(".0")) {
            return s.substring(0, s.length() - 2);
        }
        return s;
    }

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

    private Integer parseAndValidateAge() {
        String err = ZaynaInputConstraints.validateChildAge3to18(tfAge.getText());
        if (err != null) {
            showMessage(err, true);
            return null;
        }
        return Integer.parseInt(tfAge.getText().trim());
    }

    @FXML
    private void retourSuivi() {
        SceneNavigator.openParentSuiviCandidaturesEnfant();
        if (!SceneNavigator.isInParentShell()) {
            closeStandaloneWindow();
        }
    }

    private void closeStandaloneWindow() {
        Window w = resolveWindow();
        if (w instanceof Stage) {
            ((Stage) w).close();
        }
    }

    private Window resolveWindow() {
        if (btnEnregistrer != null && btnEnregistrer.getScene() != null) {
            return btnEnregistrer.getScene().getWindow();
        }
        if (lblEcole != null && lblEcole.getScene() != null) {
            return lblEcole.getScene().getWindow();
        }
        return null;
    }

    /** Retour au suivi ; ferme la fenêtre si l’édition était ouverte hors shell Parent. */
    private void abortToSuivi() {
        Platform.runLater(() -> {
            SceneNavigator.openParentSuiviCandidaturesEnfant();
            if (!SceneNavigator.isInParentShell()) {
                closeStandaloneWindow();
            }
        });
    }

    @FXML
    private void enregistrer() {
        hideMessage();
        int parentId = AppSession.getInstance().getParentId();
        if (parentId <= 0 || candidature == null) {
            showMessage("Session invalide.", true);
            return;
        }

        String nom = tfNom.getText() != null ? tfNom.getText().trim() : "";
        String prenom = tfPrenom.getText() != null ? tfPrenom.getText().trim() : "";
        String errNom = ZaynaInputConstraints.validatePersonName(nom, ZaynaInputConstraints.LEN_ENFANT_NOM_PRENOM, "Le nom");
        if (errNom != null) {
            showMessage(errNom, true);
            return;
        }
        String errPrenom = ZaynaInputConstraints.validatePersonName(prenom, ZaynaInputConstraints.LEN_ENFANT_NOM_PRENOM, "Le prénom");
        if (errPrenom != null) {
            showMessage(errPrenom, true);
            return;
        }

        Integer ageBoxed = parseAndValidateAge();
        if (ageBoxed == null) {
            return;
        }
        int age = ageBoxed;

        String errLat = ZaynaInputConstraints.validateLatitude(tfLat.getText());
        if (errLat != null) {
            showMessage(errLat, true);
            return;
        }
        String errLng = ZaynaInputConstraints.validateLongitude(tfLng.getText());
        if (errLng != null) {
            showMessage(errLng, true);
            return;
        }
        double lat = Double.parseDouble(tfLat.getText().trim().replace(',', '.'));
        double lng = Double.parseDouble(tfLng.getText().trim().replace(',', '.'));

        Trajet t = comboTrajet.getSelectionModel().getSelectedItem();
        if (t == null || comboTrajet.isDisable()) {
            showMessage("Veuillez sélectionner un trajet de transport.", true);
            return;
        }
        if (t.getIdEcole() != idEcole) {
            showMessage("Le trajet choisi ne correspond pas à l’école de la candidature.", true);
            return;
        }

        try {
            boolean ok = candidatureEnfantService.updateEnVoyeeByParent(
                    candidatureId, parentId, nom, prenom, age, lat, lng, t.getTrajetId());
            if (!ok) {
                showMessage("Impossible de mettre à jour (candidature déjà traitée ou introuvable).", true);
                return;
            }
            AppSession.setFlashMessage("Modifications effectuées avec succès");
            SceneNavigator.openParentSuiviCandidaturesEnfant();
            if (!SceneNavigator.isInParentShell()) {
                closeStandaloneWindow();
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "update candidature", e);
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
}
