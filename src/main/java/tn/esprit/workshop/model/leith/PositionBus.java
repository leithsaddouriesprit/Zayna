package tn.esprit.workshop.model.leith;

import java.time.LocalDateTime;

public class PositionBus {

    private int id;
    private int idBus;
    private double latitude;
    private double longitude;
    private double vitesse;
    private LocalDateTime timestamp;

    public PositionBus() {}

    public PositionBus(int id, int idBus, double latitude,
                       double longitude, double vitesse,
                       LocalDateTime timestamp) {
        this.id = id;
        this.idBus = idBus;
        this.latitude = latitude;
        this.longitude = longitude;
        this.vitesse = vitesse;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdBus() {
        return idBus;
    }

    public void setIdBus(int idBus) {
        this.idBus = idBus;
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

    public double getVitesse() {
        return vitesse;
    }

    public void setVitesse(double vitesse) {
        this.vitesse = vitesse;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "PositionBus{" +
                "id=" + id +
                ", idBus=" + idBus +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", vitesse=" + vitesse +
                ", timestamp=" + timestamp +
                '}';
    }
}

