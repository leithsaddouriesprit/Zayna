package tn.esprit.workshop.controlleurs;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.esprit.workshop.model.Agent;
import tn.esprit.workshop.services.AgentService;

import java.sql.SQLException;

public class AgentController {

    @FXML
    private TextField tfNom;
    @FXML
    private TextField tfPosition;
    @FXML
    private TextField tfPrix;
    @FXML
    private TextArea tfDescription;
    @FXML
    private TextArea tfInfos;

    @FXML
    private TableView<Agent> tableEcole;
    @FXML
    private TableColumn<Agent, String> colNom;
    @FXML
    private TableColumn<Agent, String> colPosition;
    @FXML
    private TableColumn<Agent, Double> colPrix;
    @FXML
    private TableColumn<Agent, String> colDescription;
    @FXML
    private TableColumn<Agent, String> colInformations;


    private final AgentService service = new AgentService();
    private ObservableList<Agent> ecoleList = FXCollections.observableArrayList();


    @FXML
    public void initialize() {

        colNom.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getNom()));
        colPosition.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getPosition()));
        colPrix.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getPrixMensuel()));
        colDescription.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getDescription()));
        colInformations.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getInformations()));

        loadTable();

        tableEcole.setOnMouseClicked(event -> {
            Agent e = tableEcole.getSelectionModel().getSelectedItem();
            if (e != null) {
                tfNom.setText(e.getNom());
                tfPosition.setText(e.getPosition());
                tfPrix.setText(String.valueOf(e.getPrixMensuel()));
                tfDescription.setText(e.getDescription());
                tfInfos.setText(e.getInformations());
            }
        });
    }
    // ================= AJOUT =================
    @FXML
    public void ajouterEcole() {
        try {
            Agent e = new Agent();

            e.setNom(tfNom.getText());
            e.setPosition(tfPosition.getText());
            e.setPrixMensuel(Double.parseDouble(tfPrix.getText()));
            e.setDescription(tfDescription.getText());
            e.setInformations(tfInfos.getText());

            service.insertOne(e);
            showAlert("Succès", "Ecole ajoutée !");
            loadTable();

        } catch (Exception ex) {
            showAlert("Erreur", ex.getMessage());
        }
    }

    // ================= SUPPRIMER =================
    @FXML
    public void supprimerEcole() {
        Agent selected = tableEcole.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("Attention", "Sélectionnez une école !");
            return;
        }

        try {
            service.deleteOne(selected);
            loadTable();
        } catch (SQLException e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    // ================= MODIFIER =================
    @FXML
    public void modifierEcole() {

        Agent selected = tableEcole.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("Attention", "Choisissez une école !");
            return;
        }

        try {
            selected.setNom(tfNom.getText());
            selected.setPosition(tfPosition.getText());
            selected.setPrixMensuel(Double.parseDouble(tfPrix.getText()));
            selected.setDescription(tfDescription.getText());
            selected.setInformations(tfInfos.getText());

            service.updateOne(selected);
            loadTable();

        } catch (Exception e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    // ================= INITIALIZE =================


    private void loadTable() {
        try {
            ecoleList.setAll(service.selectAll());
            tableEcole.setItems(ecoleList);
        } catch (SQLException e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
