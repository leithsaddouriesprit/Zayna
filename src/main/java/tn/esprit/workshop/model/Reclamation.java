package tn.esprit.workshop.model;

import java.sql.Timestamp;

public class Reclamation {

    private int id;
    private int userId;  // Renommé pour suivre les conventions Java
    private String type;
    private String description;
    private Timestamp dateReclamation;
    private String statut;

    // NOUVEAUX CHAMPS pour les détails spécifiques
    private String chauffeurNom;      // Nom du chauffeur
    private String chauffeurPrenom;   // Prénom du chauffeur
    private String busMatricule;      // Matricule du bus
    private String cantineType;       // Type de problème cantine
    private String ecoleNom;          // Nom de l'école
    private String autrePrecision;    // Précision pour "Autre"

    public Reclamation() {}

    public Reclamation(int id, int userId, String type, String description, String statut) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.description = description;
        this.statut = statut;
    }

    // Constructeur complet avec date
    public Reclamation(int id, int userId, String type, String description,
                       Timestamp dateReclamation, String statut) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.description = description;
        this.dateReclamation = dateReclamation;
        this.statut = statut;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }  // Renommé
    public void setUserId(int userId) { this.userId = userId; }  // Renommé

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Timestamp getDateReclamation() { return dateReclamation; }
    public void setDateReclamation(Timestamp dateReclamation) { this.dateReclamation = dateReclamation; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    // Getters et Setters pour les nouveaux champs
    public String getChauffeurNom() { return chauffeurNom; }
    public void setChauffeurNom(String chauffeurNom) { this.chauffeurNom = chauffeurNom; }

    public String getChauffeurPrenom() { return chauffeurPrenom; }
    public void setChauffeurPrenom(String chauffeurPrenom) { this.chauffeurPrenom = chauffeurPrenom; }

    public String getBusMatricule() { return busMatricule; }
    public void setBusMatricule(String busMatricule) { this.busMatricule = busMatricule; }

    public String getCantineType() { return cantineType; }
    public void setCantineType(String cantineType) { this.cantineType = cantineType; }

    public String getEcoleNom() { return ecoleNom; }
    public void setEcoleNom(String ecoleNom) { this.ecoleNom = ecoleNom; }

    public String getAutrePrecision() { return autrePrecision; }
    public void setAutrePrecision(String autrePrecision) { this.autrePrecision = autrePrecision; }

    // Méthode utilitaire pour afficher un résumé lisible (sans ID)

    @Override
    public String toString() {
        return type + " - " + getDescriptionAvecDetails() + " (" + statut + ")";
    }

    // Nouvelle méthode pour obtenir une description avec détails
    public String getDescriptionAvecDetails() {
        StringBuilder sb = new StringBuilder(description);

        switch (type) {
            case "Chauffeur":
                if (chauffeurNom != null || chauffeurPrenom != null) {
                    sb.append(" [Chauffeur: ").append(chauffeurPrenom).append(" ").append(chauffeurNom).append("]");
                }
                break;
            case "Bus":
                if (busMatricule != null) {
                    sb.append(" [Bus: ").append(busMatricule).append("]");
                }
                break;
            case "Cantine":
                if (cantineType != null) {
                    sb.append(" [Problème: ").append(cantineType).append("]");
                }
                break;
            case "École":
                if (ecoleNom != null) {
                    sb.append(" [École: ").append(ecoleNom).append("]");
                }
                break;
            case "Autre":
                if (autrePrecision != null) {
                    sb.append(" [Précision: ").append(autrePrecision).append("]");
                }
                break;
        }
        return sb.toString();
    }
}
