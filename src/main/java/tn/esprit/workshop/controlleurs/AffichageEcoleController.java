package tn.esprit.workshop.controlleurs;

import tn.esprit.workshop.model.Ecole;

import tn.esprit.workshop.model.Agent;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import tn.esprit.workshop.services.EcoleAffichageService;


import java.sql.SQLException;

    public class AffichageEcoleController {

        @FXML
        private TextField tfSearch;
        @FXML
        private Label lblCount;
        @FXML
        private TableView<Ecole> tableEcoles;
        @FXML
        private TableColumn<Ecole, String> colNom;
        @FXML
        private TableColumn<Ecole, String> colPosition;
        @FXML
        private TableColumn<Ecole, Double> colPrix;
        @FXML
        private TableColumn<Ecole, String> colDescription;
        @FXML
        private TableColumn<Ecole, Integer> colAgentId;

        private final EcoleAffichageService service = new EcoleAffichageService();
        private ObservableList<Ecole> ecoleList = FXCollections.observableArrayList();

        @FXML
        public void initialize() {

            // Configuration des colonnes
            colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
            colPosition.setCellValueFactory(new PropertyValueFactory<>("position"));
            colPrix.setCellValueFactory(new PropertyValueFactory<>("prixMensuel"));
            colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
            colAgentId.setCellValueFactory(new PropertyValueFactory<>("agent_id"));

            // Styles des colonnes
            colNom.setStyle("-fx-font-weight: bold; -fx-background-color: #f8f9fa;");
            colPosition.setStyle("-fx-background-color: #f8f9fa;");
            colPrix.setStyle("-fx-background-color: #e8f5e8; -fx-font-weight: bold;");
            colDescription.setStyle("-fx-background-color: #fff3cd;");
            colAgentId.setStyle("-fx-background-color: #f8d7da;");

            loadTable();
            updateCount();

            // Recherche en temps réel
            tfSearch.textProperty().addListener((obs, oldVal, newVal) -> {
                try {
                    if (newVal.isEmpty()) {
                        loadTable();
                    } else {
                        ecoleList.setAll(service.searchEcoles(newVal));
                        tableEcoles.setItems(ecoleList);
                    }
                    updateCount();
                } catch (SQLException e) {
                    showAlert("Erreur de recherche", e.getMessage());
                }
            });
        }

        // ================= CHARGER LA TABLE =================
        private void loadTable() {
            try {
                ecoleList.setAll(service.selectAllEcoles());
                tableEcoles.setItems(ecoleList);
            } catch (SQLException e) {
                showAlert("Erreur de chargement", e.getMessage());
            }
        }

        // ================= METTRE À JOUR LE COMPTEUR =================
        private void updateCount() {
            try {
                int count = service.countEcoles();
                lblCount.setText("Total des écoles : " + count);
            } catch (SQLException e) {
                lblCount.setText("Erreur de comptage");
            }
        }

        // ================= ACTUALISER =================
        @FXML
        public void refreshTable() {
            loadTable();
            updateCount();
            tfSearch.clear();
            showAlert("Actualisation", "Liste des écoles actualisée !");
        }

        // ================= AFFICHER DÉTAILS =================
        @FXML
        public void showDetails() {
            Ecole selected = tableEcoles.getSelectionModel().getSelectedItem();

            if (selected == null) {
                showAlert("Attention", "Sélectionnez une école !");
                return;
            }

            String details = "Détails de l'école :\n\n" +
                    "Nom : " + selected.getNom() + "\n" +
                    "Position : " + selected.getPosition() + "\n" +
                    "Prix : " + selected.getPrixMensuel() + " DT/mois\n" +
                    "Description : " + selected.getDescription() + "\n" +
                    "Informations : " + selected.getInformations() + "\n" +
                    "ID Agent : " + selected.getAgentId();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Détails de l'école");
            alert.setHeaderText(selected.getNom());
            alert.setContentText(details);
            alert.showAndWait();
        }

        private void showAlert(String title, String message) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }
    }

