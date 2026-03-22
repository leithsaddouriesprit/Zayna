package tn.esprit.workshop.controlleurs.Talel.admin;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import tn.esprit.workshop.model.Talel.talel2.CategorieUser;
import tn.esprit.workshop.services.Talel.ServiceAdmin;
import tn.esprit.workshop.services.leith.*;

import java.net.URL;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.ResourceBundle;

public class AdminHomeController implements Initializable {

    @FXML private Label lblTotalUsers;
    @FXML private Label lblTotalParents;
    @FXML private Label lblTotalChauffeurs;
    @FXML private Label lblTotalAgents;
    @FXML private Label lblTotalTrajets;
    @FXML private Label lblTotalBuses;
    @FXML private Label lblTotalChildren;
    @FXML private Label lblPendingChild;
    @FXML private Label lblPendingChauffeur;
    @FXML private Label lblSchools;
    @FXML private Label lblAssignedChauffeurs;

    private final ServiceAdmin serviceAdmin = new ServiceAdmin();
    private final TrajetService trajetService = new TrajetService();
    private final BusService busService = new BusService();
    private final EnfantService enfantService = new EnfantService();
    private final CandidatureEnfantService candidatureEnfantService = new CandidatureEnfantService();
    private final CandidatureService candidatureService = new CandidatureService();
    private final EcoleService ecoleService = new EcoleService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadStats();
    }

    private void loadStats() {
        try {
            int totalUsers = serviceAdmin.getAllUtilisateurs().size();
            long parents = serviceAdmin.countByCategorie(CategorieUser.PARENT);
            long chauffeurs = serviceAdmin.countByCategorie(CategorieUser.CHAUFFEUR);
            long agents = serviceAdmin.countByCategorie(CategorieUser.RESPONSABLEECOLE);

            int trajets = 0;
            try { trajets = trajetService.selectAll().size(); } catch (SQLException ignored) {}

            int buses = 0;
            int assignedChauffeurs = 0;
            try {
                var busesList = busService.selectAll();
                buses = busesList.size();
                HashSet<Integer> assigned = new HashSet<>();
                for (var b : busesList) {
                    if (b.getIdChauffeur() != 0) {
                        assigned.add(b.getIdChauffeur());
                    }
                }
                assignedChauffeurs = assigned.size();
            } catch (SQLException ignored) {}

            int children = 0;
            try { children = enfantService.selectAll().size(); } catch (SQLException ignored) {}

            int pendingChild = 0;
            try { pendingChild = candidatureEnfantService.findAllEnVoyee().size(); } catch (SQLException ignored) {}

            int pendingChauffeur = 0;
            try { pendingChauffeur = candidatureService.findAllEnVoyee().size(); } catch (SQLException ignored) {}

            int schools = 0;
            try { schools = ecoleService.selectAll().size(); } catch (SQLException ignored) {}

            if (lblTotalUsers != null) lblTotalUsers.setText(String.valueOf(totalUsers));
            if (lblTotalParents != null) lblTotalParents.setText(String.valueOf(parents));
            if (lblTotalChauffeurs != null) lblTotalChauffeurs.setText(String.valueOf(chauffeurs));
            if (lblTotalAgents != null) lblTotalAgents.setText(String.valueOf(agents));
            if (lblTotalTrajets != null) lblTotalTrajets.setText(String.valueOf(trajets));
            if (lblTotalBuses != null) lblTotalBuses.setText(String.valueOf(buses));
            if (lblTotalChildren != null) lblTotalChildren.setText(String.valueOf(children));
            if (lblPendingChild != null) lblPendingChild.setText(String.valueOf(pendingChild));
            if (lblPendingChauffeur != null) lblPendingChauffeur.setText(String.valueOf(pendingChauffeur));
            if (lblSchools != null) lblSchools.setText(String.valueOf(schools));
            if (lblAssignedChauffeurs != null) lblAssignedChauffeurs.setText(String.valueOf(assignedChauffeurs));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

