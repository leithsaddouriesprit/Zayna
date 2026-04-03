package tn.esprit.workshop.controlleurs.leith.parent;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import tn.esprit.workshop.controlleurs.leith.SceneNavigator;
import tn.esprit.workshop.services.leith.WeatherService;
import tn.esprit.workshop.services.leith.WeatherService.WeatherInfo;
import tn.esprit.workshop.utilis.AppSession;

import tn.esprit.workshop.utilis.MyBDConnexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

public class ParentDashboardController {

    @FXML private Label lblWelcome;
    @FXML private Label lblWeatherIcon;
    @FXML private Label lblWeatherTemp;
    @FXML private Label lblWeatherStatus;
    @FXML private Label lblTip;

    private final WeatherService weatherService = new WeatherService();

    @FXML
    public void initialize() {
        String name = AppSession.getInstance().getConnectedUserName();
        String firstName = (name != null && !name.isBlank()) ? name.trim().split("\\s+")[0] : "Parent";
        if (lblWelcome != null) {
            lblWelcome.setText(buildWelcomeMessage(firstName));
        }
        if (lblWeatherStatus != null) {
            lblWeatherStatus.setText("Chargement de la météo…");
        }
        if (lblWeatherTemp != null) {
            lblWeatherTemp.setText("");
        }
        if (lblTip != null) {
            lblTip.setText(buildDailyTip(null));
        }

        Task<WeatherInfo> task = new Task<>() {
            @Override
            protected WeatherInfo call() {
                String city = resolveConnectedParentCity();
                if (city != null && !city.isBlank()) {
                    return weatherService.fetchForCity(city);
                }
                return weatherService.fetchForDefaultCity();
            }
        };
        task.setOnSucceeded(e -> applyWeatherOnFx(task.getValue()));
        task.setOnFailed(e -> Platform.runLater(() -> {
            if (lblWeatherStatus != null) {
                lblWeatherStatus.setText("Météo indisponible pour le moment");
            }
            if (lblWeatherTemp != null) {
                lblWeatherTemp.setText("");
            }
            if (lblWeatherIcon != null) {
                lblWeatherIcon.setText("☁");
            }
            if (lblTip != null) {
                lblTip.setText(buildDailyTip(null));
            }
        }));
        Thread t = new Thread(task, "parent-dashboard-weather");
        t.setDaemon(true);
        t.start();
    }

    private void applyWeatherOnFx(WeatherInfo info) {
        if (info != null && info.isAvailable() && lblWeatherTemp != null && lblWeatherStatus != null) {
            double t = info.getTemperatureCelsius() != null ? info.getTemperatureCelsius() : Double.NaN;
            String tempText = Double.isNaN(t)
                    ? "— °C"
                    : String.format(Locale.FRANCE, "%.0f°C", t);
            lblWeatherTemp.setText(tempText);
            lblWeatherStatus.setText(capitalizeFirst(info.getDescription() != null ? info.getDescription() : ""));
            if (lblWeatherIcon != null) {
                lblWeatherIcon.setText(iconForCondition(info.getCondition()));
            }
        } else if (lblWeatherStatus != null) {
            if (lblWeatherTemp != null) {
                lblWeatherTemp.setText("");
            }
            lblWeatherStatus.setText("Météo indisponible pour le moment");
            if (lblWeatherIcon != null) {
                lblWeatherIcon.setText("☁");
            }
        }
        if (lblTip != null) {
            lblTip.setText(buildDailyTip(info));
        }
    }

    private String buildWelcomeMessage(String firstName) {
        java.time.LocalTime now = java.time.LocalTime.now();
        String greeting;
        if (now.isBefore(java.time.LocalTime.of(12, 0))) {
            greeting = "Bonjour " + firstName + " 👋";
        } else if (now.isBefore(java.time.LocalTime.of(18, 0))) {
            greeting = "Bonjour " + firstName + " 👋";
        } else {
            greeting = "Bonsoir " + firstName + " 👋";
        }
        return greeting + "\nUn bon moment pour suivre le trajet de votre enfant en toute sérénité.";
    }

    private String buildDailyTip(WeatherInfo info) {
        if (info != null && info.isAvailable() && info.getCondition() != null) {
            String c = info.getCondition().toLowerCase(Locale.ROOT);
            if (c.contains("rain")) {
                return "Prévoir un imperméable ou un parapluie pour votre enfant aujourd'hui.";
            }
            if (c.contains("snow")) {
                return "Habillez bien votre enfant, les températures sont fraîches aujourd'hui.";
            }
            if (c.contains("clear")) {
                return "Une belle journée en perspective, n'oubliez pas une bouteille d'eau dans le sac.";
            }
            if (c.contains("cloud")) {
                return "Un ciel couvert aujourd'hui, prévoyez une petite veste pour votre enfant.";
            }
        }
        int day = java.time.LocalDate.now().getDayOfYear();
        switch (day % 3) {
            case 0:
                return "Préparez le sac de votre enfant la veille pour un matin plus serein.";
            case 1:
                return "Gardez votre téléphone joignable pendant l'horaire du transport.";
            default:
                return "Vérifiez l'horaire et le point de prise en charge avant de quitter la maison.";
        }
    }

    private String iconForCondition(String condition) {
        if (condition == null) return "☁";
        String c = condition.toLowerCase(Locale.ROOT);
        if (c.contains("rain")) return "🌧";
        if (c.contains("snow")) return "❄";
        if (c.contains("storm") || c.contains("thunder")) return "⛈";
        if (c.contains("clear")) return "☀";
        if (c.contains("cloud")) return "☁";
        if (c.contains("mist") || c.contains("fog")) return "🌫";
        return "☁";
    }

    private String capitalizeFirst(String s) {
        if (s == null || s.isBlank()) return "";
        String trimmed = s.trim();
        return trimmed.substring(0, 1).toUpperCase(Locale.ROOT) + trimmed.substring(1);
    }

    private String resolveConnectedParentCity() {
        int parentId = AppSession.getInstance().getParentId();
        if (parentId <= 0) {
            return null;
        }
        String sql = "SELECT u.adresse FROM parent p JOIN users u ON p.user_id = u.id WHERE p.id = ?";
        try (Connection conn = MyBDConnexion.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, parentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String adresse = rs.getString("adresse");
                    return extractCityFromAddress(adresse);
                }
            }
        } catch (SQLException ignored) {
            // fallback météo par défaut
        }
        return null;
    }

    private String extractCityFromAddress(String adresse) {
        if (adresse == null) return null;
        String trimmed = adresse.trim();
        if (trimmed.isEmpty()) return null;
        if (!trimmed.contains(",") && !trimmed.contains(";")) {
            return trimmed;
        }
        String firstPart = trimmed.split("[,;]")[0].trim();
        return firstPart.isEmpty() ? trimmed : firstPart;
    }

    @FXML
    void openMesEnfants() {
        SceneNavigator.openParentMesEnfants();
    }

    @FXML
    void openDemandeTransport() {
        SceneNavigator.openParentDemandeTransport();
    }

    @FXML
    void openSuiviCandidatures() {
        SceneNavigator.openParentSuiviCandidaturesEnfant();
    }
}
