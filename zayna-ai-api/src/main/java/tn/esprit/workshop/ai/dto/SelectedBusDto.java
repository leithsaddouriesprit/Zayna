package tn.esprit.workshop.ai.dto;

/**
 * Selected bus in the tracking context.
 */
public class SelectedBusDto {
    private Integer busId;
    private String plateNumber;

    public SelectedBusDto() {}

    public SelectedBusDto(Integer busId, String plateNumber) {
        this.busId = busId;
        this.plateNumber = plateNumber;
    }

    public Integer getBusId() { return busId; }
    public void setBusId(Integer busId) { this.busId = busId; }
    public String getPlateNumber() { return plateNumber; }
    public void setPlateNumber(String plateNumber) { this.plateNumber = plateNumber; }
}
