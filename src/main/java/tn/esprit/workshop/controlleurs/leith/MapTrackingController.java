package tn.esprit.workshop.controlleurs.leith;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import tn.esprit.workshop.model.leith.PositionBus;
import tn.esprit.workshop.services.leith.PositionBusService;

public class MapTrackingController {

    @FXML
    private WebView mapView;

    private WebEngine engine;
    private Timeline timeline;

    // Pour le test : on suit le bus id=1 (on changera plus tard via enfant/parent)
    private final int BUS_ID = 1;

    private final PositionBusService positionBusService = new PositionBusService();

    @FXML
    public void initialize() {
        engine = mapView.getEngine();
        engine.load(getClass().getResource("/map/map.html").toExternalForm());

        startAutoRefresh();
    }

    private void startAutoRefresh() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(2), event -> refreshPosition()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void refreshPosition() {
        try {
            PositionBus last = positionBusService.getLastPosition(BUS_ID);
            if (last != null) {
                double lat = last.getLatitude();
                double lng = last.getLongitude();

                // Mise à jour marker (JS)
                engine.executeScript("updateBusPosition(" + lat + "," + lng + ");");
                engine.executeScript("panToBus(" + lat + "," + lng + ");");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Optionnel : arrêter le refresh si tu changes de scène
    public void stop() {
        if (timeline != null) timeline.stop();
    }
}
