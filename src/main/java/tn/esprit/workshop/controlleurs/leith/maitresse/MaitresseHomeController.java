package tn.esprit.workshop.controlleurs.leith.maitresse;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import tn.esprit.workshop.model.tous.Ecole;
import tn.esprit.workshop.services.leith.EcoleService;
import tn.esprit.workshop.utilis.AppSession;

import java.net.URL;
import java.util.ResourceBundle;

public class MaitresseHomeController implements Initializable {

    @FXML private Label lblWelcome;
    @FXML private Label lblHint;

    private final EcoleService ecoleService = new EcoleService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String displayName = AppSession.getInstance().getConnectedUserName();
        if (displayName == null || displayName.isBlank()) {
            displayName = "Maîtresse";
        }
        String ecoleNom = "votre école";
        Integer ecoleId = AppSession.getInstance().getEcoleId();
        if (ecoleId != null) {
            try {
                Ecole e = ecoleService.getById(ecoleId);
                if (e != null && e.getNomEcole() != null && !e.getNomEcole().isBlank()) {
                    ecoleNom = e.getNomEcole();
                }
            } catch (Exception ignored) {
                // fallback
            }
        }
        lblWelcome.setText("Bonjour " + displayName + ", bienvenue à l'école " + ecoleNom + ".");
        lblHint.setText("Utilisez le menu « Gestion enfants » pour suivre les élèves de votre bus.");
    }
}
