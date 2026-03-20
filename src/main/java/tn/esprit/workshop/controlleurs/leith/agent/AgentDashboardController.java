package tn.esprit.workshop.controlleurs.leith.agent;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import tn.esprit.workshop.controlleurs.leith.SceneNavigator;
import tn.esprit.workshop.services.leith.ArretService;
import tn.esprit.workshop.services.leith.BusService;
import tn.esprit.workshop.services.leith.CandidatureEnfantService;
import tn.esprit.workshop.services.leith.CandidatureService;
import tn.esprit.workshop.services.leith.EcoleService;
import tn.esprit.workshop.services.leith.EnfantService;
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

        try {
            EcoleService ecoleService = new EcoleService();
            var ecole = ecoleService.getById(idEcole);
            lblEcole.setText(ecole != null && ecole.getNomEcole() != null ? ecole.getNomEcole() : "École id=" + idEcole);
        } catch (SQLException e) {
            lblEcole.setText("École id=" + idEcole);
        }

        loadStats(idEcole);
        loadAlertes(idEcole);
        loadResume(idEcole);
    }

    private void setStatsToZero() {
        if (lblCandidaturesChauffeur != null) lblCandidaturesChauffeur.setText("0");
        if (lblCandidaturesEnfant != null) lblCandidaturesEnfant.setText("0");
        if (lblBus != null) lblBus.setText("0");
        if (lblTrajets != null) lblTrajets.setText("0");
        if (lblEnfantsTransportes != null) lblEnfantsTransportes.setText("0");
    }

    private void loadStats(int idEcole) {
        try {
            CandidatureService candidatureService = new CandidatureService();
            int nbChauffeur = candidatureService.findAllEnVoyeeByEcoleId(idEcole).size();
            lblCandidaturesChauffeur.setText(String.valueOf(nbChauffeur));
        } catch (SQLException e) {
            lblCandidaturesChauffeur.setText("0");
        }
        try {
            CandidatureEnfantService ceService = new CandidatureEnfantService();
            int nbEnfant = ceService.findEnVoyeeByEcoleId(idEcole).size();
            lblCandidaturesEnfant.setText(String.valueOf(nbEnfant));
        } catch (SQLException e) {
            lblCandidaturesEnfant.setText("0");
        }
        try {
            BusService busService = new BusService();
            int nbBus = busService.selectByEcoleId(idEcole).size();
            lblBus.setText(String.valueOf(nbBus));
        } catch (SQLException e) {
            lblBus.setText("0");
        }
        try {
            TrajetService trajetService = new TrajetService();
            int nbTrajets = trajetService.selectByEcoleId(idEcole).size();
            lblTrajets.setText(String.valueOf(nbTrajets));
        } catch (SQLException e) {
            lblTrajets.setText("0");
        }
        try {
            EnfantService enfantService = new EnfantService();
            int nbEnfants = enfantService.countActifsByEcoleId(idEcole);
            lblEnfantsTransportes.setText(String.valueOf(nbEnfants));
        } catch (SQLException e) {
            lblEnfantsTransportes.setText("0");
        }
    }

    private void loadAlertes(int idEcole) {
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
            List<tn.esprit.workshop.model.leith.Trajet> trajets = ts.selectByEcoleId(idEcole);
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
            lblAlertes.setText("Aucun point d'attention pour le moment.");
        } else {
            lblAlertes.setText(String.join(" ", lines));
        }
    }

    private void loadResume(int idEcole) {
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
        lblResume.setText(String.join(" • ", parts));
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
