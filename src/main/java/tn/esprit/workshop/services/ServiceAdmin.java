package tn.esprit.workshop.services;

import at.favre.lib.crypto.bcrypt.BCrypt;
import tn.esprit.workshop.model.Dao.DaoUser;
import tn.esprit.workshop.model.talel.CategorieUser;
import tn.esprit.workshop.model.talel.User;
import tn.esprit.workshop.model.validation.UserValidation;
import tn.esprit.workshop.model.validation.UserValidation.ValidationResult;

import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class ServiceAdmin {

    private final DaoUser daoUser;
    private static final int BCRYPT_COST = 12;

    public ServiceAdmin() {
        this.daoUser = new DaoUser();
    }

    // ==================== MÉTHODES CRUD ====================

    /**
     * Ajoute un nouvel utilisateur avec mot de passe haché
     * @param user L'objet utilisateur (sans mot de passe)
     * @param passwordClair Le mot de passe en clair
     */
    public void ajouterUtilisateur(User user, String passwordClair) throws Exception {
        try {
            System.out.println("=== ServiceAdmin.ajouterUtilisateur ===");
            System.out.println("Email reçu: " + user.getEmail());
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
            if (daoUser.emailExists(user.getEmail())) {
                throw new Exception("Un utilisateur avec cet email existe déjà");
            }

            // ÉTAPE 3: Validations des champs obligatoires
            if (user.getNom() == null || user.getNom().trim().isEmpty()) {
                throw new Exception("Le nom est obligatoire");
            }
            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                throw new Exception("L'email est obligatoire");
            }


            // ÉTAPE 4: Hacher le mot de passe avec bcrypt
            String hash = BCrypt.withDefaults().hashToString(BCRYPT_COST, passwordClair.toCharArray());
            System.out.println("Hash généré avec succès");

            // ÉTAPE 5: Définir le mot de passe haché dans l'objet utilisateur
            user.setpassword(hash);

            // ÉTAPE 6: Validation via UserValidation (en ignorant les erreurs de mot de passe)
            ValidationResult result = UserValidation.validateUser(user, true);
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

            // ÉTAPE 7: Ajouter l'utilisateur à la base de données
            daoUser.createUser(user);
            System.out.println("✅ Utilisateur ajouté avec succès ! ID : " + user.getId());

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL: " + e.getMessage());
            System.err.println("Code erreur: " + e.getErrorCode());
            System.err.println("État SQL: " + e.getSQLState());
            e.printStackTrace();
            throw new Exception("Erreur lors de l'ajout : " + e.getMessage(), e);
        }
    }

    /**
     * Modifie un utilisateur (sans changer le mot de passe)
     */
    public void modifierUtilisateur(User user) throws Exception {
        try {
            System.out.println("=== ServiceAdmin.modifierUtilisateur ===");
            System.out.println("ID: " + user.getId() + ", Email: " + user.getEmail());

            User existingUser = daoUser.getUserById(user.getId());
            if (existingUser == null) {
                throw new Exception("Utilisateur non trouvé");
            }

            // Vérifier si l'email est changé et s'il existe déjà
            if (!existingUser.getEmail().equals(user.getEmail()) &&
                    daoUser.emailExists(user.getEmail())) {
                throw new Exception("Un utilisateur avec cet email existe déjà");
            }

            // Validations des champs
            if (user.getNom() == null || user.getNom().trim().isEmpty()) {
                throw new Exception("Le nom est obligatoire");
            }
            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                throw new Exception("L'email est obligatoire");
            }


            // Conserver l'ancien mot de passe (ne pas modifier)
            user.setpassword(existingUser.getpassword());

            // Validation via UserValidation
            ValidationResult result = UserValidation.validateUser(user, false);
            if (!result.isValid()) {
                throw new Exception(String.join("\n", result.getErrors()));
            }

            daoUser.updateUser(user);
            System.out.println("✅ Utilisateur modifié avec succès !");

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL: " + e.getMessage());
            e.printStackTrace();
            throw new Exception("Erreur lors de la modification : " + e.getMessage(), e);
        }
    }

    /**
     * Change le mot de passe d'un utilisateur
     */
    public void changerpassword(int userId, String ancienpassword, String nouveaupassword) throws Exception {
        try {
            System.out.println("=== ServiceAdmin.changerpassword ===");
            System.out.println("User ID: " + userId);

            User user = daoUser.getUserById(userId);
            if (user == null) {
                throw new Exception("Utilisateur non trouvé");
            }

            // Vérifier l'ancien mot de passe
            if (!BCrypt.verifyer().verify(ancienpassword.toCharArray(), user.getpassword()).verified) {
                throw new Exception("L'ancien mot de passe est incorrect");
            }

            // Valider le nouveau mot de passe
            if (nouveaupassword == null || nouveaupassword.trim().isEmpty()) {
                throw new Exception("Le nouveau mot de passe est obligatoire");
            }
            if (nouveaupassword.length() < 6) {
                throw new Exception("Le nouveau mot de passe doit contenir au moins 6 caractères");
            }
            if (nouveaupassword.length() > 30) {
                throw new Exception("Le nouveau mot de passe ne doit pas dépasser 30 caractères");
            }

            // Hacher le nouveau mot de passe
            String hash = BCrypt.withDefaults().hashToString(BCRYPT_COST, nouveaupassword.toCharArray());

            // Mettre à jour
            daoUser.updatepassword(userId, hash);
            System.out.println("✅ Mot de passe changé avec succès !");

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL: " + e.getMessage());
            e.printStackTrace();
            throw new Exception("Erreur lors du changement de mot de passe : " + e.getMessage(), e);
        }
    }

    public void supprimerUtilisateur(int id) throws Exception {
        try {
            System.out.println("=== ServiceAdmin.supprimerUtilisateur ===");
            System.out.println("ID: " + id);

            User user = daoUser.getUserById(id);
            if (user == null) {
                throw new Exception("Utilisateur non trouvé");
            }

            daoUser.deleteUser(id);
            System.out.println("✅ Utilisateur supprimé avec succès !");

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL: " + e.getMessage());
            e.printStackTrace();
            throw new Exception("Erreur lors de la suppression : " + e.getMessage(), e);
        }
    }

    public List<User> getAllUtilisateurs() throws Exception {
        try {
            return daoUser.getAllUsers();
        } catch (SQLException e) {
            throw new Exception("Erreur lors de la récupération : " + e.getMessage(), e);
        }
    }

    public User getUtilisateurById(int id) throws Exception {
        try {
            User user = daoUser.getUserById(id);
            if (user == null) {
                throw new Exception("Aucun utilisateur trouvé avec l'ID : " + id);
            }
            return user;
        } catch (SQLException e) {
            throw new Exception("Erreur lors de la recherche : " + e.getMessage(), e);
        }
    }

    // ==================== MÉTHODES D'AUTHENTIFICATION ====================

    /**
     * Connecte un utilisateur - VERSION AVEC BCRYPT
     */
    public User login(String email, String password) throws Exception {
        System.out.println("=== ServiceAdmin.login ===");
        System.out.println("Tentative de connexion pour: " + email);

        // Validation
        if (email == null || email.trim().isEmpty()) {
            throw new Exception("L'email est obligatoire");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new Exception("Le mot de passe est obligatoire");
        }

        try {
            // Récupérer l'utilisateur par email
            User user = daoUser.getUserByEmail(email);

            if (user == null) {
                System.out.println("❌ Utilisateur non trouvé: " + email);
                throw new Exception("Email ou mot de passe incorrect");
            }

            System.out.println("✅ Utilisateur trouvé: " + user.getNom());
            System.out.println("Vérification du mot de passe...");

            // VÉRIFICATION AVEC BCRYPT
            BCrypt.Result result = BCrypt.verifyer()
                    .verify(password.toCharArray(), user.getpassword());

            if (!result.verified) {
                System.out.println("❌ Mot de passe incorrect");
                throw new Exception("Email ou mot de passe incorrect");
            }

            System.out.println("✅ Connexion réussie pour " + user.getNom());
            return user;

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL: " + e.getMessage());
            e.printStackTrace();
            throw new Exception("Erreur lors de la connexion : " + e.getMessage(), e);
        }
    }

    // ==================== MÉTHODES DE RECHERCHE ====================

    public List<User> rechercherParNom(String nom) throws Exception {
        try {
            if (nom == null || nom.trim().isEmpty()) {
                return daoUser.getAllUsers();
            }

            List<User> allUsers = daoUser.getAllUsers();
            String recherche = nom.toLowerCase().trim();

            return allUsers.stream()
                    .filter(u -> u.getNom() != null &&
                            u.getNom().toLowerCase().contains(recherche))
                    .collect(Collectors.toList());

        } catch (SQLException e) {
            throw new Exception("Erreur lors de la recherche : " + e.getMessage(), e);
        }
    }

    public List<User> rechercherParCategorie(CategorieUser categorie) throws Exception {
        try {
            if (categorie == null) {
                return daoUser.getAllUsers();
            }

            List<User> allUsers = daoUser.getAllUsers();

            return allUsers.stream()
                    .filter(u -> u.getCategories() == categorie)
                    .collect(Collectors.toList());

        } catch (SQLException e) {
            throw new Exception("Erreur lors de la recherche : " + e.getMessage(), e);
        }
    }

    public List<User> rechercher(String nom, CategorieUser categorie) throws Exception {
        try {
            List<User> allUsers = daoUser.getAllUsers();
            List<User> result = new ArrayList<>(allUsers);

            if (nom != null && !nom.trim().isEmpty()) {
                String recherche = nom.toLowerCase().trim();
                result = result.stream()
                        .filter(u -> u.getNom() != null &&
                                u.getNom().toLowerCase().contains(recherche))
                        .collect(Collectors.toList());
            }

            if (categorie != null) {
                result = result.stream()
                        .filter(u -> u.getCategories() == categorie)
                        .collect(Collectors.toList());
            }

            return result;

        } catch (SQLException e) {
            throw new Exception("Erreur lors de la recherche : " + e.getMessage(), e);
        }
    }

    // ==================== STATISTIQUES ====================

    public long countByCategorie(CategorieUser categorie) throws Exception {
        try {
            if (categorie == null) {
                return 0;
            }

            return daoUser.getAllUsers().stream()
                    .filter(u -> u.getCategories() == categorie)
                    .count();
        } catch (SQLException e) {
            throw new Exception("Erreur lors du comptage : " + e.getMessage(), e);
        }
    }

    public int countTotal() throws Exception {
        try {
            return daoUser.getAllUsers().size();
        } catch (SQLException e) {
            throw new Exception("Erreur lors du comptage : " + e.getMessage(), e);
        }
    }

    public boolean emailExists(String email) throws Exception {
        if (email == null || email.trim().isEmpty()) {
            throw new Exception("L'email ne peut pas être vide");
        }

        try {
            return daoUser.emailExists(email);
        } catch (SQLException e) {
            throw new Exception("Erreur lors de la vérification : " + e.getMessage(), e);
        }
    }

    public boolean canLogin(String email) throws Exception {
        try {
            User user = daoUser.getUserByEmail(email);
            return user != null;
        } catch (SQLException e) {
            throw new Exception("Erreur lors de la vérification : " + e.getMessage(), e);
        }
    }
}