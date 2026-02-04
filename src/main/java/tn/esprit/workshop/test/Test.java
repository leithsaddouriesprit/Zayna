package tn.esprit.workshop.test;
import tn.esprit.workshop.services.UserService;
import tn.esprit.workshop.utilis.MyBDConnexion;


public class Test {
public static void main(String[] args)  {
    MyBDConnexion c = new MyBDConnexion();
    UserService userService = new UserService();
    // INSERT
    /*User u = new User();
    u.setNom("Ali");
    u.setPrenom("Ben Ali");
    u.setAge(20);

    try {
        userService.insertOne(u);
    } catch (SQLException e) {
        System.out.println(e.getMessage());
    }

    User u1 = new User();
    u.setNom("mohsen");
    u.setPrenom("Ben Hassen");
    u.setAge(20);

    try {
        userService.insertOne(u);
    } catch (SQLException e) {
        System.out.println(e.getMessage());
    }*/
    /*// UPDATE
    User u2 = new User();
    u2.setId(1);                // ID EXISTANT
    u2.setNom("Ali Updated");
    u2.setPrenom("Ben Ali");
    u2.setAge(25);

    try {
        userService.updateOne(u2);
    } catch (SQLException e) {
        System.out.println(e.getMessage());
    }*/
   /* // DELETE
    User u3 = new User();
    u3.setId(1); // id à supprimer

    try {
        userService.deleteOne(u3);
    } catch (SQLException e) {
        System.out.println(e.getMessage());
    }*/

/*// SELECT ALL
    try {
        List<User> users = userService.selectAll();

        for (User u : users) {
            System.out.println(
                    u.getId() + " | " +
                            u.getNom() + " | " +
                            u.getPrenom() + " | " +
                            u.getAge()
            );
        }

    } catch (SQLException e) {
        System.out.println(e.getMessage());
    }
*/

}
}
