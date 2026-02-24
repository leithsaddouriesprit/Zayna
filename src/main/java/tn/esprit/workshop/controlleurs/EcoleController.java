package tn.esprit.workshop.controlleurs;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.workshop.model.Ecole;
import tn.esprit.workshop.model.Programme;
import tn.esprit.workshop.services.EcoleService;
import tn.esprit.workshop.services.ProgrammeService;

import java.io.IOException;
import java.sql.SQLException;

public class EcoleController {

    @FXML
    private TableColumn<Ecole, Void> colAction;
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
    private TableView<Ecole> tableEcole;
    @FXML
    private TableColumn<Ecole, String> colNom;
    @FXML
    private TableColumn<Ecole, String> colPosition;
    @FXML
    private TableColumn<Ecole, Double> colPrix;
    @FXML
    private TableColumn<Ecole, String> colDescription;
    @FXML
    private TableColumn<Ecole, String> colInformations;

    private final EcoleService service = new EcoleService();
    private final ProgrammeService programmeService = new ProgrammeService();

    private ObservableList<Ecole> ecoleList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Initialisation des colonnes
        colNom.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getNom()));
        colPosition.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getPosition()));
        colPrix.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getPrixMensuel()));
        colDescription.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getDescription()));
        colInformations.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getInformations()));

        // Colonne action → bouton Programme
        colAction.setCellFactory(param -> new TableCell<Ecole, Void>() {
            private final Button btn = new Button("Programme");

            {
                btn.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white; -fx-background-radius: 15;");
                btn.setOnAction(event -> {
                    Ecole e = getTableView().getItems().get(getIndex());
                    openProgrammeWindow(e);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        loadTable();

        tableEcole.setOnMouseClicked(event -> {
            Ecole e = tableEcole.getSelectionModel().getSelectedItem();
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
            Ecole e = new Ecole();
            e.setNom(tfNom.getText());
            e.setPosition(tfPosition.getText());
            e.setPrixMensuel(Double.parseDouble(tfPrix.getText()));
            e.setDescription(tfDescription.getText());
            e.setInformations(tfInfos.getText());

            int idEcole = service.insertOne(e);

            System.out.println("l id de l ecole est"+idEcole);
            // Créer un programme par défaut
            Programme p = new Programme(idEcole, "Programme Primaire", "Programme officiel tunisien", "Primaire", "9 mois", 300);
            programmeService.insertProgramme(p);

            showAlert("Succès", "École et programme ajoutés !");
            loadTable();

        } catch (SQLException ex) {
            showAlert("Erreur SQL", ex.getMessage());
        } catch (NumberFormatException ex) {
            showAlert("Erreur", "Prix invalide !");
        } catch (Exception ex) {
            showAlert("Erreur", ex.getMessage());
        }
    }

    // ================= SUPPRIMER =================
    @FXML
    public void supprimerEcole() {
        Ecole selected = tableEcole.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Attention", "Sélectionnez une école !");
            return;
        }

        try {
            service.deleteOne(selected);
            loadTable();
        } catch (SQLException e) {
            showAlert("Erreur SQL", e.getMessage());
        }
    }

    // ================= MODIFIER =================
    @FXML
    public void modifierEcole() {
        Ecole selected = tableEcole.getSelectionModel().getSelectedItem();
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

        } catch (SQLException e) {
            showAlert("Erreur SQL", e.getMessage());
        } catch (NumberFormatException ex) {
            showAlert("Erreur", "Prix invalide !");
        } catch (Exception e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    // ================= LOAD TABLE =================
    private void loadTable() {
        try {
            ecoleList.setAll(service.selectAll());
            tableEcole.setItems(ecoleList);
        } catch (SQLException e) {
            showAlert("Erreur SQL", e.getMessage());
        }
    }

    // ================= ALERT =================
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ================= OUVRIR PROGRAMME =================
    private void openProgrammeWindow(Ecole ecole) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Programme.fxml"));
            Parent root = loader.load();

            // Pré-sélection de l'école
            tn.esprit.workshop.controlleurs.ProgrammeController controller = loader.getController();
            controller.selectEcole(ecole);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Gestion des Programmes");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}