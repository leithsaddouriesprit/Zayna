package tn.esprit.workshop.model;

import java.time.LocalDateTime;

public class Reponse {

    private int id;  // ajouté
    private int reclamation_id;
    private String message;
    private LocalDateTime date;

    public Reponse() { }

    public Reponse(int reclamation_id, String message, LocalDateTime date) {
        this.reclamation_id = reclamation_id;
        this.message = message;
        this.date = date;
    }

    public Reponse(int id, int reclamation_id, String message, LocalDateTime date) {
        this.id = id;
        this.reclamation_id = reclamation_id;
        this.message = message;
        this.date = date;
    }

    // getters et setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getreclamation_id() { return reclamation_id; }
    public void setreclamation_id(int reclamation_id) { this.reclamation_id = reclamation_id; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    @Override
    public String toString() {
        return "Reponse{" +
                "id=" + id +
                ", reclamation_id=" + reclamation_id +
                ", message='" + message + '\'' +
                ", date=" + date +
                '}';
    }
}
