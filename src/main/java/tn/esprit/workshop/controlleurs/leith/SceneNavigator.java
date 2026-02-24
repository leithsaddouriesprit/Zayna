package tn.esprit.workshop.controlleurs.leith;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.esprit.workshop.controlleurs.leith.AI.ChatAIController;

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
    public static void openChatAI(Integer busId, Integer enfantId) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    SceneNavigator.class.getResource("/leith/AI/ChatAI.fxml")
            );
            Parent root = loader.load();

            ChatAIController controller = loader.getController();
            controller.init(busId, enfantId); // <-- on crée init

            Stage stage = new Stage();
            stage.setTitle("Zayna - Assistant IA");
            stage.setScene(new Scene(root, 900, 650));
            stage.show();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error while opening ChatAI view", e);
        }
    }

}
