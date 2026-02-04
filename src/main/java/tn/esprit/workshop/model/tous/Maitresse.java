package tn.esprit.workshop.model.tous;

import java.util.Objects;

public class Maitresse {
    private int id;
    private String nom;
    private String prenom;
    private String nomEcoleAttache;


    public Maitresse() {}

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

    public String getNomEcoleAttache() {
        return nomEcoleAttache;
    }

    public void setNomEcoleAttache(String nomEcoleAttache) {
        this.nomEcoleAttache = nomEcoleAttache;
    }

    @Override
    public String toString() {
        return "Maitresse{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", nomEcoleAttache='" + nomEcoleAttache + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Maitresse maitresse)) return false;
        return id == maitresse.id && Objects.equals(nom, maitresse.nom) && Objects.equals(prenom, maitresse.prenom) && Objects.equals(nomEcoleAttache, maitresse.nomEcoleAttache);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nom, prenom, nomEcoleAttache);
    }
}
