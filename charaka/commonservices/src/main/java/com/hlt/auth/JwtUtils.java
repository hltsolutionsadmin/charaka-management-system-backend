package com.hlt.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.hlt.commonservice.dto.LoggedInUser;
import com.hlt.commonservice.enums.ERole;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class JwtUtils {
    private static final String ROLES = "roles";

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    private static final String jwtSecret = "BXNh8+ay7pPm9IhFP1PdLle3VCQ5QzDXJ0bdzITCkp3U2aZKmldRbD8B4qfMLWvj";
    private final long jwtExpirationMs = 5L * 60 * 60 * 1000;// 5 hour

    private final long systemUserJWTExpirationMs = 60L * 60 * 1000;
    private final long refreshTokeExpirationInDays = 30L * 24 * 60 * 60 * 1000; // 30 days

    public String generateJwtToken(LoggedInUser loggedInUserDetails) throws JsonProcessingException {
        String json = getJsonFromLoggedInUser(loggedInUserDetails);

        Set<String> roleNames = loggedInUserDetails.getRoles().stream()
                .map(role -> role.toString()) // Use toString() to get the name
                .collect(Collectors.toSet());
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setSubject(json)
                .claim("version", loggedInUserDetails.getVersion())
                .addClaims(Map.of(ROLES, roleNames))
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public String getSystemUserToken() {
        String systemUser = "{"
                + "\"id\": 1111111111111111,"
                + "\"password\": \"password123\","
                + "\"fullName\": \"eato\","
                + "\"roles\": [\"ROLE_SYSTEM_USER\"],"
                + "\"primaryContact\": \"9100881724\""
                + "}";
        return generateSystemUserJwtToken(systemUser);
    }

    public boolean isTokenVersionValid(String token, int userVersion) {
        int tokenVersion = getTokenVersion(token);
        return tokenVersion == userVersion;
    }

    public int getTokenVersion(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token).getPayload();
        return (int) claims.get("version");
    }

    private String generateSystemUserJwtToken(String systemUser) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .setSubject(systemUser)
                .addClaims(Map.of(ROLES, Set.of(ERole.ROLE_SUPER_ADMIN)))
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + systemUserJWTExpirationMs))
                .signWith(SignatureAlgorithm.HS512, key)
                .compact();
    }

    private long getExpiration(int expiryInDays) {
        return new Date().toInstant()
                .plus(expiryInDays, ChronoUnit.DAYS)
                .toEpochMilli();
    }

    public String generateRefreshToken(LoggedInUser loggedInUserDetails) throws JsonProcessingException {
        String json = getJsonFromLoggedInUser(loggedInUserDetails);
        Set<String> roleNames = loggedInUserDetails.getRoles().stream()
                .map(Object::toString)
                .collect(Collectors.toSet());

        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setSubject(json)
                .claim("version", loggedInUserDetails.getVersion())
                .setIssuedAt(new Date())
                .addClaims(Map.of(ROLES, roleNames))
                .setExpiration(new Date((new Date()).getTime() + refreshTokeExpirationInDays))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    private String getJsonFromLoggedInUser(LoggedInUser loggedInUserDetails) throws JsonProcessingException {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        return ow.writeValueAsString(loggedInUserDetails);
    }

    public boolean validateJwtToken(String authToken) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Jwts.parser()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(authToken);
            return true;
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public LoggedInUser getUserFromToken(String token) throws JsonProcessingException {
        // Convert jwtSecret to SecretKey
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        // Parse the JWT
        Jws<Claims> claimsJws = Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);

        List<String> roles = (List<String>) claimsJws.getBody().get(ROLES);

        // Deserialize LoggedInUser from token
        LoggedInUser loggedInUser = new LoggedInUser();
        if (claimsJws.getBody().getSubject() != null) {
            ObjectMapper mapper = new ObjectMapper();
            loggedInUser = mapper.readValue(claimsJws.getBody().getSubject(), LoggedInUser.class);
        }

        loggedInUser.setRoles(new HashSet<>(roles));
        return loggedInUser;
    }
}
