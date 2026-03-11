package tn.esprit.workshop.controlleurs.leith.agent;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.workshop.model.leith.Candidature;
import tn.esprit.workshop.services.leith.CandidatureService;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AgentCandidaturesChauffeurController implements Initializable {

    private static final Logger LOG = Logger.getLogger(AgentCandidaturesChauffeurController.class.getName());

    @FXML private TableView<Candidature> table;
    @FXML private TableColumn<Candidature, Number> colId;
    @FXML private TableColumn<Candidature, String> colNom;
    @FXML private TableColumn<Candidature, String> colPrenom;
    @FXML private TableColumn<Candidature, Number> colAge;
    @FXML private TableColumn<Candidature, Number> colExperience;
    @FXML private TableColumn<Candidature, String> colDate;
    @FXML private TableColumn<Candidature, String> colStatut;
    @FXML private TableColumn<Candidature, Void> colActions;
    @FXML private Label lblMessage;

    private final CandidatureService candidatureService = new CandidatureService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getId()));
        colNom.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNom() != null ? c.getValue().getNom() : ""));
        colPrenom.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getPrenom() != null ? c.getValue().getPrenom() : ""));
        colAge.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getAge()));
        colExperience.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getNbAnsExperience()));
        colDate.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getDateEnvoi() != null ? c.getValue().getDateEnvoi().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : ""));
        colStatut.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getStatut() != null ? c.getValue().getStatut() : ""));

        colActions.setCellFactory(tc -> new TableCell<>() {
            private final Button btnDetails = new Button("Détails");
            private final Button btnAccepter = new Button("Accepter");
            private final Button btnRefuser = new Button("Refuser");

            {
                btnDetails.setOnAction(e -> {
                    Candidature c = getTableRow().getItem();
                    if (c != null) showDetails(c);
                });
                btnAccepter.setOnAction(e -> {
                    Candidature c = getTableRow().getItem();
                    if (c != null) accepter(c.getId());
                });
                btnRefuser.setOnAction(e -> {
                    Candidature c = getTableRow().getItem();
                    if (c != null) refuser(c.getId());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(6, btnDetails, btnAccepter, btnRefuser);
                    setGraphic(box);
                }
            }
        });

        load();
    }

    private void load() {
        try {
            table.getItems().setAll(candidatureService.findAllEnVoyee());
            lblMessage.setText("");
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "load candidatures", e);
            lblMessage.setText("Erreur: " + e.getMessage());
        }
    }

    /** Popup Détails : infos chauffeur + maladie + aperçu permis recto/verso. */
    private void showDetails(Candidature c) {
        VBox root = new VBox(12);
        root.setPadding(new javafx.geometry.Insets(16));
        root.getChildren().add(new Label("ID: " + c.getId()));
        root.getChildren().add(new Label("Nom: " + (c.getNom() != null ? c.getNom() : "-")));
        root.getChildren().add(new Label("Prénom: " + (c.getPrenom() != null ? c.getPrenom() : "-")));
        root.getChildren().add(new Label("Âge: " + c.getAge()));
        root.getChildren().add(new Label("Expérience (ans): " + c.getNbAnsExperience()));
        root.getChildren().add(new Label("Maladie: " + (c.getMaladie() != null && !c.getMaladie().isEmpty() ? c.getMaladie() : "Aucune")));
        root.getChildren().add(new javafx.scene.control.Separator());
        root.getChildren().add(new Label("Permis de conduire"));
        try {
            byte[][] rectoVerso = candidatureService.getPermisRectoVerso(c.getId());
            if (rectoVerso != null) {
                if (rectoVerso.length > 0 && rectoVerso[0] != null && rectoVerso[0].length > 0) {
                    root.getChildren().add(new Label("Recto:"));
                    ImageView ivRecto = new ImageView(new Image(new ByteArrayInputStream(rectoVerso[0])));
                    ivRecto.setFitWidth(320);
                    ivRecto.setPreserveRatio(true);
                    root.getChildren().add(ivRecto);
                } else {
                    root.getChildren().add(new Label("Recto: Aucun fichier"));
                }
                if (rectoVerso.length > 1 && rectoVerso[1] != null && rectoVerso[1].length > 0) {
                    root.getChildren().add(new Label("Verso:"));
                    ImageView ivVerso = new ImageView(new Image(new ByteArrayInputStream(rectoVerso[1])));
                    ivVerso.setFitWidth(320);
                    ivVerso.setPreserveRatio(true);
                    root.getChildren().add(ivVerso);
                } else {
                    root.getChildren().add(new Label("Verso: Aucun fichier"));
                }
            } else {
                root.getChildren().add(new Label("Recto: Aucun fichier"));
                root.getChildren().add(new Label("Verso: Aucun fichier"));
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "showDetails permis", e);
            root.getChildren().add(new Label("Erreur chargement permis."));
        }
        Button fermer = new Button("Fermer");
        fermer.setOnAction(e -> ((Stage) root.getScene().getWindow()).close());
        root.getChildren().add(fermer);
        Stage popup = new Stage();
        popup.setTitle("Détails candidature");
        popup.setScene(new javafx.scene.Scene(root, 420, 580));
        popup.show();
    }

    private void accepter(int candidatureId) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer");
        confirm.setHeaderText(null);
        confirm.setContentText("Accepter cette candidature ?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
        try {
            candidatureService.accepter(candidatureId);
            new Alert(Alert.AlertType.INFORMATION, "✅ Candidature acceptée.").showAndWait();
            load();
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "accepter", e);
            new Alert(Alert.AlertType.ERROR, "❌ Erreur: " + e.getMessage()).showAndWait();
        }
    }

    private void refuser(int candidatureId) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer");
        confirm.setHeaderText(null);
        confirm.setContentText("Refuser cette candidature ?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
        try {
            candidatureService.refuser(candidatureId);
            new Alert(Alert.AlertType.INFORMATION, "✅ Candidature refusée.").showAndWait();
            load();
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "refuser", e);
            new Alert(Alert.AlertType.ERROR, "❌ Erreur: " + e.getMessage()).showAndWait();
        }
    }
}
