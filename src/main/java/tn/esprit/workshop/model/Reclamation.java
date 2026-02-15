package tn.esprit.workshop.model;

import java.sql.Timestamp;

public class Reclamation {

    private int id;
    private int userId;  // Renommé pour suivre les conventions Java
    private String type;
    private String description;
    private Timestamp dateReclamation;
    private String statut;

    public Reclamation() {}

    public Reclamation(int id, int userId, String type, String description, String statut) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.description = description;
        this.statut = statut;
    }

    // Constructeur complet avec date
    public Reclamation(int id, int userId, String type, String description,
                       Timestamp dateReclamation, String statut) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.description = description;
        this.dateReclamation = dateReclamation;
        this.statut = statut;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }  // Renommé
    public void setUserId(int userId) { this.userId = userId; }  // Renommé

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Timestamp getDateReclamation() { return dateReclamation; }
    public void setDateReclamation(Timestamp dateReclamation) { this.dateReclamation = dateReclamation; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    @Override
    public String toString() {
        return "Réclamation #" + id + " - " + type + " - " + statut;
    }
}