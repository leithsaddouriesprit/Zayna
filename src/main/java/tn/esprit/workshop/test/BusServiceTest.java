package tn.esprit.workshop.test;

import tn.esprit.workshop.model.leith.Bus;
import tn.esprit.workshop.services.leith.BusService;

import java.sql.SQLException;
import java.util.List;

public class BusServiceTest {

    public static void main(String[] args) {

        BusService busService = new BusService();

        try {
            // INSERT
            Bus bus = new Bus();
            bus.setNumeroBus("BUS-101");
            bus.setMatricule("TN-1234");
            bus.setCapacite(40);
            bus.setIdChauffeur(1);
            bus.setActif(true);

            busService.insertOne(bus);
            System.out.println("✅ Bus inséré");

            // SELECT
            List<Bus> buses = busService.selectAll();
            System.out.println("📋 Liste des bus :");
            for (Bus b : buses) {
                System.out.println(
                        b.getBusId() + " | " +
                                b.getNumeroBus() + " | " +
                                b.getMatricule()
                );
            }

            // UPDATE dernier bus
            if (!buses.isEmpty()) {
                Bus last = buses.get(buses.size() - 1);
                last.setCapacite(45);
                busService.updateOne(last);
                System.out.println("✏️ Bus mis à jour");
            }

            // DELETE dernier bus
            buses = busService.selectAll();
            if (!buses.isEmpty()) {
                Bus last = buses.get(buses.size() - 1);
                busService.deleteOne(last);
                System.out.println("🗑️ Bus supprimé");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

