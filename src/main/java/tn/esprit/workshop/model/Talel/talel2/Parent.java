package tn.esprit.workshop.model.Talel.talel2;
import java.util.List;
import java.util.ArrayList;

public class Parent extends User {
    private String prenom;
    private String telephone;
    private String adresse;
    private String profession;
    private List<String> enfantsNoms;

    // Constructeurs
    public Parent() {
        super();
        this.setCategories(CategorieUser.PARENT);
        this.enfantsNoms = new ArrayList<>();
    }

    public Parent(int id, String nom, String email, String password,
                  String telephone, String adresse) {
        super(id, nom, email, password, CategorieUser.PARENT);
        this.telephone = telephone;
        this.adresse = adresse;
        this.enfantsNoms = new ArrayList<>();
    }

    // Getters et Setters
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getProfession() { return profession; }
    public void setProfession(String profession) { this.profession = profession; }

    public List<String> getEnfantsNoms() { return enfantsNoms; }
    public void setEnfantsNoms(List<String> enfantsNoms) { this.enfantsNoms = enfantsNoms; }
    public void addEnfant(String nomEnfant) { this.enfantsNoms.add(nomEnfant); }
    }

