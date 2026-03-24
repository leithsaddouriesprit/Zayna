package tn.esprit.workshop.services.leith;

import tn.esprit.workshop.model.tous.Ecole;
import tn.esprit.workshop.utilis.MyBDConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EcoleService {

    public EcoleService() {
    }

    private Connection getConnection() throws SQLException {
        return MyBDConnexion.getInstance().getConnection();
    }

    public List<Ecole> selectAll() throws SQLException {
        List<Ecole> list = new ArrayList<>();
        String sql = "SELECT id, nom FROM ecole ORDER BY nom";
        try (Statement st = getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Ecole e = new Ecole();
                e.setId(rs.getInt("id"));
                e.setNomEcole(rs.getString("nom"));
                list.add(e);
            }
        }
        return list;
    }

    public Ecole getById(int id) throws SQLException {
        String sql = "SELECT id, nom FROM ecole WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Ecole e = new Ecole();
                    e.setId(rs.getInt("id"));
                    e.setNomEcole(rs.getString("nom"));
                    return e;
                }
            }
        }
        return null;
    }
    // Dans EcoleService.java
    public List<Ecole> getAll() throws SQLException {
        List<Ecole> list = new ArrayList<>();
        String sql = "SELECT * FROM ecole";
        try (Connection cnx = MyBDConnexion.getInstance().getConnection();
             Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Ecole e = new Ecole();
                e.setId(rs.getInt("id"));
                e.setNomEcole(rs.getString("nom"));  // ou getNomEcole() selon votre modèle
                // ... autres champs
                list.add(e);
            }
        }
        return list;
    }
}
