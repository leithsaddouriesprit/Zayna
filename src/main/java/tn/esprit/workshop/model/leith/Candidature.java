package tn.esprit.workshop.model.leith;

import java.time.LocalDateTime;

/**
 * DTO for candidature (one per chauffeur).
 * Statuts possibles: ENVOYEE, ACCEPTEE, REFUSEE.
 * Les champs age / nbAnsExperience viennent de la table chauffeur (JOIN) pour affichage uniquement.
 */
public class Candidature {

    private int id;
    private int chauffeurId;
    private int idEcole;    // École ciblée (filtrage agent)
    private String statut;   // ENVOYEE, ACCEPTEE, REFUSEE
    private LocalDateTime dateEnvoi;
    private String maladie;

    // Infos chauffeur pour affichage
    private String nom;
    private String prenom;
    private int age;
    private int nbAnsExperience;

    // Aperçu permis (optionnel)
    private byte[] permisRecto;
    private byte[] permisVerso;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getChauffeurId() {
        return chauffeurId;
    }

    public void setChauffeurId(int chauffeurId) {
        this.chauffeurId = chauffeurId;
    }

    public int getIdEcole() {
        return idEcole;
    }

    public void setIdEcole(int idEcole) {
        this.idEcole = idEcole;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public LocalDateTime getDateEnvoi() {
        return dateEnvoi;
    }

    public void setDateEnvoi(LocalDateTime dateEnvoi) {
        this.dateEnvoi = dateEnvoi;
    }

    public String getMaladie() {
        return maladie;
    }

    public void setMaladie(String maladie) {
        this.maladie = maladie;
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

    public byte[] getPermisRecto() {
        return permisRecto;
    }

    public void setPermisRecto(byte[] permisRecto) {
        this.permisRecto = permisRecto;
    }

    public byte[] getPermisVerso() {
        return permisVerso;
    }

    public void setPermisVerso(byte[] permisVerso) {
        this.permisVerso = permisVerso;
    }
}

