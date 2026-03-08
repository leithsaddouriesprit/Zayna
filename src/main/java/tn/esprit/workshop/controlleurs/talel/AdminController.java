package tn.esprit.workshop.controlleurs.talel;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.application.Platform;
import tn.esprit.workshop.model.talel.CategorieUser;
import tn.esprit.workshop.model.talel.User;
import tn.esprit.workshop.services.ServiceAdmin;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class AdminController implements Initializable {

    // ==================== COMPOSANTS FXML ====================

    @FXML
    private TableView<User> userTable;

    @FXML
    private TableColumn<User, Integer> colId;

    @FXML
    private TableColumn<User, String> colNom;

    @FXML
    private TableColumn<User, String> colEmail;

    @FXML
    private TableColumn<User, String> colPassword;

    @FXML
    private TableColumn<User, CategorieUser> colCategorie;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<CategorieUser> categoryFilterCombo;

    @FXML
    private Label totalUsersLabel;

    @FXML
    private Label parentCountLabel;

    @FXML
    private Label maitresseCountLabel;

    @FXML
    private Label adminCountLabel;

    @FXML
    private Label responsableEcoleCountLabel;

    @FXML
    private Label chauffeurCountLabel;

    @FXML
    private Button addButton;

    @FXML
    private Button editButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button refreshButton;

    @FXML
    private Button clearSearchButton;

    @FXML
    private Button logoutButton;

    @FXML
    private Label statusLabel;

    // ==================== CHAMPS DU FORMULAIRE ====================

    @FXML
    private GridPane userForm;

    @FXML
    private TextField idField;

    @FXML
    private TextField txtNom;

    @FXML
    private TextField txtEmail;

    @FXML
    private PasswordField txtMotDePasse;

    @FXML
    private ComboBox<CategorieUser> categorieCombo;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    // ==================== ATTRIBUTS ====================

    private ServiceAdmin serviceAdmin;
    private ObservableList<User> userList = FXCollections.observableArrayList();
    private FilteredList<User> filteredData;
    private User selectedUser;
    private User currentUser;

    // ==================== INITIALISATION ====================

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("=== AdminController initialisé ===");
        serviceAdmin = new ServiceAdmin();

        try {
            // CORRECTION 1: Configuration du label status
            if (statusLabel != null) {
                statusLabel.setVisible(false);
                statusLabel.setManaged(true);
                statusLabel.setWrapText(true);
            }

            setupTableColumns();
            setupComboBoxes();
            loadUserData();
            setupSearchFilter();
            setupTableSelection();
            hideForm();
            updateStatistics();
            System.out.println("✅ Initialisation terminée avec succès");
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'initialisation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPassword.setCellValueFactory(new PropertyValueFactory<>("password"));
        colCategorie.setCellValueFactory(new PropertyValueFactory<>("categories"));

        // Formater l'affichage du mot de passe
        colPassword.setCellFactory(column -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("••••••••");
                }
            }
        });

        // Formater l'affichage de la catégorie avec couleurs
        colCategorie.setCellFactory(column -> new TableCell<User, CategorieUser>() {
            @Override
            protected void updateItem(CategorieUser item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString());
                    switch (item) {
                        case ADMIN:
                            setStyle("-fx-text-fill: #9C27B0; -fx-font-weight: bold;");
                            break;
                        case PARENT:
                            setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                            break;
                        case MAITRESSE:
                            setStyle("-fx-text-fill: #FF9800; -fx-font-weight: bold;");
                            break;
                        case RESPONSABLEECOLE:
                            setStyle("-fx-text-fill: #2196F3; -fx-font-weight: bold;");
                            break;
                        case CHAUFFEUR:
                            setStyle("-fx-text-fill: #F44336; -fx-font-weight: bold;");
                            break;
                    }
                }
            }
        });
    }

    private void setupComboBoxes() {
        categoryFilterCombo.getItems().addAll(CategorieUser.values());
        categoryFilterCombo.setPromptText("Toutes les catégories");

        categorieCombo.getItems().addAll(CategorieUser.values());
        categorieCombo.setPromptText("Sélectionner une catégorie");
    }

    private void setupSearchFilter() {
        filteredData = new FilteredList<>(userList, p -> true);
        SortedList<User> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(userTable.comparatorProperty());
        userTable.setItems(sortedData);
    }

    private void setupTableSelection() {
        userTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    selectedUser = newValue;
                    if (selectedUser != null) {
                        fillFormWithUser(selectedUser);
                        showForm();
                    }
                }
        );
    }

    // ==================== MÉTHODES POUR LES BOUTONS ====================

    @FXML
    private void handleAddButton() {
        System.out.println("=== Ajouter utilisateur ===");
        selectedUser = null;
        clearForm();
        showForm();
        idField.setDisable(true);
        txtMotDePasse.setDisable(false);
        txtMotDePasse.setPromptText("Mot de passe (obligatoire)");
    }

    @FXML
    private void handleEditButton() {
        System.out.println("=== Modifier utilisateur ===");
        if (selectedUser == null) {
            afficherMessage("Veuillez sélectionner un utilisateur à modifier", "warning");
            return;
        }
        txtMotDePasse.setDisable(false);
        txtMotDePasse.setPromptText("Mot de passe (laisser vide pour ne pas changer)");
        showForm();
    }

    @FXML
    private void handleDeleteButton() {
        System.out.println("=== Supprimer utilisateur ===");
        if (selectedUser == null) {
            afficherMessage("Veuillez sélectionner un utilisateur à supprimer", "warning");
            return;
        }

        if (currentUser != null && selectedUser.getId() == currentUser.getId()) {
            afficherMessage("Vous ne pouvez pas supprimer votre propre compte", "error");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Supprimer l'utilisateur");
        confirmation.setContentText("Voulez-vous vraiment supprimer " +
                selectedUser.getNom() + " (" + selectedUser.getCategories() + ") ?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                serviceAdmin.supprimerUtilisateur(selectedUser.getId());
                userList.remove(selectedUser);
                hideForm();
                updateStatistics();
                afficherMessage("✅ Utilisateur supprimé avec succès", "success");
                System.out.println("Suppression réussie");
            } catch (Exception e) {
                System.err.println("❌ Erreur suppression: " + e.getMessage());
                e.printStackTrace();
                afficherMessage("❌ Erreur : " + e.getMessage(), "error");
            }
        }
    }

    @FXML
    private void handleRefreshButton() {
        System.out.println("=== Actualiser les données ===");
        refreshData();
    }

    @FXML
    private void handleClearSearchButton() {
        System.out.println("=== Effacer les filtres ===");
        clearSearch();
    }

    @FXML
    private void handleLogoutButton() {
        System.out.println("=== Déconnexion ===");

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Déconnexion");
        confirmation.setHeaderText("Confirmation de déconnexion");
        confirmation.setContentText("Êtes-vous sûr de vouloir vous déconnecter ?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ConnecterUser.fxml"));
                Parent root = loader.load();

                Scene currentScene = logoutButton.getScene();
                Stage stage = (Stage) currentScene.getWindow();

                stage.setScene(new Scene(root));
                stage.setTitle("Connexion - Système de Gestion");
                stage.centerOnScreen();

                System.out.println("✅ Déconnexion réussie");
            } catch (Exception e) {
                System.err.println("❌ Erreur déconnexion: " + e.getMessage());
                e.printStackTrace();
                afficherMessage("Erreur lors de la déconnexion", "error");
            }
        }
    }

    @FXML
    private void handleSaveButton() {
        System.out.println("=== Sauvegarder utilisateur ===");
        try {
            // Récupération des données
            String nom = txtNom.getText().trim();
            String email = txtEmail.getText().trim().toLowerCase();
            String passwordClair = txtMotDePasse.getText();
            CategorieUser categorie = categorieCombo.getValue();

            // CORRECTION 2: Validation CHAMP PAR CHAMP avec focus
            // Validation du nom
            if (nom.isEmpty()) {
                afficherMessage("❌ Le nom est obligatoire", "error");
                txtNom.requestFocus();
                return;
            }

            // Validation de l'email
            if (email.isEmpty()) {
                afficherMessage("❌ L'email est obligatoire", "error");
                txtEmail.requestFocus();
                return;
            }

            // Validation de la catégorie
            if (categorie == null) {
                afficherMessage("❌ La catégorie est obligatoire", "error");
                categorieCombo.requestFocus();
                return;
            }

            // Validation email
            if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
                afficherMessage("❌ Format d'email invalide (ex: nom@domaine.com)", "error");
                txtEmail.requestFocus();
                return;
            }

            // Validation mot de passe pour nouvel utilisateur
            if (selectedUser == null) {
                if (passwordClair.isEmpty()) {
                    afficherMessage("❌ Le mot de passe est obligatoire pour un nouvel utilisateur", "error");
                    txtMotDePasse.requestFocus();
                    return;
                }
                if (passwordClair.length() < 6) {
                    afficherMessage("❌ Le mot de passe doit contenir au moins 6 caractères", "error");
                    txtMotDePasse.requestFocus();
                    return;
                }
                if (passwordClair.length() > 30) {
                    afficherMessage("❌ Le mot de passe ne doit pas dépasser 30 caractères", "error");
                    txtMotDePasse.requestFocus();
                    return;
                }
            } else {
                // Validation mot de passe pour modification (si fourni)
                if (!passwordClair.isEmpty()) {
                    if (passwordClair.length() < 6) {
                        afficherMessage("❌ Le mot de passe doit contenir au moins 6 caractères", "error");
                        txtMotDePasse.requestFocus();
                        return;
                    }
                    if (passwordClair.length() > 30) {
                        afficherMessage("❌ Le mot de passe ne doit pas dépasser 30 caractères", "error");
                        txtMotDePasse.requestFocus();
                        return;
                    }
                }
            }

            if (selectedUser == null) {
                // AJOUT d'un nouvel utilisateur
                User user = new User();
                user.setNom(nom);
                user.setEmail(email);
                user.setCategories(categorie);

                serviceAdmin.ajouterUtilisateur(user, passwordClair);

                loadUserData();
                afficherMessage("✅ Utilisateur ajouté avec succès", "success");
                System.out.println("Ajout réussi");

            } else {
                // MODIFICATION d'un utilisateur existant
                selectedUser.setNom(nom);
                selectedUser.setEmail(email);
                selectedUser.setCategories(categorie);

                serviceAdmin.modifierUtilisateur(selectedUser);

                userTable.refresh();
                afficherMessage("✅ Utilisateur modifié avec succès", "success");
                System.out.println("Modification réussie");
            }

            hideForm();
            updateStatistics();

        } catch (Exception e) {
            System.err.println("❌ Erreur sauvegarde: " + e.getMessage());
            e.printStackTrace();
            afficherMessage("❌ Erreur : " + e.getMessage(), "error");
        }
    }

    @FXML
    private void handleCancelButton() {
        System.out.println("=== Annuler ===");
        hideForm();
    }

    // ==================== MÉTHODES DE RECHERCHE ====================

    @FXML
    private void handleSearchField() {
        applySearchFilter(searchField.getText(), categoryFilterCombo.getValue());
    }

    @FXML
    private void handleCategoryFilter() {
        applySearchFilter(searchField.getText(), categoryFilterCombo.getValue());
    }

    private void applySearchFilter(String searchText, CategorieUser category) {
        filteredData.setPredicate(user -> {
            if ((searchText == null || searchText.isEmpty()) && category == null) {
                return true;
            }

            boolean matchesSearch = true;
            boolean matchesCategory = true;

            if (searchText != null && !searchText.isEmpty()) {
                String lowerCaseFilter = searchText.toLowerCase();
                matchesSearch = user.getNom().toLowerCase().contains(lowerCaseFilter) ||
                        user.getEmail().toLowerCase().contains(lowerCaseFilter) ||
                        String.valueOf(user.getId()).contains(searchText) ||
                        user.getCategories().toString().toLowerCase().contains(lowerCaseFilter);
            }

            if (category != null) {
                matchesCategory = user.getCategories() == category;
            }

            return matchesSearch && matchesCategory;
        });
        updateStatistics();
    }

    private void clearSearch() {
        searchField.clear();
        categoryFilterCombo.setValue(null);
        applySearchFilter("", null);
        afficherMessage("Filtres effacés", "info");
    }

    // ==================== AUTRES MÉTHODES ====================

    private void loadUserData() {
        try {
            userList.clear();
            userList.addAll(serviceAdmin.getAllUtilisateurs());
            updateStatistics();
            System.out.println("✅ Données chargées: " + userList.size() + " utilisateurs");
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement: " + e.getMessage());
            e.printStackTrace();
            afficherMessage("Erreur lors du chargement des données", "error");
        }
    }

    private void updateStatistics() {
        try {
            int total = filteredData.size();

            long parentCount = filteredData.stream()
                    .filter(u -> u.getCategories() == CategorieUser.PARENT)
                    .count();
            long maitresseCount = filteredData.stream()
                    .filter(u -> u.getCategories() == CategorieUser.MAITRESSE)
                    .count();
            long adminCount = filteredData.stream()
                    .filter(u -> u.getCategories() == CategorieUser.ADMIN)
                    .count();
            long responsableCount = filteredData.stream()
                    .filter(u -> u.getCategories() == CategorieUser.RESPONSABLEECOLE)
                    .count();
            long chauffeurCount = filteredData.stream()
                    .filter(u -> u.getCategories() == CategorieUser.CHAUFFEUR)
                    .count();

            totalUsersLabel.setText("Total: " + total);
            parentCountLabel.setText("👪 Parents: " + parentCount);
            maitresseCountLabel.setText("👩‍🏫 Maîtresses: " + maitresseCount);
            adminCountLabel.setText("👑 Admins: " + adminCount);
            responsableEcoleCountLabel.setText("🏫 Responsables: " + responsableCount);
            chauffeurCountLabel.setText("🚗 Chauffeurs: " + chauffeurCount);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fillFormWithUser(User user) {
        idField.setText(String.valueOf(user.getId()));
        txtNom.setText(user.getNom());
        txtEmail.setText(user.getEmail());
        txtMotDePasse.clear();
        categorieCombo.setValue(user.getCategories());
    }

    private void clearForm() {
        idField.clear();
        txtNom.clear();
        txtEmail.clear();
        txtMotDePasse.clear();
        txtMotDePasse.setPromptText("Mot de passe");
        categorieCombo.setValue(null);
    }

    private void showForm() {
        userForm.setVisible(true);
        userForm.setManaged(true);
        addButton.setDisable(true);
        editButton.setDisable(true);
        deleteButton.setDisable(true);
        logoutButton.setDisable(true);
    }

    private void hideForm() {
        userForm.setVisible(false);
        userForm.setManaged(false);
        addButton.setDisable(false);
        editButton.setDisable(false);
        deleteButton.setDisable(false);
        logoutButton.setDisable(false);
        userTable.getSelectionModel().clearSelection();
    }

    private void refreshData() {
        loadUserData();
        clearSearch();
        hideForm();
        afficherMessage("Données actualisées", "success");
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            afficherMessage("👋 Bienvenue " + user.getNom() + " (" + user.getCategories() + ")", "success");
        }
    }

    // CORRECTION 3: Méthode afficherMessage avec setStyle() au lieu de getStyleClass()
    private void afficherMessage(String message, String type) {
        Platform.runLater(() -> {
            if (statusLabel == null) {
                System.err.println("statusLabel est null!");
                return;
            }

            statusLabel.setText(message);
            statusLabel.setVisible(true);

            // Application des couleurs selon le type - CORRIGÉ
            switch (type) {
                case "success":
                    statusLabel.setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724; -fx-border-color: #c3e6cb; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 10; -fx-font-weight: bold;");
                    break;
                case "error":
                    statusLabel.setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24; -fx-border-color: #f5c6cb; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 10; -fx-font-weight: bold;");
                    break;
                case "warning":
                    statusLabel.setStyle("-fx-background-color: #fff3cd; -fx-text-fill: #856404; -fx-border-color: #ffeeba; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 10; -fx-font-weight: bold;");
                    break;
                case "info":
                default:
                    statusLabel.setStyle("-fx-background-color: #d1ecf1; -fx-text-fill: #0c5460; -fx-border-color: #bee5eb; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 10; -fx-font-weight: bold;");
                    break;
            }

            // Cache le message après 5 secondes
            new Thread(() -> {
                try {
                    Thread.sleep(5000);
                    Platform.runLater(() -> {
                        if (statusLabel != null) {
                            statusLabel.setVisible(false);
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        });
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}