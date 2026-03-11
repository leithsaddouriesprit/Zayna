package tn.esprit.workshop.ai.service;

/**
 * Result of ETA computation (speed-based or OSRM).
 */
public class EtaResult {
    private Integer etaMinutes;
    private Integer etaSeconds;
    private Double distanceToNextStopKm;
    private String nextStopName;

    public EtaResult() {}

    public EtaResult(Integer etaMinutes, Integer etaSeconds, Double distanceToNextStopKm, String nextStopName) {
        this.etaMinutes = etaMinutes;
        this.etaSeconds = etaSeconds;
        this.distanceToNextStopKm = distanceToNextStopKm;
        this.nextStopName = nextStopName;
    }

    public Integer getEtaMinutes() { return etaMinutes; }
    public void setEtaMinutes(Integer etaMinutes) { this.etaMinutes = etaMinutes; }
    public Integer getEtaSeconds() { return etaSeconds; }
    public void setEtaSeconds(Integer etaSeconds) { this.etaSeconds = etaSeconds; }
    public Double getDistanceToNextStopKm() { return distanceToNextStopKm; }
    public void setDistanceToNextStopKm(Double distanceToNextStopKm) { this.distanceToNextStopKm = distanceToNextStopKm; }
    public String getNextStopName() { return nextStopName; }
    public void setNextStopName(String nextStopName) { this.nextStopName = nextStopName; }
}
