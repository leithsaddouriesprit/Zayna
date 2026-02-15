package tn.esprit.workshop.utilis;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;

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
    } */
/* @Override
    public void start(Stage primaryStage) {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GestionReponse.fxml"));
            Parent root = loader.load(); ///flowmain incor pain scroll pain i dont know what im using in builder
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e){
            System.out.println(e.getMessage());
        }

    }
    */

    /*
/// tracking scene
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
          controller.init(1, TrackingMode.PARENT, 1, 1);

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

*/
/*
    /// chauffeur scene
@Override
public void start(Stage primaryStage) {
    try {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/leith/PostulerChauffeur.fxml")
        );

        Parent root = loader.load();

        // récupérer le controller (comme MapTracking)
        PostulerChauffeurController controller = loader.getController();

        // Optionnel : init si tu veux passer un truc (ex: idEcole)
        // controller.init(1);

        Scene scene = new Scene(root, 800, 550);
        primaryStage.setTitle("Zayna – Candidature Chauffeur");
        primaryStage.setScene(scene);
        primaryStage.show();

    } catch (IOException e) {
        System.out.println("Erreur FXML: " + e.getMessage());
        System.out.println(e.getMessage());
    }
}
*/


/*
    /// Chauffeur module (hub + suivi + espace). Set session chauffeur_id then open ChauffeurHome.
    @Override
    public void start(Stage primaryStage) {
        AppSession.getInstance().setChauffeurId(5); // or from login
        SceneNavigator.openChauffeurHome();
    }

*/
/*
    // DEMO START AGENT: ouvre AgentDashboard (avec id école de test en session)
    @Override
    public void start(Stage primaryStage) {
        AppSession.getInstance().setEcoleId(1);
        SceneNavigator.openAgentDashboard();
    }
*/
/*
    // DEMO START PARENT: ouvre ParentDashboard (Mes enfants, Demande transport, Suivi candidatures)
    @Override
    public void start(Stage primaryStage) {
        AppSession.getInstance().setParentId(1); // parent_id de test
        SceneNavigator.openParentDashboard();
    }
*/
/*
/// AI chat
@Override
public void start(Stage primaryStage) {
    try {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/leith/AI/ChatAI.fxml")
        );

        Parent root = loader.load();

        Scene scene = new Scene(root, 900, 650);
        primaryStage.setTitle("Zayna – Assistant IA");
        primaryStage.setScene(scene);
        primaryStage.show();

    } catch (IOException e) {
        System.out.println("Erreur FXML: " + e.getMessage());
        System.out.println(e.getMessage());
    }
}
*/
/*
    /// parent flux
    @Override
    public void start(Stage primaryStage) {
        try {
            // 1) (Optionnel) Simuler un parent connecté pour tester
            AppSession.getInstance().setParentId(1); // parent_id existant dans la table enfant

            // 2) Charger l'écran ParentHome (page principale parent)
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/leith/ParentHome.fxml")
            );
            Parent root = loader.load();

            // 3) Afficher
            Scene scene = new Scene(root, 1000, 700);
            primaryStage.setTitle("ZAYNA – Espace Parent");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            System.out.println("Erreur lancement ParentHome: " + e.getMessage());
        }
    }
*/

    @Override
    public void start(Stage primaryStage) throws Exception {
        showSplashThenLogin(primaryStage);
    }

    private void showSplashThenLogin(Stage primaryStage) {
        try {
            String mediaPath = getClass().getResource("/leith/design/open.mp4").toExternalForm();
            Media media = new Media(mediaPath);
            MediaPlayer mediaPlayer = new MediaPlayer(media);
            MediaView mediaView = new MediaView(mediaPlayer);

            StackPane splashRoot = new StackPane(mediaView);
            splashRoot.setStyle("-fx-background-color: black;");
            Scene splashScene = new Scene(splashRoot, 1000, 650);

            mediaView.fitWidthProperty().bind(splashScene.widthProperty());
            mediaView.fitHeightProperty().bind(splashScene.heightProperty());
            mediaView.setPreserveRatio(true);

            Runnable showLogin = () -> {
                try {
                    showLoginScene(primaryStage);
                } finally {
                    mediaPlayer.dispose();
                }
            };

            mediaPlayer.setOnEndOfMedia(showLogin);
            mediaPlayer.setOnError(showLogin);

            primaryStage.setTitle("Zayna - Connexion");
            primaryStage.setScene(splashScene);
            primaryStage.setResizable(false);
            primaryStage.show();

            mediaPlayer.play();
        } catch (Exception e) {
            showLoginScene(primaryStage);
        }
    }

    private void showLoginScene(Stage primaryStage) {
        Parent root;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Talel/ConnecterUser.fxml"));
            root = loader.load();
        } catch (Exception e) {
            throw new RuntimeException("Unable to load login screen", e);
        }

        primaryStage.setTitle("Zayna - Connexion");
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false);
        primaryStage.show();
    }
/*
    public static void main(String[] args) {
        launch(args);
    }
  */
}
