package tn.esprit.workshop.controlleurs.leith;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SceneNavigator {

    private static final Logger LOGGER =
            Logger.getLogger(SceneNavigator.class.getName());
    public static void openMap(int busId, TrackingMode mode, Integer enfantId) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    SceneNavigator.class.getResource("/leith/map/MapTracking.fxml")
            );
            Parent root = loader.load();

            MapTrackingController controller = loader.getController();
            controller.init(busId, mode, enfantId);

            Stage stage = new Stage();
            stage.setTitle("Zayna - Tracking");
            stage.setScene(new Scene(root, 1000, 700));
            stage.show();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error while opening MapTracking view", e);
        }
    }

}
