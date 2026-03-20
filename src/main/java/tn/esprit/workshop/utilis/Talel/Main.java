package tn.esprit.workshop.utilis.Talel;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;

public class Main extends Application {

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
                } catch (Exception e) {
                    throw new RuntimeException(e);
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
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Talel/ConnecterUser.fxml"));
        Parent root;
        try {
            root = loader.load();
        } catch (Exception e) {
            throw new RuntimeException("Unable to load login screen", e);
        }

        primaryStage.setTitle("Zayna - Connexion");
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}