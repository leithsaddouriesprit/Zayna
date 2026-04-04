package tn.esprit.workshop.controlleurs.leith.parent;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import tn.esprit.workshop.controlleurs.leith.SceneNavigator;
import tn.esprit.workshop.model.amal.Meteo;
import tn.esprit.workshop.model.tous.Ecole;
import tn.esprit.workshop.services.amal.MeteoService;
import tn.esprit.workshop.services.leith.EcoleService;
import tn.esprit.workshop.utilis.AppSession;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ParentMeteoController implements Initializable {

    private static final Logger LOG = Logger.getLogger(ParentMeteoController.class.getName());

    @FXML private Label lblContexte;
    @FXML private VBox loadingBox;
    @FXML private VBox resultBox;
    @FXML private VBox errorBox;
    @FXML private Label lblErreur;
    @FXML private Label lblEmoji;
    @FXML private Label lblTemp;
    @FXML private Label lblRessenti;
    @FXML private Label lblDescription;
    @FXML private Label lblHumidite;
    @FXML private Label lblVent;

    private final MeteoService meteoService = new MeteoService();
    private final EcoleService ecoleService = new EcoleService();

    private int idEcoleContexte;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Integer ecoleId = AppSession.getInstance().getAndClearPendingMeteoEcoleId();
        if (ecoleId == null || ecoleId <= 0) {
            Platform.runLater(SceneNavigator::parentShellNavigateDemandeTransport);
            return;
        }

        Ecole ecole;
        try {
            ecole = ecoleService.getById(ecoleId);
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "ecole meteo", e);
            ecole = null;
        }
        if (ecole == null) {
            Platform.runLater(SceneNavigator::parentShellNavigateDemandeTransport);
            return;
        }

        final Ecole ecolePourMeteo = ecole;
        idEcoleContexte = ecoleId;
        String nomEc = ecolePourMeteo.getNomEcole() != null ? ecolePourMeteo.getNomEcole() : ("École #" + ecoleId);
        lblContexte.setText(nomEc);

        new Thread(() -> {
            try {
                Thread.sleep(150);
                Meteo meteo;
                if (ecolePourMeteo.getLatitude() != null && ecolePourMeteo.getLongitude() != null) {
                    meteo = meteoService.getMeteoByCoordinates(ecolePourMeteo.getLatitude(), ecolePourMeteo.getLongitude());
                } else {
                    meteo = meteoService.getMeteo("Tunis");
                }
                Meteo m = meteo;
                Platform.runLater(() -> showMeteo(m));
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "meteo", e);
                String msg = e.getMessage();
                if (msg == null || msg.isBlank()) {
                    msg = "Météo momentanément indisponible.";
                }
                String finalMsg = msg;
                Platform.runLater(() -> showError(finalMsg));
            }
        }).start();
    }

    private void showMeteo(Meteo meteo) {
        loadingBox.setVisible(false);
        loadingBox.setManaged(false);
        lblEmoji.setText(meteo.getEmoji());
        lblTemp.setText(String.format("%.1f °C", meteo.getTemperature()));
        lblRessenti.setText("Ressenti " + String.format("%.1f °C", meteo.getRessenti()));
        String d = meteo.getDescription() != null ? meteo.getDescription() : "";
        if (!d.isEmpty()) {
            d = Character.toUpperCase(d.charAt(0)) + d.substring(1);
        }
        lblDescription.setText(d);
        lblHumidite.setText(meteo.getHumidite() + " %");
        lblVent.setText(String.format("%.1f km/h", meteo.getVent() * 3.6));
        resultBox.setVisible(true);
        resultBox.setManaged(true);
    }

    private void showError(String message) {
        loadingBox.setVisible(false);
        loadingBox.setManaged(false);
        lblErreur.setText(message);
        lblErreur.setStyle("-fx-text-fill: #fca5a5;");
        errorBox.setVisible(true);
        errorBox.setManaged(true);
    }

    @FXML
    private void retourDemandeTransport() {
        if (idEcoleContexte > 0) {
            AppSession.getInstance().setPendingDemandeTransportEcoleRestoreId(idEcoleContexte);
        }
        SceneNavigator.parentShellNavigateDemandeTransport();
    }
}
