package tn.esprit.workshop.services.Talel;

import tn.esprit.workshop.utilis.Talel.MyBDConnexion;
import at.favre.lib.crypto.bcrypt.BCrypt;

import java.sql.*;

public class ServiceOublier {

    private Connection conn;

    /**
     * Constructeur - Initialise la connexion à la base de données
     */
    public ServiceOublier() {
        try {
            this.conn = MyBDConnexion.getInstance().getConnection();
            System.out.println("✅ ServiceOublier: Connexion BD établie");
        } catch (Exception e) {
            System.err.println("❌ ServiceOublier: Erreur de connexion BD - " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * VÉRIFICATION 1: Vérifie si l'email existe dans la base de données
     */
    public boolean emailExists(String email) {
        String query = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, email.trim().toLowerCase());
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                boolean existe = rs.getInt(1) > 0;
                System.out.println("🔍 Email '" + email + "' existe? " + existe);
                return existe;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL dans emailExists: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * VÉRIFICATION 2: Récupère le type d'utilisateur (rôle)
     */
    public String getUserType(String email) {
        String query = "SELECT categorie FROM users WHERE email = ?";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, email.trim().toLowerCase());
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getString("categorie");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL dans getUserType: " + e.getMessage());
        }
        return "Utilisateur";
    }

    /**
     * VÉRIFICATION 3: Récupère le nom de l'utilisateur
     */
    public String getUserName(String email) {
        String query = "SELECT nom FROM users WHERE email = ?";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, email.trim().toLowerCase());
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getString("nom");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL dans getUserName: " + e.getMessage());
        }
        return "";
    }

    /**
     * ÉTAPE 1: Sauvegarde un code de réinitialisation
     */
    public boolean saveResetCode(String email, String code) {
        // D'abord, récupérer l'ID de l'utilisateur
        String selectQuery = "SELECT id FROM users WHERE email = ?";
        String insertQuery = "INSERT INTO password_resets (user_id, code, expiration, used) VALUES (?, ?, DATE_ADD(NOW(), INTERVAL 5 MINUTE), false)";

        try {
            int userId = -1;
            try (PreparedStatement selectPst = conn.prepareStatement(selectQuery)) {
                selectPst.setString(1, email.trim().toLowerCase());
                ResultSet rs = selectPst.executeQuery();
                if (rs.next()) {
                    userId = rs.getInt("id");
                    System.out.println("✅ Utilisateur trouvé avec ID: " + userId);
                } else {
                    System.err.println("❌ Utilisateur non trouvé: " + email);
                    return false;
                }
            }

            try (PreparedStatement insertPst = conn.prepareStatement(insertQuery)) {
                insertPst.setInt(1, userId);
                insertPst.setString(2, code);
                int rows = insertPst.executeUpdate();

                if (rows > 0) {
                    System.out.println("✅ Code '" + code + "' sauvegardé pour " + email);
                    return true;
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL dans saveResetCode: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * ÉTAPE 2: Vérifie si le code est valide
     */
    public boolean isResetCodeValid(String email, String code) {
        String query = "SELECT pr.id FROM password_resets pr " +
                "JOIN users u ON pr.user_id = u.id " +
                "WHERE u.email = ? AND pr.code = ? AND pr.expiration > NOW() AND pr.used = false";

        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, email.trim().toLowerCase());
            pst.setString(2, code);
            ResultSet rs = pst.executeQuery();
            boolean valide = rs.next();

            if (valide) {
                System.out.println("✅ Code '" + code + "' valide pour " + email);
            } else {
                System.out.println("❌ Code '" + code + "' invalide ou expiré pour " + email);
            }

            return valide;

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL dans isResetCodeValid: " + e.getMessage());
            return false;
        }
    }

    /**
     * ÉTAPE 3: Met à jour le mot de passe (AVEC BCRYPT)
     */
    public boolean updatePassword(String email, String newPassword) {
        // Validation du mot de passe
        if (newPassword == null || newPassword.length() < 6) {
            System.out.println("❌ Mot de passe trop court: " + (newPassword != null ? newPassword.length() : 0));
            return false;
        }

        // Vérifier la force du mot de passe (optionnel)
        if (!isPasswordStrong(newPassword)) {
            System.out.println("❌ Mot de passe pas assez fort - doit contenir majuscule et chiffre");
            return false;
        }

        try {
            // ÉTAPE 3.1: Hacher le mot de passe avec BCrypt
            String hashedPassword = BCrypt.withDefaults().hashToString(12, newPassword.toCharArray());
            System.out.println("🔐 Mot de passe haché avec BCrypt");

            // ÉTAPE 3.2: Mettre à jour dans la base de données
            String updateQuery = "UPDATE users SET mot_de_passe = ? WHERE email = ?";

            try (PreparedStatement pst = conn.prepareStatement(updateQuery)) {
                pst.setString(1, hashedPassword);
                pst.setString(2, email.trim().toLowerCase());

                int rowsUpdated = pst.executeUpdate();

                if (rowsUpdated > 0) {
                    System.out.println("✅ Mot de passe mis à jour avec succès pour " + email);

                    // ÉTAPE 3.3: Marquer le token comme utilisé
                    markTokenAsUsed(email);

                    return true;
                } else {
                    System.out.println("❌ Aucune ligne mise à jour pour " + email);
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL dans updatePassword: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Marque le token de réinitialisation comme utilisé
     */
    private void markTokenAsUsed(String email) {
        String updateTokenQuery = "UPDATE password_resets pr " +
                "JOIN users u ON pr.user_id = u.id " +
                "SET pr.used = true " +
                "WHERE u.email = ? AND pr.used = false";

        try (PreparedStatement tokenPst = conn.prepareStatement(updateTokenQuery)) {
            tokenPst.setString(1, email.trim().toLowerCase());
            int rows = tokenPst.executeUpdate();
            System.out.println("✅ " + rows + " token(s) marqué(s) comme utilisé(s)");
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors du marquage du token: " + e.getMessage());
        }
    }

    /**
     * Vérifie le mot de passe lors de la connexion (utile pour le login)
     */
    public boolean verifyPassword(String email, String plainPassword) {
        String query = "SELECT mot_de_passe FROM users WHERE email = ?";

        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, email.trim().toLowerCase());
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String hashedPassword = rs.getString("mot_de_passe");
                BCrypt.Result result = BCrypt.verifyer().verify(plainPassword.toCharArray(), hashedPassword);

                boolean verified = result.verified;
                System.out.println("🔐 Vérification mot de passe pour " + email + ": " + (verified ? "✅ correct" : "❌ incorrect"));
                return verified;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL dans verifyPassword: " + e.getMessage());
        }
        return false;
    }

    /**
     * Vérifie la force du mot de passe
     */
    private boolean isPasswordStrong(String password) {
        // Au moins 6 caractères, une majuscule, un chiffre
        boolean hasUpperCase = !password.equals(password.toLowerCase());
        boolean hasDigit = password.matches(".*\\d.*");

        System.out.println("📊 Force du mot de passe: " +
                "Longueur=" + password.length() +
                ", Majuscule=" + hasUpperCase +
                ", Chiffre=" + hasDigit);

        return password.length() >= 6 && hasUpperCase && hasDigit;
    }

    /**
     * Nettoie les tokens expirés (à appeler périodiquement)
     */
    public void cleanupExpiredTokens() {
        String query = "DELETE FROM password_resets WHERE expiration < NOW() OR used = true";
        try (Statement stmt = conn.createStatement()) {
            int rows = stmt.executeUpdate(query);
            System.out.println("🧹 Nettoyage: " + rows + " tokens expirés supprimés");
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors du nettoyage des tokens: " + e.getMessage());
        }
    }
}