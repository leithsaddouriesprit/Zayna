package tn.esprit.workshop.controlleurs;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import tn.esprit.workshop.model.User;
import tn.esprit.workshop.services.UserService;

import java.sql.SQLException;
import java.util.List;

public class AfficherPersonne {
/// jdjdjjdodj
    @FXML
    private Label LabelDisplay;

    UserService userService = new UserService();

    void initialize() throws SQLException {
        List<User> users = userService.selectAll();
        LabelDisplay.setText(users.get(0).getNom());

    }
}
