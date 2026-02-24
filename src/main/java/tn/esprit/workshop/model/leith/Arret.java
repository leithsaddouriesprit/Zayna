package tn.esprit.workshop.model.leith;

public class Arret {

    private int arretId;
    private int idTrajet;
    private String nom;
    private double latitude;
    private double longitude;
    private int ordre;
    private String heurePrevue;

    public Arret() {}

    public Arret(int arretId, int idTrajet, String nom,
                 double latitude, double longitude,
                 int ordre, String heurePrevue) {
        this.arretId = arretId;
        this.idTrajet = idTrajet;
        this.nom = nom;
        this.latitude = latitude;
        this.longitude = longitude;
        this.ordre = ordre;
        this.heurePrevue = heurePrevue;
    }

    public int getArretId() {
        return arretId;
    }

    public void setArretId(int arretId) {
        this.arretId = arretId;
    }

    public int getIdTrajet() {
        return idTrajet;
    }

    public void setIdTrajet(int idTrajet) {
        this.idTrajet = idTrajet;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getOrdre() {
        return ordre;
    }

    public void setOrdre(int ordre) {
        this.ordre = ordre;
    }

    public String getHeurePrevue() {
        return heurePrevue;
    }

    public void setHeurePrevue(String heurePrevue) {
        this.heurePrevue = heurePrevue;
    }

    @Override
    public String toString() {
        return "Arret{" +
                "id=" + arretId +
                ", idTrajet=" + idTrajet +
                ", nom='" + nom + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", ordre=" + ordre +
                ", heurePrevue='" + heurePrevue + '\'' +
                '}';
    }
}
