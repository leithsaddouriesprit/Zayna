package tn.esprit.workshop.controlleurs.leith.agent;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import tn.esprit.workshop.controlleurs.leith.SceneNavigator;
import tn.esprit.workshop.model.leith.CandidatureAgent;
import tn.esprit.workshop.model.leith.CandidatureAgentStatut;
import tn.esprit.workshop.services.leith.CandidatureAgentService;
import tn.esprit.workshop.utilis.AppSession;

import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;
import javafx.scene.control.TextFormatter;

public class AgentCandidatureEcolePendingController implements Initializable {

    @FXML private Label lblStatut;
    @FXML private Label lblMessage;

    private final CandidatureAgentService candidatureAgentService = new CandidatureAgentService();

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

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modifier la candidature");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField fNom = new TextField(c.getNom());
        TextField fPrenom = new TextField(c.getPrenom());
        TextField fEcole = new TextField(c.getNomEcole() != null ? c.getNomEcole() : "");
        fEcole.setPromptText("Nom de l'école");
        UnaryOperator<TextFormatter.Change> ecoleFilter = change -> {
            String text = change.getControlNewText();
            if (text == null) return null;
            if (text.length() > 150) return null;
            return text.matches("^[\\p{L} ]*$") ? change : null;
        };
        fEcole.setTextFormatter(new TextFormatter<>(ecoleFilter));
        TextField fLat = new TextField(String.valueOf(c.getLatitude()));
        TextField fLon = new TextField(String.valueOf(c.getLongitude()));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));
        int r = 0;
        grid.addRow(r++, new Label("Nom"), fNom);
        grid.addRow(r++, new Label("Prénom"), fPrenom);
        grid.addRow(r++, new Label("École"), fEcole);
        grid.addRow(r++, new Label("Latitude"), fLat);
        grid.addRow(r++, new Label("Longitude"), fLon);
        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isEmpty() || res.get() != ButtonType.OK) {
            return;
        }

        String nomEcole = fEcole.getText() != null ? fEcole.getText().trim() : "";
        if (nomEcole.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Le nom de l'école est obligatoire.").showAndWait();
            return;
        }
        if (!nomEcole.matches("^[\\p{L} ]+$")) {
            new Alert(Alert.AlertType.WARNING, "Nom d'école invalide : lettres et espaces uniquement.").showAndWait();
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
                candidatureAgentService.updateByUserId(
                        uid,
                        fNom.getText(),
                        fPrenom.getText(),
                        c.getIdEcole(),
                        lat,
                        lon,
                        nomEcole);
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
