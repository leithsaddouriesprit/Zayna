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

    // ===================== ECOLE =====================

    // Ajouter une école et retourner son ID généré
    public int insertEcole(Ecole ecole) throws SQLException {
        String req = "INSERT INTO ecole (nom, position, prix_mensuel, description, informations) VALUES (?,?,?,?,?)";
        PreparedStatement ps = connection.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);

        ps.setString(1, ecole.getNom());
        ps.setString(2, ecole.getPosition());
        ps.setDouble(3, ecole.getPrixMensuel());
        ps.setString(4, ecole.getDescription());
        ps.setString(5, ecole.getInformations());

        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            return rs.getInt(1); // Retourne l'id généré de l'école
        }
        return -1;
    }

    // Modifier une école
    public void updateEcole(Ecole ecole) throws SQLException {
        String req = "UPDATE ecole SET nom=?, position=?, prix_mensuel=?, description=?, informations=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);

        ps.setString(1, ecole.getNom());
        ps.setString(2, ecole.getPosition());
        ps.setDouble(3, ecole.getPrixMensuel());
        ps.setString(4, ecole.getDescription());
        ps.setString(5, ecole.getInformations());
        ps.setInt(6, ecole.getId());

        ps.executeUpdate();
    }

    // Supprimer une école
    public void deleteEcole(Ecole ecole) throws SQLException {
        String req = "DELETE FROM ecole WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, ecole.getId());
        ps.executeUpdate();
    }

    // Lister toutes les écoles
    public List<Ecole> selectAllEcoles() throws SQLException {
        List<Ecole> ecoles = new ArrayList<>();
        String req = "SELECT * FROM ecole";

        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            Ecole e = new Ecole();
            e.setId(rs.getInt("id"));
            e.setNom(rs.getString("nom"));
            e.setPosition(rs.getString("position"));
            e.setPrixMensuel(rs.getDouble("prix_mensuel"));
            e.setDescription(rs.getString("description"));
            e.setInformations(rs.getString("informations"));
            ecoles.add(e);
        }
        return ecoles;
    }

    // ===================== PROGRAMME =====================

    // Ajouter un programme
    public void insertProgramme(Programme p) throws SQLException {
        String req = "INSERT INTO programme (id_ecole, nom, description, niveau, duree, prix) VALUES (?,?,?,?,?,?)";

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
        String req = "UPDATE programme SET nom=?, description=?, niveau=?, duree=?, prix=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);

        ps.setString(1, p.getNomProgramme());
        ps.setString(2, p.getDescriptionProgramme());
        ps.setString(3, p.getNiveau());
        ps.setString(4, p.getDuree());
        ps.setDouble(5, p.getPrixProgramme());
        ps.setInt(6, p.getId());

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
            p.setEcoleId(rs.getInt("id_ecole"));
            p.setNomProgramme(rs.getString("nom"));
            p.setDescriptionProgramme(rs.getString("description"));
            p.setNiveau(rs.getString("niveau"));
            p.setDuree(rs.getString("duree"));
            p.setPrixProgramme(rs.getDouble("prix"));
            programmes.add(p);
        }
        return programmes;
    }
}
