package com.roomies.security;

import io.jsonwebtoken.*;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.security.Keys;
import java.security.Key;

import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

  @Value("${jwt.access.secret}")
  private String secretKey;
  @Value("${jwt.refresh.secret}")
  private String refreshSecretKey;
  private static final long EXPIRATION_MS = 60L * 60 * 1000; // 1 hour
  private static final long REFRESH_TOKEN_EXPIRATION = 1000L * 60 * 60 * 24 * 7; // 7 days

  public String generateToken(String username) {
    return Jwts.builder()
        .setSubject(username)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
        .signWith(getSigningKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  public boolean isAccessTokenValid(String token, String username) {
    return username.equals(extractUsernameFromAccessToken(token)) && !isAccessTokenExpired(token);
  }

  private boolean isAccessTokenExpired(String token) {
    return extractAccessClaim(token, Claims::getExpiration).before(new Date());
  }

  public String extractUsernameFromAccessToken(String token) {
    return extractAccessClaim(token, Claims::getSubject);
  }

  private <T> T extractAccessClaim(String token, Function<Claims, T> resolver) {
    return resolver.apply(getAccessClaims(token));
  }

  private Claims getAccessClaims(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(getSigningKey())
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  private Key getSigningKey() {
    byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  public String generateRefreshToken(String email) {
    return Jwts.builder()
        .setSubject(email)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
        .signWith(getRefreshSigningKey(), SignatureAlgorithm.HS256)
        .compact();
  }


  public boolean isRefreshTokenValid(String token, String username) {
    return username.equals(extractUsernameFromRefreshToken(token)) &&
        !isRefreshTokenExpired(token);
  }

  private boolean isRefreshTokenExpired(String token) {
    return extractRefreshClaim(token, Claims::getExpiration).before(new Date());
  }

  public String extractUsernameFromRefreshToken(String token) {
    return extractRefreshClaim(token, Claims::getSubject);
  }

  public <T> T extractRefreshClaim(String token, Function<Claims, T> resolver) {
    return resolver.apply(getRefreshClaims(token));
  }

  private Claims getRefreshClaims(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(getRefreshSigningKey())
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  private Key getRefreshSigningKey() {
    byte[] keyBytes = refreshSecretKey.getBytes(StandardCharsets.UTF_8);
    return Keys.hmacShaKeyFor(keyBytes);
  }
}
