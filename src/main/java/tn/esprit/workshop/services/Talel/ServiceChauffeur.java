package tn.esprit.workshop.services.Talel;

import at.favre.lib.crypto.bcrypt.BCrypt;
import tn.esprit.workshop.model.Talel.Dao.DaoChauffeur;
import tn.esprit.workshop.model.Talel.Dao.DaoUser;
import tn.esprit.workshop.model.Talel.talel2.Chauffeur;
import tn.esprit.workshop.model.Talel.validation.UserValidation;
import tn.esprit.workshop.model.Talel.validation.UserValidation.ValidationResult;
import java.sql.SQLException;
import java.time.LocalDate;

public class ServiceChauffeur {

    private final DaoChauffeur daoChauffeur;
    private final DaoUser daoUser;
    private static final int BCRYPT_COST = 12;

    public ServiceChauffeur() {
        this.daoChauffeur = new DaoChauffeur();
        this.daoUser = new DaoUser();
    }

    /**
     * Ajoute un nouveau chauffeur
     * @param chauffeur L'objet chauffeur (sans mot de passe)
     * @param passwordClair Le mot de passe en clair
     * @throws Exception Si une erreur survient
     */
    public void ajouterChauffeur(Chauffeur chauffeur, String passwordClair) throws Exception {
        try {
            System.out.println("=== ServiceChauffeur.ajouterChauffeur ===");
            System.out.println("Email reçu: " + chauffeur.getEmail());
            System.out.println("Date permis: " + chauffeur.getDateObtentionPermis());

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
            if (daoUser.emailExists(chauffeur.getEmail())) {
                throw new Exception("Un chauffeur avec cet email existe déjà");
            }

            // ÉTAPE 3: Validations des champs spécifiques au chauffeur
            if (chauffeur.getNom() == null || chauffeur.getNom().trim().isEmpty()) {
                throw new Exception("Le nom est obligatoire");
            }
            if (chauffeur.getPrenom() == null || chauffeur.getPrenom().trim().isEmpty()) {
                throw new Exception("Le prénom est obligatoire");
            }
            if (chauffeur.getEmail() == null || chauffeur.getEmail().trim().isEmpty()) {
                throw new Exception("L'email est obligatoire");
            }
            if (chauffeur.getPermis() == null || chauffeur.getPermis().trim().isEmpty()) {
                throw new Exception("Le type de permis est obligatoire");
            }
            // VALIDATION DE LA DATE AJOUTÉE
            if (chauffeur.getDateObtentionPermis() == null) {
                throw new Exception("La date d'obtention du permis est obligatoire");
            }
            if (chauffeur.getDateObtentionPermis().isAfter(LocalDate.now())) {
                throw new Exception("La date d'obtention du permis ne peut pas être dans le futur");
            }
            if (chauffeur.getSalaire() < 0) {
                throw new Exception("Le salaire ne peut pas être négatif");
            }
            if (chauffeur.getTelephone() != null && !chauffeur.getTelephone().isEmpty()
                    && !chauffeur.getTelephone().matches("\\d{8}")) {
                throw new Exception("Le téléphone doit contenir 8 chiffres");
            }

            // ÉTAPE 4: Hacher le mot de passe avec bcrypt
            String hash = BCrypt.withDefaults().hashToString(BCRYPT_COST, passwordClair.toCharArray());
            System.out.println("Hash généré avec succès");

            // ÉTAPE 5: Définir le mot de passe haché dans l'objet chauffeur
            chauffeur.setpassword(hash);

            // ÉTAPE 6: Validation via UserValidation (en ignorant les erreurs de mot de passe)
            ValidationResult result = UserValidation.validateChauffeur(chauffeur, true);
            if (!result.isValid()) {
                System.out.println("Erreurs de validation brutes: " + result.getErrors());

                // Filtrer les erreurs pour ignorer celles du mot de passe
                StringBuilder erreursFiltrees = new StringBuilder();
                for (String erreur : result.getErrors()) {
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

            // ÉTAPE 7: Ajouter le chauffeur à la base de données
            daoChauffeur.createChauffeur(chauffeur);
            System.out.println("✅ Chauffeur ajouté avec succès ! ID : " + chauffeur.getId());

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL: " + e.getMessage());
            System.err.println("Code erreur: " + e.getErrorCode());
            System.err.println("État SQL: " + e.getSQLState());
            e.printStackTrace();
            throw new Exception("Erreur lors de l'ajout du chauffeur : " + e.getMessage(), e);
        }
    }
}