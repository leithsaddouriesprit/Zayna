package tn.esprit.workshop.model.leith;

import java.sql.Timestamp;

/**
 * Ligne {@code reponse_reclamation} (zaynaa).
 */
public class ReponseReclamation {

    private int id;
    private int reclamationId;
    private String message;
    private String roleRepondeur;
    private int userId;
    private Timestamp dateReponse;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getReclamationId() {
        return reclamationId;
    }

    public void setReclamationId(int reclamationId) {
        this.reclamationId = reclamationId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRoleRepondeur() {
        return roleRepondeur;
    }

    public void setRoleRepondeur(String roleRepondeur) {
        this.roleRepondeur = roleRepondeur;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Timestamp getDateReponse() {
        return dateReponse;
    }

    public void setDateReponse(Timestamp dateReponse) {
        this.dateReponse = dateReponse;
    }
}
