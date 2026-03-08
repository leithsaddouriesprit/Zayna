package tn.esprit.workshop.utilis;
import java.security.SecureRandom;
import java.util.Random;
public class CodeGenerator {

        private static final String CHARACTERS = "0123456789";
        private static final int CODE_LENGTH = 6;
        private static final Random RANDOM = new SecureRandom();

        /**
         * Génère un code de vérification à 6 chiffres
         */
        public static String generateVerificationCode() {
            StringBuilder code = new StringBuilder(CODE_LENGTH);
            for (int i = 0; i < CODE_LENGTH; i++) {
                code.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
            }
            return code.toString();
        }

        /**
         * Génère un code avec timestamp pour traçabilité
         */
        public static String generateCodeWithTimestamp() {
            String code = generateVerificationCode();
            long timestamp = System.currentTimeMillis();
            return code + ":" + timestamp;
        }

        /**
         * Vérifie si le code n'est pas expiré (5 minutes)
         */
        public static boolean isCodeValid(String storedCode) {
            try {
                String[] parts = storedCode.split(":");
                if (parts.length == 2) {
                    long timestamp = Long.parseLong(parts[1]);
                    long currentTime = System.currentTimeMillis();
                    // 5 minutes = 300000 millisecondes
                    return (currentTime - timestamp) < 300000;
                }
            } catch (Exception e) {
                return false;
            }
            return false;
        }
    }

