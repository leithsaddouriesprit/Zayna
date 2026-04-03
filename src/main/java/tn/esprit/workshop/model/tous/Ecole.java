package tn.esprit.workshop.model.tous;

import java.util.Objects;

public class Ecole {
    private int id;
    private String nomEcole;
    private String adresse;
    private Double latitude;
    private Double longitude;

public Ecole() {}
    public Ecole(int id, String nomEcole) {
        this.id = id;
        this.nomEcole = nomEcole;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNomEcole() {
        return nomEcole;
    }

    public void setNomEcole(String nomEcole) {
        this.nomEcole = nomEcole;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "Ecole{" +
                "id=" + id +
                ", nomEcole='" + nomEcole + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Ecole ecole)) return false;
        return id == ecole.id && Objects.equals(nomEcole, ecole.nomEcole);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nomEcole);
    }
}
