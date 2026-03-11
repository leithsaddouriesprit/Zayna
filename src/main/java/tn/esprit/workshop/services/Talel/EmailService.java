package tn.esprit.workshop.services.Talel;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EmailService {

    private static final Logger LOGGER = Logger.getLogger(EmailService.class.getName());

    // Configuration SMTP pour Gmail
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "465";

    // ⚠️ REMPLACEZ CES INFORMATIONS PAR VOS IDENTIFIANTS ⚠️
    private static final String EMAIL_USER = "talelismail63@gmail.com";
    private static final String EMAIL_PASSWORD = "ygrwocrwavjevjgc"; // Mot de passe d'application Gmail

    /**
     * Envoie un code de vérification par email
     */
    public static boolean sendVerificationCode(String recipientEmail, String code) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");



        // ✅ AJOUTEZ CES DEUX LIGNES ICI (avec les autres propriétés)
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        props.put("mail.debug", "true");  // Pour voir les logs détaillés
// Pour Gmail spécifiquement
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");


        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_USER, EMAIL_PASSWORD);
            }
        });

        // Activer le débogage (à désactiver en production)
        session.setDebug(true);

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_USER));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("🔐 Zayna - Récupération de mot de passe");

            // Corps de l'email en HTML pour un meilleur rendu
            String htmlContent = String.format(
                    "<!DOCTYPE html>" +
                            "<html>" +
                            "<head>" +
                            "<style>" +
                            "body { font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px; }" +
                            ".container { max-width: 600px; margin: 0 auto; background-color: white; border-radius: 10px; padding: 30px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }" +
                            ".header { text-align: center; margin-bottom: 30px; }" +
                            ".header h1 { color: #667eea; margin: 0; }" +
                            ".code { background-color: #667eea; color: white; font-size: 32px; font-weight: bold; padding: 15px; text-align: center; border-radius: 5px; letter-spacing: 5px; margin: 30px 0; }" +
                            ".footer { text-align: center; color: #718096; font-size: 12px; margin-top: 30px; }" +
                            "</style>" +
                            "</head>" +
                            "<body>" +
                            "<div class='container'>" +
                            "<div class='header'>" +
                            "<h1>🚌 Zayna</h1>" +
                            "<p>Gestion des transports scolaires</p>" +
                            "</div>" +
                            "<p>Bonjour,</p>" +
                            "<p>Vous avez demandé la réinitialisation de votre mot de passe. Voici votre code de vérification :</p>" +
                            "<div class='code'>%s</div>" +
                            "<p>Ce code est valable pendant <strong>5 minutes</strong>. Si vous n'avez pas demandé cette réinitialisation, ignorez cet email.</p>" +
                            "<div class='footer'>" +
                            "<p>© 2024 Zayna - Tous droits réservés</p>" +
                            "</div>" +
                            "</div>" +
                            "</body>" +
                            "</html>",
                    code
            );

            message.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(message);
            LOGGER.info("Email envoyé avec succès à : " + recipientEmail);
            return true;

        } catch (MessagingException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'envoi de l'email", e);
            return false;
        }
    }

    /**
     * Configuration pour utiliser un fichier de propriétés (plus sécurisé)
     */
    public static void configureFromProperties() {
        // À implémenter si vous voulez charger depuis un fichier
    }
}
