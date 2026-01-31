package tn.esprit.workshop.model;

import java.util.Objects;

public class Chauffeur {
    private int id;
    private String nom;
    private String prenom;
    private int age;
    private int nbAnsExperience;

    public Chauffeur() {}
    public Chauffeur(int id, String nom, String prenom, int age, int nbAnsExperience) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.age = age;
        this.nbAnsExperience = nbAnsExperience;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getNbAnsExperience() {
        return nbAnsExperience;
    }

    public void setNbAnsExperience(int nbAnsExperience) {
        this.nbAnsExperience = nbAnsExperience;
    }

    @Override
    public String toString() {
        return "Chauffeur{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", age=" + age +
                ", nbAnsExperience=" + nbAnsExperience +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Chauffeur chauffeur)) return false;
        return id == chauffeur.id && age == chauffeur.age && nbAnsExperience == chauffeur.nbAnsExperience && Objects.equals(nom, chauffeur.nom) && Objects.equals(prenom, chauffeur.prenom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nom, prenom, age, nbAnsExperience);
    }
}
