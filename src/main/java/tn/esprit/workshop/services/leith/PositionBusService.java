package tn.esprit.workshop.services.leith;

import tn.esprit.workshop.model.leith.PositionBus;
import tn.esprit.workshop.utilis.MyBDConnexion;

import java.sql.*;

public class PositionBusService {

    private final Connection connection;

    public PositionBusService() {
        this.connection = MyBDConnexion.getInstance().getConnection();
    }

    public void insertPosition(PositionBus p) throws SQLException {
        String req =
                "INSERT INTO position_bus (id_bus, latitude, longitude, vitesse) VALUES (" +
                        p.getIdBus() + ", " +
                        p.getLatitude() + ", " +
                        p.getLongitude() + ", " +
                        p.getVitesse() +
                        ")";
        Statement st = connection.createStatement();
        st.executeUpdate(req);
    }

    public PositionBus getLastPosition(int busId) throws SQLException {
        String req =
                "SELECT * FROM position_bus WHERE id_bus=" + busId +
                        " ORDER BY timestamp DESC LIMIT 1";

        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);

        if (rs.next()) {
            PositionBus p = new PositionBus();
            p.setId(rs.getInt("id"));
            p.setIdBus(busId);
            p.setLatitude(rs.getDouble("latitude"));
            p.setLongitude(rs.getDouble("longitude"));
            p.setVitesse(rs.getDouble("vitesse"));
            p.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
            return p;
        }
        return null;
    }
}
