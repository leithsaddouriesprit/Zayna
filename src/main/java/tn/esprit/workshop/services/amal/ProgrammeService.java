package tn.esprit.workshop.services.amal;

import tn.esprit.workshop.model.amal.Programme;
import tn.esprit.workshop.utilis.MyBDConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProgrammeService {

    private final Connection connection;

    public ProgrammeService() {
        try {
            connection = MyBDConnexion.getInstance().getConnection();
        } catch (SQLException e) {
            throw new IllegalStateException("Connexion SQL indisponible pour ProgrammeService", e);
        }
    }

    public void insertProgramme(Programme p) throws SQLException {
        String req = "INSERT INTO programme (ecoleId, nomProgramme, descriptionProgramme, niveau, duree, prixProgramme) VALUES (?,?,?,?,?,?)";

        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, p.getEcoleId());
        ps.setString(2, p.getNomProgramme());
        ps.setString(3, p.getDescriptionProgramme());
        ps.setString(4, p.getNiveau());
        ps.setInt(5, Integer.parseInt(p.getDuree()));
        ps.setDouble(6, p.getPrixProgramme());

        ps.executeUpdate();
    }

    public void updateProgramme(Programme p) throws SQLException {
        String req = "UPDATE programme SET nomProgramme=?, descriptionProgramme=?, niveau=?, duree=?, prixProgramme=? WHERE id=? AND ecoleId=?";

        PreparedStatement ps = connection.prepareStatement(req);
        ps.setString(1, p.getNomProgramme());
        ps.setString(2, p.getDescriptionProgramme());
        ps.setString(3, p.getNiveau());
        ps.setInt(4, Integer.parseInt(p.getDuree()));
        ps.setDouble(5, p.getPrixProgramme());
        ps.setInt(6, p.getId());
        ps.setInt(7, p.getEcoleId());

        ps.executeUpdate();
    }

    public void deleteProgramme(Programme p) throws SQLException {
        String req = "DELETE FROM programme WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, p.getId());
        ps.executeUpdate();
    }

    public List<Programme> selectAllProgrammes() throws SQLException {
        List<Programme> programmes = new ArrayList<>();
        String req = "SELECT * FROM programme";

        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            Programme p = new Programme();
            p.setId(rs.getInt("id"));
            Object ecoleObj = rs.getObject("ecoleId");
            p.setEcoleId(ecoleObj != null ? ((Number) ecoleObj).intValue() : 0);
            p.setNomProgramme(rs.getString("nomProgramme"));
            p.setDescriptionProgramme(rs.getString("descriptionProgramme"));
            p.setNiveau(rs.getString("niveau"));
            p.setDuree(rs.getString("duree"));
            double prix = 0.0;
            Object prixObj = rs.getObject("prixProgramme");
            if (prixObj != null) {
                prix = ((Number) prixObj).doubleValue();
            }
            p.setPrixProgramme(prix);
            programmes.add(p);
        }
        return programmes;
    }

    public List<Programme> selectProgrammesByEcoleId(int ecoleId) throws SQLException {
        List<Programme> programmes = new ArrayList<>();
        String sql = "SELECT id, ecoleId, nomProgramme, descriptionProgramme, niveau, duree, prixProgramme " +
                "FROM programme WHERE ecoleId = ? ORDER BY nomProgramme";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, ecoleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    programmes.add(mapProgramme(rs));
                }
            }
        }
        return programmes;
    }

    public String getEcoleNameById(int ecoleId) throws SQLException {
        String sql = "SELECT nom FROM ecole WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, ecoleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("nom");
                }
            }
        }
        return null;
    }

    private Programme mapProgramme(ResultSet rs) throws SQLException {
        Programme p = new Programme();
        p.setId(rs.getInt("id"));
        Object ecoleObj = rs.getObject("ecoleId");
        p.setEcoleId(ecoleObj != null ? ((Number) ecoleObj).intValue() : 0);
        p.setNomProgramme(rs.getString("nomProgramme"));
        p.setDescriptionProgramme(rs.getString("descriptionProgramme"));
        p.setNiveau(rs.getString("niveau"));
        p.setDuree(String.valueOf(rs.getInt("duree")));
        Object prixObj = rs.getObject("prixProgramme");
        p.setPrixProgramme(prixObj != null ? ((Number) prixObj).doubleValue() : 0d);
        return p;
    }
}
