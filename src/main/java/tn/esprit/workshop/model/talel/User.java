package tn.esprit.workshop.model.talel;

import java.util.Objects;

public class User {
        private int id;
        private String nom;
        private String email;
        private String motDePasse;
        private CategorieUser categories;
    public User(){
    }
        public User(int id, String nom, String email, String motDePasse, CategorieUser categorie) {
            this.id =id;
            this.nom = nom;
            this.email = email;
            this.motDePasse = motDePasse;
            this.categories = categorie;
        }




    public int getId() {return id;}
    public String getNom() { return nom; }
        public String getEmail() { return email; }
        public String getMotDePasse() { return motDePasse; }
        public CategorieUser getCategories() { return categories; }

    public void setId(int id) {this.id = id;}
    public void setNom(String nom) {this.nom = nom;}
    public void setMotDePasse(String motDePasse) {this.motDePasse = motDePasse;}
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



