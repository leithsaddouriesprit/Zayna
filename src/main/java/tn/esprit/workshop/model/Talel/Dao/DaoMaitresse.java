package tn.esprit.workshop.model.Talel.Dao;

import tn.esprit.workshop.model.Talel.talel2.CategorieUser;
import tn.esprit.workshop.model.Talel.talel2.Maitresse;
import tn.esprit.workshop.utilis.Talel.MyBDConnexion;
import java.sql.*;

public class DaoMaitresse extends DaoUser {

    public void createMaitresse(Maitresse m) throws SQLException {
        System.out.println("==========================================");
        System.out.println("=== DaoMaitresse.createMaitresse - DÉBUT ===");
        System.out.println("==========================================");

        // Afficher les données reçues
        System.out.println("📋 Données reçues:");
        System.out.println("   👤 Nom: '" + m.getNom() + "'");
        System.out.println("   📧 Email: '" + m.getEmail() + "'");
        System.out.println("   🔑 Mot de passe: [PROTÉGÉ]");
        System.out.println("   🏫 Classe responsable: '" + m.getClasseResponsable() + "'");
        System.out.println("   📜 Diplôme: '" + m.getDiplome() + "'");
        System.out.println("   💰 Salaire: " + m.getSalaire());
        System.out.println("   📞 Téléphone: '" + m.getTelephone() + "'");
        System.out.println("   🏠 Adresse: '" + m.getAdresse() + "'");
        System.out.println("   🆔 Catégorie: " + CategorieUser.MAITRESSE.name());

        // Vérification de l'instance MyBDConnexion
        System.out.println("\n🔍 Vérification de la connexion:");
        MyBDConnexion bdInstance = MyBDConnexion.getInstance();
        System.out.println("   📦 Instance MyBDConnexion: " + bdInstance);
        System.out.println("   🆔 HashCode instance: " + System.identityHashCode(bdInstance));

        // Tentative d'ouverture de connexion
        System.out.println("\n🔌 Tentative d'ouverture de connexion...");
        long startTime = System.currentTimeMillis();

        String sql = "INSERT INTO users (nom, email, mot_de_passe, categorie, classe_responsable, diplome, salaire, telephone, adresse) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
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

                ps.setString(1, m.getNom());
                System.out.println("   1. nom = '" + m.getNom() + "'");

                ps.setString(2, m.getEmail());
                System.out.println("   2. email = '" + m.getEmail() + "'");

                ps.setString(3, m.getpassword());
                System.out.println("   3. mot_de_passe = [HASHÉ]");

                ps.setString(4, CategorieUser.MAITRESSE.name());
                System.out.println("   4. categorie = '" + CategorieUser.MAITRESSE.name() + "'");

                ps.setString(5, m.getClasseResponsable());
                System.out.println("   5. classe_responsable = '" + m.getClasseResponsable() + "'");

                ps.setString(6, m.getDiplome());
                System.out.println("   6. diplome = '" + m.getDiplome() + "'");

                ps.setDouble(7, m.getSalaire());
                System.out.println("   7. salaire = " + m.getSalaire());

                ps.setString(8, m.getTelephone());
                System.out.println("   8. telephone = '" + m.getTelephone() + "'");

                ps.setString(9, m.getAdresse());
                System.out.println("   9. adresse = '" + m.getAdresse() + "'");

                // Exécution
                System.out.println("\n🚀 Exécution de la requête...");
                long execStartTime = System.currentTimeMillis();

                int affectedRows = ps.executeUpdate();

                long execTime = System.currentTimeMillis() - execStartTime;
                System.out.println("   ✅ Requête exécutée en " + execTime + "ms");
                System.out.println("   📊 Lignes affectées: " + affectedRows);

                if (affectedRows == 0) {
                    System.err.println("❌ ERREUR: Aucune ligne affectée!");
                    throw new SQLException("La création de la maîtresse a échoué, aucune ligne affectée.");
                }

                // Récupération de l'ID
                System.out.println("\n🔑 Récupération de l'ID généré...");
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int id = generatedKeys.getInt(1);
                        m.setId(id);
                        System.out.println("   ✅ ID généré avec succès: " + id);

                        // Afficher toutes les informations de la maîtresse après insertion
                        System.out.println("\n📋 Récapitulatif de la maîtresse insérée:");
                        System.out.println("   🆔 ID: " + m.getId());
                        System.out.println("   👤 Nom: " + m.getNom());
                        System.out.println("   📧 Email: " + m.getEmail());
                        System.out.println("   🏫 Classe: " + m.getClasseResponsable());
                        System.out.println("   📜 Diplôme: " + m.getDiplome());
                        System.out.println("   💰 Salaire: " + m.getSalaire());

                    } else {
                        System.err.println("❌ ERREUR: Aucun ID généré!");
                        throw new SQLException("La création de la maîtresse a échoué, aucun ID généré.");
                    }
                }
            }

            System.out.println("\n✅ DaoMaitresse.createMaitresse terminé avec succès");

        } catch (SQLException e) {
            System.err.println("\n❌ ERREUR SQL dans DaoMaitresse.createMaitresse:");
            System.err.println("   🆘 Message: " + e.getMessage());
            System.err.println("   🔢 Code erreur: " + e.getErrorCode());
            System.err.println("   📍 État SQL: " + e.getSQLState());
            System.err.println("   📚 Stack trace:");
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            System.err.println("\n❌ ERREUR GÉNÉRALE dans DaoMaitresse.createMaitresse:");
            System.err.println("   🆘 Message: " + e.getMessage());
            System.err.println("   📚 Stack trace:");
            e.printStackTrace();
            throw new SQLException("Erreur inattendue: " + e.getMessage(), e);
        }

        System.out.println("==========================================");
        System.out.println("=== DaoMaitresse.createMaitresse - FIN ===");
        System.out.println("==========================================\n");
    }
}