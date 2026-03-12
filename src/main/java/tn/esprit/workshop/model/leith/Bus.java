package tn.esprit.workshop.model.leith;

public class Bus {

    private int busId;
    private String numeroBus;
    private String matricule;
    private int capacite;
    private int idChauffeur;
    private boolean actif;
    /** École propriétaire (filtrage agent). */
    private Integer idEcole;

    public Bus() {}

    public Bus(int busId, String numeroBus, String matricule, int capacite, int idChauffeur, boolean actif) {
        this.busId = busId;
        this.numeroBus = numeroBus;
        this.matricule = matricule;
        this.capacite = capacite;
        this.idChauffeur = idChauffeur;
        this.actif = actif;
    }

    public int getBusId() {
        return busId;
    }

    public void setBusId(int busId) {
        this.busId = busId;
    }

    public String getNumeroBus() {
        return numeroBus;
    }

    public void setNumeroBus(String numeroBus) {
        this.numeroBus = numeroBus;
    }

    public String getMatricule() {
        return matricule;
    }

    public void setMatricule(String matricule) {
        this.matricule = matricule;
    }

    public int getCapacite() {
        return capacite;
    }

    public void setCapacite(int capacite) {
        this.capacite = capacite;
    }

    public int getIdChauffeur() {
        return idChauffeur;
    }

    public void setIdChauffeur(int idChauffeur) {
        this.idChauffeur = idChauffeur;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public Integer getIdEcole() {
        return idEcole;
    }

    public void setIdEcole(Integer idEcole) {
        this.idEcole = idEcole;
    }

    @Override
    public String toString() {
        return "Bus{" +
                "busId=" + busId +
                ", numeroBus='" + numeroBus + '\'' +
                ", matricule='" + matricule + '\'' +
                ", capacite=" + capacite +
                ", idChauffeur=" + idChauffeur +
                ", actif=" + actif +
                '}';
    }
}

