package tn.esprit.workshop.security.Talel;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import tn.esprit.workshop.model.Talel.talel2.User;

import java.security.Key;
import java.util.Date;

public class JwtUtil {

    // Clé secrète sécurisée (minimum 256 bits pour HS256)
    private static final String SECRET_KEY = "MY_secret";
    private static final Key KEY = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    // Générer token pour un utilisateur
    public static String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail()) // email comme sub
                .claim("userId", user.getId())
                .claim("nom", user.getNom())
                .claim("categorie", user.getCategories().toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600_000)) // 1h
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    // Valider token et récupérer claims
    public static Claims validateToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}


