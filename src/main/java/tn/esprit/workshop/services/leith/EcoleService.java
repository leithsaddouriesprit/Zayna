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

    private static Ecole mapEcole(ResultSet rs) throws SQLException {
        Ecole e = new Ecole();
        e.setId(rs.getInt("id"));
        e.setNomEcole(rs.getString("nom"));
        String adresse = rs.getString("adresse");
        e.setAdresse(rs.wasNull() ? null : adresse);
        double lat = rs.getDouble("latitude");
        e.setLatitude(rs.wasNull() ? null : lat);
        double lon = rs.getDouble("longitude");
        e.setLongitude(rs.wasNull() ? null : lon);
        return e;
    }

    public List<Ecole> selectAll() throws SQLException {
        List<Ecole> list = new ArrayList<>();
        String sql = "SELECT id, nom, adresse, latitude, longitude FROM ecole ORDER BY nom";
        try (Statement st = getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapEcole(rs));
            }
        }
        return list;
    }

    public Ecole getById(int id) throws SQLException {
        String sql = "SELECT id, nom, adresse, latitude, longitude FROM ecole WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapEcole(rs);
                }
            }
        }
        return null;
    }

    public List<Ecole> getAll() throws SQLException {
        List<Ecole> list = new ArrayList<>();
        String sql = "SELECT id, nom, adresse, latitude, longitude FROM ecole ORDER BY nom";
        try (Connection cnx = MyBDConnexion.getInstance().getConnection();
             Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapEcole(rs));
            }
        }
        return list;
    }
}
