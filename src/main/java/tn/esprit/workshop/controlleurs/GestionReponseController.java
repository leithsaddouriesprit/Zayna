package tn.esprit.workshop.controlleurs;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import tn.esprit.workshop.model.Reclamation;
import tn.esprit.workshop.model.Reponse;
import tn.esprit.workshop.model.Talel.talel2.CategorieUser;
import tn.esprit.workshop.model.Talel.talel2.User;
import tn.esprit.workshop.services.ReclamationService;
import tn.esprit.workshop.services.ReponseService;

import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import tn.esprit.workshop.services.Talel.ServiceAdmin;
import tn.esprit.workshop.services.TraductionService;
import tn.esprit.workshop.utilis.AppSession;


public class GestionReponseController implements Initializable {

    // Table des réclamations
    @FXML private TableView<Reclamation> tableReclamation;
    @FXML private TableColumn<Reclamation, String> colType;
    @FXML private TableColumn<Reclamation, String> colDescription;
    @FXML private TableColumn<Reclamation, String> colDetails;
    @FXML private TableColumn<Reclamation, String> colStatut;
    @FXML private TableColumn<Reclamation, Timestamp> colDate;
    @FXML private TableColumn<Reclamation, String> colPrioriteReclamation;
    @FXML private ProgressBar progressBar;
    @FXML private Label lblPourcentage;
    @FXML private Label lblStatut;
    @FXML private Label detailInfosLabel;
    @FXML private Label totalReclamations;
    @FXML private Label enAttenteCount;
    @FXML private Label traiteesCount;

    // Détails de la réclamation (sans ID affiché)
    @FXML private Label detailTypeLabel;
    @FXML private TextArea detailMessageArea;
    @FXML private Label detailDateLabel;
    @FXML private Label detailUserLabel;

    // Section réponse existante
    @FXML private VBox reponseExistanteBox;
    @FXML private TextArea reponseExistanteArea;
    @FXML private Label reponseDateLabel;
    @FXML private Label reponseAuteurLabel;
    @FXML private Label detailAuteurLabel;  // ✅ NOUVEAU

    // Formulaire de réponse
    @FXML private TextArea reponseField;
    @FXML private TextField searchField;
    @FXML private Label statusLabel;
    // ✅ NOUVEAUX CHAMPS POUR LA TRADUCTION
    @FXML private ChoiceBox<String> langueCibleChoice;
    @FXML private Label traductionMessageLabel;
    @FXML private Button traduireMessageButton;
    @FXML private TableColumn<Reclamation, String> colAuteur;
    // Map pour stocker les codes ISO des langues
    private Map<String, String> languesMap;
    private final ReclamationService reclamationService = new ReclamationService();
    private final ReponseService reponseService = new ReponseService();
    private final ServiceAdmin serviceAdmin = new ServiceAdmin();


