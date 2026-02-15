package tn.esprit.workshop.model;

public class Agent {
    private int agentId;
    private String nom;
    private String position;
    private double prixMensuel;
    private String description;
    private String informations;

    public Agent() {}

    public Agent(int agent_id, String nom, String position, double prixMensuel, String description, String informations) {
        this.agentId = agent_id;
        this.nom = nom;
        this.position = position;
        this.prixMensuel = prixMensuel;
        this.description = description;
        this.informations = informations;
    }

    public int getAgentId() {
        return agentId;
    }

    public void setAgentId(int id) {
        this.agentId = agentId;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public double getPrixMensuel() {
        return prixMensuel;
    }

    public void setPrixMensuel(double prixMensuel) {
        this.prixMensuel = prixMensuel;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInformations() {
        return informations;
    }

    public void setInformations(String informations) {
        this.informations = informations;
    }
}

