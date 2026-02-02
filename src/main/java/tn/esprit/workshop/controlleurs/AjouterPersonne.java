package tn.esprit.workshop.controlleurs;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import tn.esprit.workshop.model.Ecole;
import tn.esprit.workshop.model.User;
import tn.esprit.workshop.services.UserService;

import java.sql.SQLException;

public class AjouterPersonne {

    @FXML
    private TextField Email;

    @FXML
    private TextField NomPrenom;

    @FXML
    private TextField Robot;

    @FXML
    private Button login;

    @FXML
    void enter(ActionEvent event) {

    }

    UserService userService = new UserService();
    @FXML
    void ajouterPersonne(ActionEvent event) throws SQLException {
        User Sinda = new User();

        String email = Email.getText().toString();
        String nomPrenom = NomPrenom.getText().toString();
        String robot = Robot.getText().toString();

        Sinda.setNom(nomPrenom);

        userService.insertOne(new User(1,"Sinda","SindaOO",20));

    }

}
