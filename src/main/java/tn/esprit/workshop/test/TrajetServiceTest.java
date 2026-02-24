package tn.esprit.workshop.test;

import tn.esprit.workshop.model.leith.Bus;
import tn.esprit.workshop.model.leith.Trajet;
import tn.esprit.workshop.services.leith.BusService;
import tn.esprit.workshop.services.leith.TrajetService;


import java.sql.SQLException;
import java.time.LocalTime;
import java.util.List;

public class TrajetServiceTest {

    public static void main(String[] args) {

        try {
            // 1️⃣ Services
            BusService busService = new BusService();
            TrajetService trajetService = new TrajetService();

            // 2️⃣ INSERT BUS (obligatoire AVANT trajet)
            Bus bus = new Bus();
            bus.setNumeroBus("BUS-TEST");
            bus.setMatricule("TN-9999");
            bus.setCapacite(40);
            bus.setIdChauffeur(1);
            bus.setActif(true);

            busService.insertOne(bus);
            System.out.println("✅ Bus de test inséré");

            // 3️⃣ Récupérer le BON id_bus
            List<Bus> buses = busService.selectAll();
            Bus lastBus = buses.get(buses.size() - 1);

            // 4️⃣ INSERT TRAJET (maintenant seulement)
            Trajet t = new Trajet();
            t.setNom("Trajet Test");
            t.setIdBus(lastBus.getBusId()); // 🔴 clé étrangère valide
            t.setIdEcole(1);
            t.setHeureDepart(LocalTime.of(7, 30));
            t.setActif(true);
            t.setStatut("PLANIFIE");

            trajetService.insertOne(t);
            System.out.println("✅ Trajet inséré avec succès");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
