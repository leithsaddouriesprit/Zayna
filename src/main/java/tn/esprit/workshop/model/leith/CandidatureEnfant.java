package tn.esprit.workshop.model.leith;

import java.time.LocalDateTime;

/**
 * Candidature transport enfant (parent demande, agent valide).
 * Statuts: ENVOYEE, ACCEPTEE, REFUSEE.
 */
public class CandidatureEnfant {

    private int id;
    private int parentId;
    private int idEcole;
    private String nomEnfant;
    private String prenomEnfant;
    private int age;
    private double latitude;
    private double longitude;
    private String statut;
    private Integer trajetId;
    private LocalDateTime dateDemande;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getParentId() { return parentId; }
    public void setParentId(int parentId) { this.parentId = parentId; }

    public int getIdEcole() { return idEcole; }
    public void setIdEcole(int idEcole) { this.idEcole = idEcole; }

    public String getNomEnfant() { return nomEnfant; }
    public void setNomEnfant(String nomEnfant) { this.nomEnfant = nomEnfant; }

    public String getPrenomEnfant() { return prenomEnfant; }
    public void setPrenomEnfant(String prenomEnfant) { this.prenomEnfant = prenomEnfant; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public Integer getTrajetId() { return trajetId; }
    public void setTrajetId(Integer trajetId) { this.trajetId = trajetId; }

    public LocalDateTime getDateDemande() { return dateDemande; }
    public void setDateDemande(LocalDateTime dateDemande) { this.dateDemande = dateDemande; }
}
