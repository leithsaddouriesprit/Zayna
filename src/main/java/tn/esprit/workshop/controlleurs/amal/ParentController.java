package tn.esprit.workshop.controlleurs.amal;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.*;
import tn.esprit.workshop.controlleurs.leith.SceneNavigator;
import tn.esprit.workshop.model.amal.Programme;
import tn.esprit.workshop.model.leith.Arret;
import tn.esprit.workshop.model.leith.Trajet;
import tn.esprit.workshop.model.tous.Ecole;
import tn.esprit.workshop.services.amal.ProgrammeService;
import tn.esprit.workshop.services.leith.ArretService;
import tn.esprit.workshop.services.leith.EcoleService;
import tn.esprit.workshop.services.leith.TrajetService;
import tn.esprit.workshop.utilis.AppSession;

import java.sql.SQLException;
import java.util.List;

public class ParentController {

    @FXML private VBox mainPane;
    @FXML private VBox trajetsPane;
    @FXML private ScrollPane trajetsScrollPane;
    @FXML private Label lblTrajetsEcole;

    @FXML private ListView<Ecole> listViewEcoles;

    @FXML private VBox detailsContainer;
    @FXML private VBox messageContainer;
    @FXML private VBox programmesContainer;

    @FXML private Label lblNomEcole;
    @FXML private Label lblAdresseEcole;
    @FXML private Label lblCoordsEcole;

    @FXML private FlowPane programmesFlowPane;

    @FXML private Button btnInscrire;
    @FXML private Button btnVoirTrajets;
    @FXML private Button btnCalendrier;
    @FXML private Button btnMeteo;

    private final EcoleService ecoleService = new EcoleService();
    private final ProgrammeService programmeService = new ProgrammeService();
    private final TrajetService trajetService = new TrajetService();
    private final ArretService arretService = new ArretService();

    private final ObservableList<Ecole> ecoleList = FXCollections.observableArrayList();

    private Ecole ecoleSelectionnee = null;

    @FXML
    public void initialize() {
        if (btnVoirTrajets != null) {
            btnVoirTrajets.setDisable(true);
        }
        setupListView();
        loadEcoles();
        setupListeners();
        Platform.runLater(this::restoreDemandeTransportEcoleSelection);
    }

    /** Rétablit l’école sélectionnée après retour depuis inscription / calendrier / météo (shell). */
    private void restoreDemandeTransportEcoleSelection() {
        Integer id = AppSession.getInstance().getAndClearPendingDemandeTransportEcoleRestoreId();
        if (id == null) {
            return;
        }
        for (Ecole e : ecoleList) {
            if (e.getId() == id) {
                listViewEcoles.getSelectionModel().select(e);
                break;
            }
        }
    }

    private void rememberEcoleForReturnToDemandeTransport() {
        if (ecoleSelectionnee != null) {
            AppSession.getInstance().setPendingDemandeTransportEcoleRestoreId(ecoleSelectionnee.getId());
        }
    }

    private void setupListView() {
        listViewEcoles.setCellFactory(param -> new ListCell<Ecole>() {
            @Override
            protected void updateItem(Ecole ecole, boolean empty) {
                super.updateItem(ecole, empty);
                if (empty || ecole == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox card = new VBox(6);
                    card.getStyleClass().add("card");
                    card.setPadding(new Insets(10));
                    Label nomLabel = new Label(ecole.getNomEcole() != null ? ecole.getNomEcole() : ("École #" + ecole.getId()));
                    nomLabel.getStyleClass().add("title-sm");
                    String adr = ecole.getAdresse();
                    Label adrLabel = new Label(adr != null && !adr.isEmpty() ? adr : "Adresse non renseignée");
                    adrLabel.getStyleClass().add("shell-account");
                    adrLabel.setWrapText(true);
                    card.getChildren().addAll(nomLabel, adrLabel);
                    setGraphic(card);
                }
            }
        });
    }

    private void loadEcoles() {
        try {
            ecoleList.setAll(ecoleService.selectAll());
            listViewEcoles.setItems(ecoleList);
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les écoles : " + e.getMessage(), AlertType.ERROR);
        }
    }

