package tn.esprit.workshop.controlleurs;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.esprit.workshop.model.Ecole;
import tn.esprit.workshop.model.Programme;
import tn.esprit.workshop.services.EcoleService;
import tn.esprit.workshop.services.ProgrammeService;

import java.sql.SQLException;
import java.util.List;

public class ProgrammeController {

    @FXML private ComboBox<Ecole> cbEcole;
    @FXML private TextField tfNom;
    @FXML private TextField tfNiveau;
    @FXML private TextField tfDuree;
    @FXML private TextField tfPrix;
    @FXML private TextArea tfDescription;

    @FXML private TableView<Programme> tableProgramme;
    @FXML private TableColumn<Programme, String> colEcole;
    @FXML private TableColumn<Programme, String> colNom;
    @FXML private TableColumn<Programme, String> colNiveau;
    @FXML private TableColumn<Programme, String> colDuree;
    @FXML private TableColumn<Programme, Double> colPrix;

    private final ProgrammeService programmeService = new ProgrammeService();
    private final EcoleService ecoleService = new EcoleService();

    private ObservableList<Programme> programmeList = FXCollections.observableArrayList();
    private ObservableList<Ecole> ecoleList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadEcoles();

        colEcole.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(getNomEcole(data.getValue().getEcoleId())));
        colNom.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getNomProgramme()));
        colNiveau.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getNiveau()));
        colDuree.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getDuree()));
        colPrix.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getPrixProgramme()));

        tableProgramme.setOnMouseClicked(event -> {
            Programme p = tableProgramme.getSelectionModel().getSelectedItem();
            if (p != null) {
                cbEcole.getSelectionModel().select(getEcoleById(p.getEcoleId()));
                tfNom.setText(p.getNomProgramme());
                tfNiveau.setText(p.getNiveau());
                tfDuree.setText(p.getDuree());
                tfPrix.setText(String.valueOf(p.getPrixProgramme()));
                tfDescription.setText(p.getDescriptionProgramme());
            }
        });

        loadTable();
    }

    private void loadEcoles() {
        try {
            List<Ecole> list = ecoleService.selectAll();
            ecoleList.setAll(list);
            cbEcole.setItems(ecoleList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String getNomEcole(int id) {
        Ecole e = getEcoleById(id);
        return e != null ? e.getNom() : "";
    }

    private Ecole getEcoleById(int id) {
        return ecoleList.stream().filter(e -> e.getId() == id).findFirst().orElse(null);
    }

    private void loadTable() {
        try {
            List<Programme> list = programmeService.selectAllProgrammes();
            programmeList.setAll(list);
            tableProgramme.setItems(programmeList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void ajouterProgramme() {
        try {
            Programme p = new Programme();
            Ecole selectedEcole = cbEcole.getSelectionModel().getSelectedItem();
            if (selectedEcole == null) {
                showAlert("Erreur", "Veuillez sélectionner une école !");
                return;
            }
            p.setEcoleId(selectedEcole.getId());
            p.setNomProgramme(tfNom.getText());
            p.setNiveau(tfNiveau.getText());
            p.setDuree(tfDuree.getText());
            p.setPrixProgramme(Double.parseDouble(tfPrix.getText()));
            p.setDescriptionProgramme(tfDescription.getText());

            programmeService.insertProgramme(p);
            loadTable();
            clearFields();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void modifierProgramme() {
        Programme selected = tableProgramme.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Ecole selectedEcole = cbEcole.getSelectionModel().getSelectedItem();
        if (selectedEcole == null) {
            showAlert("Erreur", "Veuillez sélectionner une école !");
            return;
        }

        selected.setEcoleId(selectedEcole.getId());
        selected.setNomProgramme(tfNom.getText());
        selected.setNiveau(tfNiveau.getText());
        selected.setDuree(tfDuree.getText());
        selected.setPrixProgramme(Double.parseDouble(tfPrix.getText()));
        selected.setDescriptionProgramme(tfDescription.getText());

        try {
            programmeService.updateProgramme(selected);
            loadTable();
            clearFields();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void supprimerProgramme() {
        Programme selected = tableProgramme.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        try {
            programmeService.deleteProgramme(selected);
            loadTable();
            clearFields();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void clearFields() {
        cbEcole.getSelectionModel().clearSelection();
        tfNom.clear();
        tfNiveau.clear();
        tfDuree.clear();
        tfPrix.clear();
        tfDescription.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Méthode pour pré-sélectionner une école depuis EcoleController
    public void selectEcole(Ecole ecole) {
        cbEcole.getSelectionModel().select(ecole);
    }
}
