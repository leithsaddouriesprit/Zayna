package tn.esprit.workshop.services;

import tn.esprit.workshop.model.Agent;
import tn.esprit.workshop.utilis.MyBDConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AgentService {

    private Connection connection;

    public AgentService() {
        connection = MyBDConnexion.getInstance().getConnection();
    }

    // ================= AJOUT =================
    public void insertOne(Agent agent) throws SQLException {

        String req = "INSERT INTO agent (nom, position, prix_mensuel, description, informations) VALUES (?,?,?,?,?)";

        PreparedStatement ps = connection.prepareStatement(req);

        ps.setString(1, agent.getNom());
        ps.setString(2, agent.getPosition());
        ps.setDouble(3, agent.getPrixMensuel());
        ps.setString(4, agent.getDescription());
        ps.setString(5, agent.getInformations());

        ps.executeUpdate();
    }

    // ================= MODIFIER =================
    public void updateOne(Agent agent) throws SQLException {

        String req = "UPDATE agent SET nom=?, position=?, prix_mensuel=?, description=?, informations=? WHERE id=?";

        PreparedStatement ps = connection.prepareStatement(req);

        ps.setString(1, agent.getNom());
        ps.setString(2, agent.getPosition());
        ps.setDouble(3, agent.getPrixMensuel());
        ps.setString(4, agent.getDescription());
        ps.setString(5, agent.getInformations());
        ps.setInt(6, agent.getAgentId());

        ps.executeUpdate();
    }

    // ================= SUPPRIMER =================
    public void deleteOne(Agent agent) throws SQLException {

        String req = "DELETE FROM agent WHERE id=?";

        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, agent.getAgentId());
        ps.executeUpdate();
    }

    // ================= AFFICHER =================
    public List<Agent> selectAll() throws SQLException {

        List<Agent> agents = new ArrayList<>();
        String req = "SELECT * FROM agent";

        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {

            Agent e = new Agent();

            e.setAgentId(rs.getInt("id"));
            e.setNom(rs.getString("nom"));
            e.setPosition(rs.getString("position"));
            e.setPrixMensuel(rs.getDouble("prix_mensuel"));
            e.setDescription(rs.getString("description"));
            e.setInformations(rs.getString("informations"));

            agents.add(e);
        }

        return agents;
    }
}