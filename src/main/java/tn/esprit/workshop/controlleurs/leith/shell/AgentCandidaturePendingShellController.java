package tn.esprit.workshop.controlleurs.leith.shell;

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

/**
 * Shell plein écran pour responsable d’école : candidature {@code EN_ATTENTE} ou {@code REFUSEE} uniquement.
 * Pas d’accès au {@link AgentShellController} complet.
 */
public class AgentCandidaturePendingShellController implements Initializable {

    @FXML private Label lblAccountInfo;
    @FXML private Label lblStatut;
    @FXML private TextArea txtDetails;
    @FXML private Label lblMessage;

    private final CandidatureAgentService candidatureAgentService = new CandidatureAgentService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        updateAccountLabel();
        refreshStatut();
    }

    private void updateAccountLabel() {
        if (lblAccountInfo != null) {
            String name = AppSession.getInstance().getConnectedUserName();
            String role = AppSession.getInstance().getConnectedUserRole();
            lblAccountInfo.setText((name == null || name.isEmpty() ? "Responsable école" : name)
                    + " (" + (role == null || role.isEmpty() ? "Agent École" : role) + ")");
        }
    }

    private void refreshStatut() {
        Integer uid = AppSession.getInstance().getConnectedUserId();
        if (uid == null) {
            if (lblStatut != null) lblStatut.setText("Session invalide.");
            return;
        }
        new Thread(() -> {
            try {
                CandidatureAgent c = candidatureAgentService.findByUserId(uid);
                Platform.runLater(() -> applyCandidatureToUi(c));
            } catch (SQLException e) {
                Platform.runLater(() -> {
                    if (lblStatut != null) lblStatut.setText("Erreur");
                    if (txtDetails != null) txtDetails.setText(e.getMessage());
                });
            }
        }).start();
    }

    private void applyCandidatureToUi(CandidatureAgent c) {
        if (c == null) {
            lblStatut.setText("Aucune candidature");
            txtDetails.setText("Aucune candidature trouvée pour ce compte. Contactez l’administrateur.");
            return;
        }
        lblStatut.setText("Statut : " + labelStatut(c.getStatut()));
        String nomE = c.getEcole() != null && !c.getEcole().isBlank() ? c.getEcole() : "—";
        StringBuilder sb = new StringBuilder();
        sb.append("Nom : ").append(nullToDash(c.getNom())).append("\n");
        sb.append("Prénom : ").append(nullToDash(c.getPrenom())).append("\n");
        sb.append("Établissement : ").append(nomE).append("\n");
        sb.append("Adresse : ").append(nullToDash(c.getAdresse())).append("\n");
        sb.append("Latitude : ").append(c.getLatitude()).append("\n");
        sb.append("Longitude : ").append(c.getLongitude()).append("\n");
        if (c.getCreatedAt() != null) {
            sb.append("Créée le : ").append(c.getCreatedAt()).append("\n");
        }
        if (c.getUpdatedAt() != null) {
            sb.append("Dernière mise à jour : ").append(c.getUpdatedAt());
        }
        txtDetails.setText(sb.toString());
    }

    private static String nullToDash(String s) {
        return s == null || s.isBlank() ? "—" : s;
    }

    private static String labelStatut(CandidatureAgentStatut s) {
        if (s == null) return "—";
        return switch (s) {
            case EN_ATTENTE -> "En attente de validation";
            case APPROUVEE -> "Approuvée";
            case REFUSEE -> "Refusée — vous pouvez modifier et renvoyer la candidature";
        };
    }

    private Stage stage() {
        if (lblStatut != null && lblStatut.getScene() != null && lblStatut.getScene().getWindow() instanceof Stage st) {
            return st;
        }
        return null;
    }

    @FXML
    private void logout() {
        Stage st = stage();
        if (st != null) {
            st.close();
        }
        SceneNavigator.showLoginWindow();
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
        TextField fEcole = new TextField(c.getEcole() != null ? c.getEcole() : "");
        TextArea fAdresse = new TextArea(c.getAdresse() != null ? c.getAdresse() : "");
        fAdresse.setPrefRowCount(2);
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
        grid.addRow(r++, new Label("Adresse"), fAdresse);
        grid.addRow(r++, new Label("Latitude"), fLat);
        grid.addRow(r++, new Label("Longitude"), fLon);
        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isEmpty() || res.get() != ButtonType.OK) {
            return;
        }
        if (fEcole.getText() == null || fEcole.getText().isBlank()) {
            new Alert(Alert.AlertType.WARNING, "Le nom de l'établissement (ecole) est obligatoire.").showAndWait();
            return;
        }
        if (fEcole.getText().trim().length() > 150) {
            new Alert(Alert.AlertType.WARNING, "Le nom de l'établissement ne doit pas dépasser 150 caractères.").showAndWait();
            return;
        }
        if (fAdresse.getText() == null || fAdresse.getText().isBlank()) {
            new Alert(Alert.AlertType.WARNING, "L'adresse est obligatoire.").showAndWait();
            return;
        }
        if (fAdresse.getText().trim().length() > 255) {
            new Alert(Alert.AlertType.WARNING, "L'adresse ne doit pas dépasser 255 caractères.").showAndWait();
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
                candidatureAgentService.updateCandidature(
                        uid,
                        fNom.getText(),
                        fPrenom.getText(),
                        fEcole.getText(),
                        fAdresse.getText(),
                        lat,
                        lon);
                Platform.runLater(() -> {
                    if (lblMessage != null) lblMessage.setText("Candidature mise à jour.");
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
        confirm.setContentText("Supprimer votre candidature supprimera aussi votre compte utilisateur. Continuer ?");
        Optional<ButtonType> r = confirm.showAndWait();
        if (r.isEmpty() || r.get() != ButtonType.OK) {
            return;
        }
        Integer uid = AppSession.getInstance().getConnectedUserId();
        if (uid == null) return;

        new Thread(() -> {
            try {
                candidatureAgentService.deleteCandidatureAndUser(uid);
                Platform.runLater(() -> {
                    Stage st = stage();
                    if (st != null) st.close();
                    SceneNavigator.showLoginWindow();
                });
            } catch (SQLException e) {
                Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait());
            }
        }).start();
    }
}
