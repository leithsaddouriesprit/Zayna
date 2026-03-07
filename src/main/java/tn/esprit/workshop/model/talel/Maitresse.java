package tn.esprit.workshop.model.talel;

import java.time.LocalDate;

public class Maitresse extends User {
    private String classeResponsable;
    private String diplome;
    private double salaire;
    private String telephone;
    private String adresse;
   
    // Constructeurs
    public Maitresse() {
        super();
        this.setCategories(CategorieUser.MAITRESSE);
    }

    public Maitresse(int id, String nom, String email, String password,
                     String specialite, String classeResponsable) {
        super(id, nom, email, password, CategorieUser.MAITRESSE);
        this.classeResponsable = classeResponsable;
    }

    // Getters et Setters

    public String getClasseResponsable() { return classeResponsable; }
    public void setClasseResponsable(String classeResponsable) { this.classeResponsable = classeResponsable; }

    public String getDiplome() { return diplome; }
    public void setDiplome(String diplome) { this.diplome = diplome; }

    public double getSalaire() { return salaire; }
    public void setSalaire(double salaire) { this.salaire = salaire; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
}