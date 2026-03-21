package tn.esprit.workshop.controlleurs;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import tn.esprit.workshop.model.Reclamation;
import tn.esprit.workshop.model.Reponse;
import tn.esprit.workshop.services.FiltrageService;
import tn.esprit.workshop.services.ReclamationService;
import tn.esprit.workshop.services.ReponseService;
import tn.esprit.workshop.services.TraductionService;
import javafx.geometry.Insets;
import java.util.Optional;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class GestionReclamationController implements Initializable {
    @FXML private Label compteurReclamations;
    @FXML private ChoiceBox<String> typeChoice;
    @FXML private TextArea messageField;
    @FXML private TextArea reponseArea;
    @FXML private TextField searchField;
    @FXML private Label statusLabel;
    @FXML private Label statutReponseLabel;
    @FXML private Label traductionLabel; // Ajoutez ce champ
    @FXML private ChoiceBox<String> langueCibleChoice;
    private Map<String, String> languesMap;
    private Reponse reponseCourante;
    @FXML private Label totalReclamations;
    @FXML private Label enAttenteCount;
    @FXML private Label traiteesCount;

    // Nouveaux champs FXML
    @FXML private VBox panelChauffeur;
    @FXML private VBox panelBus;
    @FXML private VBox panelCantine;
    @FXML private VBox panelEcole;
    @FXML private VBox panelAutre;
    @FXML private VBox panelTrajet;
    @FXML private TextField chauffeurNomField;
    @FXML private TextField chauffeurPrenomField;
    @FXML private TextField busMatriculeField;
    @FXML private ChoiceBox<String> cantineTypeChoice;
    @FXML private TextField ecoleNomField;
    @FXML private TextField autrePrecisionField;
    @FXML private Label detailUserLabel;
    @FXML private Label detailTypeLabel;
    @FXML private TextArea detailMessageArea;
    @FXML private Label detailDateLabel;
    @FXML private Label detailInfosLabel;
    @FXML private TableView<Reclamation> tableReclamation;
    @FXML private TableColumn<Reclamation, String> colType;
    @FXML private TableColumn<Reclamation, String> colDescription;
    @FXML private TableColumn<Reclamation, String> colDetails;
    @FXML private TableColumn<Reclamation, String> colStatut;
    @FXML private TableColumn<Reclamation, Timestamp> colDate;
    // ✅ AJOUTEZ CES DEUX LIGNES
    @FXML private Button traduireReponseButton;
    @FXML private Label traductionReponseLabel;
    private final ReclamationService service = new ReclamationService();
    private final ReponseService reponseService = new ReponseService();
    private final int currentUserId = 1;
    private final TraductionService traductionService = new TraductionService();
    private final FiltrageService filtrageService = new FiltrageService();
    private final UserService userService = new UserService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Types de réclamation
        typeChoice.getItems().addAll("Bus", "École", "Chauffeur", "Cantine", "Trajet", "Autre");

        // Initialiser les choix pour la cantine
        cantineTypeChoice.getItems().addAll("Qualité des repas", "Quantité insuffisante",
                "Hygiène", "Service", "Autre");

        // Cacher tous les panels au départ
        cacherTousLesPanels();

        // Configuration des colonnes
        configurerColonnes();

        // Style conditionnel pour le statut
        configurerStyleStatut();

        // Listener pour changer les panels selon le type sélectionné
        typeChoice.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> changerPanelSelonType(newVal)
        );

        // Listener pour la sélection dans la table
        tableReclamation.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        remplirFormulaireAvecReclamation(newSelection);
                        chargerReponse(newSelection.getId());
                        afficherDetailsReclamation(newSelection);

                    }
                }
        );
        initialiserLangues();

        // Charger les réclamations
        afficherReclamations();

        // ✅ INITIALISER LES STATISTIQUES À 0 AU CAS OÙ
        if (totalReclamations == null) totalReclamations.setText("0");
        if (enAttenteCount == null) enAttenteCount.setText("0");
        if (traiteesCount == null) traiteesCount.setText("0");

    }


    private void initialiserLangues() {
        // ✅ Créer la map avec les noms affichés et les codes ISO
        languesMap = new LinkedHashMap<>();
        languesMap.put("Anglais", "en");
        languesMap.put("Français", "fr");
        languesMap.put("Espagnol", "es");
        languesMap.put("Allemand", "de");
        languesMap.put("Italien", "it");
        languesMap.put("Arabe", "ar");
        languesMap.put("Chinois", "zh");
        languesMap.put("Japonais", "ja");
        languesMap.put("Russe", "ru");
        languesMap.put("Portugais", "pt");

        // ✅ Remplir le ChoiceBox avec les NOMS (pas les codes)
        langueCibleChoice.getItems().clear();
        langueCibleChoice.getItems().addAll(languesMap.keySet());

        // ✅ Sélectionner une valeur par défaut
        if (!languesMap.isEmpty()) {
            langueCibleChoice.setValue("Anglais");
        }

        System.out.println("Langues initialisées: " + languesMap.size());
    }

    // ================= GESTION DES PANELS DYNAMIQUES =================

    private void changerPanelSelonType(String type) {
        cacherTousLesPanels();

        if (type == null) return;

        switch (type) {
            case "Chauffeur":
                panelChauffeur.setManaged(true);
                panelChauffeur.setVisible(true);
                break;
            case "Bus":
                panelBus.setManaged(true);
                panelBus.setVisible(true);
                break;
            case "Cantine":
                panelCantine.setManaged(true);
                panelCantine.setVisible(true);
                break;
            case "École":
                panelEcole.setManaged(true);
                panelEcole.setVisible(true);
                break;
            case "Autre":
                panelAutre.setManaged(true);
                panelAutre.setVisible(true);
                break;
            case "Trajet":
                panelTrajet.setManaged(true);
                panelTrajet.setVisible(true);
                break;
        }
    }

    private void cacherTousLesPanels() {
        if (panelChauffeur != null) {
            panelChauffeur.setManaged(false);
            panelChauffeur.setVisible(false);
        }
        if (panelBus != null) {
            panelBus.setManaged(false);
            panelBus.setVisible(false);
        }
        if (panelCantine != null) {
            panelCantine.setManaged(false);
            panelCantine.setVisible(false);
        }
        if (panelEcole != null) {
            panelEcole.setManaged(false);
            panelEcole.setVisible(false);
        }
        if (panelAutre != null) {
            panelAutre.setManaged(false);
            panelAutre.setVisible(false);
        }
        if (panelTrajet != null) {
            panelTrajet.setManaged(false);
            panelTrajet.setVisible(false);
        }
    }

    // ================= CONFIGURATION =================

    private void configurerColonnes() {

        colType.setCellValueFactory(new PropertyValueFactory<>("type"));

        // Afficher la description
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));

        // ✅ Pour la colonne Détails : extraire les détails selon le type
        colDetails.setCellValueFactory(cellData -> {
            Reclamation r = cellData.getValue();
            String details = "";

            switch (r.getType()) {
                case "Chauffeur":
                    if (r.getChauffeurPrenom() != null || r.getChauffeurNom() != null) {
                        details = (r.getChauffeurPrenom() != null ? r.getChauffeurPrenom() + " " : "") +
                                (r.getChauffeurNom() != null ? r.getChauffeurNom() : "");
                    }
                    break;
                case "Bus":
                    details = r.getBusMatricule() != null ? r.getBusMatricule() : "";
                    break;
                case "Cantine":
                    details = r.getCantineType() != null ? r.getCantineType() : "";
                    break;
                case "École":
                    details = r.getEcoleNom() != null ? r.getEcoleNom() : "";
                    break;
                case "Autre":
                    details = r.getAutrePrecision() != null ? r.getAutrePrecision() : "";
                    break;
                case "Trajet":
                    details = "-";
                    break;
            }

            return new javafx.beans.property.SimpleStringProperty(details);
        });

        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateReclamation"));

        // Format de la date
        colDate.setCellFactory(column -> new TableCell<>() {
            private final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");

            @Override
            protected void updateItem(Timestamp item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : format.format(item));
            }
        });
    }

    private void configurerStyleStatut() {
        colStatut.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "EN_ATTENTE" ->
                                setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                        case "TRAITEE" ->
                                setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                        default ->
                                setStyle("");
                    }
                }
            }
        });
    }

    // Nouvelle méthode pour extraire les détails
    private String getDetailsFromReclamation(Reclamation r) {
        if (r.getType() == null) return "";

        switch (r.getType()) {
            case "Chauffeur":
                if (r.getChauffeurPrenom() != null || r.getChauffeurNom() != null) {
                    return (r.getChauffeurPrenom() != null ? r.getChauffeurPrenom() + " " : "") +
                            (r.getChauffeurNom() != null ? r.getChauffeurNom() : "");
                }
                break;
            case "Bus":
                return r.getBusMatricule() != null ? r.getBusMatricule() : "";
            case "Cantine":
                return r.getCantineType() != null ? r.getCantineType() : "";
            case "École":
                return r.getEcoleNom() != null ? r.getEcoleNom() : "";
            case "Autre":
                return r.getAutrePrecision() != null ? r.getAutrePrecision() : "";
            case "Trajet":
                return "-";
        }
        return "";
    }

    // ================= CHARGER REPONSE =================

    private void chargerReponse(int reclamationId) {
        try {
            Reponse reponse = reponseService.getReponseByReclamationId(reclamationId);
            this.reponseCourante = reponse; // Stocker la réponse
            if (reponse != null) {
                reponseArea.setText(reponse.getMessage());
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                statutReponseLabel.setText("Répondu le " + reponse.getDate().format(formatter));
                // ✅ Afficher qui a répondu
                Users user = userService.getById(reponse.getUserId());
                if (user != null) {
                    reponseAuteurLabel.setText("Par: " + user.getPrenom() + " " + user.getNom());
                } else {
                    reponseAuteurLabel.setText("Par: Administrateur");
                }
                // ✅ Activer le bouton de traduction
                if (traduireReponseButton != null) {
                    traduireReponseButton.setDisable(false);
                }
            } else {
                reponseArea.setText("Aucune réponse pour le moment...");
                statutReponseLabel.setText("En attente de réponse");
                // ✅ Désactiver le bouton de traduction
                if (traduireReponseButton != null) {
                    traduireReponseButton.setDisable(true);
                }
                if (traductionReponseLabel != null) {
                    traductionReponseLabel.setText("");
                }
            }
        } catch (SQLException e) {
            reponseArea.setText("Erreur chargement réponse");
            statutReponseLabel.setText("Indisponible");
            e.printStackTrace();
        }
    }
    // ================= AFFICHER DÉTAILS =================

    private void afficherDetailsReclamation(Reclamation r) {
        // Mettre à jour les champs de base
        if (detailTypeLabel != null) detailTypeLabel.setText(r.getType());
        if (detailMessageArea != null) detailMessageArea.setText(r.getDescription());

        // Formatage de la date
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        if (r.getDateReclamation() != null && detailDateLabel != null) {
            detailDateLabel.setText(format.format(r.getDateReclamation()));
        }

        // ✅ Afficher les vraies infos utilisateur
        if (detailUserLabel != null) {
            Users user = userService.getById(r.getUserId());
            if (user != null) {
                detailUserLabel.setText(user.getPrenom() + " " + user.getNom());
            } else {
                detailUserLabel.setText("Utilisateur #" + r.getUserId());
            }
        }

        // Afficher les détails spécifiques
        if (detailInfosLabel != null) {
            String infos = getDetailsComplets(r);
            detailInfosLabel.setText(infos);
        }
    }

    // ✅ MÉTHODE UTILITAIRE POUR LES DÉTAILS SPÉCIFIQUES
    private String getDetailsComplets(Reclamation r) {
        if (r.getType() == null) return "Aucun détail";

        switch (r.getType()) {
            case "Chauffeur":
                StringBuilder chauffeur = new StringBuilder();
                if (r.getChauffeurPrenom() != null) chauffeur.append(r.getChauffeurPrenom()).append(" ");
                if (r.getChauffeurNom() != null) chauffeur.append(r.getChauffeurNom());
                return chauffeur.length() > 0 ? chauffeur.toString() : "Nom non spécifié";
            case "Bus":
                return r.getBusMatricule() != null ? r.getBusMatricule() : "Matricule non spécifié";
            case "Cantine":
                return r.getCantineType() != null ? r.getCantineType() : "Type non spécifié";
            case "École":
                return r.getEcoleNom() != null ? r.getEcoleNom() : "Nom non spécifié";
            case "Autre":
                return r.getAutrePrecision() != null ? r.getAutrePrecision() : "Précision non spécifiée";
            case "Trajet":
                return "Aucun détail requis";
            default:
                return "Détails non disponibles";
        }
    }

    // ================= REMPLIR FORMULAIRE =================

    private void remplirFormulaireAvecReclamation(Reclamation r) {
        typeChoice.setValue(r.getType());
        messageField.setText(r.getDescription());

        // Remplir les champs spécifiques
        chauffeurNomField.setText(r.getChauffeurNom());
        chauffeurPrenomField.setText(r.getChauffeurPrenom());
        busMatriculeField.setText(r.getBusMatricule());
        cantineTypeChoice.setValue(r.getCantineType());
        ecoleNomField.setText(r.getEcoleNom());
        autrePrecisionField.setText(r.getAutrePrecision());

        // Afficher le bon panel
        changerPanelSelonType(r.getType());
    }

    // ================= CRUD =================

    @FXML
    private void envoyerReclamation() {
        String type = typeChoice.getValue();
        String message = messageField.getText();

        // Validations
        if (type == null || type.isEmpty()) {
            showAlert("Erreur de saisie", "❌ Veuillez sélectionner un type de réclamation !", Alert.AlertType.WARNING);
            return;
        }

        if (message == null || message.trim().isEmpty()) {
            showAlert("Erreur de saisie", "❌ Le message ne peut pas être vide !", Alert.AlertType.WARNING);
            messageField.requestFocus();
            return;
        }

        if (message.trim().length() < 10) {
            showAlert("Erreur de saisie", "❌ Le message doit contenir au moins 10 caractères !", Alert.AlertType.WARNING);
            messageField.requestFocus();
            return;
        }

        if (message.trim().length() > 500) {
            showAlert("Erreur de saisie", "❌ Le message ne peut pas dépasser 500 caractères !", Alert.AlertType.WARNING);
            messageField.requestFocus();
            return;
        }

        // Récupérer les valeurs des champs spécifiques
        String chauffeurNom = chauffeurNomField.getText();
        String chauffeurPrenom = chauffeurPrenomField.getText();
        String busMatricule = busMatriculeField.getText();
        String cantineType = cantineTypeChoice.getValue();
        String ecoleNom = ecoleNomField.getText();
        String autrePrecision = autrePrecisionField.getText();

        // Validation spécifique selon le type
        if (type.equals("Chauffeur") && (chauffeurNom.trim().isEmpty() || chauffeurPrenom.trim().isEmpty())) {
            showAlert("Erreur", "❌ Veuillez saisir le nom et prénom du chauffeur", Alert.AlertType.WARNING);
            return;
        }

        if (type.equals("Bus") && busMatricule.trim().isEmpty()) {
            showAlert("Erreur", "❌ Veuillez saisir le matricule du bus", Alert.AlertType.WARNING);
            return;
        }

        if (type.equals("Cantine") && (cantineType == null || cantineType.isEmpty())) {
            showAlert("Erreur", "❌ Veuillez sélectionner le type de problème", Alert.AlertType.WARNING);
            return;
        }

        if (type.equals("École") && ecoleNom.trim().isEmpty()) {
            showAlert("Erreur", "❌ Veuillez saisir le nom de l'école", Alert.AlertType.WARNING);
            return;
        }

        if (type.equals("Autre") && autrePrecision.trim().isEmpty()) {
            showAlert("Erreur", "❌ Veuillez préciser votre demande", Alert.AlertType.WARNING);
            return;
        }
        // ✅ FILTRAGE : Vérifier les mots grossiers
        FiltrageService.ResultatFiltrage resultatFiltrage = filtrageService.analyser(message);
        if (resultatFiltrage.contientGrossieretes()) {
            // Créer une alerte personnalisée
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("⚠️ Langage inapproprié");
            alert.setHeaderText("Des mots inappropriés ont été détectés");
            // Créer un contenu plus détaillé
            VBox content = new VBox(10);
            content.setPadding(new Insets(20));

            Label originalLabel = new Label("Message original:");
            originalLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #e74c3c;");
            TextArea originalArea = new TextArea(message);
            originalArea.setEditable(false);
            originalArea.setPrefRowCount(2);
            originalArea.setStyle("-fx-background-color: #f8f8f8;");

            Label filtreLabel = new Label("Message filtré proposé:");
            filtreLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #27ae60;");
            TextArea filtreArea = new TextArea(resultatFiltrage.getTexteFiltre());
            filtreArea.setEditable(false);
            filtreArea.setPrefRowCount(2);
            filtreArea.setStyle("-fx-background-color: #f8f8f8;");

            content.getChildren().addAll(originalLabel, originalArea, filtreLabel, filtreArea);
            alert.getDialogPane().setContent(content);

            // Boutons personnalisés
            ButtonType btnFiltre = new ButtonType("✅ Utiliser la version filtrée", ButtonBar.ButtonData.OK_DONE);
            ButtonType btnModifier = new ButtonType("✏️ Modifier mon message", ButtonBar.ButtonData.NO);
            ButtonType btnAnnuler = new ButtonType("❌ Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(btnFiltre, btnModifier, btnAnnuler);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent()) {
                if (result.get() == btnFiltre) {
                    // Remplacer par la version filtrée
                    messageField.setText(resultatFiltrage.getTexteFiltre());
                    message = resultatFiltrage.getTexteFiltre();
                    statusLabel.setText("✅ Message filtré automatiquement");
                } else if (result.get() == btnModifier) {
                    // L'utilisateur veut modifier
                    messageField.requestFocus();
                    messageField.selectAll();
                    return; // Arrêter l'envoi
                } else {
                    // Annuler
                    return; // Arrêter l'envoi
                }
            } else {
                return; // Annuler si la boîte de dialogue est fermée
            }
        }
            // Confirmation
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Envoyer la réclamation");
        confirm.setContentText("Voulez-vous vraiment envoyer cette réclamation ?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                // Traduction du message (optionnelle - ne bloque pas l'envoi)
                String langueOriginale = "inconnue";
                String messageTraduit = "";

                try {
                    langueOriginale = traductionService.detecterLangue(message);
                    messageTraduit = traductionService.traduireVersFrancais(message);
                    statusLabel.setText("✅ Langue détectée : " + langueOriginale);

                    showAlert("Succès", "✅ Réclamation envoyée !", Alert.AlertType.INFORMATION);
                    viderFormulaire();
                    afficherReclamations(); // ✅ Cette ligne mettra à jour les stats
                } catch (Exception e) {
                    System.out.println("Traduction non disponible: " + e.getMessage());
                    // On continue sans traduction
                }

                // Appel au service (à adapter selon votre méthode)
                service.ajouterReclamationEtRetournerId(
                        currentUserId,
                        type,
                        message.trim(),
                        "EN_ATTENTE",
                        chauffeurNom,
                        chauffeurPrenom,
                        busMatricule,
                        cantineType,
                        ecoleNom,
                        autrePrecision
                        // Note: Vous devrez peut-être modifier votre méthode pour accepter langueOriginale et messageTraduit
                );

                showAlert("Succès", "✅ Réclamation envoyée avec succès !", Alert.AlertType.INFORMATION);
                viderFormulaire();
                afficherReclamations();

            } catch (SQLException e) {
                showAlert("Erreur", "❌ Erreur lors de l'envoi : " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void modifierReclamation() {
        Reclamation selected = tableReclamation.getSelectionModel().getSelectedItem();
        String type = typeChoice.getValue();
        String message = messageField.getText();

        if (selected == null) {
            showAlert("Erreur", "❌ Veuillez sélectionner une réclamation à modifier !", Alert.AlertType.WARNING);
            return;
        }

        if (type == null || type.isEmpty()) {
            showAlert("Erreur", "❌ Veuillez sélectionner un type !", Alert.AlertType.WARNING);
            return;
        }

        if (message == null || message.trim().isEmpty()) {
            showAlert("Erreur", "❌ Le message ne peut pas être vide !", Alert.AlertType.WARNING);
            return;
        }

        if (message.trim().length() < 10) {
            showAlert("Erreur", "❌ Le message doit contenir au moins 10 caractères !", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Modifier la réclamation");
        confirm.setContentText("Voulez-vous vraiment modifier cette réclamation ?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                selected.setType(type);
                selected.setDescription(message.trim());

                // Mettre à jour les champs spécifiques
                selected.setChauffeurNom(chauffeurNomField.getText());
                selected.setChauffeurPrenom(chauffeurPrenomField.getText());
                selected.setBusMatricule(busMatriculeField.getText());
                selected.setCantineType(cantineTypeChoice.getValue());
                selected.setEcoleNom(ecoleNomField.getText());
                selected.setAutrePrecision(autrePrecisionField.getText());

                service.updateOne(selected);

                showAlert("Succès", "✅ Réclamation modifiée avec succès !", Alert.AlertType.INFORMATION);
                afficherReclamations();

            } catch (SQLException e) {
                showAlert("Erreur", "❌ Erreur modification : " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void supprimerReclamation() {
        Reclamation selected = tableReclamation.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("Erreur", "❌ Veuillez sélectionner une réclamation à supprimer !", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de suppression");
        confirm.setHeaderText("Supprimer la réclamation");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer cette réclamation ?\nCette action est irréversible.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                // Vérifier s'il y a une réponse associée
                Reponse reponse = reponseService.getReponseByReclamationId(selected.getId());
                if (reponse != null) {
                    reponseService.delete(reponse.getId());
                }

                service.deleteOne(selected.getId());

                showAlert("Succès", "✅ Réclamation supprimée avec succès !", Alert.AlertType.INFORMATION);
                afficherReclamations();
                viderFormulaire();

            } catch (SQLException e) {
                showAlert("Erreur", "❌ Erreur suppression : " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    // ================= RECHERCHE =================

    @FXML
    private void rechercherReclamation() {
        String keyword = searchField.getText();

        if (keyword == null || keyword.trim().isEmpty()) {
            showAlert("Information", "ℹ️ Veuillez entrer un mot-clé pour la recherche", Alert.AlertType.INFORMATION);
            afficherReclamations();
            statusLabel.setText("Affichage de toutes les réclamations");
            return;
        }

        if (keyword.trim().length() < 2) {
            showAlert("Information", "ℹ️ Le mot-clé doit contenir au moins 2 caractères", Alert.AlertType.INFORMATION);
            searchField.requestFocus();
            return;
        }

        try {
            List<Reclamation> resultats = service.rechercherParMotCle(keyword.trim());

            if (resultats.isEmpty()) {
                showAlert("Résultat", "ℹ️ Aucune réclamation trouvée pour : " + keyword, Alert.AlertType.INFORMATION);
                statusLabel.setText("🔍 Aucun résultat pour : " + keyword);
            } else {
                statusLabel.setText("🔍 " + resultats.size() + " résultat(s) pour : " + keyword);
            }

            tableReclamation.setItems(FXCollections.observableArrayList(resultats));

        } catch (SQLException e) {
            showAlert("Erreur", "❌ Erreur lors de la recherche : " + e.getMessage(), Alert.AlertType.ERROR);
            statusLabel.setText("❌ Erreur recherche");
            e.printStackTrace();
        }
    }

    // ================= AFFICHAGE =================
    // ================= AFFICHAGE =================

    private void afficherReclamations() {
        try {
            // Récupérer la liste des réclamations
            List<Reclamation> liste = service.selectAll();

            // Créer l'ObservableList avec le bon type
            javafx.collections.ObservableList<Reclamation> data = FXCollections.observableArrayList(liste);

            // Mettre à jour la table
            tableReclamation.setItems(data);

            // ✅ METTRE À JOUR LES STATISTIQUES DE LA BARRE LATÉRALE
            mettreAJourStatistiques(liste);

            // Mettre à jour le compteur de réclamations
            if (compteurReclamations != null) {
                int taille = liste.size();
                if (taille == 0) {
                    compteurReclamations.setText("Aucune réclamation");
                } else if (taille == 1) {
                    compteurReclamations.setText("1 réclamation");
                } else {
                    compteurReclamations.setText(taille + " réclamations");
                }
            }

        } catch (SQLException e) {
            statusLabel.setText("❌ Erreur chargement");
            e.printStackTrace();
        }
    }

    // ✅ NOUVELLE MÉTHODE POUR METTRE À JOUR LES STATISTIQUES
    private void mettreAJourStatistiques(List<Reclamation> liste) {
        if (liste == null || liste.isEmpty()) {
            // Si la liste est vide, mettre tous les compteurs à 0
            if (totalReclamations != null) totalReclamations.setText("0");
            if (enAttenteCount != null) enAttenteCount.setText("0");
            if (traiteesCount != null) traiteesCount.setText("0");
            return;
        }

        // Compter les réclamations par statut
        int total = liste.size();
        int enAttente = 0;
        int traitees = 0;

        for (Reclamation r : liste) {
            if ("EN_ATTENTE".equals(r.getStatut())) {
                enAttente++;
            } else if ("TRAITEE".equals(r.getStatut())) {
                traitees++;
            }
        }

        // Mettre à jour les labels
        if (totalReclamations != null) {
            totalReclamations.setText(String.valueOf(total));
        }

        if (enAttenteCount != null) {
            enAttenteCount.setText(String.valueOf(enAttente));
        }

        if (traiteesCount != null) {
            traiteesCount.setText(String.valueOf(traitees));
        }

        System.out.println("📊 Statistiques mises à jour - Total: " + total +
                ", En attente: " + enAttente +
                ", Traitées: " + traitees);
    }
// ================= TRADUCTION REPONSE =================

    @FXML
    private void traduireReponse() {
        if (reponseCourante == null) {
            showAlert("Information", "Aucune réponse à traduire", Alert.AlertType.INFORMATION);
            return;
        }

        String langueCibleNom = langueCibleChoice.getValue();
        if (langueCibleNom == null) {
            showAlert("Information", "Veuillez choisir une langue", Alert.AlertType.INFORMATION);
            return;
        }

        String langueCibleCode = languesMap.get(langueCibleNom);
        String message = reponseCourante.getMessage();

        statusLabel.setText("⏳ Traduction de la réponse...");

        new Thread(() -> {
            try {
                String traduit = traductionService.traduireVersLangue(message, langueCibleCode);

                javafx.application.Platform.runLater(() -> {
                    if (traduit == null || traduit.startsWith("[")) {
                        traductionReponseLabel.setText("⚠️ " + (traduit != null ? traduit : "Erreur"));
                        traductionReponseLabel.setStyle("-fx-text-fill: #e67e22;");
                    } else {
                        traductionReponseLabel.setText("📝 " + traduit);
                        traductionReponseLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                        statusLabel.setText("✅ Réponse traduite");
                    }
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    traductionReponseLabel.setText("❌ Erreur: " + e.getMessage());
                    traductionReponseLabel.setStyle("-fx-text-fill: #e74c3c;");
                    statusLabel.setText("❌ Erreur de traduction");
                });
            }
        }).start();
    }
    @FXML
    private void traduireMessage() {
        String message = messageField.getText();
        if (message == null || message.trim().isEmpty()) {
            traductionLabel.setText("Veuillez écrire un message à traduire");
            traductionLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        String langueCibleNom = langueCibleChoice.getValue();

        System.out.println("Langue sélectionnée: '" + langueCibleNom + "'");
        if (langueCibleNom == null || langueCibleNom.isEmpty()) {
            traductionLabel.setText("⚠️ Veuillez choisir une langue dans la liste");
            traductionLabel.setStyle("-fx-text-fill: #e67e22;");
            return;
        }
        // ✅ Vérifier que languesMap n'est pas null
        if (languesMap == null) {
            traductionLabel.setText("❌ Erreur de configuration des langues");
            traductionLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }
        String langueCibleCode = languesMap.get(langueCibleNom);
        System.out.println("Code correspondant: '" + langueCibleCode + "'");
        if (langueCibleCode == null) {
            traductionLabel.setText("❌ Langue non supportée: " + langueCibleNom);
            traductionLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }
        statusLabel.setText("⏳ Traduction en cours...");
        traductionLabel.setText("Recherche de traduction...");



        // Désactiver le bouton pendant la traduction
        // traduireButton.setDisable(true);

        new Thread(() -> {
            try {
                String traduit = traductionService.traduireVersLangue(message, langueCibleCode);

                javafx.application.Platform.runLater(() -> {
                    // ✅ Gestion de tous les cas possibles
                    if (traduit == null) {
                        traductionLabel.setText("❌ Erreur de traduction");
                        traductionLabel.setStyle("-fx-text-fill: #e74c3c;");
                        statusLabel.setText("❌ Échec de la traduction");
                    }
                    else if (traduit.startsWith("[")) {
                        traductionLabel.setText("⚠️ " + traduit);
                        traductionLabel.setStyle("-fx-text-fill: #e67e22;");
                        statusLabel.setText("⚠️ " + traduit);
                    }
                    else if (traduit.startsWith("Erreur") || traduit.contains("Erreur")) {
                        traductionLabel.setText("❌ " + traduit);
                        traductionLabel.setStyle("-fx-text-fill: #e74c3c;");
                        statusLabel.setText("❌ " + traduit);
                    }
                    else {
                        traductionLabel.setText("📝 " + traduit);
                        traductionLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                        statusLabel.setText("✅ Traduction effectuée");
                    }
                    // traduireButton.setDisable(false);
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    traductionLabel.setText("❌ Erreur: " + e.getMessage());
                    traductionLabel.setStyle("-fx-text-fill: #e74c3c;");
                    statusLabel.setText("❌ Erreur de traduction");
                    // traduireButton.setDisable(false);
                });
            }
        }).start();
    }
    // ================= RESET =================

    @FXML
    private void viderFormulaire() {
        typeChoice.setValue(null);
        messageField.clear();

        // Vider les nouveaux champs
        chauffeurNomField.clear();
        chauffeurPrenomField.clear();
        busMatriculeField.clear();
        cantineTypeChoice.setValue(null);
        ecoleNomField.clear();
        autrePrecisionField.clear();

        reponseArea.clear();
        statutReponseLabel.setText("En attente de réponse");
        tableReclamation.getSelectionModel().clearSelection();
        searchField.clear();
        statusLabel.setText("Formulaire réinitialisé");

        cacherTousLesPanels();
    }


    // ================= UTILITAIRE =================

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}