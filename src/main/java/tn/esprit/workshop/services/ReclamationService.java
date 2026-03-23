package tn.esprit.workshop.services;

import tn.esprit.workshop.model.Reclamation;
import tn.esprit.workshop.utilis.MyBDConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReclamationService {

    // ✅ Méthode pour obtenir la connexion
    private Connection getConnection() throws SQLException {
        return MyBDConnexion.getInstance().getConnection();
    }

    // CREATE
    public void insertOne(Reclamation r) throws SQLException {
        String sql = "INSERT INTO reclamation (user_id, type, description, statut, " +
                "chauffeur_nom, chauffeur_prenom, bus_matricule, cantine_type, ecole_nom, autre_precision) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection cnx = getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, r.getUserId());
            ps.setString(2, r.getType());
            ps.setString(3, r.getDescription());
            ps.setString(4, r.getStatut());
            ps.setString(5, r.getChauffeurNom());
            ps.setString(6, r.getChauffeurPrenom());
            ps.setString(7, r.getBusMatricule());
            ps.setString(8, r.getCantineType());
            ps.setString(9, r.getEcoleNom());
            ps.setString(10, r.getAutrePrecision());
            ps.executeUpdate();
        }
    }

    // READ ALL
    public List<Reclamation> selectAll() throws SQLException {
        List<Reclamation> list = new ArrayList<>();
        String sql = "SELECT * FROM reclamation ORDER BY date_reclamation DESC";
        try (Connection cnx = getConnection();
             Statement st = cnx.createStatement();
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
        try (Connection cnx = getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
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
        try (Connection cnx = getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
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
        String sql = "UPDATE reclamation SET type = ?, description = ?, statut = ?, " +
                "chauffeur_nom = ?, chauffeur_prenom = ?, bus_matricule = ?, " +
                "cantine_type = ?, ecole_nom = ?, autre_precision = ? WHERE id = ?";
        try (Connection cnx = getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, r.getType());
            ps.setString(2, r.getDescription());
            ps.setString(3, r.getStatut());
            ps.setString(4, r.getChauffeurNom());
            ps.setString(5, r.getChauffeurPrenom());
            ps.setString(6, r.getBusMatricule());
            ps.setString(7, r.getCantineType());
            ps.setString(8, r.getEcoleNom());
            ps.setString(9, r.getAutrePrecision());
            ps.setInt(10, r.getId());

            int rowsAffected = ps.executeUpdate();
            System.out.println("Lignes mises à jour: " + rowsAffected);
        }
    }

    // DELETE
    public void deleteOne(int id) throws SQLException {
        String sql = "DELETE FROM reclamation WHERE id = ?";
        try (Connection cnx = getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rowsAffected = ps.executeUpdate();
            System.out.println("Réclamation supprimée, lignes affectées : " + rowsAffected);
        }
    }

    public void deleteOne(Reclamation r) throws SQLException {
        deleteOne(r.getId());
    }

    // Rechercher par statut
    public List<Reclamation> rechercherParStatut(String statut) throws SQLException {
        List<Reclamation> list = new ArrayList<>();
        String sql = "SELECT * FROM reclamation WHERE statut = ? ORDER BY date_reclamation DESC";
        try (Connection cnx = getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
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
        try (Connection cnx = getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
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
    public int ajouterReclamationEtRetournerId(int userId, String type, String description, String statut,
                                               String chauffeurNom, String chauffeurPrenom, String busMatricule,
                                               String cantineType, String ecoleNom, String autrePrecision) throws SQLException {
        String sql = "INSERT INTO reclamation (user_id, type, description, statut, " +
                "chauffeur_nom, chauffeur_prenom, bus_matricule, cantine_type, ecole_nom, autre_precision) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection cnx = getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setString(2, type);
            ps.setString(3, description);
            ps.setString(4, statut);
            ps.setString(5, chauffeurNom);
            ps.setString(6, chauffeurPrenom);
            ps.setString(7, busMatricule);
            ps.setString(8, cantineType);
            ps.setString(9, ecoleNom);
            ps.setString(10, autrePrecision);
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
        r.setChauffeurNom(rs.getString("chauffeur_nom"));
        r.setChauffeurPrenom(rs.getString("chauffeur_prenom"));
        r.setBusMatricule(rs.getString("bus_matricule"));
        r.setCantineType(rs.getString("cantine_type"));
        r.setEcoleNom(rs.getString("ecole_nom"));
        r.setAutrePrecision(rs.getString("autre_precision"));
        return r;
    }
    // Récupérer les réclamations par école
    public List<Reclamation> getByEcoleId(int ecoleId) throws SQLException {
        List<Reclamation> list = new ArrayList<>();
        String sql = "SELECT * FROM reclamation WHERE ecole_nom = (SELECT nom FROM ecole WHERE id = ?) ORDER BY date_reclamation DESC";
        try (Connection cnx = getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, ecoleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractReclamationFromResultSet(rs));
                }
            }
        }
        return list;
    }
}