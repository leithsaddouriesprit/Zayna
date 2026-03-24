package tn.esprit.workshop.services.leith;

import tn.esprit.workshop.model.leith.Bus;
import tn.esprit.workshop.services.CRUD;
import tn.esprit.workshop.utilis.MyBDConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BusService implements CRUD<Bus> {

    private static final Logger LOG = Logger.getLogger(BusService.class.getName());

    public BusService() {
    }

    private Connection getConnection() throws SQLException {
        return MyBDConnexion.getInstance().getConnection();
    }

    @Override
    public void insertOne(Bus bus) throws SQLException {
        Integer idEcole = bus.getIdEcole();
        if (idEcole == null || idEcole == 0) {
            LOG.warning("insertOne(bus): id_ecole is missing or zero (idEcole=" + idEcole + "). Agent flow requires a valid school.");
            throw new SQLException("Le bus doit être rattaché à une école (id_ecole manquant).");
        }
        LOG.log(Level.FINE, "insertOne(bus): id_ecole={0}, numero_bus={1}", new Object[]{idEcole, bus.getNumeroBus()});

        String sql = "INSERT INTO bus (numero_bus, matricule, capacite, id_chauffeur, actif, id_ecole) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, bus.getNumeroBus());
            ps.setString(2, bus.getMatricule());
            ps.setInt(3, bus.getCapacite());
            // NULL when no chauffeur assigned (0 would violate FK if chauffeur.id has no 0)
            ps.setObject(4, bus.getIdChauffeur() == 0 ? null : bus.getIdChauffeur());
            ps.setBoolean(5, bus.isActif());
            ps.setInt(6, idEcole);
            ps.executeUpdate();
        }
    }

    @Override
    public void updateOne(Bus bus) throws SQLException {
        String sql = "UPDATE bus SET numero_bus=?, matricule=?, capacite=?, id_chauffeur=?, actif=?, id_ecole=? WHERE id=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, bus.getNumeroBus());
            ps.setString(2, bus.getMatricule());
            ps.setInt(3, bus.getCapacite());
            ps.setObject(4, bus.getIdChauffeur() == 0 ? null : bus.getIdChauffeur());
            ps.setBoolean(5, bus.isActif());
            ps.setObject(6, bus.getIdEcole());
            ps.setInt(7, bus.getBusId());
            ps.executeUpdate();
        }
    }

    @Override
    public void deleteOne(Bus bus) throws SQLException {
        desaffecterDesTrajets(bus.getBusId());
        String req = "DELETE FROM bus WHERE id=" + bus.getBusId();
        Statement st = getConnection().createStatement();
        st.executeUpdate(req);
    }

    /** Désaffecte ce bus de tous les trajets avant suppression. */
    public void desaffecterDesTrajets(int busId) throws SQLException {
        String req = "UPDATE trajet SET id_bus = NULL WHERE id_bus = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(req)) {
            ps.setInt(1, busId);
            ps.executeUpdate();
        }
    }

    /** Affecte un chauffeur au bus. */
    public void updateIdChauffeur(int busId, Integer chauffeurId) throws SQLException {
        String req = "UPDATE bus SET id_chauffeur = ? WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(req)) {
            ps.setObject(1, chauffeurId);
            ps.setInt(2, busId);
            ps.executeUpdate();
        }
    }

    @Override
    public List<Bus> selectAll() throws SQLException {
        return selectByEcoleId(null);
    }

    /** Bus de l'école (idEcole null = tous). Pour l'agent : idEcole = AppSession.getEcoleId(). */
    public List<Bus> selectByEcoleId(Integer idEcole) throws SQLException {
        List<Bus> list = new ArrayList<>();
        String sql = idEcole == null
                ? "SELECT * FROM bus ORDER BY numero_bus"
                : "SELECT * FROM bus WHERE id_ecole = ? ORDER BY numero_bus";
        if (idEcole == null) {
            try (Statement st = getConnection().createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) list.add(mapBus(rs));
            }
        } else {
            try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
                ps.setInt(1, idEcole);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapBus(rs));
                }
            }
        }
        return list;
    }

    /** Bus actifs (actif = 1). Si idEcole != null, limité à cette école. */
    public List<Bus> selectActifs(Integer idEcole) throws SQLException {
        List<Bus> list = new ArrayList<>();
        String sql = idEcole == null
                ? "SELECT * FROM bus WHERE actif = 1 ORDER BY numero_bus"
                : "SELECT * FROM bus WHERE actif = 1 AND id_ecole = ? ORDER BY numero_bus";
        if (idEcole == null) {
            try (PreparedStatement ps = getConnection().prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapBus(rs));
            }
        } else {
            try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
                ps.setInt(1, idEcole);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapBus(rs));
                }
            }
        }
        return list;
    }

    private Bus mapBus(ResultSet rs) throws SQLException {
        Bus b = new Bus();
        b.setBusId(rs.getInt("id"));
        b.setNumeroBus(rs.getString("numero_bus"));
        b.setMatricule(rs.getString("matricule"));
        b.setCapacite(rs.getInt("capacite"));
        b.setIdChauffeur(rs.getInt("id_chauffeur"));
        b.setActif(rs.getBoolean("actif"));
        int idEcole = rs.getInt("id_ecole");
        if (!rs.wasNull()) b.setIdEcole(idEcole);
        return b;
    }
    public Bus getById(int id) throws SQLException {
        String sql = "SELECT * FROM bus WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapBus(rs);
            }
        }
        return null;
    }

    /** Bus assigned to this chauffeur (at most one). Ignores buses without chauffeur. */
    public Bus getByChauffeurId(int chauffeurId) throws SQLException {
        String sql = "SELECT * FROM bus WHERE id_chauffeur = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, chauffeurId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapBus(rs);
            }
        }
        return null;
    }
    // Dans BusService.java
    public List<Bus> getAll() throws SQLException {
        List<Bus> list = new ArrayList<>();
        String sql = "SELECT * FROM bus";
        try (Connection cnx = MyBDConnexion.getInstance().getConnection();
             Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Bus b = new Bus();
                b.setNumeroBus(rs.getString("id"));
                b.setMatricule(rs.getString("matricule"));
                // ... autres champs
                list.add(b);
            }
        }
        return list;
    }
}
