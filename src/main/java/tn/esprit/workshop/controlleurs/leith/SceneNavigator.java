package tn.esprit.workshop.controlleurs.leith;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import tn.esprit.workshop.controlleurs.leith.AI.ChatAIController;
import tn.esprit.workshop.utilis.AppSession;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SceneNavigator {

    private static final Logger LOGGER =
            Logger.getLogger(SceneNavigator.class.getName());

    /** URL of app.css for use in popup scenes (e.g. ComboBox dropdown). */
    public static String getAppCssUrl() {
        URL cssUrl = SceneNavigator.class.getResource("/leith/app.css");
        if (cssUrl == null) cssUrl = ClassLoader.getSystemResource("leith/app.css");
        if (cssUrl == null) {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if (cl != null) cssUrl = cl.getResource("leith/app.css");
        }
        return cssUrl != null ? cssUrl.toExternalForm() : null;
    }

    /**
     * Applies app.css to this ComboBox's dropdown popup when it is shown.
     * Call this in initialize() for each ComboBox so the dropdown uses the dark theme.
     */
    public static void applyAppCssToComboBoxPopup(ComboBox<?> combo) {
        if (combo == null) return;
        String url = getAppCssUrl();
        if (url == null) return;
        combo.showingProperty().addListener((obs, wasShowing, nowShowing) -> {
            if (!nowShowing) return;
            javafx.application.Platform.runLater(() -> {
                try {
                    Object skin = combo.getSkin();
                    if (skin == null) return;
                    java.lang.reflect.Method getPopup = skin.getClass().getMethod("getPopup");
                    Object popupContent = getPopup.invoke(skin);
                    if (popupContent instanceof javafx.scene.Node) {
                        javafx.scene.Scene sc = ((javafx.scene.Node) popupContent).getScene();
                        if (sc != null && !sc.getStylesheets().contains(url)) {
                            sc.getStylesheets().add(0, url);
                        }
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.FINE, "Could not apply CSS to ComboBox popup", e);
                }
            });
        });
    }

    /** Applique app.css à la scène et styleClass "root" au nœud racine (palette sombre unique). */
    private static void applyAppCss(javafx.scene.Scene scene, Parent root) {
        java.net.URL cssUrl = SceneNavigator.class.getResource("/leith/app.css");
        if (cssUrl == null) {
            cssUrl = ClassLoader.getSystemResource("leith/app.css");
        }
        if (cssUrl == null) {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if (cl != null) cssUrl = cl.getResource("leith/app.css");
        }
        if (cssUrl == null) {
            LOGGER.severe("app.css introuvable (vérifier src/main/resources/leith/app.css)");
        } else {
            String url = cssUrl.toExternalForm();
            if (!scene.getStylesheets().contains(url)) {
                scene.getStylesheets().add(0, url);
            }
            if (root != null) {
                if (!root.getStylesheets().contains(url)) {
                    root.getStylesheets().add(0, url);
                }
                if (!root.getStyleClass().contains("root")) {
                    root.getStyleClass().add(0, "root");
                }
            }
        }
        scene.setFill(Color.web("#0F172A"));
    }

    public static void openMap(int busId, TrackingMode mode, Integer enfantId, Integer trajetId) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    SceneNavigator.class.getResource("/leith/MapTracking.fxml")
            );
            Parent root = loader.load();
            MapTrackingController controller = loader.getController();
            if (controller == null) {
                LOGGER.severe("MapTracking: loader.getController() is null");
                return;
            }
            try {
                controller.init(busId, mode, enfantId, trajetId);
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "MapTracking init failed", ex);
            }

            Stage stage = new Stage();
            stage.setOnCloseRequest(e -> controller.stopTracking());
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 1000, 700);
            applyAppCss(scene, root);
            stage.setTitle("Zayna - Tracking");
            stage.setScene(scene);
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
            if (controller == null) {
                LOGGER.severe("Tracking: loader.getController() is null");
                return;
            }
            try {
                controller.init(enfantId);
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Tracking init failed", ex);
            }

            Stage stage = new Stage();
            stage.setOnCloseRequest(e -> controller.stopTracking());
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 1000, 700);
            applyAppCss(scene, root);
            stage.setTitle("Zayna - Tracking");
            stage.setScene(scene);
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
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 900, 650);
            applyAppCss(scene, root);
            stage.setTitle("Zayna - Assistant IA");
            stage.setScene(scene);
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
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 500, 400);
            applyAppCss(scene, root);
            stage.setTitle("Zayna – Espace Chauffeur");
            stage.setScene(scene);
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
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 800, 550);
            applyAppCss(scene, root);
            stage.setTitle("Zayna – Candidature Chauffeur");
            stage.setScene(scene);
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
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 600, 500);
            applyAppCss(scene, root);
            stage.setTitle("Zayna – Suivi Candidature");
            stage.setScene(scene);
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
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 700, 600);
            applyAppCss(scene, root);
            stage.setTitle("Zayna – Espace Chauffeur");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error opening EspaceChauffeur", e);
        }
    }

    // ---------- Flux Agent ----------
    public static void openAgentDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    SceneNavigator.class.getResource("/leith/agent/AgentDashboard.fxml")
            );
            Parent root = loader.load();
            Stage stage = new Stage();
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 500, 520);
            applyAppCss(scene, root);
            stage.setTitle("Zayna – Tableau de bord Agent");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error opening AgentDashboard", e);
        }
    }

    public static void openAgentCandidaturesChauffeur() {
        openAgentStage("/leith/agent/AgentCandidaturesChauffeur.fxml", "Candidatures Chauffeur", 850, 450);
    }

    public static void openAgentChauffeursAcceptes() {
        openAgentStage("/leith/agent/AgentChauffeursAcceptes.fxml", "Chauffeurs acceptés", 550, 400);
    }

    public static void openAgentBus() {
        openAgentStage("/leith/agent/AgentBus.fxml", "Gestion Bus", 550, 450);
    }

    public static void openAgentAffecterChauffeurBus() {
        openAgentStage("/leith/agent/AgentAffecterChauffeurBus.fxml", "Affecter Chauffeur → Bus", 650, 220);
    }

    public static void openAgentTrajets() {
        openAgentStage("/leith/agent/AgentTrajets.fxml", "Gestion Trajets", 550, 450);
    }

    public static void openAgentAffecterBusTrajet() {
        openAgentStage("/leith/agent/AgentAffecterBusTrajet.fxml", "Affecter Bus → Trajet", 650, 220);
    }

    public static void openAgentArrets() {
        openAgentStage("/leith/agent/AgentArrets.fxml", "Arrêts du trajet", 600, 450);
    }

    public static void openAgentCandidaturesEnfant() {
        openAgentStage("/leith/agent/AgentCandidaturesEnfant.fxml", "Candidatures Enfant", 600, 400);
    }

    private static void openAgentStage(String fxml, String title, int w, int h) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(fxml));
            Parent root = loader.load();
            Stage stage = new Stage();
            javafx.scene.Scene scene = new javafx.scene.Scene(root, w, h);
            applyAppCss(scene, root);
            stage.setTitle("Zayna – " + title);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error opening " + fxml, e);
        }
    }

    // ---------- Flux Parent ----------
    /** Ouvre le tableau de bord parent (Mes enfants, Demande transport, Suivi candidatures). */
    public static void openParentDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    SceneNavigator.class.getResource("/leith/parent/ParentDashboard.fxml")
            );
            Parent root = loader.load();
            Stage stage = new Stage();
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 420, 320);
            applyAppCss(scene, root);
            stage.setTitle("Zayna – Espace Parent");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error opening ParentDashboard", e);
        }
    }

    /** Ouvre l'écran Mes enfants (liste + Tracking / AI). */
    public static void openParentMesEnfants() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    SceneNavigator.class.getResource("/leith/ParentHome.fxml")
            );
            Parent root = loader.load();
            Stage stage = new Stage();
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 1000, 700);
            applyAppCss(scene, root);
            stage.setTitle("Zayna – Mes enfants");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error opening ParentMesEnfants", e);
        }
    }

    public static void openParentDemandeTransport() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    SceneNavigator.class.getResource("/leith/parent/ParentDemandeTransport.fxml")
            );
            Parent root = loader.load();
            Stage stage = new Stage();
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 480, 380);
            applyAppCss(scene, root);
            stage.setTitle("Zayna – Demande transport enfant");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error opening ParentDemandeTransport", e);
        }
    }

    public static void openParentSuiviCandidaturesEnfant() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    SceneNavigator.class.getResource("/leith/parent/ParentSuiviCandidaturesEnfant.fxml")
            );
            Parent root = loader.load();
            Stage stage = new Stage();
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 550, 400);
            applyAppCss(scene, root);
            stage.setTitle("Zayna – Suivi candidatures transport");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error opening ParentSuiviCandidaturesEnfant", e);
        }
    }

}
