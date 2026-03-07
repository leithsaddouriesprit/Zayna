package tn.esprit.workshop.controlleurs.talel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.esprit.workshop.model.talel.Parent;
import tn.esprit.workshop.model.talel.User;
import tn.esprit.workshop.services.talel.ServiceParent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.Optional;

public class ControleurParent {

    @FXML
    private TextField txtNom;
    @FXML
    private TextField txtEmail;
    @FXML
    private PasswordField txtMotDePasse;
    @FXML
    private TextField txtTelephone;
    @FXML
    private TextArea txtAdresse;
    @FXML
    private TextField txtProfession;
    @FXML
    private TextField txtEnfant;
    @FXML
    private ListView<String> listeEnfants;
    @FXML
    private Label lblMessage;
    @FXML
    private Button logoutButton;

    private ServiceParent serviceParent;
    private ObservableList<String> enfantsList = FXCollections.observableArrayList();
    private User connectedUser;

    @FXML
    public void initialize() {
        serviceParent = new ServiceParent();
        listeEnfants.setItems(enfantsList);
    }

    @FXML
    private void ajouterParent() {
        try {
            // Validation des champs obligatoires
            if (txtNom.getText().isEmpty() || txtEmail.getText().isEmpty() ||
                    txtMotDePasse.getText().isEmpty() || txtTelephone.getText().isEmpty()) {
                afficherMessage("Veuillez remplir tous les champs obligatoires", "warning");
                return;
            }

            // Validation email
            if (!txtEmail.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                afficherMessage("Format d'email invalide", "error");
                return;
            }

            // Validation téléphone
            if (!txtTelephone.getText().matches("\\d{8}")) {
                afficherMessage("Le téléphone doit contenir 8 chiffres", "error");
                return;
            }

            // Création de l'objet Parent
            Parent parent = new Parent();
            parent.setNom(txtNom.getText().trim());
            parent.setEmail(txtEmail.getText().trim().toLowerCase());
            // On ne set pas le mot de passe ici, on le passera au service
            parent.setTelephone(txtTelephone.getText().trim());
            parent.setAdresse(txtAdresse.getText().trim());
            parent.setProfession(txtProfession.getText().trim());

            // Ajouter les enfants
            for (String enfant : enfantsList) {
                parent.addEnfant(enfant);
            }

            // Récupérer le mot de passe en clair (passwordClair)
            String passwordClair = txtMotDePasse.getText();
// AJOUTEZ CES VALIDATIONS
            if (passwordClair.length() < 6) {
                afficherMessage("Le mot de passe doit contenir au moins 6 caractères", "error");
                return;
            }
            if (passwordClair.length() > 30) {
                afficherMessage("Le mot de passe ne doit pas dépasser 30 caractères", "error");
                return;
            }
            // Appel au service avec le paramètre passwordClair
            serviceParent.ajouterParent(parent, passwordClair);

            afficherMessage("Parent ajouté avec succès ! ID: " + parent.getId(), "success");
            viderFormulaire();

        } catch (Exception e) {
                System.err.println("=== ERREUR DÉTAILLÉE ===");
                System.err.println("Type d'erreur: " + e.getClass().getName());
                System.err.println("Message: " + e.getMessage());
                System.err.println("Cause: " + e.getCause());
                System.err.println("Stack trace:");
                e.printStackTrace();  // CECI EST CRITIQUE

                // Message plus détaillé pour l'utilisateur
                String errorMsg = e.getMessage() != null ? e.getMessage() : "Erreur inconnue (voir console)";
                afficherMessage("Erreur : " + errorMsg, "error");
        }
    }

    @FXML
    private void ajouterEnfant() {
        String enfant = txtEnfant.getText().trim();
        if (enfant.isEmpty()) {
            afficherMessage("Veuillez saisir le nom de l'enfant", "warning");
            return;
        }

        if (!enfantsList.contains(enfant)) {
            enfantsList.add(enfant);
            txtEnfant.clear();
        } else {
            afficherMessage("Cet enfant est déjà dans la liste", "warning");
        }
    }

    @FXML
    private void supprimerEnfant() {
        String selected = listeEnfants.getSelectionModel().getSelectedItem();
        if (selected != null) {
            enfantsList.remove(selected);
        }
    }

    @FXML
    private void viderFormulaire() {
        txtNom.clear();
        txtEmail.clear();
        txtMotDePasse.clear();
        txtTelephone.clear();
        txtAdresse.clear();
        txtProfession.clear();
        enfantsList.clear();
        txtEnfant.clear();
    }

    private void afficherMessage(String message, String type) {
        lblMessage.setText(message);
        lblMessage.getStyleClass().removeAll("success", "error", "warning");
        lblMessage.getStyleClass().add(type);
        lblMessage.setVisible(true);

        new Thread(() -> {
            try {
                Thread.sleep(5000);
                javafx.application.Platform.runLater(() -> lblMessage.setVisible(false));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void setUser(User user) {
        this.connectedUser = user;
    }

    @FXML
    private void handleLogoutButton() {
        System.out.println("Clic sur Déconnexion");
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Déconnexion");
        confirmation.setHeaderText("Confirmation de déconnexion");
        confirmation.setContentText("Êtes-vous sûr de vouloir vous déconnecter ?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ConnecterUser.fxml"));
                javafx.scene.Parent root = loader.load();
                Scene currentScene = logoutButton.getScene();
                Stage stage = (Stage) currentScene.getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Connexion - Système de Gestion");
                stage.centerOnScreen();
                System.out.println("Déconnexion réussie");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Erreur lors de la déconnexion",
                        "Impossible de retourner à la page de connexion: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}