package tn.esprit.workshop.test;

import tn.esprit.workshop.model.leith.Arret;
import tn.esprit.workshop.services.leith.ArretService;

import java.sql.SQLException;
import java.util.List;

public class ArretServiceTest {

    public static void main(String[] args) {

        ArretService arretService = new ArretService();

        try {
            Arret a = new Arret();
            a.setIdTrajet(2); // trajet existant
            a.setNom("Maison Leith");
            a.setLatitude(36.8065);
            a.setLongitude(10.1815);
            a.setOrdre(1);
            a.setHeurePrevue("07:45");

            arretService.insertOne(a);
            System.out.println("✅ Arrêt inséré");

            List<Arret> arrets = arretService.selectAll();
            for (Arret ar : arrets) {
                System.out.println(
                        ar.getArretId() + " | " +
                                ar.getNom() + " | " +
                                ar.getLatitude() + " | " +
                                ar.getLongitude()
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

