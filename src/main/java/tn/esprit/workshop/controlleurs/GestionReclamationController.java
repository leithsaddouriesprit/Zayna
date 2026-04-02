package tn.esprit.workshop.controlleurs;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import tn.esprit.workshop.model.Reclamation;
import tn.esprit.workshop.model.Reponse;
import tn.esprit.workshop.model.leith.Bus;
import tn.esprit.workshop.model.tous.Chauffeur;
import tn.esprit.workshop.model.tous.Ecole;
import tn.esprit.workshop.services.FiltrageService;
import tn.esprit.workshop.services.ReclamationService;
import tn.esprit.workshop.services.ReponseService;
import tn.esprit.workshop.services.TraductionService;
import javafx.geometry.Insets;

import java.sql.*;
import java.util.Optional;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import tn.esprit.workshop.model.Talel.talel2.User;
import tn.esprit.workshop.model.Talel.talel2.CategorieUser;
import tn.esprit.workshop.services.Talel.ServiceAdmin;
import tn.esprit.workshop.services.leith.BusService;
import tn.esprit.workshop.services.leith.ChauffeurService;
import tn.esprit.workshop.services.leith.EcoleService;
import tn.esprit.workshop.utilis.AppSession;
import tn.esprit.workshop.utilis.MyBDConnexion;

public class GestionReclamationController implements Initializable {

    @FXML
    private Label compteurReclamations;
    @FXML
    private ChoiceBox<String> typeChoice;
    @FXML
    private TextArea messageField;
    @FXML
    private Label reponseAuteurLabel;
    @FXML
    private TextArea reponseArea;
    @FXML
    private TextField searchField;
    @FXML
    private Label statusLabel;
    @FXML
    private Label statutReponseLabel;
    @FXML
    private Label traductionLabel;
    @FXML
    private ChoiceBox<String> langueCibleChoice;
    private Map<String, String> languesMap;
    private Reponse reponseCourante;
    @FXML
    private Label totalReclamations;
    @FXML
    private Label enAttenteCount;
    @FXML
    private Label traiteesCount;
    @FXML
    private Label detailUserTypeLabel;
    @FXML
    private ChoiceBox<String> prioriteChoice;
    // Nouveaux champs FXML
    @FXML
    private ChoiceBox<Chauffeur> chauffeurChoice;
    @FXML
    private ChoiceBox<Bus> busChoice;
    @FXML
    private ChoiceBox<Ecole> ecoleChoice;
    @FXML
    private VBox panelChauffeur;
    @FXML
    private VBox panelBus;
    @FXML
    private VBox panelCantine;
    @FXML
    private VBox panelEcole;
    @FXML
    private VBox panelAutre;
    @FXML
    private VBox panelTrajet;
    @FXML
    private TextField chauffeurNomField;
    @FXML
    private TextField chauffeurPrenomField;
    @FXML
    private TextField busMatriculeField;
    @FXML
    private ChoiceBox<String> cantineTypeChoice;
    @FXML
    private TextField ecoleNomField;
    @FXML
    private TextField autrePrecisionField;
    @FXML
    private Label detailUserLabel;
    @FXML
    private Label detailTypeLabel;
    @FXML
    private TextArea detailMessageArea;
    @FXML
    private Label detailDateLabel;
    @FXML
    private Label detailInfosLabel;
    @FXML
    private TableView<Reclamation> tableReclamation;
    @FXML
    private TableColumn<Reclamation, String> colType;
    @FXML
    private TableColumn<Reclamation, String> colDescription;
    @FXML
    private TableColumn<Reclamation, String> colDetails;
    @FXML
    private TableColumn<Reclamation, String> colStatut;
    @FXML
    private TableColumn<Reclamation, Timestamp> colDate;
    @FXML
    private TableColumn<Reclamation, String> colPriorite;

    @FXML
    private Button traduireReponseButton;
    @FXML
    private Button envoyerButton;
    @FXML
    private Label traductionReponseLabel;

    private final ChauffeurService chauffeurService = new ChauffeurService();
    private final BusService busService = new BusService();
    private final EcoleService ecoleService = new EcoleService();
    private final ReclamationService service = new ReclamationService();
    private final ReponseService reponseService = new ReponseService();
    private final TraductionService traductionService = new TraductionService();
    private final FiltrageService filtrageService = new FiltrageService();
    private final ServiceAdmin serviceAdmin = new ServiceAdmin();
    private int currentUserId;
    private CategorieUser currentUserRole;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Récupérer l'utilisateur connecté depuis AppSession
        currentUserId = AppSession.getInstance().getConnectedUserId();
        currentUserRole = AppSession.getInstance().getConnectedUserRoleEnum();

