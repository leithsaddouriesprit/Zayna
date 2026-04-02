package tn.esprit.workshop.controlleurs;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.workshop.model.Ecole;
import tn.esprit.workshop.model.Enfant;
import tn.esprit.workshop.model.Presence;
import tn.esprit.workshop.services.EcoleService;
import tn.esprit.workshop.services.EnfantService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public class MaitresseController {

    // ===== COMPOSANTS FXML =====

    // Statistiques
    @FXML private Label lblTotalEnfants;
    @FXML private Label lblPresentAujourdhui;
    @FXML private Label lblAbsentAujourdhui;
    @FXML private Label lblRetardAujourdhui;

    // Tableau des enfants
    @FXML private TableView<Enfant> tableEnfants;
    @FXML private TableColumn<Enfant, String> colEnfantNom;
    @FXML private TableColumn<Enfant, String> colEnfantClasse;
    @FXML private TableColumn<Enfant, String> colEnfantStatut;

    // Champs enfant
    @FXML private TextField tfNom;
    @FXML private TextField tfPrenom;
    @FXML private DatePicker dpDateNaissance;
    @FXML private TextField tfClasse;
    @FXML private TextField tfTelephone;
    @FXML private TextField tfEmail;

    // Champs présence
    @FXML private DatePicker dpDatePresence;
    @FXML private ComboBox<String> cbStatutMatin;
    @FXML private TextField tfHeureArrivee;
    @FXML private TextField tfHeureDepart;
    @FXML private ComboBox<String> cbStatutMidi;
    @FXML private ComboBox<String> cbStatutSoir;
    @FXML private CheckBox chkRemorque;
    @FXML private TextField tfRemorqueur;
    @FXML private TextArea tfObservations;

    // ===== SERVICES =====
    private final EnfantService enfantService = new EnfantService();
    private final EcoleService ecoleService = new EcoleService();

    // ===== LISTS =====
    private ObservableList<Enfant> enfantList = FXCollections.observableArrayList();
    private Ecole ecoleConnectee = null;

    @FXML
    public void initialize() {
        setupTableColumns();
        setupComboBoxes();
        loadEnfants();
        setupListeners();
        chargerStatistiques();

        dpDatePresence.setValue(LocalDate.now());
    }

    // ===== CONFIGURATION DES COLONNES =====
    private void setupTableColumns() {
        colEnfantNom.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getNomComplet()));
        colEnfantClasse.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getClasse()));
        colEnfantStatut.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getStatut()));

        colEnfantStatut.setCellFactory(col -> new TableCell<Enfant, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label label = new Label(item);
                    if ("ACTIF".equals(item)) {
                        label.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 4px 12px; -fx-background-radius: 20px;");
                    } else {
                        label.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-padding: 4px 12px; -fx-background-radius: 20px;");
                    }
                    setGraphic(label);
                }
            }
        });
    }

    // ===== CONFIGURATION DES COMBOBOX =====
    private void setupComboBoxes() {
        ObservableList<String> statuts = FXCollections.observableArrayList("PRESENT", "ABSENT", "RETARD", "JUSTIFIE");
        cbStatutMatin.setItems(statuts);
        cbStatutMidi.setItems(statuts);
        cbStatutSoir.setItems(statuts);

        cbStatutMatin.setValue("PRESENT");
        cbStatutMidi.setValue("PRESENT");
        cbStatutSoir.setValue("PRESENT");
    }

    // ===== CHARGEMENT DES DONNÉES =====
    private void loadEnfants() {
        try {
            List<Enfant> list = enfantService.selectAllEnfants();
            enfantList.setAll(list);
            tableEnfants.setItems(enfantList);
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les enfants: " + e.getMessage(), AlertType.ERROR);
        }
    }

    private void chargerStatistiques() {
        int total = enfantList.size();
        lblTotalEnfants.setText(String.valueOf(total));

        LocalDate aujourdhui = LocalDate.now();
        int presents = 0, absents = 0, retards = 0;

        try {
            for (Enfant enfant : enfantList) {
                List<Presence> presences = enfantService.selectPresenceByEnfant(enfant.getId());
                for (Presence p : presences) {
                    if (p.getDate().equals(aujourdhui)) {
                        if ("PRESENT".equals(p.getStatutMatin())) presents++;
                        else if ("ABSENT".equals(p.getStatutMatin())) absents++;
                        else if ("RETARD".equals(p.getStatutMatin())) retards++;
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        lblPresentAujourdhui.setText(String.valueOf(presents));
        lblAbsentAujourdhui.setText(String.valueOf(absents));
        lblRetardAujourdhui.setText(String.valueOf(retards));
    }

    // ===== LISTENERS =====
    private void setupListeners() {
        tableEnfants.getSelectionModel().selectedItemProperty().addListener((obs, old, nouveau) -> {
            if (nouveau != null) {
                remplirChampsEnfant(nouveau);
            }
        });

        chkRemorque.selectedProperty().addListener((obs, old, val) -> {
            tfRemorqueur.setDisable(!val);
            if (!val) tfRemorqueur.clear();
        });
        tfRemorqueur.setDisable(true);

        setupHeureValidation(tfHeureArrivee);
        setupHeureValidation(tfHeureDepart);
    }

    private void setupHeureValidation(TextField tf) {
        tf.textProperty().addListener((obs, old, nouveau) -> {
            if (nouveau == null || nouveau.isEmpty()) return;
            if (nouveau.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$")) {
                tf.setStyle("-fx-border-color: #27ae60;");
            } else {
                tf.setStyle("-fx-border-color: #e74c3c;");
            }
        });
    }

    // ===== REMPLIR LES CHAMPS =====
    private void remplirChampsEnfant(Enfant enfant) {
        tfNom.setText(enfant.getNom());
        tfPrenom.setText(enfant.getPrenom());
        dpDateNaissance.setValue(enfant.getDateNaissance());
        tfClasse.setText(enfant.getClasse());
        tfTelephone.setText(enfant.getNumTelephoneParent());
        tfEmail.setText(enfant.getEmailParent());
    }

    @FXML
    private void clearEnfantFields() {
        tfNom.clear();
        tfPrenom.clear();
        dpDateNaissance.setValue(null);
        tfClasse.clear();
        tfTelephone.clear();
        tfEmail.clear();
        tableEnfants.getSelectionModel().clearSelection();
    }

    private void clearPresenceFields() {
        dpDatePresence.setValue(LocalDate.now());
        cbStatutMatin.setValue("PRESENT");
        cbStatutMidi.setValue("PRESENT");
        cbStatutSoir.setValue("PRESENT");
        tfHeureArrivee.clear();
        tfHeureDepart.clear();
        chkRemorque.setSelected(false);
        tfRemorqueur.clear();
        tfObservations.clear();

        tfHeureArrivee.setStyle("");
        tfHeureDepart.setStyle("");
    }

    // ===== VALIDATIONS =====
    private boolean validateEnfantFields() {
        if (tfNom.getText().trim().isEmpty()) {
            showAlert("Erreur", "Le nom est obligatoire", AlertType.WARNING);
            tfNom.requestFocus();
            return false;
        }
        if (tfPrenom.getText().trim().isEmpty()) {
            showAlert("Erreur", "Le prénom est obligatoire", AlertType.WARNING);
            tfPrenom.requestFocus();
            return false;
        }
        if (dpDateNaissance.getValue() == null) {
            showAlert("Erreur", "La date de naissance est obligatoire", AlertType.WARNING);
            dpDateNaissance.requestFocus();
            return false;
        }
        return true;
    }

    private boolean validatePresenceFields() {
        if (tableEnfants.getSelectionModel().getSelectedItem() == null) {
            showAlert("Erreur", "Veuillez sélectionner un enfant", AlertType.WARNING);
            return false;
        }
        if (dpDatePresence.getValue() == null) {
            showAlert("Erreur", "La date est obligatoire", AlertType.WARNING);
            dpDatePresence.requestFocus();
            return false;
        }
        return true;
    }

    // ===== CRUD ENFANTS =====
    @FXML
    private void ajouterEnfant() {
        if (!validateEnfantFields()) return;

        try {
            Enfant enfant = new Enfant();
            enfant.setNom(tfNom.getText().trim());
            enfant.setPrenom(tfPrenom.getText().trim());
            enfant.setDateNaissance(dpDateNaissance.getValue());
            enfant.setClasse(tfClasse.getText().trim());
            enfant.setNumTelephoneParent(tfTelephone.getText().trim());
            enfant.setEmailParent(tfEmail.getText().trim());
            enfant.setStatut("ACTIF");
            enfant.setDateInscription(LocalDate.now());

            if (ecoleConnectee != null) {
                enfant.setEcoleId(ecoleConnectee.getId());
            }

            enfantService.insertEnfant(enfant);
            loadEnfants();
            clearEnfantFields();
            chargerStatistiques();
            showAlert("Succès", "Enfant ajouté avec succès!", AlertType.INFORMATION);
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur SQL: " + e.getMessage(), AlertType.ERROR);
        }
    }

    @FXML
    private void modifierEnfant() {
        Enfant selected = tableEnfants.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Info", "Sélectionnez un enfant à modifier", AlertType.WARNING);
            return;
        }

        if (!validateEnfantFields()) return;

        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setContentText("Modifier cet enfant ?");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                selected.setNom(tfNom.getText().trim());
                selected.setPrenom(tfPrenom.getText().trim());
                selected.setDateNaissance(dpDateNaissance.getValue());
                selected.setClasse(tfClasse.getText().trim());
                selected.setNumTelephoneParent(tfTelephone.getText().trim());
                selected.setEmailParent(tfEmail.getText().trim());

                enfantService.updateEnfant(selected);
                loadEnfants();
                clearEnfantFields();
                chargerStatistiques();
                showAlert("Succès", "Enfant modifié avec succès!", AlertType.INFORMATION);
            } catch (SQLException e) {
                showAlert("Erreur", e.getMessage(), AlertType.ERROR);
            }
        }
    }

    @FXML
    private void supprimerEnfant() {
        Enfant selected = tableEnfants.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Info", "Sélectionnez un enfant à supprimer", AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setContentText("Supprimer l'enfant \"" + selected.getNomComplet() + "\" ?");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                enfantService.deleteEnfant(selected.getId());
                loadEnfants();
                clearEnfantFields();
                chargerStatistiques();
                showAlert("Succès", "Enfant supprimé!", AlertType.INFORMATION);
            } catch (SQLException e) {
                showAlert("Erreur", e.getMessage(), AlertType.ERROR);
            }
        }
    }

    // ===== GESTION DES PRÉSENCES =====
    @FXML
    private void enregistrerPresence() {
        if (!validatePresenceFields()) return;

        Enfant selected = tableEnfants.getSelectionModel().getSelectedItem();

        try {
            Presence presence = new Presence();
            presence.setEnfantId(selected.getId());
            presence.setDate(dpDatePresence.getValue());
            presence.setStatutMatin(cbStatutMatin.getValue());

            if (tfHeureArrivee.getText() != null && !tfHeureArrivee.getText().isEmpty()) {
                presence.setHeureArrivee(LocalTime.parse(tfHeureArrivee.getText().trim()));
            }

            presence.setStatutMidi(cbStatutMidi.getValue());

            if (tfHeureDepart.getText() != null && !tfHeureDepart.getText().isEmpty()) {
                presence.setHeureDepart(LocalTime.parse(tfHeureDepart.getText().trim()));
            }

            presence.setStatutSoir(cbStatutSoir.getValue());
            presence.setRemorqueEnfant(chkRemorque.isSelected());
            presence.setRemorqueur(tfRemorqueur.getText().trim());
            presence.setObservations(tfObservations.getText().trim());

            enfantService.ajouterPresence(presence);
            clearPresenceFields();
            chargerStatistiques();
            showAlert("Succès", "Présence enregistrée pour " + selected.getNomComplet(), AlertType.INFORMATION);

        } catch (Exception e) {
            showAlert("Erreur", "Erreur: " + e.getMessage(), AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void voirHistorique() {
        Enfant selected = tableEnfants.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Info", "Sélectionnez un enfant", AlertType.WARNING);
            return;
        }

        try {
            List<Presence> presences = enfantService.selectPresenceByEnfant(selected.getId());

            if (presences.isEmpty()) {
                showAlert("Info", "Aucun historique pour " + selected.getNomComplet(), AlertType.INFORMATION);
                return;
            }

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Historique - " + selected.getNomComplet());

            VBox root = new VBox(15);
            root.setStyle("-fx-padding: 20px; -fx-background-color: white;");
            root.setPrefWidth(800);
            root.setPrefHeight(500);

            Label title = new Label("📋 Historique des présences - " + selected.getNomComplet());
            title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

            TableView<Presence> table = new TableView<>();
            table.setItems(FXCollections.observableArrayList(presences));

            TableColumn<Presence, String> colDate = new TableColumn<>("Date");
            colDate.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getDate().toString()));
            colDate.setPrefWidth(100);

            TableColumn<Presence, String> colMatin = new TableColumn<>("Matin");
            colMatin.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStatutMatin()));
            colMatin.setPrefWidth(80);

            TableColumn<Presence, String> colArrivee = new TableColumn<>("Arrivée");
            colArrivee.setCellValueFactory(data -> {
                LocalTime h = data.getValue().getHeureArrivee();
                return new javafx.beans.property.SimpleStringProperty(h != null ? h.toString() : "-");
            });
            colArrivee.setPrefWidth(80);

            TableColumn<Presence, String> colMidi = new TableColumn<>("Midi");
            colMidi.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStatutMidi()));
            colMidi.setPrefWidth(80);

            TableColumn<Presence, String> colDepart = new TableColumn<>("Départ");
            colDepart.setCellValueFactory(data -> {
                LocalTime h = data.getValue().getHeureDepart();
                return new javafx.beans.property.SimpleStringProperty(h != null ? h.toString() : "-");
            });
            colDepart.setPrefWidth(80);

            TableColumn<Presence, String> colSoir = new TableColumn<>("Soir");
            colSoir.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStatutSoir()));
            colSoir.setPrefWidth(80);

            TableColumn<Presence, String> colRemorque = new TableColumn<>("Remorque");
            colRemorque.setCellValueFactory(data -> {
                boolean r = data.getValue().isRemorqueEnfant();
                return new javafx.beans.property.SimpleStringProperty(r ? data.getValue().getRemorqueur() : "-");
            });
            colRemorque.setPrefWidth(120);

            table.getColumns().addAll(colDate, colMatin, colArrivee, colMidi, colDepart, colSoir, colRemorque);
            table.setPrefHeight(350);

            Button closeBtn = new Button("Fermer");
            closeBtn.setOnAction(e -> stage.close());
            closeBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-padding: 10px 30px; -fx-cursor: hand;");

            root.getChildren().addAll(title, table, closeBtn);
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (SQLException e) {
            showAlert("Erreur", e.getMessage(), AlertType.ERROR);
        }
    }

    // ===== ALERTES =====
    private void showAlert(String title, String message, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}