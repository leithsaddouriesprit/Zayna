package tn.esprit.workshop.controlleurs.amal;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import tn.esprit.workshop.model.amal.Programme;
import tn.esprit.workshop.model.tous.Ecole;
import tn.esprit.workshop.services.amal.ProgrammeService;
import tn.esprit.workshop.services.leith.EcoleService;
import tn.esprit.workshop.utilis.AppSession;
import tn.esprit.workshop.utilis.ZaynaInputConstraints;

import java.sql.SQLException;
import java.util.Optional;

public class ProgrammeController {

    @FXML private Label lblEcoleNom;
    @FXML private TextField tfNom;
    @FXML private TextField tfNiveau;
    @FXML private TextField tfDuree;
    @FXML private TextArea tfDescription;

    @FXML private TableView<Programme> tableProgramme;
    @FXML private TableColumn<Programme, String> colNom;
    @FXML private TableColumn<Programme, String> colNiveau;
    @FXML private TableColumn<Programme, String> colDuree;

    @FXML private Label lblTotalProgrammes;

    private final ProgrammeService programmeService = new ProgrammeService();
    private final EcoleService ecoleService = new EcoleService();

    private ObservableList<Programme> programmeList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        setupTableSelectionListener();
        refreshEcoleHeader();
        loadTable();
        ZaynaInputConstraints.apply(tfDuree, ZaynaInputConstraints.positiveIntegerDigits(ZaynaInputConstraints.MAX_PROGRAMME_DUREE_DIGITS));
    }

    private void refreshEcoleHeader() {
        Integer idEcole = AppSession.getInstance().getEcoleId();
        if (lblEcoleNom == null) {
            return;
        }
        if (idEcole == null) {
            lblEcoleNom.setText("Aucune école associée à la session.");
            return;
        }
        try {
            Ecole e = ecoleService.getById(idEcole);
            String nom = e != null && e.getNomEcole() != null ? e.getNomEcole() : ("École #" + idEcole);
            lblEcoleNom.setText("École : " + nom);
        } catch (SQLException ex) {
            lblEcoleNom.setText("École #" + idEcole);
        }
    }

    private void setupTableColumns() {
        colNom.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getNomProgramme()));
        colNiveau.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getNiveau()));
        colDuree.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getDuree()));
    }

    private void setupTableSelectionListener() {
        tableProgramme.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                remplirChamps(newSelection);
            }
        });
    }

    private void loadTable() {
        Integer idEcole = AppSession.getInstance().getEcoleId();
        try {
            if (idEcole == null) {
                programmeList.clear();
                tableProgramme.setItems(programmeList);
                updateStatistics();
                return;
            }
            programmeList.setAll(programmeService.selectByEcoleId(idEcole));
            tableProgramme.setItems(programmeList);
            updateStatistics();
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les programmes : " + e.getMessage(), AlertType.ERROR);
        }
    }

    private void updateStatistics() {
        lblTotalProgrammes.setText("Total programmes: " + programmeList.size());
    }

    private void remplirChamps(Programme p) {
        tfNom.setText(p.getNomProgramme());
        tfNiveau.setText(p.getNiveau());
        tfDuree.setText(p.getDuree());
        tfDescription.setText(p.getDescriptionProgramme());
    }

    @FXML
    void clearFields() {
        tfNom.clear();
        tfNiveau.clear();
        tfDuree.clear();
        tfDescription.clear();
        tableProgramme.getSelectionModel().clearSelection();
    }

    private boolean validateSessionEcole() {
        if (AppSession.getInstance().getEcoleId() == null) {
            showAlert("Session", "Aucune école n'est associée à votre compte agent.", AlertType.WARNING);
            return false;
        }
        return true;
    }

    private boolean validateFields() {
        if (tfNom.getText().trim().isEmpty()) {
            showAlert("Erreur", "Le nom du programme est obligatoire !", AlertType.WARNING);
            tfNom.requestFocus();
            return false;
        }
        if (tfNiveau.getText().trim().isEmpty()) {
            showAlert("Erreur", "Le niveau est obligatoire !", AlertType.WARNING);
            tfNiveau.requestFocus();
            return false;
        }
        if (tfDuree.getText().trim().isEmpty()) {
            showAlert("Erreur", "La durée est obligatoire !", AlertType.WARNING);
            tfDuree.requestFocus();
            return false;
        }
        String errDuree = ZaynaInputConstraints.validateProgramDuration(tfDuree.getText());
        if (errDuree != null) {
            showAlert("Erreur", errDuree, AlertType.WARNING);
            tfDuree.requestFocus();
            return false;
        }
        return true;
    }

    @FXML
    private void ajouterProgramme() {
        if (!validateSessionEcole() || !validateFields()) {
            return;
        }

        try {
            Programme p = new Programme();
            p.setEcoleId(AppSession.getInstance().getEcoleId());
            p.setNomProgramme(tfNom.getText().trim());
            p.setNiveau(tfNiveau.getText().trim());
            p.setDuree(tfDuree.getText().trim());
            p.setDescriptionProgramme(tfDescription.getText() != null ? tfDescription.getText().trim() : "");

            programmeService.insertProgramme(p);
            loadTable();
            clearFields();
            showAlert("Succès", "Programme ajouté avec succès !", AlertType.INFORMATION);

        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de l'ajout : " + e.getMessage(), AlertType.ERROR);
        }
    }

    @FXML
    private void modifierProgramme() {
        Programme selected = tableProgramme.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Information", "Veuillez sélectionner un programme à modifier !", AlertType.WARNING);
            return;
        }

        if (!validateFields()) {
            return;
        }

        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Modifier le programme");
        confirm.setContentText("Voulez-vous vraiment modifier ce programme ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                selected.setNomProgramme(tfNom.getText().trim());
                selected.setNiveau(tfNiveau.getText().trim());
                selected.setDuree(tfDuree.getText().trim());
                selected.setDescriptionProgramme(tfDescription.getText() != null ? tfDescription.getText().trim() : "");

                programmeService.updateProgramme(selected);
                loadTable();
                clearFields();
                showAlert("Succès", "Programme modifié avec succès !", AlertType.INFORMATION);
            } catch (SQLException e) {
                showAlert("Erreur", "Erreur lors de la modification : " + e.getMessage(), AlertType.ERROR);
            }
        }
    }

    @FXML
    private void supprimerProgramme() {
        Programme selected = tableProgramme.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Information", "Veuillez sélectionner un programme à supprimer !", AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de suppression");
        confirm.setHeaderText("Supprimer le programme");
        confirm.setContentText("Voulez-vous vraiment supprimer le programme \"" +
                selected.getNomProgramme() + "\" ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                programmeService.deleteProgramme(selected);
                loadTable();
                clearFields();
                showAlert("Succès", "Programme supprimé avec succès !", AlertType.INFORMATION);
            } catch (SQLException e) {
                showAlert("Erreur", "Erreur lors de la suppression : " + e.getMessage(), AlertType.ERROR);
            }
        }
    }

    private void showAlert(String title, String message, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
