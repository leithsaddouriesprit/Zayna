package tn.esprit.workshop.controlleurs.leith;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import tn.esprit.workshop.services.leith.CandidatureService;
import tn.esprit.workshop.services.leith.ChauffeurService;
import tn.esprit.workshop.utilis.AppSession;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.ResourceBundle;



public class PostulerChauffeurController implements Initializable {

    private static final int MAX_IMAGE_DIMENSION = 900;
    private static final float JPEG_QUALITY = 0.6f;
    private static final int MAX_TOTAL_IMAGE_BYTES = 900_000;

    @FXML private TextField tfNom;
    @FXML private TextField tfPrenom;

    @FXML private Spinner<Integer> spAge;
    @FXML private Spinner<Integer> spExperience;

    @FXML private ChoiceBox<String> cbMaladie; // non stocké
    @FXML private TextField tfMaladieDetails;
    @FXML private Label lblMaladieDetails;
    @FXML private Button btnEnvoyer;

    @FXML private Label lblMessage;

    @FXML private ImageView imgPermisRecto;
    @FXML private ImageView imgPermisVerso;
    @FXML private Label lblPermisRectoStatus;
    @FXML private Label lblPermisVersoStatus;

    private byte[] permisRectoBytes;
    private String permisRectoName;
    private String permisRectoMime;

    private byte[] permisVersoBytes;
    private String permisVersoName;
    private String permisVersoMime;

    private ChauffeurService chauffeurService;
    private CandidatureService candidatureService;

