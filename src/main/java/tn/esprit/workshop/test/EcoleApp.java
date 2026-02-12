package tn.esprit.workshop.test;

import javafx.application.Application;
import javafx.stage.Stage;
import tn.esprit.workshop.controlleurs.leith.SceneNavigator;
import tn.esprit.workshop.controlleurs.leith.TrackingMode;

public class EcoleApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        SceneNavigator.openMap(1, TrackingMode.ECOLE, 1, 2);
    }
}
