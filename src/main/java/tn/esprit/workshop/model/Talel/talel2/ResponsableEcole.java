package tn.esprit.workshop.model.Talel.talel2;

public class ResponsableEcole extends User {
    private String prenom; // Prénom (agent_ecole) ; users.nom = nom de famille uniquement
    private String titre;
    /** Nom de l’établissement → colonne {@code candidature_agent.ecole}. */
    private String ecole;
    /** Adresse de l’établissement → colonne {@code candidature_agent.adresse}. */
    private String adresseEcole;
    private Double latitude; // Latitude école
    private Double longitude; // Longitude école
    private String telephone;
    private String adresse;
    private double salaire;

    // Constructeurs
    public ResponsableEcole() {
        super();
        this.setCategories(CategorieUser.RESPONSABLEECOLE);
    }

    public ResponsableEcole(int id, String nom, String email, String motDePasse,
                            String titre, String ecole) {
        super(id, nom, email, motDePasse, CategorieUser.RESPONSABLEECOLE);
        this.titre = titre;
        this.ecole = ecole;
    }

    // Getters et Setters
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getEcole() { return ecole; }
    public void setEcole(String ecole) { this.ecole = ecole; }

    public String getAdresseEcole() { return adresseEcole; }
    public void setAdresseEcole(String adresseEcole) { this.adresseEcole = adresseEcole; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }


    public double getSalaire() { return salaire; }
    public void setSalaire(double salaire) { this.salaire = salaire; }
}