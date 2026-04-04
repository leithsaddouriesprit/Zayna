package tn.esprit.workshop.controlleurs.Talel.admin;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import tn.esprit.workshop.services.admin.AdminDataService;
import tn.esprit.workshop.services.admin.AdminDataService.TrajetRow;
import tn.esprit.workshop.services.leith.BusService;
import tn.esprit.workshop.services.leith.CandidatureEnfantService;
import tn.esprit.workshop.services.leith.CandidatureService;
import tn.esprit.workshop.services.leith.EcoleService;
import tn.esprit.workshop.services.leith.ReclamationService;
import tn.esprit.workshop.model.tous.Ecole;

import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AdminStatsController implements Initializable {

    @FXML private Label lblTopTrajetsSchool;
    @FXML private Label lblTopChildrenSchool;
    @FXML private Label lblTopReclamationsSchool;

    @FXML private TableView<SchoolStatsRow> tableStats;
    @FXML private TableColumn<SchoolStatsRow, String> colEcole;
    @FXML private TableColumn<SchoolStatsRow, Number> colTrajets;
    @FXML private TableColumn<SchoolStatsRow, Number> colEnfants;
    @FXML private TableColumn<SchoolStatsRow, Number> colBuses;
    @FXML private TableColumn<SchoolStatsRow, Number> colPendingChild;
    @FXML private TableColumn<SchoolStatsRow, Number> colAcceptedChauffeurs;
    @FXML private TableColumn<SchoolStatsRow, Number> colReclamations;

    private final AdminDataService adminDataService = new AdminDataService();
    private final EcoleService ecoleService = new EcoleService();
    private final BusService busService = new BusService();
    private final CandidatureEnfantService candidatureEnfantService = new CandidatureEnfantService();
    private final CandidatureService candidatureService = new CandidatureService();
    private final ReclamationService reclamationService = new ReclamationService();

    public static final class SchoolStatsRow {
        private final String ecoleNom;
        private final int trajets;
        private final int enfants;
        private final int buses;
        private final int pendingChild;
        private final int acceptedChauffeurs;
        private final int reclamations;

        public SchoolStatsRow(String ecoleNom, int trajets, int enfants, int buses, int pendingChild, int acceptedChauffeurs, int reclamations) {
            this.ecoleNom = ecoleNom;
            this.trajets = trajets;
            this.enfants = enfants;
            this.buses = buses;
            this.pendingChild = pendingChild;
            this.acceptedChauffeurs = acceptedChauffeurs;
            this.reclamations = reclamations;
        }

        public String getEcoleNom() { return ecoleNom; }
        public int getTrajets() { return trajets; }
        public int getEnfants() { return enfants; }
        public int getBuses() { return buses; }
        public int getPendingChild() { return pendingChild; }
        public int getAcceptedChauffeurs() { return acceptedChauffeurs; }
        public int getReclamations() { return reclamations; }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colEcole.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getEcoleNom()));
        colTrajets.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getTrajets()));
        colEnfants.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getEnfants()));
        colBuses.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getBuses()));
        colPendingChild.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getPendingChild()));
        colAcceptedChauffeurs.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getAcceptedChauffeurs()));
        colReclamations.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getReclamations()));
        loadStats();
    }

    private void loadStats() {
        try {
            List<Ecole> ecoles = ecoleService.selectAll();
            Map<Integer, String> ecoleNames = ecoles.stream()
                    .collect(Collectors.toMap(Ecole::getId, Ecole::getNomEcole));

            List<TrajetRow> trajets = adminDataService.getTrajetsWithDetails(null);

            Map<Integer, Long> trajetsByEcole = trajets.stream()
                    .filter(t -> t.idEcole != 0)
                    .collect(Collectors.groupingBy(t -> t.idEcole, Collectors.counting()));

            Map<Integer, Long> enfantsByEcole = trajets.stream()
                    .filter(t -> t.idEcole != 0)
                    .collect(Collectors.groupingBy(t -> t.idEcole,
                            Collectors.summingLong(t -> t.enfantsCount)));

            Map<Integer, Long> busesByEcole = busService.selectAll().stream()
                    .filter(b -> b.getIdEcole() != null && b.getIdEcole() != 0)
                    .collect(Collectors.groupingBy(b -> b.getIdEcole(), Collectors.counting()));

            Map<Integer, Long> pendingChildByEcole = candidatureEnfantService.findAllEnVoyee().stream()
                    .filter(c -> c.getIdEcole() != 0)
                    .collect(Collectors.groupingBy(c -> c.getIdEcole(), Collectors.counting()));

            Map<Integer, Long> acceptedChauffeursByEcole = candidatureService.findAllAccepteesByEcoleId(null).stream()
                    .filter(c -> c.getIdEcole() != 0)
                    .collect(Collectors.groupingBy(c -> c.getIdEcole(), Collectors.counting()));

            Map<Integer, Integer> reclamationsByEcole = reclamationService.countReclamationsByEcoleIdGlobale();

            List<SchoolStatsRow> rows = new ArrayList<>();
            // Union of all school ids appearing in any stats map
            Set<Integer> ids = Stream.of(trajetsByEcole.keySet(), enfantsByEcole.keySet(),
                            busesByEcole.keySet(), pendingChildByEcole.keySet(), acceptedChauffeursByEcole.keySet(),
                            reclamationsByEcole.keySet())
                    .flatMap(Set::stream)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            for (Integer id : ids) {
                String nom = ecoleNames.get(id);
                if (nom == null || nom.isBlank()) {
                    nom = "École";
                }
                int nbTrajets = trajetsByEcole.getOrDefault(id, 0L).intValue();
                int nbEnfants = enfantsByEcole.getOrDefault(id, 0L).intValue();
                int nbBuses = busesByEcole.getOrDefault(id, 0L).intValue();
                int nbPendingChild = pendingChildByEcole.getOrDefault(id, 0L).intValue();
                int nbAccepted = acceptedChauffeursByEcole.getOrDefault(id, 0L).intValue();
                int nbReclamations = reclamationsByEcole.getOrDefault(id, 0);
                rows.add(new SchoolStatsRow(nom, nbTrajets, nbEnfants, nbBuses, nbPendingChild, nbAccepted, nbReclamations));
            }

            tableStats.getItems().setAll(rows);

            if (lblTopReclamationsSchool != null) {
                ReclamationService.TopEcoleReclamations top = reclamationService.getTopEcoleByReclamationsGlobale();
                if (top == null || top.nombreReclamations <= 0) {
                    lblTopReclamationsSchool.setText("Aucune donnée");
                } else {
                    String nomTop = top.nomEcole != null && !top.nomEcole.isBlank() ? top.nomEcole.trim() : "École";
                    String unit = top.nombreReclamations > 1 ? "réclamations" : "réclamation";
                    lblTopReclamationsSchool.setText(String.format(
                            java.util.Locale.FRANCE, "%s (%d %s)", nomTop, top.nombreReclamations, unit));
                }
            }

            lblTopTrajetsSchool.setText(rows.stream()
                    .max(Comparator.comparingInt(SchoolStatsRow::getTrajets))
                    .map(r -> r.getEcoleNom() + " (" + r.getTrajets() + ")")
                    .orElse("—"));
            lblTopChildrenSchool.setText(rows.stream()
                    .max(Comparator.comparingInt(SchoolStatsRow::getEnfants))
                    .map(r -> r.getEcoleNom() + " (" + r.getEnfants() + ")")
                    .orElse("—"));
        } catch (SQLException e) {
            e.printStackTrace();
            if (lblTopReclamationsSchool != null) {
                lblTopReclamationsSchool.setText("—");
            }
        }
    }
}