    private void setupListeners() {
        listViewEcoles.getSelectionModel().selectedItemProperty().addListener((obs, old, nouvelleEcole) -> {
            if (nouvelleEcole != null) {
                ecoleSelectionnee = nouvelleEcole;
                afficherDetailsEcole(nouvelleEcole);
                chargerProgrammes(nouvelleEcole.getId());

                detailsContainer.setVisible(true);
                detailsContainer.setManaged(true);
                programmesContainer.setVisible(true);
                programmesContainer.setManaged(true);
                messageContainer.setVisible(false);
                messageContainer.setManaged(false);
                if (btnVoirTrajets != null) {
                    btnVoirTrajets.setDisable(false);
                }
            } else {
                ecoleSelectionnee = null;
                if (btnVoirTrajets != null) {
                    btnVoirTrajets.setDisable(true);
                }
            }
        });
    }

    private void afficherDetailsEcole(Ecole ecole) {
        lblNomEcole.setText(ecole.getNomEcole() != null ? ecole.getNomEcole() : ("École #" + ecole.getId()));
        lblAdresseEcole.setText(ecole.getAdresse() != null && !ecole.getAdresse().isEmpty() ? ecole.getAdresse() : "—");
        if (ecole.getLatitude() != null && ecole.getLongitude() != null) {
            lblCoordsEcole.setText(String.format("%.5f ; %.5f", ecole.getLatitude(), ecole.getLongitude()));
        } else {
            lblCoordsEcole.setText("—");
        }
    }

    private void chargerProgrammes(int ecoleId) {
        programmesFlowPane.getChildren().clear();
        try {
            List<Programme> programmes = programmeService.selectByEcoleId(ecoleId);
            if (programmes.isEmpty()) {
                Label empty = new Label("Aucun programme pour cette école.");
                empty.getStyleClass().add("shell-account");
                programmesFlowPane.getChildren().add(empty);
                return;
            }
            for (Programme p : programmes) {
                programmesFlowPane.getChildren().add(createProgramCard(p));
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les programmes : " + e.getMessage(), AlertType.ERROR);
        }
    }

    private VBox createProgramCard(Programme p) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(14));
        card.getStyleClass().add("card");
        card.setMinWidth(240);
        card.setMaxWidth(280);

        Label titleLabel = new Label(p.getNomProgramme());
        titleLabel.getStyleClass().add("title-sm");
        Label niveauLabel = new Label(p.getNiveau() != null ? p.getNiveau() : "");
        niveauLabel.getStyleClass().add("shell-account");
        Label descriptionLabel = new Label(p.getDescriptionProgramme() != null ? p.getDescriptionProgramme() : "");
        descriptionLabel.setWrapText(true);
        descriptionLabel.getStyleClass().add("detail-value");
        Label dureeText = new Label("Durée : " + (p.getDuree() != null ? p.getDuree() : "—") + " mois");
        dureeText.getStyleClass().add("shell-account");

