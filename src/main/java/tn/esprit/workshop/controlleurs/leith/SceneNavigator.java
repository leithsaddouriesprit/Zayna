package tn.esprit.workshop.controlleurs.leith;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.esprit.workshop.controlleurs.leith.AI.ChatAIController;
import tn.esprit.workshop.utilis.AppSession;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SceneNavigator {

    private static final Logger LOGGER =
            Logger.getLogger(SceneNavigator.class.getName());
    public static void openMap(int busId, TrackingMode mode, Integer enfantId, Integer trajetId) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    SceneNavigator.class.getResource("/leith/MapTracking.fxml")
            );
            Parent root = loader.load();

            MapTrackingController controller = loader.getController();
            controller.init(busId, mode, enfantId, trajetId);

            Stage stage = new Stage();
            stage.setTitle("Zayna - Tracking");
            stage.setScene(new Scene(root, 1000, 700));
            stage.show();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error while opening MapTracking view", e);
        }
    }
    /**
     * Ouvre l'écran Tracking en passant l'id de l'enfant (bus/trajet déduits).
     */
    public static void openTracking(int enfantId) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    SceneNavigator.class.getResource("/leith/MapTracking.fxml")
            );
            Parent root = loader.load();
            MapTrackingController controller = loader.getController();
            controller.init(enfantId);
            Stage stage = new Stage();
            stage.setTitle("Zayna - Tracking");
            stage.setScene(new Scene(root, 1000, 700));
            stage.show();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error while opening Tracking view", e);
        }
    }

    /**
     * Ouvre l'écran Chat AI avec l'enfant sélectionné.
     */
    public static void openChatAI(int enfantId) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    SceneNavigator.class.getResource("/leith/AI/ChatAI.fxml")
            );
            Parent root = loader.load();
            ChatAIController controller = loader.getController();
            controller.init(enfantId);
            Stage stage = new Stage();
            stage.setTitle("Zayna - Assistant IA");
            stage.setScene(new Scene(root, 900, 650));
            stage.show();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error while opening ChatAI view", e);
        }
    }

    /** Ouvre l'accueil Chauffeur (hub). */
    public static void openChauffeurHome() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    SceneNavigator.class.getResource("/leith/ChauffeurHome.fxml")
            );
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Zayna – Espace Chauffeur");
            stage.setScene(new Scene(root, 500, 400));
            stage.show();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error opening ChauffeurHome", e);
        }
    }

    /** Ouvre le formulaire Postuler Chauffeur; onClose runs when the window is closed. */
    public static void openPostulerChauffeur(Runnable onClose) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    SceneNavigator.class.getResource("/leith/PostulerChauffeur.fxml")
            );
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Zayna – Candidature Chauffeur");
            stage.setScene(new Scene(root, 800, 550));
            if (onClose != null) {
                stage.setOnHidden(e -> onClose.run());
            }
            stage.show();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error opening PostulerChauffeur", e);
        }
    }

    /** Ouvre Suivi Candidature; onClose runs when the window is closed. */
    public static void openSuiviCandidature(Runnable onClose) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    SceneNavigator.class.getResource("/leith/SuiviCandidature.fxml")
            );
            Parent root = loader.load();
            SuiviCandidatureController controller = loader.getController();
            controller.init(AppSession.getInstance().getChauffeurId());
            Stage stage = new Stage();
            stage.setTitle("Zayna – Suivi Candidature");
            stage.setScene(new Scene(root, 600, 500));
            if (onClose != null) {
                stage.setOnHidden(e -> onClose.run());
            }
            stage.show();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error opening SuiviCandidature", e);
        }
    }

    /** Ouvre Espace Chauffeur (bus, trajet, arrêts). */
    public static void openEspaceChauffeur() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    SceneNavigator.class.getResource("/leith/EspaceChauffeur.fxml")
            );
            Parent root = loader.load();
            EspaceChauffeurController controller = loader.getController();
            controller.init(AppSession.getInstance().getChauffeurId());
            Stage stage = new Stage();
            stage.setTitle("Zayna – Espace Chauffeur");
            stage.setScene(new Scene(root, 700, 600));
            stage.show();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error opening EspaceChauffeur", e);
        }
    }

}
