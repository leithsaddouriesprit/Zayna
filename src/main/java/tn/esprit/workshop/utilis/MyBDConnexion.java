package tn.esprit.workshop.utilis;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyBDConnexion {
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private static final String URL = "jdbc:mysql://localhost:3306/workshopx";

    private Connection connection;
    private static MyBDConnexion instance;

    public MyBDConnexion() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connexion established");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
public static MyBDConnexion getInstance() {
        if (instance == null) instance = new MyBDConnexion();
        return instance;
}
public Connection getConnection() {
        return connection;
}
}
