package tn.esprit.workshop.controlleurs.talel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.esprit.workshop.model.talel.CategorieUser;
import tn.esprit.workshop.model.talel.DaoUser;
import tn.esprit.workshop.model.talel.User;
import tn.esprit.workshop.services.UserService;


public class ConnecterUserController {

        @FXML
        private TextField emailField;

        @FXML
        private PasswordField passwordField;

        @FXML
        private void connecter() {
            String email = emailField.getText();
            String password = passwordField.getText();

            // Ici tu ajoutes la logique de connexion
            System.out.println("Email: " + email + ", Mot de passe: " + password);
        }

        @FXML
        private void creerCompte(ActionEvent event) {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/CreationCompte.fxml"));
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    @FXML
    public boolean AddUser(String nom, String email, String motDePasse, CategorieUser categorie) {
        User u = new User();
        u.setNom(nom);
        u.setEmail(email);
        u.setMotDePasse(motDePasse);
        u.setCategories(categorie);

        try {
            DaoUser.CrieerUser(u);
            return true;
        } catch (Exception e) {
            System.out.println("Erreur ajout user : " + e.getMessage());
            return false;
        }
    }

    public void AddUser(ActionEvent actionEvent) {
        User u = new User();
        u.setNom(nom);
        u.setEmail(email);
        u.setMotDePasse(motDePasse);
        u.setCategories(categorie);

        try {
            DaoUser.CrieerUser(u);
            return true;
        } catch (Exception e) {
            System.out.println("Erreur ajout user : " + e.getMessage());
            return false;
        }
        }
}




