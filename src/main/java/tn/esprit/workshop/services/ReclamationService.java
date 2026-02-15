package tn.esprit.workshop.services;

import tn.esprit.workshop.model.Reclamation;
import tn.esprit.workshop.utilis.MyBDConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReclamationService {

    private static final Connection cnx = MyBDConnexion.getInstance().getConnection();

    // CREATE
    public void insertOne(Reclamation r) throws SQLException {
        String sql = "INSERT INTO reclamation (user_id, type, description, statut) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, r.getUserId());
            ps.setString(2, r.getType());
            ps.setString(3, r.getDescription());
            ps.setString(4, r.getStatut());
            ps.executeUpdate();
        }
    }

    // READ ALL
    public List<Reclamation> selectAll() throws SQLException {
        List<Reclamation> list = new ArrayList<>();
        String sql = "SELECT * FROM reclamation ORDER BY date_reclamation DESC";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(extractReclamationFromResultSet(rs));
            }
        }
        return list;
    }

    // READ BY ID
    public Reclamation getById(int id) throws SQLException {
        String sql = "SELECT * FROM reclamation WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractReclamationFromResultSet(rs);
                }
            }
        }
        return null;
    }

    // READ BY USER ID
    public List<Reclamation> getByUserId(int userId) throws SQLException {
        List<Reclamation> list = new ArrayList<>();
        String sql = "SELECT * FROM reclamation WHERE user_id = ? ORDER BY date_reclamation DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractReclamationFromResultSet(rs));
                }
            }
        }
        return list;
    }

    // UPDATE
    public void updateOne(Reclamation r) throws SQLException {
        String sql = "UPDATE reclamation SET type = ?, description = ?, statut = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, r.getType());
            ps.setString(2, r.getDescription());
            ps.setString(3, r.getStatut());
            ps.setInt(4, r.getId());
            ps.executeUpdate();
        }
    }

    // DELETE
    public void deleteOne(Reclamation r) throws SQLException {
        // 1. Supprimer les réponses liées
        String sqlReponse = "DELETE FROM reponse WHERE reclamation_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sqlReponse)) {
            ps.setInt(1, r.getId());
            ps.executeUpdate();
        }

        // 2. Supprimer la réclamation
        String sqlReclamation = "DELETE FROM reclamation WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sqlReclamation)) {
            ps.setInt(1, r.getId());
            ps.executeUpdate();
        }
    }




    // Rechercher par statut
    public List<Reclamation> rechercherParStatut(String statut) throws SQLException {
        List<Reclamation> list = new ArrayList<>();
        String sql = "SELECT * FROM reclamation WHERE statut = ? ORDER BY date_reclamation DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, statut);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractReclamationFromResultSet(rs));
                }
            }
        }
        return list;
    }

    // Rechercher par mot-clé
    public List<Reclamation> rechercherParMotCle(String keyword) throws SQLException {
        List<Reclamation> result = new ArrayList<>();
        String sql = "SELECT * FROM reclamation WHERE type LIKE ? OR description LIKE ? ORDER BY date_reclamation DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(extractReclamationFromResultSet(rs));
                }
            }
        }
        return result;
    }

    // Ajouter et retourner ID
    public int ajouterReclamationEtRetournerId(int userId, String type, String description, String statut) throws SQLException {
        String sql = "INSERT INTO reclamation (user_id, type, description, statut) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setString(2, type);
            ps.setString(3, description);
            ps.setString(4, statut);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Impossible de récupérer l'ID de la réclamation !");
    }

    // Méthode utilitaire pour extraire une réclamation du ResultSet
    private Reclamation extractReclamationFromResultSet(ResultSet rs) throws SQLException {
        Reclamation r = new Reclamation();
        r.setId(rs.getInt("id"));
        r.setUserId(rs.getInt("user_id"));
        r.setType(rs.getString("type"));
        r.setDescription(rs.getString("description"));
        r.setDateReclamation(rs.getTimestamp("date_reclamation"));
        r.setStatut(rs.getString("statut"));
        return r;
    }
}