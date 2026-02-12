package tn.esprit.workshop.services.leith;

import tn.esprit.workshop.model.tous.Chauffeur;
import tn.esprit.workshop.utilis.MyBDConnexion;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ChauffeurService {

    private final Connection connection;

    public ChauffeurService() {
        this.connection = MyBDConnexion.getInstance().getConnection();
    }

    public Chauffeur getById(int id) throws SQLException {

        String req = "SELECT * FROM chauffeur WHERE id = " + id;
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);

        if (rs.next()) {

            Chauffeur c = new Chauffeur();
            c.setId(rs.getInt("id"));
            c.setNom(rs.getString("nom"));
            c.setPrenom(rs.getString("prenom"));
            c.setAge(rs.getInt("age"));
            c.setNbAnsExperience(rs.getInt("nb_ans_experience"));

            return c;
        }

        return null;
    }
}

