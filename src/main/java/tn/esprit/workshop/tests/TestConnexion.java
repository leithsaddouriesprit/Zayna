package tn.esprit.workshop.tests;
import tn.esprit.workshop.utilis.MyBDConnexion;

import java.sql.SQLException;

public class TestConnexion {
    public static void main(String[] args) {
        System.out.println("🔍 TEST DE CONNEXION - Zayna");
        System.out.println("=============================");

        try {
            MyBDConnexion connexion = MyBDConnexion.getInstance();
            var conn = connexion.getConnection();

            System.out.println("✅ SUCCÈS: Projet connecté à la base 'zaynaa' (port 3306)");

            try (var stmt = conn.createStatement();
                 var rs = stmt.executeQuery("SELECT 'Connexion OK' as message")) {
                if (rs.next()) {
                    System.out.println("📊 Base de données: " + rs.getString("message"));
                }
                System.out.println("\n🎉 Votre projet est prêt à utiliser la base de données !");
            } catch (SQLException e) {
                System.out.println("⚠️ Connecté mais impossible d'exécuter des requêtes: " + e.getMessage());
            }
        } catch (Exception e) {
            System.out.println("❌ ÉCHEC: Projet NON connecté");
            System.out.println("   " + e.getMessage());
            System.out.println("\nVérifiez: MySQL sur port 3306, base 'zaynaa' existante.");
        }
        System.out.println("=============================");
    }
}