package tn.esprit.workshop.model.Talel.talel2;

public class ResponsableEcole extends User {
    private String titre; // "Directeur", "Directrice", "Coordinateur", etc.
    private String ecole;
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
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getEcole() { return ecole; }
    public void setEcole(String ecole) { this.ecole = ecole; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }


    public double getSalaire() { return salaire; }
    public void setSalaire(double salaire) { this.salaire = salaire; }
}