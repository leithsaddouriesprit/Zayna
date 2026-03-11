package tn.esprit.workshop.model.Talel.talel2;

import java.time.LocalDate;
import java.util.List;

public class Chauffeur extends User {
    private String permis; // "B", "C", "D", etc.
    private LocalDate dateObtentionPermis;
    private String telephone;
    private String adresse;
    private String vehicule; // Modèle du véhicule
    private List<String> lignes; // Lignes de bus qu'il dessert
    private double salaire;
   

    // Constructeurs
    public Chauffeur() {
        super();
        this.setCategories(CategorieUser.CHAUFFEUR);
    }

    public Chauffeur(int id, String nom, String email, String password,
                     String permis, String telephone) {
        super(id, nom, email, password, CategorieUser.CHAUFFEUR);
        this.permis = permis;
        this.telephone = telephone;
    }

    // Getters et Setters
    public String getPermis() { return permis; }
    public void setPermis(String permis) { this.permis = permis; }

    public LocalDate getDateObtentionPermis() { return dateObtentionPermis; }
    public void setDateObtentionPermis(LocalDate dateObtentionPermis) { this.dateObtentionPermis = dateObtentionPermis; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getVehicule() { return vehicule; }
    public void setVehicule(String vehicule) { this.vehicule = vehicule; }

    public List<String> getLignes() { return lignes; }
    public void setLignes(List<String> lignes) { this.lignes = lignes; }
    public void addLigne(String ligne) { this.lignes.add(ligne); }
    public double getSalaire() { return salaire; }
    public void setSalaire(double salaire) { this.salaire = salaire; }

    @Override
    public String toString() {
        return "Chauffeur{" +
                "id=" + getId() +
                ", nom='" + getNom() + '\'' +
                ", permis='" + permis + '\'' +
                ", vehicule='" + vehicule + '\'' +
                '}';
    }
}