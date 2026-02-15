package tn.esprit.workshop.model;

import java.sql.Timestamp;

public class Reclamation {

    private int id;
    private int user_id;
    private String type;
    private String description;
    private Timestamp dateReclamation;
    private String statut;

    public Reclamation() {}

    public Reclamation(int id, int user_id, String type, String description, String statut) {
        this.id = id;
        this.user_id = user_id;
        this.type = type;
        this.description = description;
        this.statut = statut;
    }

    public Reclamation(int id, String parent, String value, String text, String enAttente) {

    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getuser_id() { return user_id; }
    public void setuser_id(int user_id) { this.user_id = user_id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Timestamp getDateReclamation() { return dateReclamation; }
    public void setDateReclamation(Timestamp dateReclamation) { this.dateReclamation = dateReclamation; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }



}
