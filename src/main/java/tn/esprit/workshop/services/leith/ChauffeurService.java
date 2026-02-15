package tn.esprit.workshop.services.leith;

import tn.esprit.workshop.model.tous.Chauffeur;
import tn.esprit.workshop.utilis.MyBDConnexion;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;

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

    public int ajouterChauffeurEtRetournerId(String nom, String prenom, int age, int nbAnsExperience) throws SQLException {
        String sql = "INSERT INTO chauffeur (nom, prenom, age, nb_ans_experience) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nom);
            ps.setString(2, prenom);
            ps.setInt(3, age);
            ps.setInt(4, nbAnsExperience);

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("Impossible de récupérer l'ID généré pour le chauffeur.");
    }

    public void envoyerCandidatureAgentEcole(int chauffeurId) throws SQLException {
        // Table de traçage: candidature_chauffeur
        String sql = "INSERT INTO candidature (chauffeur_id, statut) VALUES (?, 'ENVOYEE')";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, chauffeurId);
            ps.executeUpdate();
        }
    }


}

