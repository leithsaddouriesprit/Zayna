package tn.esprit.workshop.services.Talel;

import at.favre.lib.crypto.bcrypt.BCrypt;
import tn.esprit.workshop.model.Talel.Dao.DaoResponsableEcole;
import tn.esprit.workshop.model.Talel.Dao.DaoUser;
import tn.esprit.workshop.model.Talel.talel2.ResponsableEcole;
import tn.esprit.workshop.model.Talel.validation.UserValidation;
import tn.esprit.workshop.model.Talel.validation.UserValidation.ValidationResult;

import java.sql.SQLException;

public class ServiceResponsableEcole {

    private final DaoResponsableEcole daoResponsable;
    private final DaoUser daoUser;
    private static final int BCRYPT_COST = 12;

    public ServiceResponsableEcole() {
        this.daoResponsable = new DaoResponsableEcole();
        this.daoUser = new DaoUser();
    }

    /**
     * Ajoute un nouveau responsable d'école
     * @param responsable L'objet responsable (sans mot de passe)
     * @param passwordClair Le mot de passe en clair
     * @throws Exception Si une erreur survient
     */
    public void ajouterResponsable(ResponsableEcole responsable, String passwordClair) throws Exception {
        try {
            System.out.println("=== ServiceResponsableEcole.ajouterResponsable ===");
            System.out.println("Email reçu: " + responsable.getEmail());
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
            if (daoUser.emailExists(responsable.getEmail())) {
                throw new Exception("Un responsable avec cet email existe déjà");
            }

            // ÉTAPE 3: Validations des champs spécifiques au responsable
            if (responsable.getNom() == null || responsable.getNom().trim().isEmpty()) {
                throw new Exception("Le nom est obligatoire");
            }
            if (responsable.getEmail() == null || responsable.getEmail().trim().isEmpty()) {
                throw new Exception("L'email est obligatoire");
            }
            if (responsable.getTitre() == null || responsable.getTitre().trim().isEmpty()) {
                throw new Exception("Le titre est obligatoire");
            }
            if (responsable.getEcole() == null || responsable.getEcole().trim().isEmpty()) {
                throw new Exception("Le nom de l'école est obligatoire");
            }
            if (responsable.getSalaire() < 0) {
                throw new Exception("Le salaire ne peut pas être négatif");
            }
            if (responsable.getTelephone() != null && !responsable.getTelephone().isEmpty()
                    && !responsable.getTelephone().matches("\\d{8}")) {
                throw new Exception("Le téléphone doit contenir 8 chiffres");
            }

            // ÉTAPE 4: Hacher le mot de passe avec bcrypt
            String hash = BCrypt.withDefaults().hashToString(BCRYPT_COST, passwordClair.toCharArray());
            System.out.println("Hash généré avec succès");

            // ÉTAPE 5: Définir le mot de passe haché dans l'objet responsable
            responsable.setpassword(hash);

            // ÉTAPE 6: Validation via UserValidation (en ignorant les erreurs de mot de passe)
            ValidationResult result = UserValidation.validateResponsableEcole(responsable, true);
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

            // ÉTAPE 7: Ajouter le responsable à la base de données
            daoResponsable.createResponsableEcole(responsable);
            System.out.println("✅ Responsable d'école ajouté avec succès ! ID : " + responsable.getId());

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL: " + e.getMessage());
            System.err.println("Code erreur: " + e.getErrorCode());
            System.err.println("État SQL: " + e.getSQLState());
            e.printStackTrace();
            throw new Exception("Erreur lors de l'ajout du responsable : " + e.getMessage(), e);
        }
    }
}