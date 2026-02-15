
package tn.esprit.workshop.services.talel;



import tn.esprit.workshop.model.talel.CategorieUser;
import tn.esprit.workshop.model.talel.DaoUser;
import tn.esprit.workshop.model.talel.User;
import tn.esprit.workshop.security.JwtUtil;

import java.util.List;

public class UserService {

    private DaoUser daoUser;

    public UserService() {
        this.daoUser = new DaoUser();
    }

    // 🔹 CREATE
    public boolean AddUser(String nom, String email, String motDePasse, CategorieUser categorie) {
        User u = new User();
        u.setNom(nom);
        u.setEmail(email);
        u.setMotDePasse(motDePasse);
        u.setCategories(categorie);

        try {
            DaoUser.CrieerUser(u);
            return true;
        } catch (Exception e) {
            System.out.println("Erreur ajout user : " + e.getMessage());
            return false;
        }
    }

    // 🔹 READ ALL
    public List<User> getAllUsers() {
        return daoUser.getAllUsers();
    }

    // 🔹 READ BY ID
    public User getUserById(int id) {
        return daoUser.getUserById(id);
    }

    // 🔹 UPDATE
    public boolean ModifyUser(int id, String nom, String email, String motDePasse, CategorieUser categorie) {
        User u = new User();
        u.setId(id);
        u.setNom(nom);
        u.setEmail(email);
        u.setMotDePasse(motDePasse);
        u.setCategories(categorie);

        return daoUser.updateUser(u);
    }

    // 🔹 DELETE
    public boolean DeleteUser(int id) {
        return daoUser.deleteUser(id);
    }
    // CONNECTED

    public String connecter(String email, String motDePasse){
        User user = daoUser.connecter(email, motDePasse);

        if(user == null){
            System.out.println("Email ou mot de passe incorrect !");
            return null;
        }

        // Générer JWT
        String token = JwtUtil.generateToken(user);
        return token;
    }
}


