package tn.esprit.workshop.utilis;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyBDConnexion {
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private static final String URL = "jdbc:mysql://localhost:3306/zaynaa?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

    private Connection connection;
    private static MyBDConnexion instance;

    // ✅ Constructeur privé (Singleton)
    private MyBDConnexion() {
        try {
            // ✅ Charger le driver MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Connexion établie à zaynaa");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Driver MySQL non trouvé: " + e.getMessage());
            connection = null;
        } catch (SQLException e) {
            System.err.println("❌ Erreur de connexion: " + e.getMessage());
            connection = null;
        }
    }

    public static MyBDConnexion getInstance() {
        if (instance == null) {
            instance = new MyBDConnexion();
        }
        return instance;
    }

    /**
     * Retourne la connexion et la réinitialise si elle est fermée
     */
    public synchronized Connection getConnection() throws SQLException {
        try {
            // Si la connexion est null ou fermée, en créer une nouvelle
            if (connection == null || connection.isClosed()) {
                System.out.println("🔄 Reconnexion à la base de données...");
                try {
                    Class.forName("com.mysql.cj.jdbc.Driver");
                    connection = DriverManager.getConnection(URL, USER, PASSWORD);
                    System.out.println("✅ Reconnecté avec succès");
                } catch (ClassNotFoundException e) {
                    throw new SQLException("Driver MySQL non trouvé", e);
                } catch (SQLException e) {
                    throw new SQLException("Erreur de reconnexion: " + e.getMessage(), e);
                }
            }
            return connection;
        } catch (SQLException e) {
            System.err.println("❌ Erreur getConnection: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Ferme la connexion (appelé à l'arrêt)
     */
    public synchronized void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("🔌 Connexion fermée");
            } catch (SQLException e) {
                System.err.println("❌ Erreur fermeture: " + e.getMessage());
            }
            connection = null;
        }
    }
}