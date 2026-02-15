package tn.esprit.workshop.model.talel;

import tn.esprit.workshop.utilis.MyBDConnexion;
import tn.esprit.workshop.model.talel.CategorieUser;
import tn.esprit.workshop.model.talel.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DaoUser {

    public static void CrieerUser(User u) {
    }

    public User connecter(String email, String motDePasse) {

        return null;
    }

    // CREAT
        public class UserDAO {
            private Connection getConnection() throws SQLException {
                return DriverManager.getConnection(
                        "jdbc:mysql://localhost:3307/gestion_ecole", "root", "T220499I"); // Met ton mot de passe MySQL
            }

            public void CrieerUser(User u) {
                String sql = "INSERT INTO Users (nom,email,mot_de_passe,role) VALUES (?,?,?,?)";
                try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, u.getNom());
                    ps.setString(2, u.getEmail());
                    ps.setString(3, u.getMotDePasse());
                    ps.setString(4, u.getCategories().name());
                    ps.executeUpdate();
                    System.out.println("User ajouté !");
                } catch (SQLException e) {
                    System.out.println("Erreur : " + e.getMessage());
                }
            }

            // CONNECTED
            public User connecter(String email, String motDePasse) {
                String sql = "SELECT * FROM Users WHERE email=? AND mot_de_passe=?";
                try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, email);
                    ps.setString(2, motDePasse);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        User u = new User();
                        u.setId(rs.getInt("id"));
                        u.setNom(rs.getString("nom"));
                        u.setEmail(rs.getString("email"));
                        u.setMotDePasse(rs.getString("mot_de_passe"));
                        u.setCategories(CategorieUser.valueOf(rs.getString("categorie")));
                        return u;
                    }
                } catch (SQLException e) {
                    System.out.println("Erreur : " + e.getMessage());
                }
                return null;
            }
        }


        // 🔹 READ ALL
        public List<User> getAllUsers () {
            List<User> users = new ArrayList<>();
            String sql = "SELECT * FROM users";
            UserDAO ConnectionDB = new UserDAO();
            try (Connection conn = ConnectionDB.getConnection();
                 Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {

                while (rs.next()) {
                    User u = new User();
                    u.setId(rs.getInt("id"));
                    u.setNom(rs.getString("nom"));
                    u.setEmail(rs.getString("email"));
                    u.setMotDePasse(rs.getString("mot_de_passe"));
                    u.setCategories(CategorieUser.valueOf(rs.getString("categorie")));
                    users.add(u);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return users;
        }

        // 🔹 READ BY ID
        public User getUserById ( int id){
            String sql = "SELECT * FROM users WHERE id=?";
            try (Connection conn = MyBDConnexion.getInstance().getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    User u = new User();
                    u.setId(rs.getInt("id"));
                    u.setNom(rs.getString("nom"));
                    u.setEmail(rs.getString("email"));
                    u.setMotDePasse(rs.getString("mot_de_passe"));
                    u.setCategories(CategorieUser.valueOf(rs.getString("categorie")));
                    return u;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        // 🔹 UPDATE
        public boolean updateUser (User u){
            String sql = "UPDATE users SET nom=?, email=?, mot_de_passe=?, categorie=? WHERE id=?";
            try (Connection conn = MyBDConnexion.getInstance().getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, u.getNom());
                ps.setString(2, u.getEmail());
                ps.setString(3, u.getMotDePasse());
                ps.setString(4, u.getCategories().name());
                ps.setInt(5, u.getId());

                int rows = ps.executeUpdate();
                return rows > 0;

            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        // 🔹 DELETE
        public boolean deleteUser ( int id){
            String sql = "DELETE FROM users WHERE id=?";
            try (Connection conn = MyBDConnexion.getInstance().getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, id);
                int rows = ps.executeUpdate();
                return rows > 0;

            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }


    }
