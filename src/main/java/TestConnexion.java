import java.sql.Connection;
import java.sql.DriverManager;

public class TestConnexion {
    public static void main(String[] args) {
        String url = "jdbc:mysql://127.0.0.1:3310/test?useSSL=false&serverTimezone=UTC";
        String user = "root";
        String password = ""; // vide par défaut XAMPP

        try (Connection cnx = DriverManager.getConnection(url, user, password)) {
            System.out.println("✅ IntelliJ connecté à MySQL !");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
