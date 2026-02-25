package tn.esprit.workshop.services.leith;

import tn.esprit.workshop.model.leith.Bus;
import tn.esprit.workshop.services.CRUD;
import tn.esprit.workshop.utilis.MyBDConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BusService implements CRUD<Bus> {

    private final Connection connection;

    public BusService() {
        this.connection = MyBDConnexion.getInstance().getConnection();
    }

    @Override
    public void insertOne(Bus bus) throws SQLException {
        String req =
                "INSERT INTO bus (numero_bus, matricule, capacite, id_chauffeur, actif) VALUES (" +
                        "'" + bus.getNumeroBus() + "', " +
                        "'" + bus.getMatricule() + "', " +
                        bus.getCapacite() + ", " +
                        bus.getIdChauffeur() + ", " +
                        bus.isActif() +
                        ")";
        Statement st = connection.createStatement();
        st.executeUpdate(req);
    }

    @Override
    public void updateOne(Bus bus) throws SQLException {
        String req =
                "UPDATE bus SET " +
                        "numero_bus='" + bus.getNumeroBus() + "', " +
                        "matricule='" + bus.getMatricule() + "', " +
                        "capacite=" + bus.getCapacite() + ", " +
                        "id_chauffeur=" + bus.getIdChauffeur() + ", " +
                        "actif=" + bus.isActif() +
                        " WHERE id=" + bus.getBusId();

        Statement st = connection.createStatement();
        st.executeUpdate(req);
    }

    @Override
    public void deleteOne(Bus bus) throws SQLException {
        String req = "DELETE FROM bus WHERE id=" + bus.getBusId();
        Statement st = connection.createStatement();
        st.executeUpdate(req);
    }

    @Override
    public List<Bus> selectAll() throws SQLException {
        List<Bus> list = new ArrayList<>();
        String req = "SELECT * FROM bus";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            Bus b = new Bus();
            b.setBusId(rs.getInt("id"));
            b.setNumeroBus(rs.getString("numero_bus"));
            b.setMatricule(rs.getString("matricule"));
            b.setCapacite(rs.getInt("capacite"));
            b.setIdChauffeur(rs.getInt("id_chauffeur"));
            b.setActif(rs.getBoolean("actif"));
            list.add(b);
        }
        return list;
    }
    public Bus getById(int id) throws SQLException {

        String req = "SELECT * FROM bus WHERE id = " + id;

        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);

        if (rs.next()) {

            Bus bus = new Bus();

            bus.setBusId(rs.getInt("id"));
            bus.setNumeroBus(rs.getString("numero_bus"));
            bus.setMatricule(rs.getString("matricule"));
            bus.setCapacite(rs.getInt("capacite"));
            bus.setIdChauffeur(rs.getInt("id_chauffeur"));
            bus.setActif(rs.getBoolean("actif"));

            return bus;
        }

        return null;
    }

    /**
     * Bus assigned to this chauffeur (at most one). Ignores buses without chauffeur.
     */
    public Bus getByChauffeurId(int chauffeurId) throws SQLException {
        String sql = "SELECT id, numero_bus, matricule, capacite, id_chauffeur, actif FROM bus WHERE id_chauffeur = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, chauffeurId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Bus bus = new Bus();
                    bus.setBusId(rs.getInt("id"));
                    bus.setNumeroBus(rs.getString("numero_bus"));
                    bus.setMatricule(rs.getString("matricule"));
                    bus.setCapacite(rs.getInt("capacite"));
                    bus.setIdChauffeur(rs.getInt("id_chauffeur"));
                    bus.setActif(rs.getBoolean("actif"));
                    return bus;
                }
            }
        }
        return null;
    }

}
