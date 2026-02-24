package tn.esprit.workshop.services;

import tn.esprit.workshop.model.Ecole;
import tn.esprit.workshop.utilis.MyBDConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EcoleService {

    private Connection connection;

    public EcoleService() {
        connection = MyBDConnexion.getInstance().getConnection();
    }

    // Ajouter une école
    public void insertEcole(Ecole ecole) throws SQLException {
        String req = "INSERT INTO ecole (nom, position, prix_mensuel, description, informations) VALUES (?,?,?,?,?)";
        PreparedStatement ps = connection.prepareStatement(req);

        ps.setString(1, ecole.getNom());
        ps.setString(2, ecole.getPosition());
        ps.setDouble(3, ecole.getPrixMensuel());
        ps.setString(4, ecole.getDescription());
        ps.setString(5, ecole.getInformations());

        ps.executeUpdate();
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
    public void deleteEcole(int id) throws SQLException {
        String req = "DELETE FROM ecole WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    // Afficher toutes les écoles
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

    // Trouver une école par ID
    public Ecole findEcoleById(int id) throws SQLException {
        String req = "SELECT * FROM ecole WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, id);

        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            Ecole e = new Ecole();
            e.setId(rs.getInt("id"));
            e.setNom(rs.getString("nom"));
            e.setPosition(rs.getString("position"));
            e.setPrixMensuel(rs.getDouble("prix_mensuel"));
            e.setDescription(rs.getString("description"));
            e.setInformations(rs.getString("informations"));
            return e;
        }
        return null;
    }
}