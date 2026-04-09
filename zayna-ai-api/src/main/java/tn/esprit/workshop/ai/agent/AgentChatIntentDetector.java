package tn.esprit.workshop.ai.agent;

import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AgentChatIntentDetector {

    public record Detection(AgentChatIntentType type, String busToken, String nameQuery) {
        static Detection of(AgentChatIntentType t) {
            return new Detection(t, null, null);
        }

        static Detection bus(AgentChatIntentType t, String token) {
            return new Detection(t, token, null);
        }

        static Detection name(AgentChatIntentType t, String q) {
            return new Detection(t, null, q);
        }
    }

    private static final Set<String> BUS_DE_NAME_STOPWORDS = Set.of(
            "mon", "ma", "mes", "notre", "nos", "votre", "vos", "leur", "leurs",
            "le", "la", "les", "l", "une", "un", "des", "du", "cette", "cet",
            "son", "sa", "ses", "tout", "toute", "tous", "chaque", "quelques",
            "agent", "ecole", "ecoles");

    private static final Pattern BUS_LABELED = Pattern.compile(
            "(?:bus|le\\s+bus)\\s*(?:n°|#|numero|numéro)?\\s*([A-Za-z0-9\\-]+)",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern MAITRESSE_DU_BUS = Pattern.compile(
            "(?i)ma[iî]tresse\\s+(?:du|de\\s+l[ae]\\s+)?bus\\s*(?:n°|#|numero|numéro)?\\s*([A-Za-z0-9\\-]+)");
    private static final Pattern NOM_MAITRESSE_DU_BUS = Pattern.compile(
            "(?i)(?:nom\\s+(?:de\\s+)?(?:la\\s+)?)?(?:qui\\s+est|quelle\\s+est)?\\s*(?:la\\s+)?ma[iî]tresse\\s+du\\s+bus\\s*(?:n°|#|numero|numéro)?\\s*([A-Za-z0-9\\-]+)");
    private static final Pattern CHAUFFEUR_DU_BUS = Pattern.compile(
            "(?i)(?:chauffeur|conducteur)\\s+(?:du|de\\s+l[ae]\\s+)?bus\\s*(?:n°|#|numero|numéro)?\\s*([A-Za-z0-9\\-]+)");
    private static final Pattern QUI_CHAUFFEUR_DU_BUS = Pattern.compile(
            "(?i)(?:qui\\s+est\\s+(?:le\\s+)?)?(?:quel\\s+)?(?:chauffeur|conducteur)\\s+(?:est\\s+)?(?:affect[\\p{L}]*|assign[\\p{L}]*)\\s+(?:a|à|au)\\s+bus\\s*(?:n°|#|numero|numéro)?\\s*([A-Za-z0-9\\-]+)");
    private static final Pattern NOM_CHAUFFEUR_DU_BUS = Pattern.compile(
            "(?i)nom\\s+(?:du\\s+)?(?:chauffeur|conducteur)\\s+du\\s+bus\\s*(?:n°|#|numero|numéro)?\\s*([A-Za-z0-9\\-]+)");

    public Detection detect(String rawInput) {
        if (rawInput == null || rawInput.isBlank()) {
            return Detection.of(AgentChatIntentType.UNSUPPORTED);
        }
        String raw = rawInput.trim();
        String n = AgentChatTextNormalizer.forMatching(raw);

        if (n.matches("^(bonjour|salut|hello|bonsoir|coucou|hi|hey|merci|ok|d\\s*accord)\\b[\\s!?.,]*$")
                || n.matches("^merci\\b.*")) {
            return Detection.of(AgentChatIntentType.GREETING_SMALLTALK);
        }

        if (n.contains("candidature")
                && matchesAny(n, "\\b(accepte|accepter|valide|valider|approuve|approuver)\\b")) {
            return Detection.of(AgentChatIntentType.ACCEPT_CANDIDATURE_ENFANT);
        }

        if (containsAnyLiteral(n, "toute la base", "toutes les ecoles", "all schools", "globalement dans le systeme")) {
            return Detection.of(AgentChatIntentType.OUT_OF_SCOPE_GLOBAL);
        }

        // --- 1) Relations « sans » (avant listes / comptages généraux) ---
        if (isBusWithoutMaitresseQuestion(n)) {
            if (isCountIntent(n)) {
                return Detection.of(AgentChatIntentType.COUNT_BUSES_WITHOUT_MAITRESSE);
            }
            return Detection.of(AgentChatIntentType.LIST_BUSES_WITHOUT_MAITRESSE);
        }
        if (isTrajetWithoutBusQuestion(n)) {
            if (isCountIntent(n)) {
                return Detection.of(AgentChatIntentType.COUNT_TRAJETS_WITHOUT_BUS);
            }
            return Detection.of(AgentChatIntentType.LIST_TRAJETS_WITHOUT_BUS);
        }
        if (isBusSansChauffeurQuestion(n)) {
            if (isCountIntent(n)) {
                return Detection.of(AgentChatIntentType.COUNT_BUSES_WITHOUT_CHAUFFEUR);
            }
            return Detection.of(AgentChatIntentType.LIST_BUSES_UNASSIGNED_CHAUFFEUR);
        }

        // --- 2) Candidatures en liste / attente (sans exiger « liste ») ---
        if (n.contains("candidature") && !isCountIntent(n)) {
            boolean wantList = hasListCue(n) || n.contains("attente") || n.contains("en attente");
            if (wantList) {
                if (n.contains("chauffeur") && !n.contains("enfant")) {
                    return Detection.of(AgentChatIntentType.LIST_PENDING_CHAUFFEUR_CANDIDATURES);
                }
                if (n.contains("enfant") && !n.contains("chauffeur")) {
                    return Detection.of(AgentChatIntentType.LIST_PENDING_ENFANT_CANDIDATURES);
                }
                return Detection.of(AgentChatIntentType.LIST_PENDING_CANDIDATURES_BOTH);
            }
        }

        // --- 3) Maîtresse d'un bus ---
        Matcher mnb = NOM_MAITRESSE_DU_BUS.matcher(raw);
        if (mnb.find()) {
            return Detection.bus(AgentChatIntentType.FIND_MAITRESSE_BY_BUS, mnb.group(1).trim());
        }
        mnb = MAITRESSE_DU_BUS.matcher(raw);
        if (mnb.find()) {
            return Detection.bus(AgentChatIntentType.FIND_MAITRESSE_BY_BUS, mnb.group(1).trim());
        }
        if (matchesAny(n,
                "maitresse.*bus",
                "bus.*maitresse",
                "qui.*maitresse.*bus",
                "quel.*maitresse.*bus",
                "nom.*maitresse.*bus")) {
            String tok = extractBusToken(raw, n);
            return Detection.bus(AgentChatIntentType.FIND_MAITRESSE_BY_BUS, tok);
        }

        Matcher mcf = NOM_CHAUFFEUR_DU_BUS.matcher(raw);
        if (mcf.find()) {
            return Detection.bus(AgentChatIntentType.FIND_CHAUFFEUR_BY_BUS, mcf.group(1).trim());
        }
        mcf = QUI_CHAUFFEUR_DU_BUS.matcher(raw);
        if (mcf.find()) {
            return Detection.bus(AgentChatIntentType.FIND_CHAUFFEUR_BY_BUS, mcf.group(1).trim());
        }
        mcf = CHAUFFEUR_DU_BUS.matcher(raw);
        if (mcf.find()) {
            return Detection.bus(AgentChatIntentType.FIND_CHAUFFEUR_BY_BUS, mcf.group(1).trim());
        }
        if (containsChauffeurWord(n) && matchesAny(n,
                "chauffeur.*bus",
                "conducteur.*bus",
                "qui.*chauffeur.*bus",
                "quel.*chauffeur.*bus",
                "nom.*chauffeur.*bus")) {
            String tok = extractBusToken(raw, n);
            return Detection.bus(AgentChatIntentType.FIND_CHAUFFEUR_BY_BUS, tok);
        }

        Detection enfBt = detectListEnfantsByBusOrTrajet(raw, n);
        if (enfBt != null) {
            return enfBt;
        }

        Detection enfRel = detectEnfantTrajetOrBusQuestion(raw, n);
        if (enfRel != null) {
            return enfRel;
        }

        // --- 4) Quel bus pour une personne (maîtresse / chauffeur nommé) ---
        Detection busForChauffeur = detectBusForNamedChauffeur(raw, n);
        if (busForChauffeur != null) {
            return busForChauffeur;
        }
        Detection busForTeacher = detectBusForNamedTeacher(raw, n);
        if (busForTeacher != null) {
            return busForTeacher;
        }

        // --- 5) Recherche maîtresse / enfant / bus ---
        if (matchesEnfantSearchCue(n)) {
            String nameQ = extractNameAfterEnfant(raw);
            if (nameQ == null || nameQ.isBlank()) {
                nameQ = stripLeadingQuestionWords(raw);
            }
            return Detection.name(AgentChatIntentType.FIND_ENFANT, nameQ != null ? nameQ : "");
        }
        if (matchesSearchVerb(n) && containsMaitresseWord(n)) {
            String nameQ = extractNameAfterMaitresse(raw);
            if (nameQ == null || nameQ.isBlank()) {
                nameQ = stripLeadingQuestionWords(raw);
            }
            return Detection.name(AgentChatIntentType.SEARCH_MAITRESSE, nameQ != null ? nameQ : "");
        }
        if (matchesSearchVerb(n) && n.contains("bus")) {
            String tok = extractBusToken(raw, n);
            if (tok == null || tok.isBlank()) {
                tok = tailAfterBusKeyword(raw);
            }
            return Detection.bus(AgentChatIntentType.SEARCH_BUS, tok);
        }

        // --- 6) Bus / chauffeur (affectation, autres formulations) ---
        if (matchesAny(n, "bus.*non affecte", "pas de chauffeur", "bus.*sans chauffeur")) {
            if (isCountIntent(n)) {
                return Detection.of(AgentChatIntentType.COUNT_BUSES_WITHOUT_CHAUFFEUR);
            }
            return Detection.of(AgentChatIntentType.LIST_BUSES_UNASSIGNED_CHAUFFEUR);
        }
        if (matchesAny(n, "bus.*affecte", "bus avec chauffeur", "bus assigne")) {
            if (isCountIntent(n)) {
                return Detection.of(AgentChatIntentType.COUNT_BUSES_WITH_CHAUFFEUR);
            }
            return Detection.of(AgentChatIntentType.LIST_BUSES_ASSIGNED_CHAUFFEUR);
        }

        // --- 7) Comptages ---
        if (isCountIntent(n)) {
            if (containsMaitresseWord(n)) {
                return Detection.of(AgentChatIntentType.COUNT_MAITRESSES);
            }
            if (containsChauffeurWord(n) && !n.contains("candidature")) {
                if (matchesAny(n, "non affecte", "sans bus", "non assigne", "pas affecte")) {
                    return Detection.of(AgentChatIntentType.COUNT_CHAUFFEURS_UNASSIGNED);
                }
                if (matchesAny(n, "affecte", "assigne", "sur un bus", "a un bus")) {
                    return Detection.of(AgentChatIntentType.COUNT_CHAUFFEURS_ASSIGNED);
                }
                return Detection.of(AgentChatIntentType.COUNT_CHAUFFEURS);
            }
            if (n.contains("enfant") && !n.contains("candidature")) {
                if (matchesAny(n, "not on board", "pas a bord", "ne sont pas a bord", "hors bord", "non a bord")) {
                    return Detection.of(AgentChatIntentType.COUNT_ENFANTS_OFF_BOARD);
                }
                if (matchesAny(n, "on board", "a bord", "abord")) {
                    return Detection.of(AgentChatIntentType.COUNT_ENFANTS_ON_BOARD);
                }
                if (matchesAny(n, "accept", "accepte", "acceptee")) {
                    return Detection.of(AgentChatIntentType.COUNT_ENFANTS_ACCEPTED_CANDIDATURES);
                }
                if (matchesAny(n, "transporte", "avec bus", "bus assigne")) {
                    return Detection.of(AgentChatIntentType.COUNT_ENFANTS_TRANSPORTED);
                }
                return Detection.of(AgentChatIntentType.COUNT_ENFANTS);
            }
            if (n.contains("trajet")) {
                return Detection.of(AgentChatIntentType.COUNT_TRAJETS);
            }
            if (n.contains("candidature") && n.contains("chauffeur")) {
                return Detection.of(AgentChatIntentType.COUNT_PENDING_CHAUFFEUR_CANDIDATURES);
            }
            if (n.contains("candidature") && n.contains("enfant")) {
                return Detection.of(AgentChatIntentType.COUNT_PENDING_ENFANT_CANDIDATURES);
            }
            if (n.contains("candidature") && n.contains("attente")) {
                return Detection.of(AgentChatIntentType.COUNT_PENDING_ENFANT_CANDIDATURES);
            }
            if (n.contains("bus")) {
                return Detection.of(AgentChatIntentType.COUNT_BUS);
            }
        }

        // --- 8) Listes : maitresses, trajets, bus, candidatures, chauffeurs, enfants ---
        if (hasListCue(n)) {
            if (n.contains("candidature")) {
                if (n.contains("chauffeur") && !n.contains("enfant")) {
                    return Detection.of(AgentChatIntentType.LIST_PENDING_CHAUFFEUR_CANDIDATURES);
                }
                if (n.contains("enfant") && !n.contains("chauffeur")) {
                    return Detection.of(AgentChatIntentType.LIST_PENDING_ENFANT_CANDIDATURES);
                }
                return Detection.of(AgentChatIntentType.LIST_PENDING_CANDIDATURES_BOTH);
            }
            if (containsChauffeurWord(n) && !n.contains("candidature")) {
                if (matchesAny(n, "non affecte", "sans bus", "non assigne", "pas affecte")) {
                    return Detection.of(AgentChatIntentType.LIST_CHAUFFEURS_UNASSIGNED);
                }
                if (matchesAny(n, "affecte", "assigne")) {
                    return Detection.of(AgentChatIntentType.LIST_CHAUFFEURS_ASSIGNED);
                }
                return Detection.of(AgentChatIntentType.LIST_CHAUFFEURS);
            }
            if (n.contains("enfant") && !n.contains("candidature")) {
                if (matchesAny(n, "sans trajet", "pas de trajet", "n ont pas de trajet")) {
                    return Detection.of(AgentChatIntentType.LIST_ENFANTS_WITHOUT_TRAJET);
                }
                if (matchesAny(n, "sans bus", "pas de bus", "pas de bus via", "n ont pas de bus")) {
                    return Detection.of(AgentChatIntentType.LIST_ENFANTS_WITHOUT_BUS);
                }
                if (matchesAny(n, "not on board", "pas a bord", "non a bord", "ne sont pas a bord")) {
                    return Detection.of(AgentChatIntentType.LIST_ENFANTS_OFF_BOARD);
                }
                if (matchesAny(n, "on board", "a bord", "abord")) {
                    return Detection.of(AgentChatIntentType.LIST_ENFANTS_ON_BOARD);
                }
                if (matchesAny(n, "accept", "accepte", "acceptee")) {
                    return Detection.of(AgentChatIntentType.LIST_ENFANTS_ACCEPTED_CANDIDATURES);
                }
                if (matchesAny(n, "transporte")) {
                    return Detection.of(AgentChatIntentType.LIST_ENFANTS_TRANSPORTED);
                }
                return Detection.of(AgentChatIntentType.LIST_ENFANTS);
            }
            if (containsMaitresseWord(n) && !n.contains("bus")) {
                return Detection.of(AgentChatIntentType.LIST_MAITRESSES);
            }
            if (n.contains("trajet")) {
                return Detection.of(AgentChatIntentType.LIST_TRAJETS);
            }
            if (n.contains("bus")) {
                return Detection.of(AgentChatIntentType.LIST_BUSES);
            }
        }

        // --- 9) Infos école ---
        if (matchesAny(n,
                "mon ecole",
                "notre ecole",
                "quelle est mon ecole",
                "nom de mon ecole",
                "informations sur mon ecole",
                "informations de mon ecole",
                "donne les informations de mon ecole",
                "infos sur mon ecole")) {
            return Detection.of(AgentChatIntentType.GENERAL_SCHOOL_INFO);
        }
        if (n.contains("ecole") && matchesAny(n, "information", "informations", "nom de", "quelle est")) {
            return Detection.of(AgentChatIntentType.GENERAL_SCHOOL_INFO);
        }

        return Detection.of(AgentChatIntentType.UNSUPPORTED);
    }

    private Detection detectListEnfantsByBusOrTrajet(String raw, String n) {
        if (!n.contains("enfant")) {
            return null;
        }
        boolean list = hasListCue(n) || Pattern.compile("quel(?:le)?s?\\s+enfants?").matcher(n).find();
        if (!list) {
            return null;
        }
        if (matchesAny(n, "sur le trajet", "du trajet", "dans le trajet", "sur trajet", "du trajet")) {
            return Detection.bus(AgentChatIntentType.LIST_ENFANTS_BY_TRAJET, extractTrajetToken(raw, n));
        }
        if (matchesAny(n, "dans le bus", "sur le bus", "du bus", "au bus")) {
            return Detection.bus(AgentChatIntentType.LIST_ENFANTS_BY_BUS, extractBusToken(raw, n));
        }
        if (n.contains("trajet") && !n.contains("bus")) {
            return Detection.bus(AgentChatIntentType.LIST_ENFANTS_BY_TRAJET, extractTrajetToken(raw, n));
        }
        if (n.contains("bus")) {
            return Detection.bus(AgentChatIntentType.LIST_ENFANTS_BY_BUS, extractBusToken(raw, n));
        }
        return null;
    }

    private static Detection detectEnfantTrajetOrBusQuestion(String raw, String n) {
        if (!n.contains("enfant")) {
            return null;
        }
        Matcher m = Pattern.compile(
                "(?i)sur\\s+quel\\s+trajet\\s+(?:est\\s+)?(?:l\\s*['’]?\\s*)?enfant\\s+(.+)")
                .matcher(raw.trim());
        if (m.find()) {
            return Detection.name(AgentChatIntentType.FIND_TRAJET_FOR_ENFANT, cleanNameCandidate(m.group(1)));
        }
        m = Pattern.compile("(?i)quel\\s+trajet\\s+(?:a|pour)\\s+(?:l\\s*['’]?\\s*)?enfant\\s+(.+)")
                .matcher(raw.trim());
        if (m.find()) {
            return Detection.name(AgentChatIntentType.FIND_TRAJET_FOR_ENFANT, cleanNameCandidate(m.group(1)));
        }
        m = Pattern.compile("(?i)quel\\s+bus\\s+(?:transporte|porte)\\s+(?:l\\s*['’]?\\s*)?enfant\\s+(.+)")
                .matcher(raw.trim());
        if (m.find()) {
            return Detection.name(AgentChatIntentType.FIND_BUS_FOR_ENFANT, cleanNameCandidate(m.group(1)));
        }
        m = Pattern.compile("(?i)dans\\s+quel\\s+bus\\s+(?:est\\s+)?(?:l\\s*['’]?\\s*)?enfant\\s+(.+)")
                .matcher(raw.trim());
        if (m.find()) {
            return Detection.name(AgentChatIntentType.FIND_BUS_FOR_ENFANT, cleanNameCandidate(m.group(1)));
        }
        return null;
    }

    private static Detection detectBusForNamedChauffeur(String raw, String n) {
        boolean role = containsChauffeurWord(n)
                || Pattern.compile("(?i)bus\\s+(?:de|du|pour)\\s+(?:le\\s+)?(?:chauffeur|conducteur)").matcher(raw)
                .find();
        if (!role) {
            return null;
        }
        Matcher m = Pattern.compile(
                "(?i)(?:quel|lequel)\\s+bus\\s+pour\\s+(?:le\\s+)?(?:chauffeur|conducteur)\\s+([\\p{L}0-9'’\\-\\s]{2,50})\\s*\\??")
                .matcher(raw);
        if (m.find()) {
            return Detection.name(AgentChatIntentType.FIND_BUS_FOR_CHAUFFEUR, cleanNameCandidate(m.group(1)));
        }
        m = Pattern.compile(
                "(?i)\\bbus\\s+de\\s+(?:le\\s+)?(?:chauffeur|conducteur)\\s+([\\p{L}0-9'’\\-\\s]{2,50})\\s*\\??")
                .matcher(raw);
        if (m.find()) {
            return Detection.name(AgentChatIntentType.FIND_BUS_FOR_CHAUFFEUR, cleanNameCandidate(m.group(1)));
        }
        m = Pattern.compile(
                "(?i)(?:quel|lequel)\\s+bus\\s+(?:est\\s+)?(?:affect[\\p{L}]*|assign[\\p{L}]*)\\s+(?:a|à|au)\\s+(?:le\\s+)?(?:chauffeur|conducteur)\\s+([\\p{L}0-9'’\\-\\s]{2,50})")
                .matcher(raw);
        if (m.find()) {
            return Detection.name(AgentChatIntentType.FIND_BUS_FOR_CHAUFFEUR, cleanNameCandidate(m.group(1)));
        }
        m = Pattern.compile(
                "(?i)([\\p{L}0-9'’\\-\\s]{2,40})\\s+est\\s+sur\\s+quel\\s+bus\\s*\\??")
                .matcher(raw);
        if (m.find()) {
            return Detection.name(AgentChatIntentType.FIND_BUS_FOR_CHAUFFEUR, cleanNameCandidate(m.group(1)));
        }
        m = Pattern.compile(
                "(?i)([\\p{L}0-9'’\\-\\s]{2,40})\\s+est\\s+(?:affect[\\p{L}]*|assign[\\p{L}]*)\\s+(?:a|à|au)\\s+quel\\s+bus\\s*\\??")
                .matcher(raw);
        if (m.find()) {
            return Detection.name(AgentChatIntentType.FIND_BUS_FOR_CHAUFFEUR, cleanNameCandidate(m.group(1)));
        }
        return null;
    }

    private static boolean containsChauffeurWord(String n) {
        return n.contains("chauffeur") || n.contains("conducteur");
    }

    private static boolean matchesEnfantSearchCue(String n) {
        if (!n.contains("enfant")) {
            return false;
        }
        if (matchesSearchVerb(n)) {
            return true;
        }
        if (Pattern.compile("informations?\\s+sur\\s+l\\s*enfant").matcher(n).find()) {
            return true;
        }
        if (n.contains("donne moi") || (Pattern.compile("\\bdonne\\b").matcher(n).find() && n.contains("enfant"))) {
            return true;
        }
        return false;
    }

    private static String extractNameAfterEnfant(String raw) {
        Matcher m = Pattern.compile("(?i)enfant\\s+(.+)").matcher(raw.trim());
        if (!m.find()) {
            return null;
        }
        String tail = m.group(1).trim().replaceFirst("^[\\p{Punct}\\s]+", "");
        int cut = indexOfIgnoreCase(tail, " du trajet");
        if (cut > 0) {
            tail = tail.substring(0, cut).trim();
        }
        cut = indexOfIgnoreCase(tail, "?");
        if (cut > 0) {
            tail = tail.substring(0, cut).trim();
        }
        tail = tail.replaceAll("(?i)^(la|le|les|un|une)\\s+", "").trim();
        if (tail.length() > 80) {
            tail = tail.substring(0, 80).trim();
        }
        return tail;
    }

    private String extractTrajetToken(String raw, String n) {
        Matcher m = Pattern.compile("(?i)trajet\\s*(?:n°|#|numero|numéro)?\\s*([A-Za-z0-9\\-]+)").matcher(raw);
        if (m.find()) {
            return m.group(1).trim();
        }
        Matcher m2 = Pattern.compile("\\b(\\d{1,4})\\b").matcher(raw);
        if (n.contains("trajet") && m2.find()) {
            return m2.group(1);
        }
        return null;
    }

    private static Detection detectBusForNamedTeacher(String raw, String n) {
        if (n.contains("sans maitresse") || n.contains("pas de maitresse")) {
            return null;
        }
        Matcher m = Pattern.compile(
                "(?i)([\\p{L}0-9'’\\-\\s]{2,40})\\s+est\\s+sur\\s+quel\\s+bus\\s*\\??")
                .matcher(raw);
        if (m.find()) {
            return Detection.name(AgentChatIntentType.FIND_BUS_FOR_MAITRESSE, cleanNameCandidate(m.group(1)));
        }
        m = Pattern.compile(
                "(?i)([\\p{L}0-9'’\\-\\s]{2,40})\\s+est\\s+(?:affect[\\p{L}]*|assign[\\p{L}]*)\\s+(?:a|à|au)\\s+quel\\s+bus\\s*\\??")
                .matcher(raw);
        if (m.find()) {
            return Detection.name(AgentChatIntentType.FIND_BUS_FOR_MAITRESSE, cleanNameCandidate(m.group(1)));
        }
        m = Pattern.compile(
                "(?i)(?:quel|lequel)\\s+bus\\s+pour\\s+([\\p{L}0-9'’\\-\\s]{2,50})\\s*\\??")
                .matcher(raw);
        if (m.find()) {
            return Detection.name(AgentChatIntentType.FIND_BUS_FOR_MAITRESSE, cleanNameCandidate(m.group(1)));
        }
        m = Pattern.compile(
                "(?i)\\bbus\\s+de\\s+([\\p{L}0-9'’\\-\\s]{2,50})\\s*\\??")
                .matcher(raw);
        if (m.find()) {
            String cand = cleanNameCandidate(m.group(1));
            if (!isBusDeStopwordName(cand)) {
                return Detection.name(AgentChatIntentType.FIND_BUS_FOR_MAITRESSE, cand);
            }
        }
        m = Pattern.compile(
                "(?i)(?:quel|lequel)\\s+bus\\s+(?:est\\s+)?(?:affect[\\p{L}]*|assign[\\p{L}]*)\\s+(?:a|à|au)\\s+([\\p{L}0-9'’\\-\\s]{2,50})")
                .matcher(raw);
        if (m.find()) {
            return Detection.name(AgentChatIntentType.FIND_BUS_FOR_MAITRESSE, cleanNameCandidate(m.group(1)));
        }
        m = Pattern.compile(
                "(?i)(?:quel|lequel)\\s+bus\\s+a\\s+([\\p{L}0-9'’\\-\\s]{2,40})\\s*\\??")
                .matcher(raw);
        if (m.find()) {
            return Detection.name(AgentChatIntentType.FIND_BUS_FOR_MAITRESSE, cleanNameCandidate(m.group(1)));
        }
        m = Pattern.compile(
                "(?i)bus\\s+(?:est\\s+)?(?:affect[\\p{L}]*|assign[\\p{L}]*)\\s+(?:a|à|au)\\s+([\\p{L}0-9'’\\-\\s]{2,50})")
                .matcher(raw);
        if (m.find() && !containsMaitresseWord(AgentChatTextNormalizer.forMatching(m.group(1)))) {
            return Detection.name(AgentChatIntentType.FIND_BUS_FOR_MAITRESSE, cleanNameCandidate(m.group(1)));
        }
        m = Pattern.compile(
                "(?i)([\\p{L}0-9'’\\-\\s]{2,40})\\s+est\\s+(?:affect[\\p{L}]*|assign[\\p{L}]*)\\s+(?:a|à|au)\\s+quel\\s+bus")
                .matcher(raw);
        if (m.find()) {
            return Detection.name(AgentChatIntentType.FIND_BUS_FOR_MAITRESSE, cleanNameCandidate(m.group(1)));
        }
        if (matchesAny(n, "quel bus.*maitresse", "bus.*maitresse", "affect.*maitresse")) {
            String nameQ = extractNameAfterMaitresse(raw);
            if (nameQ != null && !nameQ.isBlank()) {
                return Detection.name(AgentChatIntentType.FIND_BUS_FOR_MAITRESSE, nameQ);
            }
        }
        return null;
    }

    private static boolean isBusDeStopwordName(String name) {
        if (name == null || name.isBlank()) {
            return true;
        }
        String[] parts = name.trim().split("\\s+");
        return parts.length == 0 || BUS_DE_NAME_STOPWORDS.contains(parts[0].toLowerCase(Locale.ROOT));
    }

    private static String cleanNameCandidate(String s) {
        if (s == null) {
            return "";
        }
        String t = s.trim().replaceAll("[?!.:;]+$", "").trim();
        if (t.length() > 60) {
            t = t.substring(0, 60).trim();
        }
        return t;
    }

    private static boolean isBusSansChauffeurQuestion(String n) {
        return n.contains("bus") && n.contains("sans chauffeur");
    }

    private static boolean isBusWithoutMaitresseQuestion(String n) {
        if (!n.contains("bus")) {
            return false;
        }
        if (n.contains("sans maitresse")) {
            return true;
        }
        if (n.contains("pas de maitresse")) {
            return true;
        }
        if (n.contains("n ont pas de maitresse") || n.contains("nont pas de maitresse")) {
            return true;
        }
        return Pattern.compile("n\\s+ont pas de maitresse").matcher(n).find();
    }

    private static boolean isTrajetWithoutBusQuestion(String n) {
        if (!n.contains("trajet")) {
            return false;
        }
        if (n.contains("sans bus") || n.contains("pas de bus")) {
            return true;
        }
        if (n.contains("n ont pas de bus") || n.contains("nont pas de bus")) {
            return true;
        }
        return Pattern.compile("n\\s+ont pas de bus").matcher(n).find();
    }

    private static boolean isCountIntent(String n) {
        if (n.startsWith("combien")) {
            return true;
        }
        if (n.contains(" combien ") || n.endsWith(" combien")) {
            return true;
        }
        if (Pattern.compile("combien\\s+y\\s*a\\s*-?\\s*t\\s*-?\\s*il").matcher(n).find()) {
            return true;
        }
        if (n.startsWith("nombre ") || n.contains(" nombre de ") || n.contains("nombre de ")) {
            return true;
        }
        if (Pattern.compile("combien\\s+.*\\s+avons").matcher(n).find()
                || Pattern.compile("combien\\s+.*\\s+avez").matcher(n).find()) {
            return true;
        }
        return false;
    }

    private static boolean hasListCue(String n) {
        if (Pattern.compile("\\b(liste|lister)\\b").matcher(n).find()) {
            return true;
        }
        if (Pattern.compile("\\b(afficher|affiche|montre|voir)\\b").matcher(n).find()) {
            return true;
        }
        if (n.contains("donne moi") || Pattern.compile("\\bdonne\\b").matcher(n).find()) {
            return true;
        }
        if (Pattern.compile("\\bquels\\b").matcher(n).find() || Pattern.compile("\\bquelles\\b").matcher(n).find()) {
            return true;
        }
        if (Pattern.compile("quelles?\\s+sont\\s+les").matcher(n).find()) {
            return true;
        }
        return false;
    }

    private static boolean matchesSearchVerb(String n) {
        return Pattern.compile("\\b(cherche|recherche|trouve|trouver)\\b").matcher(n).find();
    }

    private static boolean containsMaitresseWord(String n) {
        return n.contains("maitresse");
    }

    private static boolean containsAnyLiteral(String n, String... literals) {
        for (String lit : literals) {
            if (n.contains(lit)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesAny(String n, String... regexes) {
        for (String rx : regexes) {
            if (Pattern.compile(rx).matcher(n).find()) {
                return true;
            }
        }
        return false;
    }

    private String extractBusToken(String raw, String normalized) {
        Matcher m = BUS_LABELED.matcher(raw);
        if (m.find()) {
            return m.group(1).trim();
        }
        Matcher m2 = Pattern.compile("\\b(\\d{1,4})\\b").matcher(raw);
        if (normalized.contains("bus") && m2.find()) {
            return m2.group(1);
        }
        return null;
    }

    private static String tailAfterBusKeyword(String raw) {
        String t = raw.replaceFirst("(?i)^.*?(cherche|recherche|trouve|trouver)\\s+(?:un\\s+|le\\s+|la\\s+)?bus\\s*", "").trim();
        t = t.replaceAll("[?!.:;]+$", "").trim();
        return t.isBlank() ? null : t;
    }

    private static String extractNameAfterMaitresse(String raw) {
        Matcher m = Pattern.compile("(?i)ma[iî]tresse\\s+(.+)").matcher(raw.trim());
        if (!m.find()) {
            return null;
        }
        String tail = m.group(1).trim().replaceFirst("^[\\p{Punct}\\s]+", "");
        int cut = indexOfIgnoreCase(tail, " du bus");
        if (cut > 0) {
            tail = tail.substring(0, cut).trim();
        }
        cut = indexOfIgnoreCase(tail, "?");
        if (cut > 0) {
            tail = tail.substring(0, cut).trim();
        }
        tail = tail.replaceAll("(?i)^(la|le|les|un|une)\\s+", "").trim();
        if (tail.length() > 80) {
            tail = tail.substring(0, 80).trim();
        }
        return tail;
    }

    private static int indexOfIgnoreCase(String s, String sub) {
        return s.toLowerCase(Locale.ROOT).indexOf(sub.toLowerCase(Locale.ROOT));
    }

    private static String stripLeadingQuestionWords(String raw) {
        String t = raw.replaceFirst("(?i)^(combien|quel|quelle|quels|quelles|donne|donner|affiche|liste|cherche|recherche|trouve|trouver)\\s+[^\\s]+\\s*", "");
        t = t.replaceFirst("(?i)^(de|des|du|la|le|les|une|un)\\s+", "");
        return t.trim();
    }
}
