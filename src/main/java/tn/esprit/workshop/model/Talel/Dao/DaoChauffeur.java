package tn.esprit.workshop.model.Talel.Dao;
import tn.esprit.workshop.model.Talel.talel2.CategorieUser;
import tn.esprit.workshop.model.Talel.talel2.Chauffeur;
import tn.esprit.workshop.utilis.Talel.MyBDConnexion;
import java.sql.*;


public class DaoChauffeur extends DaoUser {

    public void createChauffeur(Chauffeur c) throws SQLException {
        String sql = "INSERT INTO users (nom, email, mot_de_passe, categorie, permis, date_obtention_permis, telephone, adresse, vehicule, salaire) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = MyBDConnexion.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, c.getNom());
            ps.setString(2, c.getEmail());
            ps.setString(3, c.getpassword());
            ps.setString(4, CategorieUser.CHAUFFEUR.name()); // Force CHAUFFEUR category
            ps.setString(5, c.getPermis());
            ps.setDate(6, c.getDateObtentionPermis() != null ? Date.valueOf(c.getDateObtentionPermis()) : null);
            ps.setString(7, c.getTelephone());
            ps.setString(8, c.getAdresse());
            ps.setString(9, c.getVehicule());
            ps.setDouble(10, c.getSalaire());

            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("La création du chauffeur a échoué, aucune ligne affectée.");
            }

            // Récupérer l'ID généré
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    c.setId(generatedKeys.getInt(1));

                }
            }
        }
    }
}