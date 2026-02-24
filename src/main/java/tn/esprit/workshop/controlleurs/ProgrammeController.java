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
import tn.esprit.workshop.model.Programme;
import tn.esprit.workshop.services.EcoleService;
import tn.esprit.workshop.services.ProgrammeService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ProgrammeController {

    // ===== COMPOSANTS FXML =====
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

    @FXML private Label lblTotalProgrammes;
    @FXML private Label lblPrixMoyen;

    // ===== SERVICES =====
    private final ProgrammeService programmeService = new ProgrammeService();
    private final EcoleService ecoleService = new EcoleService();

    // ===== OBSERVABLE LISTS =====
    private ObservableList<Programme> programmeList = FXCollections.observableArrayList();
    private ObservableList<Ecole> ecoleList = FXCollections.observableArrayList();

    // ===== INITIALISATION =====
    @FXML
    public void initialize() {
        loadEcoles();
        setupTableColumns();
        setupTableSelectionListener();
        loadTable();
        setupNumericValidation();
    }

    // ===== CONFIGURATION DES COLONNES =====
    private void setupTableColumns() {
        colEcole.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(getNomEcole(data.getValue().getEcoleId())));
        colNom.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getNomProgramme()));
        colNiveau.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getNiveau()));
        colDuree.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getDuree()));
        colPrix.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getPrixProgramme()));

        // Formatage de la colonne prix
        colPrix.setCellFactory(tc -> new TableCell<Programme, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f DT", price));
                }
            }
        });
    }

    // ===== LISTENER DE SÉLECTION =====
    private void setupTableSelectionListener() {
        tableProgramme.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                remplirChamps(newSelection);
            }
        });
    }

    // ===== CHARGEMENT DES ÉCOLES =====
    private void loadEcoles() {
        try {
            List<Ecole> list = ecoleService.selectAllEcoles();
            ecoleList.setAll(list);
            cbEcole.setItems(ecoleList);

            // Personnalisation de l'affichage dans le ComboBox
            cbEcole.setCellFactory(param -> new ListCell<Ecole>() {
                @Override
                protected void updateItem(Ecole ecole, boolean empty) {
                    super.updateItem(ecole, empty);
                    if (empty || ecole == null) {
                        setText(null);
                    } else {
                        setText(ecole.getNom());
                    }
                }
            });

            cbEcole.setButtonCell(new ListCell<Ecole>() {
                @Override
                protected void updateItem(Ecole ecole, boolean empty) {
                    super.updateItem(ecole, empty);
                    if (empty || ecole == null) {
                        setText(null);
                    } else {
                        setText(ecole.getNom());
                    }
                }
            });

        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les écoles : " + e.getMessage(), AlertType.ERROR);
        }
    }

    // ===== CHARGEMENT DES PROGRAMMES =====
    private void loadTable() {
        try {
            List<Programme> list = programmeService.selectAllProgrammes();
            programmeList.setAll(list);
            tableProgramme.setItems(programmeList);
            updateStatistics();
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les programmes : " + e.getMessage(), AlertType.ERROR);
        }
    }

    // ===== UTILITAIRES =====
    private String getNomEcole(int id) {
        Ecole e = getEcoleById(id);
        return e != null ? e.getNom() : "Inconnue";
    }

    private Ecole getEcoleById(int id) {
        return ecoleList.stream().filter(e -> e.getId() == id).findFirst().orElse(null);
    }

    // ===== STATISTIQUES =====
    private void updateStatistics() {
        int total = programmeList.size();
        double moyenne = programmeList.stream()
                .mapToDouble(Programme::getPrixProgramme)
                .average()
                .orElse(0.0);

        lblTotalProgrammes.setText("Total programmes: " + total);
        lblPrixMoyen.setText(String.format("Prix moyen: %.2f DT", moyenne));
    }

    // ===== VALIDATION NUMÉRIQUE =====
    private void setupNumericValidation() {
        tfPrix.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                tfPrix.setText(oldValue);
            }
        });

        tfDuree.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                tfDuree.setText(oldValue);
            }
        });
    }

    // ===== REMPLIR LES CHAMPS =====
    private void remplirChamps(Programme p) {
        cbEcole.getSelectionModel().select(getEcoleById(p.getEcoleId()));
        tfNom.setText(p.getNomProgramme());
        tfNiveau.setText(p.getNiveau());
        tfDuree.setText(p.getDuree());
        tfPrix.setText(String.valueOf(p.getPrixProgramme()));
        tfDescription.setText(p.getDescriptionProgramme());
    }

    // ===== VIDER LES CHAMPS =====
    @FXML
    private void clearFields() {
        cbEcole.getSelectionModel().clearSelection();
        tfNom.clear();
        tfNiveau.clear();
        tfDuree.clear();
        tfPrix.clear();
        tfDescription.clear();
        tableProgramme.getSelectionModel().clearSelection();
    }

    // ===== VALIDATION DES CHAMPS =====
    private boolean validateFields() {
        if (cbEcole.getSelectionModel().getSelectedItem() == null) {
            showAlert("Erreur", "Veuillez sélectionner une école !", AlertType.WARNING);
            return false;
        }
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
        if (tfPrix.getText().trim().isEmpty()) {
            showAlert("Erreur", "Le prix est obligatoire !", AlertType.WARNING);
            tfPrix.requestFocus();
            return false;
        }
        return true;
    }

    // ===== AJOUTER UN PROGRAMME =====
    @FXML
    private void ajouterProgramme() {
        if (!validateFields()) return;

        try {
            Ecole selectedEcole = cbEcole.getSelectionModel().getSelectedItem();

            Programme p = new Programme();
            p.setEcoleId(selectedEcole.getId());
            p.setNomProgramme(tfNom.getText().trim());
            p.setNiveau(tfNiveau.getText().trim());
            p.setDuree(tfDuree.getText().trim());
            p.setPrixProgramme(Double.parseDouble(tfPrix.getText().trim()));
            p.setDescriptionProgramme(tfDescription.getText().trim());

            programmeService.insertProgramme(p);
            loadTable();
            clearFields();
            showAlert("Succès", "Programme ajouté avec succès !", AlertType.INFORMATION);

        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de l'ajout : " + e.getMessage(), AlertType.ERROR);
            e.printStackTrace();
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Format de prix invalide !", AlertType.ERROR);
        }
    }

    // ===== MODIFIER UN PROGRAMME =====
    @FXML
    private void modifierProgramme() {
        Programme selected = tableProgramme.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Information", "Veuillez sélectionner un programme à modifier !", AlertType.WARNING);
            return;
        }

        if (!validateFields()) return;

        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Modifier le programme");
        confirm.setContentText("Voulez-vous vraiment modifier ce programme ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Ecole selectedEcole = cbEcole.getSelectionModel().getSelectedItem();
                selected.setEcoleId(selectedEcole.getId());
                selected.setNomProgramme(tfNom.getText().trim());
                selected.setNiveau(tfNiveau.getText().trim());
                selected.setDuree(tfDuree.getText().trim());
                selected.setPrixProgramme(Double.parseDouble(tfPrix.getText().trim()));
                selected.setDescriptionProgramme(tfDescription.getText().trim());

                programmeService.updateProgramme(selected);
                loadTable();
                clearFields();
                showAlert("Succès", "Programme modifié avec succès !", AlertType.INFORMATION);
            } catch (SQLException e) {
                showAlert("Erreur", "Erreur lors de la modification : " + e.getMessage(), AlertType.ERROR);
            } catch (NumberFormatException e) {
                showAlert("Erreur", "Format de prix invalide !", AlertType.ERROR);
            }
        }
    }

    // ===== SUPPRIMER UN PROGRAMME =====
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

    // ===== RETOUR À L'ACCUEIL AGENT =====
    @FXML
    private void retourAccueil() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/workshop/AgentInterface.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) cbEcole.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Interface Agent - Gestion des Écoles");
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de retourner à l'accueil: " + e.getMessage(), AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // ===== FERMETURE DE LA FENÊTRE =====
    @FXML
    private void fermerFenetre() {
        Stage stage = (Stage) cbEcole.getScene().getWindow();
        stage.close();
    }

    // ===== MÉTHODE POUR PRÉ-SÉLECTIONNER UNE ÉCOLE =====
    // 👈👈👈 MÉTHODE APPELÉE DEPUIS AgentController
    public void setEcoleSelectionnee(Ecole ecole) {
        if (ecole != null) {
            cbEcole.getSelectionModel().select(ecole);

            // Optionnel: Filtrer les programmes pour cette école
            ObservableList<Programme> filtered = FXCollections.observableArrayList();
            for (Programme p : programmeList) {
                if (p.getEcoleId() == ecole.getId()) {
                    filtered.add(p);
                }
            }
            tableProgramme.setItems(filtered);
        }
    }

    // ===== AFFICHAGE DES ALERTES =====
    private void showAlert(String title, String message, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}