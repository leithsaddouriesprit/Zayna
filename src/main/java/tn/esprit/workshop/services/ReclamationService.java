package tn.esprit.workshop.services;

import tn.esprit.workshop.model.Reclamation;
import tn.esprit.workshop.utilis.MyBDConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReclamationService implements CRUD<Reclamation> {

    private final Connection connection;

    public ReclamationService() {
        connection = MyBDConnexion.getInstance().getConnection();
    }

    // ✅ AJOUTER
    @Override
    public void insertOne(Reclamation r) throws SQLException {

        if(r.getDescription().isEmpty() || r.getType().isEmpty()){
            throw new IllegalArgumentException("Les champs sont obligatoires !");
        }

        String req = "INSERT INTO reclamation (user_id, type, description, statut) VALUES (?, ?, ?, ?)";

        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, r.getUserId());
        ps.setString(2, r.getType());
        ps.setString(3, r.getDescription());
        ps.setString(4, "EN_ATTENTE");

        ps.executeUpdate();
        System.out.println("✅ Réclamation ajoutée");
    }

    // ✅ MODIFIER
    @Override
    public void updateOne(Reclamation r) throws SQLException {

        String req = "UPDATE reclamation SET type=?, description=?, statut=? WHERE id=?";

        PreparedStatement ps = connection.prepareStatement(req);
        ps.setString(1, r.getType());
        ps.setString(2, r.getDescription());
        ps.setString(3, r.getStatut());
        ps.setInt(4, r.getId());

        ps.executeUpdate();
        System.out.println("✅ Réclamation modifiée");
    }

    // ✅ SUPPRIMER
    @Override
    public void deleteOne(Reclamation r) throws SQLException {

        String req = "DELETE FROM reclamation WHERE id=?";

        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, r.getId());

        ps.executeUpdate();
        System.out.println("🗑 Réclamation supprimée");
    }

    // ✅ AFFICHER TOUT
    @Override
    public List<Reclamation> selectAll() throws SQLException {

        List<Reclamation> list = new ArrayList<>();
        String req = "SELECT * FROM reclamation";

        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);

        while(rs.next()){
            Reclamation r = new Reclamation();
            r.setId(rs.getInt("id"));
            r.setUserId(rs.getInt("user_id"));
            r.setType(rs.getString("type"));
            r.setDescription(rs.getString("description"));
            r.setStatut(rs.getString("statut"));

            list.add(r);
        }

        return list;
    }

    // ✅ RECHERCHER
    public List<Reclamation> rechercherParStatut(String statut) throws SQLException {

        List<Reclamation> list = new ArrayList<>();

        String req = "SELECT * FROM reclamation WHERE statut = ?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setString(1, statut);

        ResultSet rs = ps.executeQuery();

        while(rs.next()){
            Reclamation r = new Reclamation();
            r.setId(rs.getInt("id"));
            r.setUserId(rs.getInt("user_id"));
            r.setType(rs.getString("type"));
            r.setDescription(rs.getString("description"));
            r.setStatut(rs.getString("statut"));

            list.add(r);
        }

        return list;
    }
}
