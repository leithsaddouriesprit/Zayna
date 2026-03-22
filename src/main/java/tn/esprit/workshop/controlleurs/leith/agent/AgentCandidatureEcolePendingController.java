package tn.esprit.workshop.controlleurs.leith.agent;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import tn.esprit.workshop.controlleurs.leith.SceneNavigator;
import tn.esprit.workshop.model.leith.CandidatureAgent;
import tn.esprit.workshop.model.leith.CandidatureAgentStatut;
import tn.esprit.workshop.model.tous.Ecole;
import tn.esprit.workshop.services.leith.CandidatureAgentService;
import tn.esprit.workshop.services.leith.EcoleService;
import tn.esprit.workshop.utilis.AppSession;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class AgentCandidatureEcolePendingController implements Initializable {

    @FXML private Label lblStatut;
    @FXML private Label lblMessage;

    private final CandidatureAgentService candidatureAgentService = new CandidatureAgentService();
    private final EcoleService ecoleService = new EcoleService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        refreshStatut();
    }

    private void refreshStatut() {
        Integer uid = AppSession.getInstance().getConnectedUserId();
        if (uid == null) {
            lblStatut.setText("Session invalide.");
            return;
        }
        new Thread(() -> {
            try {
                CandidatureAgent c = candidatureAgentService.findByUserId(uid);
                Platform.runLater(() -> {
                    if (c == null) {
                        lblStatut.setText("Aucune candidature trouvée. Contactez l'administrateur.");
                        return;
                    }
                    lblStatut.setText("Statut : " + labelStatut(c.getStatut()));
                });
            } catch (SQLException e) {
                Platform.runLater(() -> lblStatut.setText("Erreur : " + e.getMessage()));
            }
        }).start();
    }

    private static String labelStatut(CandidatureAgentStatut s) {
        if (s == null) return "—";
        return switch (s) {
            case EN_ATTENTE -> "En attente de validation";
            case APPROUVEE -> "Approuvée";
            case REFUSEE -> "Refusée — vous pouvez modifier et renvoyer la candidature";
        };
    }

    @FXML
    private void onModifier() {
        Integer uid = AppSession.getInstance().getConnectedUserId();
        if (uid == null) return;

        CandidatureAgent c;
        try {
            c = candidatureAgentService.findByUserId(uid);
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
            return;
        }
        if (c == null) {
            new Alert(Alert.AlertType.WARNING, "Aucune candidature.").showAndWait();
            return;
        }

        List<Ecole> ecoles;
        try {
            ecoles = ecoleService.selectAll();
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modifier la candidature");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField fNom = new TextField(c.getNom());
        TextField fPrenom = new TextField(c.getPrenom());
        ComboBox<Ecole> comboEcole = new ComboBox<>();
        comboEcole.getItems().setAll(ecoles);
        comboEcole.setConverter(new StringConverter<>() {
            @Override
            public String toString(Ecole e) {
                return e == null ? "" : e.getNomEcole();
            }

            @Override
            public Ecole fromString(String s) {
                return null;
            }
        });
        for (Ecole e : ecoles) {
            if (e.getId() == c.getIdEcole()) {
                comboEcole.getSelectionModel().select(e);
                break;
            }
        }
        SceneNavigator.applyAppCssToComboBoxPopup(comboEcole);
        TextField fLat = new TextField(String.valueOf(c.getLatitude()));
        TextField fLon = new TextField(String.valueOf(c.getLongitude()));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));
        int r = 0;
        grid.addRow(r++, new Label("Nom"), fNom);
        grid.addRow(r++, new Label("Prénom"), fPrenom);
        grid.addRow(r++, new Label("École"), comboEcole);
        grid.addRow(r++, new Label("Latitude"), fLat);
        grid.addRow(r++, new Label("Longitude"), fLon);
        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isEmpty() || res.get() != ButtonType.OK) {
            return;
        }

        Ecole sel = comboEcole.getSelectionModel().getSelectedItem();
        if (sel == null) {
            new Alert(Alert.AlertType.WARNING, "Choisissez une école.").showAndWait();
            return;
        }
        double lat;
        double lon;
        try {
            lat = Double.parseDouble(fLat.getText().trim().replace(',', '.'));
            lon = Double.parseDouble(fLon.getText().trim().replace(',', '.'));
        } catch (NumberFormatException ex) {
            new Alert(Alert.AlertType.ERROR, "Latitude / longitude invalides.").showAndWait();
            return;
        }

        new Thread(() -> {
            try {
                candidatureAgentService.updateByUserId(uid, fNom.getText(), fPrenom.getText(), sel.getId(), lat, lon);
                Platform.runLater(() -> {
                    lblMessage.setText("Candidature mise à jour.");
                    refreshStatut();
                });
            } catch (SQLException e) {
                Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait());
            }
        }).start();
    }

    @FXML
    private void onSupprimer() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setContentText("Supprimer votre candidature ? Vous devrez contacter un administrateur pour continuer.");
        Optional<ButtonType> r = confirm.showAndWait();
        if (r.isEmpty() || r.get() != ButtonType.OK) {
            return;
        }
        Integer uid = AppSession.getInstance().getConnectedUserId();
        if (uid == null) return;

        new Thread(() -> {
            try {
                candidatureAgentService.deleteByUserId(uid);
                Platform.runLater(() -> {
                    SceneNavigator.unregisterAgentShell();
                    Stage st = lblMessage.getScene() != null ? (Stage) lblMessage.getScene().getWindow() : null;
                    if (st != null) st.close();
                    SceneNavigator.showLoginWindow();
                });
            } catch (SQLException e) {
                Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait());
            }
        }).start();
    }
}
