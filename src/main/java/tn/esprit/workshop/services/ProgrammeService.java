package tn.esprit.workshop.services;

import tn.esprit.workshop.model.Ecole;
import tn.esprit.workshop.model.Programme;
import tn.esprit.workshop.utilis.MyBDConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProgrammeService {

    private Connection connection;

    public ProgrammeService() {
        connection = MyBDConnexion.getInstance().getConnection();
    }

    // ===================== PROGRAMME =====================

    // Ajouter un programme
    public void insertProgramme(Programme p) throws SQLException {
        String req = "INSERT INTO programme (ecole_id, nom_programme, description_programme, niveau, duree, prix_programme) VALUES (?,?,?,?,?,?)";

        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, p.getEcoleId());
        ps.setString(2, p.getNomProgramme());
        ps.setString(3, p.getDescriptionProgramme());
        ps.setString(4, p.getNiveau());
        ps.setString(5, p.getDuree());
        ps.setDouble(6, p.getPrixProgramme());

        ps.executeUpdate();
    }

    // Modifier un programme
    public void updateProgramme(Programme p) throws SQLException {
        String req = "UPDATE programme SET ecole_id=?, nom_programme=?, description_programme=?, niveau=?, duree=?, prix_programme=? WHERE id=?";

        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, p.getEcoleId());
        ps.setString(2, p.getNomProgramme());
        ps.setString(3, p.getDescriptionProgramme());
        ps.setString(4, p.getNiveau());
        ps.setString(5, p.getDuree());
        ps.setDouble(6, p.getPrixProgramme());
        ps.setInt(7, p.getId());

        ps.executeUpdate();
    }

    // Supprimer un programme
    public void deleteProgramme(Programme p) throws SQLException {
        String req = "DELETE FROM programme WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, p.getId());
        ps.executeUpdate();
    }

    // Lister tous les programmes
    public List<Programme> selectAllProgrammes() throws SQLException {
        List<Programme> programmes = new ArrayList<>();
        String req = "SELECT * FROM programme";

        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            Programme p = new Programme();
            p.setId(rs.getInt("id"));
            p.setEcoleId(rs.getInt("ecole_id"));  // Changé ici
            p.setNomProgramme(rs.getString("nom_programme"));
            p.setDescriptionProgramme(rs.getString("description_programme"));
            p.setNiveau(rs.getString("niveau"));
            p.setDuree(rs.getString("duree"));
            p.setPrixProgramme(rs.getDouble("prix_programme"));
            programmes.add(p);
        }
        return programmes;
    }
}