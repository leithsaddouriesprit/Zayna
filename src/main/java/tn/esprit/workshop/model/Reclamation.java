package tn.esprit.workshop.model;

import java.sql.Timestamp;

public class Reclamation {

    private int id;
    private int userId;
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

    public Reclamation(int id, String parent, String value, String text, String enAttente) {

    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Timestamp getDateReclamation() { return dateReclamation; }
    public void setDateReclamation(Timestamp dateReclamation) { this.dateReclamation = dateReclamation; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
}
