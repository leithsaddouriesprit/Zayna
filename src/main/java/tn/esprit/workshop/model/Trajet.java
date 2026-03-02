package tn.esprit.workshop.model;

import java.time.LocalTime;

public class Trajet {
    private int id;
    private int ecoleId;
    private String nomTrajet;
    private String pointDepart;
    private String pointArrivee;
    private LocalTime heureDepart;
    private LocalTime heureArrivee;
    private String jours;
    private double prixMensuel;
    private String description;
    private int placesDisponibles;

    public Trajet() {}

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEcoleId() { return ecoleId; }
    public void setEcoleId(int ecoleId) { this.ecoleId = ecoleId; }

    public String getNomTrajet() { return nomTrajet; }
    public void setNomTrajet(String nomTrajet) { this.nomTrajet = nomTrajet; }

    public String getPointDepart() { return pointDepart; }
    public void setPointDepart(String pointDepart) { this.pointDepart = pointDepart; }

    public String getPointArrivee() { return pointArrivee; }
    public void setPointArrivee(String pointArrivee) { this.pointArrivee = pointArrivee; }

    public LocalTime getHeureDepart() { return heureDepart; }
    public void setHeureDepart(LocalTime heureDepart) { this.heureDepart = heureDepart; }

    public LocalTime getHeureArrivee() { return heureArrivee; }
    public void setHeureArrivee(LocalTime heureArrivee) { this.heureArrivee = heureArrivee; }

    public String getJours() { return jours; }
    public void setJours(String jours) { this.jours = jours; }

    public double getPrixMensuel() { return prixMensuel; }
    public void setPrixMensuel(double prixMensuel) { this.prixMensuel = prixMensuel; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getPlacesDisponibles() { return placesDisponibles; }
    public void setPlacesDisponibles(int placesDisponibles) { this.placesDisponibles = placesDisponibles; }
}