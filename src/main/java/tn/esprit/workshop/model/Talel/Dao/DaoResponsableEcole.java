package tn.esprit.workshop.model.Talel.Dao;

import tn.esprit.workshop.model.Talel.talel2.CategorieUser;
import tn.esprit.workshop.model.Talel.talel2.ResponsableEcole;
import tn.esprit.workshop.utilis.MyBDConnexion;

import java.sql.*;

public class DaoResponsableEcole extends DaoUser {

    /**
     * Inscription responsable : {@code users} + {@code candidature_agent} uniquement (colonnes {@code ecole}, {@code adresse}, …).
     * Aucune ligne {@code ecole} ni {@code agent_ecole}.
     */
    public void createResponsableEcole(ResponsableEcole responsable) throws SQLException {
        String sqlUser = "INSERT INTO users (nom, email, mot_de_passe, categorie, telephone, adresse) VALUES (?, ?, ?, ?, ?, ?)";

        Connection conn = MyBDConnexion.getInstance().getConnection();
        boolean prevAc = conn.getAutoCommit();
        conn.setAutoCommit(false);
        try {
            String ecole = responsable.getEcole() != null ? responsable.getEcole().trim() : "";
            String adresseCand = responsable.getAdresseEcole() != null ? responsable.getAdresseEcole().trim() : "";
            double lat = responsable.getLatitude() != null ? responsable.getLatitude() : 0;
            double lng = responsable.getLongitude() != null ? responsable.getLongitude() : 0;

            int userId;
            try (PreparedStatement ps = conn.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS)) {
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
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (!generatedKeys.next()) {
                        throw new SQLException("La création du responsable d'école a échoué, aucun ID généré.");
                    }
                    userId = generatedKeys.getInt(1);
                    responsable.setId(userId);
                }
            }

            String sqlCand = "INSERT INTO candidature_agent (user_id, nom, prenom, ecole, adresse, latitude, longitude, statut) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, 'EN_ATTENTE')";
            try (PreparedStatement psCand = conn.prepareStatement(sqlCand)) {
                psCand.setInt(1, userId);
                psCand.setString(2, responsable.getNom() != null ? responsable.getNom().trim() : "");
                psCand.setString(3, responsable.getPrenom() != null ? responsable.getPrenom().trim() : "");
                psCand.setString(4, ecole);
                psCand.setString(5, adresseCand);
                psCand.setDouble(6, lat);
                psCand.setDouble(7, lng);
                psCand.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(prevAc);
        }
    }
}
