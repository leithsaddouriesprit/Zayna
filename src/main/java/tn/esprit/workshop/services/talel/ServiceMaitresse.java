package tn.esprit.workshop.services.talel;

import at.favre.lib.crypto.bcrypt.BCrypt;
import tn.esprit.workshop.model.Dao.DaoMaitresse;
import tn.esprit.workshop.model.Dao.DaoUser;
import tn.esprit.workshop.model.talel.Maitresse;
import tn.esprit.workshop.model.validation.UserValidation;
import tn.esprit.workshop.model.validation.UserValidation.ValidationResult;

import java.sql.SQLException;

public class ServiceMaitresse {

    private final DaoMaitresse daoMaitresse;
    private final DaoUser daoUser;
    private static final int BCRYPT_COST = 12;

    public ServiceMaitresse() {
        this.daoMaitresse = new DaoMaitresse();
        this.daoUser = new DaoUser();
    }

    /**
     * Ajoute une nouvelle maîtresse
     * @param maitresse L'objet maîtresse (sans mot de passe)
     * @param passwordClair Le mot de passe en clair
     * @throws Exception Si une erreur survient
     */
    public void ajouterMaitresse(Maitresse maitresse, String passwordClair) throws Exception {
        try {
            System.out.println("=== ServiceMaitresse.ajouterMaitresse ===");
            System.out.println("Email reçu: " + maitresse.getEmail());
            System.out.println("Password clair présent: " + (passwordClair != null && !passwordClair.isEmpty()));

            // ÉTAPE 1: Valider le mot de passe en clair
            if (passwordClair == null || passwordClair.trim().isEmpty()) {
                throw new Exception("Le mot de passe est obligatoire");
            }
            if (passwordClair.length() < 6) {
                throw new Exception("Le mot de passe doit contenir au moins 6 caractères");
            }
            if (passwordClair.length() > 30) {
                throw new Exception("Le mot de passe ne doit pas dépasser 30 caractères");
            }

            // ÉTAPE 2: Vérifier si l'email existe déjà
            if (daoUser.emailExists(maitresse.getEmail())) {
                throw new Exception("Une maîtresse avec cet email existe déjà");
            }

            // ÉTAPE 3: Validations des champs spécifiques à la maîtresse
            if (maitresse.getNom() == null || maitresse.getNom().trim().isEmpty()) {
                throw new Exception("Le nom est obligatoire");
            }
            if (maitresse.getEmail() == null || maitresse.getEmail().trim().isEmpty()) {
                throw new Exception("L'email est obligatoire");
            }
            if (maitresse.getClasseResponsable() == null || maitresse.getClasseResponsable().trim().isEmpty()) {
                throw new Exception("La classe responsable est obligatoire");
            }
            if (maitresse.getSalaire() < 0) {
                throw new Exception("Le salaire ne peut pas être négatif");
            }
            if (maitresse.getTelephone() != null && !maitresse.getTelephone().isEmpty()
                    && !maitresse.getTelephone().matches("\\d{8}")) {
                throw new Exception("Le téléphone doit contenir 8 chiffres");
            }

            // ÉTAPE 4: Hacher le mot de passe avec bcrypt
            String hash = BCrypt.withDefaults().hashToString(BCRYPT_COST, passwordClair.toCharArray());
            System.out.println("Hash généré avec succès");

            // ÉTAPE 5: Définir le mot de passe haché dans l'objet maîtresse
            maitresse.setpassword(hash);

            // ÉTAPE 6: Validation via UserValidation (en ignorant les erreurs de mot de passe)
            ValidationResult result = UserValidation.validateMaitresse(maitresse, true);
            if (!result.isValid()) {
                System.out.println("Erreurs de validation brutes: " + result.getErrors());

                // Filtrer les erreurs pour ignorer celles du mot de passe
                StringBuilder erreursFiltrees = new StringBuilder();
                for (String erreur : result.getErrors()) {
                    // Ignorer les erreurs concernant le mot de passe
                    if (!erreur.contains("mot de passe") &&
                            !erreur.contains("password") &&
                            !erreur.contains("30 caractères") &&
                            !erreur.contains("6 caractères")) {
                        erreursFiltrees.append(erreur).append("\n");
                    }
                }

                if (erreursFiltrees.length() > 0) {
                    throw new Exception(erreursFiltrees.toString().trim());
                }
                System.out.println("✅ Validation du mot de passe ignorée (déjà haché)");
            }

            // ÉTAPE 7: Ajouter la maîtresse à la base de données
            daoMaitresse.createMaitresse(maitresse);
            System.out.println("✅ Maîtresse ajoutée avec succès ! ID : " + maitresse.getId());

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL: " + e.getMessage());
            System.err.println("Code erreur: " + e.getErrorCode());
            System.err.println("État SQL: " + e.getSQLState());
            e.printStackTrace();
            throw new Exception("Erreur lors de l'ajout de la maîtresse : " + e.getMessage(), e);
        }
    }
}