package tn.esprit.workshop.utilis.Talel;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @deprecated Use {@link tn.esprit.workshop.utilis.MyBDConnexion} instead.
 * All Talel and Leith database access now uses the shared connection (zaynaa @ port 3306).
 * This class is kept only for reference; no code should reference it.
 */
@Deprecated
public class MyBDConnexion {
    // Connexion adaptée au schéma partagé "zaynaa"
    private static final String USER = "root";
    private static final String PASSWORD = "T220499I";
    private static final String URL = "jdbc:mysql://localhost:3306/zaynaa?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

    private Connection connection;
    private static MyBDConnexion instance;

    private MyBDConnexion() {
        try {
            // Charger le driver MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Établir la connexion
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Connexion établie avec succès à la base 'zaynaa'");
            System.out.println("   Version MySQL Connector: 9.3.0");
            System.out.println("   Port: 3307");

        } catch (ClassNotFoundException e) {
            System.err.println("❌ Driver MySQL non trouvé !");
            System.err.println("   Vérifiez que la dépendance est bien téléchargée");
            System.err.println("   Exécutez: mvn clean install");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("❌ Échec de la connexion");
            System.err.println("   Erreur: " + e.getMessage());

            // Diagnostic du port 3307
            if (e.getMessage().contains("Communications")) {
                System.err.println("\n🔍 PROBLÈME DÉTECTÉ: Port 3307 inaccessible");
                System.err.println("   Solutions:");
                System.err.println("   1. Vérifiez que MySQL utilise bien le port 3307");
                System.err.println("   2. Essayez avec le port 3306 (standard)");
                System.err.println("   3. Vérifiez que MySQL est démarré");
            }
            e.printStackTrace();
        }
    }

    public static MyBDConnexion getInstance() {
        if (instance == null) {
            instance = new MyBDConnexion();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            // ✅ VÉRIFICATION CRITIQUE : Si la connexion est fermée, on la réouvre
            if (connection == null || connection.isClosed()) {
                System.out.println("🔄 Connexion fermée détectée, reconnexion...");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✅ Reconnexion réussie");
            } else {
                System.out.println("✅ Connexion existante et ouverte");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la vérification/reconnexion: " + e.getMessage());
            e.printStackTrace();

            // Tentative de reconnexion forcée
            try {
                System.out.println("🔄 Tentative de reconnexion forcée...");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✅ Reconnexion forcée réussie");
            } catch (SQLException ex) {
                System.err.println("❌ Échec de la reconnexion forcée: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
        return connection;
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("🔌 Connexion fermée");
            } catch (SQLException e) {
                System.err.println("❌ Erreur lors de la fermeture: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}