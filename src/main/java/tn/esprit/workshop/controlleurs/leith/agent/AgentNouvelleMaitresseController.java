package tn.esprit.workshop.controlleurs.leith.agent;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import tn.esprit.workshop.controlleurs.leith.SceneNavigator;
import tn.esprit.workshop.services.leith.MaitresseMetierService;
import tn.esprit.workshop.utilis.AppSession;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class AgentNouvelleMaitresseController implements Initializable {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    @FXML private TextField fieldNom;
    @FXML private TextField fieldPrenom;
    @FXML private TextField fieldEmail;
    @FXML private PasswordField fieldPassword;
    @FXML private ComboBox<MaitresseMetierService.BusOption> comboBus;
    @FXML private Label lblMessage;

    private final MaitresseMetierService service = new MaitresseMetierService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        SceneNavigator.applyAppCssToComboBoxPopup(comboBus);
        reloadBuses();
    }

    private void reloadBuses() {
        Integer ecoleId = AppSession.getInstance().getEcoleId();
        if (ecoleId == null) return;
        new Thread(() -> {
            try {
                List<MaitresseMetierService.BusOption> list = new ArrayList<>();
                list.add(new MaitresseMetierService.BusOption(0, "Aucun / Non affecté"));
                list.addAll(service.listAvailableBusesForEcole(ecoleId, 0));
                Platform.runLater(() -> {
                    comboBus.getItems().setAll(list);
                    if (!list.isEmpty()) {
                        comboBus.getSelectionModel().select(0);
                    }
                });
            } catch (SQLException e) {
                Platform.runLater(() -> lblMessage.setText("Erreur bus : " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void onReset() {
        fieldNom.clear();
        fieldPrenom.clear();
        fieldEmail.clear();
        fieldPassword.clear();
        if (!comboBus.getItems().isEmpty()) {
            comboBus.getSelectionModel().select(0);
        }
        lblMessage.setText("");
    }

    @FXML
    private void onSave() {
        lblMessage.setStyle("-fx-text-fill: #fca5a5;");
        Integer ecoleId = AppSession.getInstance().getEcoleId();
        if (ecoleId == null) {
            lblMessage.setText("École non définie pour votre session.");
            return;
        }
        String nom = fieldNom.getText() != null ? fieldNom.getText().trim() : "";
        String prenom = fieldPrenom.getText() != null ? fieldPrenom.getText().trim() : "";
        String email = fieldEmail.getText() != null ? fieldEmail.getText().trim() : "";
        String password = fieldPassword.getText() != null ? fieldPassword.getText() : "";
        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || password.isEmpty()) {
            lblMessage.setText("Veuillez remplir le nom, le prénom, l'email et le mot de passe.");
            return;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            lblMessage.setText("Veuillez saisir une adresse email valide.");
            return;
        }
        MaitresseMetierService.BusOption bus = comboBus.getSelectionModel().getSelectedItem();
        Integer idBus = (bus == null || bus.getId() <= 0) ? null : bus.getId();

        new Thread(() -> {
            try {
                service.createMaitresseForEcole(
                        ecoleId,
                        nom,
                        prenom,
                        email,
                        password,
                        idBus
                );
                Platform.runLater(() -> {
                    lblMessage.setStyle("-fx-text-fill: #86efac;");
                    lblMessage.setText("Maîtresse créée avec succès.");
                    onReset();
                });
            } catch (Exception ignored) {
                Platform.runLater(() -> {
                    lblMessage.setStyle("-fx-text-fill: #fca5a5;");
                    lblMessage.setText("Création de la maîtresse impossible.");
                });
            }
        }).start();
    }
}
