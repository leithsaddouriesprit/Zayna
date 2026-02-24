package tn.esprit.workshop.controlleurs;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import tn.esprit.workshop.model.Ecole;
import tn.esprit.workshop.services.EcoleService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class AgentController {

    // ===== COMPOSANTS POUR ÉCOLES =====
    @FXML private TextField tfNom;
    @FXML private TextField tfPosition;
    @FXML private TextField tfPrix;
    @FXML private TextArea tfDescription;
    @FXML private TextArea tfInfos;
    @FXML private TableView<Ecole> tableEcole;
    @FXML private TableColumn<Ecole, String> colNom;
    @FXML private TableColumn<Ecole, String> colPosition;
    @FXML private TableColumn<Ecole, Double> colPrix;
    @FXML private TableColumn<Ecole, String> colDescription;
    @FXML private TableColumn<Ecole, String> colInformations;
    @FXML private TableColumn<Ecole, Void> colAction;

    // ===== SERVICES =====
    private final EcoleService ecoleService = new EcoleService();

    // ===== LISTS =====
    private ObservableList<Ecole> ecoleList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupEcoleTable();
        loadEcoles();
    }

    // ===== CONFIGURATION TABLE ÉCOLES =====
    private void setupEcoleTable() {
        colNom.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getNom()));
        colPosition.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getPosition()));
        colPrix.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getPrixMensuel()));
        colDescription.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getDescription()));
        colInformations.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getInformations()));

        // Formatage du prix
        colPrix.setCellFactory(tc -> new TableCell<Ecole, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty || price == null ? null : String.format("%.2f DT", price));
            }
        });

        // Bouton d'action "Programmes" qui ouvre l'interface dédiée
        colAction.setCellFactory(param -> new TableCell<Ecole, Void>() {
            private final Button btn = new Button("📋 Programmes");
            {
                btn.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-background-radius: 15; -fx-cursor: hand; -fx-font-weight: bold; -fx-padding: 5 15;");
                btn.setOnAction(event -> {
                    Ecole ecole = getTableView().getItems().get(getIndex());
                    ouvrirGestionProgrammes(ecole);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        // Sélection dans la table pour remplir les champs
        tableEcole.getSelectionModel().selectedItemProperty().addListener((obs, old, ecole) -> {
            if (ecole != null) {
                tfNom.setText(ecole.getNom());
                tfPosition.setText(ecole.getPosition());
                tfPrix.setText(String.valueOf(ecole.getPrixMensuel()));
                tfDescription.setText(ecole.getDescription());
                tfInfos.setText(ecole.getInformations());
            }
        });
    }

    // ===== CHARGEMENT DES DONNÉES =====
    private void loadEcoles() {
        try {
            List<Ecole> list = ecoleService.selectAllEcoles();
            ecoleList.setAll(list);
            tableEcole.setItems(ecoleList);
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les écoles: " + e.getMessage(), AlertType.ERROR);
        }
    }

    // ===== OUVRIR L'INTERFACE PROGRAMMES AVEC L'ÉCOLE SÉLECTIONNÉE =====
    private void ouvrirGestionProgrammes(Ecole ecole) {
        try {
            // Charger le fichier Programme.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Programme.fxml"));
            Parent root = loader.load();

            // Récupérer le contrôleur et passer l'école sélectionnée
            ProgrammeController programmeController = loader.getController();
            programmeController.setEcoleSelectionnee(ecole);

            // Changer de scène
            Stage stage = (Stage) tableEcole.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestion des Programmes - " + ecole.getNom());
            stage.show();

        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir la gestion des programmes: " + e.getMessage(), AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // ===== VALIDATIONS =====
    private boolean validateEcoleFields() {
        if (tfNom.getText().trim().isEmpty()) {
            showAlert("Erreur", "Le nom de l'école est obligatoire", AlertType.WARNING);
            tfNom.requestFocus();
            return false;
        }
        if (tfPrix.getText().trim().isEmpty()) {
            showAlert("Erreur", "Le prix est obligatoire", AlertType.WARNING);
            tfPrix.requestFocus();
            return false;
        }
        return true;
    }

    // ===== VIDER LES CHAMPS =====
    @FXML
    private void clearFields() {
        tfNom.clear();
        tfPosition.clear();
        tfPrix.clear();
        tfDescription.clear();
        tfInfos.clear();
        tableEcole.getSelectionModel().clearSelection();
    }

    // ===== CRUD ÉCOLES =====
    @FXML
    private void ajouterEcole() {
        if (!validateEcoleFields()) return;

        try {
            Ecole e = new Ecole();
            e.setNom(tfNom.getText().trim());
            e.setPosition(tfPosition.getText().trim());
            e.setPrixMensuel(Double.parseDouble(tfPrix.getText().trim()));
            e.setDescription(tfDescription.getText().trim());
            e.setInformations(tfInfos.getText().trim());

            ecoleService.insertEcole(e);
            loadEcoles();
            clearFields();
            showAlert("Succès", "École ajoutée avec succès!", AlertType.INFORMATION);
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur SQL: " + e.getMessage(), AlertType.ERROR);
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Format de prix invalide", AlertType.ERROR);
        }
    }

    @FXML
    private void modifierEcole() {
        Ecole selected = tableEcole.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Info", "Sélectionnez une école à modifier", AlertType.WARNING);
            return;
        }

        if (!validateEcoleFields()) return;

        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setContentText("Modifier cette école ?");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                selected.setNom(tfNom.getText().trim());
                selected.setPosition(tfPosition.getText().trim());
                selected.setPrixMensuel(Double.parseDouble(tfPrix.getText().trim()));
                selected.setDescription(tfDescription.getText().trim());
                selected.setInformations(tfInfos.getText().trim());

                ecoleService.updateEcole(selected);
                loadEcoles();
                clearFields();
                showAlert("Succès", "École modifiée avec succès!", AlertType.INFORMATION);
            } catch (SQLException e) {
                showAlert("Erreur", e.getMessage(), AlertType.ERROR);
            }
        }
    }

    @FXML
    private void supprimerEcole() {
        Ecole selected = tableEcole.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Info", "Sélectionnez une école à supprimer", AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setContentText("Supprimer l'école \"" + selected.getNom() + "\" ?");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                ecoleService.deleteEcole(selected.getId());
                loadEcoles();
                clearFields();
                showAlert("Succès", "École supprimée!", AlertType.INFORMATION);
            } catch (SQLException e) {
                showAlert("Erreur", e.getMessage(), AlertType.ERROR);
            }
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