    private Reclamation reclamationSelectionnee;
    private Reponse reponseExistante;
    private final TraductionService traductionService = new TraductionService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configurerColonnes();
        configurerStyleStatut();
        configurerListenerSelection();
        configurerCompteurCaracteres();
        initialiserLangues();
        afficherToutesReclamations();
        statusLabel.setText("Affichage de toutes les réclamations");

    }

    // ================= CONFIGURATION =================
    private void initialiserLangues() {
        // Liste des langues disponibles (nom affiché -> code ISO)
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

        // Remplir le ChoiceBox avec les noms des langues
        langueCibleChoice.getItems().addAll(languesMap.keySet());
        langueCibleChoice.setValue("Anglais"); // Valeur par défaut

        System.out.println("Langues initialisées: " + languesMap.size());
    }
    private void configurerColonnes() {
        // Masquer l'ID
        // ✅ Colonne Auteur
        colAuteur.setCellValueFactory(cellData -> {
            Reclamation r = cellData.getValue();
            try {
                User user = serviceAdmin.getUtilisateurById(r.getUserId());
                if (user != null) {
                    return new javafx.beans.property.SimpleStringProperty(
                            user.getNom() + " (" + user.getCategories() + ")"
                    );
                }
            } catch (Exception e) {
                return new javafx.beans.property.SimpleStringProperty("Utilisateur #" + r.getUserId());
            }
            return new javafx.beans.property.SimpleStringProperty("Utilisateur #" + r.getUserId());
        });


        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        // ✅ NOUVELLE COLONNE DÉTAILS
        colDetails.setCellValueFactory(cellData -> {
            Reclamation r = cellData.getValue();
            String details = "";

            switch (r.getType()) {
                case "Bus":
                    details = r.getBusMatricule() != null ? r.getBusMatricule() : "-";
                    break;
                case "Chauffeur":
                    if (r.getChauffeurPrenom() != null || r.getChauffeurNom() != null) {
                        details = (r.getChauffeurPrenom() != null ? r.getChauffeurPrenom() + " " : "") +
                                (r.getChauffeurNom() != null ? r.getChauffeurNom() : "");
                    } else {
                        details = "-";
                    }
                    break;
                case "Cantine":
                    details = r.getCantineType() != null ? r.getCantineType() : "-";
                    break;
                case "École":
                    details = r.getEcoleNom() != null ? r.getEcoleNom() : "-";
                    break;
                case "Autre":
                    details = r.getAutrePrecision() != null ? r.getAutrePrecision() : "-";
                    break;
                case "Trajet":
                    details = "-";
                    break;
            }
            colPrioriteReclamation.setCellValueFactory(new PropertyValueFactory<>("priorite"));
            colPrioriteReclamation.setCellFactory(column -> new TableCell<Reclamation, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);
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
                                setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                                break;
                        }
                    }
                }
            });
            return new javafx.beans.property.SimpleStringProperty(details);
        });

        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Formatage de la date
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateReclamation"));
        colDate.setCellFactory(column -> new TableCell<Reclamation, Timestamp>() {
            private final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");

            @Override
            protected void updateItem(Timestamp item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(format.format(item));
                }
            }
        });
    }

    // ================= STATISTIQUES =================

        private void mettreAJourStatistiques(List<Reclamation> liste) {
            if (liste == null || liste.isEmpty()) {
                if (totalReclamations != null) totalReclamations.setText("0");
                if (enAttenteCount != null) enAttenteCount.setText("0");
                if (traiteesCount != null) traiteesCount.setText("0");
                if (progressBar != null) {
                    progressBar.setProgress(0);
                    lblPourcentage.setText("0%");
                    lblStatut.setText("0 traitée(s) sur 0");
                    progressBar.setStyle("-fx-accent: #ef4444; -fx-background-color: #2A2A2A; -fx-background-radius: 10;");
                }
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

            // ✅ BARRE DE PROGRESSION AVEC COULEURS DYNAMIQUES
            if (progressBar != null) {
                double progression = (double) traitees / total;
                int pourcentage = (int)(progression * 100);

                progressBar.setProgress(progression);
                lblPourcentage.setText(pourcentage + "%");
                lblStatut.setText(traitees + " traitée(s) sur " + total);

                // 🎨 Changement de couleur selon le pourcentage
                String couleur;
                if (pourcentage < 30) {
                    couleur = "#ef4444";  // Rouge
                } else if (pourcentage < 70) {
                    couleur = "#f59e0b";  // Orange
                } else {
                    couleur = "#22c55e";  // Vert
                }

                progressBar.setStyle(String.format(
                        "-fx-accent: %s; -fx-background-color: #2A2A2A; -fx-background-radius: 10;",
                        couleur
                ));

                System.out.println("📊 Progression: " + progression + " (" + pourcentage + "%) - Couleur: " + couleur);
            }

            System.out.println("📊 Statistiques mises à jour - Total: " + total +
                    ", En attente: " + enAttente +
                    ", Traitées: " + traitees);
        }
    @FXML
    private void traduireMessage() {
        if (reclamationSelectionnee == null) {
            showAlert("Information", "Veuillez sélectionner une réclamation", Alert.AlertType.INFORMATION);
            return;
        }

        String message = reclamationSelectionnee.getDescription();
        if (message == null || message.trim().isEmpty()) {
            traductionMessageLabel.setText("Message vide");
            traductionMessageLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        String langueCibleNom = langueCibleChoice.getValue();
        if (langueCibleNom == null) {
            showAlert("Information", "Veuillez choisir une langue", Alert.AlertType.INFORMATION);
            return;
        }

        String langueCibleCode = languesMap.get(langueCibleNom);
        if (langueCibleCode == null) {
            traductionMessageLabel.setText("Langue non supportée");
            return;
        }

        statusLabel.setText("⏳ Traduction en cours...");
        traductionMessageLabel.setText("Traduction en cours...");

        // Traduction dans un thread séparé
        new Thread(() -> {
            try {
                String traduit = traductionService.traduireVersLangue(message, langueCibleCode);

                javafx.application.Platform.runLater(() -> {
                    if (traduit == null || traduit.startsWith("[")) {
                        traductionMessageLabel.setText("⚠️ " + (traduit != null ? traduit : "Erreur"));
                        traductionMessageLabel.setStyle("-fx-text-fill: #e67e22;");
                        statusLabel.setText("⚠️ Traduction non disponible");
                    } else {
                        traductionMessageLabel.setText("📝 " + traduit);
                        traductionMessageLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                        statusLabel.setText("✅ Message traduit");
                    }
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    traductionMessageLabel.setText("❌ Erreur: " + e.getMessage());
                    traductionMessageLabel.setStyle("-fx-text-fill: #e74c3c;");
                    statusLabel.setText("❌ Erreur de traduction");
                });
            }
        }).start();
    }

    private void configurerStyleStatut() {
        colStatut.setCellFactory(column -> new TableCell<Reclamation, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("EN_ATTENTE".equals(item)) {
                        setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                    } else if ("TRAITEE".equals(item)) {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    }
                }
            }
        });
    }

    private void configurerCompteurCaracteres() {
        reponseField.textProperty().addListener((obs, oldVal, newVal) -> {
            int longueur = newVal.length();
            if (longueur > 500) {
                reponseField.setText(oldVal);
                statusLabel.setText("❌ Maximum 500 caractères !");
            } else if (longueur > 0) {
                statusLabel.setText("📝 " + longueur + "/500 caractères");
                if (longueur < 5) {
                    statusLabel.setText(statusLabel.getText() + " (minimum 5)");
                }
            } else {
                statusLabel.setText("");
            }
        });
    }

    private void configurerListenerSelection() {
        tableReclamation.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        reclamationSelectionnee = newSelection;
                        afficherDetailsReclamation(newSelection);
                        chargerReponseExistante(newSelection.getId());
                    }
                }
        );
    }
    // ✅ AJOUTER cette méthode pour afficher les détails complets dans le panneau de droite
