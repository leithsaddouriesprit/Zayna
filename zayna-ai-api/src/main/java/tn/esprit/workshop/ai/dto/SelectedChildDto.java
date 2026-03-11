package tn.esprit.workshop.ai.dto;

/**
 * Selected child in the tracking context.
 */
public class SelectedChildDto {
    private Integer childId;
    private String nom;
    private String prenom;
    private Boolean onBoard;

    public SelectedChildDto() {}

    public SelectedChildDto(Integer childId, String nom, String prenom, Boolean onBoard) {
        this.childId = childId;
        this.nom = nom;
        this.prenom = prenom;
        this.onBoard = onBoard;
    }

    public Integer getChildId() { return childId; }
    public void setChildId(Integer childId) { this.childId = childId; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public Boolean getOnBoard() { return onBoard; }
    public void setOnBoard(Boolean onBoard) { this.onBoard = onBoard; }
}