        card.getChildren().addAll(titleLabel, niveauLabel, new Separator(), descriptionLabel, dureeText);
        return card;
    }

    @FXML
    private void voirTrajets() {
        if (ecoleSelectionnee == null) {
            showAlert("Information", "Veuillez sélectionner une école.", AlertType.WARNING);
            return;
        }
        if (lblTrajetsEcole != null) {
            lblTrajetsEcole.setText("Trajets — " + (ecoleSelectionnee.getNomEcole() != null ? ecoleSelectionnee.getNomEcole() : ""));
        }
        VBox root = new VBox(18);
        root.setPadding(new Insets(4, 0, 8, 0));
        try {
            List<Trajet> trajets = trajetService.selectByEcoleId(ecoleSelectionnee.getId());
            if (trajets.isEmpty()) {
                Label empty = new Label("Aucun trajet disponible pour cette école.");
                empty.getStyleClass().add("shell-account");
                root.getChildren().add(empty);
            } else {
                for (Trajet t : trajets) {
                    root.getChildren().add(buildTrajetSection(t));
                }
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les trajets : " + e.getMessage(), AlertType.ERROR);
            return;
        }
        trajetsScrollPane.setContent(root);
        mainPane.setVisible(false);
        mainPane.setManaged(false);
        trajetsPane.setVisible(true);
        trajetsPane.setManaged(true);
    }

    private VBox buildTrajetSection(Trajet t) throws SQLException {
        VBox box = new VBox(8);
        box.getStyleClass().add("card");
        box.setPadding(new Insets(12));

        String nom = t.getNom() != null ? t.getNom() : ("Trajet #" + t.getTrajetId());
        Label title = new Label(nom);
        title.getStyleClass().add("title-sm");

        String heure = t.getHeureDepart() != null ? t.getHeureDepart().toString() : "—";
        Label line1 = new Label("Départ : " + heure + "   •   Prix : " + String.format("%.2f", t.getPrix()) + " DT");
        line1.getStyleClass().add("detail-value");
        Label line2 = new Label("Statut : " + (t.getStatut() != null ? t.getStatut() : "—"));
        line2.getStyleClass().add("shell-account");

        Label arretsTitle = new Label("Arrêts (ordre)");
        arretsTitle.getStyleClass().add("detail-label");

        VBox arretsBox = new VBox(4);
        List<Arret> arrets = arretService.getArretsByTrajetOrdered(t.getTrajetId());
        if (arrets.isEmpty()) {
            arretsBox.getChildren().add(new Label("Aucun arrêt enregistré."));
        } else {
            for (Arret a : arrets) {
                String h = a.getHeurePrevue() != null ? a.getHeurePrevue() : "—";
                Label al = new Label(a.getOrdre() + ". " + (a.getNom() != null ? a.getNom() : "Arrêt") + " — " + h);
                al.getStyleClass().add("shell-account");
                arretsBox.getChildren().add(al);
            }
        }

        box.getChildren().addAll(title, line1, line2, arretsTitle, arretsBox);
        return box;
    }

    @FXML
    private void retourDemandeTransport() {
        trajetsPane.setVisible(false);
        trajetsPane.setManaged(false);
        mainPane.setVisible(true);
        mainPane.setManaged(true);
    }

    @FXML
    private void voirCalendrier() {
        if (ecoleSelectionnee == null) {
            showAlert("Information", "Veuillez sélectionner une école.", AlertType.WARNING);
            return;
        }
        rememberEcoleForReturnToDemandeTransport();
        SceneNavigator.parentShellNavigateCalendrier();
    }

    @FXML
    private void voirMeteo() {
        if (ecoleSelectionnee == null) {
            showAlert("Information", "Veuillez sélectionner une école.", AlertType.WARNING);
            return;
        }
        rememberEcoleForReturnToDemandeTransport();
        AppSession.getInstance().setPendingMeteoEcoleId(ecoleSelectionnee.getId());
        SceneNavigator.parentShellNavigateMeteo();
    }

    @FXML
    private void inscrireEnfant() {
        if (ecoleSelectionnee == null) {
            showAlert("Information", "Veuillez sélectionner une école.", AlertType.WARNING);
            return;
        }
        int parentId = AppSession.getInstance().getParentId();
        if (parentId <= 0) {
            showAlert("Session", "Compte parent non identifié. Reconnectez-vous.", AlertType.ERROR);
            return;
        }
        List<Trajet> trajets;
        try {
            trajets = trajetService.selectByEcoleId(ecoleSelectionnee.getId());
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les trajets pour cette école.", AlertType.ERROR);
            return;
        }
        if (trajets.isEmpty()) {
            showAlert("Transport",
                    "Aucun trajet n’est disponible pour cette école pour le moment. Vous ne pouvez pas envoyer de candidature sans trajet. Contactez l’établissement.",
                    AlertType.INFORMATION);
            return;
        }
        AppSession.getInstance().setPendingInscriptionEcoleId(ecoleSelectionnee.getId());
        rememberEcoleForReturnToDemandeTransport();
        SceneNavigator.parentShellNavigateInscriptionEnfant();
    }

    private void showAlert(String title, String message, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.setOnShown(ev -> {
            Scene sc = alert.getDialogPane().getScene();
            if (sc != null && sc.getRoot() instanceof Parent) {
                SceneNavigator.applyAppCss(sc, (Parent) sc.getRoot());
            }
        });
        alert.showAndWait();
    }
}
