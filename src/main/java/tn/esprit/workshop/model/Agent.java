package tn.esprit.workshop.model;

public class Agent {
    private int agentId;
    private String nom;
    private String position;
    private double prixMensuel;
    private String description;
    private String informations;

    // Constructeur par défaut
    public Agent() {}

    // Constructeur avec paramètres (corrigé)
    public Agent(int agentId, String nom, String position, double prixMensuel, String description, String informations) {
        this.agentId = agentId;  // Correction: agentId au lieu de agent_id
        this.nom = nom;
        this.position = position;
        this.prixMensuel = prixMensuel;
        this.description = description;
        this.informations = informations;
    }

    // Getters
    public int getAgentId() {
        return agentId;
    }

    public String getNom() {
        return nom;
    }

    public String getPosition() {
        return position;
    }

    public double getPrixMensuel() {
        return prixMensuel;
    }

    public String getDescription() {
        return description;
    }

    public String getInformations() {
        return informations;
    }

    // Setters (corrigés)
    public void setAgentId(int agentId) {  // Correction: paramètre agentId au lieu de id
        this.agentId = agentId;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public void setPrixMensuel(double prixMensuel) {
        this.prixMensuel = prixMensuel;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setInformations(String informations) {
        this.informations = informations;
    }

    // Méthode toString() pour l'affichage dans les ComboBox
    @Override
    public String toString() {
        return nom;  // Affiche le nom dans les listes déroulantes
    }
}