// ✅ AJOUTER cette méthode pour obtenir les détails complets
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

    // ================= AFFICHAGE DÉTAILS =================

    private void afficherDetailsReclamation(Reclamation r) {
        if (detailTypeLabel != null) detailTypeLabel.setText(r.getType());
        if (detailMessageArea != null) detailMessageArea.setText(r.getDescription());
        // ✅ Afficher l'auteur de la réclamation
        if (detailAuteurLabel != null) {
            try {
                User user = serviceAdmin.getUtilisateurById(r.getUserId());
                if (user != null) {
                    String nom = user.getNom();
                    String categorie = user.getCategories().toString();
                    detailAuteurLabel.setText(nom + " (" + categorie + ")");
                } else {
                    detailAuteurLabel.setText("Utilisateur #" + r.getUserId());
                }
            } catch (Exception e) {
                detailAuteurLabel.setText("Utilisateur #" + r.getUserId());
            }
        }
        // ✅ Réinitialiser le label de traduction
        if (traductionMessageLabel != null) {
            traductionMessageLabel.setText("");
        }
        // ✅ AJOUT : Afficher les détails spécifiques
        if (detailInfosLabel != null) {
            String infos = getDetailsComplets(r);
            detailInfosLabel.setText(infos);
        }


        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        if (r.getDateReclamation() != null && detailDateLabel != null) {
            detailDateLabel.setText(format.format(r.getDateReclamation()));
        }

    }


    private void chargerReponseExistante(int reclamationId) {
        try {
            reponseExistante = reponseService.getByReclamationId(reclamationId);

            if (reponseExistante != null && reponseExistanteBox != null) {
                if (reponseExistanteArea != null) {
                    reponseExistanteArea.setText(reponseExistante.getMessage());
                }

                if (reponseDateLabel != null) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                    reponseDateLabel.setText("Réponse du " + reponseExistante.getDate().format(formatter));
                }

                if (reponseAuteurLabel != null) {
                    reponseAuteurLabel.setText("Par : Administrateur");
                }

                reponseExistanteBox.setManaged(true);
                reponseExistanteBox.setVisible(true);
                reponseField.setText(reponseExistante.getMessage());
                statusLabel.setText("✅ Réponse existante chargée");

            } else if (reponseExistanteBox != null) {
                reponseExistanteBox.setManaged(false);
                reponseExistanteBox.setVisible(false);
                reponseField.clear();
                statusLabel.setText("Aucune réponse pour cette réclamation");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("❌ Erreur lors du chargement de la réponse");
        }
    }

    // ================= ACTIONS =================

    @FXML
    private void repondreReclamation() {
        // Vérifier les droits
        CategorieUser currentUserRole = AppSession.getInstance().getConnectedUserRoleEnum();
        if (currentUserRole != CategorieUser.ADMIN && currentUserRole != CategorieUser.RESPONSABLEECOLE) {
            showAlert("Accès refusé", "Seuls les administrateurs et responsables d'école peuvent répondre", Alert.AlertType.WARNING);
            return;
        }

        // ✅ Vérification pour responsable école
        if (currentUserRole == CategorieUser.RESPONSABLEECOLE) {
            int ecoleIdResponsable = AppSession.getInstance().getEcoleId();
            int ecoleIdReclamation = reclamationSelectionnee.getIdEcole();
            if (ecoleIdReclamation != ecoleIdResponsable) {
                showAlert("Accès refusé", "Vous ne pouvez répondre qu'aux réclamations de votre école.", Alert.AlertType.WARNING);
                return;
            }
        }

        if (!validerSelectionEtReponse()) return;
        String reponseTexte = reponseField.getText().trim();
        try {
            // ✅ Récupérer l'ID de l'utilisateur connecté
            int userId = AppSession.getInstance().getConnectedUserId();

            if (reponseExistante != null) {
                // Modifier la réponse existante
                reponseExistante.setMessage(reponseTexte);
                reponseExistante.setDate(LocalDateTime.now());
                reponseExistante.setUserId(userId);  // ✅ NOUVEAU
                reponseService.update(reponseExistante);
                showAlert("Succès", "✅ Réponse modifiée avec succès !", Alert.AlertType.INFORMATION);
            } else {
                // Créer une nouvelle réponse
                Reponse nouvelleReponse = new Reponse(
                        reclamationSelectionnee.getId(),
                        userId,  // ✅ NOUVEAU : ID de l'utilisateur connecté
                        reponseTexte,
                        LocalDateTime.now()
                );
                reponseService.insertOne(nouvelleReponse);
                showAlert("Succès", "✅ Réponse envoyée avec succès !", Alert.AlertType.INFORMATION);
            }

            // Mettre à jour le statut de la réclamation
            reclamationSelectionnee.setStatut("TRAITEE");
            reclamationService.updateOne(reclamationSelectionnee);

            afficherToutesReclamations();
            chargerReponseExistante(reclamationSelectionnee.getId());

        } catch (SQLException e) {
            showAlert("Erreur", "❌ Erreur : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
    @FXML
    private void modifierReponse() {
        // ✅ Vérifier si l'utilisateur peut modifier
        CategorieUser currentUserRole = AppSession.getInstance().getConnectedUserRoleEnum();
        if (currentUserRole != CategorieUser.ADMIN && currentUserRole != CategorieUser.RESPONSABLEECOLE) {
            showAlert("Accès refusé", "Seuls les administrateurs et responsables d'école peuvent modifier", Alert.AlertType.WARNING);
            return;
        }
        if (reponseExistante == null) {
            showAlert("Erreur", "❌ Aucune réponse à modifier !", Alert.AlertType.WARNING);
            return;
        }

        String reponseTexte = reponseField.getText().trim();

        if (!validerReponse(reponseTexte)) return;

        if (reponseExistante.getMessage().equals(reponseTexte)) {
            showAlert("Information", "ℹ️ Aucune modification détectée", Alert.AlertType.INFORMATION);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Modifier la réponse");
        confirm.setContentText("Voulez-vous vraiment modifier cette réponse ?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                reponseExistante.setMessage(reponseTexte);
                reponseExistante.setDate(LocalDateTime.now());
                reponseService.update(reponseExistante);

                showAlert("Succès", "✅ Réponse modifiée avec succès !", Alert.AlertType.INFORMATION);
                chargerReponseExistante(reclamationSelectionnee.getId());

            } catch (SQLException e) {
                showAlert("Erreur", "❌ Erreur : " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void supprimerReponse() {
        // ✅ Vérifier si l'utilisateur peut supprimer
        CategorieUser currentUserRole = AppSession.getInstance().getConnectedUserRoleEnum();
        if (currentUserRole != CategorieUser.ADMIN && currentUserRole != CategorieUser.RESPONSABLEECOLE) {
            showAlert("Accès refusé", "Seuls les administrateurs et responsables d'école peuvent supprimer", Alert.AlertType.WARNING);
            return;
        }
        try {
            // Vérifications
            if (reponseExistante == null) {
                showAlert("Erreur", "❌ Aucune réponse à supprimer !", Alert.AlertType.WARNING);
                return;
            }

            if (reclamationSelectionnee == null) {
                showAlert("Erreur", "❌ Aucune réclamation sélectionnée !", Alert.AlertType.WARNING);
                return;
            }

            // Confirmation
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmation de suppression");
            confirm.setHeaderText("Supprimer la réponse");
            confirm.setContentText("Êtes-vous sûr de vouloir supprimer cette réponse ?");

            if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                try {
                    // ✅ 1. Supprimer la réponse
                    reponseService.delete(reponseExistante.getId());

                    // ✅ 2. FORCER le changement de statut de la réclamation
                    reclamationSelectionnee.setStatut("EN_ATTENTE");

                    // ✅ 3. Mettre à jour dans la base de données
                    reclamationService.updateOne(reclamationSelectionnee);

                    // ✅ 4. Rafraîchir l'affichage
                    afficherToutesReclamations();

                    // ✅ 5. Réinitialiser les variables
                    reponseExistante = null;

                    // ✅ 6. Mettre à jour l'interface
                    if (reponseExistanteBox != null) {
                        reponseExistanteBox.setManaged(false);
                        reponseExistanteBox.setVisible(false);
                    }
                    if (reponseField != null) {
                        reponseField.clear();
                    }

                    // ✅ 7. Afficher le nouveau statut dans la console (pour vérification)
                    System.out.println("✅ Statut mis à jour: " + reclamationSelectionnee.getStatut());

                    showAlert("Succès", "✅ Réponse supprimée et statut mis à jour !", Alert.AlertType.INFORMATION);

                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert("Erreur", "❌ Erreur base de données: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "❌ Erreur inattendue: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void supprimerReponseDebug() {
        try {
            // Afficher des informations de débogage
            System.out.println("=== DÉBOGAGE SUPPRESSION ===");
            System.out.println("reponseExistante: " + (reponseExistante != null ?
                    "ID=" + reponseExistante.getId() : "null"));
            System.out.println("reclamationSelectionnee: " + (reclamationSelectionnee != null ?
                    "ID=" + reclamationSelectionnee.getId() : "null"));
            System.out.println("reponseExistanteBox: " + (reponseExistanteBox != null ?
                    "visible=" + reponseExistanteBox.isVisible() : "null"));

            // Appeler la méthode normale
            supprimerReponse();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void rechercherReponse() {
        String keyword = searchField.getText().trim();

        if (keyword.isEmpty()) {
            afficherToutesReclamations();
            statusLabel.setText("Affichage de toutes les réclamations");
            return;
        }

        if (keyword.length() < 2) {
            showAlert("Information", "ℹ️ Minimum 2 caractères", Alert.AlertType.INFORMATION);
            return;
        }

        try {
            List<Reclamation> resultats = reclamationService.rechercherParMotCle(keyword);

            if (resultats.isEmpty()) {
                showAlert("Résultat", "ℹ️ Aucune réclamation trouvée", Alert.AlertType.INFORMATION);
                statusLabel.setText("🔍 Aucun résultat pour : " + keyword);
            } else {
                statusLabel.setText("🔍 " + resultats.size() + " résultat(s)");
            }

            tableReclamation.setItems(FXCollections.observableArrayList(resultats));

        } catch (SQLException e) {
            showAlert("Erreur", "❌ Erreur recherche", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void reinitialiserRecherche() {
        searchField.clear();
        afficherToutesReclamations();
        statusLabel.setText("Affichage de toutes les réclamations");
    }

    // ================= UTILITAIRES =================

    private void afficherToutesReclamations() {
        try {
            CategorieUser currentUserRole = AppSession.getInstance().getConnectedUserRoleEnum();

            System.out.println("=== DÉBOGAGE AFFICHAGE RÉCLAMATIONS ===");
            System.out.println("Rôle: " + currentUserRole);

            List<Reclamation> reclamations;

            if (currentUserRole == CategorieUser.RESPONSABLEECOLE) {
                int ecoleId = AppSession.getInstance().getEcoleId();
                System.out.println("ID École du responsable: " + ecoleId);

                // Récupérer les réclamations avec la méthode
                reclamations = reclamationService.getReclamationsByEcoleId(ecoleId);

                System.out.println("Nombre de réclamations trouvées: " + reclamations.size());
                for (Reclamation r : reclamations) {
                    System.out.println("  - ID: " + r.getId() + ", Type: " + r.getType() +
                            ", id_ecole: " + r.getIdEcole() +
                            ", id_chauffeur: " + r.getIdChauffeur() +
                            ", id_bus: " + r.getIdBus());
                }

                statusLabel.setText("Affichage des réclamations de votre école (" + reclamations.size() + ")");
            }
            else if (currentUserRole == CategorieUser.ADMIN) {
                reclamations = reclamationService.selectAll();
                statusLabel.setText("Affichage de toutes les réclamations");
            }
            else {
                reclamations = new ArrayList<>();
                statusLabel.setText("Vous n'avez pas accès à cette interface");
            }

            ObservableList<Reclamation> data = FXCollections.observableArrayList(reclamations);
            tableReclamation.setItems(data);
            mettreAJourStatistiques(reclamations);
            tableReclamation.refresh();

        } catch (SQLException e) {
            statusLabel.setText("❌ Erreur chargement");
            e.printStackTrace();
        }
    }
    private boolean validerSelectionEtReponse() {
        if (reclamationSelectionnee == null) {
            showAlert("Erreur", "❌ Veuillez sélectionner une réclamation !", Alert.AlertType.WARNING);
            return false;
        }

        String texte = reponseField.getText();
        return validerReponse(texte);
    }

    private boolean validerReponse(String texte) {
        if (texte == null || texte.trim().isEmpty()) {
            showAlert("Erreur", "❌ La réponse ne peut pas être vide !", Alert.AlertType.WARNING);
            reponseField.requestFocus();
            return false;
        }

        if (texte.trim().length() < 5) {
            showAlert("Erreur", "❌ Minimum 5 caractères !", Alert.AlertType.WARNING);
            reponseField.requestFocus();
            return false;
        }

        if (texte.length() > 500) {
            showAlert("Erreur", "❌ Maximum 500 caractères !", Alert.AlertType.WARNING);
            reponseField.requestFocus();
            return false;
        }

        return true;
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

}