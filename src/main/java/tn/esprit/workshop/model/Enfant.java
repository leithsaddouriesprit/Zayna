package tn.esprit.workshop.model;

import java.time.LocalDate;

public class Enfant {
    private int id;
    private String nom;
    private String prenom;
    private LocalDate dateNaissance;
    private int parentId;
    private int ecoleId;
    private String classe;
    private String numTelephoneParent;
    private String emailParent;
    private String photo;
    private String statut;  // ACTIF, INACTIF, EXCLU
    private LocalDate dateInscription;

    // Constructeurs
    public Enfant() {}

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public LocalDate getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(LocalDate dateNaissance) { this.dateNaissance = dateNaissance; }

    public int getParentId() { return parentId; }
    public void setParentId(int parentId) { this.parentId = parentId; }

    public int getEcoleId() { return ecoleId; }
    public void setEcoleId(int ecoleId) { this.ecoleId = ecoleId; }

    public String getClasse() { return classe; }
    public void setClasse(String classe) { this.classe = classe; }

    public String getNumTelephoneParent() { return numTelephoneParent; }
    public void setNumTelephoneParent(String numTelephoneParent) { this.numTelephoneParent = numTelephoneParent; }

    public String getEmailParent() { return emailParent; }
    public void setEmailParent(String emailParent) { this.emailParent = emailParent; }

    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public LocalDate getDateInscription() { return dateInscription; }
    public void setDateInscription(LocalDate dateInscription) { this.dateInscription = dateInscription; }

    public String getNomComplet() { return prenom + " " + nom; }

    @Override
    public String toString() {
        return getNomComplet();
    }
}