package tn.esprit.workshop.services;
import tn.esprit.workshop.model.tous.User;
import tn.esprit.workshop.utilis.MyBDConnexion;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UserService implements CRUD<User> {

    private final Connection connection;

    public UserService() {
        this.connection = MyBDConnexion.getInstance().getConnection();
    }


    @Override
    public void insertOne(User users) throws SQLException {
        String req =
                "INSERT INTO `users` (`nom`, `prenom`, `age`) " +
                        "VALUES ('" + users.getNom() + "', '" + users.getPrenom() + "', " + users.getAge() + ")";

        Statement st = connection.createStatement();
        st.executeUpdate(req);
    }

    @Override
    public void updateOne(User users) throws SQLException {
        String req =
                "UPDATE `users` SET " +
                        "`nom` = '" + users.getNom() + "', " +
                        "`prenom` = '" + users.getPrenom() + "', " +
                        "`age` = " + users.getAge() +
                        " WHERE `id` = " + users.getId();
        Statement st = connection.createStatement();
        int rows = st.executeUpdate(req);
        if (rows == 0) {
            System.out.println("❌ Client non trouvé (id = " + users.getId() + ")");
        } else {
            System.out.println("✅ Client mis à jour avec succès");
        }

    }

    @Override
    public void deleteOne(User users) throws SQLException {
        String req =
                "DELETE FROM `users` WHERE `id` = " + users.getId();

        Statement st = connection.createStatement();
        int rows = st.executeUpdate(req);
        if (rows == 0) {
            System.out.println("Client non trouvé");
    }
        else {
        System.out.println("client supprimé avec succés");
        }
    }

    @Override
    public List<User> selectAll() throws SQLException {
        List<User> userList = new ArrayList<>();
        String req = "SELECT * FROM users";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()){
        User users = new User();

        users.setId(rs.getInt("id"));
        users.setNom(rs.getString("nom"));
        users.setPrenom(rs.getString("prenom"));
        users.setAge(rs.getInt("age"));

        userList.add(users);
        }
    return userList;
    }

    }

