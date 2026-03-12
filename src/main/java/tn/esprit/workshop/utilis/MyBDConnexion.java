package tn.esprit.workshop.utilis;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Shared database connection for the Zayna application (Leith and Talel).
 * Uses MySQL on port 3306, database name: zaynaa.
 */
public class MyBDConnexion {
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private static final String URL = "jdbc:mysql://localhost:3306/zaynaa?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

    private Connection connection;
    private static MyBDConnexion instance;

    public MyBDConnexion() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ignored) {
            // Driver may already be loaded
        }
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connexion established (zaynaa @ 3306)");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
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
     * Returns the shared connection. Reconnects if the connection was closed.
     * @throws IllegalStateException if the connection is not available
     */
    public Connection getConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return connection;
        }
        if (connection != null && connection.isClosed()) {
            try {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                return connection;
            } catch (SQLException e) {
                connection = null;
                throw e;
            }
        }
        if (connection == null) {
            throw new IllegalStateException("Database connection not established. Check that MySQL is running on port 3306 and database 'zaynaa' exists.");
        }
        return connection;
    }
}
