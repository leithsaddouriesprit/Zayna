package tn.esprit.workshop.model.Talel.validation;

import tn.esprit.workshop.model.Talel.talel2.*;
import tn.esprit.workshop.model.Talel.talel2.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class UserValidation {

    // ==================== CONSTANTES ====================

    private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final String NAME_PATTERN = "^[a-zA-ZÀ-ÿ\\s-]{2,50}$";
    private static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";

    private static final int MIN_NAME_LENGTH = 2;
    private static final int MAX_NAME_LENGTH = 50;
    private static final int MIN_PASSWORD_LENGTH = 3;
    private static final int MAX_PASSWORD_LENGTH = 100;
    private static final int MAX_EMAIL_LENGTH = 100;

    // ==================== CLASSE DE RÉSULTAT ====================

    /**
     * Classe interne pour encapsuler le résultat de validation
     */
    public static class ValidationResult {
        private final boolean isValid;
        private final List<String> errors;

        public ValidationResult(boolean isValid, List<String> errors) {
            this.isValid = isValid;
            this.errors = errors != null ? errors : new ArrayList<>();
        }

        public boolean isValid() {
            return isValid;
        }

        public List<String> getErrors() {
            return errors;
        }

        public String getErrorsAsString() {
            if (errors.isEmpty()) {
                return "";
            }
            StringBuilder sb = new StringBuilder();
            for (String error : errors) {
                sb.append("• ").append(error).append("\n");
            }
            return sb.toString();
        }

        public String getFirstError() {
            return errors.isEmpty() ? null : errors.get(0);
        }
    }

    // ==================== VALIDATION COMPLÈTE UTILISATEUR ====================

    /**
     * Valide un utilisateur complet (pour création)
     */
    public static ValidationResult validateUser(User user, boolean checkPassword) {
        List<String> errors = new ArrayList<>();

        if (user == null) {
            errors.add("L'utilisateur ne peut pas être null");
            return new ValidationResult(false, errors);
        }

        // Validation du nom
        errors.addAll(validateNom(user.getNom()));

        // Validation de l'email
        errors.addAll(validateEmail(user.getEmail(), true));

        // Validation du mot de passe selon le paramètre
        if (checkPassword) {
            errors.addAll(validateMotDePasse(user.getpassword(), true));
        } else if (user.getpassword() != null && !user.getpassword().isEmpty()) {
            errors.addAll(validateMotDePasse(user.getpassword(), true));
        }

        // Validation de la catégorie
        errors.addAll(validateCategorie(user.getCategories()));

        return new ValidationResult(errors.isEmpty(), errors);
    }

    public static ValidationResult validateUserForCreation(User user) {
        List<String> errors = new ArrayList<>();

        if (user == null) {
            errors.add("L'utilisateur ne peut pas être null");
            return new ValidationResult(false, errors);
        }

        // Validation de tous les champs
        errors.addAll(validateNom(user.getNom()));
        errors.addAll(validateEmail(user.getEmail(), true));
        errors.addAll(validateMotDePasse(user.getpassword(), true));
        errors.addAll(validateCategorie(user.getCategories()));

        return new ValidationResult(errors.isEmpty(), errors);
    }

    /**
     * Valide un utilisateur pour modification
     */
    public static ValidationResult validateUserForUpdate(User user, boolean passwordRequired) {
        List<String> errors = new ArrayList<>();

        if (user == null) {
            errors.add("L'utilisateur ne peut pas être null");
            return new ValidationResult(false, errors);
        }

        if (user.getId() <= 0) {
            errors.add("ID utilisateur invalide");
        }

        // Validation des champs
        errors.addAll(validateNom(user.getNom()));
        errors.addAll(validateEmail(user.getEmail(), false));

        if (passwordRequired) {
            errors.addAll(validateMotDePasse(user.getpassword(), true));
        } else if (user.getpassword() != null && !user.getpassword().isEmpty()) {
            errors.addAll(validateMotDePasse(user.getpassword(), true));
        }

        errors.addAll(validateCategorie(user.getCategories()));

        return new ValidationResult(errors.isEmpty(), errors);
    }

    // ==================== VALIDATION INDIVIDUELLE DES CHAMPS ====================

    /**
     * Valide le nom
     */
    public static List<String> validateNom(String nom) {
        List<String> errors = new ArrayList<>();

        if (nom == null || nom.trim().isEmpty()) {
            errors.add("Le nom est requis");
            return errors;
        }

        String trimmedNom = nom.trim();

        if (trimmedNom.length() < MIN_NAME_LENGTH) {
            errors.add("Le nom doit contenir au moins " + MIN_NAME_LENGTH + " caractères");
        }

        if (trimmedNom.length() > MAX_NAME_LENGTH) {
            errors.add("Le nom ne doit pas dépasser " + MAX_NAME_LENGTH + " caractères");
        }

        if (!trimmedNom.matches("^[a-zA-ZÀ-ÿ\\s-]+$")) {
            errors.add("Le nom ne doit contenir que des lettres, espaces et tirets");
        }

        // Vérifier les espaces multiples
        if (trimmedNom.contains("  ")) {
            errors.add("Le nom ne doit pas contenir d'espaces multiples");
        }

        return errors;
    }

    /**
     * Valide le prénom (champ chauffeur, même règles que le nom).
     */
    public static List<String> validatePrenom(String prenom) {
        List<String> errors = new ArrayList<>();
        if (prenom == null || prenom.trim().isEmpty()) {
            errors.add("Le prénom est requis");
            return errors;
        }
        String t = prenom.trim();
        if (t.length() < MIN_NAME_LENGTH) {
            errors.add("Le prénom doit contenir au moins " + MIN_NAME_LENGTH + " caractères");
        }
        if (t.length() > MAX_NAME_LENGTH) {
            errors.add("Le prénom ne doit pas dépasser " + MAX_NAME_LENGTH + " caractères");
        }
        if (!t.matches("^[a-zA-ZÀ-ÿ\\s-]+$")) {
            errors.add("Le prénom ne doit contenir que des lettres, espaces et tirets");
        }
        if (t.contains("  ")) {
            errors.add("Le prénom ne doit pas contenir d'espaces multiples");
        }
        return errors;
    }

    /**
     * Valide l'email
     * @param email L'email à valider
     * @param checkFormat Vérifier le format (true) ou juste les caractères (false)
     */
    public static List<String> validateEmail(String email, boolean checkFormat) {
        List<String> errors = new ArrayList<>();

        if (email == null || email.trim().isEmpty()) {
            errors.add("L'email est requis");
            return errors;
        }

        String trimmedEmail = email.trim();

        if (trimmedEmail.length() > MAX_EMAIL_LENGTH) {
            errors.add("L'email ne doit pas dépasser " + MAX_EMAIL_LENGTH + " caractères");
        }

        if (checkFormat) {
            if (!Pattern.compile(EMAIL_PATTERN).matcher(trimmedEmail).matches()) {
                errors.add("Format d'email invalide (ex: nom@domaine.com)");
            }
        }

        // Vérifications supplémentaires
        if (trimmedEmail.contains(" ")) {
            errors.add("L'email ne doit pas contenir d'espaces");
        }

        if (trimmedEmail.contains("..")) {
            errors.add("L'email ne doit pas contenir de points consécutifs");
        }

        return errors;
    }

    /**
     * Valide l'email sans vérification de format (pour login)
     */
    public static List<String> validateEmail(String email) {
        return validateEmail(email, true);
    }

    /**
     * Valide le mot de passe
     * @param password Le mot de passe à valider
     * @param strictMode Mode strict (vérification complète) ou simple
     */
    public static List<String> validateMotDePasse(String password, boolean strictMode) {
        List<String> errors = new ArrayList<>();

        if (password == null || password.isEmpty()) {
            errors.add("Le mot de passe est requis");
            return errors;
        }

        if (strictMode) {
            // Validation stricte pour la création
            if (password.length() < MIN_PASSWORD_LENGTH) {
                errors.add("Le mot de passe doit contenir au moins " + MIN_PASSWORD_LENGTH + " caractères");
            }

            if (password.length() > MAX_PASSWORD_LENGTH) {
                errors.add("Le mot de passe ne doit pas dépasser " + MAX_PASSWORD_LENGTH + " caractères");
            }

            if (!password.matches(".*[A-Z].*")) {
                errors.add("Le mot de passe doit contenir au moins une majuscule");
            }

            if (!password.matches(".*[a-z].*")) {
                errors.add("Le mot de passe doit contenir au moins une minuscule");
            }

            if (!password.matches(".*\\d.*")) {
                errors.add("Le mot de passe doit contenir au moins un chiffre");
            }

            if (!password.matches(".*[@#$%^&+=!].*")) {
                errors.add("Le mot de passe doit contenir au moins un caractère spécial (@#$%^&+=!)");
            }

            if (password.contains(" ")) {
                errors.add("Le mot de passe ne doit pas contenir d'espaces");
            }
        } else {
            // Validation simple pour la connexion
            if (password.length() < MIN_PASSWORD_LENGTH) {
                errors.add("Le mot de passe doit contenir au moins " + MIN_PASSWORD_LENGTH + " caractères");
            }
        }

        return errors;
    }

    /**
     * Valide le mot de passe en mode strict par défaut
     */
    public static List<String> validateMotDePasse(String password) {
        return validateMotDePasse(password, true);
    }

    /**
     * Valide la confirmation du mot de passe
     */
    public static List<String> validatePasswordConfirmation(String password, String confirmation) {
        List<String> errors = new ArrayList<>();

        if (!password.equals(confirmation)) {
            errors.add("Les mots de passe ne correspondent pas");
        }

        return errors;
    }

    /**
     * Valide la catégorie
     */
    public static List<String> validateCategorie(CategorieUser categorie) {
        List<String> errors = new ArrayList<>();

        if (categorie == null) {
            errors.add("La catégorie est requise");
        }

        return errors;
    }

    /**
     * Valide l'ID
     */
    public static List<String> validateId(int id) {
        List<String> errors = new ArrayList<>();

        if (id <= 0) {
            errors.add("L'ID doit être un nombre positif");
        }

        return errors;
    }

    // ==================== VALIDATIONS COMMUNES ====================

    /**
     * Valide le téléphone (8 chiffres) - Version simple
     */
    public static List<String> validateTelephone(String telephone) {
        List<String> errors = new ArrayList<>();

        if (telephone != null && !telephone.trim().isEmpty()) {
            String cleaned = telephone.trim().replaceAll("[\\s\\-+]", "");
            if (!cleaned.matches("\\d{8}")) {
                errors.add("Le téléphone doit contenir exactement 8 chiffres");
            }
        }

        return errors;
    }

    /**
     * Valide le téléphone avec option obligatoire
     */
    public static List<String> validateTelephone(String telephone, boolean required) {
        List<String> errors = new ArrayList<>();

        if (required && (telephone == null || telephone.trim().isEmpty())) {
            errors.add("Le téléphone est obligatoire");
        } else if (telephone != null && !telephone.trim().isEmpty()) {
            String cleaned = telephone.trim().replaceAll("[\\s\\-+]", "");
            if (!cleaned.matches("\\d{8}")) {
                errors.add("Le téléphone doit contenir exactement 8 chiffres");
            }
        }

        return errors;
    }

    /**
     * Valide l'adresse - Version simple
     */
    public static List<String> validateAdresse(String adresse) {
        List<String> errors = new ArrayList<>();

        if (adresse != null && !adresse.trim().isEmpty()) {
            if (adresse.length() > 500) {
                errors.add("L'adresse ne doit pas dépasser 500 caractères");
            }
        }

        return errors;
    }

    /**
     * Valide l'adresse avec option obligatoire
     */
    public static List<String> validateAdresse(String adresse, boolean required) {
        List<String> errors = new ArrayList<>();

        if (required && (adresse == null || adresse.trim().isEmpty())) {
            errors.add("L'adresse est obligatoire");
        } else if (adresse != null && !adresse.trim().isEmpty()) {
            if (adresse.length() > 500) {
                errors.add("L'adresse ne doit pas dépasser 500 caractères");
            }
        }

        return errors;
    }

    /**
     * Valide le salaire - Version simple
     */
    public static List<String> validateSalaire(double salaire) {
        List<String> errors = new ArrayList<>();

        if (salaire < 0) {
            errors.add("Le salaire ne peut pas être négatif");
        }
        if (salaire > 1000000) {
            errors.add("Le salaire ne peut pas dépasser 1 000 000 DT");
        }

        return errors;
    }

    /**
     * Valide le salaire avec plage spécifique
     */
    public static List<String> validateSalaire(double salaire, double min, double max) {
        List<String> errors = new ArrayList<>();

        if (salaire < min) {
            errors.add("Le salaire ne peut pas être inférieur à " + min + " DT");
        }
        if (salaire > max) {
            errors.add("Le salaire ne peut pas dépasser " + max + " DT");
        }

        return errors;
    }

    // ==================== VALIDATION DE CONNEXION ====================

    /**
     * Valide les identifiants de connexion
     */
    public static ValidationResult validateLogin(String email, String password) {
        List<String> errors = new ArrayList<>();

        // Validation de l'email (format obligatoire)
        if (email == null || email.trim().isEmpty()) {
            errors.add("L'email est requis");
        } else {
            List<String> emailErrors = validateEmail(email, true);
            if (!emailErrors.isEmpty()) {
                errors.addAll(emailErrors);
            }
        }

        // Validation du mot de passe (simple pour la connexion)
        if (password == null || password.isEmpty()) {
            errors.add("Le mot de passe est requis");
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }

    // ==================== VALIDATION D'INSCRIPTION ====================

    /**
     * Valide le formulaire d'inscription complet
     */
    public static ValidationResult validateRegistration(String nom, String email, String password,
                                                        String confirmPassword, CategorieUser categorie) {
        List<String> errors = new ArrayList<>();

        // Validation du nom
        errors.addAll(validateNom(nom));

        // Validation de l'email
        errors.addAll(validateEmail(email, true));

        // Validation du mot de passe
        errors.addAll(validateMotDePasse(password, true));

        // Validation de la confirmation
        if (!password.equals(confirmPassword)) {
            errors.add("Les mots de passe ne correspondent pas");
        }

        // Validation de la catégorie
        errors.addAll(validateCategorie(categorie));

        return new ValidationResult(errors.isEmpty(), errors);
    }

    // ==================== VALIDATIONS SPÉCIFIQUES POUR CHAUFFEUR ====================

    /**
     * Valide un chauffeur complet
     * @param chauffeur Le chauffeur à valider
     * @param isNew Si true, validation pour création; si false, pour modification
     */
    public static ValidationResult validateChauffeur(Chauffeur chauffeur, boolean isNew) {
        List<String> errors = new ArrayList<>();

        if (chauffeur == null) {
            errors.add("Le chauffeur ne peut pas être null");
            return new ValidationResult(false, errors);
        }

        // Validation des champs de base (hérités de User) + prénom (table chauffeur)
        if (isNew) {
            errors.addAll(validateNom(chauffeur.getNom()));
            errors.addAll(validatePrenom(chauffeur.getPrenom()));
            errors.addAll(validateEmail(chauffeur.getEmail(), true));
            errors.addAll(validateMotDePasse(chauffeur.getpassword(), true));
            errors.addAll(validateCategorie(chauffeur.getCategories()));
        } else {
            // Pour modification, on valide seulement si les champs sont présents
            if (chauffeur.getNom() != null && !chauffeur.getNom().trim().isEmpty()) {
                errors.addAll(validateNom(chauffeur.getNom()));
            }
            if (chauffeur.getEmail() != null && !chauffeur.getEmail().trim().isEmpty()) {
                errors.addAll(validateEmail(chauffeur.getEmail(), true));
            }
            if (chauffeur.getpassword() != null && !chauffeur.getpassword().isEmpty()) {
                errors.addAll(validateMotDePasse(chauffeur.getpassword(), true));
            }
            if (chauffeur.getId() <= 0) {
                errors.add("ID du chauffeur invalide");
            }
        }

        // Validation spécifique au chauffeur
        errors.addAll(validatePermis(chauffeur.getPermis(), isNew));
        errors.addAll(validateDateObtentionPermis(chauffeur.getDateObtentionPermis()));
        errors.addAll(validateTelephone(chauffeur.getTelephone()));
        errors.addAll(validateAdresse(chauffeur.getAdresse()));
        errors.addAll(validateVehicule(chauffeur.getVehicule()));
        errors.addAll(validateSalaire(chauffeur.getSalaire()));
        errors.addAll(validateLignes(chauffeur.getLignes()));

        return new ValidationResult(errors.isEmpty(), errors);
    }

    /**
     * Valide le type de permis
     */
    public static List<String> validatePermis(String permis, boolean isRequired) {
        List<String> errors = new ArrayList<>();

        if (isRequired && (permis == null || permis.trim().isEmpty())) {
            errors.add("Le type de permis est obligatoire");
        } else if (permis != null && !permis.trim().isEmpty()) {
            String permisUpper = permis.trim().toUpperCase();
            if (!permisUpper.matches("[BCDE]")) {
                errors.add("Le permis doit être de type B, C, D ou E");
            }
        }

        return errors;
    }

    /**
     * Valide la date d'obtention du permis.
     * Pas de règle d'âge minimum (pas de date de naissance dans le formulaire).
     */
    public static List<String> validateDateObtentionPermis(LocalDate date) {
        List<String> errors = new ArrayList<>();

        if (date != null) {
            if (date.isAfter(LocalDate.now())) {
                errors.add("La date d'obtention du permis ne peut pas être dans le futur");
            }
            if (date.isBefore(LocalDate.now().minusYears(70))) {
                errors.add("La date d'obtention du permis est trop ancienne");
            }
        }

        return errors;
    }

    /**
     * Valide le véhicule
     */
    public static List<String> validateVehicule(String vehicule) {
        List<String> errors = new ArrayList<>();

        if (vehicule != null && !vehicule.trim().isEmpty()) {
            String trimmed = vehicule.trim();
            if (trimmed.length() < 2) {
                errors.add("Le modèle du véhicule doit contenir au moins 2 caractères");
            }
            if (trimmed.length() > 100) {
                errors.add("Le modèle du véhicule ne doit pas dépasser 100 caractères");
            }
            if (!trimmed.matches("^[a-zA-Z0-9\\s\\-]+$")) {
                errors.add("Le modèle du véhicule ne doit contenir que des lettres, chiffres, espaces et tirets");
            }
        }

        return errors;
    }

    /**
     * Valide les lignes de bus
     */
    public static List<String> validateLignes(List<String> lignes) {
        List<String> errors = new ArrayList<>();

        if (lignes != null && !lignes.isEmpty()) {
            for (String ligne : lignes) {
                if (ligne == null || ligne.trim().isEmpty()) {
                    errors.add("Une ligne de bus ne peut pas être vide");
                } else {
                    String trimmed = ligne.trim();
                    if (trimmed.length() > 50) {
                        errors.add("Le nom d'une ligne ne doit pas dépasser 50 caractères");
                    }
                }
            }
        }

        return errors;
    }

    // ==================== VALIDATIONS SPÉCIFIQUES POUR MAITRESSE ====================

    /**
     * Valide une maîtresse complète
     */
    public static ValidationResult validateMaitresse(Maitresse maitresse, boolean isNew) {
        List<String> errors = new ArrayList<>();

        if (maitresse == null) {
            errors.add("La maîtresse ne peut pas être null");
            return new ValidationResult(false, errors);
        }

        // Validation des champs de base
        if (isNew) {
            errors.addAll(validateNom(maitresse.getNom()));
            errors.addAll(validateEmail(maitresse.getEmail(), true));
            errors.addAll(validateMotDePasse(maitresse.getpassword(), true));
            errors.addAll(validateCategorie(maitresse.getCategories()));
        } else {
            if (maitresse.getNom() != null && !maitresse.getNom().trim().isEmpty()) {
                errors.addAll(validateNom(maitresse.getNom()));
            }
            if (maitresse.getEmail() != null && !maitresse.getEmail().trim().isEmpty()) {
                errors.addAll(validateEmail(maitresse.getEmail(), true));
            }
            if (maitresse.getId() <= 0) {
                errors.add("ID de la maîtresse invalide");
            }
        }

        // Validation spécifique
        if (isNew && (maitresse.getClasseResponsable() == null || maitresse.getClasseResponsable().trim().isEmpty())) {
            errors.add("La classe responsable est obligatoire");
        } else if (maitresse.getClasseResponsable() != null && !maitresse.getClasseResponsable().trim().isEmpty()) {
            String classe = maitresse.getClasseResponsable().trim();
            if (classe.length() > 50) {
                errors.add("La classe responsable ne doit pas dépasser 50 caractères");
            }
        }

        // Validation du diplôme
        if (maitresse.getDiplome() != null && !maitresse.getDiplome().trim().isEmpty()) {
            String diplome = maitresse.getDiplome().trim();
            if (diplome.length() > 100) {
                errors.add("Le diplôme ne doit pas dépasser 100 caractères");
            }
        }

        errors.addAll(validateTelephone(maitresse.getTelephone()));
        errors.addAll(validateAdresse(maitresse.getAdresse()));
        errors.addAll(validateSalaire(maitresse.getSalaire()));

        return new ValidationResult(errors.isEmpty(), errors);
    }

    // ==================== VALIDATIONS SPÉCIFIQUES POUR PARENT ====================

    /**
     * Valide un parent complet
     */
    public static ValidationResult validateParent(Parent parent, boolean isNew) {
        List<String> errors = new ArrayList<>();

        if (parent == null) {
            errors.add("Le parent ne peut pas être null");
            return new ValidationResult(false, errors);
        }

        // Validation des champs de base
        if (isNew) {
            errors.addAll(validateNom(parent.getNom()));
            if (parent.getPrenom() != null && !parent.getPrenom().trim().isEmpty()) {
                errors.addAll(validatePrenom(parent.getPrenom()));
            } else {
                errors.add("Le prénom est obligatoire");
            }
            errors.addAll(validateEmail(parent.getEmail(), true));
            errors.addAll(validateMotDePasse(parent.getpassword(), true));
            errors.addAll(validateCategorie(parent.getCategories()));
        } else {
            if (parent.getNom() != null && !parent.getNom().trim().isEmpty()) {
                errors.addAll(validateNom(parent.getNom()));
            }
            if (parent.getEmail() != null && !parent.getEmail().trim().isEmpty()) {
                errors.addAll(validateEmail(parent.getEmail(), true));
            }
            if (parent.getId() <= 0) {
                errors.add("ID du parent invalide");
            }
        }

        // Validation du téléphone (obligatoire pour parent)
        errors.addAll(validateTelephone(parent.getTelephone(), isNew));

        // Validation de l'adresse
        errors.addAll(validateAdresse(parent.getAdresse(), isNew));

        // Validation de la profession
        if (parent.getProfession() != null && !parent.getProfession().trim().isEmpty()) {
            String profession = parent.getProfession().trim();
            if (profession.length() > 100) {
                errors.add("La profession ne doit pas dépasser 100 caractères");
            }
        }

        // Enfants non requis à l'inscription (créés plus tard via candidature enfant)
        if (parent.getEnfantsNoms() != null && !parent.getEnfantsNoms().isEmpty()) {
            errors.addAll(validateEnfants(parent.getEnfantsNoms()));
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }

    /**
     * Valide la liste des enfants (optionnel à l'inscription)
     */
    public static List<String> validateEnfants(List<String> enfants) {
        List<String> errors = new ArrayList<>();

        if (enfants != null && !enfants.isEmpty()) {
            for (String enfant : enfants) {
                if (enfant == null || enfant.trim().isEmpty()) {
                    errors.add("Le nom d'un enfant ne peut pas être vide");
                } else {
                    String trimmed = enfant.trim();
                    if (trimmed.length() < 2) {
                        errors.add("Le nom d'un enfant doit contenir au moins 2 caractères");
                    }
                    if (trimmed.length() > 100) {
                        errors.add("Le nom d'un enfant ne doit pas dépasser 100 caractères");
                    }
                    if (!trimmed.matches("^[a-zA-ZÀ-ÿ\\s-]+$")) {
                        errors.add("Le nom d'un enfant ne doit contenir que des lettres, espaces et tirets");
                    }
                }
            }
        }

        return errors;
    }

    // ==================== VALIDATIONS SPÉCIFIQUES POUR RESPONSABLE ECOLE ====================

    /**
     * Valide un responsable d'école complet
     */
    public static ValidationResult validateResponsableEcole(ResponsableEcole responsable, boolean isNew) {
        List<String> errors = new ArrayList<>();

        if (responsable == null) {
            errors.add("Le responsable ne peut pas être null");
            return new ValidationResult(false, errors);
        }

        // Validation des champs de base + prénom (agent_ecole)
        if (isNew) {
            errors.addAll(validateNom(responsable.getNom()));
            errors.addAll(validatePrenom(responsable.getPrenom()));
            errors.addAll(validateEmail(responsable.getEmail(), true));
            errors.addAll(validateMotDePasse(responsable.getpassword(), true));
            errors.addAll(validateCategorie(responsable.getCategories()));
        } else {
            if (responsable.getNom() != null && !responsable.getNom().trim().isEmpty()) {
                errors.addAll(validateNom(responsable.getNom()));
            }
            if (responsable.getEmail() != null && !responsable.getEmail().trim().isEmpty()) {
                errors.addAll(validateEmail(responsable.getEmail(), true));
            }
            if (responsable.getId() <= 0) {
                errors.add("ID du responsable invalide");
            }
        }

        if (isNew && (responsable.getEcole() == null || responsable.getEcole().trim().isEmpty())) {
            errors.add("Le nom de votre établissement est obligatoire");
        }
        if (isNew && (responsable.getAdresseEcole() == null || responsable.getAdresseEcole().trim().isEmpty())) {
            errors.add("L'adresse de l'établissement est obligatoire");
        }
        if (responsable.getEcole() != null && !responsable.getEcole().trim().isEmpty()) {
            String ecole = responsable.getEcole().trim();
            if (ecole.length() < 2) {
                errors.add("Le nom de l'école doit contenir au moins 2 caractères");
            }
            if (ecole.length() > 150) {
                errors.add("Le nom de l'école ne doit pas dépasser 150 caractères");
            }
        }

        // Latitude / longitude (obligatoires à la création, plages valides)
        if (isNew) {
            if (responsable.getLatitude() == null) {
                errors.add("La latitude est obligatoire");
            } else if (responsable.getLatitude() < -90 || responsable.getLatitude() > 90) {
                errors.add("La latitude doit être entre -90 et 90");
            }
            if (responsable.getLongitude() == null) {
                errors.add("La longitude est obligatoire");
            } else if (responsable.getLongitude() < -180 || responsable.getLongitude() > 180) {
                errors.add("La longitude doit être entre -180 et 180");
            }
        }

        errors.addAll(validateTelephone(responsable.getTelephone()));
        errors.addAll(validateAdresse(responsable.getAdresse()));

        return new ValidationResult(errors.isEmpty(), errors);
    }

    // ==================== VALIDATIONS SPÉCIFIQUES SUPPLÉMENTAIRES ====================

    /**
     * Valide le téléphone (version plus stricte) - Pour compatibilité
     */
    public static List<String> validateTelephone(String telephone, boolean strict, boolean unused) {
        List<String> errors = new ArrayList<>();

        if (telephone != null && !telephone.trim().isEmpty()) {
            String cleaned = telephone.trim().replaceAll("[\\s\\-+]", "");
            if (!cleaned.matches("\\d{8}")) {
                errors.add("Le téléphone doit contenir exactement 8 chiffres");
            }
            if (strict && cleaned.startsWith("0")) {
                errors.add("Le téléphone ne doit pas commencer par 0");
            }
        } else if (strict) {
            errors.add("Le téléphone est obligatoire");
        }

        return errors;
    }

    // ==================== UTILITAIRES ====================

    /**
     * Vérifie si l'email a un format valide (sans collection d'erreurs)
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return Pattern.compile(EMAIL_PATTERN).matcher(email.trim()).matches();
    }

    /**
     * Vérifie si le mot de passe est fort
     */
    public static boolean isStrongPassword(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        return Pattern.compile(PASSWORD_PATTERN).matcher(password).matches();
    }

    /**
     * Nettoie une chaîne de caractères (supprime les espaces inutiles)
     */
    public static String sanitize(String input) {
        if (input == null) {
            return "";
        }
        return input.trim().replaceAll("\\s+", " ");
    }

    /**
     * Nettoie un email (supprime les espaces et met en minuscules)
     */
    public static String sanitizeEmail(String email) {
        if (email == null) {
            return "";
        }
        return email.trim().toLowerCase();
    }
}