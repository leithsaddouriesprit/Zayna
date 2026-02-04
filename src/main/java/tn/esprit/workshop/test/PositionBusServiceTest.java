package tn.esprit.workshop.test;

import tn.esprit.workshop.model.leith.PositionBus;
import tn.esprit.workshop.services.leith.PositionBusService;

import java.sql.SQLException;

public class PositionBusServiceTest {

    public static void main(String[] args) {

        PositionBusService service = new PositionBusService();

        try {
            PositionBus p = new PositionBus();
            p.setIdBus(1);
            p.setLatitude(36.807);
            p.setLongitude(10.182);
            p.setVitesse(35);

            service.insertPosition(p);
            System.out.println("📍 Position enregistrée");

            PositionBus last = service.getLastPosition(1);
            if (last != null) {
                System.out.println(
                        "Dernière position : " +
                                last.getLatitude() + ", " +
                                last.getLongitude()
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

