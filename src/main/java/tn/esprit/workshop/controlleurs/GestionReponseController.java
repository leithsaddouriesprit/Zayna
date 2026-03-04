package tn.esprit.workshop.controlleurs;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import tn.esprit.workshop.model.Reclamation;
import tn.esprit.workshop.model.Reponse;
import tn.esprit.workshop.services.ReclamationService;
import tn.esprit.workshop.services.ReponseService;

import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import tn.esprit.workshop.services.TraductionService;


public class GestionReponseController implements Initializable {

    // Table des réclamations
    @FXML private TableView<Reclamation> tableReclamation;
    @FXML private TableColumn<Reclamation, Integer> colId;
    @FXML private TableColumn<Reclamation, String> colType;
    @FXML private TableColumn<Reclamation, String> colDescription;
    @FXML private TableColumn<Reclamation, String> colDetails;
    @FXML private TableColumn<Reclamation, String> colStatut;
    @FXML private TableColumn<Reclamation, Timestamp> colDate;
    @FXML private Label detailInfosLabel;

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

    // Formulaire de réponse
    @FXML private TextArea reponseField;
    @FXML private TextField searchField;
    @FXML private Label statusLabel;
    // ✅ NOUVEAUX CHAMPS POUR LA TRADUCTION
    @FXML private ChoiceBox<String> langueCibleChoice;
    @FXML private Label traductionMessageLabel;
    @FXML private Button traduireMessageButton;

    // Map pour stocker les codes ISO des langues
    private Map<String, String> languesMap;
    private final ReclamationService reclamationService = new ReclamationService();
    private final ReponseService reponseService = new ReponseService();

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
        colId.setCellFactory(column -> new TableCell<Reclamation, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(""); // Ne rien afficher
                // Optionnel : mettre une icône ou un symbole à la place
                // if (!empty) setText("📌");
            }
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

        if (detailUserLabel != null) {
            detailUserLabel.setText("Utilisateur #" + r.getUserId());
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
        if (!validerSelectionEtReponse()) return;

        String reponseTexte = reponseField.getText().trim();

        try {
            if (reponseExistante != null) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Confirmation");
                confirm.setHeaderText("Une réponse existe déjà");
                confirm.setContentText("Voulez-vous remplacer la réponse existante ?");

                if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                    return;
                }

                reponseExistante.setMessage(reponseTexte);
                reponseExistante.setDate(LocalDateTime.now());
                reponseService.update(reponseExistante);
                showAlert("Succès", "✅ Réponse modifiée avec succès !", Alert.AlertType.INFORMATION);

            } else {
                Reponse nouvelleReponse = new Reponse(
                        reclamationSelectionnee.getId(),
                        reponseTexte,
                        LocalDateTime.now()
                );
                reponseService.insertOne(nouvelleReponse);
                showAlert("Succès", "✅ Réponse envoyée avec succès !", Alert.AlertType.INFORMATION);
            }

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
        if (reponseExistante == null) {
            showAlert("Erreur", "❌ Aucune réponse à supprimer !", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de suppression");
        confirm.setHeaderText("Supprimer la réponse");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer cette réponse ?\nCette action est irréversible.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                reponseService.delete(reponseExistante.getId());

                reclamationSelectionnee.setStatut("EN_ATTENTE");
                reclamationService.updateOne(reclamationSelectionnee);

                showAlert("Succès", "✅ Réponse supprimée !", Alert.AlertType.INFORMATION);

                afficherToutesReclamations();
                reponseExistante = null;
                reponseExistanteBox.setManaged(false);
                reponseExistanteBox.setVisible(false);
                reponseField.clear();

            } catch (SQLException e) {
                showAlert("Erreur", "❌ Erreur : " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
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
            List<Reclamation> reclamations = reclamationService.selectAll();
            tableReclamation.setItems(FXCollections.observableArrayList(reclamations));

            if (!reclamations.isEmpty()) {
                tableReclamation.getSelectionModel().selectFirst();
            }
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