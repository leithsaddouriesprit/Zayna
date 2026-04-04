package tn.esprit.workshop.model.leith;

import java.sql.Timestamp;

/**
 * Ligne {@code reclamation_historique}.
 */
public class ReclamationHistorique {

    private int id;
    private int reclamationId;
    private String actionType;
    private String ancienStatut;
    private String nouveauStatut;
    private Integer ancienAssigne;
    private Integer nouveauAssigne;
    private String messageAction;
    private int userIdAction;
    private String roleAction;
    private Timestamp dateAction;

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

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getAncienStatut() {
        return ancienStatut;
    }

    public void setAncienStatut(String ancienStatut) {
        this.ancienStatut = ancienStatut;
    }

    public String getNouveauStatut() {
        return nouveauStatut;
    }

    public void setNouveauStatut(String nouveauStatut) {
        this.nouveauStatut = nouveauStatut;
    }

    public Integer getAncienAssigne() {
        return ancienAssigne;
    }

    public void setAncienAssigne(Integer ancienAssigne) {
        this.ancienAssigne = ancienAssigne;
    }

    public Integer getNouveauAssigne() {
        return nouveauAssigne;
    }

    public void setNouveauAssigne(Integer nouveauAssigne) {
        this.nouveauAssigne = nouveauAssigne;
    }

    public String getMessageAction() {
        return messageAction;
    }

    public void setMessageAction(String messageAction) {
        this.messageAction = messageAction;
    }

    public int getUserIdAction() {
        return userIdAction;
    }

    public void setUserIdAction(int userIdAction) {
        this.userIdAction = userIdAction;
    }

    public String getRoleAction() {
        return roleAction;
    }

    public void setRoleAction(String roleAction) {
        this.roleAction = roleAction;
    }

    public Timestamp getDateAction() {
        return dateAction;
    }

    public void setDateAction(Timestamp dateAction) {
        this.dateAction = dateAction;
    }
}
