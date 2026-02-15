package tn.esprit.workshop.services;

import tn.esprit.workshop.model.Reponse;
import tn.esprit.workshop.utilis.MyBDConnexion;

import java.sql.*;

public class ReponseService {

    Connection cnx = MyBDConnexion.getInstance().getConnection();

    public void insertOne(Reponse r) throws SQLException {

        String sql = "INSERT INTO reponse (reclamation_id, message, date_reponse) VALUES (?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, r.getReclamationId());
        ps.setString(2, r.getMessage());
        ps.setTimestamp(3, Timestamp.valueOf(r.getDate()));
        ps.executeUpdate();
    }
}
