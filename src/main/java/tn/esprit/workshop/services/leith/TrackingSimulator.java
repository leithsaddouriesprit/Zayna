package tn.esprit.workshop.services.leith;

import tn.esprit.workshop.model.leith.PositionBus;

import java.util.Timer;
import java.util.TimerTask;

public class TrackingSimulator {

    private double lat = 36.8065;
    private double lng = 10.1815;

    public void startSimulation() {

        Timer timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                lat += 0.0005;
                lng += 0.0005;

                PositionBus p = new PositionBus();
                p.setIdBus(1); // bus test
                p.setLatitude(lat);
                p.setLongitude(lng);
                p.setVitesse(40);

                System.out.println("📍 Nouvelle position : " + lat + ", " + lng);

                // 👉 plus tard : PositionBusService.insertPosition(p);
            }
        }, 0, 2000); // toutes les 2 secondes
    }
}
