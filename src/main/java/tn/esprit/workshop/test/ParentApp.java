package tn.esprit.workshop.test;

import javafx.application.Application;
import javafx.stage.Stage;
import tn.esprit.workshop.controlleurs.leith.SceneNavigator;
import tn.esprit.workshop.controlleurs.leith.TrackingMode;

public class ParentApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        // enfantId = 1 (doit exister et avoir trajet_id)
        SceneNavigator.openMap(1, TrackingMode.PARENT, 1, 2);
    }
}