    private static final String STATUT_REFUSEE = "REFUSEE";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        chauffeurService = new ChauffeurService();
        candidatureService = new CandidatureService();
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
        // Cacher au début
        tfMaladieDetails.setVisible(false);
        lblMaladieDetails.setVisible(false);

// Listener dynamique
        cbMaladie.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {

            if ("Oui".equals(newVal)) {
                tfMaladieDetails.setVisible(true);
                lblMaladieDetails.setVisible(true);
            } else {
                tfMaladieDetails.setVisible(false);
                lblMaladieDetails.setVisible(false);
                tfMaladieDetails.clear();
            }
        });
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

        if ("Oui".equals(cbMaladie.getValue())) {
            if (tfMaladieDetails.getText().trim().isEmpty()) {
                showError("Veuillez préciser la maladie.");
                return false;
            }
        }

        return true;
    }

    @FXML
    void onUploadPermisRecto(ActionEvent event) {
        uploadSide(true);
    }

    @FXML
    void onUploadPermisVerso(ActionEvent event) {
        uploadSide(false);
    }

    /**
     * Loads image from file, resizes to max dimension (preserving ratio), encodes as JPEG.
     * Returns compressed bytes for DB storage; mime is always "image/jpeg".
     */
    private byte[] readAndCompress(File file) throws Exception {
        BufferedImage original = ImageIO.read(file);
        if (original == null) throw new IllegalArgumentException("Format d'image non supporté");

        int w = original.getWidth();
        int h = original.getHeight();
        double scale = Math.min(1.0, Math.min((double) MAX_IMAGE_DIMENSION / w, (double) MAX_IMAGE_DIMENSION / h));
        int nw = (int) Math.round(w * scale);
        int nh = (int) Math.round(h * scale);

        BufferedImage scaled = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = scaled.createGraphics();
        g.drawImage(original, 0, 0, nw, nh, null);
        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        if (!writers.hasNext()) throw new IllegalStateException("Aucun writer JPEG disponible");
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
            ImageWriter writer = writers.next();
            writer.setOutput(ios);
            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(JPEG_QUALITY);
            }
            writer.write(null, new IIOImage(scaled, null, null), param);
            writer.dispose();
        }
        return baos.toByteArray();
    }

    private void uploadSide(boolean recto) {
        try {
            FileChooser fc = new FileChooser();
            fc.setTitle(recto ? "Choisir la photo RECTO" : "Choisir la photo VERSO");
            fc.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
            );

            File file = fc.showOpenDialog(btnEnvoyer.getScene().getWindow());
            if (file == null) return;

            byte[] bytes = readAndCompress(file);
            String name = recto ? "permis_recto.jpg" : "permis_verso.jpg";
            String mime = "image/jpeg";

            Image preview = new Image(new ByteArrayInputStream(bytes));

            if (recto) {
                permisRectoBytes = bytes;
                permisRectoName = name;
                permisRectoMime = mime;
                imgPermisRecto.setImage(preview);
                lblPermisRectoStatus.setText("Recto: " + name + " (" + (bytes.length / 1024) + " Ko)");
            } else {
                permisVersoBytes = bytes;
                permisVersoName = name;
                permisVersoMime = mime;
                imgPermisVerso.setImage(preview);
                lblPermisVersoStatus.setText("Verso: " + name + " (" + (bytes.length / 1024) + " Ko)");
            }

        } catch (Exception e) {
            showError("Impossible de lire ou compresser l'image. Réessayez.");
            e.printStackTrace();
        }
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
        if (permisRectoBytes == null || permisVersoBytes == null) {
            showError("Veuillez importer les 2 photos du permis (Recto + Verso).");
            return;
        }

        int totalBytes = permisRectoBytes.length + permisVersoBytes.length;
        System.out.println("[Candidature] rectoBytes=" + permisRectoBytes.length + ", versoBytes=" + permisVersoBytes.length + ", total=" + totalBytes);
        if (totalBytes > MAX_TOTAL_IMAGE_BYTES) {
            showError("Image trop grande, choisissez une image plus petite.");
            return;
        }

        String maladie = null;
        if ("Oui".equals(cbMaladie.getValue())) {
            maladie = tfMaladieDetails.getText().trim();
        }
        try {
            Integer sessionChauffeurId = AppSession.getInstance().getChauffeurId();
            int chauffeurId;
            if (sessionChauffeurId != null) {
                tn.esprit.workshop.model.leith.Candidature c = candidatureService.findByChauffeurId(sessionChauffeurId);
                if (c == null) {
                    chauffeurService.envoyerCandidatureAgentEcole(
                            sessionChauffeurId,
                            permisRectoBytes, permisRectoName, permisRectoMime,
                            permisVersoBytes, permisVersoName, permisVersoMime,
                            maladie
                    );
                    chauffeurId = sessionChauffeurId;
                } else if (STATUT_REFUSEE.equals(c.getStatut())) {
                    candidatureService.updateToEnAttenteWithDocuments(
                            sessionChauffeurId,
                            permisRectoBytes, permisRectoName, permisRectoMime,
                            permisVersoBytes, permisVersoName, permisVersoMime,
                            maladie
                    );
                    chauffeurId = sessionChauffeurId;
                } else {
                    showError("Vous avez déjà une candidature en cours ou acceptée.");
                    return;
                }
            } else {
                chauffeurId = chauffeurService.ajouterChauffeurEtRetournerId(nom, prenom, age, exp);
                chauffeurService.envoyerCandidatureAgentEcole(
                        chauffeurId,
                        permisRectoBytes, permisRectoName, permisRectoMime,
                        permisVersoBytes, permisVersoName, permisVersoMime,
                        maladie
                );
                AppSession.getInstance().setChauffeurId(chauffeurId);
            }
            showSuccess("Candidature envoyée ✅ (ID chauffeur = " + chauffeurId + ")");

            // reset
            tfNom.clear();
            tfPrenom.clear();
            spAge.getValueFactory().setValue(25);
            spExperience.getValueFactory().setValue(0);
            cbMaladie.setValue("Non");

            // reset photo après succès (recto/verso + ancien champ unique)
            permisRectoBytes = null;
            permisRectoName = null;
            permisRectoMime = null;
            imgPermisRecto.setImage(null);
            lblPermisRectoStatus.setText("Aucun fichier");
            permisVersoBytes = null;
            permisVersoName = null;
            permisVersoMime = null;
            imgPermisVerso.setImage(null);
            lblPermisVersoStatus.setText("Aucun fichier");
            tfMaladieDetails.clear();
            tfMaladieDetails.setVisible(false);
            lblMaladieDetails.setVisible(false);
        } catch (SQLException e) {
            e.printStackTrace();
            showError("SQL Error: " + e.getMessage());
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