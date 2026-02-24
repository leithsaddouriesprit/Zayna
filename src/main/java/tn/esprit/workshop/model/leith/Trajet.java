package tn.esprit.workshop.model.leith;

import java.time.LocalTime;

public class Trajet {

    private int trajetId;
    private String nom;
    private int idBus;
    private int idEcole;
    private LocalTime heureDepart;
    private boolean actif;
    private String statut; // PLANIFIE, EN_COURS, TERMINE

    public Trajet() {}

    public Trajet(int trajetId, String nom, int idBus, int idEcole,
                  LocalTime heureDepart, boolean actif, String statut) {
        this.trajetId = trajetId;
        this.nom = nom;
        this.idBus = idBus;
        this.idEcole = idEcole;
        this.heureDepart = heureDepart;
        this.actif = actif;
        this.statut = statut;
    }

    public int getTrajetId() {
        return trajetId;
    }

    public void setTrajetId(int trajetId) {
        this.trajetId = trajetId;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public int getIdBus() {
        return idBus;
    }

    public void setIdBus(int idBus) {
        this.idBus = idBus;
    }

    public int getIdEcole() {
        return idEcole;
    }

    public void setIdEcole(int idEcole) {
        this.idEcole = idEcole;
    }

    public LocalTime getHeureDepart() {
        return heureDepart;
    }

    public void setHeureDepart(LocalTime heureDepart) {
        this.heureDepart = heureDepart;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    @Override
    public String toString() {
        return "Trajet{" +
                "trajetId=" + trajetId +
                ", nom='" + nom + '\'' +
                ", idBus=" + idBus +
                ", idEcole=" + idEcole +
                ", heureDepart=" + heureDepart +
                ", actif=" + actif +
                ", statut='" + statut + '\'' +
                '}';
    }
}
