package com.officecollab.documentcore.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Signs and verifies HS256 JWTs using the shared secret configured on the ONLYOFFICE Document Server side
 * (JWT_SECRET env var there / ${ONLYOFFICE_JWT_SECRET} here). Used for two distinct things:
 *   1. Signing the editor config we hand back from GET /documents/{id}/editor-config (the "token" field
 *      ONLYOFFICE's API.js expects alongside the config).
 *   2. Verifying the JWT ONLYOFFICE attaches to its save callback, so an unauthenticated caller can't spoof a
 *      callback and overwrite a document.
 * This is a completely separate trust boundary from the Supabase user JWTs Spring Security validates on
 * /api/v1/** -- this secret is shared only with the ONLYOFFICE Document Server, never with browser clients.
 */
@Service
public class OnlyOfficeJwtService {

    private final SecretKey key;

    public OnlyOfficeJwtService(@Value("${onlyoffice.jwt.secret}") String secret) {
        if (!StringUtils.hasText(secret)) {
            // Deliberately not failing startup: this lets the app boot in environments where ONLYOFFICE
            // integration isn't configured yet (e.g. a first-run POC). Any attempt to actually sign an
            // editor config or verify a callback will fail loudly instead, which is what we want.
            this.key = null;
        } else {
            this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        }
    }

    public String sign(Map<String, Object> claims) {
        requireKey();
        return Jwts.builder()
                .claims(claims)
                .signWith(key)
                .compact();
    }

    /** Throws JwtException (caught by the callback controller and turned into a 403) if the token is invalid. */
    public Claims verify(String token) {
        requireKey();
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private void requireKey() {
        if (key == null) {
            throw new JwtException("ONLYOFFICE_JWT_SECRET is not configured");
        }
    }
}
