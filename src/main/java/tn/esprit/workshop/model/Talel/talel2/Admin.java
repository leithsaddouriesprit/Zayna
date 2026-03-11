package tn.esprit.workshop.model.Talel.talel2;

public class Admin extends User {

    // Constructeurs
    public Admin() {
        super();
        this.setCategories(CategorieUser.ADMIN);
    }

    public Admin(int id, String nom, String email, String motDePasse) {
        super(id, nom, email, motDePasse, CategorieUser.ADMIN);
    }

}