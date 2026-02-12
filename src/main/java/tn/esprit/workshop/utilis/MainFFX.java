package tn.esprit.workshop.utilis;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.esprit.workshop.controlleurs.leith.MapTrackingController;
import tn.esprit.workshop.controlleurs.leith.TrackingMode;

import java.io.IOException;
import java.sql.SQLException;

public class MainFFX extends Application {

    public static void main(String[] args) {
        launch(args);
    }

/*
    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/leith/MapTracking.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.show();
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
*/
  /* @Override
    public void start(Stage primaryStage) {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterPersonne.fxml"));
            Parent root = loader.load(); ///flowmain incor pain scroll pain i dont know what im using in builder
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e){
            System.out.println(e.getMessage());
        }

    }
    */


  @Override
  public void start(Stage primaryStage) {

      try {
          FXMLLoader loader = new FXMLLoader(
                  getClass().getResource("/leith/MapTracking.fxml")
          );

          // if user.role =="parent"

          Parent root = loader.load();

          // récupérer le controller pour init
          MapTrackingController controller = loader.getController();

          // TEST ÉCOLE
          controller.init(1, TrackingMode.ECOLE, 1, 1);

          // (si tu veux tester Parent à la place)
          // controller.init(1, TrackingMode.PARENT, 1);

          Scene scene = new Scene(root, 1100, 700);
          primaryStage.setTitle("Zayna – Bus Tracking");
          primaryStage.setScene(scene);
          primaryStage.show();
      } catch (IOException e) {
          System.out.println(e.getMessage());
      } catch (SQLException e) {
          throw new RuntimeException(e);
      }


  }

}
