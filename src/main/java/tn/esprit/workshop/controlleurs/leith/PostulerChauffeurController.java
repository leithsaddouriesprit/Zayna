package tn.esprit.workshop.controlleurs.leith;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import tn.esprit.workshop.services.leith.ChauffeurService;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;



public class PostulerChauffeurController implements Initializable {

    @FXML private TextField tfNom;
    @FXML private TextField tfPrenom;

    @FXML private Spinner<Integer> spAge;
    @FXML private Spinner<Integer> spExperience;

    @FXML private ChoiceBox<String> cbMaladie; // non stocké
    @FXML private Button btnEnvoyer;

    @FXML private Label lblMessage;

    private ChauffeurService chauffeurService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        chauffeurService = new ChauffeurService();
        tfNom.setTextFormatter(new TextFormatter<>(c ->
                c.getControlNewText().matches("[\\p{L} \\-']*") ? c : null));

        tfPrenom.setTextFormatter(new TextFormatter<>(c ->
                c.getControlNewText().matches("[\\p{L} \\-']*") ? c : null));



        // Spinners
        spAge.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(18, 70, 25));
        spExperience.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 50, 0));

        // ChoiceBox (non stocké)
        cbMaladie.getItems().addAll("Non", "Oui");
        cbMaladie.setValue("Non");

        lblMessage.setText("");
    }

    private void commitSpinner(Spinner<Integer> sp) {
        if (sp.isEditable()) {
            sp.increment(0); // force commit du texte de l’éditeur vers la valeur
        }
    }

    private boolean isNameValid(String s) {
        if (s == null) return false;
        s = s.trim();

        // 1) longueur min
        if (s.length() < 3) return false;

        // 2) caractères autorisés
        if (!s.matches("^[\\p{L}][\\p{L} \\-']*$")) return false;

        // 3) au moins une voyelle (latin + accents)
        String voyelles = "aeiouyAEIOUYàâäéèêëïîôöùûüÿÀÂÄÉÈÊËÏÎÔÖÙÛÜŸ";
        boolean hasVowel = false;
        for (int i = 0; i < s.length(); i++) {
            if (voyelles.indexOf(s.charAt(i)) >= 0) {
                hasVowel = true;
                break;
            }
        }
        if (!hasVowel) return false;

        // 4) éviter répétitions bizarres (ex: "aaa", "zzzz")
        if (s.matches(".*(\\p{L})\\1\\1.*")) return false;

        return true;
    }

    private boolean validerSaisie(String nom, String prenom, int age, int exp) {

        nom = (nom == null) ? "" : nom.trim();
        prenom = (prenom == null) ? "" : prenom.trim();

        String nameRegex = "^[\\p{L}][\\p{L} \\-']{1,49}$";

        if (!isNameValid(nom)) {
            showError("Nom invalide (min 3 caractères, lettres uniquement, doit contenir une voyelle).");
            return false;
        }

        if (!isNameValid(prenom)) {
            showError("Prénom invalide (min 3 caractères, lettres uniquement, doit contenir une voyelle).");
            return false;
        }

        if (nom.isEmpty()) {
            showError("Nom obligatoire.");
            return false;
        }
        if (!nom.matches(nameRegex)) {
            showError("Nom invalide (lettres, espaces, - ' seulement).");
            return false;
        }

        if (prenom.isEmpty()) {
            showError("Prénom obligatoire.");
            return false;
        }
        if (!prenom.matches(nameRegex)) {
            showError("Prénom invalide (lettres, espaces, - ' seulement).");
            return false;
        }

        if (age < 18 || age > 70) {
            showError("Âge invalide (18 à 70).");
            return false;
        }

        if (exp < 0 || exp > 50) {
            showError("Expérience invalide (0 à 50).");
            return false;
        }

        if (exp > (age - 18)) {
            showError("Expérience incohérente par rapport à l’âge.");
            return false;
        }

        if (cbMaladie.getValue() == null) {
            showError("Veuillez choisir Oui/Non pour la maladie.");
            return false;
        }

        return true;
    }

    @FXML
    void envoyerCandidature(ActionEvent event) {
        // Effacer le message à chaque clic
        commitSpinner(spAge);
        commitSpinner(spExperience);
       /// lblMessage.setText("");

        // Lire les valeurs
        String nom = (tfNom.getText() == null) ? "" : tfNom.getText().trim();
        String prenom = (tfPrenom.getText() == null) ? "" : tfPrenom.getText().trim();
        int age = spAge.getValue();
        int exp = spExperience.getValue();

        // IMPORTANT: validation UNIQUE (pas de double validation)
        if (!validerSaisie(nom, prenom, age, exp)) {
            System.out.println("[VALIDATION BLOQUEE] nom=" + nom + ", prenom=" + prenom + ", age=" + age + ", exp=" + exp);
            return;
        }

        // champ non stocké (tu peux le garder pour logique métier plus tard)
        String maladie = cbMaladie.getValue(); // "Oui"/"Non"
        System.out.println("[VALIDATION OK] maladie=" + maladie);

        try {
            int chauffeurId = chauffeurService.ajouterChauffeurEtRetournerId(nom, prenom, age, exp);
            chauffeurService.envoyerCandidatureAgentEcole(chauffeurId);

            showSuccess("Candidature envoyée ✅ (ID chauffeur = " + chauffeurId + ")");

            // reset
            tfNom.clear();
            tfPrenom.clear();
            spAge.getValueFactory().setValue(25);
            spExperience.getValueFactory().setValue(0);
            cbMaladie.setValue("Non");

        } catch (SQLException e) {
            showError("Erreur lors de l'envoi ❌ (voir console)");
             // mieux que System.err.println seul
        }
    }





    private void showError(String msg) {
        lblMessage.setStyle("-fx-text-fill: #d32f2f; -fx-font-size: 13px;");
        lblMessage.setText(msg);
    }

    private void showSuccess(String msg) {
        lblMessage.setStyle("-fx-text-fill: #2e7d32; -fx-font-size: 13px;");
        lblMessage.setText(msg);
    }


}