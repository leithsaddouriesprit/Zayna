package tn.esprit.workshop.model.Talel.Dao;

import tn.esprit.workshop.model.Talel.talel2.CategorieUser;
import tn.esprit.workshop.model.Talel.talel2.Parent;
import tn.esprit.workshop.utilis.MyBDConnexion;
import java.sql.*;

public class DaoParent extends DaoUser {

    public void createParent(Parent parent) throws SQLException {
        System.out.println("==========================================");
        System.out.println("=== DaoParent.createParent - DÉBUT ===");
        System.out.println("==========================================");

        // Afficher les données reçues
        System.out.println("📋 Données reçues:");
        System.out.println("   👤 Nom: '" + parent.getNom() + "'");
        System.out.println("   📧 Email: '" + parent.getEmail() + "'");
        System.out.println("   🔑 Mot de passe: [PROTÉGÉ]");
        System.out.println("   📞 Téléphone: '" + parent.getTelephone() + "'");
        System.out.println("   🏠 Adresse: '" + parent.getAdresse() + "'");
        System.out.println("   💼 Profession: '" + parent.getProfession() + "'");
        System.out.println("   🆔 Catégorie: " + CategorieUser.PARENT.name());

        // Vérification de l'instance MyBDConnexion
        System.out.println("\n🔍 Vérification de la connexion:");
        MyBDConnexion bdInstance = MyBDConnexion.getInstance();
        System.out.println("   📦 Instance MyBDConnexion: " + bdInstance);
        System.out.println("   🆔 HashCode instance: " + System.identityHashCode(bdInstance));

        // Tentative d'ouverture de connexion
        System.out.println("\n🔌 Tentative d'ouverture de connexion...");
        long startTime = System.currentTimeMillis();

        String sqlUsers = "INSERT INTO users (nom, email, mot_de_passe, categorie, telephone, adresse, profession) VALUES (?, ?, ?, ?, ?, ?, ?)";
        System.out.println("   📝 SQL users: " + sqlUsers);

        try (Connection conn = MyBDConnexion.getInstance().getConnection()) {
            long connectionTime = System.currentTimeMillis() - startTime;
            System.out.println("   ✅ Connexion obtenue en " + connectionTime + "ms");

            // 1) Insert users
            int userId;
            try (PreparedStatement ps = conn.prepareStatement(sqlUsers, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, parent.getNom());
                ps.setString(2, parent.getEmail());
                ps.setString(3, parent.getpassword());
                ps.setString(4, CategorieUser.PARENT.name());
                ps.setString(5, parent.getTelephone());
                ps.setString(6, parent.getAdresse());
                ps.setString(7, parent.getProfession() != null ? parent.getProfession() : "");
                int affectedRows = ps.executeUpdate();
                if (affectedRows == 0) throw new SQLException("Création users échouée, aucune ligne affectée.");
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (!gk.next()) throw new SQLException("Aucun ID généré pour users.");
                    userId = gk.getInt(1);
                }
            }
            System.out.println("   ✅ users créé, id = " + userId);

            // 2) Insert parent (business table: id, nom, prenom, email, user_id only)
            String sqlParent = "INSERT INTO parent (nom, prenom, email, user_id) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlParent, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, parent.getNom());
                ps.setString(2, parent.getPrenom() != null ? parent.getPrenom() : "");
                ps.setString(3, parent.getEmail());
                ps.setInt(4, userId);
                int affected = ps.executeUpdate();
                if (affected == 0) throw new SQLException("Création parent échouée, aucune ligne affectée.");
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (!gk.next()) throw new SQLException("Aucun ID généré pour parent.");
                    parent.setId(gk.getInt(1));
                }
            }
            System.out.println("   ✅ parent créé, id = " + parent.getId());
            System.out.println("\n✅ DaoParent.createParent terminé avec succès");

        } catch (SQLException e) {
            System.err.println("\n❌ ERREUR SQL dans DaoParent.createParent:");
            System.err.println("   🆘 Message: " + e.getMessage());
            System.err.println("   🔢 Code erreur: " + e.getErrorCode());
            System.err.println("   📍 État SQL: " + e.getSQLState());
            System.err.println("   📚 Stack trace:");
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            System.err.println("\n❌ ERREUR GÉNÉRALE dans DaoParent.createParent:");
            System.err.println("   🆘 Message: " + e.getMessage());
            System.err.println("   📚 Stack trace:");
            e.printStackTrace();
            throw new SQLException("Erreur inattendue: " + e.getMessage(), e);
        }

        System.out.println("==========================================");
        System.out.println("=== DaoParent.createParent - FIN ===");
        System.out.println("==========================================\n");
    }

    /** Lecture seule : tous les parents (pour écran admin). */
    public java.util.List<Parent> findAll() throws SQLException {
        java.util.List<Parent> list = new java.util.ArrayList<>();
        String sql = "SELECT id, nom, prenom, email, user_id FROM parent ORDER BY id";
        try (Connection conn = MyBDConnexion.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Parent p = new Parent();
                p.setId(rs.getInt("id"));
                p.setNom(rs.getString("nom"));
                p.setPrenom(rs.getString("prenom"));
                p.setEmail(rs.getString("email"));
                list.add(p);
            }
        }
        return list;
    }
}