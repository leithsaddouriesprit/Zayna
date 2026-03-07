package tn.esprit.workshop.model.Dao;

import tn.esprit.workshop.model.talel.CategorieUser;
import tn.esprit.workshop.model.talel.Parent;
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

        String sql = "INSERT INTO users (nom, email, mot_de_passe, categorie, telephone, adresse, profession) VALUES (?, ?, ?, ?, ?, ?, ?)";
        System.out.println("   📝 SQL: " + sql);

        try (Connection conn = MyBDConnexion.getInstance().getConnection()) {
            long connectionTime = System.currentTimeMillis() - startTime;
            System.out.println("   ✅ Connexion obtenue en " + connectionTime + "ms");
            System.out.println("   🔗 Connexion: " + conn);
            System.out.println("   🔍 Connexion isClosed? " + conn.isClosed());
            System.out.println("   🆔 HashCode connexion: " + System.identityHashCode(conn));

            // Préparation de la requête
            System.out.println("\n⚙️ Préparation de la requête...");
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                System.out.println("   ✅ PreparedStatement créé");

                // Remplissage des paramètres
                System.out.println("\n📥 Remplissage des paramètres:");

                ps.setString(1, parent.getNom());
                System.out.println("   1. nom = '" + parent.getNom() + "'");

                ps.setString(2, parent.getEmail());
                System.out.println("   2. email = '" + parent.getEmail() + "'");

                ps.setString(3, parent.getpassword());
                System.out.println("   3. mot_de_passe = [HASHÉ]");

                ps.setString(4, CategorieUser.PARENT.name());
                System.out.println("   4. categorie = '" + CategorieUser.PARENT.name() + "'");

                ps.setString(5, parent.getTelephone());
                System.out.println("   5. telephone = '" + parent.getTelephone() + "'");

                ps.setString(6, parent.getAdresse());
                System.out.println("   6. adresse = '" + parent.getAdresse() + "'");

                ps.setString(7, parent.getProfession());
                System.out.println("   7. profession = '" + parent.getProfession() + "'");

                // Exécution
                System.out.println("\n🚀 Exécution de la requête...");
                long execStartTime = System.currentTimeMillis();

                int affectedRows = ps.executeUpdate();

                long execTime = System.currentTimeMillis() - execStartTime;
                System.out.println("   ✅ Requête exécutée en " + execTime + "ms");
                System.out.println("   📊 Lignes affectées: " + affectedRows);

                if (affectedRows == 0) {
                    System.err.println("❌ ERREUR: Aucune ligne affectée!");
                    throw new SQLException("La création du parent a échoué, aucune ligne affectée.");
                }

                // Récupération de l'ID
                System.out.println("\n🔑 Récupération de l'ID généré...");
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int id = generatedKeys.getInt(1);
                        parent.setId(id);
                        System.out.println("   ✅ ID généré avec succès: " + id);
                    } else {
                        System.err.println("❌ ERREUR: Aucun ID généré!");
                        throw new SQLException("La création du parent a échoué, aucun ID généré.");
                    }
                }
            }

            System.out.println("\n✅ DaoParent.createParent terminé avec succès");
            System.out.println("   🆔 ID final: " + parent.getId());

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
}