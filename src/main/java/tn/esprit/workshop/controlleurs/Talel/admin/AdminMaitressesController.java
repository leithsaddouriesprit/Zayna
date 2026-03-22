package tn.esprit.workshop.controlleurs.Talel.admin;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import tn.esprit.workshop.model.Talel.talel2.CategorieUser;
import tn.esprit.workshop.model.Talel.talel2.User;
import tn.esprit.workshop.services.Talel.ServiceAdmin;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AdminMaitressesController implements Initializable {

    @FXML private TableView<User> tableMaitresses;
    @FXML private TableColumn<User, Number> colId;
    @FXML private TableColumn<User, String> colNom;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colEcole;
    @FXML private TableColumn<User, Void> colReclamations;
    @FXML private javafx.scene.control.Label lblTotal;

    private final ServiceAdmin serviceAdmin = new ServiceAdmin();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getId()));
        colNom.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNom() != null ? c.getValue().getNom() : ""));
        colEmail.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getEmail() != null ? c.getValue().getEmail() : ""));
        colEcole.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty("Non associé"));
        colReclamations.setCellFactory(col -> {
            Button btn = new Button("Réclamations");
            btn.setOnAction(e -> showReclamationsPlaceholder());
            return new javafx.scene.control.TableCell<>() {
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : btn);
                }
            };
        });
        loadData();
    }

    private void showReclamationsPlaceholder() {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Réclamations");
        a.setHeaderText(null);
        a.setContentText("Fonctionnalité à venir");
        a.showAndWait();
    }

    private void loadData() {
        try {
            List<User> list = serviceAdmin.rechercherParCategorie(CategorieUser.MAITRESSE);
            tableMaitresses.getItems().setAll(list);
            if (lblTotal != null) lblTotal.setText("Maîtresses: " + list.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
