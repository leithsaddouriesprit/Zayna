package tn.esprit.workshop.utilis;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Couche optionnelle : API publique PurgoMalum (containsprofanity). Jamais obligatoire : en cas d’erreur ou timeout,
 * retourne {@code false} pour ne pas bloquer (le filtre local a déjà été appliqué avant).
 */
final class OptionalPurgoMalumClient {

    private static final Logger LOG = Logger.getLogger(OptionalPurgoMalumClient.class.getName());
    private static final String ENDPOINT = "https://www.purgomalum.com/service/containsprofanity?text=";
    private static final int CONNECT_MS = 1200;
    private static final int READ_MS = 1200;

    private OptionalPurgoMalumClient() {
    }

    static boolean containsProfanityRemote(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        String trimmed = text.length() > 800 ? text.substring(0, 800) : text;
        HttpURLConnection c = null;
        try {
            String url = ENDPOINT + URLEncoder.encode(trimmed, StandardCharsets.UTF_8);
            c = (HttpURLConnection) new URL(url).openConnection();
            c.setConnectTimeout(CONNECT_MS);
            c.setReadTimeout(READ_MS);
            c.setRequestMethod("GET");
            c.setInstanceFollowRedirects(true);
            int code = c.getResponseCode();
            if (code != 200) {
                return false;
            }
            String body = readAll(c.getInputStream()).trim().toLowerCase(Locale.ROOT);
            return "true".equals(body);
        } catch (IOException e) {
            LOG.log(Level.FINE, "PurgoMalum indisponible, ignoré", e);
            return false;
        } finally {
            if (c != null) {
                c.disconnect();
            }
        }
    }

    private static String readAll(InputStream in) throws IOException {
        byte[] buf = in.readAllBytes();
        return new String(buf, StandardCharsets.UTF_8);
    }
}
