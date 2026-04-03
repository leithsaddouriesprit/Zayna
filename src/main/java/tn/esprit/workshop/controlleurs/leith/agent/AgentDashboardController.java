package tn.esprit.workshop.controlleurs.leith.agent;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import tn.esprit.workshop.controlleurs.leith.SceneNavigator;
import tn.esprit.workshop.model.leith.Trajet;
import tn.esprit.workshop.services.leith.ArretService;
import tn.esprit.workshop.services.leith.BusService;
import tn.esprit.workshop.services.leith.CandidatureEnfantService;
import tn.esprit.workshop.services.leith.CandidatureService;
import tn.esprit.workshop.services.leith.EcoleService;
import tn.esprit.workshop.services.leith.EnfantService;
import tn.esprit.workshop.services.leith.MaitresseMetierService;
import tn.esprit.workshop.services.leith.TrajetService;
import tn.esprit.workshop.utilis.AppSession;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AgentDashboardController {

    @FXML private Label lblEcole;
    @FXML private Label lblAgentName;
    @FXML private Label lblSubtitle;
    @FXML private Label lblCandidaturesChauffeur;
    @FXML private Label lblCandidaturesEnfant;
    @FXML private Label lblBus;
    @FXML private Label lblTrajets;
    @FXML private Label lblEnfantsTransportes;
    @FXML private Label lblMaitressesEcole;
    @FXML private Label lblAlertes;
    @FXML private Label lblResume;
    @FXML private VBox boxAlertes;

    @FXML
    public void initialize() {
        Integer idEcole = AppSession.getInstance().getEcoleId();
        String agentName = AppSession.getInstance().getConnectedUserName();
        lblAgentName.setText(agentName != null && !agentName.isEmpty() ? agentName : "Agent École");
        lblSubtitle.setText("Vue d'ensemble de votre école aujourd'hui");

        if (idEcole == null) {
            lblEcole.setText("École : (non définie)");
            setStatsToZero();
            lblAlertes.setText("Aucune donnée disponible pour cette session.");
            lblResume.setText("Connectez-vous en tant qu'agent pour afficher les indicateurs.");
            return;
        }

        lblEcole.setText("Chargement…");
        setStatsToZero();
        lblAlertes.setText("Chargement…");
        lblResume.setText("");

        Task<DashboardSnapshot> task = new Task<>() {
            @Override
            protected DashboardSnapshot call() {
                return buildSnapshot(idEcole);
            }
        };
        task.setOnSucceeded(e -> applySnapshot(task.getValue()));
        task.setOnFailed(e -> Platform.runLater(() -> {
            lblEcole.setText("Erreur de chargement");
            lblAlertes.setText("Impossible de charger les indicateurs.");
            lblResume.setText("");
            setStatsToZero();
        }));
        Thread th = new Thread(task, "agent-dashboard-load");
        th.setDaemon(true);
        th.start();
    }

    private record DashboardSnapshot(
            String ecoleLabel,
            String nbChauffeur,
            String nbEnfant,
            String nbBus,
            String nbTrajets,
            String nbEnfantsTransportes,
            String nbMaitresses,
            String alertes,
            String resume
    ) {}

    private DashboardSnapshot buildSnapshot(int idEcole) {
        String ecoleLabel = "Établissement";
        try {
            EcoleService ecoleService = new EcoleService();
            var ecole = ecoleService.getById(idEcole);
            if (ecole != null && ecole.getNomEcole() != null && !ecole.getNomEcole().isBlank()) {
                ecoleLabel = ecole.getNomEcole();
            }
        } catch (SQLException ignored) {
            // garder libellé par défaut
        }

        String nbCh = "0", nbE = "0", nbB = "0", nbT = "0", nbEnf = "0", nbM = "0";
        try {
            CandidatureService candidatureService = new CandidatureService();
            nbCh = String.valueOf(candidatureService.findAllEnVoyeeByEcoleId(idEcole).size());
        } catch (SQLException ignored) {}
        try {
            CandidatureEnfantService ceService = new CandidatureEnfantService();
            nbE = String.valueOf(ceService.findEnVoyeeByEcoleId(idEcole).size());
        } catch (SQLException ignored) {}
        try {
            BusService busService = new BusService();
            nbB = String.valueOf(busService.selectByEcoleId(idEcole).size());
        } catch (SQLException ignored) {}
        try {
            TrajetService trajetService = new TrajetService();
            nbT = String.valueOf(trajetService.selectByEcoleId(idEcole).size());
        } catch (SQLException ignored) {}
        try {
            EnfantService enfantService = new EnfantService();
            nbEnf = String.valueOf(enfantService.countActifsByEcoleId(idEcole));
        } catch (SQLException ignored) {}
        try {
            MaitresseMetierService maitresseMetierService = new MaitresseMetierService();
            nbM = String.valueOf(maitresseMetierService.listByEcole(idEcole, "").size());
        } catch (SQLException ignored) {}

        String alertes = computeAlertesText(idEcole);
        String resume = computeResumeText(idEcole);

        return new DashboardSnapshot(ecoleLabel, nbCh, nbE, nbB, nbT, nbEnf, nbM, alertes, resume);
    }

    private void applySnapshot(DashboardSnapshot d) {
        if (d == null) {
            return;
        }
        lblEcole.setText(d.ecoleLabel());
        lblCandidaturesChauffeur.setText(d.nbChauffeur());
        lblCandidaturesEnfant.setText(d.nbEnfant());
        lblBus.setText(d.nbBus());
        lblTrajets.setText(d.nbTrajets());
        lblEnfantsTransportes.setText(d.nbEnfantsTransportes());
        if (lblMaitressesEcole != null) {
            lblMaitressesEcole.setText(d.nbMaitresses());
        }
        lblAlertes.setText(d.alertes());
        lblResume.setText(d.resume());
    }

    private String computeAlertesText(int idEcole) {
        List<String> lines = new ArrayList<>();
        try {
            CandidatureService cs = new CandidatureService();
            int n = cs.findAllEnVoyeeByEcoleId(idEcole).size();
            if (n > 0) lines.add("• " + n + " candidature(s) chauffeur à traiter.");
        } catch (SQLException ignored) {}
        try {
            CandidatureEnfantService ces = new CandidatureEnfantService();
            int n = ces.findEnVoyeeByEcoleId(idEcole).size();
            if (n > 0) lines.add("• " + n + " candidature(s) enfant en attente.");
        } catch (SQLException ignored) {}
        try {
            BusService busService = new BusService();
            long sansChauffeur = busService.selectByEcoleId(idEcole).stream()
                    .filter(b -> b.getIdChauffeur() == 0).count();
            if (sansChauffeur > 0) lines.add("• " + sansChauffeur + " bus sans chauffeur affecté.");
        } catch (SQLException ignored) {}
        try {
            TrajetService ts = new TrajetService();
            List<Trajet> trajets = ts.selectByEcoleId(idEcole);
            ArretService arretService = new ArretService();
            int sansArrets = 0;
            for (var t : trajets) {
                if (arretService.getArretsByTrajetOrdered(t.getTrajetId()).isEmpty()) sansArrets++;
            }
            if (sansArrets > 0) lines.add("• " + sansArrets + " trajet(s) sans arrêts.");
            int sansBus = (int) trajets.stream().filter(t -> t.getIdBus() == 0).count();
            if (sansBus > 0) lines.add("• " + sansBus + " trajet(s) sans bus.");
        } catch (SQLException ignored) {}
        if (lines.isEmpty()) {
            return "Aucun point d'attention pour le moment.";
        }
        return String.join(" ", lines);
    }

    private String computeResumeText(int idEcole) {
        List<String> parts = new ArrayList<>();
        try {
            CandidatureService cs = new CandidatureService();
            int acceptes = cs.findAllAccepteesByEcoleId(idEcole).size();
            parts.add(acceptes + " chauffeur(s) accepté(s)");
        } catch (SQLException e) {
            parts.add("0 chauffeur(s) accepté(s)");
        }
        try {
            TrajetService ts = new TrajetService();
            int trajets = ts.selectByEcoleId(idEcole).size();
            parts.add(trajets + " trajet(s)");
        } catch (SQLException e) {
            parts.add("0 trajet(s)");
        }
        try {
            CandidatureEnfantService ces = new CandidatureEnfantService();
            int enAttente = ces.findEnVoyeeByEcoleId(idEcole).size();
            parts.add(enAttente + " candidature(s) enfant en attente");
        } catch (SQLException e) {
            parts.add("0 candidature(s) enfant en attente");
        }
        try {
            BusService busService = new BusService();
            long affectes = busService.selectByEcoleId(idEcole).stream()
                    .filter(b -> b.getIdChauffeur() != 0).count();
            parts.add(affectes + " bus affecté(s)");
        } catch (SQLException e) {
            parts.add("0 bus affecté(s)");
        }
        return String.join(" • ", parts);
    }

    private void setStatsToZero() {
        if (lblCandidaturesChauffeur != null) lblCandidaturesChauffeur.setText("0");
        if (lblCandidaturesEnfant != null) lblCandidaturesEnfant.setText("0");
        if (lblBus != null) lblBus.setText("0");
        if (lblTrajets != null) lblTrajets.setText("0");
        if (lblEnfantsTransportes != null) lblEnfantsTransportes.setText("0");
        if (lblMaitressesEcole != null) lblMaitressesEcole.setText("0");
    }

    @FXML void openCandidaturesChauffeur() { SceneNavigator.openAgentCandidaturesChauffeur(); }
    @FXML void openChauffeursAcceptes() { SceneNavigator.openAgentChauffeursAcceptes(); }
    @FXML void openBus() { SceneNavigator.openAgentBus(); }
    @FXML void openAffecterChauffeurBus() { SceneNavigator.openAgentAffecterChauffeurBus(); }
    @FXML void openTrajets() { SceneNavigator.openAgentTrajets(); }
    @FXML void openAffecterBusTrajet() { SceneNavigator.openAgentAffecterBusTrajet(); }
    @FXML void openArrets() { SceneNavigator.openAgentArrets(); }
    @FXML void openCandidaturesEnfant() { SceneNavigator.openAgentCandidaturesEnfant(); }
}
