package tn.esprit.workshop.ai.dto;

/**
 * Snapshot of bus tracking data (position, ETA, next stop).
 * All fields nullable when data is missing.
 */
public class TrackingSnapshotDto {
    private String lastUpdateTime;  // ISO-8601
    private Double busLat;
    private Double busLng;
    private Double speedKmh;
    private Double distanceToNextStopKm;
    private Integer etaSeconds;
    private Integer etaMinutes;
    private String nextStopName;
    private Double nextStopLat;
    private Double nextStopLng;
    private Integer routeId;
    private Integer trajetId;

    public TrackingSnapshotDto() {}

    public String getLastUpdateTime() { return lastUpdateTime; }
    public void setLastUpdateTime(String lastUpdateTime) { this.lastUpdateTime = lastUpdateTime; }
    public Double getBusLat() { return busLat; }
    public void setBusLat(Double busLat) { this.busLat = busLat; }
    public Double getBusLng() { return busLng; }
    public void setBusLng(Double busLng) { this.busLng = busLng; }
    public Double getSpeedKmh() { return speedKmh; }
    public void setSpeedKmh(Double speedKmh) { this.speedKmh = speedKmh; }
    public Double getDistanceToNextStopKm() { return distanceToNextStopKm; }
    public void setDistanceToNextStopKm(Double distanceToNextStopKm) { this.distanceToNextStopKm = distanceToNextStopKm; }
    public Integer getEtaSeconds() { return etaSeconds; }
    public void setEtaSeconds(Integer etaSeconds) { this.etaSeconds = etaSeconds; }
    public Integer getEtaMinutes() { return etaMinutes; }
    public void setEtaMinutes(Integer etaMinutes) { this.etaMinutes = etaMinutes; }
    public String getNextStopName() { return nextStopName; }
    public void setNextStopName(String nextStopName) { this.nextStopName = nextStopName; }
    public Double getNextStopLat() { return nextStopLat; }
    public void setNextStopLat(Double nextStopLat) { this.nextStopLat = nextStopLat; }
    public Double getNextStopLng() { return nextStopLng; }
    public void setNextStopLng(Double nextStopLng) { this.nextStopLng = nextStopLng; }
    public Integer getRouteId() { return routeId; }
    public void setRouteId(Integer routeId) { this.routeId = routeId; }
    public Integer getTrajetId() { return trajetId; }
    public void setTrajetId(Integer trajetId) { this.trajetId = trajetId; }
}
