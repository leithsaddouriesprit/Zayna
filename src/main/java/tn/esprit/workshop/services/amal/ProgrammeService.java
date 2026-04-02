package tn.esprit.workshop.services.amal;

import tn.esprit.workshop.model.amal.Programme;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProgrammeService {

    private final Connection connection;

    public ProgrammeService() {
        connection = MyBDConnexion.getInstance().getConnection();
    }

    public void insertProgramme(Programme p) throws SQLException {
        String req = "INSERT INTO programme (nomProgramme, descriptionProgramme, niveau, duree) VALUES (?,?,?,?)";

        PreparedStatement ps = connection.prepareStatement(req);
        ps.setString(1, p.getNomProgramme());
        ps.setString(2, p.getDescriptionProgramme());
        ps.setString(3, p.getNiveau());
        ps.setString(4, p.getDuree());

        ps.executeUpdate();
    }

    public void updateProgramme(Programme p) throws SQLException {
        String req = "UPDATE programme SET nomProgramme=?, descriptionProgramme=?, niveau=?, duree=? WHERE id=?";

        PreparedStatement ps = connection.prepareStatement(req);
        ps.setString(1, p.getNomProgramme());
        ps.setString(2, p.getDescriptionProgramme());
        ps.setString(3, p.getNiveau());
        ps.setString(4, p.getDuree());
        ps.setInt(5, p.getId());

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
}
