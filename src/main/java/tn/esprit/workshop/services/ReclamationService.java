package tn.esprit.workshop.services;

import tn.esprit.workshop.model.Reclamation;
import tn.esprit.workshop.model.Talel.talel2.CategorieUser;
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
        String sql = "INSERT INTO reclamation (user_id, type, description, statut, priorite " +
                "chauffeur_nom, chauffeur_prenom, bus_matricule, cantine_type, ecole_nom, autre_precision) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?)";
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
        String sql = "UPDATE reclamation SET type = ?, description = ?, statut = ?, priorite = ?," +
                "chauffeur_nom = ?, chauffeur_prenom = ?, bus_matricule = ?, " +
                "cantine_type = ?, ecole_nom = ?, autre_precision = ? WHERE id = ?";
        try (Connection cnx = getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, r.getType());
            ps.setString(2, r.getDescription());
            ps.setString(3, r.getStatut());
            ps.setString(4, r.getPriorite());
            ps.setString(5, r.getChauffeurNom());
            ps.setString(6, r.getChauffeurPrenom());
            ps.setString(7, r.getBusMatricule());
            ps.setString(8, r.getCantineType());
            ps.setString(9, r.getEcoleNom());
            ps.setString(10, r.getAutrePrecision());
            ps.setInt(11, r.getId());

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

    // Version avec 10 paramètres (pour les appels existants)
    public int ajouterReclamationEtRetournerId(int userId, String type, String description, String statut,String priorite,
                                               String chauffeurNom, String chauffeurPrenom, String busMatricule,
                                               String cantineType, String ecoleNom, String autrePrecision) throws SQLException {
        return ajouterReclamationEtRetournerId(userId, type, description, statut, priorite,
                chauffeurNom, chauffeurPrenom, busMatricule, cantineType, ecoleNom, autrePrecision,
                0, 0, 0, 0, 0);
    }

    // Version avec 15 paramètres (complète)
    public int ajouterReclamationEtRetournerId(int userId, String type, String description, String statut, String priorite,
                                               String chauffeurNom, String chauffeurPrenom, String busMatricule,
                                               String cantineType, String ecoleNom, String autrePrecision,
                                               int idChauffeur, int idBus, int idEcole, int idMaitresse, int idParent) throws SQLException {

        // Convertir les IDs 0 en NULL pour la base de données
        Integer idChauffeurVal = idChauffeur == 0 ? null : idChauffeur;
        Integer idBusVal = idBus == 0 ? null : idBus;
        Integer idEcoleVal = idEcole == 0 ? null : idEcole;
        Integer idMaitresseVal = idMaitresse == 0 ? null : idMaitresse;
        Integer idParentVal = idParent == 0 ? null : idParent;

        String sql = "INSERT INTO reclamation (user_id, type, description, statut, priorite," +
                "chauffeur_nom, chauffeur_prenom, bus_matricule, cantine_type, ecole_nom, autre_precision, " +
                "id_chauffeur, id_bus, id_ecole, id_maitresse, id_parent) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?)";

        try (Connection cnx = getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, userId);
            ps.setString(2, type);
            ps.setString(3, description);
            ps.setString(4, statut);
            ps.setString(5, priorite);
            ps.setString(6, chauffeurNom);
            ps.setString(7, chauffeurPrenom);
            ps.setString(8, busMatricule);
            ps.setString(9, cantineType);
            ps.setString(10, ecoleNom);
            ps.setString(11, autrePrecision);

            // Gérer les nulls pour les clés étrangères
            if (idChauffeurVal == null) {
                ps.setNull(12, Types.INTEGER);
            } else {
                ps.setInt(12, idChauffeurVal);
            }

            if (idBusVal == null) {
                ps.setNull(13, Types.INTEGER);
            } else {
                ps.setInt(13, idBusVal);
            }

            if (idEcoleVal == null) {
                ps.setNull(14, Types.INTEGER);
            } else {
                ps.setInt(14, idEcoleVal);
            }

            if (idMaitresseVal == null) {
                ps.setNull(15, Types.INTEGER);
            } else {
                ps.setInt(15, idMaitresseVal);
            }

            if (idParentVal == null) {
                ps.setNull(16, Types.INTEGER);
            } else {
                ps.setInt(16, idParentVal);
            }

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
        r.setPriorite(rs.getString("priorite"));

        // Champs texte
        r.setChauffeurNom(rs.getString("chauffeur_nom"));
        r.setChauffeurPrenom(rs.getString("chauffeur_prenom"));
        r.setBusMatricule(rs.getString("bus_matricule"));
        r.setCantineType(rs.getString("cantine_type"));
        r.setEcoleNom(rs.getString("ecole_nom"));
        r.setAutrePrecision(rs.getString("autre_precision"));

        // Clés étrangères (avec try-catch pour éviter les erreurs si colonnes n'existent pas)
        try {
            r.setIdChauffeur(rs.getInt("id_chauffeur"));
        } catch (SQLException e) { r.setIdChauffeur(0); }

        try {
            r.setIdBus(rs.getInt("id_bus"));
        } catch (SQLException e) { r.setIdBus(0); }

        try {
            r.setIdEcole(rs.getInt("id_ecole"));
        } catch (SQLException e) { r.setIdEcole(0); }

        try {
            r.setIdMaitresse(rs.getInt("id_maitresse"));
        } catch (SQLException e) { r.setIdMaitresse(0); }

        try {
            r.setIdParent(rs.getInt("id_parent"));
        } catch (SQLException e) { r.setIdParent(0); }

        return r;
    }

    // Récupérer les réclamations selon le rôle
    public List<Reclamation> getReclamationsByRole(CategorieUser role, int roleId, int ecoleId) throws SQLException {
        List<Reclamation> list = new ArrayList<>();
        String sql;

        switch (role) {
            case PARENT:
                sql = "SELECT r.* FROM reclamation r " +
                        "JOIN parent p ON r.user_id = p.user_id " +
                        "WHERE p.id = ? ORDER BY r.date_reclamation DESC";
                break;
            case CHAUFFEUR:
                sql = "SELECT r.* FROM reclamation r " +
                        "JOIN chauffeur c ON r.user_id = c.user_id " +
                        "WHERE c.id = ? ORDER BY r.date_reclamation DESC";
                break;
            case MAITRESSE:
                sql = "SELECT r.* FROM reclamation r " +
                        "JOIN maitresse m ON r.user_id = m.user_id " +
                        "WHERE m.id = ? ORDER BY r.date_reclamation DESC";
                break;
            case RESPONSABLEECOLE:
                sql = "SELECT r.* FROM reclamation r " +
                        "WHERE r.id_ecole = ? ORDER BY r.date_reclamation DESC";
                break;
            case ADMIN:
            default:
                sql = "SELECT * FROM reclamation ORDER BY date_reclamation DESC";
                break;
        }

        try (Connection cnx = getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {

            if (role == CategorieUser.PARENT || role == CategorieUser.CHAUFFEUR || role == CategorieUser.MAITRESSE) {
                ps.setInt(1, roleId);
            } else if (role == CategorieUser.RESPONSABLEECOLE) {
                ps.setInt(1, ecoleId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractReclamationFromResultSet(rs));
                }
            }
        }
        return list;
    }

    // Récupérer les réclamations d'une école spécifique
    public List<Reclamation> getByEcoleId(int ecoleId) throws SQLException {
        List<Reclamation> list = new ArrayList<>();
        String sql = "SELECT * FROM reclamation WHERE id_ecole = ? ORDER BY date_reclamation DESC";
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

    // Récupérer toutes les réclamations liées à une école (via chauffeur, bus, maîtresse, ou directement)
    public List<Reclamation> getReclamationsByEcoleId(int ecoleId) throws SQLException {
        List<Reclamation> list = new ArrayList<>();

        String sql = "SELECT DISTINCT r.* FROM reclamation r " +
                "LEFT JOIN chauffeur c ON r.id_chauffeur = c.id " +
                "LEFT JOIN bus b ON r.id_bus = b.id " +
                "LEFT JOIN maitresse m ON r.id_maitresse = m.id " +
                "LEFT JOIN users u ON r.user_id = u.id " +
                "LEFT JOIN parent p ON u.id = p.user_id " +
                "LEFT JOIN agent_ecole ae ON u.id = ae.user_id " +
                "WHERE r.id_ecole = ? " +                    // Réclamation directe sur l'école
                "OR c.id_ecole = ? " +                       // Réclamation sur un chauffeur de cette école
                "OR b.id_ecole = ? " +                       // Réclamation sur un bus de cette école
                "OR m.id_ecole = ? " +                       // Réclamation sur une maîtresse de cette école
                "OR (u.categorie = 'PARENT' AND p.id_ecole = ?) " +  // Réclamation d'un parent de cette école
                "OR (u.categorie = 'RESPONSABLEECOLE' AND ae.id_ecole = ?) " + // Réclamation d'un responsable de cette école
                "ORDER BY r.date_reclamation DESC";

        try (Connection cnx = getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {

            // Mettre le même paramètre 6 fois
            for (int i = 1; i <= 6; i++) {
                ps.setInt(i, ecoleId);
            }

            System.out.println("Exécution de la requête SQL pour l'école ID: " + ecoleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractReclamationFromResultSet(rs));
                }
            }
            System.out.println("Réclamations trouvées: " + list.size());
        }
        return list;
    }

    // ==================== MÉTHODES DE RÉCUPÉRATION D'ÉCOLE ====================

    /**
     * Récupère l'ID de l'école d'un parent
     */
    public int getEcoleIdByParentId(int parentId) throws SQLException {
        String sql = "SELECT id_ecole FROM parent WHERE id = ?";
        try (Connection cnx = getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, parentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_ecole");
                }
            }
        }
        return 0;
    }

    /**
     * Récupère l'ID de l'école d'un chauffeur
     */
    public int getEcoleIdByChauffeurId(int chauffeurId) throws SQLException {
        String sql = "SELECT id_ecole FROM chauffeur WHERE id = ?";
        try (Connection cnx = getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, chauffeurId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_ecole");
                }
            }
        }
        return 0;
    }

    /**
     * Récupère l'ID de l'école d'une maîtresse
     */
    public int getEcoleIdByMaitresseId(int maitresseId) throws SQLException {
        String sql = "SELECT id_ecole FROM maitresse WHERE id = ?";
        try (Connection cnx = getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, maitresseId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_ecole");
                }
            }
        }
        return 0;
    }
}