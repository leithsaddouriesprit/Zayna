package tn.esprit.workshop.controlleurs;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.workshop.model.Ecole;
import tn.esprit.workshop.model.Programme;
import tn.esprit.workshop.model.Trajet;
import tn.esprit.workshop.model.JourFerieApi;
import tn.esprit.workshop.model.Meteo;
import tn.esprit.workshop.services.EcoleService;
import tn.esprit.workshop.services.ProgrammeService;
import tn.esprit.workshop.services.TrajetService;
import tn.esprit.workshop.services.JourFerieApiService;
import tn.esprit.workshop.services.MeteoService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class ParentController {

    // ===== COMPOSANTS FXML =====
    @FXML private ListView<Ecole> listViewEcoles;

    @FXML private VBox detailsContainer;
    @FXML private VBox messageContainer;
    @FXML private VBox programmesContainer;

    @FXML private Label lblNomEcole;
    @FXML private Label lblPositionEcole;
    @FXML private Label lblPrixEcole;
    @FXML private Label lblDescriptionEcole;
    @FXML private Label lblInfosEcole;

    @FXML private FlowPane programmesFlowPane;

    @FXML private Button btnInscrire;
    @FXML private Button btnVoirTrajets;
    @FXML private Button btnCalendrier;
    @FXML private Button btnMeteo;  // NOUVEAU

    // ===== SERVICES =====
    private final EcoleService ecoleService = new EcoleService();
    private final ProgrammeService programmeService = new ProgrammeService();
    private final TrajetService trajetService = new TrajetService();
    private final JourFerieApiService jourFerieApiService = new JourFerieApiService();
    private final MeteoService meteoService = new MeteoService();  // NOUVEAU

    // ===== LISTS =====
    private ObservableList<Ecole> ecoleList = FXCollections.observableArrayList();

    private Ecole ecoleSelectionnee = null;

    @FXML
    public void initialize() {
        setupListView();
        loadEcoles();
        setupListeners();
    }

    // ===== CONFIGURATION DE LA LISTE =====
    private void setupListView() {
        listViewEcoles.setCellFactory(param -> new ListCell<Ecole>() {
            @Override
            protected void updateItem(Ecole ecole, boolean empty) {
                super.updateItem(ecole, empty);

                if (empty || ecole == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox card = new VBox(8);
                    card.setStyle("-fx-padding: 15px; -fx-background-color: white; -fx-background-radius: 12px; -fx-border-color: #e0e7ed; -fx-border-width: 1px; -fx-border-radius: 12px;");

                    Label nomLabel = new Label(ecole.getNom());
                    nomLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

                    HBox locationBox = new HBox(5);
                    Label locationIcon = new Label("📍");
                    locationIcon.setStyle("-fx-font-size: 14px;");
                    Label locationText = new Label(ecole.getPosition() != null ? ecole.getPosition() : "Position non spécifiée");
                    locationText.setStyle("-fx-font-size: 13px; -fx-text-fill: #34495e;");
                    locationBox.getChildren().addAll(locationIcon, locationText);

                    Label prixLabel = new Label(String.format("💰 %.2f DT/mois", ecole.getPrixMensuel()));
                    prixLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");

                    card.getChildren().addAll(nomLabel, locationBox, prixLabel);
                    setGraphic(card);
                }
            }
        });
    }

    // ===== CHARGEMENT DES ÉCOLES =====
    private void loadEcoles() {
        try {
            List<Ecole> list = ecoleService.selectAllEcoles();
            ecoleList.setAll(list);
            listViewEcoles.setItems(ecoleList);
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les écoles: " + e.getMessage(), AlertType.ERROR);
        }
    }

    // ===== LISTENER DE SÉLECTION =====
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
            }
        });
    }

    // ===== AFFICHER LES DÉTAILS D'UNE ÉCOLE =====
    private void afficherDetailsEcole(Ecole ecole) {
        lblNomEcole.setText(ecole.getNom());
        lblPositionEcole.setText(ecole.getPosition() != null ? ecole.getPosition() : "Non spécifiée");
        lblPrixEcole.setText(String.format("%.2f DT/mois", ecole.getPrixMensuel()));
        lblDescriptionEcole.setText(ecole.getDescription() != null && !ecole.getDescription().isEmpty() ?
                ecole.getDescription() : "Aucune description");
        lblInfosEcole.setText(ecole.getInformations() != null && !ecole.getInformations().isEmpty() ?
                ecole.getInformations() : "Aucune information supplémentaire");
    }

    // ===== CHARGER LES PROGRAMMES SOUS FORME DE CARTES =====
    private void chargerProgrammes(int ecoleId) {
        programmesFlowPane.getChildren().clear();

        try {
            List<Programme> tousLesProgrammes = programmeService.selectAllProgrammes();

            for (Programme p : tousLesProgrammes) {
                if (p.getEcoleId() == ecoleId) {
                    VBox programCard = createProgramCard(p);
                    programmesFlowPane.getChildren().add(programCard);
                }
            }

            if (programmesFlowPane.getChildren().isEmpty()) {
                Label emptyLabel = new Label("Aucun programme disponible pour cette école");
                emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d; -fx-padding: 30px;");
                programmesFlowPane.getChildren().add(emptyLabel);
            }

        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les programmes: " + e.getMessage(), AlertType.ERROR);
        }
    }

    // ===== CRÉER UNE CARTE DE PROGRAMME =====
    private VBox createProgramCard(Programme p) {
        VBox card = new VBox(15);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 20px;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);" +
                        "-fx-padding: 20px;" +
                        "-fx-border-color: #3498db;" +
                        "-fx-border-width: 2px;" +
                        "-fx-border-radius: 20px;" +
                        "-fx-min-width: 280px;" +
                        "-fx-max-width: 280px;"
        );

        // Effet hover
        card.setOnMouseEntered(e ->
                card.setStyle(
                        "-fx-background-color: white;" +
                                "-fx-background-radius: 20px;" +
                                "-fx-effect: dropshadow(gaussian, #3498db, 15, 0, 0, 5);" +
                                "-fx-padding: 20px;" +
                                "-fx-border-color: #2980b9;" +
                                "-fx-border-width: 2px;" +
                                "-fx-border-radius: 20px;" +
                                "-fx-min-width: 280px;" +
                                "-fx-max-width: 280px;" +
                                "-fx-scale-x: 1.02;" +
                                "-fx-scale-y: 1.02;"
                )
        );

        card.setOnMouseExited(e ->
                card.setStyle(
                        "-fx-background-color: white;" +
                                "-fx-background-radius: 20px;" +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);" +
                                "-fx-padding: 20px;" +
                                "-fx-border-color: #3498db;" +
                                "-fx-border-width: 2px;" +
                                "-fx-border-radius: 20px;" +
                                "-fx-min-width: 280px;" +
                                "-fx-max-width: 280px;"
                )
        );

        // En-tête avec icône et titre
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label("📘");
        iconLabel.setStyle(
                "-fx-font-size: 36px;" +
                        "-fx-background-color: #3498db;" +
                        "-fx-background-radius: 50%;" +
                        "-fx-padding: 10px;" +
                        "-fx-text-fill: white;"
        );

        VBox titleBox = new VBox(5);
        Label titleLabel = new Label(p.getNomProgramme());
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label niveauLabel = new Label(p.getNiveau());
        niveauLabel.setStyle(
                "-fx-background-color: #e74c3c;" +
                        "-fx-background-radius: 20px;" +
                        "-fx-padding: 4px 12px;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 12px;"
        );

        titleBox.getChildren().addAll(titleLabel, niveauLabel);
        header.getChildren().addAll(iconLabel, titleBox);

        // Description
        Label descriptionLabel = new Label(p.getDescriptionProgramme());
        descriptionLabel.setWrapText(true);
        descriptionLabel.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-text-fill: #2c3e50;" +
                        "-fx-padding: 10px 0;" +
                        "-fx-font-weight: 500;"
        );

        // Ligne de séparation
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #ecf0f1; -fx-padding: 5px 0;");

        // Détails (durée)
        HBox dureeBox = new HBox(10);
        dureeBox.setAlignment(Pos.CENTER_LEFT);

        Label dureeIcon = new Label("⏱️");
        dureeIcon.setStyle("-fx-font-size: 18px;");

        Label dureeText = new Label("Durée: " + p.getDuree() + " mois");
        dureeText.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #34495e;");

        dureeBox.getChildren().addAll(dureeIcon, dureeText);

        card.getChildren().addAll(header, descriptionLabel, separator, dureeBox);

        return card;
    }

    // ===== VOIR LES TRAJETS DANS UNE FENÊTRE POPUP =====
    @FXML
    private void voirTrajets() {
        if (ecoleSelectionnee == null) {
            showAlert("Information", "Veuillez sélectionner une école", AlertType.WARNING);
            return;
        }

        try {
            // Récupérer les trajets de l'école
            List<Trajet> trajets = trajetService.selectTrajetsByEcole(ecoleSelectionnee.getId());

            if (trajets.isEmpty()) {
                showAlert("Information", "Aucun trajet disponible pour cette école", AlertType.INFORMATION);
                return;
            }

            // Créer une nouvelle fenêtre (popup)
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setTitle("Trajets disponibles - " + ecoleSelectionnee.getNom());

            // Conteneur principal
            VBox root = new VBox(20);
            root.setStyle("-fx-background-color: white; -fx-padding: 25px; -fx-background-radius: 15px;");
            root.setPrefWidth(600);
            root.setPrefHeight(400);

            // Titre
            Label titleLabel = new Label("🚌 Trajets disponibles pour " + ecoleSelectionnee.getNom());
            titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 0 0 15px 0; -fx-border-width: 0 0 2px 0; -fx-border-color: #f39c12;");

            // Liste des trajets
            ListView<Trajet> trajetsList = new ListView<>();
            trajetsList.setItems(FXCollections.observableArrayList(trajets));
            trajetsList.setPrefHeight(300);

            // Personnalisation de l'affichage des trajets
            trajetsList.setCellFactory(param -> new ListCell<Trajet>() {
                @Override
                protected void updateItem(Trajet trajet, boolean empty) {
                    super.updateItem(trajet, empty);

                    if (empty || trajet == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        VBox card = new VBox(8);
                        card.setStyle("-fx-padding: 15px; -fx-background-color: #fef5e7; -fx-background-radius: 12px; -fx-border-color: #f39c12; -fx-border-width: 1px; -fx-border-radius: 12px;");

                        // Nom du trajet
                        Label nomLabel = new Label(trajet.getNomTrajet());
                        nomLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

                        // Trajet (départ → arrivée)
                        HBox routeBox = new HBox(5);
                        Label routeIcon = new Label("🔄");
                        routeIcon.setStyle("-fx-font-size: 14px;");
                        Label routeText = new Label(trajet.getPointDepart() + " → " + trajet.getPointArrivee());
                        routeText.setStyle("-fx-font-size: 14px; -fx-text-fill: #34495e;");
                        routeBox.getChildren().addAll(routeIcon, routeText);

                        // Horaires
                        HBox timeBox = new HBox(5);
                        Label timeIcon = new Label("⏰");
                        timeIcon.setStyle("-fx-font-size: 14px;");
                        Label timeText = new Label(trajet.getHeureDepart().toString() + " - " + trajet.getHeureArrivee().toString());
                        timeText.setStyle("-fx-font-size: 14px; -fx-text-fill: #34495e;");
                        timeBox.getChildren().addAll(timeIcon, timeText);

                        // Jours
                        HBox joursBox = new HBox(5);
                        Label joursIcon = new Label("📅");
                        joursIcon.setStyle("-fx-font-size: 14px;");
                        Label joursText = new Label(trajet.getJours());
                        joursText.setStyle("-fx-font-size: 14px; -fx-text-fill: #34495e;");
                        joursBox.getChildren().addAll(joursIcon, joursText);

                        // Prix et places
                        HBox infoBox = new HBox(20);

                        Label prixLabel = new Label(String.format("💰 %.2f DT/mois", trajet.getPrixMensuel()));
                        prixLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #e67e22;");

                        Label placesLabel = new Label("🪑 " + trajet.getPlacesDisponibles() + " places");
                        if (trajet.getPlacesDisponibles() < 10) {
                            placesLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");
                        } else {
                            placesLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");
                        }

                        infoBox.getChildren().addAll(prixLabel, placesLabel);

                        card.getChildren().addAll(nomLabel, routeBox, timeBox, joursBox, infoBox);

                        // Description si elle existe
                        if (trajet.getDescription() != null && !trajet.getDescription().isEmpty()) {
                            Label descLabel = new Label(trajet.getDescription());
                            descLabel.setWrapText(true);
                            descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d; -fx-padding: 5px 0 0 0;");
                            card.getChildren().add(descLabel);
                        }

                        setGraphic(card);
                    }
                }
            });

            // Bouton fermer
            Button closeButton = new Button("Fermer");
            closeButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px 30px; -fx-background-radius: 25px; -fx-cursor: hand;");
            closeButton.setOnAction(e -> popupStage.close());

            root.getChildren().addAll(titleLabel, trajetsList, closeButton);

            Scene scene = new Scene(root);
            popupStage.setScene(scene);
            popupStage.showAndWait();

        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les trajets: " + e.getMessage(), AlertType.ERROR);
        }
    }

    // ===== VOIR LE CALENDRIER DES JOURS FÉRIÉS =====
    @FXML
    private void voirCalendrier() {
        // Créer une fenêtre de chargement
        Stage loadingStage = new Stage();
        loadingStage.initModality(Modality.APPLICATION_MODAL);
        loadingStage.setTitle("Chargement");
        loadingStage.setResizable(false);

        VBox loadingRoot = new VBox(20);
        loadingRoot.setAlignment(Pos.CENTER);
        loadingRoot.setStyle("-fx-background-color: white; -fx-padding: 30px; -fx-background-radius: 15px;");

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(60, 60);

        Label loadingLabel = new Label("Récupération des jours fériés...");
        loadingLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label apiLabel = new Label("Connexion à l'API Nager.Date");
        apiLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");

        loadingRoot.getChildren().addAll(progressIndicator, loadingLabel, apiLabel);

        Scene loadingScene = new Scene(loadingRoot, 320, 220);
        loadingStage.setScene(loadingScene);
        loadingStage.show();

        // Charger les données dans un thread séparé
        new Thread(() -> {
            try {
                Thread.sleep(500);

                // Récupérer les données pour 2025 et 2026
                List<JourFerieApi> joursFeries2025 = jourFerieApiService.getJoursFeries(2025);
                List<JourFerieApi> joursFeries2026 = jourFerieApiService.getJoursFeries(2026);

                javafx.application.Platform.runLater(() -> {
                    loadingStage.close();
                    afficherFenetreCalendrier(joursFeries2025, joursFeries2026);
                });

            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    loadingStage.close();
                    showAlert("Erreur", "Impossible de charger le calendrier: " + e.getMessage(), AlertType.ERROR);
                    e.printStackTrace();
                });
            }
        }).start();
    }

    // ===== AFFICHER LA FENÊTRE DU CALENDRIER =====
    private void afficherFenetreCalendrier(List<JourFerieApi> jours2025, List<JourFerieApi> jours2026) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Calendrier scolaire - Jours fériés Tunisie");
        stage.setResizable(false);

        VBox root = new VBox(15);
        root.setStyle("-fx-background-color: white; -fx-padding: 25px; -fx-background-radius: 15px;");
        root.setPrefWidth(800);
        root.setPrefHeight(500);

        Label titleLabel = new Label("📅 Jours fériés en Tunisie");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 0 0 15px 0; -fx-border-width: 0 0 2px 0; -fx-border-color: #2ecc71;");

        Label sourceLabel = new Label("Source: Nager.Date API");
        sourceLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d; -fx-padding: 0 0 10px 0;");

        Label countLabel = new Label("2025: " + jours2025.size() + " jours • 2026: " + jours2026.size() + " jours");
        countLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #34495e; -fx-padding: 5px 0;");

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab tab2025 = new Tab("2025");
        tab2025.setContent(creerTableauJoursFeries(jours2025));
        tab2025.setClosable(false);

        Tab tab2026 = new Tab("2026");
        tab2026.setContent(creerTableauJoursFeries(jours2026));
        tab2026.setClosable(false);

        tabPane.getTabs().addAll(tab2025, tab2026);

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        Button refreshButton = new Button("🔄 Rafraîchir");
        refreshButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px 25px; -fx-background-radius: 25px; -fx-cursor: hand;");
        refreshButton.setOnAction(e -> {
            stage.close();
            voirCalendrier();
        });

        Button closeButton = new Button("Fermer");
        closeButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px 40px; -fx-background-radius: 25px; -fx-cursor: hand;");
        closeButton.setOnAction(e -> stage.close());

        buttonBox.getChildren().addAll(refreshButton, closeButton);

        root.getChildren().addAll(titleLabel, sourceLabel, countLabel, tabPane, buttonBox);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.showAndWait();
    }

    // ===== CRÉER UN TABLEAU DE JOURS FÉRIÉS =====
    private TableView<JourFerieApi> creerTableauJoursFeries(List<JourFerieApi> joursFeries) {
        TableView<JourFerieApi> table = new TableView<>();
        table.setItems(FXCollections.observableArrayList(joursFeries));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");

        TableColumn<JourFerieApi, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        LocalDate.parse(data.getValue().getDate()).format(formatter)
                ));
        colDate.setPrefWidth(150);

        TableColumn<JourFerieApi, String> colNomAr = new TableColumn<>("Nom (arabe)");
        colNomAr.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getLocalName()));
        colNomAr.setPrefWidth(250);

        TableColumn<JourFerieApi, String> colNomEn = new TableColumn<>("Nom (anglais)");
        colNomEn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getName()));
        colNomEn.setPrefWidth(200);

        TableColumn<JourFerieApi, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(data -> {
            List<String> types = data.getValue().getTypes();
            String typeStr = (types != null && !types.isEmpty()) ? types.get(0) : "Public";
            return new javafx.beans.property.SimpleStringProperty(typeStr);
        });
        colType.setPrefWidth(100);

        table.getColumns().addAll(colDate, colNomAr, colNomEn, colType);
        table.setPrefHeight(300);
        table.setStyle("-fx-font-size: 13px;");

        return table;
    }

    // ===== VOIR LA MÉTÉO (NOUVEAU) =====
    @FXML
    private void voirMeteo() {
        // Créer une fenêtre de chargement
        Stage loadingStage = new Stage();
        loadingStage.initModality(Modality.APPLICATION_MODAL);
        loadingStage.setTitle("Chargement");
        loadingStage.setResizable(false);

        VBox loadingRoot = new VBox(20);
        loadingRoot.setAlignment(Pos.CENTER);
        loadingRoot.setStyle("-fx-background-color: white; -fx-padding: 30px; -fx-background-radius: 15px;");

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(60, 60);

        Label loadingLabel = new Label("Récupération de la météo...");
        loadingLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label apiLabel = new Label("Connexion à l'API OpenWeatherMap");
        apiLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");

        loadingRoot.getChildren().addAll(progressIndicator, loadingLabel, apiLabel);

        Scene loadingScene = new Scene(loadingRoot, 320, 220);
        loadingStage.setScene(loadingScene);
        loadingStage.show();

        // Charger les données dans un thread séparé
        new Thread(() -> {
            try {
                Thread.sleep(500);

                // Déterminer la ville à interroger
                String ville = "Tunis";
                if (ecoleSelectionnee != null && ecoleSelectionnee.getPosition() != null && !ecoleSelectionnee.getPosition().isEmpty()) {
                    ville = ecoleSelectionnee.getPosition();
                }

                Meteo meteo = meteoService.getMeteo(ville);

                javafx.application.Platform.runLater(() -> {
                    loadingStage.close();
                    afficherFenetreMeteo(meteo);
                });

            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    loadingStage.close();
                    showAlert("Erreur", "Impossible de charger la météo: " + e.getMessage(), AlertType.ERROR);
                    e.printStackTrace();
                });
            }
        }).start();
    }

    // ===== AFFICHER LA FENÊTRE MÉTÉO =====
    private void afficherFenetreMeteo(Meteo meteo) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Météo - " + meteo.getVille());

        VBox root = new VBox(20);
        root.setStyle("-fx-background-color: white; -fx-padding: 30px; -fx-background-radius: 20px;");
        root.setAlignment(Pos.CENTER);
        root.setPrefWidth(350);

        // Emoji météo
        Label emojiLabel = new Label(meteo.getEmoji());
        emojiLabel.setStyle("-fx-font-size: 70px;");

        // Température
        Label tempLabel = new Label(String.format("%.1f°C", meteo.getTemperature()));
        tempLabel.setStyle("-fx-font-size: 48px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Ressenti
        Label ressentiLabel = new Label("Ressenti: " + String.format("%.1f°C", meteo.getRessenti()));
        ressentiLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        // Description
        Label descriptionLabel = new Label(meteo.getDescription());
        descriptionLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #34495e;");

        // Séparateur
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #ecf0f1;");

        // Détails
        GridPane details = new GridPane();
        details.setHgap(20);
        details.setVgap(12);
        details.setAlignment(Pos.CENTER);

        Label humiditeIcon = new Label("💧");
        humiditeIcon.setStyle("-fx-font-size: 18px;");
        Label humiditeLabel = new Label("Humidité:");
        humiditeLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #34495e;");
        Label humiditeValue = new Label(meteo.getHumidite() + "%");
        humiditeValue.setStyle("-fx-text-fill: #2c3e50;");
        details.add(humiditeIcon, 0, 0);
        details.add(humiditeLabel, 1, 0);
        details.add(humiditeValue, 2, 0);

        Label ventIcon = new Label("💨");
        ventIcon.setStyle("-fx-font-size: 18px;");
        Label ventLabel = new Label("Vent:");
        ventLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #34495e;");
        Label ventValue = new Label(String.format("%.1f km/h", meteo.getVent()));
        ventValue.setStyle("-fx-text-fill: #2c3e50;");
        details.add(ventIcon, 0, 1);
        details.add(ventLabel, 1, 1);
        details.add(ventValue, 2, 1);

        // Bouton fermer
        Button closeButton = new Button("Fermer");
        closeButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12px 40px; -fx-background-radius: 25px; -fx-cursor: hand;");
        closeButton.setOnAction(e -> stage.close());

        root.getChildren().addAll(emojiLabel, tempLabel, ressentiLabel, descriptionLabel, separator, details, closeButton);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.showAndWait();
    }

    // ===== INSCRIPTION =====
    @FXML
    private void inscrireEnfant() {
        if (ecoleSelectionnee == null) {
            showAlert("Information", "Veuillez sélectionner une école", AlertType.WARNING);
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Inscription");
        dialog.setHeaderText("Inscription à " + ecoleSelectionnee.getNom());

        ButtonType confirmerButton = new ButtonType("Confirmer l'inscription", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmerButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nomEnfant = new TextField();
        nomEnfant.setPromptText("Nom de l'enfant");

        TextField prenomEnfant = new TextField();
        prenomEnfant.setPromptText("Prénom de l'enfant");

        DatePicker dateNaissance = new DatePicker();
        dateNaissance.setPromptText("Date de naissance");

        ComboBox<String> niveauCombo = new ComboBox<>();
        niveauCombo.getItems().addAll("Préscolaire", "Primaire", "Collège", "Lycée");
        niveauCombo.setPromptText("Niveau scolaire");

        // Ajout d'une sélection de trajet (optionnel)
        ComboBox<Trajet> trajetCombo = new ComboBox<>();
        trajetCombo.setPromptText("Choisir un trajet (optionnel)");

        try {
            List<Trajet> trajets = trajetService.selectTrajetsByEcole(ecoleSelectionnee.getId());
            if (!trajets.isEmpty()) {
                trajetCombo.setItems(FXCollections.observableArrayList(trajets));
                trajetCombo.setCellFactory(param -> new ListCell<Trajet>() {
                    @Override
                    protected void updateItem(Trajet trajet, boolean empty) {
                        super.updateItem(trajet, empty);
                        setText(empty || trajet == null ? null : trajet.getNomTrajet() + " (" + trajet.getPointDepart() + " → " + trajet.getPointArrivee() + ")");
                    }
                });

                trajetCombo.setButtonCell(new ListCell<Trajet>() {
                    @Override
                    protected void updateItem(Trajet trajet, boolean empty) {
                        super.updateItem(trajet, empty);
                        setText(empty || trajet == null ? null : trajet.getNomTrajet());
                    }
                });
            } else {
                trajetCombo.setDisable(true);
                trajetCombo.setPromptText("Aucun trajet disponible");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        grid.add(new Label("Nom:"), 0, 0);
        grid.add(nomEnfant, 1, 0);
        grid.add(new Label("Prénom:"), 0, 1);
        grid.add(prenomEnfant, 1, 1);
        grid.add(new Label("Date naissance:"), 0, 2);
        grid.add(dateNaissance, 1, 2);
        grid.add(new Label("Niveau:"), 0, 3);
        grid.add(niveauCombo, 1, 3);
        grid.add(new Label("Trajet:"), 0, 4);
        grid.add(trajetCombo, 1, 4);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == confirmerButton) {
            if (nomEnfant.getText().isEmpty() || prenomEnfant.getText().isEmpty() ||
                    dateNaissance.getValue() == null || niveauCombo.getValue() == null) {
                showAlert("Erreur", "Veuillez remplir tous les champs obligatoires", AlertType.ERROR);
                return;
            }

            String trajetInfo = trajetCombo.getValue() != null ?
                    trajetCombo.getValue().getNomTrajet() + " (" + trajetCombo.getValue().getPointDepart() + " → " + trajetCombo.getValue().getPointArrivee() + ")" :
                    "Aucun trajet sélectionné";

            String message = String.format(
                    "✅ Inscription confirmée !\n\n" +
                            "École: %s\n" +
                            "Enfant: %s %s\n" +
                            "Date naissance: %s\n" +
                            "Niveau: %s\n" +
                            "Trajet: %s\n\n" +
                            "Un email de confirmation a été envoyé.",
                    ecoleSelectionnee.getNom(),
                    prenomEnfant.getText(), nomEnfant.getText(),
                    dateNaissance.getValue().toString(),
                    niveauCombo.getValue(),
                    trajetInfo
            );

            showAlert("Succès", message, AlertType.INFORMATION);
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