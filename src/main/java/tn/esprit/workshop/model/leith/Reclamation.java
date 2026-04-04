package tn.esprit.workshop.model.leith;

import java.sql.Timestamp;

/**
 * Ligne {@code reclamation} (zaynaa).
 */
public class Reclamation {

    private int id;
    private String objet;
    private String description;
    private String categorie;
    private String priorite;
    private String statut;
    private String roleCreateur;
    private int userId;
    /** Utilisateur responsable (agent/admin) — nullable. */
    private Integer userIdAssigne;
    private String roleAssigne;
    private Timestamp dateAssignation;
    private Integer idParent;
    private Integer idChauffeur;
    private Integer idMaitresse;
    /**
     * École concernée par le ticket lorsqu’elle peut être déterminée à la création (candidature acceptée,
     * bus du chauffeur, session agent/maîtresse, etc.). Nullable si non déductible.
     */
    private Integer idEcole;
    private Integer idBus;
    private Integer idTrajet;
    private Timestamp dateCreation;
    private Timestamp dateModification;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getObjet() {
        return objet;
    }

    public void setObjet(String objet) {
        this.objet = objet;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public String getPriorite() {
        return priorite;
    }

    public void setPriorite(String priorite) {
        this.priorite = priorite;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getRoleCreateur() {
        return roleCreateur;
    }

    public void setRoleCreateur(String roleCreateur) {
        this.roleCreateur = roleCreateur;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Integer getUserIdAssigne() {
        return userIdAssigne;
    }

    public void setUserIdAssigne(Integer userIdAssigne) {
        this.userIdAssigne = userIdAssigne;
    }

    public String getRoleAssigne() {
        return roleAssigne;
    }

    public void setRoleAssigne(String roleAssigne) {
        this.roleAssigne = roleAssigne;
    }

    public Timestamp getDateAssignation() {
        return dateAssignation;
    }

    public void setDateAssignation(Timestamp dateAssignation) {
        this.dateAssignation = dateAssignation;
    }

    public boolean isAssignee() {
        return userIdAssigne != null && userIdAssigne > 0;
    }

    public Integer getIdParent() {
        return idParent;
    }

    public void setIdParent(Integer idParent) {
        this.idParent = idParent;
    }

    public Integer getIdChauffeur() {
        return idChauffeur;
    }

    public void setIdChauffeur(Integer idChauffeur) {
        this.idChauffeur = idChauffeur;
    }

    public Integer getIdMaitresse() {
        return idMaitresse;
    }

    public void setIdMaitresse(Integer idMaitresse) {
        this.idMaitresse = idMaitresse;
    }

    public Integer getIdEcole() {
        return idEcole;
    }

    public void setIdEcole(Integer idEcole) {
        this.idEcole = idEcole;
    }

    public Integer getIdBus() {
        return idBus;
    }

    public void setIdBus(Integer idBus) {
        this.idBus = idBus;
    }

    public Integer getIdTrajet() {
        return idTrajet;
    }

    public void setIdTrajet(Integer idTrajet) {
        this.idTrajet = idTrajet;
    }

    public Timestamp getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Timestamp dateCreation) {
        this.dateCreation = dateCreation;
    }

    public Timestamp getDateModification() {
        return dateModification;
    }

    public void setDateModification(Timestamp dateModification) {
        this.dateModification = dateModification;
    }
}
