package tn.esprit.workshop.model.leith;

public class Enfant {

    private int enfantId;
    private String nom;
    private String prenom;
    private int parentId;
    private int trajetId;
    private boolean actif;
    private boolean onBoard;

    public Enfant() {}

    public Enfant(int enfantId, String nom, String prenom, int parentId, int trajetId, boolean actif) {
        this.enfantId = enfantId;
        this.nom = nom;
        this.prenom = prenom;
        this.parentId = parentId;
        this.trajetId = trajetId;
        this.actif = actif;
    }

    public boolean isOnBoard() {
        return onBoard;
    }

    public void setOnBoard(boolean onBoard) {
        this.onBoard = onBoard;
    }

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

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public int getTrajetId() {
        return trajetId;
    }

    public void setTrajetId(int trajetId) {
        this.trajetId = trajetId;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    @Override
    public String toString() {
        return "Enfant{" +
                "enfantId=" + enfantId +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", parentId=" + parentId +
                ", trajetId=" + trajetId +
                ", actif=" + actif +
                ", onBoard=" + onBoard +
                '}';
    }
}
