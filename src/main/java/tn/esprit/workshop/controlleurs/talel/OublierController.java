package tn.esprit.workshop.controlleurs.talel;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.workshop.services.EmailService;
import tn.esprit.workshop.services.talel.ServiceOublier;
import tn.esprit.workshop.utilis.CodeGenerator;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class OublierController implements Initializable {

    // ==================== ÉLÉMENTS FXML ====================
    @FXML private VBox emailStep;
    @FXML private VBox codeStep;
    @FXML private VBox newPasswordStep;
    @FXML private VBox successStep;

    @FXML private TextField emailField;
    @FXML private TextField codeField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    @FXML private Label messageLabel;
    @FXML private Label timerLabel;
    @FXML private Label userTypeLabel;

    @FXML private Button sendCodeButton;
    @FXML private Button verifyCodeButton;
    @FXML private Button resetPasswordButton;

    @FXML private ProgressIndicator loadingIndicator;

    // ==================== VARIABLES ====================
    private String verificationCode;
    private String userEmail;
    private Timeline timeline;
    private int timeSeconds = 300; // 5 minutes

    // ✅ CORRECTION 1: Créer une instance du service (PAS STATIC)
    private ServiceOublier serviceOublier = new ServiceOublier();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Animation d'entrée
        animateEntrance();

        // Validation en temps réel
        setupRealTimeValidation();

        // Focus sur le premier champ
        emailField.requestFocus();

        System.out.println("=== OublierController initialisé avec ServiceOublier ===");
    }

    // ==================== ANIMATIONS ====================
    private void animateEntrance() {
        FadeTransition fade = new FadeTransition(Duration.seconds(0.5), emailStep);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private void setupRealTimeValidation() {
        // Validation email
        emailField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                if (newVal.contains("@") && newVal.contains(".")) {
                    emailField.setStyle("-fx-border-color: #48bb78;");
                } else {
                    emailField.setStyle("-fx-border-color: #f56565;");
                }
            } else {
                emailField.setStyle("-fx-border-color: #e2e8f0;");
            }
        });
    }

    // ==================== ÉTAPE 1: ENVOI DU CODE ====================
    @FXML
    private void handleSendCode() {
        String email = emailField.getText().trim();

        // Validation
        if (email.isEmpty()) {
            showError("Veuillez entrer votre email");
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            showError("Format d'email invalide");
            return;
        }

        // Afficher le chargement
        showLoading(true);

        // ✅ CORRECTION 2: Utiliser l'instance au lieu de la classe
        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(event -> {

            if (!serviceOublier.emailExists(email)) {  // ← CORRIGÉ
                showLoading(false);
                showError("Cet email n'est pas enregistré dans notre système");
                return;
            }

            // Générer le code
            verificationCode = CodeGenerator.generateVerificationCode();
            userEmail = email;

            // ✅ CORRECTION 3: Utiliser l'instance pour saveResetCode
            serviceOublier.saveResetCode(email, verificationCode);  // ← CORRIGÉ

            // ✅ CORRECTION 4: Utiliser l'instance pour getUserType
            String userType = serviceOublier.getUserType(email);  // ← CORRIGÉ
            userTypeLabel.setText("Compte : " + userType);

            boolean emailSent = EmailService.sendVerificationCode(email, verificationCode);

            // NOUVEAU CODE (simulation) :
           // boolean emailSent = true; // Simuler l'envoi
           // System.out.println("🔐 CODE DE TEST (simulé) : " + verificationCode);
           // showSuccess("✅ Code de test: " + verificationCode);


            showLoading(false);

            if (emailSent) {
                showSuccess("✅ Code envoyé à " + maskEmail(email));
                showCodeStep();
                startTimer();
            } else {
                showError("❌ Erreur d'envoi. Vérifiez votre connexion internet.");
            }
        });
        pause.play();
    }

    // ==================== ÉTAPE 2: VÉRIFICATION DU CODE ====================
    @FXML
    private void handleVerifyCode() {
        String enteredCode = codeField.getText().trim();

        if (enteredCode.isEmpty()) {
            showError("Veuillez entrer le code de vérification");
            return;
        }

        showLoading(true);

        PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
        pause.setOnFinished(event -> {
            showLoading(false);

            // ✅ CORRECTION 5: Utiliser l'instance pour isResetCodeValid
            if (serviceOublier.isResetCodeValid(userEmail, enteredCode)) {  // ← CORRIGÉ
                showSuccess("✅ Code valide !");
                stopTimer();
                showNewPasswordStep();
            } else {
                showError("❌ Code incorrect ou expiré");
                shakeNode(codeField);
            }
        });
        pause.play();
    }

    // ==================== ÉTAPE 3: NOUVEAU MOT DE PASSE ====================
    @FXML
    private void handleResetPassword() {
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validations
        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showError("Veuillez remplir tous les champs");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showError("Les mots de passe ne correspondent pas");
            shakeNode(confirmPasswordField);
            return;
        }

        if (newPassword.length() < 6) {
            showError("Le mot de passe doit contenir au moins 6 caractères");
            return;
        }

        // Vérifier la force du mot de passe
        if (!isPasswordStrong(newPassword)) {
            showError("Le mot de passe doit contenir au moins une majuscule et un chiffre");
            return;
        }

        showLoading(true);

        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(event -> {
            // ✅ CORRECTION 6: Utiliser l'instance pour updatePassword
            boolean updated = serviceOublier.updatePassword(userEmail, newPassword);  // ← CORRIGÉ
            showLoading(false);

            if (updated) {
                showSuccessStep();
            } else {
                showError("❌ Erreur lors de la réinitialisation");
            }
        });
        pause.play();
    }

    // ==================== NAVIGATION ====================
    @FXML
    private void handleBackToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/ConnecterUser.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) emailField.getScene().getWindow();

            // Animation de transition
            Scene currentScene = stage.getScene();
            root.setOpacity(0);
            currentScene.setRoot(root);

            FadeTransition fade = new FadeTransition(Duration.seconds(0.5), root);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.play();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur de navigation");
        }
    }

    @FXML
    private void handleResendCode() {
        handleSendCode(); // Réutilise la même logique
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    private void showCodeStep() {
        emailStep.setVisible(false);
        emailStep.setManaged(false);
        codeStep.setVisible(true);
        codeStep.setManaged(true);

        // Animation
        FadeTransition fade = new FadeTransition(Duration.seconds(0.5), codeStep);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();

        codeField.requestFocus();
    }

    private void showNewPasswordStep() {
        codeStep.setVisible(false);
        codeStep.setManaged(false);
        newPasswordStep.setVisible(true);
        newPasswordStep.setManaged(true);

        FadeTransition fade = new FadeTransition(Duration.seconds(0.5), newPasswordStep);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();

        newPasswordField.requestFocus();
    }

    private void showSuccessStep() {
        newPasswordStep.setVisible(false);
        newPasswordStep.setManaged(false);
        successStep.setVisible(true);
        successStep.setManaged(true);

        // Animation de succès
        ScaleTransition scale = new ScaleTransition(Duration.seconds(0.5), successStep);
        scale.setFromX(0.8);
        scale.setFromY(0.8);
        scale.setToX(1);
        scale.setToY(1);
        scale.play();

        // Redirection automatique après 3 secondes
        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> handleBackToLogin());
        pause.play();
    }

    private void startTimer() {
        timeSeconds = 300;
        timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);

        KeyFrame frame = new KeyFrame(Duration.seconds(1), event -> {
            timeSeconds--;
            int minutes = timeSeconds / 60;
            int seconds = timeSeconds % 60;
            timerLabel.setText(String.format("⏱️ Code valable : %d:%02d", minutes, seconds));

            if (timeSeconds <= 0) {
                stopTimer();
                showError("⌛ Code expiré. Veuillez recommencer.");
                resetToEmailStep();
            }
        });

        timeline.getKeyFrames().add(frame);
        timeline.play();
    }

    private void stopTimer() {
        if (timeline != null) {
            timeline.stop();
        }
    }

    private void resetToEmailStep() {
        emailStep.setVisible(true);
        emailStep.setManaged(true);
        codeStep.setVisible(false);
        codeStep.setManaged(false);
        newPasswordStep.setVisible(false);
        newPasswordStep.setManaged(false);
        successStep.setVisible(false);
        successStep.setManaged(false);

        verificationCode = null;
        emailField.clear();
    }

    private void showLoading(boolean show) {
        loadingIndicator.setVisible(show);
        sendCodeButton.setDisable(show);
        verifyCodeButton.setDisable(show);
        resetPasswordButton.setDisable(show);
    }

    private void showError(String message) {
        messageLabel.setText("❌ " + message);
        messageLabel.setStyle("-fx-background-color: #fed7d7; -fx-text-fill: #c53030; " +
                "-fx-padding: 10; -fx-background-radius: 5;");
        messageLabel.setVisible(true);

        // Cache après 5 secondes
        PauseTransition pause = new PauseTransition(Duration.seconds(5));
        pause.setOnFinished(e -> messageLabel.setVisible(false));
        pause.play();
    }

    private void showSuccess(String message) {
        messageLabel.setText("✅ " + message);
        messageLabel.setStyle("-fx-background-color: #c6f6d5; -fx-text-fill: #22543d; " +
                "-fx-padding: 10; -fx-background-radius: 5;");
        messageLabel.setVisible(true);

        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> messageLabel.setVisible(false));
        pause.play();
    }

    private void shakeNode(javafx.scene.Node node) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(100), node);
        tt.setFromX(0);
        tt.setByX(10);
        tt.setCycleCount(6);
        tt.setAutoReverse(true);
        tt.play();
    }

    private String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex > 1) {
            return email.substring(0, 1) + "****" +
                    email.substring(atIndex - 1);
        }
        return email;
    }

    private boolean isPasswordStrong(String password) {
        // Au moins une majuscule, un chiffre
        return password.matches(".*[A-Z].*") &&
                password.matches(".*[0-9].*");
    }
}