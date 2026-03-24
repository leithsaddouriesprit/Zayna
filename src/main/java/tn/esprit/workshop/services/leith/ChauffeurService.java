package tn.esprit.workshop.services.leith;

import tn.esprit.workshop.model.tous.Chauffeur;
import tn.esprit.workshop.utilis.MyBDConnexion;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChauffeurService {

    public ChauffeurService() {
    }

    private Connection getConnection() throws SQLException {
        return MyBDConnexion.getInstance().getConnection();
    }

    public Chauffeur getById(int id) throws SQLException {
        String sql = "SELECT id, nom, prenom, age, nb_ans_experience FROM chauffeur WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Chauffeur c = new Chauffeur();
                    c.setId(rs.getInt("id"));
                    c.setNom(rs.getString("nom"));
                    c.setPrenom(rs.getString("prenom"));
                    c.setAge(rs.getInt("age"));
                    c.setNbAnsExperience(rs.getInt("nb_ans_experience"));
                    return c;
                }
            }
        }
        return null;
    }

    public int ajouterChauffeurEtRetournerId(String nom, String prenom, int age, int nbAnsExperience) throws SQLException {
        String sql = "INSERT INTO chauffeur (nom, prenom, age, nb_ans_experience) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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

    public void envoyerCandidatureAgentEcole(int chauffeurId, int idEcole, byte[] rectoBytes, String rectoName, String rectoMime, byte[] versoBytes, String versoName, String versoMime, String maladie) throws SQLException {

        String sql = "INSERT INTO candidature " +
                "(chauffeur_id, id_ecole, statut, date_envoi, maladie, " +
                " permis_recto, permis_recto_nom, permis_recto_mime, " +
                " permis_verso, permis_verso_nom, permis_verso_mime) " +
                "VALUES (?, ?, 'ENVOYEE', CURRENT_TIMESTAMP, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, chauffeurId);
            ps.setInt(2, idEcole);
            ps.setString(3, maladie);

            ps.setBytes(4, rectoBytes);
            ps.setString(5, rectoName);
            ps.setString(6, rectoMime);

            ps.setBytes(7, versoBytes);
            ps.setString(8, versoName);
            ps.setString(9, versoMime);

            ps.executeUpdate();
        }
    }
    // Dans ChauffeurService.java
    public List<Chauffeur> getAll() throws SQLException {
        List<Chauffeur> list = new ArrayList<>();
        String sql = "SELECT * FROM chauffeur";
        try (Connection cnx = MyBDConnexion.getInstance().getConnection();
             Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Chauffeur c = new Chauffeur();
                c.setId(rs.getInt("id"));
                c.setNom(rs.getString("nom"));
                c.setPrenom(rs.getString("prenom"));
                // ... autres champs
                list.add(c);
            }
        }
        return list;
    }

}

