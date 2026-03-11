package tn.esprit.workshop.ai.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Data quality flags for the tracking context.
 */
public class DataQualityDto {
    private Boolean isTestMode;   // true if busId=1 default or missing real ids
    private Boolean isStale;     // e.g. lastUpdate > 30s/60s
    private List<String> missingFields = new ArrayList<>();

    public DataQualityDto() {}

    public Boolean getIsTestMode() { return isTestMode; }
    public void setIsTestMode(Boolean testMode) { isTestMode = testMode; }
    public Boolean getIsStale() { return isStale; }
    public void setIsStale(Boolean stale) { isStale = stale; }
    public List<String> getMissingFields() { return missingFields; }
    public void setMissingFields(List<String> missingFields) { this.missingFields = missingFields != null ? missingFields : new ArrayList<>(); }
}
