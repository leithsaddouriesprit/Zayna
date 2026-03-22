package tn.esprit.workshop.model;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public class Reponse {

    private int id;  // ajouté
    private int reclamation_id;
    private int userId;          // ✅ AJOUTEZ CETTE LIGNE
    private String message;
    private LocalDateTime date;

    public Reponse() { }

    public Reponse(int reclamation_id, String message, LocalDateTime date) {
        this.reclamation_id = reclamation_id;
        this.message = message;
        this.date = date;
    }

    public Reponse(int id, int reclamation_id ,int userId, String message, LocalDateTime date) {
        this.id = id;
        this.reclamation_id = reclamation_id;
        this.userId = userId;
        this.message = message;
        this.date = date;
    }

    // getters et setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getReclamationId() { return reclamation_id; }
    public void setReclamationId(int reclamation_id) { this.reclamation_id = reclamation_id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }


    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    // ✅ Méthode utilitaire pour formater la date de façon lisible
    public String getDateFormatee() {
        if (date == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return date.format(formatter);
    }
    // ✅ Méthode pour obtenir un aperçu court de la réponse (pour affichage)
    public String getApercu() {
        if (message == null || message.isEmpty()) return "";
        if (message.length() <= 50) return message;
        return message.substring(0, 47) + "...";
    }


    // ✅ toString sans ID
    @Override
    public String toString() {
        return "Réponse du " + getDateFormatee() + " : " + getApercu();
    }
}
