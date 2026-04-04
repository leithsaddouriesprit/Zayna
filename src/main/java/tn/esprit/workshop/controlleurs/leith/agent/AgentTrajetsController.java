package tn.esprit.workshop.controlleurs.leith.agent;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import tn.esprit.workshop.model.leith.Trajet;
import tn.esprit.workshop.services.leith.TrajetService;
import tn.esprit.workshop.utilis.AppSession;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AgentTrajetsController implements Initializable {

    private static final Logger LOG = Logger.getLogger(AgentTrajetsController.class.getName());

    @FXML private TableView<Trajet> table;
    @FXML private TableColumn<Trajet, Number> colId;
    @FXML private TableColumn<Trajet, String> colNom;
    @FXML private TableColumn<Trajet, Number> colIdBus;
    @FXML private TableColumn<Trajet, String> colHeure;
    @FXML private TableColumn<Trajet, Number> colPrix;
    @FXML private TableColumn<Trajet, String> colStatut;
    @FXML private Button btnModifier;
    @FXML private Button btnSupprimer;
    @FXML private Label lblMessage;

    private final TrajetService trajetService = new TrajetService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getTrajetId()));
        colNom.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNom()));
        colIdBus.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getIdBus()));
        colHeure.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getHeureDepart() != null ? c.getValue().getHeureDepart().toString() : ""));
        colPrix.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getPrix()));
        colStatut.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getStatut() != null ? c.getValue().getStatut() : ""));

        table.getSelectionModel().selectedItemProperty().addListener((o, old, sel) -> {
            btnModifier.setDisable(sel == null);
            btnSupprimer.setDisable(sel == null);
        });

        load();
    }

    private void load() {
        Integer idEcole = AppSession.getInstance().getEcoleId();
        if (idEcole == null) {
            lblMessage.setText("Aucune école en session.");
            return;
        }
        try {
            table.getItems().setAll(trajetService.selectByEcoleId(idEcole));
            lblMessage.setText("");
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "load trajets", e);
            lblMessage.setText("Erreur: " + e.getMessage());
        }
    }

    @FXML
    void ajouter() {
        Integer idEcole = AppSession.getInstance().getEcoleId();
        if (idEcole == null) {
            new Alert(Alert.AlertType.WARNING, "Aucune école en session.").showAndWait();
            return;
        }
        Dialog<Trajet> d = dialogTrajet(null, idEcole);
        d.showAndWait().ifPresent(t -> {
            try {
                trajetService.insertOne(t);
                new Alert(Alert.AlertType.INFORMATION, "✅ Trajet ajouté.").showAndWait();
                load();
            } catch (SQLException e) {
                new Alert(Alert.AlertType.ERROR, "❌ Erreur: " + e.getMessage()).showAndWait();
            }
        });
    }

    @FXML
    void modifier() {
        Trajet sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) {
            new Alert(Alert.AlertType.WARNING, "Sélectionnez un trajet.").showAndWait();
            return;
        }
        Dialog<Trajet> d = dialogTrajet(sel, sel.getIdEcole());
        d.showAndWait().ifPresent(t -> {
            try {
                t.setTrajetId(sel.getTrajetId());
                trajetService.updateOne(t);
                new Alert(Alert.AlertType.INFORMATION, "✅ Trajet modifié.").showAndWait();
                load();
            } catch (SQLException e) {
                new Alert(Alert.AlertType.ERROR, "❌ Erreur: " + e.getMessage()).showAndWait();
            }
        });
    }

    @FXML
    void supprimer() {
        Trajet sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) {
            new Alert(Alert.AlertType.WARNING, "Sélectionnez un trajet.").showAndWait();
            return;
        }
        new Alert(Alert.AlertType.CONFIRMATION, "Supprimer ce trajet ?").showAndWait()
                .filter(r -> r == ButtonType.OK)
                .ifPresent(r -> {
                    try {
                        trajetService.deleteOne(sel);
                        new Alert(Alert.AlertType.INFORMATION, "✅ Trajet supprimé.").showAndWait();
                        load();
                    } catch (SQLException e) {
                        new Alert(Alert.AlertType.ERROR, "❌ Erreur: " + e.getMessage()).showAndWait();
                    }
                });
    }

    private Dialog<Trajet> dialogTrajet(Trajet existing, int idEcole) {
        Dialog<Trajet> d = new Dialog<>();
        d.setTitle(existing == null ? "Nouveau trajet" : "Modifier trajet");
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField tfNom = new TextField(existing != null ? existing.getNom() : "");
        TextField tfIdBus = new TextField(existing != null && existing.getIdBus() != 0 ? String.valueOf(existing.getIdBus()) : "");
        TextField tfHeure = new TextField(existing != null && existing.getHeureDepart() != null ? existing.getHeureDepart().toString() : "08:00");
        TextField tfPrix = new TextField(existing != null ? String.valueOf(existing.getPrix()) : "0");
        TextField tfStatut = new TextField(existing != null && existing.getStatut() != null ? existing.getStatut() : "PLANIFIE");

        javafx.scene.layout.GridPane gp = new javafx.scene.layout.GridPane();
        gp.setHgap(8); gp.setVgap(8);
        gp.add(new Label("Nom:"), 0, 0); gp.add(tfNom, 1, 0);
        gp.add(new Label("Id Bus (0=vide):"), 0, 1); gp.add(tfIdBus, 1, 1);
        gp.add(new Label("Heure départ (HH:mm):"), 0, 2); gp.add(tfHeure, 1, 2);
        gp.add(new Label("Prix:"), 0, 3); gp.add(tfPrix, 1, 3);
        gp.add(new Label("Statut:"), 0, 4); gp.add(tfStatut, 1, 4);
        d.getDialogPane().setContent(gp);

        d.setResultConverter(bt -> {
            if (bt != ButtonType.OK) return null;
            String nom = tfNom.getText() != null ? tfNom.getText().trim() : "";
            if (nom.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "❌ Nom trajet obligatoire.").showAndWait();
                return null;
            }
            String heureStr = tfHeure.getText() != null ? tfHeure.getText().trim() : "";
            if (heureStr.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "❌ Heure départ obligatoire (ex: 08:00).").showAndWait();
                return null;
            }
            java.time.LocalTime heure;
            try {
                heure = java.time.LocalTime.parse(heureStr);
            } catch (Exception e) {
                new Alert(Alert.AlertType.WARNING, "❌ Heure invalide. Format attendu: HH:mm").showAndWait();
                return null;
            }
            Trajet t = new Trajet();
            t.setNom(nom);
            try {
                t.setIdBus(tfIdBus.getText().trim().isEmpty() ? 0 : Integer.parseInt(tfIdBus.getText().trim()));
            } catch (NumberFormatException e) {
                t.setIdBus(0);
            }
            t.setHeureDepart(heure);
            t.setIdEcole(idEcole);
            String prixStr = tfPrix.getText() != null ? tfPrix.getText().trim().replace(',', '.') : "0";
            try {
                t.setPrix(Double.parseDouble(prixStr.isEmpty() ? "0" : prixStr));
            } catch (NumberFormatException e) {
                new Alert(Alert.AlertType.WARNING, "❌ Prix invalide (nombre attendu).").showAndWait();
                return null;
            }
            t.setStatut(tfStatut.getText() != null && !tfStatut.getText().trim().isEmpty() ? tfStatut.getText().trim() : "PLANIFIE");
            return t;
        });
        return d;
    }
}
