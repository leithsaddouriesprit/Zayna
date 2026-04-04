package tn.esprit.workshop.utilis;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

/**
 * Filtres de saisie et validations alignés sur zaynaa.sql (longueurs / types).
 * Périmètre : formulaires explicitement branchés par les contrôleurs (pas d’usage global implicite).
 */
public final class ZaynaInputConstraints {

    private ZaynaInputConstraints() {
    }

    /* --- Longueurs (zaynaa.sql) --- */
    public static final int LEN_MAITRESSE_NOM_PRENOM = 100; // maitresse.nom, maitresse.prenom
    public static final int LEN_PARENT_NOM_PRENOM = 80;      // parent.nom, parent.prenom
    public static final int LEN_ENFANT_NOM_PRENOM = 80;      // candidature_enfant.nom_enfant, prenom_enfant
    public static final int LEN_CHAUFFEUR_NOM_PRENOM = 80;   // chauffeur.nom, chauffeur.prenom
    public static final int LEN_AGENT_ECOLE_NOM_PRENOM = 80; // agent_ecole.nom, agent_ecole.prenom (responsable)
    public static final int LEN_USERS_EMAIL = 100;           // users.email
    public static final int LEN_PARENT_EMAIL_COL = 120;      // parent.email (colonne) — users limite à 100 à l’insertion
    public static final int LEN_ECOLE_NOM = 150;             // ecole.nom
    public static final int LEN_USERS_ECOLE = 200;           // users.ecole (responsable)
    public static final int MAX_PROGRAMME_DUREE_DIGITS = 9;   // programme.duree int(11)

    private static final Pattern EMAIL_FINAL = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    /** Saisie email : lettres, chiffres, @ . _ - (sans %) pour coller au cahier ; validation finale plus tolérante. */
    private static final Pattern EMAIL_INPUT_CHAR = Pattern.compile("^[a-zA-Z0-9@._-]*$");

    public static UnaryOperator<TextFormatter.Change> lettersAndSpacesOnly(int maxLen) {
        return change -> {
            String t = change.getControlNewText();
            if (t == null) {
                return null;
            }
            if (t.length() > maxLen) {
                return null;
            }
            if (!t.matches("^[\\p{L} ]*$")) {
                return null;
            }
            return change;
        };
    }

    public static UnaryOperator<TextFormatter.Change> emailInput(int maxLen) {
        return change -> {
            String t = change.getControlNewText();
            if (t == null) {
                return null;
            }
            if (t.length() > maxLen) {
                return null;
            }
            if (!EMAIL_INPUT_CHAR.matcher(t).matches()) {
                return null;
            }
            return change;
        };
    }

    public static UnaryOperator<TextFormatter.Change> digitsOnly(int maxLen) {
        return change -> {
            String t = change.getControlNewText();
            if (t == null) {
                return null;
            }
            if (t.length() > maxLen) {
                return null;
            }
            if (!t.matches("\\d*")) {
                return null;
            }
            return change;
        };
    }

    /** Entier positif (durée programme), au plus maxDigits chiffres. */
    public static UnaryOperator<TextFormatter.Change> positiveIntegerDigits(int maxDigits) {
        return change -> {
            String t = change.getControlNewText();
            if (t == null) {
                return null;
            }
            if (t.isEmpty()) {
                return change;
            }
            if (t.length() > maxDigits || !t.matches("\\d+")) {
                return null;
            }
            return change;
        };
    }

    /**
     * Latitude / longitude : chiffres, un séparateur . ou , optionnel, signe - au début uniquement.
     */
    public static UnaryOperator<TextFormatter.Change> signedDecimalCoordinate() {
        return change -> {
            String t = change.getControlNewText();
            if (t == null) {
                return null;
            }
            if (t.isEmpty()) {
                return change;
            }
            if (t.equals("-")) {
                return change;
            }
            if (!t.matches("^-?[0-9]*([.,][0-9]*)?$")) {
                return null;
            }
            return change;
        };
    }

    /**
     * Salaire DECIMAL(10,2) : positif, au plus 8 chiffres avant la virgule et 2 après.
     */
    public static UnaryOperator<TextFormatter.Change> positiveDecimalMoney() {
        return change -> {
            String t = change.getControlNewText();
            if (t == null) {
                return null;
            }
            if (t.isEmpty()) {
                return change;
            }
            String norm = t.replace(',', '.');
            if (!norm.matches("^\\d*(\\.\\d{0,2})?$")) {
                return null;
            }
            int dot = norm.indexOf('.');
            String intPart = dot < 0 ? norm : norm.substring(0, dot);
            if (intPart.length() > 8) {
                return null;
            }
            return change;
        };
    }

