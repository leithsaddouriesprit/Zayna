package tn.esprit.workshop.services.Talel;

import at.favre.lib.crypto.bcrypt.BCrypt;
import tn.esprit.workshop.model.Talel.Dao.DaoParent;
import tn.esprit.workshop.model.Talel.Dao.DaoUser;
import tn.esprit.workshop.model.Talel.talel2.Parent;
import tn.esprit.workshop.model.Talel.validation.UserValidation;
import tn.esprit.workshop.model.Talel.validation.UserValidation.ValidationResult;

import java.sql.SQLException;

public class ServiceParent {

    private final DaoParent daoParent;
    private final DaoUser daoUser;
    private static final int BCRYPT_COST = 12;

    public ServiceParent() {
        this.daoParent = new DaoParent();
        this.daoUser = new DaoUser();
    }

    /**
     * Ajoute un nouveau parent avec mot de passe
     *
     * @param parent L'objet parent (sans mot de passe)
     * @param passwordClair Le mot de passe en clair
     * @throws Exception Si une erreur survient
     */
    public void ajouterParent(Parent parent, String passwordClair) throws Exception {
        try {
            System.out.println("=== ServiceParent.ajouterParent ===");
            System.out.println("Email reçu: " + parent.getEmail());
            System.out.println("Password clair présent: " + (passwordClair != null && !passwordClair.isEmpty()));

            // ÉTAPE 1: Valider le mot de passe en clair (paramètre)
            if (passwordClair == null || passwordClair.trim().isEmpty()) {
                throw new Exception("Le mot de passe est obligatoire");
            }
            if (passwordClair.length() < 6) {  // CORRIGÉ: 6 au lieu de 3
                throw new Exception("Le mot de passe doit contenir au moins 6 caractères");
            }
            if (passwordClair.length() > 30) { // AJOUTÉ: validation max 30 caractères
                throw new Exception("Le mot de passe ne doit pas dépasser 30 caractères");
            }

            // ÉTAPE 2: Vérifier si l'email existe déjà
            if (daoUser.emailExists(parent.getEmail())) {
                throw new Exception("Un parent avec cet email existe déjà");
            }

            // ÉTAPE 3: Validations des champs obligatoires
            if (parent.getNom() == null || parent.getNom().trim().isEmpty()) {
                throw new Exception("Le nom est obligatoire");
            }
            if (parent.getPrenom() == null || parent.getPrenom().trim().isEmpty()) {
                throw new Exception("Le prénom est obligatoire");
            }
            if (parent.getEmail() == null || parent.getEmail().trim().isEmpty()) {
                throw new Exception("L'email est obligatoire");
            }
            if (parent.getTelephone() == null || parent.getTelephone().trim().isEmpty()) {
                throw new Exception("Le téléphone est obligatoire");
            }
            if (!parent.getTelephone().matches("\\d{8}")) {
                throw new Exception("Le téléphone doit contenir 8 chiffres");
            }
            if (parent.getAdresse() == null || parent.getAdresse().trim().isEmpty()) {
                throw new Exception("L'adresse est obligatoire");
            }

            // ÉTAPE 4: Hacher le mot de passe avec bcrypt
            String hash = BCrypt.withDefaults().hashToString(BCRYPT_COST, passwordClair.toCharArray());
            System.out.println("Hash généré avec succès");

            // ÉTAPE 5: Définir le mot de passe haché dans l'objet parent
            parent.setpassword(hash); // Utilisez setPassword() si c'est le bon nom

            // ÉTAPE 6: Validation finale de l'objet parent complet (avec mot de passe)
            ValidationResult result = UserValidation.validateParent(parent, true);
            if (!result.isValid()) {
                System.out.println("Erreurs de validation: " + result.getErrors());
                throw new Exception(String.join("\n", result.getErrors()));
            }

            // ÉTAPE 7: Ajouter le parent à la base de données
            daoParent.createParent(parent);
            System.out.println("✅ Parent ajouté avec succès ! ID : " + parent.getId());

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL: " + e.getMessage());
            System.err.println("Code erreur: " + e.getErrorCode());
            System.err.println("État SQL: " + e.getSQLState());
            e.printStackTrace();
            throw new Exception("Erreur lors de l'ajout du parent : " + e.getMessage(), e);
        }
    }
}