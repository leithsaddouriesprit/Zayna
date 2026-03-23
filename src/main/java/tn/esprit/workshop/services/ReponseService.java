package tn.esprit.workshop.services;

import tn.esprit.workshop.model.Reponse;
import tn.esprit.workshop.utilis.MyBDConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReponseService {

    // ✅ Méthode pour obtenir la connexion
    private Connection getConnection() throws SQLException {
        return MyBDConnexion.getInstance().getConnection();
    }

    // CREATE
    public void insertOne(Reponse r) throws SQLException {
        String sql = "INSERT INTO reponse (reclamation_id, user_id, message, date_reponse) VALUES (?, ?, ?, ?)";
        try (Connection cnx = getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, r.getReclamationId());
            ps.setInt(2, r.getUserId());
            ps.setString(3, r.getMessage());
            ps.setTimestamp(4, Timestamp.valueOf(r.getDate()));
            ps.executeUpdate();
        }
    }

    // READ ALL
    public List<Reponse> getAll() throws SQLException {
        List<Reponse> reponses = new ArrayList<>();
        String sql = "SELECT * FROM reponse";
        try (Connection cnx = getConnection();
             Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Reponse r = new Reponse(
                        rs.getInt("id"),
                        rs.getInt("reclamation_id"),
                        rs.getInt("user_id"),
                        rs.getString("message"),
                        rs.getTimestamp("date_reponse").toLocalDateTime()
                );
                reponses.add(r);
            }
        }
        return reponses;
    }

    // Rechercher une réponse par id de réclamation
    public Reponse getByReclamationId(int reclamationId) throws SQLException {
        String sql = "SELECT * FROM reponse WHERE reclamation_id = ?";
        try (Connection cnx = getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, reclamationId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Reponse(
                        rs.getInt("id"),
                        rs.getInt("reclamation_id"),
                        rs.getInt("user_id"),
                        rs.getString("message"),
                        rs.getTimestamp("date_reponse").toLocalDateTime()
                );
            }
        }
        return null;
    }

    // Version améliorée
    public Reponse getReponseByReclamationId(int reclamationId) throws SQLException {
        String sql = "SELECT * FROM reponse WHERE reclamation_id = ?";
        try (Connection cnx = getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, reclamationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Reponse r = new Reponse();
                    r.setId(rs.getInt("id"));
                    r.setReclamationId(rs.getInt("reclamation_id"));
                    r.setUserId(rs.getInt("user_id"));
                    r.setMessage(rs.getString("message"));
                    r.setDate(rs.getTimestamp("date_reponse").toLocalDateTime());
                    return r;
                }
            }
        }
        return null;
    }

    // Version simplifiée pour retourner juste le message
    public String getReponseMessageByReclamationId(int reclamationId) throws SQLException {
        Reponse r = getReponseByReclamationId(reclamationId);
        return r != null ? r.getMessage() : null;
    }

    // Supprimer par id de réclamation
    public void deleteByReclamationId(int reclamationId) throws SQLException {
        String sql = "DELETE FROM reponse WHERE reclamation_id = ?";
        try (Connection cnx = getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, reclamationId);
            int rowsAffected = ps.executeUpdate();
            System.out.println("Réponses supprimées : " + rowsAffected);
        }
    }

    // READ BY ID
    public Reponse getById(int id) throws SQLException {
        String sql = "SELECT * FROM reponse WHERE id = ?";
        try (Connection cnx = getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Reponse(
                        rs.getInt("id"),
                        rs.getInt("reclamation_id"),
                        rs.getInt("user_id"),
                        rs.getString("message"),
                        rs.getTimestamp("date_reponse").toLocalDateTime()
                );
            }
        }
        return null;
    }

    // UPDATE
    public void update(Reponse r) throws SQLException {
        String sql = "UPDATE reponse SET reclamation_id = ?, user_id = ?, message = ?, date_reponse = ? WHERE id = ?";
        try (Connection cnx = getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, r.getReclamationId());
            ps.setInt(2, r.getUserId());
            ps.setString(3, r.getMessage());
            ps.setTimestamp(4, Timestamp.valueOf(r.getDate()));
            ps.setInt(5, r.getId());
            ps.executeUpdate();
        }
    }

    // DELETE
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM reponse WHERE id = ?";
        try (Connection cnx = getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // SEARCH by message
    public List<Reponse> searchByMessage(String keyword) throws SQLException {
        List<Reponse> reponses = new ArrayList<>();
        String sql = "SELECT * FROM reponse WHERE message LIKE ?";
        try (Connection cnx = getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Reponse r = new Reponse(
                        rs.getInt("id"),
                        rs.getInt("reclamation_id"),
                        rs.getInt("user_id"),
                        rs.getString("message"),
                        rs.getTimestamp("date_reponse").toLocalDateTime()
                );
                reponses.add(r);
            }
        }
        return reponses;
    }
}