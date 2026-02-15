package tn.esprit.workshop.controlleurs.talel;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import tn.esprit.workshop.model.talel.CategorieUser;

public class CreationCompteController {

    @FXML
    private TextField nomField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField motDePasseField;

    @FXML
    private TextField categoriesField;


    @FXML
    private void creerCompte() {
        String nom = nomField.getText();
        String email = emailField.getText();
        String mdp = motDePasseField.getText();
        String categorieTexte = categoriesField.getText().toUpperCase();

        try {
            CategorieUser cat = CategorieUser.valueOf(categorieTexte);
            System.out.println("Création de compte : " + nom + ", " + email + ", catégorie : " + cat);
        } catch (IllegalArgumentException e) {
            System.out.println("Erreur : catégorie invalide. Valeurs possibles : ADMIN, CLIENT, INVITE");
        }
    }
}
