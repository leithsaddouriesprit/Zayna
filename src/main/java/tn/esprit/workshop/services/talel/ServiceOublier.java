package tn.esprit.workshop.services.talel;

import tn.esprit.workshop.utilis.MyBDConnexion;
import at.favre.lib.crypto.bcrypt.BCrypt;
import java.sql.*;

public class ServiceOublier {

    private Connection conn;

    public ServiceOublier() {
        this.conn = MyBDConnexion.getInstance().getConnection();
    }

    /**
     * Réinitialise le mot de passe d'un utilisateur
     * @param nom Le nom de l'utilisateur
     * @param telephone Le numéro de téléphone
     * @param newPasswordClair Le nouveau mot de passe en clair
     * @return true si la réinitialisation a réussi, false sinon
     */
    public boolean resetPassword(String nom, String telephone, String newPasswordClair) {
        try {
            System.out.println("=== ServiceOublier.resetPassword ===");
            System.out.println("Nom: " + nom + ", Téléphone: " + telephone);

            // ÉTAPE 1: Validation des données
            if (nom == null || nom.trim().isEmpty()) {
                System.out.println("❌ Nom invalide");
                return false;
            }
            if (telephone == null || telephone.trim().isEmpty()) {
                System.out.println("❌ Téléphone invalide");
                return false;
            }
            if (newPasswordClair == null || newPasswordClair.trim().isEmpty()) {
                System.out.println("❌ Nouveau mot de passe vide");
                return false;
            }
            if (newPasswordClair.length() < 6) {
                System.out.println("❌ Nouveau mot de passe trop court: " + newPasswordClair.length() + " caractères");
                return false;
            }
            if (newPasswordClair.length() > 30) {
                System.out.println("❌ Nouveau mot de passe trop long: " + newPasswordClair.length() + " caractères");
                return false;
            }

            // Nettoyage
            nom = nom.trim();
            telephone = telephone.trim().replaceAll("\\s+", "");

            // ÉTAPE 2: Vérifier si l'utilisateur existe et récupérer son ID
            String checkSql = "SELECT id FROM users WHERE nom = ? AND telephone = ?";
            int userId = -1;

            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, nom);
                checkStmt.setString(2, telephone);

                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    userId = rs.getInt("id");
                    System.out.println("✅ Utilisateur trouvé avec ID: " + userId);
                } else {
                    System.out.println("❌ Utilisateur non trouvé: " + nom + " - " + telephone);
                    return false;
                }
            }

            // ÉTAPE 3: Hacher le nouveau mot de passe
            String hash = BCrypt.withDefaults().hashToString(12, newPasswordClair.toCharArray());
            System.out.println("✅ Nouveau mot de passe haché avec succès");

            // ÉTAPE 4: Mettre à jour le mot de passe dans la base de données
            String updateSql = "UPDATE users SET mot_de_passe = ? WHERE id = ?";

            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setString(1, hash);
                updateStmt.setInt(2, userId);

                int rowsUpdated = updateStmt.executeUpdate();

                if (rowsUpdated > 0) {
                    System.out.println("✅ Mot de passe réinitialisé avec succès pour l'utilisateur: " + nom);
                    return true;
                } else {
                    System.out.println("❌ Échec de la réinitialisation du mot de passe");
                    return false;
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL: " + e.getMessage());
            System.err.println("Code erreur: " + e.getErrorCode());
            System.err.println("État SQL: " + e.getSQLState());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Vérifie si un utilisateur existe
     * @param nom Le nom de l'utilisateur
     * @param telephone Le numéro de téléphone
     * @return true si l'utilisateur existe, false sinon
     */
    public boolean utilisateurExiste(String nom, String telephone) {
        try {
            System.out.println("=== ServiceOublier.utilisateurExiste ===");

            if (nom == null || nom.trim().isEmpty() || telephone == null || telephone.trim().isEmpty()) {
                System.out.println("❌ Paramètres invalides");
                return false;
            }

            nom = nom.trim();
            telephone = telephone.trim().replaceAll("\\s+", "");

            String sql = "SELECT id FROM users WHERE nom = ? AND telephone = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, nom);
                stmt.setString(2, telephone);

                ResultSet rs = stmt.executeQuery();
                boolean existe = rs.next();

                if (existe) {
                    System.out.println("✅ Utilisateur existe: " + nom);
                } else {
                    System.out.println("❌ Utilisateur n'existe pas: " + nom);
                }

                return existe;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Méthode utilitaire pour vérifier un mot de passe (pour le login)
     * @param passwordClair Le mot de passe en clair
     * @param hashedPassword Le mot de passe hashé stocké en base
     * @return true si le mot de passe correspond
     */
    public boolean verifyPassword(String passwordClair, String hashedPassword) {
        try {
            if (passwordClair == null || hashedPassword == null) {
                System.out.println("❌ Paramètres null pour la vérification du mot de passe");
                return false;
            }

            BCrypt.Result result = BCrypt.verifyer().verify(passwordClair.toCharArray(), hashedPassword);
            boolean verified = result.verified;

            if (verified) {
                System.out.println("✅ Mot de passe vérifié avec succès");
            } else {
                System.out.println("❌ Mot de passe incorrect");
            }

            return verified;

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la vérification du mot de passe: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Change le mot de passe d'un utilisateur après vérification de l'ancien
     * @param userId L'ID de l'utilisateur
     * @param ancienPasswordClair L'ancien mot de passe en clair
     * @param nouveauPasswordClair Le nouveau mot de passe en clair
     * @return true si le changement a réussi, false sinon
     */
    public boolean changerPassword(int userId, String ancienPasswordClair, String nouveauPasswordClair) {
        try {
            System.out.println("=== ServiceOublier.changerPassword ===");
            System.out.println("User ID: " + userId);

            // ÉTAPE 1: Validation des entrées
            if (ancienPasswordClair == null || ancienPasswordClair.trim().isEmpty()) {
                System.out.println("❌ Ancien mot de passe vide");
                return false;
            }
            if (nouveauPasswordClair == null || nouveauPasswordClair.trim().isEmpty()) {
                System.out.println("❌ Nouveau mot de passe vide");
                return false;
            }
            if (nouveauPasswordClair.length() < 6) {
                System.out.println("❌ Nouveau mot de passe trop court");
                return false;
            }
            if (nouveauPasswordClair.length() > 30) {
                System.out.println("❌ Nouveau mot de passe trop long");
                return false;
            }

            // ÉTAPE 2: Récupérer le mot de passe actuel
            String sqlSelect = "SELECT mot_de_passe FROM users WHERE id = ?";
            String ancienHash = null;

            try (PreparedStatement stmt = conn.prepareStatement(sqlSelect)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    ancienHash = rs.getString("mot_de_passe");
                } else {
                    System.out.println("❌ Utilisateur non trouvé");
                    return false;
                }
            }

            // ÉTAPE 3: Vérifier l'ancien mot de passe
            BCrypt.Result result = BCrypt.verifyer().verify(ancienPasswordClair.toCharArray(), ancienHash);
            if (!result.verified) {
                System.out.println("❌ Ancien mot de passe incorrect");
                return false;
            }

            // ÉTAPE 4: Hacher le nouveau mot de passe
            String nouveauHash = BCrypt.withDefaults().hashToString(12, nouveauPasswordClair.toCharArray());

            // ÉTAPE 5: Mettre à jour
            String sqlUpdate = "UPDATE users SET mot_de_passe = ? WHERE id = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sqlUpdate)) {
                stmt.setString(1, nouveauHash);
                stmt.setInt(2, userId);

                int rowsUpdated = stmt.executeUpdate();

                if (rowsUpdated > 0) {
                    System.out.println("✅ Mot de passe changé avec succès");
                    return true;
                } else {
                    System.out.println("❌ Échec du changement de mot de passe");
                    return false;
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}