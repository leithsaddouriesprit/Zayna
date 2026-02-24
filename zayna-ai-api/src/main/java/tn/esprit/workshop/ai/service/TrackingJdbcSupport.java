package tn.esprit.workshop.ai.service;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.Map;


@Service
public class TrackingJdbcSupport {

    private final DataSource dataSource;

    public TrackingJdbcSupport(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Map<String, Object> getBusBasicInfo(int busId) {
        String sql = "SELECT id, matricule FROM bus WHERE id=?";
        // JDBC simple
        return Map.of();
    }
}