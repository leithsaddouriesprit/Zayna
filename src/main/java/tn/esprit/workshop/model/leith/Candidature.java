package tn.esprit.workshop.model.leith;

import java.time.LocalDateTime;

/**
 * DTO for candidature (one per chauffeur). Used for Suivi and Home button state.
 */
public class Candidature {

    private int id;
    private int chauffeurId;
    private String statut;   // EN_ATTENTE, ACCEPTEE, REFUSEE, ANNULEE
    private LocalDateTime dateEnvoi;
    private String maladie;
    private String nom;      // from chauffeur (for display)
    private String prenom;
    private byte[] permisRecto; // optional preview

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getChauffeurId() { return chauffeurId; }
    public void setChauffeurId(int chauffeurId) { this.chauffeurId = chauffeurId; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public LocalDateTime getDateEnvoi() { return dateEnvoi; }
    public void setDateEnvoi(LocalDateTime dateEnvoi) { this.dateEnvoi = dateEnvoi; }

    public String getMaladie() { return maladie; }
    public void setMaladie(String maladie) { this.maladie = maladie; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public byte[] getPermisRecto() { return permisRecto; }
    public void setPermisRecto(byte[] permisRecto) { this.permisRecto = permisRecto; }
}
