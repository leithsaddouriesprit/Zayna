package tn.esprit.workshop.model;

public class Programme {

    private int id;
    private int ecoleId;
    private String nomProgramme;
    private String descriptionProgramme;
    private String niveau;
    private String duree;
    private double prixProgramme;

    public Programme() {}

    public Programme(int ecoleId, String nomProgramme, String descriptionProgramme,
                     String niveau, String duree, double prixProgramme) {
        this.ecoleId = ecoleId;
        this.nomProgramme = nomProgramme;
        this.descriptionProgramme = descriptionProgramme;
        this.niveau = niveau;
        this.duree = duree;
        this.prixProgramme = prixProgramme;
    }

    // GETTERS & SETTERS
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEcoleId() { return ecoleId; }
    public void setEcoleId(int ecoleId) { this.ecoleId = ecoleId; }

    public String getNomProgramme() { return nomProgramme; }
    public void setNomProgramme(String nomProgramme) { this.nomProgramme = nomProgramme; }

    public String getDescriptionProgramme() { return descriptionProgramme; }
    public void setDescriptionProgramme(String descriptionProgramme) { this.descriptionProgramme = descriptionProgramme; }

    public String getNiveau() { return niveau; }
    public void setNiveau(String niveau) { this.niveau = niveau; }

    public String getDuree() { return duree; }
    public void setDuree(String duree) { this.duree = duree; }

    public double getPrixProgramme() { return prixProgramme; }
    public void setPrixProgramme(double prixProgramme) { this.prixProgramme = prixProgramme; }
}
