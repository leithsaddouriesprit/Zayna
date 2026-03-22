package tn.esprit.workshop.model.leith;

/**
 * Ligne affichée pour l'agent : enfant rattaché à un trajet de son école (périmètre via trajet.id_ecole).
 */
public class AgentEnfantEcoleRow {

    private int enfantId;
    private String nom;
    private String prenom;
    private int trajetId;
    private String trajetNom;
    private Integer idBus;
    private String busLabel;
    private boolean onBoard;
    private boolean actif;

    public int getEnfantId() {
        return enfantId;
    }

    public void setEnfantId(int enfantId) {
        this.enfantId = enfantId;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public int getTrajetId() {
        return trajetId;
    }

    public void setTrajetId(int trajetId) {
        this.trajetId = trajetId;
    }

    public String getTrajetNom() {
        return trajetNom;
    }

    public void setTrajetNom(String trajetNom) {
        this.trajetNom = trajetNom;
    }

    public Integer getIdBus() {
        return idBus;
    }

    public void setIdBus(Integer idBus) {
        this.idBus = idBus;
    }

    public String getBusLabel() {
        return busLabel;
    }

    public void setBusLabel(String busLabel) {
        this.busLabel = busLabel;
    }

    public boolean isOnBoard() {
        return onBoard;
    }

    public void setOnBoard(boolean onBoard) {
        this.onBoard = onBoard;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }
}
