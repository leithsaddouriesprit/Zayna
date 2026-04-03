package tn.esprit.workshop.controlleurs.amal;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import tn.esprit.workshop.controlleurs.leith.SceneNavigator;
import tn.esprit.workshop.model.leith.Arret;
import tn.esprit.workshop.model.leith.Trajet;
import tn.esprit.workshop.services.leith.ArretService;
import tn.esprit.workshop.services.leith.TrajetService;
import tn.esprit.workshop.utilis.AppSession;

import java.sql.SQLException;
import java.util.List;

public class ParentTrajetsController {

    @FXML private Label lblTitle;
    @FXML private VBox trajetsContainer;
    private final TrajetService trajetService = new TrajetService();
    private final ArretService arretService = new ArretService();

    @FXML
    public void initialize() {
        Integer ecoleId = AppSession.getInstance().getSelectedEcoleId();
        String ecoleName = AppSession.getInstance().getSelectedEcoleName();
        lblTitle.setText("Trajets – " + (ecoleName.isBlank() ? "établissement" : ecoleName));
        if (ecoleId == null) {
            addInfo("Aucune école n'est sélectionnée. Retournez au choix d'établissement.");
            return;
        }
        loadTrajets(ecoleId);
    }

    private void loadTrajets(int ecoleId) {
        trajetsContainer.getChildren().clear();
        try {
            List<Trajet> trajets = trajetService.selectByEcoleId(ecoleId);
            if (trajets.isEmpty()) {
                addInfo("Aucun trajet n'est encore défini pour cet établissement.");
                return;
            }
            for (Trajet trajet : trajets) {
                VBox card = new VBox(6);
                card.getStyleClass().addAll("card-dark", "amal-trajet-card");
                String h = trajet.getHeureDepart() != null ? trajet.getHeureDepart().toString() : "--:--";
                Label nom = new Label(trajet.getNom());
                nom.getStyleClass().add("card-title");
                Label heure = new Label("Heure depart: " + h);
                Label prix = new Label(String.format("Prix: %.2f DT", trajet.getPrix()));
                Label statut = new Label("Statut: " + (trajet.getStatut() == null ? "-" : trajet.getStatut()));
                heure.getStyleClass().add("section-subtitle");
                prix.getStyleClass().add("section-subtitle");
                statut.getStyleClass().add("section-subtitle");
                card.getChildren().addAll(nom, heure, prix, statut);

                List<Arret> arrets = arretService.getByTrajetId(trajet.getTrajetId());
                if (arrets.isEmpty()) {
                    Label none = new Label("Aucun arrêt enregistré pour ce trajet.");
                    none.getStyleClass().add("section-subtitle");
                    card.getChildren().add(none);
                } else {
                    for (Arret arret : arrets) {
                        Label stop = new Label("Arrêt " + arret.getOrdre() + " — " + arret.getNom());
                        stop.getStyleClass().add("section-subtitle");
                        card.getChildren().add(stop);
                    }
                }
                trajetsContainer.getChildren().add(card);
            }
        } catch (SQLException e) {
            addInfo("Impossible de charger les trajets. Réessayez plus tard.");
        }
    }

    private void addInfo(String text) {
        Label info = new Label(text);
        info.getStyleClass().add("section-subtitle");
        trajetsContainer.getChildren().add(info);
    }

    @FXML
    private void backToParentInterface() {
        SceneNavigator.openParentInterface();
    }
}
