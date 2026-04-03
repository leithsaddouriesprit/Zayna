package tn.esprit.workshop.model.amal;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DemandeInscription {
    private int id;
    private String parentNom;
    private String parentPrenom;
    private String parentEmail;
    private String parentTelephone;
    private String enfantNom;
    private String enfantPrenom;
    private LocalDate enfantDateNaissance;
    private String niveauScolaire;
    private int ecoleId;
    private int trajetId;
    private LocalDateTime dateDemande;
    private String statut;
    private String commentaireAgent;
    private LocalDateTime dateTraitement;

    public DemandeInscription() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getParentNom() { return parentNom; }
    public void setParentNom(String parentNom) { this.parentNom = parentNom; }

    public String getParentPrenom() { return parentPrenom; }
    public void setParentPrenom(String parentPrenom) { this.parentPrenom = parentPrenom; }

    public String getParentEmail() { return parentEmail; }
    public void setParentEmail(String parentEmail) { this.parentEmail = parentEmail; }

    public String getParentTelephone() { return parentTelephone; }
    public void setParentTelephone(String parentTelephone) { this.parentTelephone = parentTelephone; }

    public String getEnfantNom() { return enfantNom; }
    public void setEnfantNom(String enfantNom) { this.enfantNom = enfantNom; }

    public String getEnfantPrenom() { return enfantPrenom; }
    public void setEnfantPrenom(String enfantPrenom) { this.enfantPrenom = enfantPrenom; }

    public LocalDate getEnfantDateNaissance() { return enfantDateNaissance; }
    public void setEnfantDateNaissance(LocalDate enfantDateNaissance) { this.enfantDateNaissance = enfantDateNaissance; }

    public String getNiveauScolaire() { return niveauScolaire; }
    public void setNiveauScolaire(String niveauScolaire) { this.niveauScolaire = niveauScolaire; }

    public int getEcoleId() { return ecoleId; }
    public void setEcoleId(int ecoleId) { this.ecoleId = ecoleId; }

    public int getTrajetId() { return trajetId; }
    public void setTrajetId(int trajetId) { this.trajetId = trajetId; }

    public LocalDateTime getDateDemande() { return dateDemande; }
    public void setDateDemande(LocalDateTime dateDemande) { this.dateDemande = dateDemande; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getCommentaireAgent() { return commentaireAgent; }
    public void setCommentaireAgent(String commentaireAgent) { this.commentaireAgent = commentaireAgent; }

    public LocalDateTime getDateTraitement() { return dateTraitement; }
    public void setDateTraitement(LocalDateTime dateTraitement) { this.dateTraitement = dateTraitement; }
}
