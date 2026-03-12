package tn.esprit.workshop.model.Talel.Dao;

import tn.esprit.workshop.model.Talel.talel2.*;
import tn.esprit.workshop.utilis.MyBDConnexion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DaoUser {

    // SUPPRIMEZ: private Connection connection;

    // ==================== CONSTRUCTEUR ====================

    public DaoUser() {
        // PLUS RIEN ICI
    }

    // ==================== MÉTHODES DE CRÉATION ====================

    /**
     * Crée un nouvel utilisateur (version de base)
     */
    public void createUser(User u) throws SQLException {
        String sql = "INSERT INTO users (nom, email, mot_de_passe, categorie) VALUES (?, ?, ?, ?)";

        try (Connection conn = MyBDConnexion.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, u.getNom());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getpassword());
            ps.setString(4, u.getCategories().name());

            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("La création de l'utilisateur a échoué, aucune ligne affectée.");
            }

            // Récupérer l'ID généré
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    u.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    // ==================== MÉTHODES D'AUTHENTIFICATION ====================

    /**
     * Récupère un utilisateur par son email
     */
    public User getUserByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ?";

        try (Connection conn = MyBDConnexion.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }

    /**
     * Authentifie un utilisateur
     */
    public User authenticate(String email, String password) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ? AND mot_de_passe = ?";

        try (Connection conn = MyBDConnexion.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }

    // ==================== MÉTHODES DE LECTURE ====================

    /**
     * Récupère tous les utilisateurs
     */
    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY id";

        try (Connection conn = MyBDConnexion.getInstance().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        }
        return users;
    }

    /**
     * Récupère un utilisateur par son ID
     */
    public User getUserById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = MyBDConnexion.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }

    // ==================== MÉTHODES DE MISE À JOUR ====================

    /**
     * Met à jour un utilisateur
     */
    public boolean updateUser(User u) throws SQLException {
        String sql = "UPDATE users SET nom = ?, email = ?, mot_de_passe = ?, categorie = ? WHERE id = ?";

        try (Connection conn = MyBDConnexion.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, u.getNom());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getpassword());
            ps.setString(4, u.getCategories().name());
            ps.setInt(5, u.getId());

            return ps.executeUpdate() > 0;
        }
    }

    // ==================== MÉTHODES DE SUPPRESSION ====================

    /**
     * Supprime un utilisateur
     */
    public boolean deleteUser(int id) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = MyBDConnexion.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // ==================== MÉTHODES DE VÉRIFICATION ====================

    /**
     * Vérifie si un email existe déjà
     */
    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";

        // ✅ DOIT avoir UNE NOUVELLE connexion à chaque appel
        try (Connection conn = MyBDConnexion.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur emailExists: " + e.getMessage());
            throw e;
        }
        return false;
    }

    /**
     * Vérifie si un email existe déjà en excluant un ID spécifique
     */
    public boolean emailExistsExceptId(String email, int id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ? AND id != ?";

        try (Connection conn = MyBDConnexion.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setInt(2, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    // ==================== MÉTHODES DE MAPPING ====================

    /**
     * Mappe un ResultSet vers l'objet User approprié selon sa catégorie
     */
    protected User mapResultSetToUser(ResultSet rs) throws SQLException {
        String categorieStr = rs.getString("categorie");
        if (categorieStr == null) {
            return null;
        }

        CategorieUser categorie = CategorieUser.valueOf(categorieStr);

        switch (categorie) {
            case CHAUFFEUR:
                Chauffeur chauffeur = new Chauffeur();
                chauffeur.setId(rs.getInt("id"));
                chauffeur.setNom(rs.getString("nom"));
                chauffeur.setEmail(rs.getString("email"));
                chauffeur.setpassword(rs.getString("mot_de_passe"));
                chauffeur.setCategories(categorie);
                chauffeur.setPermis(rs.getString("permis"));
                chauffeur.setDateObtentionPermis(rs.getDate("date_obtention_permis") != null ?
                        rs.getDate("date_obtention_permis").toLocalDate() : null);
                chauffeur.setTelephone(rs.getString("telephone"));
                chauffeur.setAdresse(rs.getString("adresse"));
                chauffeur.setVehicule(rs.getString("vehicule"));
                chauffeur.setSalaire(rs.getDouble("salaire"));
                return chauffeur;

            case MAITRESSE:
                Maitresse maitresse = new Maitresse();
                maitresse.setId(rs.getInt("id"));
                maitresse.setNom(rs.getString("nom"));
                maitresse.setEmail(rs.getString("email"));
                maitresse.setpassword(rs.getString("mot_de_passe"));
                maitresse.setCategories(categorie);
                maitresse.setClasseResponsable(rs.getString("classe_responsable"));
                maitresse.setDiplome(rs.getString("diplome"));
                maitresse.setTelephone(rs.getString("telephone"));
                maitresse.setAdresse(rs.getString("adresse"));
                maitresse.setSalaire(rs.getDouble("salaire"));
                return maitresse;

            case PARENT:
                Parent parent = new Parent();
                parent.setId(rs.getInt("id"));
                parent.setNom(rs.getString("nom"));
                parent.setEmail(rs.getString("email"));
                parent.setpassword(rs.getString("mot_de_passe"));
                parent.setCategories(categorie);
                parent.setTelephone(rs.getString("telephone"));
                parent.setAdresse(rs.getString("adresse"));
                parent.setProfession(rs.getString("profession"));
                return parent;

            case RESPONSABLEECOLE:
                ResponsableEcole responsable = new ResponsableEcole();
                responsable.setId(rs.getInt("id"));
                responsable.setNom(rs.getString("nom"));
                responsable.setEmail(rs.getString("email"));
                responsable.setpassword(rs.getString("mot_de_passe"));
                responsable.setCategories(categorie);
                responsable.setTitre(rs.getString("titre"));
                responsable.setEcole(rs.getString("ecole"));
                responsable.setTelephone(rs.getString("telephone"));
                responsable.setAdresse(rs.getString("adresse"));
                responsable.setSalaire(rs.getDouble("salaire"));
                return responsable;

            default:
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setNom(rs.getString("nom"));
                user.setEmail(rs.getString("email"));
                user.setpassword(rs.getString("mot_de_passe"));
                user.setCategories(categorie);
                return user;
        }
    }

    // ==================== MÉTHODES SUPPLÉMENTAIRES UTILES ====================

    /**
     * Compte le nombre d'utilisateurs par catégorie
     */
    public int countByCategory(CategorieUser category) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE categorie = ?";

        try (Connection conn = MyBDConnexion.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category.name());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * Récupère les utilisateurs par catégorie
     */
    public List<User> getUsersByCategory(CategorieUser category) throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE categorie = ? ORDER BY id";

        try (Connection conn = MyBDConnexion.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
            }
        }
        return users;
    }

    /**
     * Met à jour uniquement le mot de passe d'un utilisateur
     */
    public boolean updatepassword(int userId, String motDePasseHache) throws SQLException {
        String sql = "UPDATE users SET mot_de_passe = ? WHERE id = ?";

        try (Connection conn = MyBDConnexion.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, motDePasseHache);
            ps.setInt(2, userId);

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }
    }
}