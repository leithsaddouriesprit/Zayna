package tn.esprit.workshop.model.leith;

public class MaitresseEcoleRow {

    private int maitresseId;
    private int userId;
    private String nom;
    private String prenom;
    private String email;
    private Integer idBus;
    private String busLabel;
    /** Renseigné pour la liste admin. */
    private String ecoleNom;

    public int getMaitresseId() {
        return maitresseId;
    }

    public void setMaitresseId(int maitresseId) {
        this.maitresseId = maitresseId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getEcoleNom() {
        return ecoleNom;
    }

    public void setEcoleNom(String ecoleNom) {
        this.ecoleNom = ecoleNom;
    }
}
