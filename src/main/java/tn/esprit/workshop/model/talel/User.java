package tn.esprit.workshop.model.talel;
import java.util.Objects;

public class User {
        private int id;
        private String nom;
        private String email;
        private String password;
        private CategorieUser categories;
    public User(){
    }
        public User(int id, String nom, String email, String password, CategorieUser categorie) {
            this.id =id;
            this.nom = nom;
            this.email = email;
            this.password = password;
            this.categories = categorie;
        }


    public int getId() {return id;}
    public String getNom() { return nom; }
        public String getEmail() { return email; }
        public String getpassword() { return password; }
        public CategorieUser getCategories() { return categories; }

    public void setId(int id) {this.id = id;}
    public void setNom(String nom) {this.nom = nom;}
    public void setpassword(String password) {this.password = password;}
    public void setCategories(CategorieUser categories) {this.categories = categories;}
    public void setEmail(String email) {this.email = email;}

    @Override
        public String toString() {
            return "id:" + id + "Nom: " + nom + ", Email: " + email + ", Catégorie: " + categories;
        }


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof User user)) return false;
        return Objects.equals(nom, user.nom) && Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash( nom, email);
    }
}



