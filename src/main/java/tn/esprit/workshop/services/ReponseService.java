package tn.esprit.workshop.services;

import tn.esprit.workshop.model.Reponse;
import tn.esprit.workshop.utilis.MyBDConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReponseService {

    Connection cnx = MyBDConnexion.getInstance().getConnection();

    // CREATE
    public void insertOne(Reponse r) throws SQLException {
        String sql = "INSERT INTO reponse (reclamation_id, message, date_reponse) VALUES (?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, r.getReclamationId());
            ps.setString(2, r.getMessage());
            ps.setTimestamp(3, Timestamp.valueOf(r.getDate()));
            ps.executeUpdate();
        }
    }

    // READ ALL
    public List<Reponse> getAll() throws SQLException {
        List<Reponse> reponses = new ArrayList<>();
        String sql = "SELECT * FROM reponse";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Reponse r = new Reponse(
                        rs.getInt("id"),
                        rs.getInt("reclamation_id"),
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
        String sql = "SELECT * FROM reponse WHERE reclamationId = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, reclamationId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Reponse(
                        rs.getInt("reponseID"),
                        rs.getInt("reclamationId"),
                        rs.getString("message"),
                        rs.getTimestamp("date_reponse").toLocalDateTime()
                );
            }
        }
        return null;
    }

    // READ BY ID
    public Reponse getById(int id) throws SQLException {
        String sql = "SELECT * FROM reponse WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Reponse(
                        rs.getInt("id"),
                        rs.getInt("reclamation_id"),
                        rs.getString("message"),
                        rs.getTimestamp("date_reponse").toLocalDateTime()
                );
            }
        }
        return null;
    }

    // UPDATE
    public void update(Reponse r) throws SQLException {
        String sql = "UPDATE reponse SET reclamation_id = ?, message = ?, date_reponse = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, r.getReclamationId());
            ps.setString(2, r.getMessage());
            ps.setTimestamp(3, Timestamp.valueOf(r.getDate()));
            ps.setInt(4, r.getId());
            ps.executeUpdate();
        }
    }

    // DELETE
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM reponse WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // SEARCH by message (exemple)
    public List<Reponse> searchByMessage(String keyword) throws SQLException {
        List<Reponse> reponses = new ArrayList<>();
        String sql = "SELECT * FROM reponse WHERE message LIKE ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Reponse r = new Reponse(
                        rs.getInt("id"),
                        rs.getInt("reclamation_id"),
                        rs.getString("message"),
                        rs.getTimestamp("date_reponse").toLocalDateTime()
                );
                reponses.add(r);
            }
        }
        return reponses;
    }
}
