package tn.esprit.workshop.services;

import tn.esprit.workshop.model.Reclamation;
import tn.esprit.workshop.utilis.MyBDConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReclamationService {

    // 🔹 Déclarer la connexion
    private final Connection cnx = MyBDConnexion.getInstance().getConnection();

    // CREATE
    public void insertOne(Reclamation r) throws SQLException {
        String sql = "INSERT INTO reclamation (user_id, type, description, statut) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, r.getuser_id());
            ps.setString(2, r.getType());
            ps.setString(3, r.getDescription());
            ps.setString(4, r.getStatut());
            ps.executeUpdate();
        }
    }

    // READ ALL
    public List<Reclamation> selectAll() throws SQLException {
        List<Reclamation> list = new ArrayList<>();
        String sql = "SELECT * FROM reclamation";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Reclamation r = new Reclamation(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("type"),
                        rs.getString("description"),
                        rs.getString("statut")
                );
                list.add(r);
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

    // Rechercher par statut
    public List<Reclamation> rechercherParStatut(String statut) throws SQLException {
        List<Reclamation> list = new ArrayList<>();
        String sql = "SELECT * FROM reclamation WHERE statut = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, statut);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Reclamation r = new Reclamation(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("type"),
                        rs.getString("description"),
                        rs.getString("statut")
                );
                list.add(r);
            }
        }
        return list;
    }

    // Rechercher par mot-clé
    public List<Reclamation> rechercherParMotCle(String keyword) throws SQLException {
        List<Reclamation> result = new ArrayList<>();
        String sql = "SELECT * FROM reclamation WHERE type LIKE ? OR description LIKE ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Reclamation r = new Reclamation(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("type"),
                        rs.getString("description"),
                        rs.getString("statut")
                );
                result.add(r);
            }
        }
        return result;
    }

    // Méthode qui ajoute une réclamation et retourne son ID
    public int ajouterReclamationEtRetournerId(int user_id, String parent, String type, String description, String statut) throws SQLException {
        String sql = "INSERT INTO reclamation (user_id, type, description, statut) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, user_id);
            ps.setString(2, type);
            ps.setString(3, description);
            ps.setString(4, statut);
            ps.executeUpdate();

            // 🔹 Récupérer l'ID auto-généré
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Impossible de récupérer l'ID de la réclamation !");
    }
}
