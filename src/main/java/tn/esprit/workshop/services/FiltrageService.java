package tn.esprit.workshop.services;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class FiltrageService {

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .build();

    // API PurgoMalum
    private static final String API_CONTAINS = "https://www.purgomalum.com/service/containsprofanity";
    private static final String API_FILTER = "https://www.purgomalum.com/service/json";

    // ✅ NOUVEAU : Stocker les tentatives des utilisateurs
    private final Map<Integer, TentativeUtilisateur> tentativesUtilisateurs = new ConcurrentHashMap<>();

    // Nombre de tentatives autorisées avant blocage
    private static final int MAX_TENTATIVES = 2;

    // Durée du blocage en minutes
    private static final int BLOCAGE_MINUTES = 1;

    /**
     * Vérifie si un texte contient des mots grossiers
     */
    public boolean contientGrossieretes(String texte) {
        if (texte == null || texte.trim().isEmpty()) {
            return false;
        }

        try {
            String encodedText = URLEncoder.encode(texte, StandardCharsets.UTF_8.toString());
            String url = API_CONTAINS + "?text=" + encodedText;

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

            try (Response response = client.newCall(request).execute()) {
                String resultat = response.body().string().trim();
                return "true".equals(resultat);
            }
        } catch (Exception e) {
            System.err.println("Erreur filtrage: " + e.getMessage());
            return false;
        }
    }

    /**
     * Filtre un texte en remplaçant les mots grossiers par des étoiles
     */
    public String filtrerTexte(String texte) {
        if (texte == null || texte.trim().isEmpty()) {
            return texte;
        }

        try {
            String encodedText = URLEncoder.encode(texte, StandardCharsets.UTF_8.toString());
            String url = API_FILTER + "?text=" + encodedText + "&fillchar=*";

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

            try (Response response = client.newCall(request).execute()) {
                String jsonResponse = response.body().string();

                if (jsonResponse.contains("\"result\":")) {
                    int start = jsonResponse.indexOf("\"result\":\"") + 10;
                    int end = jsonResponse.indexOf("\"", start);
                    return jsonResponse.substring(start, end);
                }
                return texte;
            }
        } catch (Exception e) {
            System.err.println("Erreur filtrage: " + e.getMessage());
            return texte;
        }
    }

    /**
     * ✅ NOUVEAU : Vérifie si un utilisateur est bloqué
     * @param userId L'ID de l'utilisateur
     * @return true si l'utilisateur est bloqué, false sinon
     *
    /**
     * ✅ NOUVEAU : Enregistre une tentative d'envoi de réclamation avec gros mots
     * @param userId L'ID de l'utilisateur
     * @return Le résultat du filtrage (bloqué ou non)
     */
    public ResultatFiltrage enregistrerTentative(int userId, String message) {
        boolean contientGrossieretes = contientGrossieretes(message);

        if (contientGrossieretes) {
            TentativeUtilisateur tentative = tentativesUtilisateurs.get(userId);

            if (tentative == null) {
                tentative = new TentativeUtilisateur();
                tentativesUtilisateurs.put(userId, tentative);
            }

            int tentativeActuelle = tentative.incrementerTentatives();

            if (tentativeActuelle >= MAX_TENTATIVES) {
                tentative.bloquer();
                return new ResultatFiltrage(message, true, true,
                        "⚠️ VOUS ÊTES BLOQUÉ !\n" +
                                "Vous avez fait " + MAX_TENTATIVES + " tentatives avec des messages inappropriés.\n" +
                                "Vous ne pourrez plus envoyer de réclamation pendant " + BLOCAGE_MINUTES + " minutes.");
            } else {
                int restantes = MAX_TENTATIVES - tentativeActuelle;
                return new ResultatFiltrage(message, true, false,
                        "❌ MESSAGE REFUSÉ - Langage inapproprié détecté !\n\n" +
                                "Votre réclamation n'a PAS été envoyée.\n" +
                                "Il vous reste " + restantes + " tentative(s) avant blocage.\n\n" +
                                "Veuillez reformuler votre message sans mots grossiers.");
            }
        }

        // Pas de gros mots, réinitialiser les tentatives
        resetTentatives(userId);
        return new ResultatFiltrage(message, false, false, null);
    }
    /**
     * ✅ NOUVEAU : Réinitialise les tentatives d'un utilisateur (après un envoi réussi)
     */
    public void resetTentatives(int userId) {
        tentativesUtilisateurs.remove(userId);
    }


    /**
     * Version simplifiée pour l'analyse (sans blocage)
     */
    public ResultatFiltrage analyser(String texte) {
        if (texte == null || texte.trim().isEmpty()) {
            return new ResultatFiltrage(texte, false, false, null);
        }

        boolean contientGrossieretes = contientGrossieretes(texte);
        String texteFiltre = contientGrossieretes ? filtrerTexte(texte) : texte;

        return new ResultatFiltrage(texteFiltre, contientGrossieretes, false, null);
    }

    /**
     * ✅ CLASSE INTERNE pour gérer les tentatives d'un utilisateur
     */
    public static class TentativeUtilisateur {
        private int compteurTentatives;
        private LocalDateTime debutBlocage;

        public TentativeUtilisateur() {
            this.compteurTentatives = 0;
            this.debutBlocage = null;
        }

        public int incrementerTentatives() {
            this.compteurTentatives++;
            return this.compteurTentatives;
        }

        public void bloquer() {
            this.debutBlocage = LocalDateTime.now();
        }


        public boolean blocageExpire() {
            if (debutBlocage == null) return true;
            return LocalDateTime.now().isAfter(debutBlocage.plusMinutes(BLOCAGE_MINUTES));
        }

        public long getMinutesRestantes() {
            if (debutBlocage == null) return 0;
            long minutes = java.time.Duration.between(LocalDateTime.now(),
                    debutBlocage.plusMinutes(BLOCAGE_MINUTES)).toMinutes();
            return Math.max(0, minutes);
        }

        public int getCompteurTentatives() {
            return compteurTentatives;
        }
    }

    /**
     * ✅ CLASSE INTERNE POUR LE RÉSULTAT (améliorée)
     */
    public static class ResultatFiltrage {
        private final String texteFiltre;
        private final boolean contientGrossieretes;
        private final boolean estBloque;
        private final String messageErreur;

        public ResultatFiltrage(String texteFiltre, boolean contientGrossieretes,
                                boolean estBloque, String messageErreur) {
            this.texteFiltre = texteFiltre;
            this.contientGrossieretes = contientGrossieretes;
            this.estBloque = estBloque;
            this.messageErreur = messageErreur;
        }

        public String getTexteFiltre() { return texteFiltre; }
        public boolean contientGrossieretes() { return contientGrossieretes; }
        public boolean estBloque() { return estBloque; }
        public String getMessageErreur() { return messageErreur; }
    }
    // Map pour stocker les tentatives
    private final Map<Integer, Integer> tentatives = new ConcurrentHashMap<>();
    private final Map<Integer, LocalDateTime> bloques = new ConcurrentHashMap<>();

    /**
     * Incrémente le compteur de tentatives d'un utilisateur
     * @return le nombre de tentatives (1, 2 ou 3)
     */
    public int incrementerTentatives(int userId) {
        int count = tentatives.getOrDefault(userId, 0) + 1;
        tentatives.put(userId, count);
        return count;
    }

    /**
     * Vérifie si un utilisateur est bloqué
     */
    public boolean estBloque(int userId) {
        LocalDateTime finBlocage = bloques.get(userId);
        if (finBlocage == null) return false;
        if (LocalDateTime.now().isAfter(finBlocage)) {
            bloques.remove(userId);
            tentatives.remove(userId);
            return false;
        }
        return true;
    }

    /**
     * Bloque un utilisateur pour 5 minutes
     */
    public void bloquerUtilisateur(int userId) {
        bloques.put(userId, LocalDateTime.now().plusMinutes(5));
    }


    /**
     * Retourne le temps restant de blocage
     */
    public String getTempsRestantBlocage(int userId) {
        LocalDateTime finBlocage = bloques.get(userId);
        if (finBlocage == null) return "0";
        long minutes = java.time.Duration.between(LocalDateTime.now(), finBlocage).toMinutes();
        return String.valueOf(Math.max(1, minutes));
    }
}