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
            throw new IllegalStateException("Connexion base indisponible pour ProgrammeService", e);
        }
    }

    private static int parseDureeMois(String duree) {
        if (duree == null || duree.trim().isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(duree.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public void insertProgramme(Programme p) throws SQLException {
        String req = "INSERT INTO programme (ecoleId, nomProgramme, descriptionProgramme, niveau, duree) VALUES (?,?,?,?,?)";

        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, p.getEcoleId());
        ps.setString(2, p.getNomProgramme());
        ps.setString(3, p.getDescriptionProgramme());
        ps.setString(4, p.getNiveau());
        ps.setInt(5, parseDureeMois(p.getDuree()));

        ps.executeUpdate();
    }

    public void updateProgramme(Programme p) throws SQLException {
        String req = "UPDATE programme SET ecoleId=?, nomProgramme=?, descriptionProgramme=?, niveau=?, duree=? WHERE id=?";

        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, p.getEcoleId());
        ps.setString(2, p.getNomProgramme());
        ps.setString(3, p.getDescriptionProgramme());
        ps.setString(4, p.getNiveau());
        ps.setInt(5, parseDureeMois(p.getDuree()));
        ps.setInt(6, p.getId());

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
        ResultSetMetaData md = rs.getMetaData();
        boolean hasPrixProgramme = false;
        for (int i = 1; i <= md.getColumnCount(); i++) {
            if ("prixProgramme".equalsIgnoreCase(md.getColumnLabel(i))) {
                hasPrixProgramme = true;
                break;
            }
        }

        while (rs.next()) {
            programmes.add(mapRow(rs, hasPrixProgramme));
        }
        return programmes;
    }

    public List<Programme> selectByEcoleId(int ecoleId) throws SQLException {
        List<Programme> programmes = new ArrayList<>();
        String req = "SELECT * FROM programme WHERE ecoleId = ? ORDER BY nomProgramme";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setInt(1, ecoleId);
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData md = rs.getMetaData();
                boolean hasPrixProgramme = false;
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    if ("prixProgramme".equalsIgnoreCase(md.getColumnLabel(i))) {
                        hasPrixProgramme = true;
                        break;
                    }
                }
                while (rs.next()) {
                    programmes.add(mapRow(rs, hasPrixProgramme));
                }
            }
        }
        return programmes;
    }

    private Programme mapRow(ResultSet rs, boolean hasPrixProgramme) throws SQLException {
        Programme p = new Programme();
        p.setId(rs.getInt("id"));
        Object ecoleObj = rs.getObject("ecoleId");
        p.setEcoleId(ecoleObj != null ? ((Number) ecoleObj).intValue() : 0);
        p.setNomProgramme(rs.getString("nomProgramme"));
        p.setDescriptionProgramme(rs.getString("descriptionProgramme"));
        p.setNiveau(rs.getString("niveau"));
        Object dureeObj = rs.getObject("duree");
        if (dureeObj == null) {
            p.setDuree("");
        } else if (dureeObj instanceof Number) {
            p.setDuree(String.valueOf(((Number) dureeObj).intValue()));
        } else {
            p.setDuree(dureeObj.toString());
        }
        if (hasPrixProgramme) {
            double prix = rs.getDouble("prixProgramme");
            p.setPrixProgramme(rs.wasNull() ? 0.0 : prix);
        } else {
            p.setPrixProgramme(0.0);
        }
        return p;
    }
}
