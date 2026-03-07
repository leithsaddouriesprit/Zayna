package tn.esprit.workshop.model.Dao;

import tn.esprit.workshop.model.talel.CategorieUser;
import tn.esprit.workshop.model.talel.ResponsableEcole;
import tn.esprit.workshop.utilis.MyBDConnexion;

import java.sql.*;

public class DaoResponsableEcole extends DaoUser {

    public void createResponsableEcole(ResponsableEcole responsable) throws SQLException {
        String sql = "INSERT INTO users (nom, email, mot_de_passe, categorie, titre, ecole, telephone, adresse, salaire) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = MyBDConnexion.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Set basic user information
            ps.setString(1, responsable.getNom());
            ps.setString(2, responsable.getEmail());
            ps.setString(3, responsable.getpassword());
            ps.setString(4, CategorieUser.RESPONSABLEECOLE.name()); // Force RESPONSABLEECOLE category

            // Set ResponsableEcole-specific fields
            ps.setString(5, responsable.getTitre());
            ps.setString(6, responsable.getEcole());
            ps.setString(7, responsable.getTelephone());
            ps.setString(8, responsable.getAdresse());
            ps.setDouble(9, responsable.getSalaire());

            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("La création du responsable d'école a échoué, aucune ligne affectée.");
            }

            // Récupérer l'ID généré
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    responsable.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("La création du responsable d'école a échoué, aucun ID généré.");
                }
            }
        }
    }
}
