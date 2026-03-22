package tn.esprit.workshop.controlleurs.leith;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import tn.esprit.workshop.controlleurs.leith.AI.ChatAIController;
import tn.esprit.workshop.controlleurs.leith.shell.AdminShellController;
import tn.esprit.workshop.controlleurs.leith.shell.AgentShellController;
import tn.esprit.workshop.controlleurs.leith.shell.ChauffeurShellController;
import tn.esprit.workshop.controlleurs.leith.shell.MaitresseShellController;
import tn.esprit.workshop.controlleurs.leith.shell.ParentShellController;
import tn.esprit.workshop.model.Talel.talel2.User;
import tn.esprit.workshop.utilis.AppSession;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SceneNavigator {

    private static final Logger LOGGER =
            Logger.getLogger(SceneNavigator.class.getName());

    // Role shells: when set, navigation loads content into the shell instead of opening new stages
    private static ParentShellController parentShell;
    private static ChauffeurShellController chauffeurShell;
    private static AgentShellController agentShell;
    private static AdminShellController adminShell;
    private static MaitresseShellController maitresseShell;

    /** User to inject into Admin content when opening AdminShell (set by ConnecterController). */
    private static User pendingAdminUser;

    public static void registerParentShell(ParentShellController c) { parentShell = c; }
    public static void unregisterParentShell() { parentShell = null; }
    public static void registerChauffeurShell(ChauffeurShellController c) { chauffeurShell = c; }
    public static void unregisterChauffeurShell() { chauffeurShell = null; }
    public static void registerAgentShell(AgentShellController c) { agentShell = c; }
    public static void unregisterAgentShell() { agentShell = null; }
    public static void registerAdminShell(AdminShellController c) { adminShell = c; }
    public static void unregisterAdminShell() { adminShell = null; }
    public static void registerMaitresseShell(MaitresseShellController c) { maitresseShell = c; }
    public static void unregisterMaitresseShell() { maitresseShell = null; }

    public static void setPendingAdminUser(User user) { pendingAdminUser = user; }
    public static User getAndClearPendingAdminUser() {
        User u = pendingAdminUser;
        pendingAdminUser = null;
        return u;
    }

    public static boolean isInChauffeurShell() { return chauffeurShell != null; }
    /** When in ChauffeurShell, navigates to home instead of closing; call before close() in sub-pages. */
    public static void chauffeurShellGoHome() {
        if (chauffeurShell != null) chauffeurShell.loadContent("/leith/ChauffeurHome.fxml");
    }
    public static boolean isInAgentShell() { return agentShell != null; }
    /** When in AgentShell, navigates to dashboard instead of closing. */
    public static void agentShellGoHome() {
        if (agentShell != null) agentShell.loadContent("/leith/agent/AgentDashboard.fxml");
    }
    public static boolean isInAdminShell() { return adminShell != null; }
    /** When in AdminShell, perform shell logout (close stage, show login). Use from Admin content to avoid parallel logout logic. */
    public static void requestAdminShellLogout() {
        if (adminShell != null) adminShell.doLogout();
    }

    /** Opens the login window in a new Stage (e.g. after logout). */
    public static void showLoginWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource("/Talel/ConnecterUser.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Zayna - Connexion");
            stage.setScene(new javafx.scene.Scene(root));
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error showing login window", e);
        }
    }

    /** Opens the Parent role in a single maximized window (shell with sidebar). */
    public static void openParentShell() {
        try {
            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource("/leith/shell/ParentShell.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 900, 600);
            applyAppCss(scene, root);
            stage.setTitle("Zayna – Espace Parent");
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error opening ParentShell", e);
        }
    }

    /** Opens the Chauffeur role in a single maximized window. */
    public static void openChauffeurShell() {
        try {
            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource("/leith/shell/ChauffeurShell.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 900, 600);
            applyAppCss(scene, root);
            stage.setTitle("Zayna – Espace Chauffeur");
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error opening ChauffeurShell", e);
        }
    }

    /** Opens the Agent role in a single maximized window. */
    public static void openAgentShell() {
        try {
            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource("/leith/shell/AgentShell.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 900, 600);
            applyAppCss(scene, root);
            stage.setTitle("Zayna – Tableau de bord Agent");
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error opening AgentShell", e);
        }
    }

    /** Opens the Admin role in a single maximized window (shell with sidebar). Pending user set by ConnecterController. */
    public static void openAdminShell() {
        try {
            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource("/leith/shell/AdminShell.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 900, 600);
            stage.setTitle("Zayna – Administration");
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error opening AdminShell", e);
        }
    }

    /** Espace maîtresse (shell avec accueil + gestion enfants). */
    public static void openMaitresseShell() {
        try {
            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource("/leith/shell/MaitresseShell.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 900, 600);
            applyAppCss(scene, root);
            stage.setTitle("Zayna – Espace Maîtresse");
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error opening MaitresseShell", e);
        }
    }

    public static boolean isInMaitresseShell() {
        return maitresseShell != null;
    }

    public static void maitresseShellGoHome() {
        if (maitresseShell != null) {
            maitresseShell.loadContent("/leith/maitresse/MaitresseHome.fxml");
        }
    }

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
            if (controller == null) {
                LOGGER.severe("ChatAI: loader.getController() is null");
                return;
            }
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

    /** Ouvre l'accueil Chauffeur (hub). In-shell if ChauffeurShell is open. */
    public static void openChauffeurHome() {
        if (chauffeurShell != null) {
            chauffeurShell.loadContent("/leith/ChauffeurHome.fxml");
            return;
        }
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

    /** Ouvre le formulaire Postuler Chauffeur; in-shell if ChauffeurShell is open. */
    public static void openPostulerChauffeur(Runnable onClose) {
        if (chauffeurShell != null) {
            chauffeurShell.loadContent("/leith/PostulerChauffeur.fxml");
            return;
        }
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

    /** Ouvre Suivi Candidature; in-shell if ChauffeurShell is open. */
    public static void openSuiviCandidature(Runnable onClose) {
        if (chauffeurShell != null) {
            chauffeurShell.loadContent("/leith/SuiviCandidature.fxml");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(
                    SceneNavigator.class.getResource("/leith/SuiviCandidature.fxml")
            );
            Parent root = loader.load();
            SuiviCandidatureController controller = loader.getController();
            Integer id = AppSession.getInstance().getChauffeurId();
            if (id != null) controller.init(id);
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

    /** Ouvre Espace Chauffeur; in-shell if ChauffeurShell is open. */
    public static void openEspaceChauffeur() {
        if (chauffeurShell != null) {
            chauffeurShell.loadContent("/leith/EspaceChauffeur.fxml");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(
                    SceneNavigator.class.getResource("/leith/EspaceChauffeur.fxml")
            );
            Parent root = loader.load();
            EspaceChauffeurController controller = loader.getController();
            Integer id = AppSession.getInstance().getChauffeurId();
            if (id != null) controller.init(id);
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
        if (agentShell != null) {
            agentShell.loadContent("/leith/agent/AgentDashboard.fxml");
            return;
        }
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
        if (agentShell != null) { agentShell.loadContent("/leith/agent/AgentCandidaturesChauffeur.fxml"); return; }
        openAgentStage("/leith/agent/AgentCandidaturesChauffeur.fxml", "Candidatures Chauffeur", 850, 450);
    }

    public static void openAgentChauffeursAcceptes() {
        if (agentShell != null) { agentShell.loadContent("/leith/agent/AgentChauffeursAcceptes.fxml"); return; }
        openAgentStage("/leith/agent/AgentChauffeursAcceptes.fxml", "Chauffeurs acceptés", 550, 400);
    }

    public static void openAgentBus() {
        if (agentShell != null) { agentShell.loadContent("/leith/agent/AgentBus.fxml"); return; }
        openAgentStage("/leith/agent/AgentBus.fxml", "Gestion Bus", 550, 450);
    }

    public static void openAgentAffecterChauffeurBus() {
        if (agentShell != null) { agentShell.loadContent("/leith/agent/AgentAffecterChauffeurBus.fxml"); return; }
        openAgentStage("/leith/agent/AgentAffecterChauffeurBus.fxml", "Affecter Chauffeur → Bus", 650, 220);
    }

    public static void openAgentTrajets() {
        if (agentShell != null) { agentShell.loadContent("/leith/agent/AgentTrajets.fxml"); return; }
        openAgentStage("/leith/agent/AgentTrajets.fxml", "Gestion Trajets", 550, 450);
    }

    public static void openAgentAffecterBusTrajet() {
        if (agentShell != null) { agentShell.loadContent("/leith/agent/AgentAffecterBusTrajet.fxml"); return; }
        openAgentStage("/leith/agent/AgentAffecterBusTrajet.fxml", "Affecter Bus → Trajet", 650, 220);
    }

    public static void openAgentArrets() {
        if (agentShell != null) { agentShell.loadContent("/leith/agent/AgentArrets.fxml"); return; }
        openAgentStage("/leith/agent/AgentArrets.fxml", "Arrêts du trajet", 600, 450);
    }

    public static void openAgentCandidaturesEnfant() {
        if (agentShell != null) { agentShell.loadContent("/leith/agent/AgentCandidaturesEnfant.fxml"); return; }
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
    public static void openParentDashboard() {
        if (parentShell != null) {
            parentShell.loadContent("/leith/parent/ParentDashboard.fxml");
            return;
        }
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

    public static void openParentMesEnfants() {
        if (parentShell != null) {
            parentShell.loadContent("/leith/ParentHome.fxml");
            return;
        }
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
        if (parentShell != null) {
            parentShell.loadContent("/leith/parent/ParentDemandeTransport.fxml");
            return;
        }
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
        if (parentShell != null) {
            parentShell.loadContent("/leith/parent/ParentSuiviCandidaturesEnfant.fxml");
            return;
        }
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
