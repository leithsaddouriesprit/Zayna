package tn.esprit.workshop.model;

import java.time.LocalDateTime;

public class Reponse {

    private int id;  // ajouté
    private int reclamationId;
    private String message;
    private LocalDateTime date;

    public Reponse() { }

    public Reponse(int reclamationId, String message, LocalDateTime date) {
        this.reclamationId = reclamationId;
        this.message = message;
        this.date = date;
    }

    public Reponse(int id, int reclamationId, String message, LocalDateTime date) {
        this.id = id;
        this.reclamationId = reclamationId;
        this.message = message;
        this.date = date;
    }

    // getters et setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getReclamationId() { return reclamationId; }
    public void setReclamationId(int reclamationId) { this.reclamationId = reclamationId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    @Override
    public String toString() {
        return "Reponse{" +
                "id=" + id +
                ", reclamationId=" + reclamationId +
                ", message='" + message + '\'' +
                ", date=" + date +
                '}';
    }
}
