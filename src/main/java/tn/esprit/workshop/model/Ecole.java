package tn.esprit.workshop.model;

public class Ecole {
    private int id;
    private String nom;
    private String position;
    private double prixMensuel;
    private String description;
    private String informations;

    public Ecole() {}

    public Ecole(int id, String nom, String position, double prixMensuel, String description, String informations) {
        this.id = id;
        this.nom = nom;
        this.position = position;
        this.prixMensuel = prixMensuel;
        this.description = description;
        this.informations = informations;
    }

    // Getters
    public int getId() { return id; }
    public String getNom() { return nom; }
    public String getPosition() { return position; }
    public double getPrixMensuel() { return prixMensuel; }
    public String getDescription() { return description; }
    public String getInformations() { return informations; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setNom(String nom) { this.nom = nom; }
    public void setPosition(String position) { this.position = position; }
    public void setPrixMensuel(double prixMensuel) { this.prixMensuel = prixMensuel; }
    public void setDescription(String description) { this.description = description; }
    public void setInformations(String informations) { this.informations = informations; }

    @Override
    public String toString() {
        return nom;
    }
}
