package tn.esprit.workshop.services.leith;

import tn.esprit.workshop.model.tous.Chauffeur;
import tn.esprit.workshop.utilis.MyBDConnexion;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    public void envoyerCandidatureAgentEcole(int chauffeurId, byte[] rectoBytes, String rectoName, String rectoMime, byte[] versoBytes, String versoName, String versoMime, String maladie) throws SQLException {

        // date_upload omitted so column must be NULLable or have DEFAULT; date_envoi via CURRENT_TIMESTAMP
        String sql = "INSERT INTO candidature " +
                "(chauffeur_id, statut, date_envoi, maladie, " +
                " permis_recto, permis_recto_nom, permis_recto_mime, " +
                " permis_verso, permis_verso_nom, permis_verso_mime) " +
                "VALUES (?, 'ENVOYEE', CURRENT_TIMESTAMP, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, chauffeurId);
            ps.setString(2, maladie);  // nullable

            ps.setBytes(3, rectoBytes);
            ps.setString(4, rectoName);
            ps.setString(5, rectoMime);

            ps.setBytes(6, versoBytes);
            ps.setString(7, versoName);
            ps.setString(8, versoMime);

            ps.executeUpdate();
        }
    }


}

