package tn.esprit.workshop.controlleurs;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.VBox;  // IMPORTANT: pour VBox
import javafx.stage.Stage;
import tn.esprit.workshop.model.Ecole;
import tn.esprit.workshop.model.Programme;
import tn.esprit.workshop.model.Trajet;
import tn.esprit.workshop.services.EcoleService;
import tn.esprit.workshop.services.ProgrammeService;
import tn.esprit.workshop.services.TrajetService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
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

    // ===== COMPOSANTS POUR PROGRAMMES =====
    @FXML private VBox programmeMessageContainer;
    @FXML private VBox programmeFormContainer;
    @FXML private Label programmeListLabel;
    @FXML private ComboBox<Ecole> cbEcole;
    @FXML private TextField tfNomProgramme;
    @FXML private TextField tfNiveau;
    @FXML private TextField tfDuree;
    @FXML private TextField tfPrixProgramme;
    @FXML private TextArea tfDescriptionProgramme;
    @FXML private TableView<Programme> tableProgramme;
    @FXML private TableColumn<Programme, String> colProgEcole;
    @FXML private TableColumn<Programme, String> colProgNom;
    @FXML private TableColumn<Programme, String> colProgNiveau;
    @FXML private TableColumn<Programme, String> colProgDuree;
    @FXML private TableColumn<Programme, Double> colProgPrix;
    @FXML private TableColumn<Programme, String> colProgDescription;

    // ===== COMPOSANTS POUR TRAJETS =====
    @FXML private VBox trajetMessageContainer;
    @FXML private VBox trajetFormContainer;
    @FXML private Label trajetListLabel;
    @FXML private TableView<Trajet> tableTrajets;
    @FXML private TableColumn<Trajet, String> colTrajetNom;
    @FXML private TableColumn<Trajet, String> colTrajetDepart;
    @FXML private TableColumn<Trajet, String> colTrajetArrivee;
    @FXML private TableColumn<Trajet, String> colTrajetHeureDepart;
    @FXML private TableColumn<Trajet, String> colTrajetHeureArrivee;
    @FXML private TableColumn<Trajet, String> colTrajetJours;
    @FXML private TableColumn<Trajet, Double> colTrajetPrix;
    @FXML private TableColumn<Trajet, Integer> colTrajetPlaces;

    @FXML private TextField tfNomTrajet;
    @FXML private TextField tfPointDepart;
    @FXML private TextField tfPointArrivee;
    @FXML private TextField tfHeureDepart;
    @FXML private TextField tfHeureArrivee;
    @FXML private TextField tfJours;
    @FXML private TextField tfPrixTrajet;
    @FXML private TextField tfPlaces;
    @FXML private TextArea tfDescriptionTrajet;

    // ===== SERVICES =====
    private final EcoleService ecoleService = new EcoleService();
    private final ProgrammeService programmeService = new ProgrammeService();
    private final TrajetService trajetService = new TrajetService();

    // ===== LISTS =====
    private ObservableList<Ecole> ecoleList = FXCollections.observableArrayList();
    private ObservableList<Programme> programmeList = FXCollections.observableArrayList();
    private ObservableList<Trajet> trajetList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupEcoleTable();
        setupProgrammeTable();
        setupTrajetTable();
        loadEcoles();
        loadProgrammes();
        loadTrajets();
        setupListeners();
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

        // Bouton d'action "Programmes"
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

        // Sélection dans la table
        tableEcole.getSelectionModel().selectedItemProperty().addListener((obs, old, ecole) -> {
            if (ecole != null) {
                tfNom.setText(ecole.getNom());
                tfPosition.setText(ecole.getPosition());
                tfPrix.setText(String.valueOf(ecole.getPrixMensuel()));
                tfDescription.setText(ecole.getDescription());
                tfInfos.setText(ecole.getInformations());

                // Activer les onglets programmes et trajets
                activerOngletsPourEcole(ecole);
            }
        });
    }

    // ===== CONFIGURATION TABLE PROGRAMMES =====
    private void setupProgrammeTable() {
        colProgEcole.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(getNomEcole(data.getValue().getEcoleId())));
        colProgNom.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getNomProgramme()));
        colProgNiveau.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getNiveau()));
        colProgDuree.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getDuree()));
        colProgPrix.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getPrixProgramme()));
        colProgDescription.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getDescriptionProgramme()));

        // Formatage du prix
        colProgPrix.setCellFactory(tc -> new TableCell<Programme, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty || price == null ? null : String.format("%.2f DT", price));
            }
        });

        // Sélection dans la table
        tableProgramme.getSelectionModel().selectedItemProperty().addListener((obs, old, prog) -> {
            if (prog != null) {
                cbEcole.getSelectionModel().select(getEcoleById(prog.getEcoleId()));
                tfNomProgramme.setText(prog.getNomProgramme());
                tfNiveau.setText(prog.getNiveau());
                tfDuree.setText(prog.getDuree());
                tfPrixProgramme.setText(String.valueOf(prog.getPrixProgramme()));
                tfDescriptionProgramme.setText(prog.getDescriptionProgramme());
            }
        });
    }

    // ===== CONFIGURATION TABLE TRAJETS =====
    private void setupTrajetTable() {
        colTrajetNom.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getNomTrajet()));
        colTrajetDepart.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getPointDepart()));
        colTrajetArrivee.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getPointArrivee()));
        colTrajetHeureDepart.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getHeureDepart().toString()));
        colTrajetHeureArrivee.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getHeureArrivee().toString()));
        colTrajetJours.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getJours()));
        colTrajetPrix.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getPrixMensuel()));
        colTrajetPlaces.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getPlacesDisponibles()));

        // Formatage du prix
        colTrajetPrix.setCellFactory(tc -> new TableCell<Trajet, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty || price == null ? null : String.format("%.2f DT", price));
            }
        });

        // Sélection dans la table
        tableTrajets.getSelectionModel().selectedItemProperty().addListener((obs, old, trajet) -> {
            if (trajet != null) {
                tfNomTrajet.setText(trajet.getNomTrajet());
                tfPointDepart.setText(trajet.getPointDepart());
                tfPointArrivee.setText(trajet.getPointArrivee());
                tfHeureDepart.setText(trajet.getHeureDepart().toString());
                tfHeureArrivee.setText(trajet.getHeureArrivee().toString());
                tfJours.setText(trajet.getJours());
                tfPrixTrajet.setText(String.valueOf(trajet.getPrixMensuel()));
                tfPlaces.setText(String.valueOf(trajet.getPlacesDisponibles()));
                tfDescriptionTrajet.setText(trajet.getDescription());
            }
        });
    }

    // ===== CHARGEMENT DES DONNÉES =====
    private void loadEcoles() {
        try {
            List<Ecole> list = ecoleService.selectAllEcoles();
            ecoleList.setAll(list);
            tableEcole.setItems(ecoleList);
            cbEcole.setItems(ecoleList);
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les écoles: " + e.getMessage(), AlertType.ERROR);
        }
    }

    private void loadProgrammes() {
        try {
            List<Programme> list = programmeService.selectAllProgrammes();
            programmeList.setAll(list);
            tableProgramme.setItems(programmeList);
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les programmes: " + e.getMessage(), AlertType.ERROR);
        }
    }

    private void loadTrajets() {
        try {
            List<Trajet> list = trajetService.selectAllTrajets();
            trajetList.setAll(list);
            tableTrajets.setItems(trajetList);
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les trajets: " + e.getMessage(), AlertType.ERROR);
        }
    }

    private void loadTrajetsByEcole(int ecoleId) {
        try {
            List<Trajet> list = trajetService.selectTrajetsByEcole(ecoleId);
            trajetList.setAll(list);
            tableTrajets.setItems(trajetList);

            boolean hasTrajets = !list.isEmpty();
            trajetListLabel.setVisible(hasTrajets);
            trajetListLabel.setManaged(hasTrajets);
            tableTrajets.setVisible(hasTrajets);
            tableTrajets.setManaged(hasTrajets);

        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les trajets: " + e.getMessage(), AlertType.ERROR);
        }
    }

    // ===== CONFIGURATION DES LISTENERS =====
    private void setupListeners() {
        // Affichage du ComboBox
        cbEcole.setCellFactory(param -> new ListCell<Ecole>() {
            @Override
            protected void updateItem(Ecole ecole, boolean empty) {
                super.updateItem(ecole, empty);
                setText(empty || ecole == null ? null : ecole.getNom());
            }
        });

        cbEcole.setButtonCell(new ListCell<Ecole>() {
            @Override
            protected void updateItem(Ecole ecole, boolean empty) {
                super.updateItem(ecole, empty);
                setText(empty || ecole == null ? null : ecole.getNom());
            }
        });
    }

    // ===== ACTIVER LES ONGLETS POUR UNE ÉCOLE =====
    private void activerOngletsPourEcole(Ecole ecole) {
        // Activer l'onglet programmes
        if (programmeMessageContainer != null && programmeFormContainer != null) {
            programmeMessageContainer.setVisible(false);
            programmeMessageContainer.setManaged(false);
            programmeFormContainer.setVisible(true);
            programmeFormContainer.setManaged(true);
        }

        // Charger les programmes de cette école
        ObservableList<Programme> programmesEcole = FXCollections.observableArrayList();
        for (Programme p : programmeList) {
            if (p.getEcoleId() == ecole.getId()) {
                programmesEcole.add(p);
            }
        }

        boolean hasProgrammes = !programmesEcole.isEmpty();
        if (programmeListLabel != null && tableProgramme != null) {
            programmeListLabel.setVisible(hasProgrammes);
            programmeListLabel.setManaged(hasProgrammes);
            tableProgramme.setVisible(hasProgrammes);
            tableProgramme.setManaged(hasProgrammes);
            tableProgramme.setItems(programmesEcole);
        }

        // Activer l'onglet trajets
        if (trajetMessageContainer != null && trajetFormContainer != null) {
            trajetMessageContainer.setVisible(false);
            trajetMessageContainer.setManaged(false);
            trajetFormContainer.setVisible(true);
            trajetFormContainer.setManaged(true);
        }

        // Charger les trajets de cette école
        loadTrajetsByEcole(ecole.getId());
    }

    // ===== UTILITAIRES =====
    private String getNomEcole(int id) {
        Ecole e = getEcoleById(id);
        return e != null ? e.getNom() : "Inconnue";
    }

    private Ecole getEcoleById(int id) {
        return ecoleList.stream().filter(e -> e.getId() == id).findFirst().orElse(null);
    }

    // ===== OUVRIR L'INTERFACE PROGRAMMES =====
    private void ouvrirGestionProgrammes(Ecole ecole) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Programme.fxml"));
            Parent root = loader.load();

            ProgrammeController programmeController = loader.getController();
            programmeController.setEcoleSelectionnee(ecole);

            Stage stage = (Stage) tableEcole.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestion des Programmes - " + ecole.getNom());
            stage.show();

        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir la gestion des programmes: " + e.getMessage(), AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // ===== VALIDATIONS ÉCOLES =====
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

    // ===== VALIDATIONS PROGRAMMES =====
    private boolean validateProgrammeFields() {
        if (cbEcole.getValue() == null) {
            showAlert("Erreur", "Veuillez sélectionner une école", AlertType.WARNING);
            return false;
        }
        if (tfNomProgramme.getText().trim().isEmpty()) {
            showAlert("Erreur", "Le nom du programme est obligatoire", AlertType.WARNING);
            tfNomProgramme.requestFocus();
            return false;
        }
        if (tfPrixProgramme.getText().trim().isEmpty()) {
            showAlert("Erreur", "Le prix est obligatoire", AlertType.WARNING);
            tfPrixProgramme.requestFocus();
            return false;
        }
        return true;
    }

    // ===== VALIDATIONS TRAJETS =====
    private boolean validateTrajetFields() {
        if (tfNomTrajet.getText().trim().isEmpty()) {
            showAlert("Erreur", "Le nom du trajet est obligatoire", AlertType.WARNING);
            tfNomTrajet.requestFocus();
            return false;
        }
        if (tfPointDepart.getText().trim().isEmpty()) {
            showAlert("Erreur", "Le point de départ est obligatoire", AlertType.WARNING);
            tfPointDepart.requestFocus();
            return false;
        }
        if (tfPointArrivee.getText().trim().isEmpty()) {
            showAlert("Erreur", "Le point d'arrivée est obligatoire", AlertType.WARNING);
            tfPointArrivee.requestFocus();
            return false;
        }
        if (tfHeureDepart.getText().trim().isEmpty()) {
            showAlert("Erreur", "L'heure de départ est obligatoire", AlertType.WARNING);
            tfHeureDepart.requestFocus();
            return false;
        }
        if (tfHeureArrivee.getText().trim().isEmpty()) {
            showAlert("Erreur", "L'heure d'arrivée est obligatoire", AlertType.WARNING);
            tfHeureArrivee.requestFocus();
            return false;
        }
        return true;
    }

    // ===== VIDER LES CHAMPS =====
    @FXML
    private void clearEcoleFields() {
        tfNom.clear();
        tfPosition.clear();
        tfPrix.clear();
        tfDescription.clear();
        tfInfos.clear();
        tableEcole.getSelectionModel().clearSelection();
    }

    @FXML
    private void clearProgrammeFields() {
        cbEcole.setValue(null);
        tfNomProgramme.clear();
        tfNiveau.clear();
        tfDuree.clear();
        tfPrixProgramme.clear();
        tfDescriptionProgramme.clear();
        tableProgramme.getSelectionModel().clearSelection();
    }

    @FXML
    private void clearTrajetFields() {
        tfNomTrajet.clear();
        tfPointDepart.clear();
        tfPointArrivee.clear();
        tfHeureDepart.clear();
        tfHeureArrivee.clear();
        tfJours.clear();
        tfPrixTrajet.clear();
        tfPlaces.setText("30");
        tfDescriptionTrajet.clear();
        tableTrajets.getSelectionModel().clearSelection();
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
            clearEcoleFields();
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
                clearEcoleFields();
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
                clearEcoleFields();
                showAlert("Succès", "École supprimée!", AlertType.INFORMATION);
            } catch (SQLException e) {
                showAlert("Erreur", e.getMessage(), AlertType.ERROR);
            }
        }
    }

    // ===== CRUD PROGRAMMES =====
    @FXML
    private void ajouterProgramme() {
        if (!validateProgrammeFields()) return;

        try {
            Programme p = new Programme();
            p.setEcoleId(cbEcole.getValue().getId());
            p.setNomProgramme(tfNomProgramme.getText().trim());
            p.setNiveau(tfNiveau.getText().trim());
            p.setDuree(tfDuree.getText().trim());
            p.setPrixProgramme(Double.parseDouble(tfPrixProgramme.getText().trim()));
            p.setDescriptionProgramme(tfDescriptionProgramme.getText().trim());

            programmeService.insertProgramme(p);
            loadProgrammes();
            clearProgrammeFields();

            // Recharger les programmes de l'école sélectionnée
            Ecole selectedEcole = tableEcole.getSelectionModel().getSelectedItem();
            if (selectedEcole != null) {
                activerOngletsPourEcole(selectedEcole);
            }

            showAlert("Succès", "Programme ajouté!", AlertType.INFORMATION);
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur SQL: " + e.getMessage(), AlertType.ERROR);
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Format de prix invalide", AlertType.ERROR);
        }
    }

    @FXML
    private void modifierProgramme() {
        Programme selected = tableProgramme.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Info", "Sélectionnez un programme à modifier", AlertType.WARNING);
            return;
        }

        if (!validateProgrammeFields()) return;

        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setContentText("Modifier ce programme ?");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                selected.setEcoleId(cbEcole.getValue().getId());
                selected.setNomProgramme(tfNomProgramme.getText().trim());
                selected.setNiveau(tfNiveau.getText().trim());
                selected.setDuree(tfDuree.getText().trim());
                selected.setPrixProgramme(Double.parseDouble(tfPrixProgramme.getText().trim()));
                selected.setDescriptionProgramme(tfDescriptionProgramme.getText().trim());

                programmeService.updateProgramme(selected);
                loadProgrammes();
                clearProgrammeFields();

                Ecole selectedEcole = tableEcole.getSelectionModel().getSelectedItem();
                if (selectedEcole != null) {
                    activerOngletsPourEcole(selectedEcole);
                }

                showAlert("Succès", "Programme modifié!", AlertType.INFORMATION);
            } catch (SQLException e) {
                showAlert("Erreur", e.getMessage(), AlertType.ERROR);
            }
        }
    }

    @FXML
    private void supprimerProgramme() {
        Programme selected = tableProgramme.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Info", "Sélectionnez un programme à supprimer", AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setContentText("Supprimer ce programme ?");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                programmeService.deleteProgramme(selected);
                loadProgrammes();
                clearProgrammeFields();

                Ecole selectedEcole = tableEcole.getSelectionModel().getSelectedItem();
                if (selectedEcole != null) {
                    activerOngletsPourEcole(selectedEcole);
                }

                showAlert("Succès", "Programme supprimé!", AlertType.INFORMATION);
            } catch (SQLException e) {
                showAlert("Erreur", e.getMessage(), AlertType.ERROR);
            }
        }
    }

    // ===== CRUD TRAJETS =====
    @FXML
    private void ajouterTrajet() {
        Ecole selectedEcole = tableEcole.getSelectionModel().getSelectedItem();
        if (selectedEcole == null) {
            showAlert("Erreur", "Veuillez sélectionner une école d'abord", AlertType.WARNING);
            return;
        }

        if (!validateTrajetFields()) return;

        try {
            Trajet t = new Trajet();
            t.setEcoleId(selectedEcole.getId());
            t.setNomTrajet(tfNomTrajet.getText().trim());
            t.setPointDepart(tfPointDepart.getText().trim());
            t.setPointArrivee(tfPointArrivee.getText().trim());
            t.setHeureDepart(LocalTime.parse(tfHeureDepart.getText().trim()));
            t.setHeureArrivee(LocalTime.parse(tfHeureArrivee.getText().trim()));
            t.setJours(tfJours.getText().trim());
            t.setPrixMensuel(Double.parseDouble(tfPrixTrajet.getText().trim()));
            t.setPlacesDisponibles(Integer.parseInt(tfPlaces.getText().trim()));
            t.setDescription(tfDescriptionTrajet.getText().trim());

            trajetService.insertTrajet(t);
            loadTrajets();
            clearTrajetFields();
            loadTrajetsByEcole(selectedEcole.getId());
            showAlert("Succès", "Trajet ajouté avec succès!", AlertType.INFORMATION);
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur SQL: " + e.getMessage(), AlertType.ERROR);
        } catch (DateTimeParseException e) {
            showAlert("Erreur", "Format d'heure invalide (utilisez HH:MM)", AlertType.ERROR);
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Format de nombre invalide", AlertType.ERROR);
        }
    }

    @FXML
    private void modifierTrajet() {
        Trajet selected = tableTrajets.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Info", "Sélectionnez un trajet à modifier", AlertType.WARNING);
            return;
        }

        if (!validateTrajetFields()) return;

        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setContentText("Modifier ce trajet ?");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                selected.setNomTrajet(tfNomTrajet.getText().trim());
                selected.setPointDepart(tfPointDepart.getText().trim());
                selected.setPointArrivee(tfPointArrivee.getText().trim());
                selected.setHeureDepart(LocalTime.parse(tfHeureDepart.getText().trim()));
                selected.setHeureArrivee(LocalTime.parse(tfHeureArrivee.getText().trim()));
                selected.setJours(tfJours.getText().trim());
                selected.setPrixMensuel(Double.parseDouble(tfPrixTrajet.getText().trim()));
                selected.setPlacesDisponibles(Integer.parseInt(tfPlaces.getText().trim()));
                selected.setDescription(tfDescriptionTrajet.getText().trim());

                trajetService.updateTrajet(selected);
                loadTrajets();
                clearTrajetFields();

                Ecole selectedEcole = tableEcole.getSelectionModel().getSelectedItem();
                if (selectedEcole != null) {
                    loadTrajetsByEcole(selectedEcole.getId());
                }

                showAlert("Succès", "Trajet modifié!", AlertType.INFORMATION);
            } catch (SQLException e) {
                showAlert("Erreur", e.getMessage(), AlertType.ERROR);
            } catch (DateTimeParseException e) {
                showAlert("Erreur", "Format d'heure invalide", AlertType.ERROR);
            }
        }
    }

    @FXML
    private void supprimerTrajet() {
        Trajet selected = tableTrajets.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Info", "Sélectionnez un trajet à supprimer", AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setContentText("Supprimer ce trajet ?");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                trajetService.deleteTrajet(selected.getId());
                loadTrajets();
                clearTrajetFields();

                Ecole selectedEcole = tableEcole.getSelectionModel().getSelectedItem();
                if (selectedEcole != null) {
                    loadTrajetsByEcole(selectedEcole.getId());
                }

                showAlert("Succès", "Trajet supprimé!", AlertType.INFORMATION);
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