        // Types de réclamation
        typeChoice.getItems().addAll("Bus", "École", "Chauffeur", "Cantine", "Trajet", "Autre");

        // Initialiser les choix pour la cantine
        cantineTypeChoice.getItems().addAll("Qualité des repas", "Quantité insuffisante",
                "Hygiène", "Service", "Autre");
        prioriteChoice.getItems().addAll("Basse", "Moyenne", "Haute", "Urgente");
        prioriteChoice.setValue("Moyenne"); // Valeur par défaut
        // Cacher tous les panels au départ
        cacherTousLesPanels();

        // Configuration des colonnes
        configurerColonnes();

        // Style conditionnel pour le statut
        configurerStyleStatut();
        configurerBoutonsSelonRole();

        // Remplir les ChoiceBox
        remplirChoixChauffeur();
        remplirChoixBus();
        remplirChoixEcole();

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

        // INITIALISER LES STATISTIQUES À 0
        if (totalReclamations == null) totalReclamations.setText("0");
        if (enAttenteCount == null) enAttenteCount.setText("0");
        if (traiteesCount == null) traiteesCount.setText("0");
    }

    private void initialiserLangues() {
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

        langueCibleChoice.getItems().clear();
        langueCibleChoice.getItems().addAll(languesMap.keySet());

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
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colPriorite.setCellValueFactory(new PropertyValueFactory<>("priorite"));
        colPriorite.setCellFactory(column -> new TableCell<Reclamation, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    // Couleurs selon la priorité
                    switch (item) {
                        case "Basse":
                            setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");
                            break;
                        case "Moyenne":
                            setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                            break;
                        case "Haute":
                            setStyle("-fx-text-fill: #f97316; -fx-font-weight: bold;");
                            break;
                        case "Urgente":
                            setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-font-size: 13px;");
                            break;
                    }
                }
            }
        });
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
                        case "EN_ATTENTE" -> setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                        case "TRAITEE" -> setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                        default -> setStyle("");
                    }
                }
            }
        });
    }

    // ================= CHARGER REPONSE =================

    private void chargerReponse(int reclamationId) {
        try {
            Reponse reponse = reponseService.getReponseByReclamationId(reclamationId);
            this.reponseCourante = reponse;
            if (reponse != null) {
                reponseArea.setText(reponse.getMessage());
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                statutReponseLabel.setText("Répondu le " + reponse.getDate().format(formatter));

                try {
                    int userId = reponse.getUserId();
                    User user = serviceAdmin.getUtilisateurById(userId);
                    if (user != null) {
                        reponseAuteurLabel.setText("Par: " + user.getNom());
                    } else {
                        reponseAuteurLabel.setText("Par: Administrateur");
                    }
                } catch (Exception e) {
                    reponseAuteurLabel.setText("Par: Administrateur");
                }

                if (traduireReponseButton != null) {
                    traduireReponseButton.setDisable(false);
                }
            } else {
                reponseArea.setText("Aucune réponse pour le moment...");
                statutReponseLabel.setText("En attente de réponse");
                if (traduireReponseButton != null) {
                    traduireReponseButton.setDisable(true);
                }
                if (traductionReponseLabel != null) {
                    traductionReponseLabel.setText("");
                }
                if (reponseAuteurLabel != null) {
                    reponseAuteurLabel.setText("");
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
        if (detailTypeLabel != null) detailTypeLabel.setText(r.getType());
        if (detailMessageArea != null) detailMessageArea.setText(r.getDescription());

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        if (r.getDateReclamation() != null && detailDateLabel != null) {
            detailDateLabel.setText(format.format(r.getDateReclamation()));
        }

        if (detailUserLabel != null) {
            try {
                User user = serviceAdmin.getUtilisateurById(r.getUserId());
                if (user != null) {
                    detailUserLabel.setText(user.getNom());
                    if (detailUserTypeLabel != null) {
                        detailUserTypeLabel.setText(user.getCategories().toString());
                    }
                } else {
                    detailUserLabel.setText("Utilisateur #" + r.getUserId());
                }
            } catch (Exception e) {
                detailUserLabel.setText("Utilisateur #" + r.getUserId());
            }
        }

        if (detailInfosLabel != null) {
            String infos = getDetailsComplets(r);
            detailInfosLabel.setText(infos);
        }
    }

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
        if (prioriteChoice != null && r.getPriorite() != null) {
            prioriteChoice.setValue(r.getPriorite());
        }
        // ✅ Remplir les ChoiceBox avec les valeurs de la réclamation
        if (chauffeurChoice != null && r.getIdChauffeur() > 0) {
            try {
                Chauffeur chauffeur = chauffeurService.getById(r.getIdChauffeur());
                chauffeurChoice.setValue(chauffeur);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else if (chauffeurChoice != null) {
            chauffeurChoice.setValue(null);
        }

        if (busChoice != null && r.getIdBus() > 0) {
            try {
                Bus bus = busService.getById(r.getIdBus());
                busChoice.setValue(bus);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else if (busChoice != null) {
            busChoice.setValue(null);
        }

        if (ecoleChoice != null && r.getIdEcole() > 0) {
            try {
                Ecole ecole = ecoleService.getById(r.getIdEcole());
                ecoleChoice.setValue(ecole);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else if (ecoleChoice != null) {
            ecoleChoice.setValue(null);
        }

        if (cantineTypeChoice != null) {
            cantineTypeChoice.setValue(r.getCantineType());
        }

        if (autrePrecisionField != null) {
            autrePrecisionField.setText(r.getAutrePrecision());
        }

        // Afficher le bon panel
        changerPanelSelonType(r.getType());
    }

    // ================= CRUD =================

    @FXML
    private void envoyerReclamation() {
        // ✅ Vérifier si l'utilisateur peut créer une réclamation
        if (currentUserRole == CategorieUser.ADMIN || currentUserRole == CategorieUser.RESPONSABLEECOLE) {
            showAlert("Accès refusé",
                    "Les administrateurs et responsables d'école ne peuvent pas créer de réclamations.\n" +
                            "Seuls les parents, chauffeurs et maîtresses peuvent créer des réclamations.",
                    Alert.AlertType.WARNING);
            return;
        }
        String priorite = prioriteChoice.getValue();
        if (priorite == null) {
            priorite = "Moyenne"; // Valeur par défaut
        }

        String type = typeChoice.getValue();
        String message = messageField.getText();

        // ========== ✅ NOUVEAU : RÉCUPÉRER L'ÉCOLE DE L'UTILISATEUR CONNECTÉ ==========
        int idEcoleUtilisateur = 0;

        if (currentUserRole == CategorieUser.PARENT) {
            int parentId = AppSession.getInstance().getParentId();
            idEcoleUtilisateur = getEcoleIdByParentId(parentId);
            System.out.println("Parent - École ID: " + idEcoleUtilisateur);
        } else if (currentUserRole == CategorieUser.CHAUFFEUR) {
            int chauffeurId = AppSession.getInstance().getChauffeurId();
            idEcoleUtilisateur = getEcoleIdByChauffeurId(chauffeurId);
            System.out.println("Chauffeur - École ID: " + idEcoleUtilisateur);
        } else if (currentUserRole == CategorieUser.MAITRESSE) {
            int maitresseId = AppSession.getInstance().getMaitresseId();
            idEcoleUtilisateur = getEcoleIdByMaitresseId(maitresseId);
            System.out.println("Maîtresse - École ID: " + idEcoleUtilisateur);
        }

        // Variables pour les IDs
        int idChauffeur = 0;
        int idBus = 0;
        int idEcole = idEcoleUtilisateur;  // ✅ Utiliser l'école de l'utilisateur connecté
        int idMaitresse = 0;
        int idParent = 0;
        // Variables pour les noms (copie pour historique)
        String chauffeurNom = "";
        String chauffeurPrenom = "";
        String busMatricule = "";
        String ecoleNom = "";
        String cantineType = "";
        String autrePrecision = "";

        // Traitement selon le type avec les ChoiceBox
        if (type != null && type.equals("Chauffeur") && chauffeurChoice != null && chauffeurChoice.getValue() != null) {
            Chauffeur selectedChauffeur = chauffeurChoice.getValue();
            idChauffeur = selectedChauffeur.getId();
            chauffeurNom = selectedChauffeur.getNom();
            chauffeurPrenom = selectedChauffeur.getPrenom();
        }

        if (type != null && type.equals("Bus") && busChoice != null && busChoice.getValue() != null) {
            Bus selectedBus = busChoice.getValue();
            idBus = selectedBus.getBusId();
            busMatricule = selectedBus.getMatricule();
        }

        if (type != null && type.equals("École") && ecoleChoice != null && ecoleChoice.getValue() != null) {
            Ecole selectedEcole = ecoleChoice.getValue();
            idEcole = selectedEcole.getId();
            ecoleNom = selectedEcole.getNomEcole();
            System.out.println("École sélectionnée - ID: " + idEcole + ", Nom: " + ecoleNom); // Pour déboguer
        } else {
            idEcole = 0; // Si pas d'école sélectionnée, mettre 0 (NULL dans la base)
        }

        // Récupérer les valeurs des autres champs
        if (cantineTypeChoice != null) {
            cantineType = cantineTypeChoice.getValue();
        }

        if (autrePrecisionField != null) {
            autrePrecision = autrePrecisionField.getText();
        }

        // Validation spécifique selon le type
        if (type.equals("Chauffeur") && (chauffeurChoice.getValue() == null || chauffeurChoice.getValue().getId() == 0)) {
            showAlert("Erreur", "❌ Veuillez sélectionner un chauffeur", Alert.AlertType.WARNING);
            chauffeurChoice.requestFocus();
            return;
        }

        if (type.equals("Bus") && (busChoice.getValue() == null || busChoice.getValue().getBusId() == 0)) {
            showAlert("Erreur", "❌ Veuillez sélectionner un bus", Alert.AlertType.WARNING);
            busChoice.requestFocus();
            return;
        }

        if (type.equals("École") && (ecoleChoice.getValue() == null || ecoleChoice.getValue().getId() == 0)) {
            showAlert("Erreur", "❌ Veuillez sélectionner une école", Alert.AlertType.WARNING);
            ecoleChoice.requestFocus();
            return;
        }

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

        // Validation spécifique selon le type
        if (type.equals("Chauffeur") && chauffeurChoice != null && chauffeurChoice.getValue() == null) {
            showAlert("Erreur", "❌ Veuillez sélectionner un chauffeur", Alert.AlertType.WARNING);
            chauffeurChoice.requestFocus();
            return;
        }

        if (type.equals("Bus") && busChoice != null && busChoice.getValue() == null) {
            showAlert("Erreur", "❌ Veuillez sélectionner un bus", Alert.AlertType.WARNING);
            busChoice.requestFocus();
            return;
        }

        if (type.equals("Cantine") && (cantineType == null || cantineType.isEmpty())) {
            showAlert("Erreur", "❌ Veuillez sélectionner le type de problème", Alert.AlertType.WARNING);
            cantineTypeChoice.requestFocus();
            return;
        }

        if (type.equals("École") && ecoleChoice != null && ecoleChoice.getValue() == null) {
            showAlert("Erreur", "❌ Veuillez sélectionner une école", Alert.AlertType.WARNING);
            ecoleChoice.requestFocus();
            return;
        }

        if (type.equals("Autre") && (autrePrecision == null || autrePrecision.trim().isEmpty())) {
            showAlert("Erreur", "❌ Veuillez préciser votre demande", Alert.AlertType.WARNING);
            autrePrecisionField.requestFocus();
            return;
        }

        // ✅ FILTRAGE AVEC BLOCAGE - AUCUN ENVOI SI GROS MOT
        int userId = currentUserId;

// Vérifier si l'utilisateur est déjà bloqué
        if (filtrageService.estBloque(userId)) {
            String tempsRestant = filtrageService.getTempsRestantBlocage(userId);
            showAlert("Accès refusé",
                    "❌ Vous êtes bloqué pour " + tempsRestant + " min.\n" +
                            "Tentatives répétées avec des messages inappropriés.",
                    Alert.AlertType.ERROR);
            return;
        }

// Vérifier si le message contient des gros mots
        if (filtrageService.contientGrossieretes(message)) {
            // Enregistrer la tentative
            int tentatives = filtrageService.incrementerTentatives(userId);

            if (tentatives >= 3) {
                // Bloquer l'utilisateur
                filtrageService.bloquerUtilisateur(userId);
                showAlert("⚠️ Compte bloqué",
                        "❌ Vous êtes bloqué pour 5 minutes.\n" +
                                "Trop de tentatives avec des messages inappropriés.",
                        Alert.AlertType.ERROR);
            } else {
                // Afficher l'avertissement
                int restantes = 3 - tentatives;
                showAlert("⚠️ Message refusé",
                        "❌ Votre message contient des mots inappropriés.\n" +
                                "Il vous reste " + restantes + " tentative(s) avant blocage.\n\n" +
                                "Veuillez reformuler votre message.",
                        Alert.AlertType.WARNING);
            }
            messageField.requestFocus();
            messageField.selectAll();
            return; // ❌ Arrêter l'envoi


    } else

    {
        // Message correct, réinitialiser les tentatives
        filtrageService.resetTentatives(userId);
    }


        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Envoyer la réclamation");
        confirm.setContentText("Voulez-vous vraiment envoyer cette réclamation ?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                service.ajouterReclamationEtRetournerId(
                        currentUserId,
                        type,
                        message.trim(),
                        "EN_ATTENTE",
                        priorite,
                        chauffeurNom,
                        chauffeurPrenom,
                        busMatricule,
                        cantineType,
                        ecoleNom,
                        autrePrecision,
                        idChauffeur,
                        idBus,
                        idEcole,
                        idMaitresse,
                        idParent
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

        // ✅ Vérifier si l'utilisateur peut modifier (seulement ses propres réclamations)
        if (selected != null && selected.getUserId() != currentUserId) {
            showAlert("Accès refusé", "Vous ne pouvez modifier que vos propres réclamations", Alert.AlertType.WARNING);
            return;
        }

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
                selected.setPriorite(prioriteChoice.getValue());
                // ✅ Mettre à jour les champs spécifiques (ceux qui existent)
                // Pour Chauffeur
                if (type.equals("Chauffeur") && chauffeurChoice != null && chauffeurChoice.getValue() != null) {
                    Chauffeur selectedChauffeur = chauffeurChoice.getValue();
                    selected.setIdChauffeur(selectedChauffeur.getId());
                    selected.setChauffeurNom(selectedChauffeur.getNom());
                    selected.setChauffeurPrenom(selectedChauffeur.getPrenom());
                }

                // Pour Bus
                if (type.equals("Bus") && busChoice != null && busChoice.getValue() != null) {
                    Bus selectedBus = busChoice.getValue();
                    selected.setIdBus(selectedBus.getBusId());
                    selected.setBusMatricule(selectedBus.getMatricule());
                }

                // Pour École
                if (type.equals("École") && ecoleChoice != null && ecoleChoice.getValue() != null) {
                    Ecole selectedEcole = ecoleChoice.getValue();
                    selected.setIdEcole(selectedEcole.getId());
                    selected.setEcoleNom(selectedEcole.getNomEcole());
                }

                // Pour Cantine
                if (cantineTypeChoice != null && cantineTypeChoice.getValue() != null) {
                    selected.setCantineType(cantineTypeChoice.getValue());
                }

                // Pour Autre
                if (autrePrecisionField != null) {
                    selected.setAutrePrecision(autrePrecisionField.getText());
                }

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

        if (selected != null && selected.getUserId() != currentUserId) {
            showAlert("Accès refusé", "Vous ne pouvez supprimer que vos propres réclamations", Alert.AlertType.WARNING);
            return;
        }

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

    private void afficherReclamations() {
        try {
            CategorieUser role = AppSession.getInstance().getConnectedUserRoleEnum();
            List<Reclamation> liste;

            int roleId = 0;
            int ecoleId = 0;

            switch (role) {
                case PARENT:
                    roleId = AppSession.getInstance().getParentId();
                    break;
                case CHAUFFEUR:
                    roleId = AppSession.getInstance().getChauffeurId();
                    break;
                case MAITRESSE:
                    roleId = AppSession.getInstance().getMaitresseId();
                    break;
                case RESPONSABLEECOLE:
                    ecoleId = AppSession.getInstance().getEcoleId();
                    break;
                default:
                    break;
            }

            liste = service.getReclamationsByRole(role, roleId, ecoleId);

            tableReclamation.setItems(FXCollections.observableArrayList(liste));
            mettreAJourStatistiques(liste);

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

    private void mettreAJourStatistiques(List<Reclamation> liste) {
        if (liste == null || liste.isEmpty()) {
            if (totalReclamations != null) totalReclamations.setText("0");
            if (enAttenteCount != null) enAttenteCount.setText("0");
            if (traiteesCount != null) traiteesCount.setText("0");
            return;
        }

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

        if (totalReclamations != null) totalReclamations.setText(String.valueOf(total));
        if (enAttenteCount != null) enAttenteCount.setText(String.valueOf(enAttente));
        if (traiteesCount != null) traiteesCount.setText(String.valueOf(traitees));
    }

    // ================= TRADUCTION =================

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

        if (langueCibleNom == null || langueCibleNom.isEmpty()) {
            traductionLabel.setText("⚠️ Veuillez choisir une langue dans la liste");
            traductionLabel.setStyle("-fx-text-fill: #e67e22;");
            return;
        }

        if (languesMap == null) {
            traductionLabel.setText("❌ Erreur de configuration des langues");
            traductionLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        String langueCibleCode = languesMap.get(langueCibleNom);
        if (langueCibleCode == null) {
            traductionLabel.setText("❌ Langue non supportée: " + langueCibleNom);
            traductionLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        statusLabel.setText("⏳ Traduction en cours...");
        traductionLabel.setText("Recherche de traduction...");

        new Thread(() -> {
            try {
                String traduit = traductionService.traduireVersLangue(message, langueCibleCode);

                javafx.application.Platform.runLater(() -> {
                    if (traduit == null) {
                        traductionLabel.setText("❌ Erreur de traduction");
                        traductionLabel.setStyle("-fx-text-fill: #e74c3c;");
                        statusLabel.setText("❌ Échec de la traduction");
                    } else if (traduit.startsWith("[")) {
                        traductionLabel.setText("⚠️ " + traduit);
                        traductionLabel.setStyle("-fx-text-fill: #e67e22;");
                        statusLabel.setText("⚠️ " + traduit);
                    } else if (traduit.startsWith("Erreur") || traduit.contains("Erreur")) {
                        traductionLabel.setText("❌ " + traduit);
                        traductionLabel.setStyle("-fx-text-fill: #e74c3c;");
                        statusLabel.setText("❌ " + traduit);
                    } else {
                        traductionLabel.setText("📝 " + traduit);
                        traductionLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                        statusLabel.setText("✅ Traduction effectuée");
                    }
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    traductionLabel.setText("❌ Erreur: " + e.getMessage());
                    traductionLabel.setStyle("-fx-text-fill: #e74c3c;");
                    statusLabel.setText("❌ Erreur de traduction");
                });
            }
        }).start();
    }

    // ================= RESET =================

    @FXML
    private void viderFormulaire() {
        typeChoice.setValue(null);
        messageField.clear();

        // ✅ Vider les nouveaux champs (ceux qui existent dans votre FXML)
        if (cantineTypeChoice != null) {
            cantineTypeChoice.setValue(null);
        }

        if (autrePrecisionField != null) {
            autrePrecisionField.clear();
        }

        // ✅ Vider les ChoiceBox (les champs qui existent)
        if (chauffeurChoice != null) {
            chauffeurChoice.setValue(null);
        }

        if (busChoice != null) {
            busChoice.setValue(null);
        }

        if (ecoleChoice != null) {
            ecoleChoice.setValue(null);
        }

        // ✅ Réinitialiser les zones d'affichage
        if (reponseArea != null) {
            reponseArea.clear();
        }

        if (statutReponseLabel != null) {
            statutReponseLabel.setText("En attente de réponse");
        }

        if (tableReclamation != null) {
            tableReclamation.getSelectionModel().clearSelection();
        }

        if (searchField != null) {
            searchField.clear();
        }

        if (statusLabel != null) {
            statusLabel.setText("Formulaire réinitialisé");
        }

        cacherTousLesPanels();
    }

    // ================= CONFIGURATION DES BOUTONS =================

    private void configurerBoutonsSelonRole() {
        if (currentUserRole == CategorieUser.ADMIN) {
            typeChoice.setDisable(true);
            messageField.setDisable(true);
            if (envoyerButton != null) envoyerButton.setDisable(true);

            panelChauffeur.setDisable(true);
            panelBus.setDisable(true);
            panelCantine.setDisable(true);
            panelEcole.setDisable(true);
            panelAutre.setDisable(true);
            panelTrajet.setDisable(true);
        }

        if (currentUserRole != CategorieUser.ADMIN && currentUserRole != CategorieUser.RESPONSABLEECOLE) {
            if (traduireReponseButton != null) {
                traduireReponseButton.setDisable(true);
            }
            reponseArea.setEditable(false);
        }
    }

    // ================= REMPLIR LES CHOIX =================
    private void remplirChoixChauffeur() {
        try {
            List<Chauffeur> chauffeurs = chauffeurService.getAll();
            chauffeurChoice.getItems().clear();

            // ✅ AJOUTER UN ÉLÉMENT PAR DÉFAUT
            Chauffeur defaultItem = new Chauffeur();
            defaultItem.setId(0);
            defaultItem.setNom("Sélectionner un chauffeur");
            defaultItem.setPrenom("");
            chauffeurChoice.getItems().add(defaultItem);

            chauffeurChoice.getItems().addAll(chauffeurs);

            // Sélectionner l'élément par défaut
            chauffeurChoice.setValue(defaultItem);

            chauffeurChoice.setConverter(new StringConverter<Chauffeur>() {
                @Override
                public String toString(Chauffeur c) {
                    if (c == null) return "";
                    if (c.getId() == 0) return "🔽 Sélectionner un chauffeur";
                    return c.getPrenom() + " " + c.getNom();
                }
                @Override
                public Chauffeur fromString(String string) { return null; }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void remplirChoixBus() {
        try {
            List<Bus> buses = busService.getAll();
            busChoice.getItems().clear();

            // ✅ AJOUTER UN ÉLÉMENT PAR DÉFAUT
            Bus defaultItem = new Bus();
            defaultItem.setBusId(0);
            defaultItem.setMatricule("Sélectionner un bus");
            busChoice.getItems().add(defaultItem);

            busChoice.getItems().addAll(buses);

            // Sélectionner l'élément par défaut
            busChoice.setValue(defaultItem);

            busChoice.setConverter(new StringConverter<Bus>() {
                @Override
                public String toString(Bus b) {
                    if (b == null) return "";
                    if (b.getBusId() == 0) return "🔽 Sélectionner un bus";
                    return b.getMatricule();
                }
                @Override
                public Bus fromString(String string) { return null; }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void remplirChoixEcole() {
        try {
            List<Ecole> ecoles = ecoleService.getAll();
            ecoleChoice.getItems().clear();

            // ✅ AJOUTER UN ÉLÉMENT PAR DÉFAUT
            Ecole defaultItem = new Ecole();
            defaultItem.setId(0);
            defaultItem.setNomEcole("Sélectionner une école");
            ecoleChoice.getItems().add(defaultItem);

            ecoleChoice.getItems().addAll(ecoles);

            // Sélectionner l'élément par défaut
            ecoleChoice.setValue(defaultItem);

            ecoleChoice.setConverter(new StringConverter<Ecole>() {
                @Override
                public String toString(Ecole e) {
                    if (e == null) return "";
                    if (e.getId() == 0) return "🔽 Sélectionner une école";
                    return e.getNomEcole();
                }
                @Override
                public Ecole fromString(String string) { return null; }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    /**
     * Récupère l'ID de l'école d'un parent
     */
    private int getEcoleIdByParentId(int parentId) {
        try {
            String sql = "SELECT id_ecole FROM parent WHERE id = ?";
            Connection cnx = MyBDConnexion.getInstance().getConnection();
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, parentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id_ecole");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Récupère l'ID de l'école d'un chauffeur
     */
    private int getEcoleIdByChauffeurId(int chauffeurId) {
        try {
            String sql = "SELECT id_ecole FROM chauffeur WHERE id = ?";
            Connection cnx = MyBDConnexion.getInstance().getConnection();
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, chauffeurId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id_ecole");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Récupère l'ID de l'école d'une maîtresse
     */
    private int getEcoleIdByMaitresseId(int maitresseId) {
        try {
            String sql = "SELECT id_ecole FROM maitresse WHERE id = ?";
            Connection cnx = MyBDConnexion.getInstance().getConnection();
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, maitresseId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id_ecole");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
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