    public static void apply(TextField tf, UnaryOperator<TextFormatter.Change> filter) {
        if (tf == null) {
            return;
        }
        tf.setTextFormatter(new TextFormatter<>(filter));
    }

    /* ---------- Validations finales (trim) ---------- */

    public static String validatePersonName(String raw, int maxLen, String label) {
        if (raw == null) {
            raw = "";
        }
        String s = raw.trim();
        if (s.isEmpty()) {
            return label + " est obligatoire.";
        }
        if (s.length() > maxLen) {
            return label + " ne doit pas dépasser " + maxLen + " caractères.";
        }
        if (!s.matches("^[\\p{L} ]+$")) {
            return label + " : lettres et espaces uniquement.";
        }
        return null;
    }

    public static String validateSchoolNameLettersOnly(String raw, int maxLen) {
        if (raw == null) {
            raw = "";
        }
        String s = raw.trim();
        if (s.isEmpty()) {
            return "Le nom de l'école est obligatoire.";
        }
        if (s.length() > maxLen) {
            return "Le nom de l'école ne doit pas dépasser " + maxLen + " caractères.";
        }
        if (!s.matches("^[\\p{L} ]+$")) {
            return "Nom de l'école : lettres et espaces uniquement.";
        }
        return null;
    }

    /** emailMax : utiliser LEN_USERS_EMAIL (100) pour tout compte users. */
    public static String validateEmail(String raw, int emailMax) {
        if (raw == null) {
            raw = "";
        }
        String s = raw.trim().toLowerCase();
        if (s.isEmpty()) {
            return "L'email est obligatoire.";
        }
        if (s.length() > emailMax) {
            return "L'email ne doit pas dépasser " + emailMax + " caractères.";
        }
        if (!EMAIL_FINAL.matcher(s).matches()) {
            return "Format d'email invalide.";
        }
        return null;
    }

    public static String validatePhone8(String raw, boolean required) {
        if (raw == null) {
            raw = "";
        }
        String s = raw.trim();
        if (s.isEmpty()) {
            return required ? "Le téléphone est obligatoire." : null;
        }
        if (!s.matches("\\d{8}")) {
            return "Le téléphone doit contenir exactement 8 chiffres.";
        }
        return null;
    }

    public static String validateLatitude(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return "La latitude est obligatoire.";
        }
        try {
            double v = Double.parseDouble(raw.trim().replace(',', '.'));
            if (v < -90 || v > 90) {
                return "La latitude doit être entre -90 et 90.";
            }
        } catch (NumberFormatException e) {
            return "Latitude invalide.";
        }
        return null;
    }

    public static String validateLongitude(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return "La longitude est obligatoire.";
        }
        try {
            double v = Double.parseDouble(raw.trim().replace(',', '.'));
            if (v < -180 || v > 180) {
                return "La longitude doit être entre -180 et 180.";
            }
        } catch (NumberFormatException e) {
            return "Longitude invalide.";
        }
        return null;
    }

    public static String validateSalaryDecimal(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return "Le salaire est obligatoire.";
        }
        try {
            double v = Double.parseDouble(raw.trim().replace(',', '.'));
            if (v < 0 || Double.isNaN(v) || Double.isInfinite(v)) {
                return "Le salaire doit être un nombre positif.";
            }
            if (v > 99_999_999.99) {
                return "Le salaire dépasse la limite autorisée.";
            }
        } catch (NumberFormatException e) {
            return "Le salaire doit être un nombre valide.";
        }
        return null;
    }

    public static String validateProgramDuration(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return "La durée est obligatoire.";
        }
        try {
            long v = Long.parseLong(raw.trim());
            if (v <= 0) {
                return "La durée doit être un entier strictement positif.";
            }
            if (v > Integer.MAX_VALUE) {
                return "La durée est trop grande.";
            }
        } catch (NumberFormatException e) {
            return "La durée doit être un nombre entier valide.";
        }
        return null;
    }

    public static String validateChildAge3to18(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return "L'âge est obligatoire.";
        }
        try {
            int a = Integer.parseInt(raw.trim());
            if (a < 3 || a > 18) {
                return "L'âge doit être un nombre entre 3 et 18.";
            }
        } catch (NumberFormatException e) {
            return "L'âge doit être un nombre entre 3 et 18.";
        }
        return null;
    }
}
