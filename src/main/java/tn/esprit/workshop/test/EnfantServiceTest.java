package tn.esprit.workshop.test;

import tn.esprit.workshop.model.leith.Enfant;
import tn.esprit.workshop.services.leith.EnfantService;

import java.sql.SQLException;

public class EnfantServiceTest {

    public static void main(String[] args) {

        try {
            EnfantService enfantService = new EnfantService();

            Enfant e = new Enfant();
            e.setNom("Leith");
            e.setPrenom("Junior");
            e.setParentId(1);   // parent existant
            e.setTrajetId(1);   // trajet existant
            e.setActif(true);

            enfantService.insertOne(e);
            System.out.println("✅ Enfant inséré");

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}

