package tn.esprit.workshop.tests;
import tn.esprit.workshop.utilis.MyBDConnexion;
import java.sql.SQLException;

public class TestConnexion {
    public static void main(String[] args) {
        System.out.println("🔍 TEST DE CONNEXION - Zayna");
        System.out.println("=============================");

        // Tentative de connexion
        MyBDConnexion connexion = MyBDConnexion.getInstance();

        // Vérification
        if (connexion.getConnection() != null) {
            System.out.println("✅ SUCCÈS: Projet connecté à la base 'zayna'");

            // Tester une requête simple
            try {
                var stmt = connexion.getConnection().createStatement();
                var rs = stmt.executeQuery("SELECT 'Connexion OK' as message");
                if (rs.next()) {
                    System.out.println("📊 Base de données: " + rs.getString("message"));
                }
                rs.close();
                stmt.close();

                System.out.println("\n🎉 Votre projet est prêt à utiliser la base de données !");

            } catch (SQLException e) {
                System.out.println("⚠️ Connecté mais impossible d'exécuter des requêtes");
            }
        } else {
            System.out.println("❌ ÉCHEC: Projet NON connecté");
            System.out.println("\nVérifiez ces points:");
            System.out.println("1. MySQL est-il démarré ?");
            System.out.println("2. La base 'zayna' existe-t-elle ?");
            System.out.println("3. Le port 3307 est-il correct ?");
        }

        System.out.println("=============================");
    }
}