package tn.esprit.workshop.model.Talel.Dao;

import tn.esprit.workshop.model.Talel.talel2.CategorieUser;
import tn.esprit.workshop.model.Talel.talel2.ResponsableEcole;
import tn.esprit.workshop.utilis.MyBDConnexion;

import java.sql.*;

public class DaoResponsableEcole extends DaoUser {

    public void createResponsableEcole(ResponsableEcole responsable) throws SQLException {
        // users = authentication only : nom, email, mot_de_passe, categorie, telephone, adresse
        String sql = "INSERT INTO users (nom, email, mot_de_passe, categorie, telephone, adresse) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = MyBDConnexion.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, responsable.getNom());
            ps.setString(2, responsable.getEmail());
            ps.setString(3, responsable.getpassword());
            ps.setString(4, CategorieUser.RESPONSABLEECOLE.name());
            ps.setString(5, responsable.getTelephone());
            ps.setString(6, responsable.getAdresse());

            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("La création du responsable d'école a échoué, aucune ligne affectée.");
            }

            // Récupérer l'ID généré (users.id)
            int userId;
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (!generatedKeys.next()) {
                    throw new SQLException("La création du responsable d'école a échoué, aucun ID généré.");
                }
                userId = generatedKeys.getInt(1);
                responsable.setId(userId);
            }

            // Créer l'école (elle n'a pas besoin d'exister avant l'inscription)
            String sqlEcole = "INSERT INTO ecole (nom, adresse, latitude, longitude) VALUES (?, ?, ?, ?)";
            int idEcole;
            try (PreparedStatement psEcole = conn.prepareStatement(sqlEcole, Statement.RETURN_GENERATED_KEYS)) {
                psEcole.setString(1, responsable.getEcole() != null ? responsable.getEcole().trim() : "");
                psEcole.setString(2, responsable.getAdresseEcole() != null ? responsable.getAdresseEcole().trim() : null);
                psEcole.setDouble(3, responsable.getLatitude() != null ? responsable.getLatitude() : 0);
                psEcole.setDouble(4, responsable.getLongitude() != null ? responsable.getLongitude() : 0);
                psEcole.executeUpdate();
                try (ResultSet rsEcole = psEcole.getGeneratedKeys()) {
                    if (!rsEcole.next()) {
                        throw new SQLException("Création école : aucun ID généré.");
                    }
                    idEcole = rsEcole.getInt(1);
                }
            }

            // Insérer la ligne métier dans agent_ecole (user_id, id_ecole, nom, prenom)
            String sqlAgent = "INSERT INTO agent_ecole (user_id, id_ecole, nom, prenom) VALUES (?, ?, ?, ?)";
            try (PreparedStatement psAgent = conn.prepareStatement(sqlAgent)) {
                psAgent.setInt(1, userId);
                psAgent.setInt(2, idEcole);
                psAgent.setString(3, responsable.getNom() != null ? responsable.getNom().trim() : "");
                psAgent.setString(4, responsable.getPrenom() != null ? responsable.getPrenom().trim() : "");
                psAgent.executeUpdate();
            }
        }
    }
}
