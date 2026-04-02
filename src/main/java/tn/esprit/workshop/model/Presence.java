package tn.esprit.workshop.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Presence {
    private int id;
    private int enfantId;
    private LocalDate date;
    private String statutMatin;   // PRESENT, ABSENT, RETARD, JUSTIFIE
    private LocalTime heureArrivee;
    private String statutMidi;     // PRESENT, ABSENT, JUSTIFIE
    private LocalTime heureDepart;
    private String statutSoir;     // PRESENT, ABSENT, JUSTIFIE
    private boolean remorqueEnfant;
    private String remorqueur;
    private String observations;

    // Constructeurs
    public Presence() {}

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEnfantId() { return enfantId; }
    public void setEnfantId(int enfantId) { this.enfantId = enfantId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getStatutMatin() { return statutMatin; }
    public void setStatutMatin(String statutMatin) { this.statutMatin = statutMatin; }

    public LocalTime getHeureArrivee() { return heureArrivee; }
    public void setHeureArrivee(LocalTime heureArrivee) { this.heureArrivee = heureArrivee; }

    public String getStatutMidi() { return statutMidi; }
    public void setStatutMidi(String statutMidi) { this.statutMidi = statutMidi; }

    public LocalTime getHeureDepart() { return heureDepart; }
    public void setHeureDepart(LocalTime heureDepart) { this.heureDepart = heureDepart; }

    public String getStatutSoir() { return statutSoir; }
    public void setStatutSoir(String statutSoir) { this.statutSoir = statutSoir; }

    public boolean isRemorqueEnfant() { return remorqueEnfant; }
    public void setRemorqueEnfant(boolean remorqueEnfant) { this.remorqueEnfant = remorqueEnfant; }

    public String getRemorqueur() { return remorqueur; }
    public void setRemorqueur(String remorqueur) { this.remorqueur = remorqueur; }

    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }
}
